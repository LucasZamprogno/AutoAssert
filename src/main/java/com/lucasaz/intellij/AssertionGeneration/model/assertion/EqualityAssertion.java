package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EqualityAssertion extends Assertion {
    boolean eqInclusion = false;
    boolean eqNull = false;
    boolean eqUndefined = false;
    boolean eqBoolean = false;
    boolean eqTypeof = false;
    boolean eqInstanceOf = false;
    boolean eqNumeric = false;
    boolean eqTruthiness = false;
    boolean eqLength = false;

    public EqualityAssertion(List<PropertyAccess> propertyAccesses, String filePath, int line) {
        super(propertyAccesses, filePath, line);
    }
}
