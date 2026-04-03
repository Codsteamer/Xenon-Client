package com.xenonclient.gui;

import com.xenonclient.XenonClient;
import com.xenonclient.module.Module;
import com.xenonclient.section.Section;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * A panel that slides open when a section is selected from the wheel.
 * Displays the section's modules with toggle (left-click) and config (right-click).
 */
public class SectionPanel {

    private static final int PANEL_BG = 0xE0101020;
    private static final int PANEL_BORDER = 0xFF333355;
    private static final int HEADER_HEIGHT = 40;
    private static final int ITEM_HEIGHT = 32;
    private static final int PADDING = 12;

    private final Section section;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private float slideProgress = 0.0f;
    private int hoveredModuleIndex = -1;

    public SectionPanel(Section section, int x, int y, int width, int height) {
        this.section = section;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Section getSection() {
        return section;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, Font font) {
        if (slideProgress < 1.0f) {
            slideProgress = Math.min(1.0f, slideProgress + delta * 0.12f);
        }

        float eased = easeOutCubic(slideProgress);
        int animatedX = (int) (x + (1.0f - eased) * 60);
        float alpha = eased;

        int bgAlpha = (int) (alpha * 224);
        int bgColor = (bgAlpha << 24) | (PANEL_BG & 0x00FFFFFF);

        int shadowAlpha = (int) (alpha * 80);
        graphics.fill(animatedX + 3, y + 3, animatedX + width + 3, y + height + 3,
                (shadowAlpha << 24));

        graphics.fill(animatedX, y, animatedX + width, y + height, bgColor);

        int borderAlpha = (int) (alpha * 255);
        int borderColor = (borderAlpha << 24) | (PANEL_BORDER & 0x00FFFFFF);
        drawBorder(graphics, animatedX, y, width, height, borderColor);

        int accentColor = (borderAlpha << 24) | (section.getColor() & 0x00FFFFFF);
        graphics.fill(animatedX, y, animatedX + width, y + 3, accentColor);

        drawHeader(graphics, animatedX, y, alpha, font);
        drawModules(graphics, animatedX, y + HEADER_HEIGHT, alpha, font, mouseX, mouseY);
    }

    private void drawHeader(GuiGraphicsExtractor graphics, int panelX, int panelY, float alpha, Font font) {
        int textAlpha = (int) (alpha * 255);
        int textColor = (textAlpha << 24) | 0x00FFFFFF;

        String header = section.getIcon() + " " + section.getName();
        graphics.text(font, header, panelX + PADDING, panelY + PADDING,
                textColor, true);

        int sepColor = (int) (alpha * 100) << 24 | 0x00FFFFFF;
        graphics.fill(panelX + PADDING, panelY + HEADER_HEIGHT - 2,
                panelX + width - PADDING, panelY + HEADER_HEIGHT - 1, sepColor);
    }

    private void drawModules(GuiGraphicsExtractor graphics, int panelX, int contentY, float alpha,
                              Font font, int mouseX, int mouseY) {
        int textAlpha = (int) (alpha * 200);
        int descColor = (textAlpha << 24) | 0x00AAAAAA;

        List<Module> modules = XenonClient.getInstance().getModuleManager()
                .getModulesForSection(section.getName());

        if (modules.isEmpty()) {
            graphics.text(font, "No modules yet",
                    panelX + PADDING, contentY + PADDING, descColor, false);
            return;
        }

        hoveredModuleIndex = -1;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int slotY = contentY + PADDING + i * ITEM_HEIGHT;
            if (slotY + ITEM_HEIGHT > contentY + height - HEADER_HEIGHT) break;

            int slotX = panelX + PADDING;
            int slotW = width - PADDING * 2;
            int slotH = ITEM_HEIGHT - 4;

            boolean hovered = mouseX >= slotX && mouseX <= slotX + slotW
                    && mouseY >= slotY && mouseY <= slotY + slotH;

            if (hovered) {
                hoveredModuleIndex = i;
            }

            int slotBgAlpha = (int) (alpha * (hovered ? 70 : 40));
            int slotBg = (slotBgAlpha << 24) | 0x00FFFFFF;
            graphics.fill(slotX, slotY, slotX + slotW, slotY + slotH, slotBg);

            int slotBorderAlpha = (int) (alpha * (hovered ? 120 : 60));
            int slotBorder = hovered
                    ? ((slotBorderAlpha << 24) | (section.getColor() & 0x00FFFFFF))
                    : ((slotBorderAlpha << 24) | 0x00FFFFFF);
            drawBorder(graphics, slotX, slotY, slotW, slotH, slotBorder);

            int nameColor = (int) (alpha * 255) << 24 | 0x00FFFFFF;
            graphics.text(font, module.getName(), slotX + 8, slotY + 4, nameColor, true);

            graphics.text(font, module.getDescription(), slotX + 8, slotY + 15,
                    descColor, false);

            String status = module.isEnabled() ? "\u25cf ON" : "\u25cb OFF";
            int statusColor = module.isEnabled()
                    ? ((int) (alpha * 255) << 24 | 0x002ECC71)
                    : ((int) (alpha * 255) << 24 | 0x00E74C3C);
            int statusW = font.width(status);
            graphics.text(font, status, slotX + slotW - statusW - 8, slotY + 4,
                    statusColor, true);

            if (hovered && module.hasConfig()) {
                String configHint = "[R-Click: Config]";
                int hintColor = (int) (alpha * 180) << 24 | 0x00AAAAFF;
                int hintW = font.width(configHint);
                graphics.text(font, configHint, slotX + slotW - hintW - 8, slotY + 15,
                        hintColor, false);
            }
        }

        int footerY = contentY + height - HEADER_HEIGHT - 16;
        String hint = "Left-click: Toggle | Right-click: Config";
        int hintW = font.width(hint);
        int hintColor = (int) (alpha * 120) << 24 | 0x00AAAAAA;
        graphics.text(font, hint, panelX + (width - hintW) / 2, footerY, hintColor, false);
    }

    public boolean handleLeftClick(int mouseX, int mouseY) {
        if (hoveredModuleIndex < 0) return false;

        List<Module> modules = XenonClient.getInstance().getModuleManager()
                .getModulesForSection(section.getName());

        if (hoveredModuleIndex < modules.size()) {
            modules.get(hoveredModuleIndex).toggle();
            return true;
        }
        return false;
    }

    public Module handleRightClick(int mouseX, int mouseY) {
        if (hoveredModuleIndex < 0) return null;

        List<Module> modules = XenonClient.getInstance().getModuleManager()
                .getModulesForSection(section.getName());

        if (hoveredModuleIndex < modules.size()) {
            Module mod = modules.get(hoveredModuleIndex);
            if (mod.hasConfig()) {
                return mod;
            }
        }
        return null;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void drawBorder(GuiGraphicsExtractor graphics, int bx, int by, int bw, int bh, int color) {
        graphics.fill(bx, by, bx + bw, by + 1, color);
        graphics.fill(bx, by + bh - 1, bx + bw, by + bh, color);
        graphics.fill(bx, by, bx + 1, by + bh, color);
        graphics.fill(bx + bw - 1, by, bx + bw, by + bh, color);
    }

    private float easeOutCubic(float t) {
        return 1.0f - (float) Math.pow(1.0f - t, 3);
    }
}
