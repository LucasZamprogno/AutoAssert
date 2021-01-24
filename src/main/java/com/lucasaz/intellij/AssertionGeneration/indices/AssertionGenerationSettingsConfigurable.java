package com.lucasaz.intellij.AssertionGeneration.indices;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.Category;
import com.lucasaz.intellij.AssertionGeneration.assertions.CategoryManager;
import com.lucasaz.intellij.AssertionGeneration.util.Util;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssertionGenerationSettingsConfigurable implements SearchableConfigurable {
    public static final String BUILD_ALL_KEY = "AssertionGenerationBuildAllKey";
    public static final String AUTO_CONFIG_KEY = "AssertionGenerationAutoConfigKey";
    public static final String PATH_KEY = "AssertionGenerationTsconfigPathKey";
    public static final String VERBOSE_KEY = "AssertionGenerationVerboseKey";

    public static Map<AssertKind, String> getSelectedIsos(PropertiesComponent settings) {
        HashMap<AssertKind, String> map = new HashMap<>();
        for (Category category : CategoryManager.getConfigurableCategories()) {
            AssertKind kind = category.getKind();
            String catStorageKey = category.getStoageKey();
            map.put(kind, settings.getValue(catStorageKey));
        }
        return map;
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
            for (Category category : CategoryManager.getConfigurableCategories()) {
                JComboBox<String> jComboBox = this.mySettingsPane.getJComboBox(category.getKind());
                String catStorageKey = category.getStoageKey();
                String defaultIso = category.getDefaultIsomorphism().getTemplate();
                String loadedValue = this.settings.getValue(catStorageKey, defaultIso);
                category.initializePassedJComboBox(jComboBox, loadedValue);
            }
            boolean build = this.settings.getBoolean(BUILD_ALL_KEY);
            boolean auto = this.settings.getBoolean(AUTO_CONFIG_KEY);
            boolean verbose = this.settings.getBoolean(VERBOSE_KEY);
            final Project project = ProjectUtil.guessCurrentProject(mySettingsPane.getPanel()); // Spooky
            List<String> tsconfigPaths = Util.findAllTsconfigInProject(project);
            mySettingsPane.setAll(tsconfigPaths, pathSelected, build, auto, verbose);
            mySettingsPane.setListeners();
        }
        this.mySettingsPane.getScanButton().addActionListener(this.createScanActionListener());
        reset(); // I don't know what this does
        return mySettingsPane.getPanel();
    }

    @Override
    public boolean isModified() {
        boolean buildChanged = !(this.mySettingsPane.getBuild() == this.settings.getBoolean(BUILD_ALL_KEY));
        boolean autoChanged = !(this.mySettingsPane.getAuto() == this.settings.getBoolean(AUTO_CONFIG_KEY));
        boolean verboseChanged = !(this.mySettingsPane.getVerbose() == this.settings.getBoolean(VERBOSE_KEY));
        boolean pathChanged = !this.mySettingsPane.getPath().equals(this.settings.getValue(PATH_KEY));
        boolean isoChanged = false;
        for (Category category : CategoryManager.getConfigurableCategories()) {
            AssertKind kind = category.getKind();
            String catStorageKey = category.getStoageKey();
            String defaultIso = category.getDefaultIsomorphism().getTemplate();
            String current = this.mySettingsPane.getIso(kind);
            String prev = this.settings.getValue(catStorageKey, defaultIso); // Default value in case it has never been saved and loads null? Better way?
            isoChanged = isoChanged || !current.equals(prev); // Minor inefficiency in not breaking but eh.
        }
        return buildChanged || autoChanged || pathChanged || verboseChanged || isoChanged;
    }

    @Override
    public void apply() {
        this.settings.setValue(BUILD_ALL_KEY, mySettingsPane.getBuild());
        this.settings.setValue(AUTO_CONFIG_KEY, mySettingsPane.getAuto());
        this.settings.setValue(VERBOSE_KEY, mySettingsPane.getVerbose());
        this.settings.setValue(PATH_KEY, mySettingsPane.getPath());
        for (Category category : CategoryManager.getConfigurableCategories()) {
            AssertKind kind = category.getKind();
            String catStorageKey = category.getStoageKey();
            this.settings.setValue(catStorageKey, mySettingsPane.getIso(kind));
        }
    }

    private ActionListener createScanActionListener() {
        // TODO
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Braxton's code goes here");
            }
        };
    }
}