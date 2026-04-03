package com.xenonclient.section;

/**
 * Represents a section in the Xenon Client wheel GUI.
 * Each section occupies a slice of the radial wheel and can be selected
 * to open a detailed panel.
 */
public class Section {

    private final String name;
    private final String description;
    private int color;
    private final String icon;
    private boolean enabled;

    public Section(String name, String description, int color, String icon) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.icon = icon;
        this.enabled = true;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
