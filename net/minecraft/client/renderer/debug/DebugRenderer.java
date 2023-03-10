/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.client.renderer.debug.ChunkDebugRenderer;
import net.minecraft.client.renderer.debug.CollisionBoxRenderer;
import net.minecraft.client.renderer.debug.GameEventListenerRenderer;
import net.minecraft.client.renderer.debug.GameTestDebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.HeightMapRenderer;
import net.minecraft.client.renderer.debug.LightDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.client.renderer.debug.RaidDebugRenderer;
import net.minecraft.client.renderer.debug.SolidFaceRenderer;
import net.minecraft.client.renderer.debug.StructureRenderer;
import net.minecraft.client.renderer.debug.VillageSectionsDebugRenderer;
import net.minecraft.client.renderer.debug.WaterDebugRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class DebugRenderer {
    public final PathfindingRenderer pathfindingRenderer = new PathfindingRenderer();
    public final SimpleDebugRenderer waterDebugRenderer;
    public final SimpleDebugRenderer chunkBorderRenderer;
    public final SimpleDebugRenderer heightMapRenderer;
    public final SimpleDebugRenderer collisionBoxRenderer;
    public final SimpleDebugRenderer neighborsUpdateRenderer;
    public final StructureRenderer structureRenderer;
    public final SimpleDebugRenderer lightDebugRenderer;
    public final SimpleDebugRenderer worldGenAttemptRenderer;
    public final SimpleDebugRenderer solidFaceRenderer;
    public final SimpleDebugRenderer chunkRenderer;
    public final BrainDebugRenderer brainDebugRenderer;
    public final VillageSectionsDebugRenderer villageSectionsDebugRenderer;
    public final BeeDebugRenderer beeDebugRenderer;
    public final RaidDebugRenderer raidDebugRenderer;
    public final GoalSelectorDebugRenderer goalSelectorRenderer;
    public final GameTestDebugRenderer gameTestDebugRenderer;
    public final GameEventListenerRenderer gameEventListenerRenderer;
    private boolean renderChunkborder;

    public DebugRenderer(Minecraft minecraft) {
        this.waterDebugRenderer = new WaterDebugRenderer(minecraft);
        this.chunkBorderRenderer = new ChunkBorderRenderer(minecraft);
        this.heightMapRenderer = new HeightMapRenderer(minecraft);
        this.collisionBoxRenderer = new CollisionBoxRenderer(minecraft);
        this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(minecraft);
        this.structureRenderer = new StructureRenderer(minecraft);
        this.lightDebugRenderer = new LightDebugRenderer(minecraft);
        this.worldGenAttemptRenderer = new WorldGenAttemptRenderer();
        this.solidFaceRenderer = new SolidFaceRenderer(minecraft);
        this.chunkRenderer = new ChunkDebugRenderer(minecraft);
        this.brainDebugRenderer = new BrainDebugRenderer(minecraft);
        this.villageSectionsDebugRenderer = new VillageSectionsDebugRenderer();
        this.beeDebugRenderer = new BeeDebugRenderer(minecraft);
        this.raidDebugRenderer = new RaidDebugRenderer(minecraft);
        this.goalSelectorRenderer = new GoalSelectorDebugRenderer(minecraft);
        this.gameTestDebugRenderer = new GameTestDebugRenderer();
        this.gameEventListenerRenderer = new GameEventListenerRenderer(minecraft);
    }

    public void clear() {
        this.pathfindingRenderer.clear();
        this.waterDebugRenderer.clear();
        this.chunkBorderRenderer.clear();
        this.heightMapRenderer.clear();
        this.collisionBoxRenderer.clear();
        this.neighborsUpdateRenderer.clear();
        this.structureRenderer.clear();
        this.lightDebugRenderer.clear();
        this.worldGenAttemptRenderer.clear();
        this.solidFaceRenderer.clear();
        this.chunkRenderer.clear();
        this.brainDebugRenderer.clear();
        this.villageSectionsDebugRenderer.clear();
        this.beeDebugRenderer.clear();
        this.raidDebugRenderer.clear();
        this.goalSelectorRenderer.clear();
        this.gameTestDebugRenderer.clear();
        this.gameEventListenerRenderer.clear();
    }

    public boolean switchRenderChunkborder() {
        this.renderChunkborder = !this.renderChunkborder;
        return this.renderChunkborder;
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double d, double e, double f) {
        if (this.renderChunkborder && !Minecraft.getInstance().showOnlyReducedInfo()) {
            this.chunkBorderRenderer.render(poseStack, bufferSource, d, e, f);
        }
        this.gameTestDebugRenderer.render(poseStack, bufferSource, d, e, f);
    }

    public static Optional<Entity> getTargetedEntity(@Nullable Entity entity2, int i) {
        int j;
        Predicate<Entity> predicate;
        AABB aABB;
        Vec3 vec32;
        Vec3 vec33;
        if (entity2 == null) {
            return Optional.empty();
        }
        Vec3 vec3 = entity2.getEyePosition();
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity2, vec3, vec33 = vec3.add(vec32 = entity2.getViewVector(1.0f).scale(i)), aABB = entity2.getBoundingBox().expandTowards(vec32).inflate(1.0), predicate = entity -> !entity.isSpectator() && entity.isPickable(), j = i * i);
        if (entityHitResult == null) {
            return Optional.empty();
        }
        if (vec3.distanceToSqr(entityHitResult.getLocation()) > (double)j) {
            return Optional.empty();
        }
        return Optional.of(entityHitResult.getEntity());
    }

    public static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, BlockPos blockPos2, float f, float g, float h, float i) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 vec3 = camera.getPosition().reverse();
        AABB aABB = new AABB(blockPos, blockPos2).move(vec3);
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, aABB, f, g, h, i);
    }

    public static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, float f, float g, float h, float i, float j) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 vec3 = camera.getPosition().reverse();
        AABB aABB = new AABB(blockPos).move(vec3).inflate(f);
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, aABB, g, h, i, j);
    }

    public static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, AABB aABB, float f, float g, float h, float i) {
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, g, h, i);
    }

    public static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
        LevelRenderer.addChainedFilledBoxVertices(poseStack, vertexConsumer, d, e, f, g, h, i, j, k, l, m);
    }

    public static void renderFloatingText(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, int i, int j, int k, int l) {
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, (double)i + 0.5, (double)j + 0.5, (double)k + 0.5, l);
    }

    public static void renderFloatingText(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, double d, double e, double f, int i) {
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, d, e, f, i, 0.02f);
    }

    public static void renderFloatingText(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, double d, double e, double f, int i, float g) {
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, d, e, f, i, g, true, 0.0f, false);
    }

    public static void renderFloatingText(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, double d, double e, double f, int i, float g, boolean bl, float h, boolean bl2) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (!camera.isInitialized() || minecraft.getEntityRenderDispatcher().options == null) {
            return;
        }
        Font font = minecraft.font;
        double j = camera.getPosition().x;
        double k = camera.getPosition().y;
        double l = camera.getPosition().z;
        poseStack.pushPose();
        poseStack.translate((float)(d - j), (float)(e - k) + 0.07f, (float)(f - l));
        poseStack.mulPoseMatrix(new Matrix4f().rotation(camera.rotation()));
        poseStack.scale(-g, -g, g);
        float m = bl ? (float)(-font.width(string)) / 2.0f : 0.0f;
        font.drawInBatch(string, m -= h / g, 0.0f, i, false, poseStack.last().pose(), multiBufferSource, bl2 ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, 0xF000F0);
        poseStack.popPose();
    }

    @Environment(value=EnvType.CLIENT)
    public static interface SimpleDebugRenderer {
        public void render(PoseStack var1, MultiBufferSource var2, double var3, double var5, double var7);

        default public void clear() {
        }
    }
}

