package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.Objects;
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
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.transform.EntityTransform;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class EntityRenderDispatcher implements ResourceManagerReloadListener {
	public static final StencilRenderer.Triangle[] SHADOW_VOLUME = StencilRenderer.createNCone(12);
	private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(new ResourceLocation("textures/misc/shadow.png"));
	private static final float MAX_SHADOW_RADIUS = 32.0F;
	private static final float SHADOW_POWER_FALLOFF_Y = 0.5F;
	private Map<EntityType<?>, EntityRenderer<?>> renderers = ImmutableMap.of();
	private Map<String, EntityRenderer<? extends Player>> playerRenderers = ImmutableMap.of();
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
		if (entity instanceof AbstractClientPlayer) {
			String string = ((AbstractClientPlayer)entity).getModelName();
			EntityRenderer<? extends Player> entityRenderer = (EntityRenderer<? extends Player>)this.playerRenderers.get(string);
			return (EntityRenderer<? super T>)(entityRenderer != null ? entityRenderer : (EntityRenderer)this.playerRenderers.get("default"));
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
		EntityTransform entityTransform = EntityTransform.get(entity);
		Entity entity2 = (Entity)Objects.requireNonNullElse(entityTransform.entity(), entity);
		return this.shouldRender0(entity2, frustum, d, e, f);
	}

	private <E extends Entity> boolean shouldRender0(E entity, Frustum frustum, double d, double e, double f) {
		EntityRenderer<? super E> entityRenderer = this.getRenderer(entity);
		return entityRenderer.shouldRender(entity, frustum, d, e, f);
	}

	public <E extends Entity> void render(
		E entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource, int i
	) {
		EntityTransform entityTransform = EntityTransform.get(entity);
		float j = 1.0F;
		if (entity instanceof LivingEntity livingEntity) {
			if (entityTransform.entity() != null) {
				entityTransform.copyProperties(livingEntity);
			}

			j = livingEntity.getTransformScale(h);
		}

		Entity entity2 = (Entity)Objects.requireNonNullElse(entityTransform.entity(), entity);
		this.render0(entity2, j, d, e, f, g, h, poseStack, multiBufferSource, i);
	}

	private <E extends Entity> void render0(
		E entity, float f, double d, double e, double g, float h, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j
	) {
		EntityRenderer<? super E> entityRenderer = this.getRenderer(entity);

		try {
			Vec3 vec3 = entityRenderer.getRenderOffset(entity, i);
			double k = d + vec3.x();
			double l = e + vec3.y();
			double m = g + vec3.z();
			poseStack.pushPose();
			poseStack.translate(k, l, m);
			if (f != 1.0F) {
				poseStack.pushPose();
				poseStack.scale(f, f, f);
			}

			entityRenderer.render(entity, h, i, poseStack, multiBufferSource, j);
			if (entity.displayFireAnimation()) {
				this.renderFlame(poseStack, multiBufferSource, entity);
			}

			poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
			if (f != 1.0F) {
				poseStack.popPose();
			}

			if (this.options.entityShadows().get() && this.shouldRenderShadow && entityRenderer.shadowRadius > 0.0F && !entity.isInvisible()) {
				double n = this.distanceToSqr(entity.getX(), entity.getY(), entity.getZ());
				float o = (float)((1.0 - n / 256.0) * (double)entityRenderer.shadowStrength);
				if (o > 0.0F) {
					renderShadow(poseStack, multiBufferSource, entity, o, i, this.level, Math.min(entityRenderer.shadowRadius, 32.0F));
				}
			}

			if (this.renderHitBoxes && !entity.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
				renderHitbox(poseStack, multiBufferSource.getBuffer(RenderType.lines()), entity, i);
			}

			poseStack.popPose();
		} catch (Throwable var25) {
			CrashReport crashReport = CrashReport.forThrowable(var25, "Rendering entity in world");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being rendered");
			entity.fillCrashReportCategory(crashReportCategory);
			CrashReportCategory crashReportCategory2 = crashReport.addCategory("Renderer details");
			crashReportCategory2.setDetail("Assigned renderer", entityRenderer);
			crashReportCategory2.setDetail("Location", CrashReportCategory.formatLocation(this.level, d, e, g));
			crashReportCategory2.setDetail("Rotation", h);
			crashReportCategory2.setDetail("Delta", i);
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

		Vec3 vec3 = entity.getViewVector(f);
		Matrix4f matrix4f = poseStack.last().pose();
		Matrix3f matrix3f = poseStack.last().normal();
		vertexConsumer.vertex(matrix4f, 0.0F, entity.getEyeHeight(), 0.0F)
			.color(0, 0, 255, 255)
			.normal(matrix3f, (float)vec3.x, (float)vec3.y, (float)vec3.z)
			.endVertex();
		vertexConsumer.vertex(matrix4f, (float)(vec3.x * 2.0), (float)((double)entity.getEyeHeight() + vec3.y * 2.0), (float)(vec3.z * 2.0))
			.color(0, 0, 255, 255)
			.normal(matrix3f, (float)vec3.x, (float)vec3.y, (float)vec3.z)
			.endVertex();
	}

	private void renderFlame(PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity) {
		TextureAtlasSprite textureAtlasSprite = ModelBakery.FIRE_0.sprite();
		TextureAtlasSprite textureAtlasSprite2 = ModelBakery.FIRE_1.sprite();
		poseStack.pushPose();
		float f = entity.getBbWidth() * 1.4F;
		poseStack.scale(f, f, f);
		float g = 0.5F;
		float h = 0.0F;
		float i = entity.getBbHeight() / f;
		float j = 0.0F;
		poseStack.mulPose(Axis.YP.rotationDegrees(-this.camera.getYRot()));
		poseStack.translate(0.0F, 0.0F, -0.3F + (float)((int)i) * 0.02F);
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

			fireVertex(pose, vertexConsumer, g - 0.0F, 0.0F - j, k, o, p);
			fireVertex(pose, vertexConsumer, -g - 0.0F, 0.0F - j, k, m, p);
			fireVertex(pose, vertexConsumer, -g - 0.0F, 1.4F - j, k, m, n);
			fireVertex(pose, vertexConsumer, g - 0.0F, 1.4F - j, k, o, n);
			i -= 0.45F;
			j -= 0.45F;
			g *= 0.9F;
			k += 0.03F;
		}

		poseStack.popPose();
	}

	private static void fireVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j) {
		vertexConsumer.vertex(pose.pose(), f, g, h)
			.color(255, 255, 255, 255)
			.uv(i, j)
			.overlayCoords(0, 10)
			.uv2(240)
			.normal(pose.normal(), 0.0F, 1.0F, 0.0F)
			.endVertex();
	}

	private static void renderShadow(PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity, float f, float g, LevelReader levelReader, float h) {
		float i = h;
		if (entity instanceof Mob mob && mob.isBaby()) {
			i = h * 0.5F;
		}

		if (Rules.BEDROCK_SHADOWS.get()) {
			if (Rules.GLOWING_GLOW_SQUIDS.get()) {
				if (entity instanceof GlowSquid) {
					return;
				}

				if (entity instanceof Bee && Rules.GLOW_BEES.get() && BeeRenderer.isGlowTime(entity.level)) {
					return;
				}
			}

			renderDynamicShadow(poseStack, multiBufferSource, i, Math.min(f / 0.5F, i));
		} else {
			double d = Mth.lerp((double)g, entity.xOld, entity.getX());
			double e = Mth.lerp((double)g, entity.yOld, entity.getY());
			double j = Mth.lerp((double)g, entity.zOld, entity.getZ());
			float k = Math.min(f / 0.5F, i);
			int l = Mth.floor(d - (double)i);
			int m = Mth.floor(d + (double)i);
			int n = Mth.floor(e - (double)k);
			int o = Mth.floor(e);
			int p = Mth.floor(j - (double)i);
			int q = Mth.floor(j + (double)i);
			PoseStack.Pose pose = poseStack.last();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(SHADOW_RENDER_TYPE);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int r = p; r <= q; r++) {
				for (int s = l; s <= m; s++) {
					mutableBlockPos.set(s, 0, r);
					ChunkAccess chunkAccess = levelReader.getChunk(mutableBlockPos);

					for (int t = n; t <= o; t++) {
						mutableBlockPos.setY(t);
						float u = f - (float)(e - (double)mutableBlockPos.getY()) * 0.5F;
						renderBlockShadow(pose, vertexConsumer, chunkAccess, levelReader, mutableBlockPos, d, e, j, i, u);
					}
				}
			}
		}
	}

	private static void renderDynamicShadow(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, float g) {
		poseStack.pushPose();
		poseStack.scale(f, f * g * 4.0F, f);
		poseStack.translate(0.0F, 0.01F, 0.0F);
		Matrix4f matrix4f = poseStack.last().pose();
		StencilRenderer.render(SHADOW_VOLUME, matrix4f, multiBufferSource, 1610612736);
		poseStack.popPose();
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

						AABB aABB = voxelShape.bounds();
						double k = (double)blockPos.getX() + aABB.minX;
						double l = (double)blockPos.getX() + aABB.maxX;
						double m = (double)blockPos.getY() + aABB.minY;
						double n = (double)blockPos.getZ() + aABB.minZ;
						double o = (double)blockPos.getZ() + aABB.maxZ;
						float p = (float)(k - d);
						float q = (float)(l - d);
						float r = (float)(m - e);
						float s = (float)(n - f);
						float t = (float)(o - f);
						float u = -p / 2.0F / g + 0.5F;
						float v = -q / 2.0F / g + 0.5F;
						float w = -s / 2.0F / g + 0.5F;
						float x = -t / 2.0F / g + 0.5F;
						shadowVertex(pose, vertexConsumer, j, p, r, s, u, w);
						shadowVertex(pose, vertexConsumer, j, p, r, t, u, x);
						shadowVertex(pose, vertexConsumer, j, q, r, t, v, x);
						shadowVertex(pose, vertexConsumer, j, q, r, s, v, w);
					}
				}
			}
		}
	}

	private static void shadowVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k) {
		Vector3f vector3f = pose.pose().transformPosition(g, h, i, new Vector3f());
		vertexConsumer.vertex(vector3f.x(), vector3f.y(), vector3f.z(), 1.0F, 1.0F, 1.0F, f, j, k, OverlayTexture.NO_OVERLAY, 15728880, 0.0F, 1.0F, 0.0F);
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
