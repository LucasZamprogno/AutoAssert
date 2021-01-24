package com.lucasaz.intellij.AssertionGeneration.assertions;

import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;

public abstract class AssertionComparator {
    abstract boolean match(Assertion assertion);
}
