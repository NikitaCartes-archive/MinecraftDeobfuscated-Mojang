package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class DebugRenderer {
	public final PathfindingRenderer pathfindingRenderer;
	public final DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
	public final DebugRenderer.SimpleDebugRenderer chunkBorderRenderer;
	public final DebugRenderer.SimpleDebugRenderer heightMapRenderer;
	public final DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
	public final DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;
	public final CaveDebugRenderer caveRenderer;
	public final StructureRenderer structureRenderer;
	public final DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
	public final DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer;
	public final DebugRenderer.SimpleDebugRenderer solidFaceRenderer;
	public final DebugRenderer.SimpleDebugRenderer chunkRenderer;
	public final VillageDebugRenderer villageDebugRenderer;
	public final RaidDebugRenderer raidDebugRenderer;
	public final GoalSelectorDebugRenderer goalSelectorRenderer;
	private boolean renderChunkborder;

	public DebugRenderer(Minecraft minecraft) {
		this.pathfindingRenderer = new PathfindingRenderer(minecraft);
		this.waterDebugRenderer = new WaterDebugRenderer(minecraft);
		this.chunkBorderRenderer = new ChunkBorderRenderer(minecraft);
		this.heightMapRenderer = new HeightMapRenderer(minecraft);
		this.collisionBoxRenderer = new CollisionBoxRenderer(minecraft);
		this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(minecraft);
		this.caveRenderer = new CaveDebugRenderer(minecraft);
		this.structureRenderer = new StructureRenderer(minecraft);
		this.lightDebugRenderer = new LightDebugRenderer(minecraft);
		this.worldGenAttemptRenderer = new WorldGenAttemptRenderer(minecraft);
		this.solidFaceRenderer = new SolidFaceRenderer(minecraft);
		this.chunkRenderer = new ChunkDebugRenderer(minecraft);
		this.villageDebugRenderer = new VillageDebugRenderer(minecraft);
		this.raidDebugRenderer = new RaidDebugRenderer(minecraft);
		this.goalSelectorRenderer = new GoalSelectorDebugRenderer(minecraft);
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
		this.raidDebugRenderer.clear();
		this.goalSelectorRenderer.clear();
	}

	public boolean shouldRender() {
		return this.renderChunkborder;
	}

	public boolean switchRenderChunkborder() {
		this.renderChunkborder = !this.renderChunkborder;
		return this.renderChunkborder;
	}

	public void render(long l) {
		if (this.renderChunkborder && !Minecraft.getInstance().showOnlyReducedInfo()) {
			this.chunkBorderRenderer.render(l);
		}
	}

	public static Optional<Entity> getTargetedEntity(@Nullable Entity entity, int i) {
		if (entity == null) {
			return Optional.empty();
		} else {
			Vec3 vec3 = entity.getEyePosition(1.0F);
			Vec3 vec32 = entity.getViewVector(1.0F).scale((double)i);
			Vec3 vec33 = vec3.add(vec32);
			AABB aABB = entity.getBoundingBox().expandTowards(vec32).inflate(1.0);
			int j = i * i;
			Predicate<Entity> predicate = entityx -> !entityx.isSpectator() && entityx.isPickable();
			EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, vec3, vec33, aABB, predicate, (double)j);
			if (entityHitResult == null) {
				return Optional.empty();
			} else {
				return vec3.distanceToSqr(entityHitResult.getLocation()) > (double)j ? Optional.empty() : Optional.of(entityHitResult.getEntity());
			}
		}
	}

	public static void renderFilledBox(BlockPos blockPos, BlockPos blockPos2, float f, float g, float h, float i) {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		if (camera.isInitialized()) {
			Vec3 vec3 = camera.getPosition().reverse();
			AABB aABB = new AABB(blockPos, blockPos2).move(vec3);
			renderFilledBox(aABB, f, g, h, i);
		}
	}

	public static void renderFilledBox(BlockPos blockPos, float f, float g, float h, float i, float j) {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		if (camera.isInitialized()) {
			Vec3 vec3 = camera.getPosition().reverse();
			AABB aABB = new AABB(blockPos).move(vec3).inflate((double)f);
			renderFilledBox(aABB, g, h, i, j);
		}
	}

	public static void renderFilledBox(AABB aABB, float f, float g, float h, float i) {
		renderFilledBox(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, g, h, i);
	}

	public static void renderFilledBox(double d, double e, double f, double g, double h, double i, float j, float k, float l, float m) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
		LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, d, e, f, g, h, i, j, k, l, m);
		tesselator.end();
	}

	public static void renderFloatingText(String string, int i, int j, int k, int l) {
		renderFloatingText(string, (double)i + 0.5, (double)j + 0.5, (double)k + 0.5, l);
	}

	public static void renderFloatingText(String string, double d, double e, double f, int i) {
		renderFloatingText(string, d, e, f, i, 0.02F);
	}

	public static void renderFloatingText(String string, double d, double e, double f, int i, float g) {
		renderFloatingText(string, d, e, f, i, g, true, 0.0F, false);
	}

	public static void renderFloatingText(String string, double d, double e, double f, int i, float g, boolean bl, float h, boolean bl2) {
		Minecraft minecraft = Minecraft.getInstance();
		Camera camera = minecraft.gameRenderer.getMainCamera();
		if (camera.isInitialized() && minecraft.getEntityRenderDispatcher().options != null) {
			Font font = minecraft.font;
			double j = camera.getPosition().x;
			double k = camera.getPosition().y;
			double l = camera.getPosition().z;
			GlStateManager.pushMatrix();
			GlStateManager.translatef((float)(d - j), (float)(e - k) + 0.07F, (float)(f - l));
			GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
			GlStateManager.scalef(g, -g, g);
			EntityRenderDispatcher entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
			GlStateManager.rotatef(-entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotatef(-entityRenderDispatcher.playerRotX, 1.0F, 0.0F, 0.0F);
			GlStateManager.enableTexture();
			if (bl2) {
				GlStateManager.disableDepthTest();
			} else {
				GlStateManager.enableDepthTest();
			}

			GlStateManager.depthMask(true);
			GlStateManager.scalef(-1.0F, 1.0F, 1.0F);
			float m = bl ? (float)(-font.width(string)) / 2.0F : 0.0F;
			m -= h / g;
			font.draw(string, m, 0.0F, i);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableDepthTest();
			GlStateManager.popMatrix();
		}
	}

	@Environment(EnvType.CLIENT)
	public interface SimpleDebugRenderer {
		void render(long l);

		default void clear() {
		}
	}
}
