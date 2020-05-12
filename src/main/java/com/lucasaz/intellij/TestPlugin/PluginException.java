package com.lucasaz.intellij.TestPlugin;

public class PluginException extends Exception {
    public PluginException(String str) {
        super(str);
    }

    public PluginException(String str, Throwable t) {
        super(str, t);
    }
}
