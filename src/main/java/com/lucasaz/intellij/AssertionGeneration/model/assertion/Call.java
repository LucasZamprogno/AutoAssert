package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import lombok.Getter;

import java.util.List;

@Getter
public class Call extends PropertyAccess {
	List<Target> arguments;

	public Call(String text, List<Target> arguments) {
		super(text);
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(super.toString());
		stringBuilder.append("(");
		for (int i = 0; i < arguments.size(); i = i + 1) {
			stringBuilder.append(arguments.get(i));
			if (i + 1 < arguments.size()) {
				stringBuilder.append(", ");
			}
		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}
}
