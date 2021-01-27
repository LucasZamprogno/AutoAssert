package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import com.eclipsesource.v8.V8Object;
import com.lucasaz.intellij.AssertionGeneration.services.TypeScript;
import com.lucasaz.intellij.AssertionGeneration.visitors.impl.TypeScriptVisitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.IOException;

@Getter
@AllArgsConstructor
public class Target {
	String text;

	boolean includesPropertyAccess;
	boolean includesCallExpression;
	boolean includesIdentifier;
	boolean isExpression;
	boolean isIdentifier;
	boolean isLiteral;
	boolean isCall;
	int depth;
	String root;

	Target(V8Object targetNode) throws IOException {
		String text = TypeScript.getInstance().getNodeText(targetNode);
		final boolean[] includesPropertyAccess = {false};
		final boolean[] includesCallExpression = {false};
		final boolean[] includesIdentifier = {false};
		final String[] root = {null};
		final int[] depth = {0};
		TypeScriptVisitor targetVisitor = new TypeScriptVisitor() {
			@Override
			protected void visitPropertyAccessExpression(V8Object propertyAccessExpression) {
				includesPropertyAccess[0] = true;
				depth[0]++;
				visit(propertyAccessExpression.getObject("expression"));
			}

			@Override
			protected void visitElementAccessExpression(V8Object elementAccessExpression) {
				includesPropertyAccess[0] = true;
				depth[0]++;
				visit(elementAccessExpression.getObject("expression"));
			}

			@Override
			protected void visitCallExpression(V8Object callExpression) {
				includesCallExpression[0] = true;
				depth[0]++;
				visit(callExpression.getObject("expression"));
			}

			@Override
			protected void visitIdentifier(V8Object identifier) {
				includesIdentifier[0] = true;
				root[0] = TypeScript.getInstance().getNodeText(identifier);
			}
		};
		boolean isIdentifier = targetVisitor.isKind(targetNode, "Identifier") || targetVisitor.isKind(targetNode, "ThisKeyword"); // this ????
		boolean isExpression = isExpression(targetVisitor, targetNode);
		boolean isLiteral = isLiteral(targetVisitor, targetNode);
		boolean isCall = isCall(targetVisitor, targetNode);
		targetVisitor.visit(targetNode);
		targetVisitor.close();
		if (isExpression || isLiteral || includesCallExpression[0]) {
			depth[0] = -1;
		}

		this.text = text;
		this.includesPropertyAccess = includesPropertyAccess[0];
		this.includesCallExpression = includesCallExpression[0];
		this.includesIdentifier = includesIdentifier[0];
		this.isExpression = isExpression;
		this.isIdentifier = isIdentifier;
		this.isLiteral = isLiteral;
		this.isCall = isCall;
		this.depth = depth[0];
		this.root = root[0];
	}

	private boolean isLiteral(TypeScriptVisitor targetVisitor, V8Object literal) {
		return targetVisitor.isKind(literal, "NullKeyword") ||
				targetVisitor.isKind(literal, "UndefinedKeyword") ||
				targetVisitor.isKind(literal, "TrueKeyword") ||
				targetVisitor.isKind(literal, "FalseKeyword") ||
				targetVisitor.isKind(literal, "NumericLiteral") ||
				targetVisitor.isKind(literal, "BigIntLiteral") ||
				targetVisitor.isKind(literal, "StringLiteral") ||
				targetVisitor.isKind(literal, "JsxText") ||
				targetVisitor.isKind(literal, "JsxTextAllWhiteSpaces") ||
				targetVisitor.isKind(literal, "RegularExpressionLiteral") ||
				targetVisitor.isKind(literal, "NoSubstitutionTemplateLiteral") ||
				targetVisitor.isKind(literal, "TypeLiteral") || // Should this be here?
				targetVisitor.isKind(literal, "ArrayLiteralExpression") ||
				targetVisitor.isKind(literal, "ObjectLiteralExpression");
	}

	private boolean isExpression(TypeScriptVisitor targetVisitor, V8Object expression) {
		return // targetVisitor.isKind(expression, "ArrayLiteralExpression") || // ??
				// targetVisitor.isKind(expression, "ObjectLiteralExpression") || // ??
				// targetVisitor.isKind(expression, "PropertyAccessExpression") || // ??
				// targetVisitor.isKind(expression, "ElementAccessExpression") || // ??
				// targetVisitor.isKind(expression, "CallExpression") || // ??
				targetVisitor.isKind(expression, "NewExpression") ||
						targetVisitor.isKind(expression, "TaggedTemplateExpression") ||
						targetVisitor.isKind(expression, "TypeAssertionExpression") ||
						targetVisitor.isKind(expression, "ParenthesizedExpression") ||
						targetVisitor.isKind(expression, "FunctionExpression") ||
						targetVisitor.isKind(expression, "ArrowFunction") ||
						targetVisitor.isKind(expression, "DeleteExpression") ||
						targetVisitor.isKind(expression, "TypeOfExpression") ||
						targetVisitor.isKind(expression, "VoidExpression") ||
						targetVisitor.isKind(expression, "AwaitExpression") ||
						targetVisitor.isKind(expression, "PrefixUnaryExpression") ||
						targetVisitor.isKind(expression, "PostfixUnaryExpression") ||
						targetVisitor.isKind(expression, "BinaryExpression") ||
						targetVisitor.isKind(expression, "ConditionalExpression") ||
						targetVisitor.isKind(expression, "TemplateExpression") ||
						targetVisitor.isKind(expression, "YieldExpression") ||
						targetVisitor.isKind(expression, "SpreadElement") ||
						targetVisitor.isKind(expression, "ClassExpression") ||
						targetVisitor.isKind(expression, "OmittedExpression") ||
						targetVisitor.isKind(expression, "ExpressionWithTypeArguments") || // ??
						targetVisitor.isKind(expression, "AsExpression") || // ??
						targetVisitor.isKind(expression, "NonNullExpression") ||
						targetVisitor.isKind(expression, "SyntheticExpression") ||
						targetVisitor.isKind(expression, "ExpressionStatement");
	}

	private boolean isCall(TypeScriptVisitor targetVisitor, V8Object call) {
		return targetVisitor.isKind(call, "CallExpression");
	}

	@Override
	public String toString() {
		return text;
	}
}
