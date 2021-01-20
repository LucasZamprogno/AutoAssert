package com.lucasaz.intellij.AssertionGeneration.visitors.impl;

import com.lucasaz.intellij.AssertionGeneration.visitors.IVisitor;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@AllArgsConstructor
public class ProjectVisitor implements IVisitor<Path> {
	private final IVisitor<String> sourceVisitor;

	protected void visitDirectory(Path directory) {}

	protected boolean shouldVisitDirectory(Path directoryPath) {
		return directoryPath != null;
	}

	protected void visitFile(Path filePath) {
		try {
			String source = new String(Files.readAllBytes(filePath));
			sourceVisitor.visit(source);
		} catch (IOException ioException) {
			// TODO is there any sort of logging system?
			System.out.println("Error visiting file's source.");
			ioException.printStackTrace();
		}
	}

	protected boolean shouldVisitFile(Path filePath) {
		return filePath != null;
	}

	public void visit(Path root) {
		try {
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
					if (shouldVisitFile(filePath)) {
						ProjectVisitor.this.visitFile(filePath);
						return FileVisitResult.CONTINUE;
					} else {
						return FileVisitResult.SKIP_SUBTREE;
					}
				}

				@Override
				public FileVisitResult preVisitDirectory(Path directoryPath, BasicFileAttributes attrs) {
					if (shouldVisitDirectory(directoryPath)) {
						ProjectVisitor.this.visitDirectory(directoryPath);
						return FileVisitResult.CONTINUE;
					} else {
						return FileVisitResult.SKIP_SUBTREE;
					}
				}
			});
		} catch (IOException ioException) {
			// TODO is there any sort of logging system?
			System.out.println("Error visiting file's project.");
			ioException.printStackTrace();
		}
	}

	public void close() {
		if (sourceVisitor != null) {
			sourceVisitor.close();
		}
	}
}
