package com.lucasaz.intellij.AssertionGeneration.model.assertion;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.lucasaz.intellij.AssertionGeneration.services.TypeScript;
import com.lucasaz.intellij.AssertionGeneration.visitors.impl.TypeScriptVisitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class Assertion {
	List<PropertyAccess> propertyAccesses;
	String filePath;
	int line;

	// TODO all that equality stuff needs to go here

	public Assertion(String filePath, V8Object assertionNode, V8Object sourceFile) throws Exception {
		if (isAssertion(assertionNode)) {
			this.propertyAccesses = new ArrayList<>();
			this.filePath = filePath;
			this.line = TypeScript.getInstance().getLine(assertionNode, sourceFile);
			TypeScriptVisitor expressionVisitor = new TypeScriptVisitor() {
				@Override
				protected void visitCallExpression(V8Object callExpression) {
					try {
						List<Target> arguments = new ArrayList<>();

						V8Array v8ArgumentArray = callExpression.getArray("arguments");
						int numArguments = v8ArgumentArray.length();
						for (int i = 0; i < numArguments; i++) {
							V8Object v8Node = v8ArgumentArray.getObject(i);
							Target target = new Target(v8Node);
							// TODO this needs to get used
							// mapToV8Node.put(target, v8Node);
							arguments.add(target);
						}
						// if expression is an identifier, we must be done
						V8Object parentExpression = callExpression.getObject("expression");
						if (isKind(parentExpression, "Identifier")) {
							String identifier = TypeScript.getInstance().getNodeText(parentExpression);
							propertyAccesses.add(0, new Call(identifier, arguments));
						} else if (isKind(parentExpression, "PropertyAccessExpression")) {
							String propertyName = TypeScript.getInstance().getNodeText(parentExpression.getObject("name"));
							propertyAccesses.add(0, new Call(propertyName, arguments));
							visit(parentExpression.getObject("expression"));
						}
					} catch (IOException ioe) {
						System.out.println("Fatal error! Could not reinit typescript!");
					}
				}

				@Override
				protected void visitPropertyAccessExpression(V8Object propertyAccessExpression) {
					propertyAccesses.add(0, new PropertyAccess(TypeScript.getInstance().getNodeText(propertyAccessExpression.getObject("name"))));
					visit(propertyAccessExpression.getObject("expression"));
				}

				@Override
				protected void visitIdentifier(V8Object identifier) {
					propertyAccesses.add(0, new PropertyAccess(TypeScript.getInstance().getNodeText(identifier)));
				}
			};
			expressionVisitor.visit(assertionNode);

			// TODO this needs to get used
//			Assertion assertion = new Assertion(propertyAccesses, filePath, line);
//			if (EqualitySpecifier.isInEqualityCategory(assertion)) {
//				assertion = EqualitySpecifier.getEqualityDetails(assertion, mapToV8Node);
//			}

			// this line must be after the last use of the map
			// or else it will release all the objects inside
			expressionVisitor.close();
		} else {
			throw new Exception("Was not an assertion");
		}

	}

	private boolean isAssertion(V8Object expressionStatement) {
		String expression = TypeScript.getInstance().getNodeText(expressionStatement);
		if (expression.startsWith("expect") || expression.startsWith("should") || expression.startsWith("assert")) {
			// _might_ be an expect. check the first identifier to see if it's an expect
			V8Object currentObject = expressionStatement;
			while (currentObject.contains("expression")) {
				currentObject = currentObject.getObject("expression");
			}

			String identifier = TypeScript.getInstance().getNodeText(currentObject);
			return new TypeScriptVisitor().isKind(currentObject, "Identifier") &&
					(identifier.equals("expect") || identifier.equals("should") || identifier.equals("assert"));
		} else {
			return false;
		}
	}

	public Target getLHS() {
		PropertyAccess propertyAccess = this.getPropertyAccesses().get(0);
		if (propertyAccess.getName().equals("expect") && propertyAccess instanceof Call) {
			Call call = (Call) propertyAccess;
			if (call.getArguments().size() > 0) {
				return call.getArguments().get(0);
			}
		}
		if (propertyAccess.getName().equals("assert")) {
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
		if (propertyAccess.getName().equals("expect")) {
			for (int i = 1; i < getPropertyAccesses().size(); i = i + 1) {
				PropertyAccess rhsPropertyAccess = getPropertyAccesses().get(i);
				if (rhsPropertyAccess instanceof Call) {
					Call call = (Call) rhsPropertyAccess;
					targets.addAll(call.getArguments());
				}
			}
		} else if (propertyAccess.getName().equals("assert")) {
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
			return (call.getArguments().size() > 0 && call.getName().equals("expect"));
		} else if (getPropertyAccesses().get(0).getName().equals("assert")) {
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
			tokenArray.put(token.getName());
		}
		json.put("token", tokenArray);
		json.put("original", toString());
		return json;
	}

	public boolean lastPropertyIsCall() {
		PropertyAccess lastPropertyAccess = propertyAccesses.get(propertyAccesses.size() - 1);
		return lastPropertyAccess instanceof Call;
	}
}
