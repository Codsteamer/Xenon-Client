package com.xenonclient.module.setting;

/**
 * A color setting for modules, storing an ARGB integer color value.
 */
public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, String description, int defaultColor) {
        super(name, description, defaultColor);
    }

    public int getColor() {
        return this.value;
    }

    public void setColor(int color) {
        this.value = color;
    }

    public int getRed() {
        return (this.value >> 16) & 0xFF;
    }

    public int getGreen() {
        return (this.value >> 8) & 0xFF;
    }

    public int getBlue() {
        return this.value & 0xFF;
    }

    public int getAlpha() {
        return (this.value >> 24) & 0xFF;
    }

    public void setFromRGB(int r, int g, int b) {
        this.value = (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public void setFromHSB(float hue, float saturation, float brightness) {
        int rgb = hsbToRgb(hue, saturation, brightness);
        this.value = (0xFF << 24) | (rgb & 0x00FFFFFF);
    }

    /**
     * Converts HSB values to an RGB integer.
     */
    public static int hsbToRgb(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Extracts hue from an RGB color.
     */
    public static float rgbToHue(int r, int g, int b) {
        float[] hsb = rgbToHsb(r, g, b);
        return hsb[0];
    }

    /**
     * Extracts saturation from an RGB color.
     */
    public static float rgbToSaturation(int r, int g, int b) {
        float[] hsb = rgbToHsb(r, g, b);
        return hsb[1];
    }

    /**
     * Extracts brightness from an RGB color.
     */
    public static float rgbToBrightness(int r, int g, int b) {
        float[] hsb = rgbToHsb(r, g, b);
        return hsb[2];
    }

    /**
     * Converts RGB to HSB array [hue, saturation, brightness].
     */
    public static float[] rgbToHsb(int r, int g, int b) {
        float[] hsb = new float[3];
        int cmax = Math.max(r, Math.max(g, b));
        int cmin = Math.min(r, Math.min(g, b));
        float brightness = cmax / 255.0f;
        float saturation;
        if (cmax != 0) {
            saturation = (float) (cmax - cmin) / (float) cmax;
        } else {
            saturation = 0;
        }
        float hue;
        if (saturation == 0) {
            hue = 0;
        } else {
            float redc = (float) (cmax - r) / (float) (cmax - cmin);
            float greenc = (float) (cmax - g) / (float) (cmax - cmin);
            float bluec = (float) (cmax - b) / (float) (cmax - cmin);
            if (r == cmax) {
                hue = bluec - greenc;
            } else if (g == cmax) {
                hue = 2.0f + redc - bluec;
            } else {
                hue = 4.0f + greenc - redc;
            }
            hue = hue / 6.0f;
            if (hue < 0) {
                hue = hue + 1.0f;
            }
        }
        hsb[0] = hue;
        hsb[1] = saturation;
        hsb[2] = brightness;
        return hsb;
    }
}
