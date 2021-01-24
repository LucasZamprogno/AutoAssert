package com.lucasaz.intellij.AssertionGeneration.services;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.NonBlockingReadAction;
import com.intellij.openapi.application.ReadAction;
import com.intellij.util.concurrency.NonUrgentExecutor;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionSelector;
import com.lucasaz.intellij.AssertionGeneration.assertions.IsomorphismSelector;
import com.lucasaz.intellij.AssertionGeneration.indices.AssertionGenerationSettingsConfigurable;
import com.lucasaz.intellij.AssertionGeneration.util.Util;
import com.lucasaz.intellij.AssertionGeneration.dto.Selected;
import com.lucasaz.intellij.AssertionGeneration.exceptions.PluginException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileWatcher extends Thread implements Runnable {
    private Selected selected;
    private Path path;
    private AtomicBoolean stop;
    private WatchService watcher;
    private PropertiesComponent settings;
    private static FileWatcher instance = null;

    public static FileWatcher getInstance(Selected selected, WatchService watcher, PropertiesComponent settings) {
        if (FileWatcher.instance != null) {
            instance.stopRequeue();
        }
        FileWatcher.instance = new FileWatcher(selected, watcher, settings);
        return FileWatcher.instance;
    }

    private FileWatcher(Selected selected, WatchService watcher, PropertiesComponent settings) {
            this.selected = selected;
            this.path = Paths.get(selected.getTsFilePath());
            this.stop = new AtomicBoolean(false);
            this.watcher = watcher;
            this.settings = settings;
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
        String varName = this.selected.getSelected();
        String whitespace = this.selected.getWhitespace();
        boolean verbose = this.settings.getBoolean(AssertionGenerationSettingsConfigurable.VERBOSE_KEY);
        Map<AssertKind, String> map = AssertionGenerationSettingsConfigurable.getSelectedIsos(this.settings);
        IsomorphismSelector isoSelector = new IsomorphismSelector(map);
        String assertions = AssertionSelector.getAllAssertions(observed, varName, whitespace, verbose, isoSelector);
        return Util.spliceInto(this.selected.getOriginalFile(), assertions, this.selected.getLine());
    }
}