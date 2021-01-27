package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LengthOptions extends ConfigurableCategoryOptions {
    // private static final String[] LENGTH_OPTIONS = {"expect(LHS).to.have.length(RHS);", "expect(LHS.length).to.equal(RHS);"};

    @Override
    public AssertKind getKind() {
        return AssertKind.LENGTH;
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
