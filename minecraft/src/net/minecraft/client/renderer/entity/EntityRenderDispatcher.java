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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.EquipmentModelSet;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
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
	private Map<EntityType<?>, EntityRenderer<?, ?>> renderers = ImmutableMap.of();
	private Map<PlayerSkin.Model, EntityRenderer<? extends Player, ?>> playerRenderers = Map.of();
	public final TextureManager textureManager;
	private Level level;
	public Camera camera;
	private Quaternionf cameraOrientation;
	public Entity crosshairPickEntity;
	private final ItemRenderer itemRenderer;
	private final MapRenderer mapRenderer;
	private final BlockRenderDispatcher blockRenderDispatcher;
	private final ItemInHandRenderer itemInHandRenderer;
	private final Font font;
	public final Options options;
	private final EntityModelSet entityModels;
	private final EquipmentModelSet equipmentModels;
	private boolean shouldRenderShadow = true;
	private boolean renderHitBoxes;

	public <E extends Entity> int getPackedLightCoords(E entity, float f) {
		return this.getRenderer(entity).getPackedLightCoords(entity, f);
	}

	public EntityRenderDispatcher(
		Minecraft minecraft,
		TextureManager textureManager,
		ItemRenderer itemRenderer,
		MapRenderer mapRenderer,
		BlockRenderDispatcher blockRenderDispatcher,
		Font font,
		Options options,
		EntityModelSet entityModelSet,
		EquipmentModelSet equipmentModelSet
	) {
		this.textureManager = textureManager;
		this.itemRenderer = itemRenderer;
		this.mapRenderer = mapRenderer;
		this.itemInHandRenderer = new ItemInHandRenderer(minecraft, this, itemRenderer);
		this.blockRenderDispatcher = blockRenderDispatcher;
		this.font = font;
		this.options = options;
		this.entityModels = entityModelSet;
		this.equipmentModels = equipmentModelSet;
	}

	public <T extends Entity> EntityRenderer<? super T, ?> getRenderer(T entity) {
		if (entity instanceof AbstractClientPlayer abstractClientPlayer) {
			PlayerSkin.Model model = abstractClientPlayer.getSkin().model();
			EntityRenderer<? extends Player, ?> entityRenderer = (EntityRenderer<? extends Player, ?>)this.playerRenderers.get(model);
			return (EntityRenderer<? super T, ?>)(entityRenderer != null ? entityRenderer : (EntityRenderer)this.playerRenderers.get(PlayerSkin.Model.WIDE));
		} else {
			return (EntityRenderer<? super T, ?>)this.renderers.get(entity.getType());
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
		EntityRenderer<? super E, ?> entityRenderer = this.getRenderer(entity);
		return entityRenderer.shouldRender(entity, frustum, d, e, f);
	}

	public <E extends Entity> void render(E entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		EntityRenderer<? super E, ?> entityRenderer = this.getRenderer(entity);
		this.render(entity, d, e, f, g, poseStack, multiBufferSource, i, entityRenderer);
	}

	private <E extends Entity, S extends EntityRenderState> void render(
		E entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, EntityRenderer<? super E, S> entityRenderer
	) {
		try {
			S entityRenderState = entityRenderer.createRenderState(entity, g);
			Vec3 vec3 = entityRenderer.getRenderOffset(entityRenderState);
			double h = d + vec3.x();
			double j = e + vec3.y();
			double k = f + vec3.z();
			poseStack.pushPose();
			poseStack.translate(h, j, k);
			entityRenderer.render(entityRenderState, poseStack, multiBufferSource, i);
			if (entityRenderState.displayFireAnimation) {
				this.renderFlame(poseStack, multiBufferSource, entityRenderState, Mth.rotationAroundAxis(Mth.Y_AXIS, this.cameraOrientation, new Quaternionf()));
			}

			if (entity instanceof Player) {
				poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
			}

			if (this.options.entityShadows().get() && this.shouldRenderShadow && !entityRenderState.isInvisible) {
				float l = entityRenderer.getShadowRadius(entityRenderState);
				if (l > 0.0F) {
					double m = entityRenderState.distanceToCameraSq;
					float n = (float)((1.0 - m / 256.0) * (double)entityRenderer.shadowStrength);
					if (n > 0.0F) {
						renderShadow(poseStack, multiBufferSource, entityRenderState, n, g, this.level, Math.min(l, 32.0F));
					}
				}
			}

			if (!(entity instanceof Player)) {
				poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
			}

			if (this.renderHitBoxes && !entityRenderState.isInvisible && !Minecraft.getInstance().showOnlyReducedInfo()) {
				renderHitbox(poseStack, multiBufferSource.getBuffer(RenderType.lines()), entity, g, 1.0F, 1.0F, 1.0F);
			}

			poseStack.popPose();
		} catch (Throwable var25) {
			CrashReport crashReport = CrashReport.forThrowable(var25, "Rendering entity in world");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being rendered");
			entity.fillCrashReportCategory(crashReportCategory);
			CrashReportCategory crashReportCategory2 = crashReport.addCategory("Renderer details");
			crashReportCategory2.setDetail("Assigned renderer", entityRenderer);
			crashReportCategory2.setDetail("Location", CrashReportCategory.formatLocation(this.level, d, e, f));
			crashReportCategory2.setDetail("Delta", g);
			throw new ReportedException(crashReport);
		}
	}

	private static void renderServerSideHitbox(PoseStack poseStack, Entity entity, MultiBufferSource multiBufferSource) {
		Entity entity2 = getServerSideEntity(entity);
		if (entity2 == null) {
			DebugRenderer.renderFloatingText(poseStack, multiBufferSource, "Missing", entity.getX(), entity.getBoundingBox().maxY + 1.5, entity.getZ(), -65536);
		} else {
			poseStack.pushPose();
			poseStack.translate(entity2.getX() - entity.getX(), entity2.getY() - entity.getY(), entity2.getZ() - entity.getZ());
			renderHitbox(poseStack, multiBufferSource.getBuffer(RenderType.lines()), entity2, 1.0F, 0.0F, 1.0F, 0.0F);
			ShapeRenderer.renderVector(poseStack, multiBufferSource.getBuffer(RenderType.lines()), new Vector3f(), entity2.getDeltaMovement(), -256);
			poseStack.popPose();
		}
	}

	@Nullable
	private static Entity getServerSideEntity(Entity entity) {
		IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
		if (integratedServer != null) {
			ServerLevel serverLevel = integratedServer.getLevel(entity.level().dimension());
			if (serverLevel != null) {
				return serverLevel.getEntity(entity.getId());
			}
		}

		return null;
	}

	private static void renderHitbox(PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, float f, float g, float h, float i) {
		AABB aABB = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
		ShapeRenderer.renderLineBox(poseStack, vertexConsumer, aABB, g, h, i, 1.0F);
		if (entity instanceof EnderDragon) {
			double d = -Mth.lerp((double)f, entity.xOld, entity.getX());
			double e = -Mth.lerp((double)f, entity.yOld, entity.getY());
			double j = -Mth.lerp((double)f, entity.zOld, entity.getZ());

			for (EnderDragonPart enderDragonPart : ((EnderDragon)entity).getSubEntities()) {
				poseStack.pushPose();
				double k = d + Mth.lerp((double)f, enderDragonPart.xOld, enderDragonPart.getX());
				double l = e + Mth.lerp((double)f, enderDragonPart.yOld, enderDragonPart.getY());
				double m = j + Mth.lerp((double)f, enderDragonPart.zOld, enderDragonPart.getZ());
				poseStack.translate(k, l, m);
				ShapeRenderer.renderLineBox(
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
			float n = 0.01F;
			ShapeRenderer.renderLineBox(
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
			float o = Math.min(entity2.getBbWidth(), entity.getBbWidth()) / 2.0F;
			float p = 0.0625F;
			Vec3 vec3 = entity2.getPassengerRidingPosition(entity).subtract(entity.position());
			ShapeRenderer.renderLineBox(
				poseStack, vertexConsumer, vec3.x - (double)o, vec3.y, vec3.z - (double)o, vec3.x + (double)o, vec3.y + 0.0625, vec3.z + (double)o, 1.0F, 1.0F, 0.0F, 1.0F
			);
		}

		ShapeRenderer.renderVector(poseStack, vertexConsumer, new Vector3f(0.0F, entity.getEyeHeight(), 0.0F), entity.getViewVector(f).scale(2.0), -16776961);
	}

	private void renderFlame(PoseStack poseStack, MultiBufferSource multiBufferSource, EntityRenderState entityRenderState, Quaternionf quaternionf) {
		TextureAtlasSprite textureAtlasSprite = ModelBakery.FIRE_0.sprite();
		TextureAtlasSprite textureAtlasSprite2 = ModelBakery.FIRE_1.sprite();
		poseStack.pushPose();
		float f = entityRenderState.boundingBoxWidth * 1.4F;
		poseStack.scale(f, f, f);
		float g = 0.5F;
		float h = 0.0F;
		float i = entityRenderState.boundingBoxHeight / f;
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

	private static void renderShadow(
		PoseStack poseStack, MultiBufferSource multiBufferSource, EntityRenderState entityRenderState, float f, float g, LevelReader levelReader, float h
	) {
		float i = Math.min(f / 0.5F, h);
		int j = Mth.floor(entityRenderState.x - (double)h);
		int k = Mth.floor(entityRenderState.x + (double)h);
		int l = Mth.floor(entityRenderState.y - (double)i);
		int m = Mth.floor(entityRenderState.y);
		int n = Mth.floor(entityRenderState.z - (double)h);
		int o = Mth.floor(entityRenderState.z + (double)h);
		PoseStack.Pose pose = poseStack.last();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(SHADOW_RENDER_TYPE);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int p = n; p <= o; p++) {
			for (int q = j; q <= k; q++) {
				mutableBlockPos.set(q, 0, p);
				ChunkAccess chunkAccess = levelReader.getChunk(mutableBlockPos);

				for (int r = l; r <= m; r++) {
					mutableBlockPos.setY(r);
					float s = f - (float)(entityRenderState.y - (double)mutableBlockPos.getY()) * 0.5F;
					renderBlockShadow(pose, vertexConsumer, chunkAccess, levelReader, mutableBlockPos, entityRenderState.x, entityRenderState.y, entityRenderState.z, h, s);
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

						int k = ARGB.color(Mth.floor(j * 255.0F), 255, 255, 255);
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
			this, this.itemRenderer, this.mapRenderer, this.blockRenderDispatcher, resourceManager, this.entityModels, this.equipmentModels, this.font
		);
		this.renderers = EntityRenderers.createEntityRenderers(context);
		this.playerRenderers = EntityRenderers.createPlayerRenderers(context);
	}
}
