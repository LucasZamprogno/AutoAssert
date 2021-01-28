package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UndefinedOptions extends ConfigurableCategoryOptions {

    @Override
    public AssertKind getKind() {
        return AssertKind.UNDEFINED;
    }

    @Override
    public AssertionComparator getComparator() {
        return new AssertionComparator() {
            public boolean match(Assertion assertion) {
                return assertion.isExpect() &&
                        (assertion.hasPropertyNamed("undefined") ||
                        assertion.hasCallWithArg("undefined") ||
                        assertion.hasCallWithArg("\"undefined\"") ||
                        assertion.hasCallWithArg("'undefined'") ||
                        assertion.hasCallWithArg("`undefined`"));
            }
        };
    }

    @Override
    public List<Isomorphism> getIsomorphisms() {
        return Arrays.asList(
                new Isomorphism("expect(LHS).to.be.undefined;", endsWithPropComparator),
                new Isomorphism("expect(LHS).to.equal(undefined);", endsWithCallComparator)
        );
    }
}
