package com.lucasaz.intellij.AssertionGeneration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class Assertion {
	List<PropertyAccess> propertyAccesses;
	Target target;
}
