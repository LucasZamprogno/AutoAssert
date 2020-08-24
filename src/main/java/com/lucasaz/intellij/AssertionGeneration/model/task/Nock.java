package com.lucasaz.intellij.AssertionGeneration.model.task;

public class Nock extends Task
{

    public Nock(String testDirPath, String testFileName) {
        super("NockUnmocked",
                testDirPath,
                testFileName,
                "assertion-nock-runner");
    }
}
