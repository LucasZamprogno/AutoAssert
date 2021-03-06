package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
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

	@Override
	public String toString() {
		return text;
	}
}
