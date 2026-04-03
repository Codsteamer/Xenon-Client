package com.xenonclient.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xenonclient.XenonClient;
import com.xenonclient.module.render.BlockESPModule;
import com.xenonclient.module.render.PlayerESPModule;
import com.xenonclient.module.render.StorageESPModule;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Handles ESP rendering for blocks, storage containers, and players.
 * Registers with Fabric's LevelRenderEvents to draw outlines in the world.
 */
public class ESPRenderer {

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ESPRenderer::onRenderWorld);
    }

    private static void onRenderWorld(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        LevelRenderState levelState = context.levelState();
        PoseStack poseStack = context.poseStack();
        MultiBufferSource.BufferSource bufferSource = context.bufferSource();

        double camX = levelState.cameraRenderState.pos.x;
        double camY = levelState.cameraRenderState.pos.y;
        double camZ = levelState.cameraRenderState.pos.z;

        renderBlockESP(mc, poseStack, bufferSource, camX, camY, camZ);
        renderStorageESP(mc, poseStack, bufferSource, camX, camY, camZ, levelState);
        renderPlayerESP(poseStack, bufferSource, camX, camY, camZ, levelState);
    }

    private static void renderBlockESP(Minecraft mc, PoseStack poseStack,
                                        MultiBufferSource.BufferSource bufferSource,
                                        double camX, double camY, double camZ) {
        BlockESPModule blockESP = XenonClient.getInstance().getModuleManager().getModule(BlockESPModule.class);
        if (blockESP == null || !blockESP.isEnabled()) return;

        int range = blockESP.getRange();
        BlockPos playerPos = mc.player.blockPosition();
        int color = blockESP.getEspColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;
        if (a == 0) a = 1.0f;

        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderTypes.lines());

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockState state = mc.level.getBlockState(pos);

                    if (blockESP.isTracked(state.getBlock())) {
                        VoxelShape shape = state.getShape(mc.level, pos);
                        if (!shape.isEmpty()) {
                            poseStack.pushPose();
                            int packed = packColor(r, g, b, a);
                            ShapeRenderer.renderShape(poseStack, lineConsumer, shape,
                                    pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ,
                                    packed, a);
                            poseStack.popPose();
                        }
                    }
                }
            }
        }
    }

    private static void renderStorageESP(Minecraft mc, PoseStack poseStack,
                                          MultiBufferSource.BufferSource bufferSource,
                                          double camX, double camY, double camZ,
                                          LevelRenderState levelState) {
        StorageESPModule storageESP = XenonClient.getInstance().getModuleManager().getModule(StorageESPModule.class);
        if (storageESP == null || !storageESP.isEnabled()) return;

        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderTypes.lines());

        for (var beState : levelState.blockEntityRenderStates) {
            BlockPos pos = beState.blockPos;
            if (pos == null) continue;

            double dx = pos.getX() + 0.5 - mc.player.getX();
            double dy = pos.getY() + 0.5 - mc.player.getY();
            double dz = pos.getZ() + 0.5 - mc.player.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;
            int range = storageESP.getRange();
            if (distSq > (double) range * range) continue;

            int color = getStorageColor(beState.blockEntityType, storageESP);
            if (color == 0) continue;

            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            float a = ((color >> 24) & 0xFF) / 255.0f;
            if (a == 0) a = 1.0f;

            BlockState state = mc.level.getBlockState(pos);
            VoxelShape shape = state.getShape(mc.level, pos);
            if (!shape.isEmpty()) {
                poseStack.pushPose();
                int packed = packColor(r, g, b, a);
                ShapeRenderer.renderShape(poseStack, lineConsumer, shape,
                        pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ,
                        packed, a);
                poseStack.popPose();
            }
        }
    }

    private static int getStorageColor(BlockEntityType<?> type, StorageESPModule module) {
        if (type == BlockEntityType.CHEST || type == BlockEntityType.TRAPPED_CHEST) {
            return module.isShowChests() ? module.getChestColor() : 0;
        } else if (type == BlockEntityType.ENDER_CHEST) {
            return module.isShowEnderChests() ? module.getEnderChestColor() : 0;
        } else if (type == BlockEntityType.SHULKER_BOX) {
            return module.isShowShulkerBoxes() ? module.getShulkerColor() : 0;
        } else if (type == BlockEntityType.BARREL) {
            return module.isShowBarrels() ? module.getBarrelColor() : 0;
        } else if (type == BlockEntityType.HOPPER) {
            return module.isShowHoppers() ? module.getHopperColor() : 0;
        } else if (type == BlockEntityType.DISPENSER) {
            return module.isShowDispensers() ? module.getDispenserColor() : 0;
        } else if (type == BlockEntityType.DROPPER) {
            return module.isShowDroppers() ? module.getDispenserColor() : 0;
        } else if (type == BlockEntityType.FURNACE || type == BlockEntityType.BLAST_FURNACE || type == BlockEntityType.SMOKER) {
            return module.isShowFurnaces() ? module.getFurnaceColor() : 0;
        }
        return 0;
    }

    private static void renderPlayerESP(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                         double camX, double camY, double camZ,
                                         LevelRenderState levelState) {
        PlayerESPModule playerESP = XenonClient.getInstance().getModuleManager().getModule(PlayerESPModule.class);
        if (playerESP == null || !playerESP.isEnabled()) return;

        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderTypes.lines());

        for (EntityRenderState entityState : levelState.entityRenderStates) {
            if (entityState.entityType != EntityType.PLAYER) continue;

            double distSq = entityState.distanceToCameraSq;
            int range = playerESP.getRange();
            if (distSq > (double) range * range) continue;

            int color;
            if (entityState.isDiscrete) {
                color = playerESP.getSneakingColor();
            } else {
                color = playerESP.getDefaultColor();
            }

            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;

            float halfW = entityState.boundingBoxWidth / 2.0f;
            float h = entityState.boundingBoxHeight;

            double ex = entityState.x - camX;
            double ey = entityState.y - camY;
            double ez = entityState.z - camZ;

            drawBoxOutline(poseStack, lineConsumer,
                    ex - halfW, ey, ez - halfW,
                    ex + halfW, ey + h, ez + halfW,
                    r, g, b, 1.0f);
        }
    }

    private static void drawBoxOutline(PoseStack poseStack, VertexConsumer consumer,
                                        double x1, double y1, double z1,
                                        double x2, double y2, double z2,
                                        float r, float g, float b, float a) {
        PoseStack.Pose pose = poseStack.last();

        float fx1 = (float) x1, fy1 = (float) y1, fz1 = (float) z1;
        float fx2 = (float) x2, fy2 = (float) y2, fz2 = (float) z2;

        // Bottom face
        line(consumer, pose, fx1, fy1, fz1, fx2, fy1, fz1, r, g, b, a, 1, 0, 0);
        line(consumer, pose, fx2, fy1, fz1, fx2, fy1, fz2, r, g, b, a, 0, 0, 1);
        line(consumer, pose, fx2, fy1, fz2, fx1, fy1, fz2, r, g, b, a, -1, 0, 0);
        line(consumer, pose, fx1, fy1, fz2, fx1, fy1, fz1, r, g, b, a, 0, 0, -1);

        // Top face
        line(consumer, pose, fx1, fy2, fz1, fx2, fy2, fz1, r, g, b, a, 1, 0, 0);
        line(consumer, pose, fx2, fy2, fz1, fx2, fy2, fz2, r, g, b, a, 0, 0, 1);
        line(consumer, pose, fx2, fy2, fz2, fx1, fy2, fz2, r, g, b, a, -1, 0, 0);
        line(consumer, pose, fx1, fy2, fz2, fx1, fy2, fz1, r, g, b, a, 0, 0, -1);

        // Vertical edges
        line(consumer, pose, fx1, fy1, fz1, fx1, fy2, fz1, r, g, b, a, 0, 1, 0);
        line(consumer, pose, fx2, fy1, fz1, fx2, fy2, fz1, r, g, b, a, 0, 1, 0);
        line(consumer, pose, fx2, fy1, fz2, fx2, fy2, fz2, r, g, b, a, 0, 1, 0);
        line(consumer, pose, fx1, fy1, fz2, fx1, fy2, fz2, r, g, b, a, 0, 1, 0);
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float r, float g, float b, float a,
                              float nx, float ny, float nz) {
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
    }

    private static int packColor(float r, float g, float b, float a) {
        return ((int) (a * 255) << 24) | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
}
