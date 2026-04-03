package com.xenonclient.module.render;

import com.xenonclient.module.Module;
import com.xenonclient.module.setting.BooleanSetting;
import com.xenonclient.module.setting.ColorSetting;

/**
 * Player ESP module - highlights other players through walls.
 * Includes a tracer option (default off) that draws lines from the player to other players.
 */
public class PlayerESP extends Module {

    private final ColorSetting color;
    private final BooleanSetting tracer;

    public PlayerESP() {
        super("Player ESP", "Highlights other players through walls", "Render");
        this.color = new ColorSetting("Color", "ESP highlight color", 0xFF00FF00);
        this.tracer = new BooleanSetting("Tracer", "Draw lines from player to players", false);
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
