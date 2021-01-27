package com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.categories.CategoryOptions;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;

public abstract class ConfigurableCategoryOptions extends CategoryOptions {
    @Override
    public boolean isConfigurable() {
        return true;
    }

    protected final static AssertionComparator endsWithCallComparator = new AssertionComparator() {
        public boolean match(Assertion assertion) {
            // TODO handle .and
            return assertion.lastPropertyIsCall();
        }
    };

    protected final static AssertionComparator endsWithPropComparator = new AssertionComparator() {
        public boolean match(Assertion assertion) {
            // TODO handle .and
            return !assertion.lastPropertyIsCall();
        }
    };
}
