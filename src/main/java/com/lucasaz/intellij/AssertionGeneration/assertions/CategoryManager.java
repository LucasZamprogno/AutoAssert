package com.lucasaz.intellij.AssertionGeneration.assertions;

import java.util.ArrayList;
import java.util.List;

public class CategoryManager {
    private static final String[] NULL_OPTIONS = {"expect(LHS).to.be.null;", "expect(LHS).to.equal(null);"};
    public static final Category NULL = new Category(AssertKind.NULL, NULL_OPTIONS);

    private static final String[] UNDEFINED_OPTIONS = {"expect(LHS).to.be.undefined;", "expect(LHS).to.equal(undefined);"};
    public static final Category UNDEFINED = new Category(AssertKind.UNDEFINED, UNDEFINED_OPTIONS);

    private static final String[] EXIST_OPTIONS = {"expect(LHS).to.exist;"};
    public static final Category EXIST = new Category(AssertKind.EXIST, EXIST_OPTIONS);

    private static final String[] THROW_OPTIONS = {"expect(LHS).to.throw;"};
    public static final Category THROW = new Category(AssertKind.THROW, THROW_OPTIONS);

    private static final String[] NOT_THROW_OPTIONS = {"expect(LHS).to.not.throw;"};
    public static final Category NOT_THROW = new Category(AssertKind.NOT_THROW, NOT_THROW_OPTIONS);

    private static final String[] EQUALITY_OPTIONS = {"expect(LHS).to.equal(RHS);", "expect(LHS).to.eq(RHS);"};
    public static final Category EQUAL = new Category(AssertKind.EQUAL, EQUALITY_OPTIONS);

    private static final String[] DEEP_EQUALITY_OPTIONS = {"expect(LHS).to.deep.equal(RHS);", "expect(LHS).to.eql(RHS);"};
    public static final Category DEEP_EQUAL = new Category(AssertKind.DEEP_EQUAL, DEEP_EQUALITY_OPTIONS);

    private static final String[] LENGTH_OPTIONS = {"expect(LHS).to.have.length(RHS);", "expect(LHS.length).to.equal(RHS);"};
    public static final Category LENGTH = new Category(AssertKind.LENGTH, LENGTH_OPTIONS);

    private static final String[] TYPE_OPTIONS = {"expect(LHS).to.be.a(RHS);", "expect(LHS).to.be.an(RHS);"};
    public static final Category TYPE = new Category(AssertKind.TYPE, TYPE_OPTIONS);

    private static final String[] BOOLEAN_OPTIONS = {"expect(LHS).to.be.RHS;", "expect(LHS).to.equal(RHS);"};
    public static final Category BOOL = new Category(AssertKind.BOOL, BOOLEAN_OPTIONS);

    public static Category getCategory(AssertKind kind) {
        switch(kind) {
            case UNDEFINED: return CategoryManager.UNDEFINED;
            case EXIST: return CategoryManager.EXIST;
            case THROW: return CategoryManager.THROW;
            case NOT_THROW: return CategoryManager.NOT_THROW;
            case EQUAL: return CategoryManager.EQUAL;
            case DEEP_EQUAL: return CategoryManager.DEEP_EQUAL;
            case LENGTH: return CategoryManager.LENGTH;
            case TYPE: return CategoryManager.TYPE;
            case BOOL: return CategoryManager.BOOL;
            default: return CategoryManager.NULL; // For compiler, should never happen
        }
    }

    public static List<Category> getConfigurableCategories() {
        List<Category> list = new ArrayList<>();
        list.add(NULL);
        list.add(UNDEFINED);
        list.add(EQUAL);
        list.add(DEEP_EQUAL);
        list.add(LENGTH);
        list.add(TYPE);
        list.add(BOOL);
        return list;
    }

    public static List<Category> getAllCategories() {
        List<Category> list = CategoryManager.getConfigurableCategories();
        list.add(EXIST);
        list.add(THROW);
        list.add(NOT_THROW);
        return list;
    }
}
