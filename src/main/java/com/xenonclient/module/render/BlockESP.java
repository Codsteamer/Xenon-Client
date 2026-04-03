package com.xenonclient.module.render;

import com.xenonclient.module.Module;
import com.xenonclient.module.setting.BooleanSetting;
import com.xenonclient.module.setting.ColorSetting;

/**
 * Block ESP module - highlights blocks with an outline/box.
 * Includes a tracer option (default off) that draws lines from the player to blocks.
 */
public class BlockESP extends Module {

    private final ColorSetting color;
    private final BooleanSetting tracer;

    public BlockESP() {
        super("Block ESP", "Highlights specific blocks through walls", "Render");
        this.color = new ColorSetting("Color", "ESP highlight color", 0xFFFF0000);
        this.tracer = new BooleanSetting("Tracer", "Draw lines from player to blocks", false);
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
