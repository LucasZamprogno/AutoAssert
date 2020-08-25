package com.lucasaz.intellij.AssertionGeneration.model.task;

public class Nock extends Task
{

    public Nock(String testDirPath, String testFileName) {
        super("nock-nock",
                testDirPath,
                testFileName,
                "assertion-nock-runner");
    }
}
