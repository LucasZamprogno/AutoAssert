package com.lucasaz.intellij.AssertionGeneration.execution;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.Assertion;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class VisitProject extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		try {
			String basePathString = anActionEvent.getData(LangDataKeys.PROJECT).getBasePath();
			Path basePath = Paths.get(basePathString);
			List<Assertion> testList = new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(@NotNull AnActionEvent anActionEvent){}
}
