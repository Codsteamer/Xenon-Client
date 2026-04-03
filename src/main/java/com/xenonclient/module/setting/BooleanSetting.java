package com.xenonclient.module.setting;

/**
 * A boolean toggle setting for modules.
 */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public void toggle() {
        this.value = !this.value;
    }

    public boolean isEnabled() {
        return this.value;
    }
}
