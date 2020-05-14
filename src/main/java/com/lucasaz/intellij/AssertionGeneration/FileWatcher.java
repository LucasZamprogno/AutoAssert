package com.lucasaz.intellij.AssertionGeneration;

import com.intellij.openapi.application.NonBlockingReadAction;
import com.intellij.openapi.application.ReadAction;
import com.intellij.util.concurrency.NonUrgentExecutor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class FileWatcher extends Thread implements Runnable {
    private Selected selected;
    private Path path;
    private String target;
    private AtomicBoolean stop;
    private WatchService watcher;
    private static FileWatcher instance = null;

    public static FileWatcher getInstance(Selected selected, String target, WatchService watcher) {
        if (FileWatcher.instance != null) {
            instance.stopRequeue();
        }
        FileWatcher.instance = new FileWatcher(selected, target, watcher);
        return FileWatcher.instance;

    }

    private FileWatcher(Selected selected, String target, WatchService watcher) {
            this.selected = selected;
            this.path = Paths.get(selected.tsFilePath);
            this.target = target;
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
                        && filename.toString().equals(this.target)) {
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
        Path filepath = Paths.get(this.path.getParent().toString(), this.target);
        String runResult;
        try {
            runResult = Util.pathToFileContent(filepath);
        } catch (IOException err) {
            System.err.println("Failed to read run output");
            System.err.println(err.getMessage());
            return;
        }

        JSONObject res = new JSONObject(runResult);

        try {
            new File(filepath.toString()).delete(); // Delete .testOutput
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
        return Util.spliceInto(this.selected.originalFile, assertions, this.selected.line);
    }

    // This needs lots of testing!
    private String scuffedGenAssertions(JSONObject observed) {
        String name = this.selected.selected;
        String ws = this.selected.whitespace;
        String type = (String) observed.get("type");
        String lsp = System.getProperty("line.separator");
        String val;
        StringBuilder toReturn = new StringBuilder();
        switch (type) {
            case "boolean":
            case "number":
                val = observed.get("value").toString();
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(typeof varName).to.equal(resType);").append(lsp);
                toReturn.append(ws).append("expect(varName).to.equal(").append(val).append(");").append(lsp);
                break;
            case "string": // Only diff is the quotes in the deep equal
                val = (String) observed.get("value");
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(typeof varName).to.equal(resType);").append(lsp);
                toReturn.append(ws).append("expect(varName).to.equal(\"").append(val).append("\");").append(lsp);
                break;
            case "symbol":
                val = (String) observed.get("value");
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(typeof varName).to.equal(resType);").append(lsp);
                toReturn.append(ws).append("expect(varName.toString()).to.equal(\"").append(val).append("\");").append(lsp);
                break;
            case "function":
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(typeof varName).to.equal(resType);").append(lsp);
                break;
            case "null":
                toReturn.append(ws).append("expect(varName).to.not.exist;").append(lsp);
                toReturn.append(ws).append("expect(varName).to.equal(null);").append(lsp);
                break;
            case "undefined":
                toReturn.append(ws).append("expect(varName).to.not.exist;").append(lsp);
                toReturn.append(ws).append("expect(typeof varName).to.equal(resType);").append(lsp);
                break;
            case "array":
                JSONArray arr = (JSONArray) observed.get("value");
                val = arr.toString();
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(Array.isArray(varName)).to.be.true;").append(lsp);
                toReturn.append(ws).append("expect(varName).to.deep.equal(").append(val).append(");").append(lsp);
                break;
            case "set":
                JSONArray setArr = (JSONArray) observed.get("value");
                val = setArr.toString();
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(varName instanceof Set).to.be.true;").append(lsp);
                toReturn.append(ws).append("expect(Array.from(varName)).to.deep.equal(\"").append(val).append("\");").append(lsp); // No idea if this works
                break;
            case "object":
                JSONObject subObj = (JSONObject) observed.get("value");
                val = subObj.toString();
                toReturn.append(ws).append("expect(varName).to.exist;").append(lsp);
                toReturn.append(ws).append("expect(typeof varName).to.equal(resType);").append(lsp);
                toReturn.append(ws).append("expect(varName).to.deep.equal(\"").append(val).append("\");").append(lsp);
                break;
            default:
                // Should never happen
                toReturn.append(ws).append("// Assertion generation failed").append(lsp);
        }
        String out = toReturn.toString();
        out = out.replaceAll("varName", name);
        return out.replaceAll("resType", "\"" + type + "\"");
    }
}


/*
package com.lucasaz.intellij.TestPlugin;

import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class FileWatcher extends Thread {
    private Path path;
    private String target;
    private TestAction hostAction;
    private AtomicBoolean stop;

    public FileWatcher(Path path, String target, TestAction hostAction) {
            this.path = path;
            this.target = target;
            this.hostAction = hostAction;
            this.stop = new AtomicBoolean(false);
    }

    public boolean isStopped() { return stop.get(); }
    public void stopThread() { stop.set(true); }

    @Override
    public void run() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            this.path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            while (!isStopped()) {
                WatchKey key;
                key = watcher.poll();
                System.out.println("Running");
                if (key == null) {
                    Thread.sleep(50);
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        Thread.sleep(50);
                    } else if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
                            && filename.toString().equals(this.target)) {
                        System.out.println("~~~~~~~ HIT ~~~~~~~");
                        this.hostAction.onFileCreate();
                    }
                }
                boolean valid = key.reset();
                if (!valid)
                {
                    break;
                }
                Thread.sleep(50);
            }
        } catch (Throwable e) {
            System.out.println("aaaaaa");
            System.out.println(e.getMessage());
        }
    }
}


 */