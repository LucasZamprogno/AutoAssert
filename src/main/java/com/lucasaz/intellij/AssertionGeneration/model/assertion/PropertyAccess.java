package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PropertyAccess {
	String text;

	@Override
	public String toString() {
		return "." + text;
	}
}
