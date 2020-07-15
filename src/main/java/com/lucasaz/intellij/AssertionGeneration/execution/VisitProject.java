package com.lucasaz.intellij.AssertionGeneration.execution;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.lucasaz.intellij.AssertionGeneration.visitors.ProjectVisitor;
import com.lucasaz.intellij.AssertionGeneration.visitors.impl.TypeScriptVisitor;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class VisitProject extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		try {
			String basePathString = anActionEvent.getData(LangDataKeys.PROJECT).getBasePath();
			Path basePath = Paths.get(basePathString);

			List<String> testList = new ArrayList<String>();

			TypeScriptVisitor tsVisitor = new TypeScriptVisitor() {
				@Override
				protected void visitExpressionStatement(V8Object expressionStatement) {
					try {
						String expression = expressionStatement
								.executeStringFunction("getText", new V8Array(ts.getRuntime()));
						if (expression.startsWith("expect")) {
							// _might_ be an expect. check the first identifier to see if it's an expect
							V8Object currentObject = expressionStatement;
							while (currentObject.contains("expression")) {
								currentObject = currentObject.getObject("expression");
							}
							// TODO assert that this is a SyntaxKind.Identifier
							String identifier = currentObject
									.executeStringFunction("getText", new V8Array(ts.getRuntime()));
							if (identifier.equals("expect")) {
								testList.add(expression);
							}
						}
					} catch (Exception e) {
						// Do nothing
					}
					visitChildren(expressionStatement);
				}
			};

			ProjectVisitor projectVisitor = new ProjectVisitor(tsVisitor) {
				@Override
				protected boolean shouldVisitFile(Path filePath) {
					return super.shouldVisitFile(filePath) && filePath.toString().endsWith(".ts");
				}

				@Override
				protected boolean shouldVisitDirectory(Path dirPath) {
					return super.shouldVisitDirectory(dirPath) && !dirPath.toString().contains("node_modules");
				}
			};
			projectVisitor.visit(basePath);
			System.out.println("List length: " + testList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(@NotNull AnActionEvent anActionEvent){}
}
