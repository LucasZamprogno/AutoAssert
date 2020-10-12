package com.lucasaz.intellij.AssertionGeneration.assertions;

public class IsomorphismSelector {

    public static String getAssertion(AssertKind kind, String LHS, String RHS) {
        switch (kind) {
            case NULL:
                return "expect(" + LHS + ").to.be.null;";
            case UNDEFINED:
                return "expect(" + LHS + ").to.be.undefined;";
            case EXIST:
                return "expect(" + LHS + ").to.exist;";
            case THROW:
                return "expect(" + LHS + ").to.throw;";
            case NOT_THROW:
                return "expect(" + LHS + ").to.not.throw;";
            case EQUAL:
                return "expect(" + LHS + ").to.equal(" + RHS + ");";
            case DEEP_EQUAL:
                return "expect(" + LHS + ").to.deep.equal(" + RHS + ");";
            case LENGTH:
                return "expect(" + LHS + ").to.have.length(" + RHS + ");";
            case TYPE:
                return "expect(" + LHS + ").to.be.a(" + RHS + ");";
        }
        return "// Wat";
    }
}
