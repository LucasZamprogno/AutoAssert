package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeepEqualityOptions extends ConfigurableCategoryOptions {
    // private static final String[] DEEP_EQUALITY_OPTIONS = {"expect(LHS).to.deep.equal(RHS);", "expect(LHS).to.eql(RHS);"};

    @Override
    public AssertKind getKind() {
        return AssertKind.DEEP_EQUAL;
    }

    @Override
    public AssertionComparator getComparator() {
        return null;
    }

    @Override
    public List<Isomorphism> getIsomorphisms() {
        return Collections.singletonList(new Isomorphism("STUB", null));
    }
}
