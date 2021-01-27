package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NullOptions extends ConfigurableCategoryOptions {
    // private static final String[] NULL_OPTIONS = {"expect(LHS).to.be.null;", "expect(LHS).to.equal(null);"};

    @Override
    public AssertKind getKind() {
        return AssertKind.NULL;
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
