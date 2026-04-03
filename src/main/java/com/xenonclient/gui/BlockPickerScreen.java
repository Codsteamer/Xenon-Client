package com.xenonclient.gui;

import com.xenonclient.module.render.BlockESPModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Block picker GUI for Block ESP module.
 * Shows a scrollable grid of all blocks that can be selected.
 */
public class BlockPickerScreen extends Screen {

    private final Screen parent;
    private final BlockESPModule module;
    private final List<Block> allBlocks = new ArrayList<>();
    private final List<Block> filteredBlocks = new ArrayList<>();
    private String searchText = "";
    private int scrollOffset = 0;
    private static final int COLS = 8;
    private static final int CELL_SIZE = 28;
    private static final int PADDING = 20;

    public BlockPickerScreen(Screen parent, BlockESPModule module) {
        super(Component.literal("Block Picker"));
        this.parent = parent;
        this.module = module;
    }

    @Override
    protected void init() {
        super.init();
        allBlocks.clear();
        for (Block block : BuiltInRegistries.BLOCK) {
            allBlocks.add(block);
        }
        updateFilter();
    }

    private void updateFilter() {
        filteredBlocks.clear();
        for (Block block : allBlocks) {
            String name = block.getName().getString().toLowerCase();
            if (searchText.isEmpty() || name.contains(searchText.toLowerCase())) {
                filteredBlocks.add(block);
            }
        }
        scrollOffset = 0;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0xCC000000);

