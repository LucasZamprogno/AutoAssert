package com.lucasaz.intellij.AssertionGeneration.visitors;

public interface IVisitor<T> {
	void visit(T visitable);
	void close();
}
