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

    @Override
    public String toJSON() {
        String starterJSON = super.toJSON();
        String removedClosingBrace = starterJSON.replaceAll("}$", ",");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(removedClosingBrace);
        stringBuilder.append("\"equality\": [");

        if (eqInclusion) {
            stringBuilder.append("INCLUSION,");
        }
        if (eqNull) {
            stringBuilder.append("NULL,");
        }
        if (eqUndefined) {
            stringBuilder.append("UNDEFINED,");
        }
        if (eqBoolean) {
            stringBuilder.append("BOOLEAN,");
        }
        if (eqTypeof) {
            stringBuilder.append("TYPEOF,");
        }
        if (eqInstanceOf) {
            stringBuilder.append("INSTANCEOF,");
        }
        if (eqNumeric) {
            stringBuilder.append("NUMERIC,");
        }
        if (eqTruthiness) {
            stringBuilder.append("TRUTHINESS,");
        }
        if (eqLength) {
            stringBuilder.append("LENGTH,");
        }

        if (stringBuilder.charAt(stringBuilder.length() - 1) == ',') {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }

        stringBuilder.append("]}");
        return stringBuilder.toString();
    }
}
