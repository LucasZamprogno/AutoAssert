package com.lucasaz.intellij.AssertionGeneration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class Target {
	String text;
	boolean includesPropertyAccess;
	boolean includesCallExpression;
	boolean includesIdentifier;
}
