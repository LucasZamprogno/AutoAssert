package com.lucasaz.intellij.AssertionGeneration.indices;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;

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
    private JComboBox<String> deepEqualityIsomorphism;
    private JComboBox<String> lengthIsomorphism;
    private JComboBox<String> typeIsomorphism;
    private JComboBox<String> booleanIsomorphism;
    private JButton scanProjectButton;

    public String getPath() {
        return this.tsconfigDropdown.getSelectedItem().toString();
    }

    public boolean getBuild() {
        return this.buildAll.isSelected();
    }

    public boolean getAuto() {
        return this.autoSelect.isSelected();
    }

    public JButton getScanButton() {
        return this.scanProjectButton;
    }

    public String getIso(AssertKind kind) {
        return this.getJComboBox(kind).getSelectedItem().toString();
    }
    public JComboBox<String> getJComboBox(AssertKind kind) {
        switch(kind) {
            case NULL:
                return this.nullIsomorphism;
            case UNDEFINED:
                return this.undefinedIsomorphism;
            case EQUAL:
                return this.equalityIsomorphism;
            case DEEP_EQUAL:
                return this.deepEqualityIsomorphism;
            case LENGTH:
                return this.lengthIsomorphism;
            case TYPE:
                return this.typeIsomorphism;
            case BOOL:
                return this.booleanIsomorphism;
            default:
                return this.nullIsomorphism; // Should never happen
        }
    }

    public JComponent getPanel() {
        return jPanel;
    }

    public void setAll(List<String> dropdownOptions, String selected, boolean build, boolean auto) {
        this.setupDropdown(dropdownOptions, selected);
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
}
