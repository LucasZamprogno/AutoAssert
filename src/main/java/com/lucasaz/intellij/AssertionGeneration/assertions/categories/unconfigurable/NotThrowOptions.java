package com.lucasaz.intellij.AssertionGeneration.assertions.categories.unconfigurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;

public class NotThrowOptions extends UnconfigurableCategoryOptions {

    @Override
    public AssertKind getKind() {
        return AssertKind.NOT_THROW;
    }

    @Override
    public String getTemplate() {
        return "expect(LHS).to.not.throw;";
    }
}
