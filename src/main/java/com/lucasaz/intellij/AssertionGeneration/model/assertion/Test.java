package com.lucasaz.intellij.AssertionGeneration.model.assertion;

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
        this.assertionBlocks = createAssertionBlocks(assertions);
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
