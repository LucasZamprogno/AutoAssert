package com.lucasaz.intellij.AssertionGeneration.model;

import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
public class DynamicAnalysisResult extends AssertionGenerationResponse {
    List<Assertion> associatedBlock;
    String sourceFilePath;
    boolean error;

    public DynamicAnalysisResult(List<Assertion> associatedBlock,
                                 String sourceFilePath,
                                 boolean differentBetweenRuns,
                                 boolean error,
                                 String generatedAssertions,
                                 String errorReason) {
        super(generatedAssertions, differentBetweenRuns, error, errorReason);
        this.associatedBlock = associatedBlock;
        this.sourceFilePath = sourceFilePath;
        this.error = error;
    }

    public String toString() {
        String str = "Theirs: \n" + associatedBlock.toString() + "\n\n" +
                "Ours: \n" + this.generatedAssertions + "\n\n" + "Error: " + this.error;
        if (this.failed) {
            str = str + "\n\n" + "Fail reason: " + this.reason;
        }
        return str;
    }
}
