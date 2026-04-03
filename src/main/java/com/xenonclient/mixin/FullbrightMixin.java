package com.xenonclient.mixin;

import com.xenonclient.XenonClient;
import com.xenonclient.module.render.FullbrightModule;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for fullbright. Injects after LightmapRenderStateExtractor.extract()
 * to override the lightmap render state with full brightness.
 */
@Mixin(LightmapRenderStateExtractor.class)
public class FullbrightMixin {

    @Inject(method = "extract", at = @At("TAIL"))
    private void onExtractLightmap(LightmapRenderState state, float delta, CallbackInfo ci) {
        FullbrightModule fullbright = XenonClient.getInstance().getModuleManager().getModule(FullbrightModule.class);
        if (fullbright == null || !fullbright.isEnabled()) return;

        state.nightVisionEffectIntensity = 1.0f;
    }
}
