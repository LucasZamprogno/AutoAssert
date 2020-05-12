package com.lucasaz.intellij.TestPlugin;

import com.intellij.openapi.components.*;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "TestProjectSettings",
        storages = {
                @Storage("/Test.xml")
        }
)
public class TestProjectSettings implements PersistentStateComponent<Element> {
    public static final String TEST_SETTINGS = "TestPluginSettings";
    public static final String FIELD_NAME = "tsconfig path";
    private String path = "";
    @Nullable
    @Override
    public Element getState() {
        final Element element = new Element(TEST_SETTINGS);
        element.setAttribute(FIELD_NAME, path);
        return element;
    }

    @Override
    public void loadState(@NotNull Element state) {
        path = state.getAttributeValue(FIELD_NAME, "");
    }
}