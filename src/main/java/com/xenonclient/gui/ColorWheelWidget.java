package com.xenonclient.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.Color;

/**
 * A color wheel widget that allows picking colors via HSB model.
 * Renders a hue/saturation wheel and a brightness slider bar.
 */
public class ColorWheelWidget {

    private static final int WHEEL_RADIUS = 50;
    private static final int BRIGHTNESS_BAR_WIDTH = 12;
    private static final int BRIGHTNESS_BAR_GAP = 8;
    private static final int RINGS = 24;
    private static final int SEGMENTS = 72;

    private final int x;
    private final int y;
    private float hue;
    private float saturation;
    private float brightness;
    private boolean draggingWheel;
    private boolean draggingBrightness;

    private final ColorChangeCallback callback;

    @FunctionalInterface
    public interface ColorChangeCallback {
        void onColorChanged(int argbColor);
    }

    public ColorWheelWidget(int x, int y, int initialColor, ColorChangeCallback callback) {
        this.x = x;
        this.y = y;
        this.callback = callback;

        float[] hsb = Color.RGBtoHSB(
                (initialColor >> 16) & 0xFF,
                (initialColor >> 8) & 0xFF,
                initialColor & 0xFF,
                null
        );
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    public void render(GuiGraphicsExtractor graphics, Font font) {
        int cx = x + WHEEL_RADIUS;
        int cy = y + WHEEL_RADIUS;

        // Draw the hue/saturation wheel
        for (int ring = 0; ring < RINGS; ring++) {
            float sat = (float) (ring + 1) / RINGS;
            float innerR = (float) ring / RINGS * WHEEL_RADIUS;
            float outerR = (float) (ring + 1) / RINGS * WHEEL_RADIUS;

            for (int seg = 0; seg < SEGMENTS; seg++) {
                float h = (float) seg / SEGMENTS;
                int rgb = Color.HSBtoRGB(h, sat, brightness);
                int color = 0xFF000000 | rgb;

                float startAngle = (float) (2 * Math.PI * seg / SEGMENTS);
                float endAngle = (float) (2 * Math.PI * (seg + 1) / SEGMENTS);
                float midAngle = (startAngle + endAngle) / 2;
                float midR = (innerR + outerR) / 2;

                int px = (int) (cx + Math.cos(midAngle) * midR);
                int py = (int) (cy + Math.sin(midAngle) * midR);
                int size = Math.max(2, (int) ((outerR - innerR) * 1.2f));
                graphics.fill(px - size / 2, py - size / 2, px + size / 2, py + size / 2, color);
            }
        }

        // Draw selection indicator on the wheel
        float selAngle = hue * 2 * (float) Math.PI;
        float selR = saturation * WHEEL_RADIUS;
        int selX = (int) (cx + Math.cos(selAngle) * selR);
        int selY = (int) (cy + Math.sin(selAngle) * selR);
        graphics.fill(selX - 3, selY - 3, selX + 3, selY + 3, 0xFFFFFFFF);
        graphics.fill(selX - 2, selY - 2, selX + 2, selY + 2, getCurrentColor() | 0xFF000000);

        // Draw brightness bar
        int barX = x + WHEEL_RADIUS * 2 + BRIGHTNESS_BAR_GAP;
        int barY = y;
        int barH = WHEEL_RADIUS * 2;

        for (int i = 0; i < barH; i++) {
            float b = 1.0f - (float) i / barH;
            int rgb = Color.HSBtoRGB(hue, saturation, b);
            int color = 0xFF000000 | rgb;
            graphics.fill(barX, barY + i, barX + BRIGHTNESS_BAR_WIDTH, barY + i + 1, color);
        }

        // Brightness indicator
        int bIndicatorY = barY + (int) ((1.0f - brightness) * barH);
        graphics.fill(barX - 2, bIndicatorY - 1, barX + BRIGHTNESS_BAR_WIDTH + 2, bIndicatorY + 1, 0xFFFFFFFF);

        // Border around brightness bar
        graphics.fill(barX - 1, barY - 1, barX + BRIGHTNESS_BAR_WIDTH + 1, barY, 0xFF555555);
        graphics.fill(barX - 1, barY + barH, barX + BRIGHTNESS_BAR_WIDTH + 1, barY + barH + 1, 0xFF555555);
        graphics.fill(barX - 1, barY, barX, barY + barH, 0xFF555555);
        graphics.fill(barX + BRIGHTNESS_BAR_WIDTH, barY, barX + BRIGHTNESS_BAR_WIDTH + 1, barY + barH, 0xFF555555);

        // Color preview and hex label
        int previewY = y + WHEEL_RADIUS * 2 + 6;
        int currentColor = getCurrentColor() | 0xFF000000;
        graphics.fill(x, previewY, x + WHEEL_RADIUS * 2 + BRIGHTNESS_BAR_GAP + BRIGHTNESS_BAR_WIDTH, previewY + 14, currentColor);
        graphics.fill(x - 1, previewY - 1, x + WHEEL_RADIUS * 2 + BRIGHTNESS_BAR_GAP + BRIGHTNESS_BAR_WIDTH + 1, previewY, 0xFF555555);
        graphics.fill(x - 1, previewY + 14, x + WHEEL_RADIUS * 2 + BRIGHTNESS_BAR_GAP + BRIGHTNESS_BAR_WIDTH + 1, previewY + 15, 0xFF555555);

        String hex = String.format("#%06X", currentColor & 0x00FFFFFF);
        int hexW = font.width(hex);
        int previewMidX = x + (WHEEL_RADIUS * 2 + BRIGHTNESS_BAR_GAP + BRIGHTNESS_BAR_WIDTH) / 2;
        graphics.text(font, hex, previewMidX - hexW / 2, previewY + 3, brightness > 0.5f ? 0xFF000000 : 0xFFFFFFFF, false);
    }

    public boolean mouseClicked(int mouseX, int mouseY) {
        int cx = x + WHEEL_RADIUS;
        int cy = y + WHEEL_RADIUS;

        // Check wheel
        float dx = mouseX - cx;
        float dy = mouseY - cy;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist <= WHEEL_RADIUS) {
            draggingWheel = true;
            updateWheelFromMouse(mouseX, mouseY);
            return true;
        }

        // Check brightness bar
        int barX = x + WHEEL_RADIUS * 2 + BRIGHTNESS_BAR_GAP;
        int barY = y;
        int barH = WHEEL_RADIUS * 2;
        if (mouseX >= barX && mouseX <= barX + BRIGHTNESS_BAR_WIDTH
                && mouseY >= barY && mouseY <= barY + barH) {
            draggingBrightness = true;
            updateBrightnessFromMouse(mouseY);
            return true;
        }

        return false;
    }

