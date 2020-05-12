package com.lucasaz.intellij.TestPlugin;

import com.intellij.execution.*;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.MapDataContext;

import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;


public class TestAction extends AnAction
{
    private Project project;
    private AnActionEvent e;
    private Selected selected;
    private FileWatcher fw;
    /**
     * Convert selected text to a URL friendly string.
     * @param e
     */
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        this.e = e;
        this.project = e.getData(LangDataKeys.PROJECT);

        try {
            this.selected = this.makeSelectedInformation();
        } catch (NullPointerException | IOException err) {
            System.err.println("Failed to get selected text from editor");
            System.err.println(err.getMessage());
            return;
        }

        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Paths.get(this.selected.tsFilePath).getParent().register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            this.fw = FileWatcher.getInstance(this.selected, ".testOutput", watcher);
        } catch (IOException err) {
            System.err.println("Failed to setup file watcher");
            System.err.println(err.getMessage());
            return;
        }

        try {
            RunnerAndConfigurationSettings racs = this.createRunConfig(e);
            Executor executor = DefaultRunExecutor.getRunExecutorInstance();
            ExecutionUtil.runConfiguration(racs, executor);
        } catch (PluginException err) {
            System.err.println(err.getMessage());
        }
    }

    private RunnerAndConfigurationSettings createRunConfig(AnActionEvent e) throws PluginException {
        MapDataContext dataContext = new MapDataContext();
        dataContext.put(CommonDataKeys.PROJECT, this.project);
        dataContext.put(Location.DATA_KEY, PsiLocation.fromPsiElement(e.getData(LangDataKeys.PSI_ELEMENT)));

        List<RunConfigurationProducer<?>> tmp = RunConfigurationProducer.getProducers(this.project);
        RunConfigurationProducer<?> runConfProd = null;
        for (RunConfigurationProducer<?> rcp : tmp) {
            String packageName = rcp.toString().split("@")[0];
            if (packageName.equals("com.jetbrains.nodejs.mocha.execution.MochaRunConfigurationProducer")) {
                runConfProd = rcp;
                break;
            }
        }

        if (runConfProd == null) {
            throw new PluginException("Couldn't find MochaRunConfigurationProducer");
        }

        ConfigurationContext configurationContext = ConfigurationContext.getFromContext(dataContext);
        ConfigurationFromContext configurationFromContext = runConfProd.createConfigurationFromContext(configurationContext);
        if (configurationFromContext == null) {
            throw new PluginException("Failed to create Mocha Run Configuration");
        }
        RunnerAndConfigurationSettings racs = configurationFromContext.getConfigurationSettings();

        List<BeforeRunTask<?>> tasks = new ArrayList<>();
        tasks.add(this.createInjectionBeforeRun(racs));
        tasks.add(this.createCompileBefore(racs));
        racs.getConfiguration().setBeforeRunTasks(tasks);

        return racs;
    }

    private BeforeRunTask<?> createCompileBefore(RunnerAndConfigurationSettings racs) throws PluginException {
        BeforeRunTaskProvider<?> mbrtp = BeforeRunTaskProvider.EXTENSION_POINT_NAME.getExtensionList(this.project).get(10);
        BeforeRunTask<?> mbrt = mbrtp.createTask(racs.getConfiguration());
        String tsconfigPath = Util.findNearestTsconfig(this.selected.tsFilePath, this.project);
        if (tsconfigPath == null) {
            throw new PluginException("Failed to find tsconfig file between test file and project root");
        }
        try {
            Method setConfigPath = mbrt.getClass().getDeclaredMethod("setConfigPath", String.class);
            setConfigPath.invoke(mbrt, tsconfigPath);
        } catch (Exception err) {
            throw new PluginException("Failed to set tsconfig path for build", err);
        }
        return mbrt;
    }

    private BeforeRunTask<?> createInjectionBeforeRun(RunnerAndConfigurationSettings racs) {
        CustomBeforeRunTaskProvider cbrtp = new CustomBeforeRunTaskProvider();
        return cbrtp.createTask(racs.getConfiguration(), this.selected, this.fw);
    }

    private Selected makeSelectedInformation() throws NullPointerException, IOException {
        Editor editor = this.e.getRequiredData(CommonDataKeys.EDITOR);
        CaretModel caretModel = editor.getCaretModel();
        Caret caret = caretModel.getPrimaryCaret();
        LogicalPosition pos = caret.getLogicalPosition();

        int line = pos.line;
        int col = pos.column;
        int colZeroInd = col - 1;
        String selected = caretModel.getPrimaryCaret().getSelectedText();

        String tsFilePath = this.getFilePathString();
        String content = Util.pathToFileContent(Paths.get(tsFilePath));
        String whitespace = Util.getWhitespace(Util.toLines(content).get(line));

        return new Selected(line, colZeroInd, selected, tsFilePath, content, whitespace);
    }

    private String getFilePathString() throws NullPointerException {
        Document currentDoc = FileEditorManager.getInstance(this.project).getSelectedTextEditor().getDocument();
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
        return currentFile.getPath();
    }

    /**
     * Only make this action visible when text is selected.
     * @param e
     */
    @Override
    public void update(AnActionEvent e)
    {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        CaretModel caretModel = editor.getCaretModel();
        e.getPresentation().setEnabledAndVisible(caretModel.getCurrentCaret().hasSelection());
    }
}