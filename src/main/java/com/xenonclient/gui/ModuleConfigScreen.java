package com.xenonclient.gui;

import com.xenonclient.module.Module;
import com.xenonclient.module.render.BlockESPModule;
import com.xenonclient.module.render.PlayerESPModule;
import com.xenonclient.module.render.StorageESPModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Config screen for modules with settings.
 * Dispatches to the appropriate config rendering based on module type.
 */
public class ModuleConfigScreen extends Screen {

    private final Screen parent;
    private final Module module;
    private int scrollOffset = 0;
    private ColorWheelWidget colorWheel = null;
    private String activeColorField = null;

    public ModuleConfigScreen(Screen parent, Module module) {
        super(Component.literal(module.getName() + " Config"));
        this.parent = parent;
        this.module = module;
    }

    @Override
    protected void init() {
        super.init();
        scrollOffset = 0;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0xCC000000);

        int panelW = 300;
        int panelH = 400;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // Panel background
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xE0101020);
        // Border
        drawBorder(graphics, panelX, panelY, panelW, panelH, 0xFF333355);
        // Accent bar
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 3, 0xFF6C3CE0);

        // Title
        String title = module.getName() + " Settings";
        int titleW = this.font.width(title);
        graphics.text(this.font, title, panelX + (panelW - titleW) / 2, panelY + 10, 0xFFFFFFFF, true);

        // Separator
        graphics.fill(panelX + 10, panelY + 26, panelX + panelW - 10, panelY + 27, 0x40FFFFFF);

        int contentY = panelY + 34 - scrollOffset;

        if (module instanceof BlockESPModule blockESP) {
            renderBlockESPConfig(graphics, panelX, contentY, panelW, mouseX, mouseY, blockESP);
        } else if (module instanceof StorageESPModule storageESP) {
            renderStorageESPConfig(graphics, panelX, contentY, panelW, mouseX, mouseY, storageESP);
        } else if (module instanceof PlayerESPModule playerESP) {
            renderPlayerESPConfig(graphics, panelX, contentY, panelW, mouseX, mouseY, playerESP);
        }

        // Render color wheel if active
        if (colorWheel != null) {
            int wheelX = panelX + panelW + 10;
            int wheelY = panelY + 34;
            colorWheel.render(graphics, this.font);
        }

        // Close hint
        String hint = "Press ESC to close";
        int hintW = this.font.width(hint);
        graphics.text(this.font, hint, panelX + (panelW - hintW) / 2, panelY + panelH - 16, 0x80FFFFFF, false);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void renderBlockESPConfig(GuiGraphicsExtractor graphics, int px, int cy, int pw,
                                       int mouseX, int mouseY, BlockESPModule mod) {
        int pad = 14;

        // Tracer toggle
        renderToggleRow(graphics, px + pad, cy, pw - pad * 2, "Tracer", mod.isTracerEnabled(), mouseX, mouseY);
        cy += 18;

        // Color with swatch
        graphics.text(this.font, "ESP Color", px + pad, cy, 0xFFCCCCCC, false);
        drawColorEntry(graphics, px + pad, cy, pw - pad * 2, "ESP Color", mod.getEspColor(), mouseX, mouseY);
        cy += 18;

        graphics.text(this.font, "Range: " + mod.getRange() + " blocks", px + pad, cy, 0xFFCCCCCC, false);
        cy += 14;

        graphics.text(this.font, "Tracked Blocks:", px + pad, cy, 0xFFAAAAFF, true);
        cy += 14;

        var blocks = mod.getTrackedBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            String name = blocks.get(i).getName().getString();
            boolean hovered = mouseX >= px + pad && mouseX <= px + pw - pad
                    && mouseY >= cy && mouseY <= cy + 12;
            int color = hovered ? 0xFFFF6666 : 0xFFCCCCCC;
            graphics.text(this.font, "  " + name + (hovered ? " [X]" : ""), px + pad, cy, color, false);
            cy += 14;
        }

        cy += 6;
        boolean addHovered = mouseX >= px + pad && mouseX <= px + pad + this.font.width("[+ Add Block]")
                && mouseY >= cy && mouseY <= cy + 12;
        graphics.text(this.font, "[+ Add Block]", px + pad, cy, addHovered ? 0xFF66FF66 : 0xFF44AA44, false);
        cy += 18;

        // Range controls
        boolean decHovered = mouseX >= px + pad && mouseX <= px + pad + 20
                && mouseY >= cy && mouseY <= cy + 14;
        boolean incHovered = mouseX >= px + pad + 100 && mouseX <= px + pad + 120
                && mouseY >= cy && mouseY <= cy + 14;
        graphics.text(this.font, "[-]", px + pad, cy, decHovered ? 0xFFFFFF66 : 0xFFCCCCCC, false);
        graphics.text(this.font, "Range: " + mod.getRange(), px + pad + 24, cy, 0xFFCCCCCC, false);
        graphics.text(this.font, "[+]", px + pad + 100, cy, incHovered ? 0xFFFFFF66 : 0xFFCCCCCC, false);
    }

    private void renderStorageESPConfig(GuiGraphicsExtractor graphics, int px, int cy, int pw,
                                         int mouseX, int mouseY, StorageESPModule mod) {
        int pad = 14;

        // Tracer toggle
        renderToggleRow(graphics, px + pad, cy, pw - pad * 2, "Tracer", mod.isTracerEnabled(), mouseX, mouseY);
        cy += 18;

        String[] labels = {"Chests", "Ender Chests", "Shulker Boxes", "Barrels", "Hoppers", "Dispensers", "Droppers", "Furnaces"};
        boolean[] values = {
                mod.isShowChests(), mod.isShowEnderChests(), mod.isShowShulkerBoxes(),
                mod.isShowBarrels(), mod.isShowHoppers(), mod.isShowDispensers(),
                mod.isShowDroppers(), mod.isShowFurnaces()
        };

        graphics.text(this.font, "Toggle container types:", px + pad, cy, 0xFFAAAAFF, true);
        cy += 16;

        for (int i = 0; i < labels.length; i++) {
            boolean hovered = mouseX >= px + pad && mouseX <= px + pw - pad
                    && mouseY >= cy && mouseY <= cy + 12;
            String status = values[i] ? "\u25CF ON" : "\u25CB OFF";
            int statusColor = values[i] ? 0xFF2ECC71 : 0xFFE74C3C;
            int labelColor = hovered ? 0xFFFFFFFF : 0xFFCCCCCC;

            graphics.text(this.font, labels[i], px + pad, cy, labelColor, false);
            graphics.text(this.font, status, px + pw - pad - this.font.width(status), cy, statusColor, false);
            cy += 16;
        }

        cy += 8;
        graphics.text(this.font, "Range: " + mod.getRange() + " blocks", px + pad, cy, 0xFFCCCCCC, false);
    }

    private void renderPlayerESPConfig(GuiGraphicsExtractor graphics, int px, int cy, int pw,
                                        int mouseX, int mouseY, PlayerESPModule mod) {
        int pad = 14;

        // Tracer toggle
        renderToggleRow(graphics, px + pad, cy, pw - pad * 2, "Tracer", mod.isTracerEnabled(), mouseX, mouseY);
        cy += 18;

        graphics.text(this.font, "Player ESP Colors:", px + pad, cy, 0xFFAAAAFF, true);
        cy += 16;

        drawColorEntry(graphics, px + pad, cy, pw - pad * 2, "Default Color", mod.getDefaultColor(), mouseX, mouseY);
        cy += 18;
        drawColorEntry(graphics, px + pad, cy, pw - pad * 2, "Sneaking Color", mod.getSneakingColor(), mouseX, mouseY);
        cy += 18;
        drawColorEntry(graphics, px + pad, cy, pw - pad * 2, "Friend Color", mod.getFriendColor(), mouseX, mouseY);
        cy += 24;

        String[] toggleLabels = {"Show Health", "Show Name", "Show Distance"};
        boolean[] toggleValues = {mod.isShowHealth(), mod.isShowName(), mod.isShowDistance()};

        for (int i = 0; i < toggleLabels.length; i++) {
            boolean hovered = mouseX >= px + pad && mouseX <= px + pw - pad
                    && mouseY >= cy && mouseY <= cy + 12;
            String status = toggleValues[i] ? "\u25CF ON" : "\u25CB OFF";
            int statusColor = toggleValues[i] ? 0xFF2ECC71 : 0xFFE74C3C;
            int labelColor = hovered ? 0xFFFFFFFF : 0xFFCCCCCC;

            graphics.text(this.font, toggleLabels[i], px + pad, cy, labelColor, false);
            graphics.text(this.font, status, px + pw - pad - this.font.width(status), cy, statusColor, false);
            cy += 16;
        }

        cy += 8;
        graphics.text(this.font, "Range: " + mod.getRange() + " blocks", px + pad, cy, 0xFFCCCCCC, false);
    }

    private void renderToggleRow(GuiGraphicsExtractor graphics, int x, int y, int w,
                                   String label, boolean value, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w
                && mouseY >= y && mouseY <= y + 12;
        String status = value ? "\u25CF ON" : "\u25CB OFF";
        int statusColor = value ? 0xFF2ECC71 : 0xFFE74C3C;
        int labelColor = hovered ? 0xFFFFFFFF : 0xFFCCCCCC;

        graphics.text(this.font, label, x, y, labelColor, false);
        graphics.text(this.font, status, x + w - this.font.width(status), y, statusColor, false);
    }

    private void drawColorEntry(GuiGraphicsExtractor graphics, int x, int y, int w,
                                 String label, int color, int mouseX, int mouseY) {
        graphics.text(this.font, label, x, y, 0xFFCCCCCC, false);
        // Color preview box (clickable to open color wheel)
        int boxX = x + w - 16;
        boolean boxHovered = mouseX >= boxX - 1 && mouseX <= boxX + 13
                && mouseY >= y - 1 && mouseY <= y + 13;
        graphics.fill(boxX, y, boxX + 12, y + 12, color | 0xFF000000);
        int borderColor = boxHovered ? 0xFFFFFFFF : 0xFF555555;
        graphics.fill(boxX - 1, y - 1, boxX + 13, y, borderColor);
        graphics.fill(boxX - 1, y + 12, boxX + 13, y + 13, borderColor);
        graphics.fill(boxX - 1, y, boxX, y + 12, borderColor);
        graphics.fill(boxX + 12, y, boxX + 13, y + 12, borderColor);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromSelf) {
        int button = event.button();
        double mouseX = event.x();
        double mouseY = event.y();

        if (button != 0) return super.mouseClicked(event, fromSelf);

        // Check color wheel first
        if (colorWheel != null && colorWheel.mouseClicked((int) mouseX, (int) mouseY)) {
            return true;
        }

        int panelW = 300;
        int panelH = 400;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        int pad = 14;

        // Handle tracer toggle for all ESP modules (first row in config)
        int tracerY = panelY + 34 - scrollOffset;
        if (mouseX >= panelX + pad && mouseX <= panelX + panelW - pad
                && mouseY >= tracerY && mouseY <= tracerY + 12) {
            if (module instanceof BlockESPModule blockMod) {
                blockMod.setTracerEnabled(!blockMod.isTracerEnabled());
                return true;
            } else if (module instanceof StorageESPModule storageMod) {
                storageMod.setTracerEnabled(!storageMod.isTracerEnabled());
                return true;
            } else if (module instanceof PlayerESPModule playerMod) {
                playerMod.setTracerEnabled(!playerMod.isTracerEnabled());
                return true;
            }
        }

        if (module instanceof BlockESPModule blockESP) {
            // Color swatch click for Block ESP (second row: tracer=18px offset)
            int colorRowY = panelY + 34 - scrollOffset + 18;
            int colorBoxX = panelX + panelW - pad - 16;
            if (mouseX >= colorBoxX - 1 && mouseX <= colorBoxX + 13
                    && mouseY >= colorRowY - 1 && mouseY <= colorRowY + 13) {
                openColorWheel(panelX + panelW + 10, panelY + 34, blockESP.getEspColor(),
                        c -> blockESP.setEspColor(c), "blockEspColor");
                return true;
            }

            int cy = panelY + 34 - scrollOffset + 18 + 18 + 14;
            var blocks = blockESP.getTrackedBlocks();
            for (int i = 0; i < blocks.size(); i++) {
                if (mouseX >= panelX + pad && mouseX <= panelX + panelW - pad
                        && mouseY >= cy && mouseY <= cy + 12) {
                    blockESP.removeBlock(blocks.get(i));
                    return true;
                }
                cy += 14;
            }

            cy += 6;
            if (mouseX >= panelX + pad && mouseX <= panelX + pad + this.font.width("[+ Add Block]")
                    && mouseY >= cy && mouseY <= cy + 12) {
                this.minecraft.setScreen(new BlockPickerScreen(this, blockESP));
                return true;
            }

            cy += 18;
            if (mouseX >= panelX + pad && mouseX <= panelX + pad + 20
                    && mouseY >= cy && mouseY <= cy + 14) {
                blockESP.setRange(Math.max(8, blockESP.getRange() - 8));
                return true;
            }
            if (mouseX >= panelX + pad + 100 && mouseX <= panelX + pad + 120
                    && mouseY >= cy && mouseY <= cy + 14) {
                blockESP.setRange(Math.min(64, blockESP.getRange() + 8));
                return true;
            }
        } else if (module instanceof StorageESPModule storageESP) {
            int cy = panelY + 34 - scrollOffset + 18 + 16;
            handleStorageToggle(storageESP, panelX, panelW, pad, (int) mouseX, (int) mouseY, cy);
        } else if (module instanceof PlayerESPModule playerESP) {
            // Color swatch clicks for Player ESP
            int colorBaseY = panelY + 34 - scrollOffset + 18 + 16;
            String[] colorFields = {"defaultColor", "sneakingColor", "friendColor"};
            int[] colors = {playerESP.getDefaultColor(), playerESP.getSneakingColor(), playerESP.getFriendColor()};
            ColorWheelWidget.ColorChangeCallback[] callbacks = {
                    c -> playerESP.setDefaultColor(c),
                    c -> playerESP.setSneakingColor(c),
                    c -> playerESP.setFriendColor(c)
            };
            for (int i = 0; i < 3; i++) {
                int rowY = colorBaseY + i * 18;
                int colorBoxX = panelX + panelW - pad - 16;
                if (mouseX >= colorBoxX - 1 && mouseX <= colorBoxX + 13
                        && mouseY >= rowY - 1 && mouseY <= rowY + 13) {
                    openColorWheel(panelX + panelW + 10, panelY + 34, colors[i], callbacks[i], colorFields[i]);
                    return true;
                }
            }

            int cy = panelY + 34 - scrollOffset + 18 + 16 + 18 + 18 + 18 + 6;
            handlePlayerToggle(playerESP, panelX, panelW, pad, (int) mouseX, (int) mouseY, cy);
        }

        // Close color wheel if clicking outside it
        if (colorWheel != null && !colorWheel.isMouseOver((int) mouseX, (int) mouseY)) {
            colorWheel = null;
            activeColorField = null;
        }

        return super.mouseClicked(event, fromSelf);
    }

    private void handleStorageToggle(StorageESPModule mod, int px, int pw, int pad, int mx, int my, int cy) {
        if (mx < px + pad || mx > px + pw - pad) return;
        if (my >= cy && my <= cy + 12) { mod.setShowChests(!mod.isShowChests()); return; }
        cy += 16;
        if (my >= cy && my <= cy + 12) { mod.setShowEnderChests(!mod.isShowEnderChests()); return; }
        cy += 16;
        if (my >= cy && my <= cy + 12) { mod.setShowShulkerBoxes(!mod.isShowShulkerBoxes()); return; }
        cy += 16;
        if (my >= cy && my <= cy + 12) { mod.setShowBarrels(!mod.isShowBarrels()); return; }
        cy += 16;
        if (my >= cy && my <= cy + 12) { mod.setShowHoppers(!mod.isShowHoppers()); return; }
        cy += 16;
        if (my >= cy && my <= cy + 12) { mod.setShowDispensers(!mod.isShowDispensers()); return; }
        cy += 16;
        if (my >= cy && my <= cy + 12) { mod.setShowDroppers(!mod.isShowDroppers()); return; }
        cy += 16;
        if (my >= cy && my <= cy + 12) { mod.setShowFurnaces(!mod.isShowFurnaces()); return; }
    }

    private void handlePlayerToggle(PlayerESPModule mod, int px, int pw, int pad, int mx, int my, int cy) {
        if (mx < px + pad || mx > px + pw - pad) return;
        if (my >= cy && my <= cy + 12) { mod.setShowHealth(!mod.isShowHealth()); return; }
        cy += 16;
        if (my >= cy && my <= cy + 12) { mod.setShowName(!mod.isShowName()); return; }
        cy += 16;
        if (my >= cy && my <= cy + 12) { mod.setShowDistance(!mod.isShowDistance()); }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (colorWheel != null && event.button() == 0) {
            if (colorWheel.mouseDragged((int) event.x(), (int) event.y())) {
                return true;
            }
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (colorWheel != null) {
            colorWheel.mouseReleased();
        }
        return super.mouseReleased(event);
    }

    private void openColorWheel(int wheelX, int wheelY, int currentColor,
                                 ColorWheelWidget.ColorChangeCallback callback, String fieldName) {
        if (activeColorField != null && activeColorField.equals(fieldName)) {
            colorWheel = null;
            activeColorField = null;
        } else {
            colorWheel = new ColorWheelWidget(wheelX, wheelY, currentColor, callback);
            activeColorField = fieldName;
        }
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
