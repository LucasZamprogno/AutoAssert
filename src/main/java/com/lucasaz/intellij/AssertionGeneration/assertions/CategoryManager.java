package com.lucasaz.intellij.AssertionGeneration.assertions;

import com.lucasaz.intellij.AssertionGeneration.assertions.categories.configurable.*;
import com.lucasaz.intellij.AssertionGeneration.assertions.categories.unconfigurable.ExistOptions;
import com.lucasaz.intellij.AssertionGeneration.assertions.categories.unconfigurable.NotThrowOptions;
import com.lucasaz.intellij.AssertionGeneration.assertions.categories.unconfigurable.ThrowOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryManager {

    private static final Category[] CATEGORIES = {
            new Category(new NullOptions()),
            new Category(new UndefinedOptions()),
            new Category(new ExistOptions()),
            new Category(new ThrowOptions()),
            new Category(new NotThrowOptions()),
            new Category(new EqualityOptions()),
            new Category(new DeepEqualityOptions()),
            new Category(new LengthOptions()),
            new Category(new TypeOptions()),
            new Category(new BooleanOptions()),
    };


    public static Category getCategory(AssertKind kind) {
        for (Category category : CATEGORIES) {
            if (category.getKind() == kind) {
                return category;
            }
        }
        return null; // For compiler, should never happen
    }

    public static List<Category> getConfigurableCategories() {
        List<Category> configurableCategories = new ArrayList<>();
        for (Category category : CATEGORIES) {
            if (category.isConfigurable()) {
                configurableCategories.add(category);
            }
        }
        return configurableCategories;
    }

    public static List<Category> getAllCategories() {
        return Arrays.asList(CATEGORIES);
    }
}
