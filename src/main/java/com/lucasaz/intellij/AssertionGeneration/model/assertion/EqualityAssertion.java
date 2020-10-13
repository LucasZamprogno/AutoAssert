package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

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
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        JSONArray equalityArray = new JSONArray();
        if (eqInclusion) {
            equalityArray.put("INCLUSION");
        }
        if (eqNull) {
            equalityArray.put("NULL");
        }
        if (eqUndefined) {
            equalityArray.put("UNDEFINED");
        }
        if (eqBoolean) {
            equalityArray.put("BOOLEAN");
        }
        if (eqTypeof) {
            equalityArray.put("TYPEOF");
        }
        if (eqInstanceOf) {
            equalityArray.put("INSTANCEOF");
        }
        if (eqNumeric) {
            equalityArray.put("NUMERIC");
        }
        if (eqTruthiness) {
            equalityArray.put("TRUTHINESS");
        }
        if (eqLength) {
            equalityArray.put("LENGTH");
        }

        json.put("equality", equalityArray);
        return json;
    }
}
