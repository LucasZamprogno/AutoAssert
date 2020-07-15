package com.lucasaz.intellij.AssertionGeneration.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class Call extends PropertyAccess {
	List<Target> arguments;

	public Call(String text, List<Target> arguments) {
		super(text);
		this.arguments = arguments;
	}
}
