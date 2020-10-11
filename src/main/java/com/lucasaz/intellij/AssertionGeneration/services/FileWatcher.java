package com.lucasaz.intellij.AssertionGeneration.services;

import com.intellij.openapi.application.NonBlockingReadAction;
import com.intellij.openapi.application.ReadAction;
import com.intellij.util.concurrency.NonUrgentExecutor;
import com.lucasaz.intellij.AssertionGeneration.util.Util;
import com.lucasaz.intellij.AssertionGeneration.dto.Selected;
import com.lucasaz.intellij.AssertionGeneration.exceptions.PluginException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileWatcher extends Thread implements Runnable {
    private Selected selected;
    private Path path;
    private AtomicBoolean stop;
    private WatchService watcher;
    private static FileWatcher instance = null;

    public static FileWatcher getInstance(Selected selected, WatchService watcher) {
        if (FileWatcher.instance != null) {
            instance.stopRequeue();
        }
        FileWatcher.instance = new FileWatcher(selected, watcher);
        return FileWatcher.instance;
    }

    private FileWatcher(Selected selected, WatchService watcher) {
            this.selected = selected;
            this.path = Paths.get(selected.getTsFilePath());
            this.stop = new AtomicBoolean(false);
            this.watcher = watcher;
    }

    public void stopRequeue() { stop.set(true); }

    @Override
    public void run() {
        try {
            WatchKey key;
            key = this.watcher.poll();
            if (key == null && !this.stop.get()) {
                reAdd();
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();

                if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
                        && filename.toString().equals(Util.OUTFILE)) {
                    Thread.sleep(20); // Make sure write is finished
                    this.onFileCreate();
                    return;
                }
            }
            boolean valid = key.reset();
            if (valid && !this.stop.get()) {
                reAdd();
            }
        } catch (Throwable e) {
            System.err.println("Error in FileWatcher");
            System.err.println(e.getMessage());
        }
    }

    private void reAdd() {
        NonBlockingReadAction<Void> res = ReadAction.nonBlocking(this);
        res.submit(NonUrgentExecutor.getInstance());
    }

    private void onFileCreate() {
        Path testOutputPath = Paths.get(this.path.getParent().toString(), Util.OUTFILE);
        String runFile = Util.makeBackgroundFilename(this.selected.getTsFilePath());
        String runResult;
        try {
            runResult = Util.pathToFileContent(testOutputPath);
        } catch (IOException err) {
            System.err.println("Failed to read run output");
            System.err.println(err.getMessage());
            return;
        }

        JSONObject res = new JSONObject(runResult);

        try {
            String[] oldFiles = {runFile, testOutputPath.toString()};
            Util.cleanup(oldFiles);
            String wouldBeAssertedFile = this.makeFinalFile(res);
            FileWriter fw = new FileWriter(this.path.toString());
            fw.write(wouldBeAssertedFile);
            fw.close();
        } catch (IOException | PluginException err) {
            System.err.println("Error adding assertions after test run");
            System.err.println(err.getMessage());
        }
    }

    private String makeFinalFile(JSONObject observed) throws PluginException {
        String assertions = this.scuffedGenAssertions(observed);
        return Util.spliceInto(this.selected.getOriginalFile(), assertions, this.selected.getLine());
    }

    private String scuffedGenAssertions(JSONObject observed) {
        String name = this.selected.getSelected();
        String ws = this.selected.getWhitespace();
        String type = (String) observed.get("type");
        String lsp = System.getProperty("line.separator");
        String val;
        StringBuilder toReturn = new StringBuilder();
        switch (type) {
            case "boolean":
            case "number":
                val = observed.get("value").toString();
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(resType);").append(lsp);
                toReturn.append(ws).append("expect(varName).to.equal(").append(val).append(");").append(lsp);
                break;
            case "string": // Only diff is the quotes in the deep equal
                val = (String) observed.get("value");
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(resType);").append(lsp);
                toReturn.append(ws).append("expect(varName).to.equal(\"").append(val).append("\");").append(lsp);
                break;
            case "symbol":
                val = (String) observed.get("value");
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(resType);").append(lsp);
                toReturn.append(ws).append("expect(varName.toString()).to.equal(\"").append(val).append("\");").append(lsp);
                break;
            case "function":
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(resType);").append(lsp);
                int numArgs = (int) observed.get("args");
                if (numArgs == 0) {
                    boolean throwsBool = (boolean) observed.get("throws");
                    if (throwsBool) {
                        toReturn.append(ws).append("expect(varName).to.throw;").append(lsp);
                    } else {
                        toReturn.append(ws).append("expect(varName).to.not.throw;").append(lsp);
                    }
                }
                break;
            case "null":
                toReturn.append(ws).append("expect(varName).to.be.null;").append(lsp);
                break;
            case "undefined":
                toReturn.append(ws).append("expect(varName).to.be.undefined;").append(lsp);
                break;
            case "array":
                JSONArray arr = (JSONArray) observed.get("value");
                val = arr.toString();
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(resType);").append(lsp);
                toReturn.append(ws).append("expect(varName).to.deep.equal(").append(val).append(");").append(lsp);
                break;
            case "set":
                JSONArray setArr = (JSONArray) observed.get("value");
                val = setArr.toString();
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(\"Set\");").append(lsp); // hardcoding set for caps
                toReturn.append(ws).append("expect(Array.from(varName)).to.deep.equal(").append(val).append(");").append(lsp); // No idea if this works
                break;
            case "promise":
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(\"promise\");").append(lsp);
                break;
            case "error":
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(\"error\");").append(lsp);
                break;
            case "object":
                JSONObject subObj = (JSONObject) observed.get("value");
                val = subObj.toString();
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(varName).to.be.a(resType);").append(lsp);
                toReturn.append(ws).append("expect(varName).to.deep.equal(").append(val).append(");").append(lsp);
                break;
            case "fail":
                toReturn.append(ws).append("// Assertion generation timed out");
                break;
            default:
                // Should never happen
                toReturn.append(ws).append("// Assertion generation failed, please make sure you selected the appropriate").append(lsp);
        }
        String out = toReturn.toString();
        out = out.replaceAll("varName", name);
        return out.replaceAll("resType", "\"" + type + "\"");
    }
}