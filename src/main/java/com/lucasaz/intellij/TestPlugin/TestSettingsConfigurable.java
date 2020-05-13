package com.lucasaz.intellij.TestPlugin;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class TestSettingsConfigurable implements SearchableConfigurable {
    public static final String BUILD_ALL_KEY = "TestPluginBuildAllKey";
    public static final String AUTO_CONFIG_KEY = "TestPluginAutoConfigKey";
    public static final String PATH_KEY = "TestPluginTsconfigPathKey";
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
            String selected = this.settings.getValue(PATH_KEY, "");
            boolean build = this.settings.getBoolean(BUILD_ALL_KEY);
            boolean auto = this.settings.getBoolean(AUTO_CONFIG_KEY);
            final Project project = ProjectUtil.guessCurrentProject(mySettingsPane.getPanel()); // Spooky
            List<String> tsconfigPaths = Util.findAllTsconfigInProject(project);
            mySettingsPane.setAll(tsconfigPaths, selected, build, auto);
            mySettingsPane.setListeners();
        }
        reset(); // I don't know what this does
        return mySettingsPane.getPanel();
    }

    @Override
    public boolean isModified() {
        boolean buildChanged = !(this.mySettingsPane.getBuild() == this.settings.getBoolean(BUILD_ALL_KEY));
        boolean autoChanged = !(this.mySettingsPane.getAuto() == this.settings.getBoolean(AUTO_CONFIG_KEY));
        boolean pathChanged = !this.mySettingsPane.getPath().equals(this.settings.getValue(PATH_KEY));
        return buildChanged || autoChanged || pathChanged;
    }

    @Override
    public void apply() {
        this.settings.setValue(BUILD_ALL_KEY, mySettingsPane.getBuild());
        this.settings.setValue(AUTO_CONFIG_KEY, mySettingsPane.getAuto());
        this.settings.setValue(PATH_KEY, mySettingsPane.getPath());
    }
}