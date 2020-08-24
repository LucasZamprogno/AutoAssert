package com.lucasaz.intellij.AssertionGeneration.model;

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
	int depth;

	@Override
	public String toString() {
		return text;
	}
}
