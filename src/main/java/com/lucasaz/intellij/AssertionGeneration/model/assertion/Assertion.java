package com.lucasaz.intellij.AssertionGeneration.model.assertion;

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
		PropertyAccess propertyAccess = this.getPropertyAccesses().get(0);
		if (propertyAccess.getText().equals("expect") && propertyAccess instanceof Call) {
			Call call = (Call) propertyAccess;
			if (call.getArguments().size() > 0) {
				return call.getArguments().get(0);
			}
		}
		if (propertyAccess.getText().equals("assert")) {
			if (getPropertyAccesses().size() > 0 && getPropertyAccesses().get(1) instanceof Call) {
				Call call = (Call) getPropertyAccesses().get(1);
				return call.getArguments().get(0);
			}
		}
		return null;
	}

	public boolean isExpectingValue() {
		if (getPropertyAccesses().get(0) instanceof Call) {
			Call call = (Call) getPropertyAccesses().get(0);
			return (call.getArguments().size() > 0 && call.getText().equals("expect"));
		} else if (getPropertyAccesses().get(0).getText().equals("assert")) {
			if (getPropertyAccesses().size() > 0 && getPropertyAccesses().get(1) instanceof Call) {
				Call call = (Call) getPropertyAccesses().get(1);
				return call.getArguments().size() > 0;
			} else {
				return false;
			}
		} else {
			return false;
		}
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
