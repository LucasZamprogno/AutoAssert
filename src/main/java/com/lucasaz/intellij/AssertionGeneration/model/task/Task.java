package com.lucasaz.intellij.AssertionGeneration.model.task;

import com.lucasaz.intellij.AssertionGeneration.exceptions.PluginException;
import com.lucasaz.intellij.AssertionGeneration.util.Util;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Task {


    public final String taskName; // mock
    public final String testDir; // test
    public final String testFile; // Main.spec.ts
    public final String image; // assertion-runner
    public final String logFile = ".testOutput";
    public final String logFileInRepo; // test/.testOutput;
    public final String testFilePath; // = test/Main.spec.ts;

    public Task(String task, String testDir, String testFile) {
        this.taskName = task;
        this.testDir = testDir;
        this.testFile = testFile;
        this.image = "assertion-" + task.toLowerCase() + "-runner";
        this.logFileInRepo = Util.joinStringPaths(testDir, this.logFile);
        this.testFilePath =  Util.joinStringPaths(testDir, testFile);
    }

    public String getOutputFilePath(long id) {
        return Util.hostFSVolumeDir + "/" + id + "/" + this.logFileInRepo;
    }

    public void initVolume(long id, String test) throws PluginException, IOException {
        this.removeVolume(id); // Make sure there's no residual files. This mainly applies during testing
        String volumePath = this.relToVolume("", id); // Util.hostFSVolumeDir + "/" + id;
        String volTestPath = this.relToVolume(this.testDir, id);
        Util.ensureDir(volumePath);
        Util.ensureDir(volTestPath);
        File src = new File(this.relToServer(this.testDir));
        File dst = new File(volTestPath);
        FileUtils.copyDirectory(src, dst);
        Util.writeFile(Util.joinStringPaths(volTestPath, this.testFile), test);
    }

    public void removeVolume(long id) {
        Util.ensureDeleted(new File(Util.volumeDir + "/" + id));
    }

    public String relToVolume(String path) {
        return Util.hostFSVolumeDir + "/" + path;
    }

    public String relToVolume(String path, long id) {
        return Util.hostFSVolumeDir + "/" + String.valueOf(id) + "/" + path;
    }

    public String relToServer(String path) {
        return Util.projectDir + "/temp/" + this.taskName + "/" + path;
    }

    public String relToUnzipped(String path) {
        return "zips/" + path;
    }
}
