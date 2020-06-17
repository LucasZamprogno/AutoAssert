package com.lucasaz.intellij.AssertionGeneration.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class Selected {
    int line;
    int col;
    String selected;
    String tsFilePath;
    String originalFile;
    String whitespace;
}
