package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import com.eclipsesource.v8.V8Object;
import com.lucasaz.intellij.AssertionGeneration.services.TypeScript;
import com.lucasaz.intellij.AssertionGeneration.visitors.impl.TypeScriptVisitor;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Test {
    List<Assertion> assertions;
    String filePath;
    int line;
    List<List<Assertion>> assertionBlocks;

    public Test(String filePath, V8Object testNode, V8Object sourceFile) throws Exception {
        if (isTest(testNode)) {
            this.filePath = filePath;
            this.line = TypeScript.getInstance().getLine(testNode, sourceFile);
            this.assertions = new ArrayList<>();
            TypeScriptVisitor testVisitor = new TypeScriptVisitor() {
                @Override
                protected void visitExpressionStatement(V8Object expressionStatement) {
                    try {
                        Assertion assertion = new Assertion(filePath, expressionStatement, sourceFile);
                        assertions.add(assertion);
                    } catch (Exception e) {
                        // Do nothing
                    }
                    visitChildren(expressionStatement);
                }
            };
            testVisitor.visit(testNode);
            testVisitor.close();
            this.assertionBlocks = createAssertionBlocks(assertions);
        } else {
            throw new Exception("Was not a test");
        }
    }

    private boolean isTest(V8Object expressionStatement) {
        TypeScriptVisitor typeScriptVisitor = new TypeScriptVisitor();
        String expression = TypeScript.getInstance().getNodeText(expressionStatement);
        if (expression.startsWith("it")) {
            // _might_ be a test. check first that we have a call expression
            if (typeScriptVisitor.isKind(expressionStatement.getObject("expression"), "CallExpression")) {
                V8Object identifier = expressionStatement.getObject("expression").getObject("expression");
                return typeScriptVisitor.isKind(identifier, "Identifier") && identifier.executeJSFunction("getText").equals("it");
            } else {
                return false;
            }
        }
        return false;
    }

    private List<List<Assertion>> createAssertionBlocks(List<Assertion> assertions) {
        List<List<Assertion>> blocks = new ArrayList<>();
        int i = 0;
        while(i < assertions.size()) {
            Assertion currentAssertion = assertions.get(i);
            i = i + 1;
            if (currentAssertion.isExpectingValue()) {
                List<Assertion> block = new ArrayList<>();
                block.add(currentAssertion);
                Target expectingOn = currentAssertion.getLHS();
                while(i < assertions.size() &&
                        consecutiveLines(assertions.get(i - 1), assertions.get(i)) &&
                        assertions.get(i).isExpectingValue() &&
                        sameText(expectingOn, assertions.get(i).getLHS())) {
                    block.add(assertions.get(i));
                    i = i + 1;
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

    private boolean consecutiveLines(Assertion firstAssertion, Assertion secondAssertion) {
        return firstAssertion.getLine() + 1 == secondAssertion.getLine();
    }

    private boolean sameRootIdentifier(Target targetA, Target targetB) {
        return targetA.getRoot().equals(targetB.getRoot());
    }

    private boolean sameText(Target targetA, Target targetB) {
        return targetA.getText().equals(targetB.getText());
    }
}
