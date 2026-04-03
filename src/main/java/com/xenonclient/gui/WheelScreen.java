package com.xenonclient.gui;

import com.xenonclient.XenonClient;
import com.xenonclient.section.Section;
import com.xenonclient.section.SectionManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Optimized radial wheel screen for Xenon Client.
 * Uses filled ring bg + highlight-only arc + divider lines.
 * ~800 fill calls per frame instead of ~50,000.
 */
public class WheelScreen extends Screen {

    private static final float OUTER_RADIUS = 120.0f;
    private static final float INNER_RADIUS = 40.0f;
    private static final float ICON_RADIUS = 80.0f;
    private static final int RING_BG_COLOR = 0xC0181830;
    private static final int BORDER_COLOR = 0xFF444444;
    private static final int CENTER_GLOW = 0xFF6C3CE0;

    private int hoveredSection = -1;
    private float animationProgress = 0.0f;
    private boolean opening = true;
    private SectionPanel activePanel = null;

    public WheelScreen() {
        super(Component.literal("Xenon Client"));
    }

    @Override
    protected void init() {
        super.init();
        animationProgress = 0.0f;
        opening = true;
        activePanel = null;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (opening && animationProgress < 1.0f) {
            animationProgress = Math.min(1.0f, animationProgress + delta * 0.15f);
        }

        float easedProgress = easeOutBack(animationProgress);

        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int wheelCenterX = activePanel != null ? centerX - 140 : centerX;

        SectionManager manager = XenonClient.getInstance().getSectionManager();
        List<Section> sections = manager.getSections();
        int sectionCount = sections.size();

        if (sectionCount == 0) return;

        float anglePerSection = 360.0f / sectionCount;

        hoveredSection = getHoveredSection(mouseX, mouseY, wheelCenterX, centerY, sectionCount, easedProgress);

        drawWheel(graphics, wheelCenterX, centerY, sections, anglePerSection, easedProgress);
        drawCenter(graphics, wheelCenterX, centerY, easedProgress);
        drawLabels(graphics, wheelCenterX, centerY, sections, anglePerSection, easedProgress);
        drawTitle(graphics, wheelCenterX, centerY, easedProgress);

        if (activePanel != null) {
            activePanel.render(graphics, mouseX, mouseY, delta, this.font);
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawWheel(GuiGraphicsExtractor graphics, int cx, int cy, List<Section> sections,
                           float anglePerSection, float progress) {
        float currentOuter = OUTER_RADIUS * progress;
        float currentInner = INNER_RADIUS * progress;

        drawRing(graphics, cx, cy, currentInner, currentOuter, RING_BG_COLOR);

        if (hoveredSection >= 0) {
            Section section = sections.get(hoveredSection);
            float startAngle = hoveredSection * anglePerSection - 90;
            int highlightColor = brightenColor(section.getColor(), 0.9f);
            drawArcSegmentFast(graphics, cx, cy, currentInner, currentOuter,
                    startAngle, startAngle + anglePerSection, highlightColor);
        }

        for (int i = 0; i < sections.size(); i++) {
            float angle = i * anglePerSection - 90;
            drawRadialLine(graphics, cx, cy, currentInner, currentOuter, angle, BORDER_COLOR);
        }

        drawCircleOutline(graphics, cx, cy, currentOuter, BORDER_COLOR);
        drawCircleOutline(graphics, cx, cy, currentInner, BORDER_COLOR);
    }

    private void drawRing(GuiGraphicsExtractor graphics, int cx, int cy,
                          float innerR, float outerR, int color) {
        int outerRi = (int) outerR;
        float innerR2 = innerR * innerR;
        float outerR2 = outerR * outerR;

        for (int y = -outerRi; y <= outerRi; y++) {
            float y2 = (float) y * y;
            if (y2 > outerR2) continue;

            int outerHalf = (int) Math.sqrt(outerR2 - y2);

            if (y2 < innerR2) {
                int innerHalf = (int) Math.sqrt(innerR2 - y2);
                graphics.fill(cx - outerHalf, cy + y, cx - innerHalf, cy + y + 1, color);
                graphics.fill(cx + innerHalf + 1, cy + y, cx + outerHalf + 1, cy + y + 1, color);
            } else {
                graphics.fill(cx - outerHalf, cy + y, cx + outerHalf + 1, cy + y + 1, color);
            }
        }
    }

    private void drawArcSegmentFast(GuiGraphicsExtractor graphics, int cx, int cy,
                                     float innerR, float outerR,
                                     float startDeg, float endDeg, int color) {
        float startRad = (float) Math.toRadians(startDeg);
        float endRad = (float) Math.toRadians(endDeg);
        float outerR2 = outerR * outerR;
        float innerR2 = innerR * innerR;
        int outerRi = (int) outerR;

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int samples = 12;
        for (int s = 0; s <= samples; s++) {
            float a = startRad + (endRad - startRad) * s / samples;
            int oyy = (int) (Math.sin(a) * outerR);
            int iyy = (int) (Math.sin(a) * innerR);
            minY = Math.min(minY, Math.min(oyy, iyy));
            maxY = Math.max(maxY, Math.max(oyy, iyy));
        }
        minY = Math.max(minY, -outerRi);
        maxY = Math.min(maxY, outerRi);

        for (int y = minY; y <= maxY; y++) {
            float y2 = (float) y * y;
            if (y2 > outerR2) continue;

            int outerHalf = (int) Math.sqrt(outerR2 - y2);
            int rowMinX = Integer.MAX_VALUE;
            int rowMaxX = Integer.MIN_VALUE;

            for (int xOff = -outerHalf; xOff <= outerHalf; xOff += 2) {
                float dist2 = xOff * xOff + y2;
                if (dist2 < innerR2 || dist2 > outerR2) continue;

                float angle = (float) Math.atan2(y, xOff);
                if (isAngleInRange(angle, startRad, endRad)) {
                    if (xOff < rowMinX) rowMinX = xOff;
                    if (xOff > rowMaxX) rowMaxX = xOff;
                }
            }

            if (rowMinX <= rowMaxX) {
                graphics.fill(cx + rowMinX, cy + y, cx + rowMaxX + 2, cy + y + 1, color);
            }
        }
    }

    private boolean isAngleInRange(float angle, float start, float end) {
        float twoPi = (float) (2 * Math.PI);
        angle = ((angle % twoPi) + twoPi) % twoPi;
        start = ((start % twoPi) + twoPi) % twoPi;
        end = ((end % twoPi) + twoPi) % twoPi;

        if (start <= end) {
            return angle >= start && angle <= end;
        } else {
            return angle >= start || angle <= end;
        }
    }

    private void drawCenter(GuiGraphicsExtractor graphics, int cx, int cy, float progress) {
        float radius = INNER_RADIUS * progress * 0.9f;

        float glowR = radius + 4;
        int glowColor = (15 << 24) | (CENTER_GLOW & 0x00FFFFFF);
        drawFilledCircle(graphics, cx, cy, glowR, glowColor);

        drawFilledCircle(graphics, cx, cy, radius, 0xE0101020);
        drawCircleOutline(graphics, cx, cy, radius, CENTER_GLOW);
    }

    private void drawLabels(GuiGraphicsExtractor graphics, int cx, int cy, List<Section> sections,
                            float anglePerSection, float progress) {
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            float midAngle = (float) Math.toRadians(i * anglePerSection - 90 + anglePerSection / 2);
            float labelRadius = ICON_RADIUS * progress;

            int labelX = (int) (cx + Math.cos(midAngle) * labelRadius);
            int labelY = (int) (cy + Math.sin(midAngle) * labelRadius);

            String icon = section.getIcon();
            int iconWidth = this.font.width(icon);
            graphics.text(this.font, icon, labelX - iconWidth / 2, labelY - 10,
                    0xFFFFFFFF, true);

            String name = section.getName();
            int nameWidth = this.font.width(name);
            int textColor = i == hoveredSection ? 0xFFFFFFFF : 0xFFCCCCCC;
            graphics.text(this.font, name, labelX - nameWidth / 2, labelY + 2,
                    textColor, true);
        }
    }

    private void drawTitle(GuiGraphicsExtractor graphics, int cx, int cy, float progress) {
        if (progress < 0.5f) return;
        float titleAlpha = (progress - 0.5f) * 2.0f;
        int alpha = (int) (titleAlpha * 255) << 24;

        String title = "XENON";
        int titleWidth = this.font.width(title);
        graphics.text(this.font, title, cx - titleWidth / 2, cy - 4,
                alpha | 0x00FFFFFF, true);
    }

    private void drawRadialLine(GuiGraphicsExtractor graphics, int cx, int cy,
                                 float innerR, float outerR, float angleDeg, int color) {
        float rad = (float) Math.toRadians(angleDeg);
        float cosA = (float) Math.cos(rad);
        float sinA = (float) Math.sin(rad);
        int x1 = (int) (cx + cosA * innerR);
        int y1 = (int) (cy + sinA * innerR);
        int x2 = (int) (cx + cosA * outerR);
        int y2 = (int) (cy + sinA * outerR);

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            graphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }

    private void drawFilledCircle(GuiGraphicsExtractor graphics, int cx, int cy, float radius, int color) {
        int r = (int) radius;
        float r2 = radius * radius;
        int step = r < 20 ? 2 : 1;
        for (int y = -r; y <= r; y += step) {
            int halfWidth = (int) Math.sqrt(r2 - (long) y * y);
            graphics.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + y + step, color);
        }
    }

