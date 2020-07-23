package com.lucasaz.intellij.AssertionGeneration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class Assertion {
	List<PropertyAccess> propertyAccesses;
	Target target;

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("expect");

		if (target != null) {
			stringBuilder.append("(");
			stringBuilder.append(target.toString());
			stringBuilder.append(")");
		}

		for (PropertyAccess propertyAccess : propertyAccesses) {
			stringBuilder.append(propertyAccess.toString());
		}
		stringBuilder.append(";");
		return stringBuilder.toString();
	}
}
