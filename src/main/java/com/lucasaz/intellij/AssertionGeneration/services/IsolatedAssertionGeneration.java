package com.lucasaz.intellij.AssertionGeneration.services;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionSelector;
import com.lucasaz.intellij.AssertionGeneration.exceptions.PluginException;
import com.lucasaz.intellij.AssertionGeneration.model.AssertionGenerationResponse;
import com.lucasaz.intellij.AssertionGeneration.model.task.Task;
import com.lucasaz.intellij.AssertionGeneration.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IsolatedAssertionGeneration {
    private static final String logFile = ".testOutput";

    public static AssertionGenerationResponse generateAssertions(int lineNum, String selected, String testFile, Task task) throws PluginException {
        long id = 0; // Hardcode this so we don't need to make changes
        try {
            // Make new instrumented file content
            String singleTestOnly = IsolatedAssertionGeneration.addOnlyToTargetTest(testFile, lineNum);
            String newText = Util.spliceInto(singleTestOnly, IsolatedAssertionGeneration.createInjectionStringList(selected), lineNum);

            // Write to disk
            System.out.println("Initializing volume");
            task.initVolume(id, newText);

            // Build
            System.out.println("Building new container");
            Docker.runContainer(id, task);

            // Read logged output
            String result = Util.pathToFileContent(Paths.get(task.getOutputFilePath(id)));
            System.out.println("Test run output loaded");
            System.out.println(result.toString());

            // Make assertions + final file
            String whitespace = IsolatedAssertionGeneration.getWhitespaceFromLine(testFile, lineNum);
            JSONObject resObj = new JSONObject(result);
            boolean verbose = true;
            String assertions = AssertionSelector.getAllAssertions(resObj, selected, whitespace, verbose);
            task.removeVolume(id);
            // Probably a better way to do this but that's for a time when we have time
            String val;
            boolean failState;
            if (resObj.getString("type") == "fail") {
                failState = true;
                val = resObj.getString("reason");
            } else {
                failState = false;
                val = "No failure";
            }
            return new AssertionGenerationResponse(assertions, resObj.getBoolean("hasDiff"), failState, val);
        } catch (PluginException | IOException | JSONException e) {
            System.out.println(e.getMessage());
            task.removeVolume(id);
            return new AssertionGenerationResponse(IsolatedAssertionGeneration.makeFailCaseResponse(testFile, lineNum), false, true, e.getMessage());
        }
    }

    private static String makeFailCaseResponse(String fileContent, int lineNumber) throws PluginException {
        String whitespace = IsolatedAssertionGeneration.getWhitespaceFromLine(fileContent, lineNumber);
        return whitespace + "// Failed to generate assertions\n";
    }

    private static String getWhitespaceFromLine(String fileContent, int lineNumber) {
        String[] lines = fileContent.split("\n");
        String line = lines[lineNumber];
        return Util.getWhitespace(line);
    }

    private static String createInjectionStringList(String varName) throws IOException
    {
        InputStream is = IsolatedAssertionGeneration.class.getClassLoader().getResourceAsStream("injection.js");
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String recordingFunction = br.lines().collect(Collectors.joining("\n"));
        String save = "const longVarNameToNotClash = elemUnderTestGenerator(" + varName + ");";
        String logRemoveMe = "console.log(__dirname + \"/\" + \"" + IsolatedAssertionGeneration.logFile + "\")";
        String log = "require(\"fs\").writeFileSync(__dirname + \"/\" + \"" + IsolatedAssertionGeneration.logFile + "\", JSON.stringify(longVarNameToNotClash));";
        List<String> res = new ArrayList<>();
        res.add(recordingFunction);
        res.add(logRemoveMe);
        res.add(save);
        res.add(log);
        return Util.createString(res);
    }

    public static String addOnlyToTargetTest(String file, int lineNum) {
        List<String> lines = Util.toLines(file);
        Pattern pattern = Pattern.compile("^(\\s)*it([ ]?)\\(.*$");
        for (int i = lineNum; i >= 0; i--) {
            String line = lines.get(i);
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                line = line.replaceFirst("it", "it.only");
                lines.set(i, line);
                break;
            }
        }
        return Util.createString(lines);
    }
}
