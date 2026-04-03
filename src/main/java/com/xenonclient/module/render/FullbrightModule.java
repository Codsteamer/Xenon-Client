package com.xenonclient.module.render;

import com.xenonclient.module.Module;

/**
 * Fullbright module - makes everything fully lit.
 * Uses a mixin on the lightmap render state to override brightness.
 */
public class FullbrightModule extends Module {

    public FullbrightModule() {
        super("Fullbright", "Makes everything fully bright", "Render");
    }
}
