package com.xenonclient.module.render;

import com.xenonclient.module.Module;

/**
 * Player ESP module - highlights other players through walls.
 * Configurable colors for different player states.
 */
public class PlayerESPModule extends Module {

    private int defaultColor = 0xFFFF0000;
    private int friendColor = 0xFF00FF00;
    private int sneakingColor = 0xFFFFFF00;
    private boolean showHealth = true;
    private boolean showName = true;
    private boolean showDistance = true;
    private int range = 256;
    private boolean tracerEnabled = false;

    public PlayerESPModule() {
        super("Player ESP", "Highlights players through walls", "Render");
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    public int getDefaultColor() { return defaultColor; }
    public void setDefaultColor(int color) { this.defaultColor = color; }

    public int getFriendColor() { return friendColor; }
    public void setFriendColor(int color) { this.friendColor = color; }

    public int getSneakingColor() { return sneakingColor; }
    public void setSneakingColor(int color) { this.sneakingColor = color; }

    public boolean isShowHealth() { return showHealth; }
    public void setShowHealth(boolean v) { this.showHealth = v; }

    public boolean isShowName() { return showName; }
    public void setShowName(boolean v) { this.showName = v; }

    public boolean isShowDistance() { return showDistance; }
    public void setShowDistance(boolean v) { this.showDistance = v; }

    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }

    public boolean isTracerEnabled() { return tracerEnabled; }
    public void setTracerEnabled(boolean enabled) { this.tracerEnabled = enabled; }
}
