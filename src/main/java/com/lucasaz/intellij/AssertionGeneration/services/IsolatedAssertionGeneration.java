package com.lucasaz.intellij.AssertionGeneration.services;

import com.lucasaz.intellij.AssertionGeneration.exceptions.PluginException;
import com.lucasaz.intellij.AssertionGeneration.model.task.Task;
import com.lucasaz.intellij.AssertionGeneration.util.Util;
import org.json.JSONArray;
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

    public static String generateAssertions(int lineNum, String selected, String testFile, Task task) throws PluginException {
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
            String assertions = IsolatedAssertionGeneration.scuffedGenAssertions(resObj, selected, whitespace);
            task.removeVolume(id);
            return assertions;
        } catch (PluginException | IOException | JSONException e) {
            System.out.println(e.getMessage());
            task.removeVolume(id);
            return IsolatedAssertionGeneration.makeFailCaseResponse(testFile, lineNum);
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

    public static String scuffedGenAssertions(JSONObject observed, String name, String ws) {
        String type = (String) observed.get("type");
        String lsp = "\n"; // System.getProperty("line.separator");
        String val;
        StringBuilder toReturn = new StringBuilder();
        switch (type) {
            case "boolean":
            case "number":
                val = observed.get("value").toString();
                toReturn.append(ws).append("expect(varName).to.exist; // Exist means neither null nor undefined").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(resType); // Typecheck similar to typeof operator").append(lsp);
                toReturn.append(ws).append("expect(varName).to.equal(").append(val).append("); // Value equality check").append(lsp);
                break;
            case "string": // Only diff is the quotes in the deep equal
                val = (String) observed.get("value");
                toReturn.append(ws).append("expect(varName).to.exist; // Exist means neither null nor undefined").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(resType); // Typecheck similar to typeof operator").append(lsp);
                toReturn.append(ws).append("expect(varName).to.equal(\"").append(val).append("\"); // Value equality check").append(lsp);
                break;
            case "symbol":
                val = (String) observed.get("value");
                toReturn.append(ws).append("expect(varName).to.exist; // Exist means neither null nor undefined").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(resType); // Typecheck similar to typeof operator").append(lsp);
                toReturn.append(ws).append("expect(varName.toString()).to.equal(\"").append(val).append("\"); // Value equality check").append(lsp);
                break;
            case "function":
                toReturn.append(ws).append("expect(varName).to.exist; // Exist means neither null nor undefined").append(lsp);
                toReturn.append(ws).append("expect(varName).to.ba.a(resType); // Typecheck similar to typeof operator").append(lsp);
                break;
            case "null":
                toReturn.append(ws).append("expect(varName).to.be.null; // Equality check to null").append(lsp);
                break;
            case "undefined":
                toReturn.append(ws).append("expect(varName).to.be.undefined; // Typecheck like typeof for 'undefined'").append(lsp);
                break;
            case "array":
                JSONArray arr = (JSONArray) observed.get("value");
                val = arr.toString();
                toReturn.append(ws).append("expect(varName).to.exist; // Exist means neither null nor undefined").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(resType); // Typecheck similar to typeof operator").append(lsp);
                toReturn.append(ws).append("expect(varName).to.deep.equal(").append(val).append("); // \"deep\" keyword for value equality").append(lsp);
                break;
            case "set":
                JSONArray setArr = (JSONArray) observed.get("value");
                val = setArr.toString();
                toReturn.append(ws).append("expect(varName).to.exist; // Exist means neither null nor undefined").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(\"Set\"); // Typecheck similar to typeof operator").append(lsp); // hardcoding set for caps
                toReturn.append(ws).append("expect(Array.from(varName)).to.deep.equal(").append(val).append("); // \"deep\" keyword for value equality").append(lsp); // No idea if this works
                break;
            case "promise":
                toReturn.append(ws).append("expect(varName).to.exist; // Exist means neither null nor undefined").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(\"promise\"); // Typecheck similar to typeof operator").append(lsp);
                break;
            case "error":
                toReturn.append(ws).append("expect(varName).to.exist; // Exist means neither null nor undefined").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(\"error\"); // Typecheck similar to typeof operator").append(lsp);
                break;
            case "object":
                JSONObject subObj = (JSONObject) observed.get("value");
                val = subObj.toString();
                toReturn.append(ws).append("expect(varName).to.exist; // Exist means neither null nor undefined").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(resType); // Typecheck similar to typeof operator").append(lsp);
                toReturn.append(ws).append("expect(varName).to.deep.equal(").append(val).append("); // \"deep\" keyword for value equality").append(lsp);
                break;
            default:
                // Should never happen
                toReturn.append(ws).append("// Assertion generation failed, please make sure you selected the appropriate").append(lsp);
        }
        String out = toReturn.toString();
        out = out.replaceAll("varName", name);
        return out.replaceAll("resType", "\"" + type + "\"");
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
