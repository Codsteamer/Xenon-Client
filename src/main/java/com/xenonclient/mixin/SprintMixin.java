package com.xenonclient.mixin;

import com.xenonclient.XenonClient;
import com.xenonclient.module.movement.SprintModule;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for auto-sprint. Injects at the end of LocalPlayer.aiStep()
 * to set sprinting when the player is moving forward.
 * Fully legit - equivalent to holding the sprint key.
 */
@Mixin(LocalPlayer.class)
public abstract class SprintMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void onAiStep(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;

        SprintModule sprint = XenonClient.getInstance().getModuleManager().getModule(SprintModule.class);
        if (sprint == null || !sprint.isEnabled()) return;

        if (self.input.hasForwardImpulse()
                && !self.isSprinting()
                && !self.isUsingItem()
                && !self.isPassenger()
                && self.canSprint()) {
            self.setSprinting(true);
        }
    }
}
