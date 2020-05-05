// TODO Fix injection strings
// TODO Get string insertion working/refactored

package com.lucasaz.intellij.TestPlugin;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonArray;
import org.assertj.core.internal.bytebuddy.implementation.bytecode.Throw;
import org.json.JSONArray;
import org.json.JSONObject;


public class FileWatcher extends Thread {
    private Selected selected;
    private Path path;
    private String target;
    private AtomicBoolean stop;

    public FileWatcher(Selected selected, String target) {
            this.selected = selected;
            this.path = Paths.get(selected.tsFilePath);
            this.target = target;
            this.stop = new AtomicBoolean(false);
    }

    public boolean isStopped() { return stop.get(); }
    public void stopThread() { stop.set(true); }

    @Override
    public void run() {
        try {
            System.out.println("Started");
            WatchService watcher = FileSystems.getDefault().newWatchService();
            this.path.getParent().register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            while (!isStopped()) {
                WatchKey key;
                key = watcher.poll();
                if (key == null) {
                    Thread.sleep(50);
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
                            && filename.toString().equals(this.target)) {
                        System.out.println("~~~~~~~ HIT ~~~~~~~");
                        this.onFileCreate();
                        return;
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

    public void onFileCreate() {
        Path filepath = Paths.get(this.path.getParent().toString(), this.target);
        String runResult = "";
        runResult = Util.pathToFileContent(filepath);
        JSONObject res = new JSONObject(runResult);

        try {
            String wouldBeAssertedFile = this.makeFinalFile(res);
            FileWriter fw = new FileWriter(this.path.toString());
            fw.write(wouldBeAssertedFile);
            fw.close();
            System.out.println("RESTORED");
        } catch (IOException err) {
            System.out.println(err.getMessage());
        } catch (Throwable e) {
            System.out.println("eeeee");
        }
    }

    private String makeFinalFile(JSONObject observed) {
        String assertions = this.scuffedGenAssertions(observed);
        return Util.spliceInto(this.selected.originalFile, assertions, this.selected.line);
    }

    // This needs lots of testing!
    public String scuffedGenAssertions(JSONObject observed) {
        String name = this.selected.selected;
        String type = (String) observed.get("type");
        String val;
        StringBuilder toReturn = new StringBuilder();
        switch (type) {
            case "boolean":
            case "number":
            case "string":
                val = (String) observed.get("value");
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;\n");
                toReturn.append(this.selected.whitespace + "expect(typeof varName).to.equal(resType);\n");
                toReturn.append(this.selected.whitespace + "expect(varName).to.equal(" + val + ");\n");
                break;
            case "symbol":
                val = (String) observed.get("value"); // ???
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;\n");
                toReturn.append(this.selected.whitespace + "expect(typeof varName).to.equal(resType);\n");
                toReturn.append(this.selected.whitespace + "expect(varName.toString()).to.equal(" + val + ");\n");
                break;
            case "function":
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;\n");
                toReturn.append(this.selected.whitespace + "expect(typeof varName).to.equal(resType);\n");
                break;
            case "null":
            case "undefined":
                toReturn.append(this.selected.whitespace + "expect(varName).to.not.exist;\n");
                toReturn.append(this.selected.whitespace + "expect(typeof varName).to.equal(resType);\n");
                break;
            case "array":
                JSONArray arr = (JSONArray) observed.get("value");
                val = arr.toString();
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;\n");
                toReturn.append(this.selected.whitespace + "expect(Array.isArray(varName)).to.be.true;\n");
                toReturn.append(this.selected.whitespace + "expect(varName).to.deep.equal(" + val + ");\n");
                break;
            case "set":
                JSONArray setArr = (JSONArray) observed.get("value");
                val = setArr.toString();
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;\n");
                toReturn.append(this.selected.whitespace + "expect(varName instanceof Set).to.be.true;\n");
                toReturn.append(this.selected.whitespace + "expect(Array.from(varName)).to.deep.equal(" + val + ");\n"); // No idea if this works
                break;
            case "object":
                JSONObject subObj = new JSONObject(observed.get("value"));
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;\n");
                toReturn.append(this.selected.whitespace + "expect(typeof varName).to.equal(resType);\n");
                toReturn.append(this.selected.whitespace + "expect(varName).to.deep.equal(" + subObj.toString() + ");\n");
                break;
            default:
                // Should never happen
                toReturn.append(this.selected.whitespace + "// Assertion generation failed\n");
        }
        String out = toReturn.toString();
        out = out.replaceAll("varName", name);
        return out.replaceAll("resType", type);
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