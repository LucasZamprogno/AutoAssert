package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import com.lucasaz.intellij.AssertionGeneration.visitors.impl.ProjectVisitor;
import lombok.Getter;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Getter
public class Repo {
    String name;
    Set<TestFile> files;

    public Repo(String name, Path repoPath) {
        this.name = name;
        this.files = new HashSet<>();

        ProjectVisitor projectVisitor = new ProjectVisitor(null) {
            @Override
            protected boolean shouldVisitFile(Path filePath) {
                String filePathString = filePath.toString();
                boolean shouldVisit = super.shouldVisitFile(filePath) &&
                        (filePathString.endsWith(".ts") || filePathString.endsWith(".js"))  &&
                        (filePathString.contains("test") || filePathString.contains("spec"));
                if (shouldVisit) {
                    System.out.println(filePathString);
                }
                return shouldVisit;
            }

            @Override
            protected boolean shouldVisitDirectory(Path dirPath) {
                return super.shouldVisitDirectory(dirPath) && !dirPath.toString().contains("node_modules") && !dirPath.toString().contains(".git");
            }

            @Override
            protected void visitFile(Path filePath) {
                TestFile file = new TestFile(filePath);
                if (file.isTesting()) {
                    files.add(file);
                }
            }
        };
        projectVisitor.visit(repoPath);
        projectVisitor.close();
    }

    public Set<Test> getTests() {
        Set<Test> tests = new HashSet<>();
        for (TestFile file : files) {
            tests.addAll(file.getTests());
        }
        return tests;
    }

    public Set<Assertion> getOrphanAssertions() {
        Set<Assertion> assertions = new HashSet<>();
        for (TestFile file : files) {
            assertions.addAll(file.getOrphanAssertions());
        }
        return assertions;
    }

    public Set<Assertion> getAllAssertions() {
        Set<Assertion> assertions = new HashSet<>();
        for (TestFile file : files) {
            for (Test test : file.getTests()) {
                assertions.addAll(test.getAssertions());
            }
        }
        assertions.addAll(getOrphanAssertions());
        return assertions;
    }
}
