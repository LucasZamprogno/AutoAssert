package com.lucasaz.intellij.AssertionGeneration.model.task;

public class Typeset extends Task
{

    public Typeset(String testDirPath, String testFileName) {
        super("davidmerfield-Typeset",
                testDirPath,
                testFileName,
                "mining-typeset-runner");
    }
}
