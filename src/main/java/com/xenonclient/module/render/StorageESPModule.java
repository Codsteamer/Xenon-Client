package com.xenonclient.module.render;

import com.xenonclient.module.Module;

/**
 * Storage ESP module - highlights storage containers through walls.
 * Configurable: toggle which container types to show.
 */
public class StorageESPModule extends Module {

    private boolean showChests = true;
    private boolean showEnderChests = true;
    private boolean showShulkerBoxes = true;
    private boolean showBarrels = true;
    private boolean showHoppers = true;
    private boolean showDispensers = true;
    private boolean showDroppers = true;
    private boolean showFurnaces = true;

    private int chestColor = 0xFFFFAA00;
    private int enderChestColor = 0xFF8B00FF;
    private int shulkerColor = 0xFFFF69B4;
    private int barrelColor = 0xFF8B4513;
    private int hopperColor = 0xFF808080;
    private int dispenserColor = 0xFF444444;
    private int furnaceColor = 0xFFCC4400;

    private int range = 64;

    public StorageESPModule() {
        super("Storage ESP", "Highlights storage containers through walls", "Render");
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    public boolean isShowChests() { return showChests; }
    public void setShowChests(boolean v) { this.showChests = v; }

    public boolean isShowEnderChests() { return showEnderChests; }
    public void setShowEnderChests(boolean v) { this.showEnderChests = v; }

    public boolean isShowShulkerBoxes() { return showShulkerBoxes; }
    public void setShowShulkerBoxes(boolean v) { this.showShulkerBoxes = v; }

    public boolean isShowBarrels() { return showBarrels; }
    public void setShowBarrels(boolean v) { this.showBarrels = v; }

    public boolean isShowHoppers() { return showHoppers; }
    public void setShowHoppers(boolean v) { this.showHoppers = v; }

    public boolean isShowDispensers() { return showDispensers; }
    public void setShowDispensers(boolean v) { this.showDispensers = v; }

    public boolean isShowDroppers() { return showDroppers; }
    public void setShowDroppers(boolean v) { this.showDroppers = v; }

    public boolean isShowFurnaces() { return showFurnaces; }
    public void setShowFurnaces(boolean v) { this.showFurnaces = v; }

    public int getChestColor() { return chestColor; }
    public int getEnderChestColor() { return enderChestColor; }
    public int getShulkerColor() { return shulkerColor; }
    public int getBarrelColor() { return barrelColor; }
    public int getHopperColor() { return hopperColor; }
    public int getDispenserColor() { return dispenserColor; }
    public int getFurnaceColor() { return furnaceColor; }

    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }
}
