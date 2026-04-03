package com.xenonclient.gui;

import com.xenonclient.section.Section;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A panel that slides open when a section is selected from the wheel.
 * Displays section details and will eventually contain module toggles.
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
        drawContent(graphics, animatedX, y + HEADER_HEIGHT, alpha, font);
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

    private void drawContent(GuiGraphicsExtractor graphics, int panelX, int contentY, float alpha, Font font) {
        int textAlpha = (int) (alpha * 200);
        int descColor = (textAlpha << 24) | 0x00AAAAAA;
        int dimColor = (textAlpha << 24) | 0x00777777;

        graphics.text(font, section.getDescription(),
                panelX + PADDING, contentY + PADDING, descColor, false);

        String status = section.isEnabled() ? "\u25CF Enabled" : "\u25CB Disabled";
        int statusColor = section.isEnabled()
                ? ((textAlpha << 24) | 0x002ECC71)
                : ((textAlpha << 24) | 0x00E74C3C);
        graphics.text(font, status,
                panelX + PADDING, contentY + PADDING + 16, statusColor, false);

        int placeholderY = contentY + PADDING + 48;
        graphics.text(font, "Modules",
                panelX + PADDING, placeholderY, (textAlpha << 24) | 0x00CCCCCC, true);

        for (int i = 0; i < 4; i++) {
            int slotY = placeholderY + 16 + i * ITEM_HEIGHT;
            if (slotY + ITEM_HEIGHT > contentY + height - HEADER_HEIGHT) break;

            int slotBg = ((int) (alpha * 40) << 24) | 0x00FFFFFF;
            graphics.fill(panelX + PADDING, slotY,
                    panelX + width - PADDING, slotY + ITEM_HEIGHT - 4, slotBg);

            int slotBorder = ((int) (alpha * 60) << 24) | 0x00FFFFFF;
            drawBorder(graphics, panelX + PADDING, slotY,
                    width - PADDING * 2, ITEM_HEIGHT - 4, slotBorder);

            graphics.text(font, "Coming soon...",
                    panelX + PADDING + 8, slotY + 8, dimColor, false);
        }
    }

    private void drawBorder(GuiGraphicsExtractor graphics, int bx, int by, int bw, int bh, int color) {
        graphics.fill(bx, by, bx + bw, by + 1, color);
        graphics.fill(bx, by + bh - 1, bx + bw, by + bh, color);
        graphics.fill(bx, by, bx + 1, by + bh, color);
        graphics.fill(bx + bw - 1, by, bx + bw, by + bh, color);
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private float easeOutCubic(float t) {
        return 1.0f - (float) Math.pow(1.0f - t, 3);
    }
}
