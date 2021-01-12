package com.lucasaz.intellij.AssertionGeneration.services;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.*;
import com.lucasaz.intellij.AssertionGeneration.visitors.impl.TypeScriptVisitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EqualitySpecifier {
    private static final List<String> equalityKeywords = Arrays.asList(
            "equal",
            "eq",
            "strictEqual",
            "deepEqual",
            "toEqual",
            "eql",
            "notEqual",
            "deepStrictEqual",
            "equals",
            "toStrictEqual",
            "equalIgnoreSpaces",
            "notStrictEqual",
            "notDeepStrictEqual",
            "notDeepEqual",
            "toBe"
    );

    public static boolean isInEqualityCategory(Assertion assertion) {
        for (PropertyAccess propertyAccess : assertion.getPropertyAccesses()) {
            String keyword = propertyAccess.getText();
            if (equalityKeywords.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static boolean targetHasKindInBinop(V8Object target, String kind) {
        boolean hasIt = false;
        try {
            TypeScriptVisitor typeScriptVisitor = new TypeScriptVisitor();
            boolean isBinop = typeScriptVisitor.isKind(target, "BinaryExpression");
            if (isBinop) {
                V8Object left = target.getObject("left");
                V8Object right = target.getObject("right");
                hasIt = typeScriptVisitor.isKind(left, kind) || typeScriptVisitor.isKind(right, kind);
            }
            typeScriptVisitor.close();
        } catch (Exception exception) {
            // Do nothing
        }
        return hasIt;
    }

    private static boolean targetIsBinopWithOpIn(V8Object target, List<String> kinds) {
        boolean hasIt = false;
        try {
            TypeScriptVisitor typeScriptVisitor = new TypeScriptVisitor();
            boolean isBinop = typeScriptVisitor.isKind(target, "BinaryExpression");
            if (isBinop) {
                V8Object operatorToken = target.getObject("operatorToken");
                for (String kind : kinds) {
                    if (typeScriptVisitor.isKind(operatorToken, kind)) {
                        hasIt = true;
                        break;
                    }
                }
            }
            typeScriptVisitor.close();
        } catch (Exception exception) {
            // Do nothing
        }
        return hasIt;
    }

    private static boolean targetIsEmpty(V8Object v8Target) {
        final boolean[] isEmpty = {false};
        try {
            TypeScriptVisitor typeScriptVisitor = new TypeScriptVisitor();
            if (typeScriptVisitor.isKind(v8Target, "StringLiteral")) {
                isEmpty[0] = v8Target.getString("text").equals("");
            } else if (typeScriptVisitor.isKind(v8Target, "ObjectLiteralExpression")) {
                isEmpty[0] = v8Target.getObject("properties").getInteger("length") == 0;
            } else if (typeScriptVisitor.isKind(v8Target, "ArrayLiteralExpression")) {
                isEmpty[0] = v8Target.getObject("elements").getInteger("length") == 0;
            }
            typeScriptVisitor.close();
        } catch (Exception exception) {
            // Do nothing
        }
        return isEmpty[0];
    }

    private static boolean targetHasPropertyWithNameIn(V8Object target, List<String> propertyNames) {
        final boolean[] inPropertyNames = {false};
        try {
            TypeScriptVisitor typeScriptVisitor = new TypeScriptVisitor() {
                @Override
                protected void visitPropertyAccessExpression(V8Object propertyAccessExpression) {
                    String keyword = propertyAccessExpression
                            .getObject("name")
                            .executeStringFunction("getText", new V8Array(ts.getRuntime()));
                    if (propertyNames.contains(keyword)) {
                        inPropertyNames[0] = true;
                    } else {
                        visitChildren(propertyAccessExpression);
                    }
                }
            };
            typeScriptVisitor.visit(target);
            typeScriptVisitor.close();
        } catch (Exception exception) {
            // Do nothing
        }
        return inPropertyNames[0];
    }

    private static boolean targetHasCallWithNameIn(V8Object target, List<String> callNames) {
        final boolean[] isInCallTypes = {false};
        try {
            TypeScriptVisitor typeScriptVisitor = new TypeScriptVisitor() {
                @Override
                protected void visitCallExpression(V8Object callExpression) {
                    String keyword = "";
                    V8Object parentExpression = callExpression.getObject("expression");
                    if (isKind(parentExpression, "Identifier")) {
                        keyword = parentExpression
                                .executeStringFunction("getText", new V8Array(ts.getRuntime()));
                    } else if (isKind(parentExpression, "PropertyAccessExpression")) {
                        keyword = parentExpression
                                .getObject("name")
                                .executeStringFunction("getText", new V8Array(ts.getRuntime()));
                    }
                    if (callNames.contains(keyword)) {
                        isInCallTypes[0] = true;
                    } else {
                        visit(parentExpression);
                    }
                }
            };
            typeScriptVisitor.visit(target);
            typeScriptVisitor.close();
        } catch (Exception exception) {
            // Do nothing
        }
        return isInCallTypes[0];
    }

    private static boolean checkForArgumentsWhere(Assertion assertion, Map<Target, V8Object> mapToV8Nodes, PoorMansFirstOrderFunction function) {
        List<Target> targets = assertion.getAllArguments();
        for (Target argument : targets) {
            V8Object v8Argument = mapToV8Nodes.get(argument);
            if (function.call(v8Argument)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isExactKind(V8Object v8Target, String kind) {
        boolean isKind = false;
        try {
            TypeScriptVisitor typeScriptVisitor = new TypeScriptVisitor();
            isKind = typeScriptVisitor.isKind(v8Target, kind);
            typeScriptVisitor.close();
        } catch (Exception exception) {
            // Do nothing
        }
        return isKind;
    }

    private static boolean isEqualityInclusion(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        final List<String> inclusionKeywords = Arrays.asList(
                "includes",
                "contains",
                "has",
                "indexOf"
        );
        return checkForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                return targetHasCallWithNameIn(v8Target, inclusionKeywords);
            }
        });
    }

    private static boolean isEqualityNull(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        /*
        expect(res).to.equal(null);            ✅
        expect(null).to.equal(res);            ✅
        expect(res === null).to.equal(true);   ❌
        expect(null == res).to.equal(false);   ❌
        expect(res.equals(null)).to.be(false); ❌
        */
        return checkForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                return isExactKind(v8Target, "NullKeyword");
            }
        });
    }

    private static boolean isEqualityUndefined(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        /*
        expect(typeof res === "undefined").to.equal(true); ✅
        expect(typeof res == "undefined").to.equal(true);  ✅
        expect(typeof res).to.equal("undefined");          ✅
        expect(res).to.deep.equal(undefined);              ✅
        expect(res === undefined).to.equal(true);          ❌
        expect(res == undefined).to.equal(true);           ❌
        expect(res.equals(undefined)).to.be(false);        ❌
        */

        if (assertion.toString().contains("\"undefined\"")) {
            // check for a typeof
            return checkForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
                @Override
                boolean call(V8Object v8Target) {
                    // targetHasInSubtree(v8Target, "TypeOfKeyword");
                    return isExactKind(v8Target, "UndefinedKeyword") || (isExactKind(v8Target, "Identifier") && "undefined".equals(v8Target.get("text")));
                }
            });
        } else if (assertion.toString().contains("undefined")) {
            // check for binop or deep equal
            return checkForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
                @Override
                boolean call(V8Object v8Target) {
                    return targetHasKindInBinop(v8Target, "UndefinedKeyword") || (isExactKind(v8Target, "Identifier") && "undefined".equals(v8Target.get("text")));
                }
            });
        } else {
            return false;
        }
    }

    private static boolean isEqualityBoolean(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        /*
        expect(res).to.equal(true);           ✅
        expect(true).to.equal(res);           ✅
        expect(res === false).to.equal(true); ❌
        */
        return checkForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                return isExactKind(v8Target, "TrueKeyword") || isExactKind(v8Target, "FalseKeyword");
            }
        });
    }

    private static boolean isEqualityTypeof(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        /*
        expect(typeof res).to.equal("string");          ✅
        expect(typeof res === "string").to.equal(true); ✅
        */
        return checkForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                // return targetHasInSubtree(v8Target, "TypeOfKeyword");
                return isExactKind(v8Target, "TypeOfExpression") || targetHasKindInBinop(v8Target, "TypeOfExpression");
            }
        });
    }

    private static boolean isEqualityInstanceOf(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        /*
        expect(res instanceof Class).to.equal(true);           ✅
        expect(res instanceof Class === false).to.equal(true); ✅
        */
        return checkForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                // return targetHasInSubtree(v8Target, "InstanceOfKeyword");
                return targetIsBinopWithOpIn(v8Target, Collections.singletonList("InstanceOfKeyword"));
            }
        });
    }

    private static boolean isEqualityNumeric(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        // < > <= >= =>
        return checkForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                return targetIsBinopWithOpIn(v8Target, Arrays.asList(
                        "LessThanToken",
                        "GreaterThanToken",
                        "LessThanEqualsToken",
                        "GreaterThanEqualsToken",
                        "EqualsGreaterThanToken"
                ));
            }
        });
    }

    private static boolean isEqualityTruthiness(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        /*
        expect(!!res).to.be(true); ❌
        */
        return false;
    }

    private static boolean isEqualityLength(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        final List<String> lengthKeywords = Arrays.asList(
                "size",
                "length"
        );
        return checkForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                return targetHasPropertyWithNameIn(v8Target, lengthKeywords) || targetIsEmpty(v8Target);
            }
        });
    }

    private static boolean isEqualityCall(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        List<Target> arguments = assertion.getAllArguments();
        for (Target argument : arguments) {
            if (argument.getText().endsWith(".callCount")) {
                return true;
            }
        }
        return false;
    }

    // REQUIRES: assertion is an equality assertion
    public static EqualityAssertion getEqualityDetails(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        EqualityAssertion equalityAssertion = new EqualityAssertion(assertion.getPropertyAccesses(), assertion.getFilePath(), assertion.getLine());
        // Inclusion
        equalityAssertion.setEqInclusion(isEqualityInclusion(assertion, mapToV8Nodes));
        // Null
        equalityAssertion.setEqNull(isEqualityNull(assertion, mapToV8Nodes));
        // Undefined
        equalityAssertion.setEqUndefined(isEqualityUndefined(assertion, mapToV8Nodes));
        // Boolean
        equalityAssertion.setEqBoolean(isEqualityBoolean(assertion, mapToV8Nodes));
        // Typeof
        equalityAssertion.setEqTypeof(isEqualityTypeof(assertion, mapToV8Nodes));
        // Instanceof
        equalityAssertion.setEqInstanceOf(isEqualityInstanceOf(assertion, mapToV8Nodes));
        // Numeric
        equalityAssertion.setEqNumeric(isEqualityNumeric(assertion, mapToV8Nodes));
        // truthiness
        equalityAssertion.setEqTruthiness(isEqualityTruthiness(assertion, mapToV8Nodes));
        // length
        equalityAssertion.setEqLength(isEqualityLength(assertion, mapToV8Nodes));
        // calls
        equalityAssertion.setEqCall(isEqualityCall(assertion, mapToV8Nodes));

        return equalityAssertion;
    }

    private static abstract class PoorMansFirstOrderFunction {
        abstract boolean call(V8Object v8Target);
    }
}
