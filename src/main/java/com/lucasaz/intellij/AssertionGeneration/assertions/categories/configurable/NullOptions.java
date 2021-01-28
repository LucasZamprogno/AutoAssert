package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NullOptions extends ConfigurableCategoryOptions {

    @Override
    public AssertKind getKind() {
        return AssertKind.NULL;
    }

    @Override
    public AssertionComparator getComparator() {
        return new AssertionComparator() {
            public boolean match(Assertion assertion) {
                return assertion.isExpect() && (assertion.hasPropertyNamed("null") || assertion.hasCallWithArg("null"));
            }
        };
    }

    @Override
    public List<Isomorphism> getIsomorphisms() {
        return Arrays.asList(
                new Isomorphism("expect(LHS).to.be.null;", endsWithPropComparator),
                new Isomorphism("expect(LHS).to.equal(null);", endsWithCallComparator)
        );
    }
}
