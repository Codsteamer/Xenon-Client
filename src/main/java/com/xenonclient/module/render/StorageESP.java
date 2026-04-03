package com.xenonclient.module.render;

import com.xenonclient.module.Module;
import com.xenonclient.module.setting.BooleanSetting;
import com.xenonclient.module.setting.ColorSetting;

/**
 * Storage ESP module - highlights storage containers (chests, barrels, etc.) through walls.
 * Includes a tracer option (default off) that draws lines from the player to storage blocks.
 */
public class StorageESP extends Module {

    private final ColorSetting color;
    private final BooleanSetting tracer;

    public StorageESP() {
        super("Storage ESP", "Highlights storage containers through walls", "Render");
        this.color = new ColorSetting("Color", "ESP highlight color", 0xFFFFAA00);
        this.tracer = new BooleanSetting("Tracer", "Draw lines from player to storage", false);
        addSetting(color);
        addSetting(tracer);
    }

    public ColorSetting getColor() {
        return color;
    }

    public BooleanSetting getTracer() {
        return tracer;
    }

    public boolean isTracerEnabled() {
        return tracer.isEnabled();
    }
}
