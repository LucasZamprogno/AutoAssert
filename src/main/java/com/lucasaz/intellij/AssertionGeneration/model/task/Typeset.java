package com.lucasaz.intellij.AssertionGeneration.model.task;

public class Typeset extends Task
{

    public Typeset(String testDirPath, String testFileName) {
        super("davidmerfield-Typeset.js",
                testDirPath,
                testFileName,
                "assertion-typeset-runner");
    }
}
