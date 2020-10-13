package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class Assertion {
	List<PropertyAccess> propertyAccesses;
	String filePath;
	int line;

	public Target getLHS() {
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

	public List<Target> getRHS() {
		PropertyAccess propertyAccess = this.getPropertyAccesses().get(0);
		List<Target> targets = new ArrayList<>();
		if (propertyAccess.getText().equals("expect")) {
			for (int i = 1; i < getPropertyAccesses().size(); i = i + 1) {
				PropertyAccess rhsPropertyAccess = getPropertyAccesses().get(i);
				if (rhsPropertyAccess instanceof Call) {
					Call call = (Call) propertyAccess;
					targets.addAll(call.getArguments());
				}
			}
		} else if (propertyAccess.getText().equals("assert")) {
			if (getPropertyAccesses().size() > 0 && getPropertyAccesses().get(1) instanceof Call) {
				Call call = (Call) getPropertyAccesses().get(1);
				for (int i = 1; i < call.getArguments().size(); i = i + 1) {
					targets.add(call.getArguments().get(i));
				}
			}
		}
		return targets;
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

	public String toJSON() {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("{");

		stringBuilder.append("\"LHS\": \"");
		stringBuilder.append(getLHS());
		stringBuilder.append("\",");

		List<Target> rhs = getRHS();
		stringBuilder.append("\"RHS\": [");
		for (int i = 0; i < rhs.size(); i = i + 1) {
			stringBuilder.append("\"");
			stringBuilder.append(rhs.get(i).text);
			stringBuilder.append("\"");

			if (i < rhs.size() - 1) {
				stringBuilder.append(",");
			}
		}
		stringBuilder.append("],");

		List<PropertyAccess> tokens = getPropertyAccesses();
		stringBuilder.append("\"tokens\": [");
		for (int i = 0; i < tokens.size(); i = i + 1) {
			stringBuilder.append("\"");
			stringBuilder.append(tokens.get(i).getText());
			stringBuilder.append("\"");

			if (i < tokens.size() - 1) {
				stringBuilder.append(",");
			}
		}
		stringBuilder.append("]}");
		return stringBuilder.toString();
	}
}
