package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TypeOptions extends ConfigurableCategoryOptions {

    private boolean a(Assertion assertion) {
        return assertion.hasCallNamed("a");
    }

    private boolean an(Assertion assertion) {
        return assertion.hasCallNamed("an");
    }

    @Override
    public AssertKind getKind() {
        return AssertKind.TYPE;
    }

    @Override
    // This one will be replaced
    public AssertionComparator getComparator() {
        return new AssertionComparator() {
            public boolean match(Assertion assertion) {
                return assertion.isExpect() && (a(assertion) || an(assertion));
            }
        };
    }

    @Override
    public List<Isomorphism> getIsomorphisms() {
        return Arrays.asList(
                new Isomorphism("expect(LHS).to.be.a(RHS);", new AssertionComparator() {
                    @Override
                    public boolean match(Assertion assertion) {
                        return a(assertion);
                    }
                }),
                new Isomorphism("expect(LHS).to.be.an(RHS);", new AssertionComparator() {
                    @Override
                    public boolean match(Assertion assertion) {
                        return an(assertion);
                    }
                })
        );
    }
}
