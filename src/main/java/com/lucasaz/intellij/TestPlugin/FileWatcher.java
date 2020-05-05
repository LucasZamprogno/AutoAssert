// TODO Fix injection strings
// TODO Get string insertion working/refactored

package com.lucasaz.intellij.TestPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.intellij.openapi.application.NonBlockingReadAction;
import com.intellij.openapi.application.ReadAction;
import com.intellij.util.concurrency.NonUrgentExecutor;
import org.json.JSONArray;
import org.json.JSONObject;


public class FileWatcher extends Thread implements Runnable {
    private Selected selected;
    private Path path;
    private String target;
    private AtomicBoolean stop;
    private WatchService watcher;

    public FileWatcher(Selected selected, String target, WatchService watcher) {
            this.selected = selected;
            this.path = Paths.get(selected.tsFilePath);
            this.target = target;
            this.stop = new AtomicBoolean(false);
            this.watcher = watcher;
    }

    public boolean isStopped() { return stop.get(); }
    public void stopThread() { stop.set(true); }

    @Override
    public void run() {
        try {
            System.out.println("Running");
            WatchKey key;
            key = this.watcher.poll();
            if (key == null) {
                NonBlockingReadAction<Void> res = ReadAction.nonBlocking(this);
                res.submit(NonUrgentExecutor.getInstance());
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();

                if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
                        && filename.toString().equals(this.target)) {
                    System.out.println("~~~~~~~ HIT ~~~~~~~");
                    Thread.sleep(20);
                    this.onFileCreate();
                    return;
                }
            }
            boolean valid = key.reset();
            if (valid) {
                NonBlockingReadAction<Void> res = ReadAction.nonBlocking(this);
                res.submit(NonUrgentExecutor.getInstance());
                return;
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
        System.out.println(runResult);
        JSONObject res = new JSONObject(runResult);

        try {
            new File(filepath.toString()).delete(); // Delete .testOutput
            String wouldBeAssertedFile = this.makeFinalFile(res);
            FileWriter fw = new FileWriter(this.path.toString());
            fw.write(wouldBeAssertedFile);
            fw.close();
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
        String lsp = System.getProperty("line.separator");
        String val;
        StringBuilder toReturn = new StringBuilder();
        switch (type) {
            case "boolean":
            case "number":
            case "string":
                val = (String) observed.get("value");
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;" + lsp);
                toReturn.append(this.selected.whitespace + "expect(typeof varName).to.equal(resType);" + lsp);
                toReturn.append(this.selected.whitespace + "expect(varName).to.equal(" + val + ");" + lsp);
                break;
            case "symbol":
                val = (String) observed.get("value"); // ???
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;" + lsp);
                toReturn.append(this.selected.whitespace + "expect(typeof varName).to.equal(resType);" + lsp);
                toReturn.append(this.selected.whitespace + "expect(varName.toString()).to.equal(" + val + ");" + lsp);
                break;
            case "function":
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;" + lsp);
                toReturn.append(this.selected.whitespace + "expect(typeof varName).to.equal(resType);" + lsp);
                break;
            case "null":
            case "undefined":
                toReturn.append(this.selected.whitespace + "expect(varName).to.not.exist;" + lsp);
                toReturn.append(this.selected.whitespace + "expect(typeof varName).to.equal(resType);" + lsp);
                break;
            case "array":
                JSONArray arr = (JSONArray) observed.get("value");
                val = arr.toString();
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;" + lsp);
                toReturn.append(this.selected.whitespace + "expect(Array.isArray(varName)).to.be.true;" + lsp);
                toReturn.append(this.selected.whitespace + "expect(varName).to.deep.equal(" + val + ");" + lsp);
                break;
            case "set":
                JSONArray setArr = (JSONArray) observed.get("value");
                val = setArr.toString();
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;" + lsp);
                toReturn.append(this.selected.whitespace + "expect(varName instanceof Set).to.be.true;" + lsp);
                toReturn.append(this.selected.whitespace + "expect(Array.from(varName)).to.deep.equal(" + val + ");" + lsp); // No idea if this works
                break;
            case "object":
                JSONObject subObj = new JSONObject(observed.get("value"));
                toReturn.append(this.selected.whitespace + "expect(varName).to.exist;" + lsp);
                toReturn.append(this.selected.whitespace + "expect(typeof varName).to.equal(resType);" + lsp);
                toReturn.append(this.selected.whitespace + "expect(varName).to.deep.equal(" + subObj.toString() + ");" + lsp);
                break;
            default:
                // Should never happen
                toReturn.append(this.selected.whitespace + "// Assertion generation failed" + lsp);
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