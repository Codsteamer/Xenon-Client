package com.xenonclient.mixin;

import com.xenonclient.render.TracerRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into LevelRenderer to emit tracer gizmos during level extraction.
 * extractLevel is called each frame to collect render state including gizmos.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "extractLevel", at = @At("RETURN"))
    private void onExtractLevel(DeltaTracker deltaTracker, Camera camera, float partialTick, CallbackInfo ci) {
        TracerRenderer.emitTracers(partialTick);
    }
}
