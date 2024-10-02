package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(EnvType.CLIENT)
public class DebugRenderer {
	public final PathfindingRenderer pathfindingRenderer = new PathfindingRenderer();
	public final DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
	public final DebugRenderer.SimpleDebugRenderer chunkBorderRenderer;
	public final DebugRenderer.SimpleDebugRenderer heightMapRenderer;
	public final DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
	public final DebugRenderer.SimpleDebugRenderer supportBlockRenderer;
	public final NeighborsUpdateRenderer neighborsUpdateRenderer;
	public final RedstoneWireOrientationsRenderer redstoneWireOrientationsRenderer;
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
	public final ChunkCullingDebugRenderer chunkCullingDebugRenderer;
	public final OctreeDebugRenderer octreeDebugRenderer;
	private boolean renderChunkborder;
	private boolean renderOctree;

	public DebugRenderer(Minecraft minecraft) {
		this.waterDebugRenderer = new WaterDebugRenderer(minecraft);
		this.chunkBorderRenderer = new ChunkBorderRenderer(minecraft);
		this.heightMapRenderer = new HeightMapRenderer(minecraft);
		this.collisionBoxRenderer = new CollisionBoxRenderer(minecraft);
		this.supportBlockRenderer = new SupportBlockRenderer(minecraft);
		this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(minecraft);
		this.redstoneWireOrientationsRenderer = new RedstoneWireOrientationsRenderer(minecraft);
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
		this.chunkCullingDebugRenderer = new ChunkCullingDebugRenderer(minecraft);
		this.octreeDebugRenderer = new OctreeDebugRenderer(minecraft);
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
		this.chunkCullingDebugRenderer.clear();
	}

	public boolean switchRenderChunkborder() {
		this.renderChunkborder = !this.renderChunkborder;
		return this.renderChunkborder;
	}

	public boolean toggleRenderOctree() {
		return this.renderOctree = !this.renderOctree;
	}

	public void render(PoseStack poseStack, Frustum frustum, MultiBufferSource.BufferSource bufferSource, double d, double e, double f) {
		if (this.renderChunkborder && !Minecraft.getInstance().showOnlyReducedInfo()) {
			this.chunkBorderRenderer.render(poseStack, bufferSource, d, e, f);
		}

		if (this.renderOctree) {
			this.octreeDebugRenderer.render(poseStack, frustum, bufferSource, d, e, f);
		}

		this.gameTestDebugRenderer.render(poseStack, bufferSource, d, e, f);
	}

	public void renderAfterTranslucents(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double d, double e, double f) {
		this.chunkCullingDebugRenderer.render(poseStack, bufferSource, d, e, f);
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
			EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, vec3, vec33, aABB, EntitySelector.CAN_BE_PICKED, (double)j);
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
		ShapeRenderer.addChainedFilledBoxVertices(poseStack, vertexConsumer, d, e, f, g, h, i, j, k, l, m);
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
			poseStack.mulPose(camera.rotation());
			poseStack.scale(g, -g, g);
			float m = bl ? (float)(-font.width(string)) / 2.0F : 0.0F;
			m -= h / g;
			font.drawInBatch(
				string, m, 0.0F, i, false, poseStack.last().pose(), multiBufferSource, bl2 ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, 15728880
			);
			poseStack.popPose();
		}
	}

	private static Vec3 mixColor(float f) {
		float g = 5.99999F;
		int i = (int)(Mth.clamp(f, 0.0F, 1.0F) * 5.99999F);
		float h = f * 5.99999F - (float)i;

		return switch (i) {
			case 0 -> new Vec3(1.0, (double)h, 0.0);
			case 1 -> new Vec3((double)(1.0F - h), 1.0, 0.0);
			case 2 -> new Vec3(0.0, 1.0, (double)h);
			case 3 -> new Vec3(0.0, 1.0 - (double)h, 1.0);
			case 4 -> new Vec3((double)h, 0.0, 1.0);
			case 5 -> new Vec3(1.0, 0.0, 1.0 - (double)h);
			default -> throw new IllegalStateException("Unexpected value: " + i);
		};
	}

	private static Vec3 shiftHue(float f, float g, float h, float i) {
		Vec3 vec3 = mixColor(i).scale((double)f);
		Vec3 vec32 = mixColor((i + 0.33333334F) % 1.0F).scale((double)g);
		Vec3 vec33 = mixColor((i + 0.6666667F) % 1.0F).scale((double)h);
		Vec3 vec34 = vec3.add(vec32).add(vec33);
		double d = Math.max(Math.max(1.0, vec34.x), Math.max(vec34.y, vec34.z));
		return new Vec3(vec34.x / d, vec34.y / d, vec34.z / d);
	}

	public static void renderVoxelShape(
		PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j, boolean bl
	) {
		List<AABB> list = voxelShape.toAabbs();
		if (!list.isEmpty()) {
			int k = bl ? list.size() : list.size() * 8;
			ShapeRenderer.renderShape(poseStack, vertexConsumer, Shapes.create((AABB)list.get(0)), d, e, f, ARGB.colorFromFloat(j, g, h, i));

			for (int l = 1; l < list.size(); l++) {
				AABB aABB = (AABB)list.get(l);
				float m = (float)l / (float)k;
				Vec3 vec3 = shiftHue(g, h, i, m);
				ShapeRenderer.renderShape(poseStack, vertexConsumer, Shapes.create(aABB), d, e, f, ARGB.colorFromFloat(j, (float)vec3.x, (float)vec3.y, (float)vec3.z));
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public interface SimpleDebugRenderer {
		void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f);

		default void clear() {
		}
	}
}
