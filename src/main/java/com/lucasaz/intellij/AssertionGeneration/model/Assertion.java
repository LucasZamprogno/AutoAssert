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
	String filePath;
	int line;

	public Target getExpectingOn() {
		if (this.getPropertyAccesses().get(0) instanceof Call) {
			Call call = (Call) this.getPropertyAccesses().get(0);
			if (call.getArguments().size() > 0) {
				return call.getArguments().get(0);
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		for (PropertyAccess propertyAccess : propertyAccesses) {
			stringBuilder.append(propertyAccess.toString());
		}
		stringBuilder.deleteCharAt(0);
		stringBuilder.append(";");
		return stringBuilder.toString();
	}
}
