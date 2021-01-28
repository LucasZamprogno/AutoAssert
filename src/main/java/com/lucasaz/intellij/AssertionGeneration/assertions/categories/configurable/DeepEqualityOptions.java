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

public class DeepEqualityOptions extends ConfigurableCategoryOptions {

    private boolean isDeepEqual(Assertion assertion) {
        return assertion.hasPropertyNamed("deep") &&
                assertion.hasCallNamed("equal");
    }

    private boolean isEql(Assertion assertion) {
        return assertion.hasCallNamed("eql");
    }

    @Override
    public AssertKind getKind() {
        return AssertKind.DEEP_EQUAL;
    }

    @Override
    public AssertionComparator getComparator() {
        return new AssertionComparator() {
            public boolean match(Assertion assertion) {
                // Can't just check for deep, since one doesn't use it
                // and it could match a bunch of unrelated things
                return assertion.isExpect() && (isDeepEqual(assertion) || isEql(assertion));
            }
        };
    }

    @Override
    public List<Isomorphism> getIsomorphisms() {
        return Arrays.asList(
                new Isomorphism("expect(LHS).to.deep.equal(RHS);", new AssertionComparator() {
                    @Override
                    public boolean match(Assertion assertion) {
                        return  isDeepEqual(assertion);
                    }
                }),
                new Isomorphism("expect(LHS).to.eql(RHS);", new AssertionComparator() {
                    @Override
                    public boolean match(Assertion assertion) {
                        return isEql(assertion);
                    }
                })
        );
    }
}
