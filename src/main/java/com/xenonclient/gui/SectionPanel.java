package com.xenonclient.gui;

import com.xenonclient.XenonClient;
import com.xenonclient.module.Module;
import com.xenonclient.module.setting.BooleanSetting;
import com.xenonclient.module.setting.ColorSetting;
import com.xenonclient.module.setting.Setting;
import com.xenonclient.section.Section;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * A panel that slides open when a section is selected from the wheel.
 * Displays section details, module toggles, settings, and a color wheel
 * for modules with color configuration.
 */
public class SectionPanel {

    private static final int PANEL_BG = 0xE0101020;
    private static final int PANEL_BORDER = 0xFF333355;
    private static final int HEADER_HEIGHT = 40;
    private static final int ITEM_HEIGHT = 32;
    private static final int PADDING = 12;
    private static final int TOGGLE_SIZE = 10;
    private static final int COLOR_PREVIEW_SIZE = 10;

    private final Section section;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private float slideProgress = 0.0f;
    private int scrollOffset = 0;

    private final ColorWheelWidget colorWheel;
    private Module colorWheelModule;

    public SectionPanel(Section section, int x, int y, int width, int height) {
        this.section = section;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.colorWheel = new ColorWheelWidget(x + width + 10, y + 40, 50);
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
        graphics.fill(animatedX + 3, y + 3, animatedX + width + 3, y + height + 3, (shadowAlpha << 24));
        graphics.fill(animatedX, y, animatedX + width, y + height, bgColor);
        int borderAlpha = (int) (alpha * 255);
        int borderColor = (borderAlpha << 24) | (PANEL_BORDER & 0x00FFFFFF);
        drawBorder(graphics, animatedX, y, width, height, borderColor);
        int accentColor = (borderAlpha << 24) | (section.getColor() & 0x00FFFFFF);
        graphics.fill(animatedX, y, animatedX + width, y + 3, accentColor);
        drawHeader(graphics, animatedX, y, alpha, font);
        drawModules(graphics, animatedX, y + HEADER_HEIGHT, alpha, font, mouseX, mouseY);
        if (colorWheel.isVisible()) {
            colorWheel.render(graphics, mouseX, mouseY, alpha, font);
        }
    }

    private void drawHeader(GuiGraphicsExtractor graphics, int panelX, int panelY, float alpha, Font font) {
        int textAlpha = (int) (alpha * 255);
        int textColor = (textAlpha << 24) | 0x00FFFFFF;
        String header = section.getIcon() + " " + section.getName();
        graphics.text(font, header, panelX + PADDING, panelY + PADDING, textColor, true);
        int sepColor = (int) (alpha * 100) << 24 | 0x00FFFFFF;
        graphics.fill(panelX + PADDING, panelY + HEADER_HEIGHT - 2,
                panelX + width - PADDING, panelY + HEADER_HEIGHT - 1, sepColor);
    }

    private void drawModules(GuiGraphicsExtractor graphics, int panelX, int contentY, float alpha, Font font,
                             int mouseX, int mouseY) {
        int textAlpha = (int) (alpha * 200);
        int descColor = (textAlpha << 24) | 0x00AAAAAA;
        graphics.text(font, section.getDescription(),
                panelX + PADDING, contentY + PADDING, descColor, false);
        String status = section.isEnabled() ? "\u25CF Enabled" : "\u25CB Disabled";
        int statusColor = section.isEnabled()
                ? ((textAlpha << 24) | 0x002ECC71)
                : ((textAlpha << 24) | 0x00E74C3C);
        graphics.text(font, status,
                panelX + PADDING, contentY + PADDING + 16, statusColor, false);
        int modulesHeaderY = contentY + PADDING + 40;
        graphics.text(font, "Modules",
                panelX + PADDING, modulesHeaderY, ((int) (alpha * 255) << 24) | 0x00CCCCCC, true);
        List<Module> modules = XenonClient.getInstance().getModuleManager()
                .getModulesBySection(section.getName());
        if (modules.isEmpty()) {
            int dimColor = (textAlpha << 24) | 0x00777777;
            graphics.text(font, "No modules yet",
                    panelX + PADDING + 8, modulesHeaderY + 18, dimColor, false);
            return;
        }
        int moduleY = modulesHeaderY + 16 - scrollOffset;
        for (Module module : modules) {
            if (moduleY + getModuleHeight(module) > contentY + height - HEADER_HEIGHT) break;
            if (moduleY >= modulesHeaderY + 14) {
                drawModule(graphics, panelX, moduleY, alpha, font, module, mouseX, mouseY);
            }
            moduleY += getModuleHeight(module) + 4;
        }
    }

    private int getModuleHeight(Module module) {
        int h = ITEM_HEIGHT;
        if (module.isEnabled()) {
            for (Setting<?> setting : module.getSettings()) {
                h += 18;
            }
        }
        return h;
    }

