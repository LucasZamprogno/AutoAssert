package com.lucasaz.intellij.AssertionGeneration.services;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.*;
import com.lucasaz.intellij.AssertionGeneration.visitors.impl.TypeScriptVisitor;

import java.util.Arrays;
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
            "equalIgnoreSpaces"
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

//    private static boolean targetHasInSubtree(V8Object target, String kind) {
//        return targetHasInSubtree(target, Collections.singletonList(kind));
//    }
//
//    private static boolean targetHasInSubtree(V8Object target, List<String> kinds) {
//        // TODO don't use this
//        final boolean[] hasIt = {false};
//        try {
//            TypeScriptVisitor typeScriptVisitor = new TypeScriptVisitor() {
//                @Override
//                public void visit(V8Object node) {
//                    for (String kind : kinds) {
//                        if (isKind(node, kind)) {
//                            hasIt[0] = true;
//                            return;
//                        }
//                    }
//                    super.visit(node);
//                }
//            };
//            typeScriptVisitor.visit(target);
//            typeScriptVisitor.close();
//        } catch (Exception exception) {
//            // Do nothing
//        }
//        return hasIt[0];
//    }

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

    private static boolean checkLHSForArgumentsWhere(Assertion assertion, Map<Target, V8Object> mapToV8Nodes, PoorMansFirstOrderFunction function) {
        boolean lhsHasCallWithKeywords = false;
        if (assertion.isExpectingValue()) {
            Target lhsArgument = assertion.getLHS();
            V8Object lhsV8Argument = mapToV8Nodes.get(lhsArgument);
            lhsHasCallWithKeywords = function.call(lhsV8Argument);
        }
        return lhsHasCallWithKeywords;
    }

    private static boolean checkRHSForArgumentsWhere(Assertion assertion, Map<Target, V8Object> mapToV8Nodes, PoorMansFirstOrderFunction function) {
        List<Target> targets = assertion.getRHS();
        for (Target argument : targets) {
            V8Object rhsV8Argument = mapToV8Nodes.get(argument);
            if (function.call(rhsV8Argument)) {
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
                "has"
        );
        return checkLHSForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                return targetHasCallWithNameIn(v8Target, inclusionKeywords);
            }
        });
    }

    private static boolean isEqualityNull(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        /*
        expect(res).to.equal(null);            ✅
        expect(res === null).to.equal(true);   ❌
        expect(null == res).to.equal(false);   ❌
        expect(res.equals(null)).to.be(false); ❌
        */
        return checkRHSForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
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
        expect(res === undefined).to.equal(true);          ✅
        expect(res == undefined).to.equal(true);           ✅
        expect(res).to.deep.equal(undefined);              ✅
        expect(res.equals(undefined)).to.be(false);        ❌
        */

        if (assertion.toString().contains("\"undefined\"")) {
            // check for a typeof
            return checkLHSForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
                @Override
                boolean call(V8Object v8Target) {
                    // targetHasInSubtree(v8Target, "TypeOfKeyword");
                    return isExactKind(v8Target, "TypeOfKeyword") || targetHasKindInBinop(v8Target, "TypeOfKeyword");
                }
            });
        } else if (assertion.toString().contains("undefined")) {
            // check for binop or deep equal
            return checkRHSForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
                @Override
                boolean call(V8Object v8Target) {
                    return isExactKind(v8Target, "UndefinedKeyword");
                }
            }) || checkLHSForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
                @Override
                boolean call(V8Object v8Target) {
                    // return isExactKind(v8Target, "BinaryExpression") && targetHasInSubtree(v8Target, "UndefinedKeyword");
                    return targetHasKindInBinop(v8Target, "UndefinedKeyword");
                }
            });
        } else {
            return false;
        }
    }

    private static boolean isEqualityBoolean(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        /*
        expect(res).to.equal(true);           ✅
        expect(res === false).to.equal(true); ❌
        */
        return checkRHSForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
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
        return checkLHSForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                // return targetHasInSubtree(v8Target, "TypeOfKeyword");
                return isExactKind(v8Target, "TypeOfKeyword") || targetHasKindInBinop(v8Target, "TypeOfKeyword");
            }
        });
    }

    private static boolean isEqualityInstanceOf(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        /*
        expect(res instanceof Class).to.equal(true);           ✅
        expect(res instanceof Class === false).to.equal(true); ✅
        */
        return checkLHSForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                // return targetHasInSubtree(v8Target, "InstanceOfKeyword");
                return isExactKind(v8Target, "InstanceOfKeyword") || targetHasKindInBinop(v8Target, "InstanceOfKeyword");
            }
        });
    }

    private static boolean isEqualityNumeric(Assertion assertion, Map<Target, V8Object> mapToV8Nodes) {
        // < > <= >= =>
        checkLHSForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                // return targetHasInSubtree(v8Target, Arrays.asList(...));
                return targetIsBinopWithOpIn(v8Target, Arrays.asList(
                        "LessThanToken",
                        "GreaterThanToken",
                        "LessThanEqualsToken",
                        "GreaterThanEqualsToken",
                        "EqualsGreaterThanToken"
                ));
            }
        });
        return false;
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
        return checkLHSForArgumentsWhere(assertion, mapToV8Nodes, new PoorMansFirstOrderFunction() {
            @Override
            boolean call(V8Object v8Target) {
                return targetHasPropertyWithNameIn(v8Target, lengthKeywords);
            }
        });
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

        return equalityAssertion;
    }

    private static abstract class PoorMansFirstOrderFunction {
        abstract boolean call(V8Object v8Target);
    }
}
