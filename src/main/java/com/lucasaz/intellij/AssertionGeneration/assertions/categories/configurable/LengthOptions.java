package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LengthOptions extends ConfigurableCategoryOptions {

    private boolean hasLengthAccessInLHS(Assertion assertion) {
        return assertion.getLHS().toString().contains(".length");
    }

    private boolean usesLengthProp(Assertion assertion) {
        return assertion.hasPropertyNamed("length");
    }

    @Override
    public AssertKind getKind() {
        return AssertKind.LENGTH;
    }

    @Override
    public AssertionComparator getComparator() {
        return new AssertionComparator() {
            public boolean match(Assertion assertion) {
                return assertion.isExpect() && (hasLengthAccessInLHS(assertion) || usesLengthProp(assertion));
            }
        };
    }

    @Override
    public List<Isomorphism> getIsomorphisms() {
        return Arrays.asList(
                new Isomorphism("expect(LHS).to.have.length(RHS);", new AssertionComparator() {
                    @Override
                    public boolean match(Assertion assertion) {
                        return usesLengthProp(assertion);
                    }
                }),
                new Isomorphism("expect(LHS.length).to.equal(RHS);", new AssertionComparator() {
                    @Override
                    public boolean match(Assertion assertion) {
                        return hasLengthAccessInLHS(assertion);
                    }
                })
        );
    }
}
