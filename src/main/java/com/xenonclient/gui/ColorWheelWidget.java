package com.xenonclient.gui;

import com.xenonclient.module.setting.ColorSetting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A color wheel widget that renders a full hue/saturation wheel with a brightness slider.
 * Used in module settings to pick colors from a rich palette.
 */
public class ColorWheelWidget {

    private static final int WHEEL_SEGMENTS = 360;
    private static final int WHEEL_RINGS = 32;
    private static final int BRIGHTNESS_BAR_WIDTH = 16;
    private static final int BRIGHTNESS_BAR_MARGIN = 8;

    private final int x;
    private final int y;
    private final int wheelRadius;
    private ColorSetting colorSetting;
    private float currentHue;
    private float currentSaturation;
    private float currentBrightness;
    private boolean draggingWheel;
    private boolean draggingBrightness;
    private boolean visible;

    public ColorWheelWidget(int x, int y, int wheelRadius) {
        this.x = x;
        this.y = y;
        this.wheelRadius = wheelRadius;
        this.currentHue = 0.0f;
        this.currentSaturation = 1.0f;
        this.currentBrightness = 1.0f;
        this.draggingWheel = false;
        this.draggingBrightness = false;
        this.visible = false;
    }

    public void setColorSetting(ColorSetting colorSetting) {
        this.colorSetting = colorSetting;
        if (colorSetting != null) {
            int r = colorSetting.getRed();
            int g = colorSetting.getGreen();
            int b = colorSetting.getBlue();
            float[] hsb = ColorSetting.rgbToHsb(r, g, b);
            this.currentHue = hsb[0];
            this.currentSaturation = hsb[1];
            this.currentBrightness = hsb[2];
        }
    }

