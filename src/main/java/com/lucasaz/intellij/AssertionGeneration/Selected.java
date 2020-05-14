package com.lucasaz.intellij.AssertionGeneration;

public class Selected {
    int line;
    int col;
    String selected;
    String tsFilePath;
    String originalFile;
    String whitespace;

    public Selected(int line, int col, String selected, String tsFilePath, String originalFile, String whitespace) {
        this.line = line;
        this.col = col;
        this.selected = selected;
        this.tsFilePath = tsFilePath;
        this.originalFile = originalFile;
        this.whitespace = whitespace;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    public String getSelected() {
        return selected;
    }

    public String getTsFilePath() {
        return tsFilePath;
    }
}
