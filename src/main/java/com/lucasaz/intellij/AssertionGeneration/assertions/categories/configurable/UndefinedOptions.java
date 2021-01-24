package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;

import java.util.List;

public class UndefinedOptions extends ConfigurableCategoryOptions {
    // private static final String[] UNDEFINED_OPTIONS = {"expect(LHS).to.be.undefined;", "expect(LHS).to.equal(undefined);"};

    @Override
    public AssertKind getKind() {
        return AssertKind.UNDEFINED;
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