    public ColorSetting getColorSetting() {
        return colorSetting;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getTotalWidth() {
        return wheelRadius * 2 + BRIGHTNESS_BAR_MARGIN + BRIGHTNESS_BAR_WIDTH;
    }

    public int getTotalHeight() {
        return wheelRadius * 2;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float alpha, Font font) {
        if (!visible || colorSetting == null) return;

        int centerX = x + wheelRadius;
        int centerY = y + wheelRadius;

        // Draw background
        int bgAlpha = (int) (alpha * 200);
        int bgColor = (bgAlpha << 24) | 0x00181828;
        graphics.fill(x - 6, y - 6, x + getTotalWidth() + 6, y + getTotalHeight() + 26, bgColor);

        // Draw border
        int borderAlpha = (int) (alpha * 150);
        int borderColor = (borderAlpha << 24) | 0x00444466;
        drawBorder(graphics, x - 6, y - 6, getTotalWidth() + 12, getTotalHeight() + 32, borderColor);

        // Draw the color wheel (hue around the circle, saturation from center to edge)
        renderColorWheel(graphics, centerX, centerY, alpha);

        // Draw selector indicator on wheel
        drawWheelSelector(graphics, centerX, centerY, alpha);

        // Draw brightness bar
        renderBrightnessBar(graphics, alpha);

        // Draw current color preview
        int previewY = y + getTotalHeight() + 4;
        int previewColor = ((int) (alpha * 255) << 24) | (colorSetting.getColor() & 0x00FFFFFF);
        graphics.fill(x, previewY, x + getTotalWidth(), previewY + 14, previewColor);

        // Draw preview border
        int prevBorder = ((int) (alpha * 100) << 24) | 0x00FFFFFF;
        drawBorder(graphics, x, previewY, getTotalWidth(), 14, prevBorder);

        // Draw hex label
        String hex = String.format("#%06X", colorSetting.getColor() & 0x00FFFFFF);
        int textColor = ((int) (alpha * 255) << 24) | 0x00FFFFFF;
        int hexWidth = font.width(hex);
        graphics.text(font, hex, x + (getTotalWidth() - hexWidth) / 2, previewY + 3, textColor, true);
    }

    private void renderColorWheel(GuiGraphicsExtractor graphics, int centerX, int centerY, float alpha) {
        int alphaInt = (int) (alpha * 255);

        for (int ring = 0; ring < WHEEL_RINGS; ring++) {
            float saturation = (float) ring / (WHEEL_RINGS - 1);
            float innerR = (float) wheelRadius * ring / WHEEL_RINGS;
            float outerR = (float) wheelRadius * (ring + 1) / WHEEL_RINGS;

            int steps = Math.max(36, WHEEL_SEGMENTS / 4);
            float angleStep = 360.0f / steps;

            for (int seg = 0; seg < steps; seg++) {
                float hue = (float) seg / steps;
                int rgb = ColorSetting.hsbToRgb(hue, saturation, currentBrightness);
                int color = (alphaInt << 24) | (rgb & 0x00FFFFFF);

                float startAngle = seg * angleStep;
                float endAngle = (seg + 1) * angleStep;

                float startRad = (float) Math.toRadians(startAngle);
                float endRad = (float) Math.toRadians(endAngle);

                int ix1 = (int) (centerX + Math.cos(startRad) * innerR);
                int iy1 = (int) (centerY + Math.sin(startRad) * innerR);
                int ox1 = (int) (centerX + Math.cos(startRad) * outerR);
                int oy1 = (int) (centerY + Math.sin(startRad) * outerR);
                int ix2 = (int) (centerX + Math.cos(endRad) * innerR);
                int iy2 = (int) (centerY + Math.sin(endRad) * innerR);
                int ox2 = (int) (centerX + Math.cos(endRad) * outerR);
                int oy2 = (int) (centerY + Math.sin(endRad) * outerR);

                fillQuad(graphics, ix1, iy1, ox1, oy1, ox2, oy2, ix2, iy2, color);
            }
        }

        // Draw wheel outline
        int outlineColor = ((int) (alpha * 100) << 24) | 0x00FFFFFF;
        drawCircleOutline(graphics, centerX, centerY, wheelRadius, outlineColor);
    }

    private void drawWheelSelector(GuiGraphicsExtractor graphics, int centerX, int centerY, float alpha) {
        float angle = currentHue * 2.0f * (float) Math.PI;
        float radius = currentSaturation * wheelRadius;
        int selectorX = (int) (centerX + Math.cos(angle) * radius);
        int selectorY = (int) (centerY + Math.sin(angle) * radius);

        int outlineAlpha = (int) (alpha * 255);
        // White outer ring
        drawCircleOutline(graphics, selectorX, selectorY, 5, (outlineAlpha << 24) | 0x00FFFFFF);
        // Black inner ring
        drawCircleOutline(graphics, selectorX, selectorY, 4, (outlineAlpha << 24) | 0x00000000);
    }

    private void renderBrightnessBar(GuiGraphicsExtractor graphics, float alpha) {
        int barX = x + wheelRadius * 2 + BRIGHTNESS_BAR_MARGIN;
        int barY = y;
        int barHeight = wheelRadius * 2;
        int alphaInt = (int) (alpha * 255);

        // Draw brightness gradient
        for (int i = 0; i < barHeight; i++) {
            float brightness = 1.0f - (float) i / barHeight;
            int rgb = ColorSetting.hsbToRgb(currentHue, currentSaturation, brightness);
            int color = (alphaInt << 24) | (rgb & 0x00FFFFFF);
            graphics.fill(barX, barY + i, barX + BRIGHTNESS_BAR_WIDTH, barY + i + 1, color);
        }

        // Draw bar outline
        int outlineColor = ((int) (alpha * 100) << 24) | 0x00FFFFFF;
        drawBorder(graphics, barX, barY, BRIGHTNESS_BAR_WIDTH, barHeight, outlineColor);

        // Draw brightness selector
        int selectorY = barY + (int) ((1.0f - currentBrightness) * barHeight);
        selectorY = Math.max(barY, Math.min(barY + barHeight - 2, selectorY));
        int selectorColor = (alphaInt << 24) | 0x00FFFFFF;
        graphics.fill(barX - 2, selectorY - 1, barX + BRIGHTNESS_BAR_WIDTH + 2, selectorY + 2, selectorColor);
    }

    public boolean mouseClicked(int mouseX, int mouseY) {
        if (!visible || colorSetting == null) return false;

        int centerX = x + wheelRadius;
        int centerY = y + wheelRadius;

        // Check if clicking on the wheel
        float dx = mouseX - centerX;
        float dy = mouseY - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist <= wheelRadius) {
            draggingWheel = true;
            updateFromWheel(mouseX, mouseY, centerX, centerY);
            return true;
        }

        // Check if clicking on the brightness bar
        int barX = x + wheelRadius * 2 + BRIGHTNESS_BAR_MARGIN;
        int barY = y;
        int barHeight = wheelRadius * 2;

        if (mouseX >= barX && mouseX <= barX + BRIGHTNESS_BAR_WIDTH
                && mouseY >= barY && mouseY <= barY + barHeight) {
            draggingBrightness = true;
            updateFromBrightnessBar(mouseY, barY, barHeight);
            return true;
        }

        return false;
    }

