/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import java.util.Optional;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.CaveDebugRenderer;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.client.renderer.debug.ChunkDebugRenderer;
import net.minecraft.client.renderer.debug.CollisionBoxRenderer;
import net.minecraft.client.renderer.debug.GameTestDebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.HeightMapRenderer;
import net.minecraft.client.renderer.debug.LightDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.client.renderer.debug.RaidDebugRenderer;
import net.minecraft.client.renderer.debug.SolidFaceRenderer;
import net.minecraft.client.renderer.debug.StructureRenderer;
import net.minecraft.client.renderer.debug.VillageDebugRenderer;
import net.minecraft.client.renderer.debug.WaterDebugRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugRenderer {
    public final PathfindingRenderer pathfindingRenderer = new PathfindingRenderer();
    public final SimpleDebugRenderer waterDebugRenderer;
    public final SimpleDebugRenderer chunkBorderRenderer;
    public final SimpleDebugRenderer heightMapRenderer;
    public final SimpleDebugRenderer collisionBoxRenderer;
    public final SimpleDebugRenderer neighborsUpdateRenderer;
    public final CaveDebugRenderer caveRenderer;
    public final StructureRenderer structureRenderer;
    public final SimpleDebugRenderer lightDebugRenderer;
    public final SimpleDebugRenderer worldGenAttemptRenderer;
    public final SimpleDebugRenderer solidFaceRenderer;
    public final SimpleDebugRenderer chunkRenderer;
    public final VillageDebugRenderer villageDebugRenderer;
    public final BeeDebugRenderer beeDebugRenderer;
    public final RaidDebugRenderer raidDebugRenderer;
    public final GoalSelectorDebugRenderer goalSelectorRenderer;
    public final GameTestDebugRenderer gameTestDebugRenderer;
    private boolean renderChunkborder;

    public DebugRenderer(Minecraft minecraft) {
        this.waterDebugRenderer = new WaterDebugRenderer(minecraft);
        this.chunkBorderRenderer = new ChunkBorderRenderer(minecraft);
        this.heightMapRenderer = new HeightMapRenderer(minecraft);
        this.collisionBoxRenderer = new CollisionBoxRenderer(minecraft);
        this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(minecraft);
        this.caveRenderer = new CaveDebugRenderer();
        this.structureRenderer = new StructureRenderer(minecraft);
        this.lightDebugRenderer = new LightDebugRenderer(minecraft);
        this.worldGenAttemptRenderer = new WorldGenAttemptRenderer();
        this.solidFaceRenderer = new SolidFaceRenderer(minecraft);
        this.chunkRenderer = new ChunkDebugRenderer(minecraft);
        this.villageDebugRenderer = new VillageDebugRenderer(minecraft);
        this.beeDebugRenderer = new BeeDebugRenderer(minecraft);
        this.raidDebugRenderer = new RaidDebugRenderer(minecraft);
        this.goalSelectorRenderer = new GoalSelectorDebugRenderer(minecraft);
        this.gameTestDebugRenderer = new GameTestDebugRenderer();
    }

    public void clear() {
        this.pathfindingRenderer.clear();
        this.waterDebugRenderer.clear();
        this.chunkBorderRenderer.clear();
        this.heightMapRenderer.clear();
        this.collisionBoxRenderer.clear();
        this.neighborsUpdateRenderer.clear();
        this.caveRenderer.clear();
        this.structureRenderer.clear();
        this.lightDebugRenderer.clear();
        this.worldGenAttemptRenderer.clear();
        this.solidFaceRenderer.clear();
        this.chunkRenderer.clear();
        this.villageDebugRenderer.clear();
        this.beeDebugRenderer.clear();
        this.raidDebugRenderer.clear();
        this.goalSelectorRenderer.clear();
        this.gameTestDebugRenderer.clear();
    }

    public boolean switchRenderChunkborder() {
        this.renderChunkborder = !this.renderChunkborder;
        return this.renderChunkborder;
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double d, double e, double f, long l) {
        if (this.renderChunkborder && !Minecraft.getInstance().showOnlyReducedInfo()) {
            this.chunkBorderRenderer.render(poseStack, bufferSource, d, e, f, l);
        }
        this.gameTestDebugRenderer.render(poseStack, bufferSource, d, e, f, l);
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
        Vec3 vec3 = entity2.getEyePosition(1.0f);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity2, vec3, vec33 = vec3.add(vec32 = entity2.getViewVector(1.0f).scale(i)), aABB = entity2.getBoundingBox().expandTowards(vec32).inflate(1.0), predicate = entity -> !entity.isSpectator() && entity.isPickable(), j = i * i);
        if (entityHitResult == null) {
            return Optional.empty();
        }
        if (vec3.distanceToSqr(entityHitResult.getLocation()) > (double)j) {
            return Optional.empty();
        }
        return Optional.of(entityHitResult.getEntity());
    }

    public static void renderFilledBox(BlockPos blockPos, BlockPos blockPos2, float f, float g, float h, float i) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 vec3 = camera.getPosition().reverse();
        AABB aABB = new AABB(blockPos, blockPos2).move(vec3);
        DebugRenderer.renderFilledBox(aABB, f, g, h, i);
    }

    public static void renderFilledBox(BlockPos blockPos, float f, float g, float h, float i, float j) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 vec3 = camera.getPosition().reverse();
        AABB aABB = new AABB(blockPos).move(vec3).inflate(f);
        DebugRenderer.renderFilledBox(aABB, g, h, i, j);
    }

    public static void renderFilledBox(AABB aABB, float f, float g, float h, float i) {
        DebugRenderer.renderFilledBox(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, g, h, i);
    }

    public static void renderFilledBox(double d, double e, double f, double g, double h, double i, float j, float k, float l, float m) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
        LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, d, e, f, g, h, i, j, k, l, m);
        tesselator.end();
    }

    public static void renderFloatingText(String string, int i, int j, int k, int l) {
        DebugRenderer.renderFloatingText(string, (double)i + 0.5, (double)j + 0.5, (double)k + 0.5, l);
    }

    public static void renderFloatingText(String string, double d, double e, double f, int i) {
        DebugRenderer.renderFloatingText(string, d, e, f, i, 0.02f);
    }

    public static void renderFloatingText(String string, double d, double e, double f, int i, float g) {
        DebugRenderer.renderFloatingText(string, d, e, f, i, g, true, 0.0f, false);
    }

    public static void renderFloatingText(String string, double d, double e, double f, int i, float g, boolean bl, float h, boolean bl2) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (!camera.isInitialized() || minecraft.getEntityRenderDispatcher().options == null) {
            return;
        }
        Font font = minecraft.font;
        double j = camera.getPosition().x;
        double k = camera.getPosition().y;
        double l = camera.getPosition().z;
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)(d - j), (float)(e - k) + 0.07f, (float)(f - l));
        RenderSystem.normal3f(0.0f, 1.0f, 0.0f);
        RenderSystem.multMatrix(new Matrix4f(camera.rotation()));
        RenderSystem.scalef(g, -g, g);
        RenderSystem.enableTexture();
        if (bl2) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.depthMask(true);
        RenderSystem.scalef(-1.0f, 1.0f, 1.0f);
        float m = bl ? (float)(-font.width(string)) / 2.0f : 0.0f;
        RenderSystem.enableAlphaTest();
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(string, m -= h / g, 0.0f, i, false, Transformation.identity().getMatrix(), bufferSource, bl2, 0, 0xF000F0);
        bufferSource.endBatch();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.popMatrix();
    }

    @Environment(value=EnvType.CLIENT)
    public static interface SimpleDebugRenderer {
        public void render(PoseStack var1, MultiBufferSource var2, double var3, double var5, double var7, long var9);

        default public void clear() {
        }
    }
}

