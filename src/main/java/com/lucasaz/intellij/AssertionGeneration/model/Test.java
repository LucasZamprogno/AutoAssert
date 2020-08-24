package com.lucasaz.intellij.AssertionGeneration.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Test {
    List<Assertion> assertions;
    String filePath;
    int line;
    List<List<Assertion>> assertionBlocks;

    public Test(List<Assertion> assertions, String filePath, int line) {
        this.assertions = assertions;
        this.filePath = filePath;
        this.line = line;
        assertionBlocks = createAssertionBlocks(assertions);
    }

    private List<List<Assertion>> createAssertionBlocks(List<Assertion> assertions) {
        List<List<Assertion>> blocks = new ArrayList<>();
        int i = 0;
        while(i < assertions.size()) {
            Assertion currentAssertion = assertions.get(i);
            i = i + 1;
            if (expectingValue(currentAssertion)) {
                List<Assertion> block = new ArrayList<>();
                block.add(currentAssertion);
                Target expectingOn = getExpectingOn(currentAssertion);
                if (!expectingOn.isExpression() && !expectingOn.isLiteral() && !expectingOn.isCall()) {
                    while(i < assertions.size() &&
                            consecutiveLines(assertions.get(i - 1), assertions.get(i)) &&
                            expectingValue(assertions.get(i)) &&
                            sameRootIdentifier(expectingOn, getExpectingOn(assertions.get(i)))) {
                        block.add(assertions.get(i));
                        i = i + 1;
                    }
                } else {
                    while(i < assertions.size() &&
                            consecutiveLines(assertions.get(i - 1), assertions.get(i)) &&
                            expectingValue(assertions.get(i)) &&
                            sameText(expectingOn, getExpectingOn(assertions.get(i)))) {
                        block.add(assertions.get(i));
                        i = i + 1;
                    }
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

    private boolean expectingValue(Assertion assertion) {
        if (assertion.getPropertyAccesses().get(0) instanceof Call) {
            Call call = (Call) assertion.getPropertyAccesses().get(0);
            return (call.getArguments().size() > 0 && call.getText().equals("expect"));
        } else {
            return false;
        }
    }

    private Target getExpectingOn(Assertion assertion) {
        Call call = (Call) assertion.getPropertyAccesses().get(0);
        return call.getArguments().get(0);
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
