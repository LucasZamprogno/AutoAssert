package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;

import java.util.Arrays;
import java.util.List;

public class BooleanOptions extends ConfigurableCategoryOptions {

    @Override
    public AssertKind getKind() {
        return AssertKind.BOOL;
    }

    @Override
    public AssertionComparator getComparator() {
        return new AssertionComparator() {
            public boolean match(Assertion assertion) {
                return  assertion.isExpect() &&
                        (assertion.hasPropertyNamed("true") ||
                        assertion.hasPropertyNamed("false") ||
                        assertion.hasCallWithArg("true") ||
                        assertion.hasCallWithArg("false"));
            }
        };
    }

    @Override
    public List<Isomorphism> getIsomorphisms() {
        return Arrays.asList(
                new Isomorphism("expect(LHS).to.be.RHS;", endsWithPropComparator),
                new Isomorphism("expect(LHS).to.equal(RHS);", endsWithCallComparator)
        );
    }
}
