package com.xenonclient.render;

import com.xenonclient.XenonClient;
import com.xenonclient.module.Module;
import com.xenonclient.module.render.BlockESP;
import com.xenonclient.module.render.PlayerESP;
import com.xenonclient.module.render.StorageESP;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Renders tracer lines from the player's view to entities and blocks
 * when the corresponding ESP module's tracer option is enabled.
 * Uses the Gizmos API to draw lines in world space.
 */
public class TracerRenderer {

    private static final float LINE_WIDTH = 1.5f;

    /**
     * Called during level extraction to emit tracer gizmos.
     * Must be called while a GizmoCollector is active.
     */
    public static void emitTracers(float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.position();

        PlayerESP playerESP = getModule(PlayerESP.class);
        StorageESP storageESP = getModule(StorageESP.class);
        BlockESP blockESP = getModule(BlockESP.class);

        boolean anyActive = false;
        if (playerESP != null && playerESP.isEnabled() && playerESP.isTracerEnabled()) anyActive = true;
        if (storageESP != null && storageESP.isEnabled() && storageESP.isTracerEnabled()) anyActive = true;
        if (blockESP != null && blockESP.isEnabled() && blockESP.isTracerEnabled()) anyActive = true;

        if (!anyActive) return;

        // Player tracers
        if (playerESP != null && playerESP.isEnabled() && playerESP.isTracerEnabled()) {
            int color = playerESP.getColor().getColor();
            for (AbstractClientPlayer player : mc.level.players()) {
                if (player == mc.player) continue;
                Vec3 entityPos = player.getPosition(partialTick);
                double eyeY = entityPos.y + player.getEyeHeight(player.getPose());
                Vec3 target = new Vec3(entityPos.x, eyeY, entityPos.z);
                Gizmos.line(cameraPos, target, color, LINE_WIDTH);
            }
        }

        // Storage tracers
        if (storageESP != null && storageESP.isEnabled() && storageESP.isTracerEnabled()) {
            int color = storageESP.getColor().getColor();
            for (BlockEntity be : mc.level.getGloballyRenderedBlockEntities()) {
                if (isStorageBlockEntity(be)) {
                    Vec3 target = Vec3.atCenterOf(be.getBlockPos());
                    Gizmos.line(cameraPos, target, color, LINE_WIDTH);
                }
            }
        }

        // Block tracers
        if (blockESP != null && blockESP.isEnabled() && blockESP.isTracerEnabled()) {
            int color = blockESP.getColor().getColor();
            for (BlockEntity be : mc.level.getGloballyRenderedBlockEntities()) {
                if (!isStorageBlockEntity(be)) {
                    Vec3 target = Vec3.atCenterOf(be.getBlockPos());
                    Gizmos.line(cameraPos, target, color, LINE_WIDTH);
                }
            }
        }
    }

    private static boolean isStorageBlockEntity(BlockEntity be) {
        return be instanceof ChestBlockEntity
                || be instanceof BarrelBlockEntity
                || be instanceof ShulkerBoxBlockEntity
                || be instanceof EnderChestBlockEntity
                || be instanceof HopperBlockEntity;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : XenonClient.getInstance().getModuleManager().getModules()) {
            if (clazz.isInstance(module)) {
                return (T) module;
            }
        }
        return null;
    }
}
