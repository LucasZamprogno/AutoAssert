package com.lucasaz.intellij.AssertionGeneration.assertions.categories;

import com.lucasaz.intellij.AssertionGeneration.assertions.AssertKind;
import com.lucasaz.intellij.AssertionGeneration.assertions.AssertionComparator;
import com.lucasaz.intellij.AssertionGeneration.assertions.Isomorphism;

import java.util.List;

public abstract class CategoryOptions {
    public abstract AssertKind getKind();
    public abstract boolean isConfigurable();

    public abstract AssertionComparator getComparator();
    public abstract List<Isomorphism> getIsomorphisms();
}
