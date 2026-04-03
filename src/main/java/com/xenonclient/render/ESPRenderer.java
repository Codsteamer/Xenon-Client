package com.xenonclient.render;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles ESP rendering for blocks, storage containers, and players.
 * Uses cached, incremental block scanning for Block ESP to eliminate per-frame lag.
 */
public class ESPRenderer {

    private static final float ESP_LINE_WIDTH = 2.0f;

    // Pre-allocated normal vectors to avoid per-frame GC pressure
    private static final Vector3f NORMAL_POS_X = new Vector3f(1, 0, 0);
    private static final Vector3f NORMAL_NEG_X = new Vector3f(-1, 0, 0);
    private static final Vector3f NORMAL_POS_Y = new Vector3f(0, 1, 0);
    private static final Vector3f NORMAL_POS_Z = new Vector3f(0, 0, 1);
    private static final Vector3f NORMAL_NEG_Z = new Vector3f(0, 0, -1);

    /**
     * Custom no-depth-test RenderPipeline for ESP lines that render through walls.
     */
    private static final RenderPipeline ESP_LINES_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation("xenon_esp_lines")
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .withCull(false)
                    .build()
    );

    /**
     * Custom RenderType using the no-depth ESP pipeline.
     */
    private static final RenderType ESP_LINES = RenderType.create(
            "xenon_esp_lines",
            RenderSetup.builder(ESP_LINES_PIPELINE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup()
    );

    // --- Block ESP Cache ---
    private static final CopyOnWriteArrayList<BlockPos> cachedBlockPositions = new CopyOnWriteArrayList<>();
    private static BlockPos lastScanPos = null;
    private static int lastScanRange = 0;
    private static int lastTrackedHash = 0;
    // Incremental scan state: spread the scan across multiple frames
    private static int scanSliceIndex = 0;
    private static int scanTotalSlices = 0;
    private static List<BlockPos> pendingScanResults = null;
    // Number of frames to spread a full scan across
    private static final int SCAN_SPREAD_FRAMES = 5;

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

        VertexConsumer espConsumer = bufferSource.getBuffer(ESP_LINES);

        renderBlockESP(mc, poseStack, espConsumer, camX, camY, camZ);
        renderStorageESP(mc, poseStack, espConsumer, camX, camY, camZ, levelState);
        renderPlayerESP(poseStack, espConsumer, camX, camY, camZ, levelState);
    }

    /**
     * Block ESP with cached, incremental scanning.
     * Instead of scanning (2*range+1)^3 blocks every frame, we:
     * 1. Spread the scan across SCAN_SPREAD_FRAMES frames (each frame scans a few X-slices)
     * 2. Cache found positions and render from cache every frame
     * 3. Trigger a new scan when player moves or config changes
     */
    private static void renderBlockESP(Minecraft mc, PoseStack poseStack,
                                        VertexConsumer lineConsumer,
                                        double camX, double camY, double camZ) {
        BlockESPModule blockESP = XenonClient.getInstance().getModuleManager().getModule(BlockESPModule.class);
        if (blockESP == null || !blockESP.isEnabled()) {
            cachedBlockPositions.clear();
            pendingScanResults = null;
            return;
        }

        int range = blockESP.getRange();
        BlockPos playerPos = mc.player.blockPosition();
        int trackedHash = blockESP.getTrackedBlocks().hashCode();

        // Detect if we need a fresh scan
        boolean playerMoved = lastScanPos == null || !lastScanPos.equals(playerPos);
        boolean configChanged = trackedHash != lastTrackedHash || range != lastScanRange;

        if (playerMoved || configChanged) {
            // Start a new incremental scan
            lastScanPos = playerPos.immutable();
            lastScanRange = range;
            lastTrackedHash = trackedHash;

            scanTotalSlices = 2 * range + 1; // one slice per X-layer
            scanSliceIndex = 0;
            pendingScanResults = new ArrayList<>();
        }

        // Process scan slices incrementally
        if (pendingScanResults != null && scanSliceIndex < scanTotalSlices) {
            int slicesPerFrame = Math.max(1, scanTotalSlices / SCAN_SPREAD_FRAMES);
            int endSlice = Math.min(scanSliceIndex + slicesPerFrame, scanTotalSlices);

            for (int slice = scanSliceIndex; slice < endSlice; slice++) {
                int x = slice - lastScanRange;
                for (int y = -lastScanRange; y <= lastScanRange; y++) {
                    for (int z = -lastScanRange; z <= lastScanRange; z++) {
                        BlockPos pos = lastScanPos.offset(x, y, z);
                        BlockState state = mc.level.getBlockState(pos);
                        if (blockESP.isTracked(state.getBlock())) {
                            pendingScanResults.add(pos.immutable());
                        }
                    }
                }
            }

            scanSliceIndex = endSlice;

            // Scan complete - swap in results atomically
            if (scanSliceIndex >= scanTotalSlices) {
                cachedBlockPositions.clear();
                cachedBlockPositions.addAll(pendingScanResults);
                pendingScanResults = null;
            }
        }

        // Render from cache every frame (fast - just iterate cached positions)
        if (cachedBlockPositions.isEmpty()) return;

        int packed = ensureAlpha(blockESP.getEspColor());

        for (BlockPos pos : cachedBlockPositions) {
            BlockState state = mc.level.getBlockState(pos);
            if (state.isAir()) continue; // Block was broken since last scan

            VoxelShape shape = state.getShape(mc.level, pos);
            if (!shape.isEmpty()) {
                poseStack.pushPose();
                ShapeRenderer.renderShape(poseStack, lineConsumer, shape,
                        pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ,
                        packed, ESP_LINE_WIDTH);
                poseStack.popPose();
            }
        }
    }

    private static void renderStorageESP(Minecraft mc, PoseStack poseStack,
                                          VertexConsumer lineConsumer,
                                          double camX, double camY, double camZ,
                                          LevelRenderState levelState) {
        StorageESPModule storageESP = XenonClient.getInstance().getModuleManager().getModule(StorageESPModule.class);
        if (storageESP == null || !storageESP.isEnabled()) return;

        int rangeSq = storageESP.getRange() * storageESP.getRange();

        for (var beState : levelState.blockEntityRenderStates) {
            BlockPos pos = beState.blockPos;
            if (pos == null) continue;

            double dx = pos.getX() + 0.5 - mc.player.getX();
            double dy = pos.getY() + 0.5 - mc.player.getY();
            double dz = pos.getZ() + 0.5 - mc.player.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > rangeSq) continue;

            int color = getStorageColor(beState.blockEntityType, storageESP);
            if (color == 0) continue;

            int packed = ensureAlpha(color);

            BlockState state = mc.level.getBlockState(pos);
            VoxelShape shape = state.getShape(mc.level, pos);
            if (!shape.isEmpty()) {
                poseStack.pushPose();
                ShapeRenderer.renderShape(poseStack, lineConsumer, shape,
                        pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ,
                        packed, ESP_LINE_WIDTH);
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

    private static void renderPlayerESP(PoseStack poseStack, VertexConsumer lineConsumer,
                                         double camX, double camY, double camZ,
                                         LevelRenderState levelState) {
        PlayerESPModule playerESP = XenonClient.getInstance().getModuleManager().getModule(PlayerESPModule.class);
        if (playerESP == null || !playerESP.isEnabled()) return;

        int rangeSq = playerESP.getRange() * playerESP.getRange();

        for (EntityRenderState entityState : levelState.entityRenderStates) {
            if (entityState.entityType != EntityType.PLAYER) continue;

            double distSq = entityState.distanceToCameraSq;
            if (distSq > rangeSq) continue;

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

        // Bottom face - reuse pre-allocated normals
        line(consumer, pose, fx1, fy1, fz1, fx2, fy1, fz1, r, g, b, a, NORMAL_POS_X);
        line(consumer, pose, fx2, fy1, fz1, fx2, fy1, fz2, r, g, b, a, NORMAL_POS_Z);
        line(consumer, pose, fx2, fy1, fz2, fx1, fy1, fz2, r, g, b, a, NORMAL_NEG_X);
        line(consumer, pose, fx1, fy1, fz2, fx1, fy1, fz1, r, g, b, a, NORMAL_NEG_Z);

        // Top face
        line(consumer, pose, fx1, fy2, fz1, fx2, fy2, fz1, r, g, b, a, NORMAL_POS_X);
        line(consumer, pose, fx2, fy2, fz1, fx2, fy2, fz2, r, g, b, a, NORMAL_POS_Z);
        line(consumer, pose, fx2, fy2, fz2, fx1, fy2, fz2, r, g, b, a, NORMAL_NEG_X);
        line(consumer, pose, fx1, fy2, fz2, fx1, fy2, fz1, r, g, b, a, NORMAL_NEG_Z);

        // Vertical edges
        line(consumer, pose, fx1, fy1, fz1, fx1, fy2, fz1, r, g, b, a, NORMAL_POS_Y);
        line(consumer, pose, fx2, fy1, fz1, fx2, fy2, fz1, r, g, b, a, NORMAL_POS_Y);
        line(consumer, pose, fx2, fy1, fz2, fx2, fy2, fz2, r, g, b, a, NORMAL_POS_Y);
        line(consumer, pose, fx1, fy1, fz2, fx1, fy2, fz2, r, g, b, a, NORMAL_POS_Y);
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float r, float g, float b, float a,
                              Vector3f normal) {
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, normal).setLineWidth(ESP_LINE_WIDTH);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, normal).setLineWidth(ESP_LINE_WIDTH);
    }

    /**
     * Ensures color has full alpha if alpha channel is zero.
     */
    private static int ensureAlpha(int color) {
        if ((color & 0xFF000000) == 0) {
            return color | 0xFF000000;
        }
        return color;
    }
}
