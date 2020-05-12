package com.lucasaz.intellij.TestPlugin;

import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.NonBlockingReadAction;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.Key;
import com.intellij.util.concurrency.NonUrgentExecutor;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CustomBeforeRunTaskProvider extends BeforeRunTaskProvider<CustomBeforeRunTask> {
    private static String logFile = ".testOutput";
    public static final Key<CustomBeforeRunTask> ID = Key.create("Custom.BeforeRunTask");

    @Override
    public Key<CustomBeforeRunTask> getId() {
        return CustomBeforeRunTaskProvider.ID;
    }

    @Override
    public String getName() {
        return "CustomBeforeRunTaskProvider";
    }

    @Override
    public String getDescription(CustomBeforeRunTask task) {
        return "Description";
    }

    @Nullable
    @Override
    public CustomBeforeRunTask createTask(@NotNull RunConfiguration runConfiguration) {
        return new CustomBeforeRunTask(this.getId());
    }

    @Nullable
    public CustomBeforeRunTask createTask(@NotNull RunConfiguration runConfiguration, Selected dt, FileWatcher fw) {
        return new CustomBeforeRunTask(this.getId(), dt, fw);
    }

    @Override
    public boolean executeTask(@NotNull DataContext dataContext, @NotNull RunConfiguration runConfiguration, @NotNull ExecutionEnvironment executionEnvironment, @NotNull CustomBeforeRunTask beforeRunTask) {
        Selected dt = beforeRunTask.getData();
        FileWatcher fw = beforeRunTask.getWatcher();
        if (dt != null && fw != null) {
            boolean injectSucceeded = this.doInjection(dt);
            if (injectSucceeded) { // Only start the listener if we're going to proceed
                NonBlockingReadAction<Void> res = ReadAction.nonBlocking(fw);
                res.submit(NonUrgentExecutor.getInstance());
                return true;
            }
        }
        return false;
    }

    public boolean doInjection(Selected dt) {
        int line = dt.getLine();
        String selected = dt.getSelected();
        String filePathTs = dt.getTsFilePath();

        try {
            String fileStart = Util.pathToFileContent(Paths.get(filePathTs));
            String toInject = Util.createString(this.createInjectionStringList(selected));
            String newFile = Util.spliceInto(fileStart, toInject, line);
            FileWriter fw = new FileWriter(filePathTs);
            fw.write(newFile);
            fw.close();
            return true;
        } catch (IOException | PluginException err) {
            System.out.println(err.getMessage());
            return false;
        }
    }

    private List<String> createInjectionStringList(String varName) throws IOException
    {
        InputStream is = TestAction.class.getClassLoader().getResourceAsStream("ts-injection.ts");
        String recordingFunction = IOUtils.toString(is, Charset.defaultCharset());
        String save = "const longVarNameToNotClash: any = elemUnderTestGenerator(" + varName + ");";
        String log = "require(\"fs\").writeFileSync(__dirname + \"/\" + \"" + CustomBeforeRunTaskProvider.logFile + "\", JSON.stringify(longVarNameToNotClash));";
        List<String> res = new ArrayList<>();
        res.add(recordingFunction);
        res.add(save);
        res.add(log);
        return res;
    }
}
