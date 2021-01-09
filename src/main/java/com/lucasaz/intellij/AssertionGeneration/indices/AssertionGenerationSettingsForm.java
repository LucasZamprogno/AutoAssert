package com.lucasaz.intellij.AssertionGeneration.indices;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.IsomorphismSelector;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.List;

public class AssertionGenerationSettingsForm {
    private JPanel jPanel;
    private JCheckBox buildAll;
    private JCheckBox autoSelect;
    private JComboBox<String> tsconfigDropdown;
    private JPanel AssertionConfiguration;
    private JComboBox<String> nullIsomorphism;
    private JComboBox<String> undefinedIsomorphism;
    private JComboBox<String> equalityIsomorphism;
    private JComboBox deepEqualityIsomorphism;
    private JComboBox<String> lengthIsomorphism;
    private JComboBox<String> typeIsomorphism;
    private JComboBox<String> booleanIsomorphism;

    public String getPath() {
        return this.tsconfigDropdown.getSelectedItem().toString();
    }

    public boolean getBuild() {
        return this.buildAll.isSelected();
    }

    public boolean getAuto() {
        return this.autoSelect.isSelected();
    }

    public String getIso(String key) {
        switch(key) {
            case AssertionGenerationSettingsConfigurable.NULL_KEY:
                return this.nullIsomorphism.getSelectedItem().toString();
            case AssertionGenerationSettingsConfigurable.UNDEFINED_KEY:
                return this.undefinedIsomorphism.getSelectedItem().toString();
            case AssertionGenerationSettingsConfigurable.EQULITY_KEY:
                return this.equalityIsomorphism.getSelectedItem().toString();
            case AssertionGenerationSettingsConfigurable.DEEP_EQULITY_KEY:
                return this.deepEqualityIsomorphism.getSelectedItem().toString();
            case AssertionGenerationSettingsConfigurable.LENGTH_KEY:
                return this.lengthIsomorphism.getSelectedItem().toString();
            case AssertionGenerationSettingsConfigurable.TYPE_KEY:
                return this.typeIsomorphism.getSelectedItem().toString();
            case AssertionGenerationSettingsConfigurable.BOOL_KEY:
                return this.booleanIsomorphism.getSelectedItem().toString();
            default:
                return "";
        }
    }

    public String getNull() { return this.nullIsomorphism.getSelectedItem().toString(); }

    public JComponent getPanel() {
        return jPanel;
    }

    public void setAll(List<String> dropdownOptions, String selected, List<String> isoSelected, boolean build, boolean auto) {
        this.setupDropdown(dropdownOptions, selected);
        for (int i = 0; i < AssertionGenerationSettingsConfigurable.ISO_KEYS.length; i++) {
            String key = AssertionGenerationSettingsConfigurable.ISO_KEYS[i];
            String select = isoSelected.get(i);
            this.setupIso(key, select);
        }
        this.buildAll.setSelected(build);
        this.autoSelect.setSelected(auto);
        this.setEnabledStates();
    }

    public void setEnabledStates() {
        this.autoSelect.setEnabled(false);
        this.tsconfigDropdown.setEnabled(false);
        if (this.buildAll.isSelected()) {
            this.autoSelect.setEnabled(true);
        }
        if (this.autoSelect.isEnabled() && !this.autoSelect.isSelected()) {
            this.tsconfigDropdown.setEnabled(true);
        }
    }

    public void setListeners() {
        AssertionGenerationSettingsForm form = this;
        ActionListener al = e -> form.setEnabledStates();
        this.buildAll.addActionListener(al);
        this.autoSelect.addActionListener(al);
    }

    public void setupDropdown(List<String> configs, String selected) {
        for (String path : configs) {
            this.tsconfigDropdown.addItem(path);
        }
        this.tsconfigDropdown.setSelectedItem(selected);
    }

    public void setupIso(String key, String selected) {
        AssertKind kind = AssertionGenerationSettingsConfigurable.keyToAssertKind(key);
        String[] options;
        JComboBox<String> isoBox;
        switch(kind) {
            case NULL:
                options = IsomorphismSelector.NULL_OPTIONS;
                isoBox = this.nullIsomorphism;
                break;
            case UNDEFINED:
                options = IsomorphismSelector.UNDEFINED_OPTIONS;
                isoBox = this.undefinedIsomorphism;
                break;
            case EQUAL:
                options = IsomorphismSelector.EQUALITY_OPTIONS;
                isoBox = this.equalityIsomorphism;
                break;
            case DEEP_EQUAL:
                options = IsomorphismSelector.DEEP_EQUALITY_OPTIONS;
                isoBox = this.equalityIsomorphism;
                break;
            case LENGTH:
                options = IsomorphismSelector.LENGTH_OPTIONS;
                isoBox = this.lengthIsomorphism;
                break;
            case TYPE:
                options = IsomorphismSelector.TYPE_OPTIONS;
                isoBox = this.typeIsomorphism;
                break;
            case BOOL:
                options = IsomorphismSelector.BOOLEAN_OPTIONS;
                isoBox = this.booleanIsomorphism;
                break;
            default:
                return;
        }
        for (String option : options) {
            isoBox.addItem(option);
        }
        isoBox.setSelectedItem(selected);
    }
}
