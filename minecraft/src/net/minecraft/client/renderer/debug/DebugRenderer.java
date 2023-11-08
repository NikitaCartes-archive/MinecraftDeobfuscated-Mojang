package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class DebugRenderer {
	public final PathfindingRenderer pathfindingRenderer = new PathfindingRenderer();
	public final DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
	public final DebugRenderer.SimpleDebugRenderer chunkBorderRenderer;
	public final DebugRenderer.SimpleDebugRenderer heightMapRenderer;
	public final DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
	public final DebugRenderer.SimpleDebugRenderer supportBlockRenderer;
	public final DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;
	public final StructureRenderer structureRenderer;
	public final DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
	public final DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer;
	public final DebugRenderer.SimpleDebugRenderer solidFaceRenderer;
	public final DebugRenderer.SimpleDebugRenderer chunkRenderer;
	public final BrainDebugRenderer brainDebugRenderer;
	public final VillageSectionsDebugRenderer villageSectionsDebugRenderer;
	public final BeeDebugRenderer beeDebugRenderer;
	public final RaidDebugRenderer raidDebugRenderer;
	public final GoalSelectorDebugRenderer goalSelectorRenderer;
	public final GameTestDebugRenderer gameTestDebugRenderer;
	public final GameEventListenerRenderer gameEventListenerRenderer;
	public final LightSectionDebugRenderer skyLightSectionDebugRenderer;
	public final BreezeDebugRenderer breezeDebugRenderer;
	private boolean renderChunkborder;

	public DebugRenderer(Minecraft minecraft) {
		this.waterDebugRenderer = new WaterDebugRenderer(minecraft);
		this.chunkBorderRenderer = new ChunkBorderRenderer(minecraft);
		this.heightMapRenderer = new HeightMapRenderer(minecraft);
		this.collisionBoxRenderer = new CollisionBoxRenderer(minecraft);
		this.supportBlockRenderer = new SupportBlockRenderer(minecraft);
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
		this.skyLightSectionDebugRenderer = new LightSectionDebugRenderer(minecraft, LightLayer.SKY);
		this.breezeDebugRenderer = new BreezeDebugRenderer(minecraft);
	}

	public void clear() {
		this.pathfindingRenderer.clear();
		this.waterDebugRenderer.clear();
		this.chunkBorderRenderer.clear();
		this.heightMapRenderer.clear();
		this.collisionBoxRenderer.clear();
		this.supportBlockRenderer.clear();
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
		this.skyLightSectionDebugRenderer.clear();
		this.breezeDebugRenderer.clear();
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

	public static Optional<Entity> getTargetedEntity(@Nullable Entity entity, int i) {
		if (entity == null) {
			return Optional.empty();
		} else {
			Vec3 vec3 = entity.getEyePosition();
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

	public static void renderFilledUnitCube(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, float f, float g, float h, float i) {
		renderFilledBox(poseStack, multiBufferSource, blockPos, blockPos.offset(1, 1, 1), f, g, h, i);
	}

	public static void renderFilledBox(
		PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, BlockPos blockPos2, float f, float g, float h, float i
	) {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		if (camera.isInitialized()) {
			Vec3 vec3 = camera.getPosition().reverse();
			AABB aABB = AABB.encapsulatingFullBlocks(blockPos, blockPos2).move(vec3);
			renderFilledBox(poseStack, multiBufferSource, aABB, f, g, h, i);
		}
	}

	public static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, float f, float g, float h, float i, float j) {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		if (camera.isInitialized()) {
			Vec3 vec3 = camera.getPosition().reverse();
			AABB aABB = new AABB(blockPos).move(vec3).inflate((double)f);
			renderFilledBox(poseStack, multiBufferSource, aABB, g, h, i, j);
		}
	}

	public static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, AABB aABB, float f, float g, float h, float i) {
		renderFilledBox(poseStack, multiBufferSource, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, g, h, i);
	}

	public static void renderFilledBox(
		PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m
	) {
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
		LevelRenderer.addChainedFilledBoxVertices(poseStack, vertexConsumer, d, e, f, g, h, i, j, k, l, m);
	}

	public static void renderFloatingText(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, int i, int j, int k, int l) {
		renderFloatingText(poseStack, multiBufferSource, string, (double)i + 0.5, (double)j + 0.5, (double)k + 0.5, l);
	}

	public static void renderFloatingText(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, double d, double e, double f, int i) {
		renderFloatingText(poseStack, multiBufferSource, string, d, e, f, i, 0.02F);
	}

	public static void renderFloatingText(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, double d, double e, double f, int i, float g) {
		renderFloatingText(poseStack, multiBufferSource, string, d, e, f, i, g, true, 0.0F, false);
	}

	public static void renderFloatingText(
		PoseStack poseStack, MultiBufferSource multiBufferSource, String string, double d, double e, double f, int i, float g, boolean bl, float h, boolean bl2
	) {
		Minecraft minecraft = Minecraft.getInstance();
		Camera camera = minecraft.gameRenderer.getMainCamera();
		if (camera.isInitialized() && minecraft.getEntityRenderDispatcher().options != null) {
			Font font = minecraft.font;
			double j = camera.getPosition().x;
			double k = camera.getPosition().y;
			double l = camera.getPosition().z;
			poseStack.pushPose();
			poseStack.translate((float)(d - j), (float)(e - k) + 0.07F, (float)(f - l));
			poseStack.mulPoseMatrix(new Matrix4f().rotation(camera.rotation()));
			poseStack.scale(-g, -g, g);
			float m = bl ? (float)(-font.width(string)) / 2.0F : 0.0F;
			m -= h / g;
			font.drawInBatch(
				string, m, 0.0F, i, false, poseStack.last().pose(), multiBufferSource, bl2 ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, 15728880
			);
			poseStack.popPose();
		}
	}

	@Environment(EnvType.CLIENT)
	public interface SimpleDebugRenderer {
		void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f);

		default void clear() {
		}
	}
}