    private void drawCircleOutline(GuiGraphicsExtractor graphics, int cx, int cy, float radius, int color) {
        int steps = 64;
        for (int i = 0; i < steps; i++) {
            float angle = (float) (2 * Math.PI * i / steps);
            int x = (int) (cx + Math.cos(angle) * radius);
            int y = (int) (cy + Math.sin(angle) * radius);
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private int getHoveredSection(int mouseX, int mouseY, int cx, int cy,
                                   int sectionCount, float progress) {
        float dx = mouseX - cx;
        float dy = mouseY - cy;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        float currentOuter = OUTER_RADIUS * progress;
        float currentInner = INNER_RADIUS * progress;

        if (distance < currentInner || distance > currentOuter) {
            return -1;
        }

        float angle = (float) Math.toDegrees(Math.atan2(dy, dx)) + 90;
        if (angle < 0) angle += 360;

        float anglePerSection = 360.0f / sectionCount;
        return (int) (angle / anglePerSection) % sectionCount;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromSelf) {
        int button = event.button();
        double mouseX = event.x();
        double mouseY = event.y();

        if (button == 0 && hoveredSection >= 0) {
            SectionManager manager = XenonClient.getInstance().getSectionManager();
            Section section = manager.getSections().get(hoveredSection);

            if (activePanel != null && activePanel.getSection() == section) {
                activePanel = null;
            } else {
                int panelX = this.width / 2 + 10;
                int panelY = 40;
                int panelWidth = this.width / 2 - 50;
                int panelHeight = this.height - 80;
                activePanel = new SectionPanel(section, panelX, panelY, panelWidth, panelHeight);
            }
            return true;
        }

        if (activePanel != null && button == 0) {
            int cx = this.width / 2 - 140;
            int cy = this.height / 2;
            float dx = (float) mouseX - cx;
            float dy = (float) mouseY - cy;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > OUTER_RADIUS * easeOutBack(animationProgress)) {
                if (!activePanel.isMouseOver((int) mouseX, (int) mouseY)) {
                    activePanel = null;
                    return true;
                }
            }
        }

        return super.mouseClicked(event, fromSelf);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    private int brightenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * factor));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * factor));
        int b = Math.min(255, (int) ((color & 0xFF) * factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