    public boolean mouseDragged(int mouseX, int mouseY) {
        if (draggingWheel) {
            updateWheelFromMouse(mouseX, mouseY);
            return true;
        }
        if (draggingBrightness) {
            updateBrightnessFromMouse(mouseY);
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        draggingWheel = false;
        draggingBrightness = false;
    }

    private void updateWheelFromMouse(int mouseX, int mouseY) {
        int cx = x + WHEEL_RADIUS;
        int cy = y + WHEEL_RADIUS;
        float dx = mouseX - cx;
        float dy = mouseY - cy;

        float angle = (float) Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * (float) Math.PI;
        hue = angle / (2 * (float) Math.PI);

        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        saturation = Math.min(1.0f, dist / WHEEL_RADIUS);

        notifyCallback();
    }

    private void updateBrightnessFromMouse(int mouseY) {
        int barY = y;
        int barH = WHEEL_RADIUS * 2;
        float normalized = (float) (mouseY - barY) / barH;
        brightness = 1.0f - Math.max(0.0f, Math.min(1.0f, normalized));
        notifyCallback();
    }

    private void notifyCallback() {
        if (callback != null) {
            callback.onColorChanged(getCurrentColor());
        }
    }

    public int getCurrentColor() {
        return Color.HSBtoRGB(hue, saturation, brightness) & 0x00FFFFFF;
    }

    public int getFullWidth() {
        return WHEEL_RADIUS * 2 + BRIGHTNESS_BAR_GAP + BRIGHTNESS_BAR_WIDTH;
    }

    public int getFullHeight() {
        return WHEEL_RADIUS * 2 + 22;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + getFullWidth()
                && mouseY >= y && mouseY <= y + getFullHeight();
    }
}
