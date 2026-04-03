package com.xenonclient.module.render;

import com.xenonclient.module.Module;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Block ESP module - highlights specific blocks in the world.
 * Has a block picker GUI accessible via right-click config.
 */
public class BlockESPModule extends Module {

    private final List<Block> trackedBlocks = new ArrayList<>();
    private int espColor = 0xFFFF0000;
    private int range = 32;

    public BlockESPModule() {
        super("Block ESP", "Highlights specific blocks through walls", "Render");
        trackedBlocks.add(Blocks.DIAMOND_ORE);
        trackedBlocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    public List<Block> getTrackedBlocks() {
        return Collections.unmodifiableList(trackedBlocks);
    }

    public void addBlock(Block block) {
        if (!trackedBlocks.contains(block)) {
            trackedBlocks.add(block);
        }
    }

    public void removeBlock(Block block) {
        trackedBlocks.remove(block);
    }

    public boolean isTracked(Block block) {
        return trackedBlocks.contains(block);
    }

    public int getEspColor() {
        return espColor;
    }

    public void setEspColor(int color) {
        this.espColor = color;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }
}
