package com.lucasaz.intellij.AssertionGeneration.indices;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.IsomorphismSelector;
import com.lucasaz.intellij.AssertionGeneration.util.Util;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class AssertionGenerationSettingsConfigurable implements SearchableConfigurable {
    public static final String BUILD_ALL_KEY = "AssertionGenerationBuildAllKey";
    public static final String AUTO_CONFIG_KEY = "AssertionGenerationAutoConfigKey";
    public static final String PATH_KEY = "AssertionGenerationTsconfigPathKey";
    public static final String NULL_KEY = "AssertionGenerationNullIsoKey";
    public static final String UNDEFINED_KEY = "AssertionGenerationUndefIsoKey";
    public static final String EQULITY_KEY = "AssertionGenerationEqualIsoKey";
    public static final String DEEP_EQULITY_KEY = "AssertionGenerationDeepEqualIsoKey";
    public static final String LENGTH_KEY = "AssertionGenerationLengthIsoKey";
    public static final String TYPE_KEY = "AssertionGenerationTypeIsoKey";
    public static final String BOOL_KEY = "AssertionGenerationBoolIsoKey";
    public static final String[] ISO_KEYS = {NULL_KEY, UNDEFINED_KEY, EQULITY_KEY,
                                            DEEP_EQULITY_KEY, LENGTH_KEY, TYPE_KEY, BOOL_KEY};

    public static AssertKind keyToAssertKind(String key) {
        switch(key) {
            case AssertionGenerationSettingsConfigurable.NULL_KEY:
                return AssertKind.NULL;
            case AssertionGenerationSettingsConfigurable.UNDEFINED_KEY:
                return AssertKind.UNDEFINED;
            case AssertionGenerationSettingsConfigurable.EQULITY_KEY:
                return AssertKind.EQUAL;
            case AssertionGenerationSettingsConfigurable.DEEP_EQULITY_KEY:
                return AssertKind.DEEP_EQUAL;
            case AssertionGenerationSettingsConfigurable.LENGTH_KEY:
                return AssertKind.LENGTH;
            case AssertionGenerationSettingsConfigurable.TYPE_KEY:
                return AssertKind.TYPE;
            case AssertionGenerationSettingsConfigurable.BOOL_KEY:
                return AssertKind.BOOL;
            default:
                return AssertKind.NULL; // Should never happen
        }
    }

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
            String pathSelected = this.settings.getValue(PATH_KEY, "");
            List<String> isoSelected = new ArrayList<>();
            for (String key : AssertionGenerationSettingsConfigurable.ISO_KEYS) {
                AssertKind kind = AssertionGenerationSettingsConfigurable.keyToAssertKind(key);
                String def = new IsomorphismSelector().defaults.get(kind);
                isoSelected.add(this.settings.getValue(key, def));
            }
            boolean build = this.settings.getBoolean(BUILD_ALL_KEY);
            boolean auto = this.settings.getBoolean(AUTO_CONFIG_KEY);
            final Project project = ProjectUtil.guessCurrentProject(mySettingsPane.getPanel()); // Spooky
            List<String> tsconfigPaths = Util.findAllTsconfigInProject(project);
            mySettingsPane.setAll(tsconfigPaths, pathSelected, isoSelected, build, auto);
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
        boolean isoChanged = false;
        for (String key : AssertionGenerationSettingsConfigurable.ISO_KEYS) {
            isoChanged = isoChanged || !this.mySettingsPane.getIso(key).equals(this.settings.getValue(key));
        }
        return buildChanged || autoChanged || pathChanged || isoChanged;
    }

    @Override
    public void apply() {
        this.settings.setValue(BUILD_ALL_KEY, mySettingsPane.getBuild());
        this.settings.setValue(AUTO_CONFIG_KEY, mySettingsPane.getAuto());
        this.settings.setValue(PATH_KEY, mySettingsPane.getPath());
        for (String key : AssertionGenerationSettingsConfigurable.ISO_KEYS) {
            this.settings.setValue(NULL_KEY, mySettingsPane.getIso(key));
        }
    }
}