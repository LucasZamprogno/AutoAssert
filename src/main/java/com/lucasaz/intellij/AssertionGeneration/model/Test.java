package com.lucasaz.intellij.AssertionGeneration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class Test {
    List<Assertion> assertions;
    String filePath;
    int start;
}
