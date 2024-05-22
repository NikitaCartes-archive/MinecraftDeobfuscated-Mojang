package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class EntityRenderDispatcher implements ResourceManagerReloadListener {
	private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(ResourceLocation.withDefaultNamespace("textures/misc/shadow.png"));
	private static final float MAX_SHADOW_RADIUS = 32.0F;
	private static final float SHADOW_POWER_FALLOFF_Y = 0.5F;
	private Map<EntityType<?>, EntityRenderer<?>> renderers = ImmutableMap.of();
	private Map<PlayerSkin.Model, EntityRenderer<? extends Player>> playerRenderers = Map.of();
	public final TextureManager textureManager;
	private Level level;
	public Camera camera;
	private Quaternionf cameraOrientation;
	public Entity crosshairPickEntity;
	private final ItemRenderer itemRenderer;
	private final BlockRenderDispatcher blockRenderDispatcher;
	private final ItemInHandRenderer itemInHandRenderer;
	private final Font font;
	public final Options options;
	private final EntityModelSet entityModels;
	private boolean shouldRenderShadow = true;
	private boolean renderHitBoxes;

	public <E extends Entity> int getPackedLightCoords(E entity, float f) {
		return this.getRenderer(entity).getPackedLightCoords(entity, f);
	}

	public EntityRenderDispatcher(
		Minecraft minecraft,
		TextureManager textureManager,
		ItemRenderer itemRenderer,
		BlockRenderDispatcher blockRenderDispatcher,
		Font font,
		Options options,
		EntityModelSet entityModelSet
	) {
		this.textureManager = textureManager;
		this.itemRenderer = itemRenderer;
		this.itemInHandRenderer = new ItemInHandRenderer(minecraft, this, itemRenderer);
		this.blockRenderDispatcher = blockRenderDispatcher;
		this.font = font;
		this.options = options;
		this.entityModels = entityModelSet;
	}

	public <T extends Entity> EntityRenderer<? super T> getRenderer(T entity) {
		if (entity instanceof AbstractClientPlayer abstractClientPlayer) {
			PlayerSkin.Model model = abstractClientPlayer.getSkin().model();
			EntityRenderer<? extends Player> entityRenderer = (EntityRenderer<? extends Player>)this.playerRenderers.get(model);
			return (EntityRenderer<? super T>)(entityRenderer != null ? entityRenderer : (EntityRenderer)this.playerRenderers.get(PlayerSkin.Model.WIDE));
		} else {
			return (EntityRenderer<? super T>)this.renderers.get(entity.getType());
		}
	}

	public void prepare(Level level, Camera camera, Entity entity) {
		this.level = level;
		this.camera = camera;
		this.cameraOrientation = camera.rotation();
		this.crosshairPickEntity = entity;
	}

	public void overrideCameraOrientation(Quaternionf quaternionf) {
		this.cameraOrientation = quaternionf;
	}

	public void setRenderShadow(boolean bl) {
		this.shouldRenderShadow = bl;
	}

	public void setRenderHitBoxes(boolean bl) {
		this.renderHitBoxes = bl;
	}

	public boolean shouldRenderHitBoxes() {
		return this.renderHitBoxes;
	}

	public <E extends Entity> boolean shouldRender(E entity, Frustum frustum, double d, double e, double f) {
		EntityRenderer<? super E> entityRenderer = this.getRenderer(entity);
		return entityRenderer.shouldRender(entity, frustum, d, e, f);
	}

	public <E extends Entity> void render(
		E entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource, int i
	) {
		EntityRenderer<? super E> entityRenderer = this.getRenderer(entity);

		try {
			Vec3 vec3 = entityRenderer.getRenderOffset(entity, h);
			double j = d + vec3.x();
			double k = e + vec3.y();
			double l = f + vec3.z();
			poseStack.pushPose();
			poseStack.translate(j, k, l);
			entityRenderer.render(entity, g, h, poseStack, multiBufferSource, i);
			if (entity.displayFireAnimation()) {
				this.renderFlame(poseStack, multiBufferSource, entity, Mth.rotationAroundAxis(Mth.Y_AXIS, this.cameraOrientation, new Quaternionf()));
			}

			poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
			if (this.options.entityShadows().get() && this.shouldRenderShadow && !entity.isInvisible()) {
				float m = entityRenderer.getShadowRadius(entity);
				if (m > 0.0F) {
					double n = this.distanceToSqr(entity.getX(), entity.getY(), entity.getZ());
					float o = (float)((1.0 - n / 256.0) * (double)entityRenderer.shadowStrength);
					if (o > 0.0F) {
						renderShadow(poseStack, multiBufferSource, entity, o, h, this.level, Math.min(m, 32.0F));
					}
				}
			}

			if (this.renderHitBoxes && !entity.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
				renderHitbox(poseStack, multiBufferSource.getBuffer(RenderType.lines()), entity, h);
			}

			poseStack.popPose();
		} catch (Throwable var25) {
			CrashReport crashReport = CrashReport.forThrowable(var25, "Rendering entity in world");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being rendered");
			entity.fillCrashReportCategory(crashReportCategory);
			CrashReportCategory crashReportCategory2 = crashReport.addCategory("Renderer details");
			crashReportCategory2.setDetail("Assigned renderer", entityRenderer);
			crashReportCategory2.setDetail("Location", CrashReportCategory.formatLocation(this.level, d, e, f));
			crashReportCategory2.setDetail("Rotation", g);
			crashReportCategory2.setDetail("Delta", h);
			throw new ReportedException(crashReport);
		}
	}

	private static void renderHitbox(PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, float f) {
		AABB aABB = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
		LevelRenderer.renderLineBox(poseStack, vertexConsumer, aABB, 1.0F, 1.0F, 1.0F, 1.0F);
		if (entity instanceof EnderDragon) {
			double d = -Mth.lerp((double)f, entity.xOld, entity.getX());
			double e = -Mth.lerp((double)f, entity.yOld, entity.getY());
			double g = -Mth.lerp((double)f, entity.zOld, entity.getZ());

			for (EnderDragonPart enderDragonPart : ((EnderDragon)entity).getSubEntities()) {
				poseStack.pushPose();
				double h = d + Mth.lerp((double)f, enderDragonPart.xOld, enderDragonPart.getX());
				double i = e + Mth.lerp((double)f, enderDragonPart.yOld, enderDragonPart.getY());
				double j = g + Mth.lerp((double)f, enderDragonPart.zOld, enderDragonPart.getZ());
				poseStack.translate(h, i, j);
				LevelRenderer.renderLineBox(
					poseStack,
					vertexConsumer,
					enderDragonPart.getBoundingBox().move(-enderDragonPart.getX(), -enderDragonPart.getY(), -enderDragonPart.getZ()),
					0.25F,
					1.0F,
					0.0F,
					1.0F
				);
				poseStack.popPose();
			}
		}

		if (entity instanceof LivingEntity) {
			float k = 0.01F;
			LevelRenderer.renderLineBox(
				poseStack,
				vertexConsumer,
				aABB.minX,
				(double)(entity.getEyeHeight() - 0.01F),
				aABB.minZ,
				aABB.maxX,
				(double)(entity.getEyeHeight() + 0.01F),
				aABB.maxZ,
				1.0F,
				0.0F,
				0.0F,
				1.0F
			);
		}

		Entity entity2 = entity.getVehicle();
		if (entity2 != null) {
			float l = Math.min(entity2.getBbWidth(), entity.getBbWidth()) / 2.0F;
			float m = 0.0625F;
			Vec3 vec3 = entity2.getPassengerRidingPosition(entity).subtract(entity.position());
			LevelRenderer.renderLineBox(
				poseStack, vertexConsumer, vec3.x - (double)l, vec3.y, vec3.z - (double)l, vec3.x + (double)l, vec3.y + 0.0625, vec3.z + (double)l, 1.0F, 1.0F, 0.0F, 1.0F
			);
		}

		Vec3 vec32 = entity.getViewVector(f);
		PoseStack.Pose pose = poseStack.last();
		vertexConsumer.addVertex(pose, 0.0F, entity.getEyeHeight(), 0.0F).setColor(-16776961).setNormal(pose, (float)vec32.x, (float)vec32.y, (float)vec32.z);
		vertexConsumer.addVertex(pose, (float)(vec32.x * 2.0), (float)((double)entity.getEyeHeight() + vec32.y * 2.0), (float)(vec32.z * 2.0))
			.setColor(0, 0, 255, 255)
			.setNormal(pose, (float)vec32.x, (float)vec32.y, (float)vec32.z);
	}

	private void renderFlame(PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity, Quaternionf quaternionf) {
		TextureAtlasSprite textureAtlasSprite = ModelBakery.FIRE_0.sprite();
		TextureAtlasSprite textureAtlasSprite2 = ModelBakery.FIRE_1.sprite();
		poseStack.pushPose();
		float f = entity.getBbWidth() * 1.4F;
		poseStack.scale(f, f, f);
		float g = 0.5F;
		float h = 0.0F;
		float i = entity.getBbHeight() / f;
		float j = 0.0F;
		poseStack.mulPose(quaternionf);
		poseStack.translate(0.0F, 0.0F, 0.3F - (float)((int)i) * 0.02F);
		float k = 0.0F;
		int l = 0;
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(Sheets.cutoutBlockSheet());

		for (PoseStack.Pose pose = poseStack.last(); i > 0.0F; l++) {
			TextureAtlasSprite textureAtlasSprite3 = l % 2 == 0 ? textureAtlasSprite : textureAtlasSprite2;
			float m = textureAtlasSprite3.getU0();
			float n = textureAtlasSprite3.getV0();
			float o = textureAtlasSprite3.getU1();
			float p = textureAtlasSprite3.getV1();
			if (l / 2 % 2 == 0) {
				float q = o;
				o = m;
				m = q;
			}

			fireVertex(pose, vertexConsumer, -g - 0.0F, 0.0F - j, k, o, p);
			fireVertex(pose, vertexConsumer, g - 0.0F, 0.0F - j, k, m, p);
			fireVertex(pose, vertexConsumer, g - 0.0F, 1.4F - j, k, m, n);
			fireVertex(pose, vertexConsumer, -g - 0.0F, 1.4F - j, k, o, n);
			i -= 0.45F;
			j -= 0.45F;
			g *= 0.9F;
			k -= 0.03F;
		}

		poseStack.popPose();
	}

	private static void fireVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j) {
		vertexConsumer.addVertex(pose, f, g, h).setColor(-1).setUv(i, j).setUv1(0, 10).setLight(240).setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	private static void renderShadow(PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity, float f, float g, LevelReader levelReader, float h) {
		double d = Mth.lerp((double)g, entity.xOld, entity.getX());
		double e = Mth.lerp((double)g, entity.yOld, entity.getY());
		double i = Mth.lerp((double)g, entity.zOld, entity.getZ());
		float j = Math.min(f / 0.5F, h);
		int k = Mth.floor(d - (double)h);
		int l = Mth.floor(d + (double)h);
		int m = Mth.floor(e - (double)j);
		int n = Mth.floor(e);
		int o = Mth.floor(i - (double)h);
		int p = Mth.floor(i + (double)h);
		PoseStack.Pose pose = poseStack.last();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(SHADOW_RENDER_TYPE);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int q = o; q <= p; q++) {
			for (int r = k; r <= l; r++) {
				mutableBlockPos.set(r, 0, q);
				ChunkAccess chunkAccess = levelReader.getChunk(mutableBlockPos);

				for (int s = m; s <= n; s++) {
					mutableBlockPos.setY(s);
					float t = f - (float)(e - (double)mutableBlockPos.getY()) * 0.5F;
					renderBlockShadow(pose, vertexConsumer, chunkAccess, levelReader, mutableBlockPos, d, e, i, h, t);
				}
			}
		}
	}

	private static void renderBlockShadow(
		PoseStack.Pose pose,
		VertexConsumer vertexConsumer,
		ChunkAccess chunkAccess,
		LevelReader levelReader,
		BlockPos blockPos,
		double d,
		double e,
		double f,
		float g,
		float h
	) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState = chunkAccess.getBlockState(blockPos2);
		if (blockState.getRenderShape() != RenderShape.INVISIBLE && levelReader.getMaxLocalRawBrightness(blockPos) > 3) {
			if (blockState.isCollisionShapeFullBlock(chunkAccess, blockPos2)) {
				VoxelShape voxelShape = blockState.getShape(chunkAccess, blockPos2);
				if (!voxelShape.isEmpty()) {
					float i = LightTexture.getBrightness(levelReader.dimensionType(), levelReader.getMaxLocalRawBrightness(blockPos));
					float j = h * 0.5F * i;
					if (j >= 0.0F) {
						if (j > 1.0F) {
							j = 1.0F;
						}

						int k = FastColor.ARGB32.color(Mth.floor(j * 255.0F), 255, 255, 255);
						AABB aABB = voxelShape.bounds();
						double l = (double)blockPos.getX() + aABB.minX;
						double m = (double)blockPos.getX() + aABB.maxX;
						double n = (double)blockPos.getY() + aABB.minY;
						double o = (double)blockPos.getZ() + aABB.minZ;
						double p = (double)blockPos.getZ() + aABB.maxZ;
						float q = (float)(l - d);
						float r = (float)(m - d);
						float s = (float)(n - e);
						float t = (float)(o - f);
						float u = (float)(p - f);
						float v = -q / 2.0F / g + 0.5F;
						float w = -r / 2.0F / g + 0.5F;
						float x = -t / 2.0F / g + 0.5F;
						float y = -u / 2.0F / g + 0.5F;
						shadowVertex(pose, vertexConsumer, k, q, s, t, v, x);
						shadowVertex(pose, vertexConsumer, k, q, s, u, v, y);
						shadowVertex(pose, vertexConsumer, k, r, s, u, w, y);
						shadowVertex(pose, vertexConsumer, k, r, s, t, w, x);
					}
				}
			}
		}
	}

	private static void shadowVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, float f, float g, float h, float j, float k) {
		Vector3f vector3f = pose.pose().transformPosition(f, g, h, new Vector3f());
		vertexConsumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z(), i, j, k, OverlayTexture.NO_OVERLAY, 15728880, 0.0F, 1.0F, 0.0F);
	}

	public void setLevel(@Nullable Level level) {
		this.level = level;
		if (level == null) {
			this.camera = null;
		}
	}

	public double distanceToSqr(Entity entity) {
		return this.camera.getPosition().distanceToSqr(entity.position());
	}

	public double distanceToSqr(double d, double e, double f) {
		return this.camera.getPosition().distanceToSqr(d, e, f);
	}

	public Quaternionf cameraOrientation() {
		return this.cameraOrientation;
	}

	public ItemInHandRenderer getItemInHandRenderer() {
		return this.itemInHandRenderer;
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		EntityRendererProvider.Context context = new EntityRendererProvider.Context(
			this, this.itemRenderer, this.blockRenderDispatcher, this.itemInHandRenderer, resourceManager, this.entityModels, this.font
		);
		this.renderers = EntityRenderers.createEntityRenderers(context);
		this.playerRenderers = EntityRenderers.createPlayerRenderers(context);
	}
}
