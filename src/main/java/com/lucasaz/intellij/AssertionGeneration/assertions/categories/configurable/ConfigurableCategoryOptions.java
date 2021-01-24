package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.categories.CategoryOptions;

public abstract class ConfigurableCategoryOptions extends CategoryOptions {
    @Override
    public boolean isConfigurable() {
        return true;
    }
}
