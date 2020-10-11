package com.lucasaz.intellij.AssertionGeneration.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssertionGenerationResponse {
    String generatedAssertions;
    boolean differentBetweenRuns;
    boolean failed;
    String reason;
}
