package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;

import java.util.List;

public class BooleanOptions extends ConfigurableCategoryOptions {
    // private static final String[] BOOLEAN_OPTIONS = {"expect(LHS).to.be.RHS;", "expect(LHS).to.equal(RHS);"};

    @Override
    public AssertKind getKind() {
        return AssertKind.BOOL;
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
