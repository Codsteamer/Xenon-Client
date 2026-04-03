package com.xenonclient.module;

import com.xenonclient.module.setting.BooleanSetting;
import com.xenonclient.module.setting.ColorSetting;
import com.xenonclient.module.setting.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all Xenon Client modules.
 * Modules belong to a section (category) and can have settings including color.
 */
public abstract class Module {

    private final String name;
    private final String description;
    private final String section;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, String section) {
        this.name = name;
        this.description = description;
        this.section = section;
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSection() {
        return section;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    protected void addSetting(Setting<?> setting) {
        settings.add(setting);
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    /**
     * Returns the ColorSetting for this module if it has one, otherwise null.
     */
    public ColorSetting getColorSetting() {
        for (Setting<?> setting : settings) {
            if (setting instanceof ColorSetting colorSetting) {
                return colorSetting;
            }
        }
        return null;
    }

    /**
     * Returns true if this module has a color configuration.
     */
    public boolean hasColorConfig() {
        return getColorSetting() != null;
    }

    protected void onEnable() {}

    protected void onDisable() {}
}
