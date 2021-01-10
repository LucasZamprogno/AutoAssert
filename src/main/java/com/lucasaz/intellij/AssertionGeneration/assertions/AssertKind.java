package com.lucasaz.intellij.AssertionGeneration.assertions;

public enum AssertKind {
    EXIST ("AssertionGenerationExistIsoKey"),
    TYPE("AssertionGenerationTypeIsoKey"),
    EQUAL("AssertionGenerationEqualIsoKey"),
    DEEP_EQUAL("AssertionGenerationDeepEqualIsoKey"),
    THROW("AssertionGenerationThrowIsoKey"),
    NOT_THROW("AssertionGenerationNotThrowIsoKey"),
    NULL("AssertionGenerationNullIsoKey"),
    UNDEFINED("AssertionGenerationUndefIsoKey"),
    LENGTH("AssertionGenerationLengthIsoKey"),
    BOOL ("AssertionGenerationBoolIsoKey");
    
    public final String storageKey;
    
    AssertKind(String storageKey) {
        this.storageKey = storageKey;
    }
}