        int panelW = COLS * CELL_SIZE + PADDING * 2 + 20;
        int panelH = 340;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // Panel background
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xE0101020);
        drawBorder(graphics, panelX, panelY, panelW, panelH, 0xFF333355);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 3, 0xFF2ECC71);

        // Title
        String title = "Pick a Block";
        int titleW = this.font.width(title);
        graphics.text(this.font, title, panelX + (panelW - titleW) / 2, panelY + 8, 0xFFFFFFFF, true);

        // Search box
        int searchX = panelX + PADDING;
        int searchY = panelY + 24;
        int searchW = panelW - PADDING * 2;
        graphics.fill(searchX, searchY, searchX + searchW, searchY + 14, 0xFF1A1A2E);
        drawBorder(graphics, searchX, searchY, searchW, 14, 0xFF555577);

        String searchDisplay = searchText.isEmpty() ? "Type to search..." : searchText + "_";
        int searchColor = searchText.isEmpty() ? 0xFF666666 : 0xFFCCCCCC;
        graphics.text(this.font, searchDisplay, searchX + 4, searchY + 3, searchColor, false);

        // Block grid
        int gridX = panelX + PADDING;
        int gridY = panelY + 46;
        int gridH = panelH - 70;
        int visibleRows = gridH / CELL_SIZE;

        int totalRows = (filteredBlocks.size() + COLS - 1) / COLS;
        int maxScroll = Math.max(0, totalRows - visibleRows);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        // Clip area
        for (int row = 0; row < visibleRows && (row + scrollOffset) < totalRows; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = (row + scrollOffset) * COLS + col;
                if (idx >= filteredBlocks.size()) break;

                Block block = filteredBlocks.get(idx);
                int cellX = gridX + col * CELL_SIZE;
                int cellY = gridY + row * CELL_SIZE;

                boolean isTracked = module.isTracked(block);
                boolean hovered = mouseX >= cellX && mouseX < cellX + CELL_SIZE - 2
                        && mouseY >= cellY && mouseY < cellY + CELL_SIZE - 2;

                int bgColor;
                if (isTracked) {
                    bgColor = 0xFF2E4C31;
                } else if (hovered) {
                    bgColor = 0xFF2A2A4A;
                } else {
                    bgColor = 0xFF1A1A2E;
                }
                graphics.fill(cellX, cellY, cellX + CELL_SIZE - 2, cellY + CELL_SIZE - 2, bgColor);

                if (hovered || isTracked) {
                    int borderCol = isTracked ? 0xFF2ECC71 : 0xFF6C3CE0;
                    drawBorder(graphics, cellX, cellY, CELL_SIZE - 2, CELL_SIZE - 2, borderCol);
                }

                // Render block icon
                ItemStack stack = new ItemStack(block.asItem());
                if (!stack.isEmpty()) {
                    graphics.item(stack, cellX + (CELL_SIZE - 2 - 16) / 2,
                            cellY + (CELL_SIZE - 2 - 16) / 2);
                } else {
                    // Fallback to abbreviated name for blocks without items
                    String name = block.getName().getString();
                    if (name.length() > 4) name = name.substring(0, 3) + ".";
                    int nameW = this.font.width(name);
                    graphics.text(this.font, name, cellX + (CELL_SIZE - 2 - nameW) / 2,
                            cellY + (CELL_SIZE - 2 - 8) / 2, 0xFFCCCCCC, false);
                }
            }
        }

        // Tooltip for hovered block
        for (int row = 0; row < visibleRows && (row + scrollOffset) < totalRows; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = (row + scrollOffset) * COLS + col;
                if (idx >= filteredBlocks.size()) break;

                int cellX = gridX + col * CELL_SIZE;
                int cellY = gridY + row * CELL_SIZE;

                if (mouseX >= cellX && mouseX < cellX + CELL_SIZE - 2
                        && mouseY >= cellY && mouseY < cellY + CELL_SIZE - 2) {
                    Block block = filteredBlocks.get(idx);
                    String fullName = block.getName().getString();
                    int tipW = this.font.width(fullName) + 8;
                    int tipX = mouseX + 8;
                    int tipY = mouseY - 14;
                    if (tipX + tipW > this.width) tipX = mouseX - tipW - 4;
                    graphics.fill(tipX - 2, tipY - 2, tipX + tipW, tipY + 12, 0xF0101020);
                    drawBorder(graphics, tipX - 2, tipY - 2, tipW + 2, 14, 0xFF555577);
                    graphics.text(this.font, fullName, tipX + 2, tipY, 0xFFFFFFFF, false);
                }
            }
        }

        // Scrollbar
        if (totalRows > visibleRows) {
            int sbX = panelX + panelW - PADDING + 4;
            int sbH = gridH;
            int thumbH = Math.max(10, sbH * visibleRows / totalRows);
            int thumbY = gridY + (int) ((float) scrollOffset / maxScroll * (sbH - thumbH));
            graphics.fill(sbX, gridY, sbX + 4, gridY + sbH, 0xFF1A1A2E);
            graphics.fill(sbX, thumbY, sbX + 4, thumbY + thumbH, 0xFF6C3CE0);
        }

        // Close hint
        String hint = "ESC to go back";
        int hintW = this.font.width(hint);
        graphics.text(this.font, hint, panelX + (panelW - hintW) / 2, panelY + panelH - 16, 0x80FFFFFF, false);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromSelf) {
        if (event.button() != 0) return super.mouseClicked(event, fromSelf);

        double mouseX = event.x();
        double mouseY = event.y();

        int panelW = COLS * CELL_SIZE + PADDING * 2 + 20;
        int panelH = 340;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        int gridX = panelX + PADDING;
        int gridY = panelY + 46;
        int gridH = panelH - 70;
        int visibleRows = gridH / CELL_SIZE;
        int totalRows = (filteredBlocks.size() + COLS - 1) / COLS;

        for (int row = 0; row < visibleRows && (row + scrollOffset) < totalRows; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = (row + scrollOffset) * COLS + col;
                if (idx >= filteredBlocks.size()) break;

                int cellX = gridX + col * CELL_SIZE;
                int cellY = gridY + row * CELL_SIZE;

                if (mouseX >= cellX && mouseX < cellX + CELL_SIZE - 2
                        && mouseY >= cellY && mouseY < cellY + CELL_SIZE - 2) {
                    Block block = filteredBlocks.get(idx);
                    if (module.isTracked(block)) {
                        module.removeBlock(block);
                    } else {
                        module.addBlock(block);
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(event, fromSelf);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int totalRows = (filteredBlocks.size() + COLS - 1) / COLS;
        int panelH = 340;
        int gridH = panelH - 70;
        int visibleRows = gridH / CELL_SIZE;
        int maxScroll = Math.max(0, totalRows - visibleRows);

        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY));
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        String s = event.codepointAsString();
        if (s.length() == 1) {
            char chr = s.charAt(0);
            if (Character.isLetterOrDigit(chr) || chr == '_' || chr == ' ') {
                searchText += chr;
                updateFilter();
                return true;
            }
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 259 && !searchText.isEmpty()) { // Backspace
            searchText = searchText.substring(0, searchText.length() - 1);
            updateFilter();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawBorder(GuiGraphicsExtractor graphics, int bx, int by, int bw, int bh, int color) {
        graphics.fill(bx, by, bx + bw, by + 1, color);
        graphics.fill(bx, by + bh - 1, bx + bw, by + bh, color);
        graphics.fill(bx, by, bx + 1, by + bh, color);
        graphics.fill(bx + bw - 1, by, bx + bw, by + bh, color);
    }
}
