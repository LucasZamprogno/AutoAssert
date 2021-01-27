package com.lucasaz.intellij.AssertionGeneration.assertions;

import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;

public class Isomorphism {
    private final String template;
    private final IsomorphismGenerator generator;
    private final AssertionComparator comparator;

    public Isomorphism(String template, AssertionComparator comparator) {
        this.template = template;
        this.generator = IsomorphismGeneratorFactory.generatorFromString(template);
        this.comparator = comparator;
    }

    public String fillInAssertion(String LHS, String RHS) {
        return this.generator.gen(LHS, RHS);
    }

    public String getTemplate() {
        return this.template;
    }

    public boolean matchesTemplate(Assertion assertion) {
        return comparator != null && comparator.match(assertion);
    }
}
