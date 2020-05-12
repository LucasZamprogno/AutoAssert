package com.lucasaz.intellij.TestPlugin;

import com.intellij.execution.BeforeRunTask;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

public class CustomBeforeRunTask extends BeforeRunTask<CustomBeforeRunTask> {
    private Selected dt;
    private FileWatcher fw;

    protected CustomBeforeRunTask(@NotNull Key providerId) {
        super(providerId);
        setEnabled(true);
        this.dt = null;
        this.fw = null;
    }

    protected CustomBeforeRunTask(@NotNull Key providerId, @NotNull Selected dt, @NotNull FileWatcher fw) {
        super(providerId);
        setEnabled(true);
        this.dt = dt;
        this.fw = fw;
    }

    public Selected getData() {
        return this.dt;
    }
    public FileWatcher getWatcher() {
        return this.fw;
    }
}
