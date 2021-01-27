package com.lucasaz.intellij.AssertionGeneration.assertions.categories.unconfigurable;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;
import com.lucasaz.intellij.AssertionGeneration.assertions.categories.CategoryOptions;

import java.util.Collections;
import java.util.List;

public abstract class UnconfigurableCategoryOptions extends CategoryOptions {

    public abstract String getTemplate();

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AssertionComparator getComparator() {
        return null; // should never happen -- not configurable
    }

    @Override
    public List<Isomorphism> getIsomorphisms() {
        // can be null because this will never get called
        return Collections.singletonList(new Isomorphism(getTemplate(), null));
    }
}
