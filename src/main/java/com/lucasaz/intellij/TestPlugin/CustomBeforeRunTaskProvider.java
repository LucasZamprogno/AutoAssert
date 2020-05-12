package com.lucasaz.intellij.TestPlugin;

import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.util.Key;
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
    public CustomBeforeRunTask createTask(@NotNull RunConfiguration runConfiguration, Selected dt) {
        return new CustomBeforeRunTask(this.getId(), dt);
    }

    @Override
    public boolean executeTask(@NotNull DataContext dataContext, @NotNull RunConfiguration runConfiguration, @NotNull ExecutionEnvironment executionEnvironment, @NotNull CustomBeforeRunTask beforeRunTask) {
        Selected dt = beforeRunTask.getData();
        if (dt != null) {
            this.doInjection(dt);
        }
        return true;
    }

    public void doInjection(Selected dt) {
        int line = dt.getLine();
        String selected = dt.getSelected();
        String filePathTs = dt.getTsFilePath();
        String fileStart = Util.pathToFileContent(Paths.get(filePathTs));
        String toInject = Util.createString(this.createInjectionStringList(selected));
        String newFile = Util.spliceInto(fileStart, toInject, line);

        try {
            FileWriter fw = new FileWriter(filePathTs);
            fw.write(newFile);
            fw.close();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }

    private List<String> createInjectionStringList(String varName)
    {
        try {
            InputStream is = TestAction.class.getClassLoader().getResourceAsStream("ts-injection.ts");
            String recordingFunction = IOUtils.toString(is, Charset.defaultCharset());
            String save = "const longVarNameToNotClash: any = elemUnderTestGenerator(" + varName + ");";
            String log = "require(\"fs\").writeFileSync(__dirname + \"/\" + \"" + CustomBeforeRunTaskProvider.logFile + "\", JSON.stringify(longVarNameToNotClash));";
            List<String> res = new ArrayList<>();
            res.add(recordingFunction);
            res.add(save);
            res.add(log);
            return res;
        } catch (Exception err) {
            return new ArrayList<>();
        }
    }
}
