package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import com.eclipsesource.v8.V8Object;
import com.lucasaz.intellij.AssertionGeneration.visitors.impl.TypeScriptVisitor;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Getter
public class TestFile {
    Set<Assertion> orphanAssertions;
    Set<Test> tests;
    String source;
    Path filePath;

    TestFile(Path filePath) {
        this.filePath = filePath;
        this.orphanAssertions = new HashSet<>();
        this.tests = new HashSet<>();
        try {
            this.source = new String(Files.readAllBytes(filePath));
            this.source = this.source.replaceAll("\n[\n\t\r ]*\n", "\n");
            TypeScriptVisitor sourceVisitor = new TypeScriptVisitor() {
                private V8Object sourceFile;

                @Override
                public void visit(String source) {
                    sourceFile = getSource(source);
                    visit(sourceFile);
                }

                @Override
                public void close() {
                    sourceFile.release();
                    super.close();
                }

                @Override
                protected void visitExpressionStatement(V8Object expressionStatement) {
                    try {
                        Test test = new Test(filePath.toString(), expressionStatement, sourceFile);
                        tests.add(test);
                        return; // Don't continue with children
                    } catch (Exception e) {
                        // Do nothing - it wasn't a test
                    }
                    try {
                        Assertion assertion = new Assertion(filePath.toString(), expressionStatement, sourceFile);
                        orphanAssertions.add(assertion);
                        return; // Don't continue with children
                    } catch (Exception e) {
                        // Do nothing - it wasn't an assertion
                    }
                    visitChildren(expressionStatement);
                }

            };
            sourceVisitor.visit(this.source);
            sourceVisitor.close();

        } catch (IOException ioException) {
            System.out.println("Error visiting file's source.");
            ioException.printStackTrace();
        }
    }

    boolean isTesting() {
        return tests.size() > 0 || orphanAssertions.size() > 0;
    }
}