    public boolean mouseDragged(int mouseX, int mouseY) {
        if (!visible || colorSetting == null) return false;

        int centerX = x + wheelRadius;
        int centerY = y + wheelRadius;

        if (draggingWheel) {
            updateFromWheel(mouseX, mouseY, centerX, centerY);
            return true;
        }

        if (draggingBrightness) {
            int barY = y;
            int barHeight = wheelRadius * 2;
            updateFromBrightnessBar(mouseY, barY, barHeight);
            return true;
        }

        return false;
    }

    public void mouseReleased() {
        draggingWheel = false;
        draggingBrightness = false;
    }

    private void updateFromWheel(int mouseX, int mouseY, int centerX, int centerY) {
        float dx = mouseX - centerX;
        float dy = mouseY - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        currentHue = (float) (Math.atan2(dy, dx) / (2.0 * Math.PI));
        if (currentHue < 0) currentHue += 1.0f;

        currentSaturation = Math.min(1.0f, dist / wheelRadius);

        applyColor();
    }

    private void updateFromBrightnessBar(int mouseY, int barY, int barHeight) {
        currentBrightness = 1.0f - (float) (mouseY - barY) / barHeight;
        currentBrightness = Math.max(0.0f, Math.min(1.0f, currentBrightness));
        applyColor();
    }

    private void applyColor() {
        if (colorSetting != null) {
            colorSetting.setFromHSB(currentHue, currentSaturation, currentBrightness);
        }
    }

    public boolean isInBounds(int mouseX, int mouseY) {
        return mouseX >= x - 6 && mouseX <= x + getTotalWidth() + 6
                && mouseY >= y - 6 && mouseY <= y + getTotalHeight() + 26;
    }

    private void fillQuad(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2,
                          int x3, int y3, int x4, int y4, int color) {
        int minY = Math.min(Math.min(y1, y2), Math.min(y3, y4));
        int maxY = Math.max(Math.max(y1, y2), Math.max(y3, y4));

        for (int scanY = minY; scanY <= maxY; scanY++) {
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;

            int[][] edges = {{x1, y1, x2, y2}, {x2, y2, x3, y3},
                    {x3, y3, x4, y4}, {x4, y4, x1, y1}};

            for (int[] edge : edges) {
                int ey1 = edge[1], ey2 = edge[3];
                if (scanY >= Math.min(ey1, ey2) && scanY <= Math.max(ey1, ey2)) {
                    int ex1 = edge[0], ex2 = edge[2];
                    if (ey1 == ey2) {
                        minX = Math.min(minX, Math.min(ex1, ex2));
                        maxX = Math.max(maxX, Math.max(ex1, ex2));
                        continue;
                    }
                    int edgeX = ex1 + (scanY - ey1) * (ex2 - ex1) / (ey2 - ey1);
                    minX = Math.min(minX, edgeX);
                    maxX = Math.max(maxX, edgeX);
                }
            }

            if (minX <= maxX) {
                graphics.fill(minX, scanY, maxX + 1, scanY + 1, color);
            }
        }
    }

    private void drawCircleOutline(GuiGraphicsExtractor graphics, int cx, int cy, float radius, int color) {
        int steps = 128;
        for (int i = 0; i < steps; i++) {
            float angle = (float) (2 * Math.PI * i / steps);
            int px = (int) (cx + Math.cos(angle) * radius);
            int py = (int) (cy + Math.sin(angle) * radius);
            graphics.fill(px, py, px + 1, py + 1, color);
        }
    }

    private void drawBorder(GuiGraphicsExtractor graphics, int bx, int by, int bw, int bh, int color) {
        graphics.fill(bx, by, bx + bw, by + 1, color);
        graphics.fill(bx, by + bh - 1, bx + bw, by + bh, color);
        graphics.fill(bx, by, bx + 1, by + bh, color);
        graphics.fill(bx + bw - 1, by, bx + bw, by + bh, color);
    }
}
