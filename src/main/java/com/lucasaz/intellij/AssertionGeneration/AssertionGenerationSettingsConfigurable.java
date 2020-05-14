package com.lucasaz.intellij.AssertionGeneration;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class AssertionGenerationSettingsConfigurable implements SearchableConfigurable {
    public static final String BUILD_ALL_KEY = "AssertionGenerationBuildAllKey";
    public static final String AUTO_CONFIG_KEY = "AssertionGenerationAutoConfigKey";
    public static final String PATH_KEY = "AssertionGenerationTsconfigPathKey";
    private AssertionGenerationSettingsForm mySettingsPane;
    private PropertiesComponent settings;

    public AssertionGenerationSettingsConfigurable(Project project) {
        settings = PropertiesComponent.getInstance(project);
    }

    @NotNull
    @Override
    public String getId() {
        return "assertion.generation.settings";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Assertion Generation";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (mySettingsPane == null) {
            mySettingsPane = new AssertionGenerationSettingsForm();
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