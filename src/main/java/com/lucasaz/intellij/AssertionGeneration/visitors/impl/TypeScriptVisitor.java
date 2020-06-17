package com.lucasaz.intellij.AssertionGeneration.visitors.impl;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.utils.MemoryManager;
import com.lucasaz.intellij.AssertionGeneration.visitors.IVisitor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TypeScriptVisitor implements IVisitor<String> {
	private static final String TYPESCRIPT_PATH = "TODO";

	private final V8Object ts;
	private final V8Object syntaxKind;
	private final MemoryManager scope;
	private final NodeJS nodeJS;

	private final Map<String, Integer> syntaxKindCache = new HashMap<String, Integer>();

	public TypeScriptVisitor() throws IOException {
		// TODO there must be a better way to do this
		InputStream inputStream = TypeScriptVisitor.class.getClassLoader().getResourceAsStream(TYPESCRIPT_PATH);
		File file = File.createTempFile("typescript", null);
		file.deleteOnExit();
		FileUtils.copyInputStreamToFile(inputStream, file);

		nodeJS = NodeJS.createNodeJS();
		scope = new MemoryManager(nodeJS.getRuntime());
		ts = nodeJS.require(file);
		syntaxKind = ts.getObject("SyntaxKind");
	}

	public void visit(String source) {
		V8Object scriptTargetEnum = ts.getObject("ScriptTarget");
		V8Object scriptKindEnum = ts.getObject("ScriptKind");

		String name = "temp.ts";
		int scriptTarget = scriptTargetEnum.getInteger("ES2015");
		int scriptKind = scriptKindEnum.getInteger("TS");

		// const sourceFile = createSourceFile(name, source, scriptTarget, true, scriptKind);
		V8Array parameters = new V8Array(ts.getRuntime())
				.push(name)
				.push(source)
				.push(scriptTarget)
				.push(true) // TODO what is this parameter? I want a name
				.push(scriptKind);
		V8Object sourceFile = ts.executeObjectFunction("createSourceFile", parameters);

		// release unneeded objects
		parameters.release();
		scriptKindEnum.release();
		scriptTargetEnum.release();

		visit(sourceFile);

		// release final unneeded object
		sourceFile.release();
	}

	public void close() {
		scope.release();
		nodeJS.release();
	}

	private void visit(V8Object node) {
		if (isKind(node, "SourceFile")) {
			visitSourceFile(node);
		} else if (isKind(node, "FunctionDeclaration")) {
			visitFunctionDeclaration(node);
		} else if (isKind(node, "Constructor")) {
			visitConstructor(node);
		} else if (isKind(node, "MethodDeclaration")) {
			visitMethodDeclaration(node);
		} else if (isKind(node, "ArrowFunction")) {
			visitArrowFunction(node);
		} else if (isKind(node, "FunctionExpression")) {
			visitFunctionExpression(node);
		} else {
			visitChildren(node);
		}
	}

	protected boolean isKind(V8Object node, String kind) {
		if (!syntaxKindCache.containsKey(kind)) {
			syntaxKindCache.put(kind, syntaxKind.getInteger(kind));
		}
		return node.contains("kind") && node.getInteger("kind") == syntaxKindCache.get(kind);
	}

	protected void visitChildren(V8Object node) {
		V8Function callback = new V8Function(ts.getRuntime(), (receiver, parameters) -> {
			final V8Object child = parameters.getObject(0);
			visit(child);
			child.release();
			return null;
		});
		ts.executeJSFunction("forEachChild", node, callback);
		callback.release();
	}

	protected void visitArrowFunction(V8Object arrowFunction) {
		visitChildren(arrowFunction);
	}

	protected void visitConstructor(V8Object constructor) {
		visitChildren(constructor);
	}

	protected void visitFunctionDeclaration(V8Object functionDeclaration) {
		visitChildren(functionDeclaration);
	}

	protected void visitFunctionExpression(V8Object functionExpression) {
		visitChildren(functionExpression);
	}

	protected void visitMethodDeclaration(V8Object methodDeclaration) {
		visitChildren(methodDeclaration);
	}

	protected void visitSourceFile(V8Object sourceFile) {
		visitChildren(sourceFile);
	}
}
