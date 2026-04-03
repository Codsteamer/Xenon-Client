package com.xenonclient.module.setting;

/**
 * Base class for module settings.
 * Each setting has a name and a typed value.
 */
public abstract class Setting<T> {

    private final String name;
    private final String description;
    protected T value;
    protected final T defaultValue;

    public Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void reset() {
        this.value = defaultValue;
    }
}
