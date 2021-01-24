package com.lucasaz.intellij.AssertionGeneration.assertions;

import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;

import javax.swing.*;
import java.util.*;

public class Category {
    private final List<Isomorphism> isomorphisms = new ArrayList<>();
    private final AssertKind kind;

    public Category(AssertKind kind, String[] isomorphismTemplates) {
        this.kind = kind;
        for (String template : isomorphismTemplates) {
            this.isomorphisms.add(new Isomorphism(template, new AssertionComparator() {
                // TODO Just a stub
                @Override
                boolean match(Assertion assertion) {
                    return false;
                }
            }));
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

    public String getStorageKey() {
        return this.kind.storageKey;
    }

    public Isomorphism calculatePopularIsomorphism(Collection<Assertion> assertions) {
        Map<Isomorphism, Integer> isoCounts = new HashMap<>();

        for (Assertion assertion : assertions) {
            for (Isomorphism isomorphism : isomorphisms) {
                if (!isoCounts.containsKey(isomorphism)) {
                    isoCounts.put(isomorphism, 0);
                }
                if (isomorphism.matchesTemplate(assertion)) {
                    isoCounts.put(isomorphism, isoCounts.get(isomorphism) + 1);
                }
            }
        }

        Map.Entry<Isomorphism, Integer> mostPopularSoFar = null;
        for (Map.Entry<Isomorphism, Integer> entry : isoCounts.entrySet()) {
            if (mostPopularSoFar == null || entry.getValue() > mostPopularSoFar.getValue()) {
                mostPopularSoFar = entry;
            }
        }
        assert mostPopularSoFar != null;
        return mostPopularSoFar.getKey();
    }
}
