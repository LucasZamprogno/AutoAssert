package com.lucasaz.intellij.TestPlugin;

import javax.swing.*;

public class TestSettingsForm {
    private JTextField textField1;
    private JPanel jPanel;

    public String getPath() {
        return textField1.getText();
    }

    public JComponent getPanel() {
        return jPanel;
    }

    public void setPath(String path) {
        this.textField1.setText(path);
    }
}
