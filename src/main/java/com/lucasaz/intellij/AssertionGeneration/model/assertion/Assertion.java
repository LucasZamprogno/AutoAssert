package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

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

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("lhs", getLHS());

		List<Target> rhs = getRHS();
		JSONArray rhsArray = new JSONArray();
		for (Target target : rhs) {
			rhsArray.put(target.text);
		}
		json.put("rhs", rhsArray);

		List<PropertyAccess> tokens = getPropertyAccesses();
		JSONArray tokenArray = new JSONArray();
		for (PropertyAccess token : tokens) {
			tokenArray.put(token.getText());
		}
		json.put("token", tokenArray);
		return json;
	}
}
