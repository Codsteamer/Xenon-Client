package com.xenonclient.module.render;

import com.xenonclient.module.Module;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Block ESP module - highlights specific blocks in the world.
 * Has a block picker GUI accessible via right-click config.
 * Uses a HashSet for O(1) block lookups during scanning.
 */
public class BlockESPModule extends Module {

    private final List<Block> trackedBlocks = new ArrayList<>();
    private final Set<Block> trackedBlockSet = new HashSet<>();
    private int espColor = 0xFFFF0000;
    private int range = 32;

    public BlockESPModule() {
        super("Block ESP", "Highlights specific blocks through walls", "Render");
        addBlock(Blocks.DIAMOND_ORE);
        addBlock(Blocks.DEEPSLATE_DIAMOND_ORE);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    public List<Block> getTrackedBlocks() {
        return Collections.unmodifiableList(trackedBlocks);
    }

    public void addBlock(Block block) {
        if (trackedBlockSet.add(block)) {
            trackedBlocks.add(block);
        }
    }

    public void removeBlock(Block block) {
        if (trackedBlockSet.remove(block)) {
            trackedBlocks.remove(block);
        }
    }

    public boolean isTracked(Block block) {
        return trackedBlockSet.contains(block);
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
