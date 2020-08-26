package com.lucasaz.intellij.AssertionGeneration.model;

import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DynamicAnalysisResult {
    List<Assertion> associatedBlock;
    String sourceFilePath;
    boolean differentBetweenRuns;
    boolean error;
    String generatedAssertions;
}
