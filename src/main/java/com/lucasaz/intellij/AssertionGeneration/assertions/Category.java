package com.lucasaz.intellij.AssertionGeneration.assertions;

import com.lucasaz.intellij.AssertionGeneration.assertions.categories.CategoryOptions;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;

import javax.swing.*;
import java.util.*;

public class Category {
    private final List<Isomorphism> isomorphisms;
    private final AssertKind kind;
    private final boolean configurable;
    private final AssertionComparator comparator;

    public Category(CategoryOptions options) {
        this.kind = options.getKind();
        this.isomorphisms = options.getIsomorphisms();
        this.configurable = options.isConfigurable();
        this.comparator = options.getComparator();
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

    public boolean isConfigurable() {
        return this.configurable;
    }

    public String getStorageKey() {
        return this.kind.storageKey;
    }

    private boolean matchesCategory(Assertion assertion) {
        return comparator.match(assertion);
    }

    public Isomorphism calculatePopularIsomorphism(Collection<Assertion> assertions) {
        Map<Isomorphism, Integer> isoCounts = new HashMap<>();

        for (Assertion assertion : assertions) {
            if (matchesCategory(assertion)) {
                for (Isomorphism isomorphism : isomorphisms) {
                    if (!isoCounts.containsKey(isomorphism)) {
                        isoCounts.put(isomorphism, 0);
                    }
                    if (isomorphism.matchesTemplate(assertion)) {
                        isoCounts.put(isomorphism, isoCounts.get(isomorphism) + 1);
                    }
                }
            }
        }

        Map.Entry<Isomorphism, Integer> mostPopularSoFar = null;
        for (Map.Entry<Isomorphism, Integer> entry : isoCounts.entrySet()) {
            if (mostPopularSoFar == null || entry.getValue() > mostPopularSoFar.getValue()) {
                mostPopularSoFar = entry;
            }
        }

        if (mostPopularSoFar == null) {
            return getDefaultIsomorphism();
        } else {
            return mostPopularSoFar.getKey();
        }
    }
}
