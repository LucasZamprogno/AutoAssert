package com.lucasaz.intellij.AssertionGeneration.model;

import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class DynamicAnalysisResult extends AssertionGenerationResponse {
    List<Assertion> associatedBlock;
    String sourceFilePath;
    boolean error;

    public DynamicAnalysisResult(List<Assertion> associatedBlock, String sourceFilePath, boolean differentBetweenRuns, boolean error, String generatedAssertions) {
        super(generatedAssertions, differentBetweenRuns);
        this.associatedBlock = associatedBlock;
        this.sourceFilePath = sourceFilePath;
        this.error = error;
    }
}
