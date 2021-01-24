package com.lucasaz.intellij.AssertionGeneration.assertions.categories.unconfigurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;

public class ExistOptions extends UnconfigurableCategoryOptions {

    @Override
    public AssertKind getKind() {
        return AssertKind.EXIST;
    }

    @Override
    public String getTemplate() {
        return "expect(LHS).to.exist;";
    }
}
