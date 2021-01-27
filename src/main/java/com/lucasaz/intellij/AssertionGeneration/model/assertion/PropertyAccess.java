package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PropertyAccess {
	String name;

	@Override
	public String toString() {
		return "." + name;
	}
}
