package com.lucasaz.intellij.AssertionGeneration.model;

import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONObject;

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
        JSONObject json = new JSONObject();
        json.put("theirs", associatedBlock.toString());
        json.put("ours", this.generatedAssertions);
        json.put("error", this.error);
        json.put("diff", this.differentBetweenRuns);
        if (this.failed) {
            json.put("failed", this.reason);
        }
        return json.toString();
    }
}
