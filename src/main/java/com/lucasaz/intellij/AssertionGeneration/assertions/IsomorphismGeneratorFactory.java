package com.lucasaz.intellij.AssertionGeneration.assertions;

public class IsomorphismGeneratorFactory {
    public static IsomorphismGenerator generatorFromString(String template) {
        String[]tokens = template.split("(LHS)|(RHS)");
        if (tokens.length == 2) { // LHS only
            return new IsomorphismGenerator() {
                private String start = tokens[0];
                private String end = tokens[1];
                public String gen(String LHS, String RHS) {
                    return start + LHS + end;
                }
            };
        } else if (tokens.length == 3) {
            return new IsomorphismGenerator() {
                private String start = tokens[0];
                private String middle = tokens[1];
                private String end = tokens[2];
                public String gen(String LHS, String RHS) {
                    return start + LHS + middle + RHS + end;
                }
            };
        } else {
            return new IsomorphismGenerator() {
                public String gen(String LHS, String RHS) {
                    return "// Isomorphism template \"" + template + "\" is invalid";
                }
            };
        }

    }
}
