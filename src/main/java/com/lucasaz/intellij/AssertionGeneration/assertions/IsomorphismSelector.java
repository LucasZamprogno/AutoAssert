package com.lucasaz.intellij.AssertionGeneration.assertions;

import java.util.HashMap;
import java.util.Map;

public class IsomorphismSelector {
    public Map<AssertKind, String> defaults;
    private Map<AssertKind, Isomorphism> isoMap;

    public IsomorphismSelector() {
        Map<AssertKind, Isomorphism> genMap = new HashMap<>();
        for (Category category : CategoryManager.getAllCategories()) {
            genMap.put(category.getKind(), category.getDefaultIsomorphism());
        }
        this.isoMap = genMap;
    }

    public IsomorphismSelector(Map<AssertKind, String> isomorphisms) {
        this();
        // Replace defaults
        for (Map.Entry<AssertKind,String> entry : isomorphisms.entrySet()) {
            AssertKind kind = entry.getKey();
            String template = entry.getValue();
            Isomorphism iso = CategoryManager.getCategory(kind).getIsomorphism(template);
            this.isoMap.put(kind, iso);
        }
    }

    public String getAssertion(AssertKind kind, String LHS, String RHS) {
        return this.isoMap.get(kind).fillInAssertion(LHS, RHS);
    }
}