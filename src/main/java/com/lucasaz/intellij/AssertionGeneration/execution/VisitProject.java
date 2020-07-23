package com.lucasaz.intellij.AssertionGeneration.execution;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.lucasaz.intellij.AssertionGeneration.model.*;
import com.lucasaz.intellij.AssertionGeneration.visitors.ProjectVisitor;
import com.lucasaz.intellij.AssertionGeneration.visitors.impl.TypeScriptVisitor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
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

			List<Assertion> testList = new ArrayList<>();

			TypeScriptVisitor tsVisitor = new TypeScriptVisitor() {
				@Override
				protected void visitExpressionStatement(V8Object expressionStatement) {
					try {
						if (isAssertion(expressionStatement)) {
							testList.add(getAssertion(expressionStatement));
						}
					} catch (Exception e) {
						System.out.println(e.toString());
						// Do nothing
					}
					visitChildren(expressionStatement);
				}

				private String getText(V8Object node) {
					V8Array arguments = new V8Array(ts.getRuntime());
					String text = node.executeStringFunction("getText", arguments);
					arguments.release();
					return text;
				}

				private boolean isAssertion(V8Object expressionStatement) {
					String expression = getText(expressionStatement);
					if (expression.startsWith("expect")) {
						// _might_ be an expect. check the first identifier to see if it's an expect
						V8Object currentObject = expressionStatement;
						while (currentObject.contains("expression")) {
							currentObject = currentObject.getObject("expression");
						}

						String identifier = currentObject
								.executeStringFunction("getText", new V8Array(ts.getRuntime()));
						return isKind(currentObject, "Identifier") && identifier.equals("expect");
					} else {
						return false;
					}
				}

				private Assertion getAssertion(V8Object expressionStatement) throws IOException {

					List<PropertyAccess> propertyAccesses = new ArrayList<>();
					final Target[] target = new Target[1];

					TypeScriptVisitor expressionVisitor = new TypeScriptVisitor() {
						@Override
						protected void visitCallExpression(V8Object callExpression) {
							try {
								// if expression is an identifier, we must be done
								V8Object parentExpression = callExpression.getObject("expression");
								if (isKind(parentExpression, "Identifier")) {
									target[0] = getTarget(callExpression.getObject("arguments").getObject("0"));
								} else {
									String propertyName = getText(parentExpression.getObject("name"));
									List<Target> arguments = new ArrayList<>();

									V8Array v8ArgumentArray = callExpression.getArray("arguments");
									int numArguments = v8ArgumentArray.length();
									for (int i = 0; i < numArguments; i++) {
										arguments.add(getTarget(v8ArgumentArray.getObject(i)));
									}

									propertyAccesses.add(0, new Call(propertyName, arguments));
									visit(parentExpression.getObject("expression"));
								}
							} catch (IOException ioe) {
								System.out.println("Fatal error! Could not reinit typescript!");
							}
						}

						@Override
						protected void visitPropertyAccessExpression(V8Object propertyAccessExpression) {
							propertyAccesses.add(0, new PropertyAccess(getText(propertyAccessExpression.getObject("name"))));
							visit(propertyAccessExpression.getObject("expression"));
						}
					};
					expressionVisitor.visit(expressionStatement);
					expressionVisitor.close();

					return new Assertion(propertyAccesses, target[0]);
				}

				private Target getTarget(V8Object object) throws IOException {
					String text = getText(object);
					final boolean[] includesPropertyAccess = {false};
					final boolean[] includesCallExpression = {false};
					final boolean[] includesIdentifier = {false};
					TypeScriptVisitor targetVisitor = new TypeScriptVisitor() {
						@Override
						protected void visitPropertyAccessExpression(V8Object propertyAccessExpression) {
							includesPropertyAccess[0] = true;
							visit(propertyAccessExpression.getObject("expression"));
						}
						@Override
						protected void visitCallExpression(V8Object callExpression) {
							includesCallExpression[0] = true;
							visit(callExpression.getObject("expression"));
						}
						@Override
						protected void visitIdentifier(V8Object identifier) {
							includesIdentifier[0] = true;
						}
					};
					targetVisitor.visit(object);
					targetVisitor.close();
					return new Target(text, includesPropertyAccess[0], includesCallExpression[0], includesIdentifier[0]);
				}
			};

			ProjectVisitor projectVisitor = new ProjectVisitor(tsVisitor) {
				@Override
				protected boolean shouldVisitFile(Path filePath) {
					String filePathString = filePath.toString();
					return super.shouldVisitFile(filePath) && filePathString.endsWith(".ts")  && (filePathString.contains("test") || filePathString.contains(".spec"));
				}

				@Override
				protected boolean shouldVisitDirectory(Path dirPath) {
					return super.shouldVisitDirectory(dirPath) && !dirPath.toString().contains("node_modules");
				}
			};
			projectVisitor.visit(basePath);
			projectVisitor.close();

			PrintWriter printWriter = new PrintWriter("./test.txt");
			String output = testList.toString();
			printWriter.print(output);
			printWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(@NotNull AnActionEvent anActionEvent){}
}
