package com.lucasaz.intellij.TestPlugin;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TestSettingsConfigurable implements SearchableConfigurable {
    public static final String PATH_KEY = "TestPluginTsconfigPathKey";
    public static final String BUILD_KEY = "TestPluginShouldBuildKey";
    private TestSettingsForm mySettingsPane;
    private PropertiesComponent settings;

    public TestSettingsConfigurable(Project project) {
        settings = PropertiesComponent.getInstance(project);
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
            String saved = this.settings.getValue(PATH_KEY);
            if (saved == null) {
                this.settings.setValue(PATH_KEY, "");
                this.mySettingsPane.setPath("");
            } else {
                mySettingsPane.setPath(saved);
            }
        }
        reset();
        return mySettingsPane.getPanel();
    }

    @Override
    public boolean isModified() {
        return !this.mySettingsPane.getPath().equals(this.settings.getValue(PATH_KEY));
    }

    @Override
    public void apply() throws ConfigurationException {
        this.settings.setValue(PATH_KEY, mySettingsPane.getPath());
        // this.path = mySettingsPane.getPath();
    }
}