package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;

import java.util.List;

public class EqualityOptions extends ConfigurableCategoryOptions {
    // private static final String[] EQUALITY_OPTIONS = {"expect(LHS).to.equal(RHS);", "expect(LHS).to.eq(RHS);"};

    @Override
    public AssertKind getKind() {
        return AssertKind.EQUAL;
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
