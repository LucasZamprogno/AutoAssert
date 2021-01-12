package com.lucasaz.intellij.AssertionGeneration.assertions;

public class Isomorphism {
    private final String template;
    private final IsomorphismGenerator generator;

    public Isomorphism(String template) {
        this.template = template;
        this.generator = IsomorphismGeneratorFactory.generatorFromString(template);
    }

    public String fillInAssertion(String LHS, String RHS) {
        return this.generator.gen(LHS, RHS);
    }

    public String getTemplate() {
        return this.template;
    }
}
