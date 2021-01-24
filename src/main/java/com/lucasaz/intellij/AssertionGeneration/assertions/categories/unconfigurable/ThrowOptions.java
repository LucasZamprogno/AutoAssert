package com.lucasaz.intellij.AssertionGeneration.assertions.categories.unconfigurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;

public class ThrowOptions extends UnconfigurableCategoryOptions {

    @Override
    public AssertKind getKind() {
        return AssertKind.THROW;
    }

    @Override
    public String getTemplate() {
        return "expect(LHS).to.throw;";
    }
}
