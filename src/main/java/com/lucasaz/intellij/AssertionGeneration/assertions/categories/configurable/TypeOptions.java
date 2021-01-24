package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;

import java.util.List;

public class TypeOptions extends ConfigurableCategoryOptions {
    // private static final String[] TYPE_OPTIONS = {"expect(LHS).to.be.a(RHS);", "expect(LHS).to.be.an(RHS);"};

    @Override
    public AssertKind getKind() {
        return AssertKind.TYPE;
    }

    @Override
    public AssertionComparator getComparator() {
        return null;
    }

    @Override
    public List<Isomorphism> getIsomorphisms() {
        return null;
    }
}
