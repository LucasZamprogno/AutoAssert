package com.lucasaz.intellij.TestPlugin;

import com.intellij.execution.BeforeRunTask;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

public class CustomBeforeRunTask extends BeforeRunTask<CustomBeforeRunTask> {
    private Selected dt;

    protected CustomBeforeRunTask(@NotNull Key providerId) {
        super(providerId);
        setEnabled(true);
        this.dt = null;
    }

    protected CustomBeforeRunTask(@NotNull Key providerId, @NotNull Selected dt) {
        super(providerId);
        setEnabled(true);
        this.dt = dt;
    }

    public Selected getData() {
        return this.dt;
    }
}
