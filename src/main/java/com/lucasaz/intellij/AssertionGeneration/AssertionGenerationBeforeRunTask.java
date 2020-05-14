package com.lucasaz.intellij.AssertionGeneration;

import com.intellij.execution.BeforeRunTask;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

public class AssertionGenerationBeforeRunTask extends BeforeRunTask<AssertionGenerationBeforeRunTask> {
    private Selected dt;
    private FileWatcher fw;

    protected AssertionGenerationBeforeRunTask(@NotNull Key providerId) {
        super(providerId);
        setEnabled(true);
        this.dt = null;
        this.fw = null;
    }

    protected AssertionGenerationBeforeRunTask(@NotNull Key providerId, @NotNull Selected dt, @NotNull FileWatcher fw) {
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
