package com.lucasaz.intellij.AssertionGeneration.assertions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AssertionSelector {

    public static String getAllAssertions(JSONObject observed, String name, String ws, boolean verbose) {
        return AssertionSelector.getAllAssertions(observed, name, ws, verbose, new IsomorphismSelector());
    }

    public static String getAllAssertions(JSONObject observed, String name, String ws, boolean verbose, IsomorphismSelector isoSelector) {
        String type = (String) observed.get("type");
        String typeQuoted = "\"" + type + "\"";
        String lsp = "\n"; // We'll leave this hardcoded for now. Other option is System.getProperty("line.separator");
        String val;
        int len;
        List<String> assertions = new ArrayList<String>();
        switch (type) {
            case "boolean":
                boolean bool = observed.getBoolean("value");
                val = Boolean.toString(bool);
                if (verbose) {
                    assertions.add(isoSelector.getAssertion(AssertKind.EXIST, name, val));
                    assertions.add(isoSelector.getAssertion(AssertKind.TYPE, name, typeQuoted));
                }
                assertions.add(isoSelector.getAssertion(AssertKind.BOOL, name, val));
                break;
            case "number":
                val = observed.get("value").toString();
                if (verbose) {
                    assertions.add(isoSelector.getAssertion(AssertKind.EXIST, name, val));
                    assertions.add(isoSelector.getAssertion(AssertKind.TYPE, name, typeQuoted));
                }
                assertions.add(isoSelector.getAssertion(AssertKind.EQUAL, name, val));
                break;
            case "string": // Only diff is the quotes in the deep equal
                val = (String) observed.get("value");
                val = "\"" + val + "\"";
                if (verbose) {
                    assertions.add(isoSelector.getAssertion(AssertKind.EXIST, name, val));
                    assertions.add(isoSelector.getAssertion(AssertKind.TYPE, name, typeQuoted));
                }
                assertions.add(isoSelector.getAssertion(AssertKind.EQUAL, name, val));
                break;
            case "symbol":
                val = (String) observed.get("value");
                val = "\"" + val + "\"";
                if (verbose) {
                    assertions.add(isoSelector.getAssertion(AssertKind.EXIST, name, val));
                    assertions.add(isoSelector.getAssertion(AssertKind.TYPE, name, typeQuoted));
                }
                assertions.add(isoSelector.getAssertion(AssertKind.EQUAL, name + ".toString()", val));
                break;
            case "function":
                if (verbose) {
                    assertions.add(isoSelector.getAssertion(AssertKind.EXIST, name, null));
                }
                assertions.add(isoSelector.getAssertion(AssertKind.TYPE, name, typeQuoted));
                int numArgs = (int) observed.get("args");
                if (numArgs == 0) {
                    boolean throwsBool = (boolean) observed.get("throws");
                    if (throwsBool) {
                        assertions.add(isoSelector.getAssertion(AssertKind.THROW, name, null));
                    } else {
                        assertions.add(isoSelector.getAssertion(AssertKind.NOT_THROW, name, null));
                    }
                }
                break;
            case "null":
                assertions.add(isoSelector.getAssertion(AssertKind.NULL, name, null));
                break;
            case "undefined":
                assertions.add(isoSelector.getAssertion(AssertKind.UNDEFINED, name, null));
                break;
            case "array":
                JSONArray arr = (JSONArray) observed.get("value");
                val = arr.toString();
                len = (int) observed.get("length");
                if (verbose) {
                    assertions.add(isoSelector.getAssertion(AssertKind.EXIST, name, val));
                    assertions.add(isoSelector.getAssertion(AssertKind.TYPE, name, typeQuoted));
                    assertions.add(isoSelector.getAssertion(AssertKind.LENGTH, name, String.valueOf(len)));
                }
                assertions.add(isoSelector.getAssertion(AssertKind.DEEP_EQUAL, name, val));
                break;
            case "set":
                JSONArray setArr = (JSONArray) observed.get("value");
                val = setArr.toString();
                len = (int) observed.get("length");
                if (verbose) {
                    assertions.add(isoSelector.getAssertion(AssertKind.EXIST, name, val));
                    assertions.add(isoSelector.getAssertion(AssertKind.TYPE, name, "\"Set\"")); // hardcoding set for caps
                    assertions.add(isoSelector.getAssertion(AssertKind.LENGTH, name, String.valueOf(len))); // TODO this won't work!
                }
                assertions.add(isoSelector.getAssertion(AssertKind.DEEP_EQUAL, name, val));
                break;
            case "promise":
                if (verbose) {
                    assertions.add(isoSelector.getAssertion(AssertKind.EXIST, name, null));
                }
                assertions.add(isoSelector.getAssertion(AssertKind.TYPE, name, "\"promise\""));
                break;
            case "error":
                if (verbose) {
                    assertions.add(isoSelector.getAssertion(AssertKind.EXIST, name, null));
                }
                assertions.add(isoSelector.getAssertion(AssertKind.TYPE, name, "\"error\""));
                break;
            case "object":
                JSONObject subObj = (JSONObject) observed.get("value");
                val = subObj.toString();
                if (verbose) {
                    assertions.add(isoSelector.getAssertion(AssertKind.EXIST, name, val));
                    assertions.add(isoSelector.getAssertion(AssertKind.TYPE, name, typeQuoted));
                }
                assertions.add(isoSelector.getAssertion(AssertKind.DEEP_EQUAL, name, val));
                break;
            case "fail":
                assertions.add("// Assertion generation timed out");
                break;
            default:
                // Should never happen
                assertions.add("// Assertion generation failed, please make sure you selected the appropriate");
        }
        return AssertionSelector.buildString(assertions, ws, lsp);
    }

    private static String buildString(List<String> lines, String whitespace, String lineSep) {
        StringBuilder toReturn = new StringBuilder();
        for (String line : lines) {
            toReturn.append(whitespace).append(line).append(lineSep);
        }
        return toReturn.toString();
    }
}
