package com.lucasaz.intellij.TestPlugin;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.NonDefaultProjectConfigurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author: Fedor.Korotkov
 */
public class TestSettingsConfigurable implements SearchableConfigurable {
    private TestSettingsForm mySettingsPane;
    private final Project myProject;
    private String path = "";

    public TestSettingsConfigurable(Project project) {
        myProject = project;
    }

    @NotNull
    @Override
    public String getId() {
        return "test.settings";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Test Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (mySettingsPane == null) {
            mySettingsPane = new TestSettingsForm();
        }
        reset();
        return mySettingsPane.getPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        this.path = mySettingsPane.getPath();
    }
}