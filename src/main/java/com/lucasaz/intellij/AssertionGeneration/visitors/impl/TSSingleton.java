package com.lucasaz.intellij.AssertionGeneration.visitors.impl;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TSSingleton {
	private static final String TYPESCRIPT_PATH = "node_modules/typescript/lib/typescript.js";
	private static TSSingleton instance;
	private final NodeJS nodeJS;
	private static V8Object ts;

	public static TSSingleton getInstance() throws IOException {
		if (instance == null) {
			instance = new TSSingleton();
		}
		return instance;
	}

	public TSSingleton() throws IOException {
		// TODO there must be a better way to do this
		InputStream inputStream = TypeScriptVisitor.class.getClassLoader().getResourceAsStream(TYPESCRIPT_PATH);
		File file = File.createTempFile("typescript", null);
		file.deleteOnExit();
		FileUtils.copyInputStreamToFile(inputStream, file);
		nodeJS = NodeJS.createNodeJS();
		ts = nodeJS.require(file);
	}

	public V8Object getTS() {
		return ts;
	}

	public V8 getRuntime() {
		return nodeJS.getRuntime();
	}
}
