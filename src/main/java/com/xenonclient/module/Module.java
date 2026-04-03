package com.xenonclient.module;

/**
 * Base class for all Xenon Client modules.
 */
public abstract class Module {

    private final String name;
    private final String description;
    private final String sectionName;
    private boolean enabled;

    protected Module(String name, String description, String sectionName) {
        this.name = name;
        this.description = description;
        this.sectionName = sectionName;
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSectionName() {
        return sectionName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }

    public void enable() {
        this.enabled = true;
        onEnable();
    }

    public void disable() {
        this.enabled = false;
        onDisable();
    }

    protected void onEnable() {}

    protected void onDisable() {}

    /**
     * Whether this module has a config screen accessible via right-click.
     */
    public boolean hasConfig() {
        return false;
    }
}