    private void drawModule(GuiGraphicsExtractor graphics, int panelX, int slotY, float alpha, Font font,
                            Module module, int mouseX, int mouseY) {
        int textAlpha = (int) (alpha * 255);
        int moduleHeight = getModuleHeight(module);
        int slotBg = ((int) (alpha * 40) << 24) | 0x00FFFFFF;
        boolean hovered = mouseX >= panelX + PADDING && mouseX <= panelX + width - PADDING
                && mouseY >= slotY && mouseY <= slotY + moduleHeight;
        if (hovered) {
            slotBg = ((int) (alpha * 60) << 24) | 0x00FFFFFF;
        }
        graphics.fill(panelX + PADDING, slotY,
                panelX + width - PADDING, slotY + moduleHeight, slotBg);
        int slotBorder = ((int) (alpha * 60) << 24) | 0x00FFFFFF;
        drawBorder(graphics, panelX + PADDING, slotY,
                width - PADDING * 2, moduleHeight, slotBorder);
        int nameColor = module.isEnabled()
                ? (textAlpha << 24) | 0x00FFFFFF
                : (textAlpha << 24) | 0x00888888;
        graphics.text(font, module.getName(), panelX + PADDING + 8, slotY + 8, nameColor, false);
        int toggleX = panelX + width - PADDING - TOGGLE_SIZE - 8;
        int toggleY = slotY + 8;
        int toggleBg = module.isEnabled()
                ? ((textAlpha << 24) | 0x002ECC71)
                : ((textAlpha << 24) | 0x00555555);
        graphics.fill(toggleX, toggleY, toggleX + TOGGLE_SIZE, toggleY + TOGGLE_SIZE, toggleBg);
        int toggleBorder = (textAlpha << 24) | 0x00888888;
        drawBorder(graphics, toggleX, toggleY, TOGGLE_SIZE, TOGGLE_SIZE, toggleBorder);
        if (module.hasColorConfig()) {
            ColorSetting cs = module.getColorSetting();
            int colorPreviewX = toggleX - COLOR_PREVIEW_SIZE - 8;
            int colorPreviewY = slotY + 8;
            int previewColor = (textAlpha << 24) | (cs.getColor() & 0x00FFFFFF);
            graphics.fill(colorPreviewX, colorPreviewY,
                    colorPreviewX + COLOR_PREVIEW_SIZE, colorPreviewY + COLOR_PREVIEW_SIZE, previewColor);
            int previewBorder = (textAlpha << 24) | 0x00AAAAAA;
            drawBorder(graphics, colorPreviewX, colorPreviewY, COLOR_PREVIEW_SIZE, COLOR_PREVIEW_SIZE, previewBorder);
        }
        if (module.isEnabled()) {
            int settingY = slotY + ITEM_HEIGHT - 4;
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof BooleanSetting boolSetting) {
                    drawBooleanSetting(graphics, panelX, settingY, alpha, font, boolSetting);
                } else if (setting instanceof ColorSetting colorSetting) {
                    drawColorSettingRow(graphics, panelX, settingY, alpha, font, colorSetting);
                }
                settingY += 18;
            }
        }
    }

    private void drawBooleanSetting(GuiGraphicsExtractor graphics, int panelX, int settingY, float alpha,
                                    Font font, BooleanSetting setting) {
        int textAlpha = (int) (alpha * 200);
        int labelColor = (textAlpha << 24) | 0x00BBBBBB;
        graphics.text(font, "  " + setting.getName(), panelX + PADDING + 12, settingY + 2, labelColor, false);
        int toggleX = panelX + width - PADDING - 20;
        int toggleBg = setting.isEnabled()
                ? ((textAlpha << 24) | 0x002ECC71)
                : ((textAlpha << 24) | 0x00555555);
        graphics.fill(toggleX, settingY + 1, toggleX + 8, settingY + 9, toggleBg);
        int toggleBorder = ((int) (alpha * 150) << 24) | 0x00888888;
        drawBorder(graphics, toggleX, settingY + 1, 8, 8, toggleBorder);
        String stateText = setting.isEnabled() ? "ON" : "OFF";
        int stateColor = setting.isEnabled()
                ? ((textAlpha << 24) | 0x002ECC71)
                : ((textAlpha << 24) | 0x00888888);
        graphics.text(font, stateText, toggleX + 12, settingY + 2, stateColor, false);
    }

    private void drawColorSettingRow(GuiGraphicsExtractor graphics, int panelX, int settingY, float alpha,
                                     Font font, ColorSetting colorSetting) {
        int textAlpha = (int) (alpha * 200);
        int labelColor = (textAlpha << 24) | 0x00BBBBBB;
        graphics.text(font, "  " + colorSetting.getName(), panelX + PADDING + 12, settingY + 2, labelColor, false);
        int swatchX = panelX + width - PADDING - 28;
        int swatchColor = (textAlpha << 24) | (colorSetting.getColor() & 0x00FFFFFF);
        graphics.fill(swatchX, settingY + 1, swatchX + 12, settingY + 9, swatchColor);
        int swatchBorder = ((int) (alpha * 150) << 24) | 0x00AAAAAA;
        drawBorder(graphics, swatchX, settingY + 1, 12, 8, swatchBorder);
        String hex = String.format("#%06X", colorSetting.getColor() & 0x00FFFFFF);
        int hexColor = (textAlpha << 24) | 0x00999999;
        graphics.text(font, hex, swatchX - font.width(hex) - 4, settingY + 2, hexColor, false);
        if (colorWheel.isVisible() && colorWheel.getColorSetting() == colorSetting) {
            int indicatorColor = ((int) (alpha * 255) << 24) | 0x006C3CE0;
            graphics.fill(swatchX + 14, settingY + 1, swatchX + 16, settingY + 9, indicatorColor);
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY) {
        if (colorWheel.isVisible()) {
            if (colorWheel.mouseClicked(mouseX, mouseY)) {
                return true;
            }
            if (!colorWheel.isInBounds(mouseX, mouseY)) {
                colorWheel.setVisible(false);
                colorWheelModule = null;
            }
        }
        if (!isMouseOver(mouseX, mouseY)) return false;
        List<Module> modules = XenonClient.getInstance().getModuleManager()
                .getModulesBySection(section.getName());
        int contentY = y + HEADER_HEIGHT;
        int modulesHeaderY = contentY + PADDING + 40;
        int moduleY = modulesHeaderY + 16 - scrollOffset;
        for (Module module : modules) {
            int moduleHeight = getModuleHeight(module);
            if (moduleY + moduleHeight > contentY + height - HEADER_HEIGHT) break;
            if (mouseY >= moduleY && mouseY < moduleY + moduleHeight
                    && mouseX >= x + PADDING && mouseX <= x + width - PADDING) {
                int toggleX = x + width - PADDING - TOGGLE_SIZE - 8;
                int toggleY = moduleY + 8;
                if (mouseX >= toggleX && mouseX <= toggleX + TOGGLE_SIZE
                        && mouseY >= toggleY && mouseY <= toggleY + TOGGLE_SIZE) {
                    module.toggle();
                    return true;
                }
                if (module.hasColorConfig()) {
                    ColorSetting cs = module.getColorSetting();
                    int colorPreviewX = toggleX - COLOR_PREVIEW_SIZE - 8;
                    int colorPreviewY = moduleY + 8;
                    if (mouseX >= colorPreviewX && mouseX <= colorPreviewX + COLOR_PREVIEW_SIZE
                            && mouseY >= colorPreviewY && mouseY <= colorPreviewY + COLOR_PREVIEW_SIZE) {
                        toggleColorWheel(module, cs);
                        return true;
                    }
                }
                if (module.isEnabled()) {
                    int settingY = moduleY + ITEM_HEIGHT - 4;
                    for (Setting<?> setting : module.getSettings()) {
                        if (setting instanceof BooleanSetting boolSetting) {
                            int boolToggleX = x + width - PADDING - 20;
                            if (mouseX >= boolToggleX && mouseX <= boolToggleX + 8
                                    && mouseY >= settingY + 1 && mouseY <= settingY + 9) {
                                boolSetting.toggle();
                                return true;
                            }
                        } else if (setting instanceof ColorSetting colorSetting) {
                            int swatchX = x + width - PADDING - 28;
                            if (mouseX >= swatchX && mouseX <= swatchX + 12
                                    && mouseY >= settingY + 1 && mouseY <= settingY + 9) {
                                toggleColorWheel(module, colorSetting);
                                return true;
                            }
                        }
                        settingY += 18;
                    }
                }
                return true;
            }
            moduleY += moduleHeight + 4;
        }
        return false;
    }

    private void toggleColorWheel(Module module, ColorSetting colorSetting) {
        if (colorWheel.isVisible() && colorWheel.getColorSetting() == colorSetting) {
            colorWheel.setVisible(false);
            colorWheelModule = null;
        } else {
            colorWheel.setColorSetting(colorSetting);
            colorWheel.setVisible(true);
            colorWheelModule = module;
        }
    }

    public boolean mouseDragged(int mouseX, int mouseY) {
        if (colorWheel.isVisible()) {
            return colorWheel.mouseDragged(mouseX, mouseY);
        }
        return false;
    }

    public void mouseReleased() {
        colorWheel.mouseReleased();
    }

    public void scroll(int amount) {
        scrollOffset = Math.max(0, scrollOffset + amount * 8);
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
