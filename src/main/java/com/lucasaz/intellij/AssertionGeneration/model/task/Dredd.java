package com.lucasaz.intellij.AssertionGeneration.model.task;

public class Dredd extends Task
{

    public Dredd(String testDirPath, String testFileName) {
        super("apiaryio-dredd",
                testDirPath,
                testFileName,
                "mining-dredd-runner");
    }
}
