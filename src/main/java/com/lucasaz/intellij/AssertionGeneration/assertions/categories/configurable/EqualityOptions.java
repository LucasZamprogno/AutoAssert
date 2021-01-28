package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.PropertyAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EqualityOptions extends ConfigurableCategoryOptions {

    @Override
    public AssertKind getKind() {
        return AssertKind.EQUAL;
    }

    @Override
    public AssertionComparator getComparator() {
        return new AssertionComparator() {
            public boolean match(Assertion assertion) {
                return assertion.isExpect() &&
                        (assertion.hasCallNamed("equal") ||
                        assertion.hasCallNamed("eq"));
            }
        };
    }

    @Override
    public List<Isomorphism> getIsomorphisms() {
        return Arrays.asList(
                new Isomorphism("expect(LHS).to.equal(RHS);", new AssertionComparator() {
                    @Override
                    public boolean match(Assertion assertion) {
                        return assertion.hasCallNamed("equal");
                    }
                }),
                new Isomorphism("expect(LHS).to.eq(RHS);", new AssertionComparator() {
                    @Override
                    public boolean match(Assertion assertion) {
                        return assertion.hasCallNamed("eq");
                    }
                })
        );
    }
}
