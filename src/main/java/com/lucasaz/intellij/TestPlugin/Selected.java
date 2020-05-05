package com.lucasaz.intellij.TestPlugin;

public class Selected {
    int line;
    int col;
    String selected;
    String tsFilePath;

    public Selected(int line, int col, String selected, String tsFilePath) {
        this.line = line;
        this.col = col;
        this.selected = selected;
        this.tsFilePath = tsFilePath;
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
