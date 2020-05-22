package com.lucasaz.intellij.AssertionGeneration;

import com.intellij.execution.*;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.MapDataContext;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;


public class AssertionGeneration extends AnAction
{
    private Project project;
    private AnActionEvent e;
    private Selected selected;
    private FileWatcher fw;
    private PropertiesComponent settings;

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        this.e = e;
        this.project = e.getData(LangDataKeys.PROJECT);
        this.settings = PropertiesComponent.getInstance(project);

        try {
            this.selected = this.makeSelectedInformation();
        } catch (NullPointerException | IOException err) {
            System.err.println("Failed to get selected text from editor");
            System.err.println(err.getMessage());
            return;
        }

        // OUTFILE should be gone but attempt to delete just in case, don't want FileWatcher to miss it
        String[] toEnsureDeleted = {Util.OUTFILE};
        Util.cleanup(toEnsureDeleted);

        try {
            String newPath = Util.makeBackgroundFilename(this.selected.tsFilePath);
            FileWriter fw = new FileWriter(newPath);
            fw.write(this.selected.originalFile);
            fw.close();
        } catch (IOException err) {
            System.err.println("Error making background copy of test file");
            System.err.println(err.getMessage());
            return;
        }

        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Paths.get(this.selected.tsFilePath).getParent().register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            this.fw = FileWatcher.getInstance(this.selected, watcher);
        } catch (IOException err) {
            System.err.println("Failed to setup file watcher");
            System.err.println(err.getMessage());
            return;
        }

        try {
            RunnerAndConfigurationSettings racs = this.createRunConfig();
            Executor executor = DefaultRunExecutor.getRunExecutorInstance();
            ExecutionUtil.runConfiguration(racs, executor);
        } catch (PluginException err) {
            System.err.println(err.getMessage());
        }
    }

    private RunnerAndConfigurationSettings createRunConfig() throws PluginException {
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
        RunnerAndConfigurationSettings racs = null;
        try {
            racs = configurationFromContext.getConfigurationSettings();
            RunConfiguration rc = racs.getConfiguration();
            Method getRunSettings = rc.getClass().getDeclaredMethod("getRunSettings");
            Object rs = getRunSettings.invoke(rc);
            Field filePathField = rs.getClass().getDeclaredField("myTestFilePath");
            filePathField.setAccessible(true);
            String backgroundFilePath = Util.makeBackgroundFilename(this.selected.tsFilePath);
            filePathField.set(rs, backgroundFilePath);
        } catch (Throwable aaa) {
            System.err.println(aaa.getMessage());
        }

        List<BeforeRunTask<?>> tasks = new ArrayList<>();
        tasks.add(this.createInjectionBeforeRun(racs));
        if (this.settings.getBoolean(AssertionGenerationSettingsConfigurable.BUILD_ALL_KEY)) {
            tasks.add(this.createCompileBefore(racs));
        }
        racs.getConfiguration().setBeforeRunTasks(tasks);

        return racs;
    }

    private BeforeRunTask<?> createCompileBefore(RunnerAndConfigurationSettings racs) throws PluginException {
        BeforeRunTaskProvider<?> mbrtp = BeforeRunTaskProvider.EXTENSION_POINT_NAME.getExtensionList(this.project).get(10);
        BeforeRunTask<?> mbrt = mbrtp.createTask(racs.getConfiguration());
        String tsconfigPath;
        if (this.settings.getBoolean(AssertionGenerationSettingsConfigurable.AUTO_CONFIG_KEY)) {
            tsconfigPath = Util.findNearestTsconfig(this.selected.tsFilePath, this.project);
        } else {
            tsconfigPath = this.settings.getValue(AssertionGenerationSettingsConfigurable.PATH_KEY);
        }
        if (tsconfigPath == null) {
            throw new PluginException("Failed to find tsconfig file");
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
        AssertionGenerationBeforeRunTaskProvider cbrtp = new AssertionGenerationBeforeRunTaskProvider();
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