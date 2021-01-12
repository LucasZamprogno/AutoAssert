package com.lucasaz.intellij.AssertionGeneration.assertions;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Category {
    private final List<Isomorphism> isomorphisms = new ArrayList<>();
    private final AssertKind kind;

    public Category(AssertKind kind, String[] isomorphismTemplates) {
        this.kind = kind;
        for (String template : isomorphismTemplates) {
            this.isomorphisms.add(new Isomorphism(template));
        }
    }

    public void initializePassedJComboBox(JComboBox<String> comboBox, String selected) {
        for (Isomorphism isomorphism : this.isomorphisms) {
            comboBox.addItem(isomorphism.getTemplate());
        }
        comboBox.setSelectedItem(selected);
    }

    public Isomorphism getDefaultIsomorphism() {
        return this.isomorphisms.get(0);
    }

    public Isomorphism getIsomorphism(String template) {
        for (Isomorphism isomorphism : this.isomorphisms) {
            if (isomorphism.getTemplate().equals(template)) {
                return isomorphism;
            }
        }
        return this.getDefaultIsomorphism();
    }

    public AssertKind getKind() {
        return this.kind;
    }

    public String getStoageKey() {
        return this.kind.storageKey;
    }
}
