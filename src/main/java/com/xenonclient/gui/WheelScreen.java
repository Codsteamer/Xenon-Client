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
 * The main radial wheel screen for Xenon Client.
 * Displays sections as slices of a circle that the user can hover and click.
 */
public class WheelScreen extends Screen {

    private static final float OUTER_RADIUS = 120.0f;
    private static final float INNER_RADIUS = 40.0f;
    private static final float ICON_RADIUS = 80.0f;
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
        int count = sections.size();
        float currentOuter = OUTER_RADIUS * progress;
        float currentInner = INNER_RADIUS * progress;

        for (int i = 0; i < count; i++) {
            Section section = sections.get(i);
            float startAngle = i * anglePerSection - 90;
            int color = section.getColor();

            int baseColor = darkenColor(color, 0.6f);
            if (i == hoveredSection) {
                baseColor = brightenColor(color, 1.2f);
            }

            drawArcSegment(graphics, cx, cy, currentInner, currentOuter,
                    startAngle, startAngle + anglePerSection, baseColor);

            drawRadialLine(graphics, cx, cy, currentInner, currentOuter, startAngle, BORDER_COLOR);
        }

        drawCircleOutline(graphics, cx, cy, currentOuter, BORDER_COLOR);
        drawCircleOutline(graphics, cx, cy, currentInner, BORDER_COLOR);
    }

    private void drawCenter(GuiGraphicsExtractor graphics, int cx, int cy, float progress) {
        float radius = INNER_RADIUS * progress * 0.9f;

        for (int ring = 3; ring >= 0; ring--) {
            float r = radius + ring * 3;
            int alpha = 20 - ring * 5;
            int glowColor = (alpha << 24) | (CENTER_GLOW & 0x00FFFFFF);
            drawFilledCircle(graphics, cx, cy, r, glowColor);
        }

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

    private void drawArcSegment(GuiGraphicsExtractor graphics, int cx, int cy,
                                 float innerR, float outerR,
                                 float startDeg, float endDeg, int color) {
        int steps = 32;
        float stepAngle = (endDeg - startDeg) / steps;

        for (int s = 0; s < steps; s++) {
            float a1 = (float) Math.toRadians(startDeg + s * stepAngle);
            float a2 = (float) Math.toRadians(startDeg + (s + 1) * stepAngle);

            int outerX1 = (int) (cx + Math.cos(a1) * outerR);
            int outerY1 = (int) (cy + Math.sin(a1) * outerR);
            int outerX2 = (int) (cx + Math.cos(a2) * outerR);
            int outerY2 = (int) (cy + Math.sin(a2) * outerR);

            int innerX1 = (int) (cx + Math.cos(a1) * innerR);
            int innerY1 = (int) (cy + Math.sin(a1) * innerR);
            int innerX2 = (int) (cx + Math.cos(a2) * innerR);
            int innerY2 = (int) (cy + Math.sin(a2) * innerR);

            drawQuad(graphics, innerX1, innerY1, outerX1, outerY1,
                    outerX2, outerY2, innerX2, innerY2, color);
        }
    }

    private void drawQuad(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2,
                          int x3, int y3, int x4, int y4, int color) {
        int minY = Math.min(Math.min(y1, y2), Math.min(y3, y4));
        int maxY = Math.max(Math.max(y1, y2), Math.max(y3, y4));

        for (int y = minY; y <= maxY; y++) {
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;

            int[][] edges = {{x1, y1, x2, y2}, {x2, y2, x3, y3},
                    {x3, y3, x4, y4}, {x4, y4, x1, y1}};

            for (int[] edge : edges) {
                int ey1 = edge[1], ey2 = edge[3];
                if ((y >= Math.min(ey1, ey2)) && (y <= Math.max(ey1, ey2))) {
                    int ex1 = edge[0], ex2 = edge[2];
                    int x;
                    if (ey1 == ey2) {
                        minX = Math.min(minX, Math.min(ex1, ex2));
                        maxX = Math.max(maxX, Math.max(ex1, ex2));
                        continue;
                    }
                    x = ex1 + (y - ey1) * (ex2 - ex1) / (ey2 - ey1);
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                }
            }

            if (minX <= maxX) {
                graphics.fill(minX, y, maxX + 1, y + 1, color);
            }
        }
    }

    private void drawRadialLine(GuiGraphicsExtractor graphics, int cx, int cy,
                                 float innerR, float outerR, float angleDeg, int color) {
        float rad = (float) Math.toRadians(angleDeg);
        int x1 = (int) (cx + Math.cos(rad) * innerR);
        int y1 = (int) (cy + Math.sin(rad) * innerR);
        int x2 = (int) (cx + Math.cos(rad) * outerR);
        int y2 = (int) (cy + Math.sin(rad) * outerR);
        drawLine(graphics, x1, y1, x2, y2, color);
    }

    private void drawLine(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color) {
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
        for (int y = -r; y <= r; y++) {
            int halfWidth = (int) Math.sqrt(radius * radius - y * y);
            graphics.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + y + 1, color);
        }
    }

    private void drawCircleOutline(GuiGraphicsExtractor graphics, int cx, int cy, float radius, int color) {
        int steps = 128;
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

        // Forward click to active panel first (for module toggles, color wheel, etc.)
        if (activePanel != null && button == 0) {
            if (activePanel.mouseClicked((int) mouseX, (int) mouseY)) {
                return true;
            }
        }

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
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (activePanel != null && event.button() == 0) {
            if (activePanel.mouseDragged((int) event.x(), (int) event.y())) {
                return true;
            }
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (activePanel != null) {
            activePanel.mouseReleased();
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (activePanel != null && activePanel.isMouseOver((int) mouseX, (int) mouseY)) {
            activePanel.scroll((int) -verticalAmount);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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

    private int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int brightenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * factor));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * factor));
        int b = Math.min(255, (int) ((color & 0xFF) * factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
