package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(EnvType.CLIENT)
public class EntityRenderDispatcher implements ResourceManagerReloadListener {
	private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(new ResourceLocation("textures/misc/shadow.png"));
	private Map<EntityType<?>, EntityRenderer<?>> renderers = ImmutableMap.of();
	private Map<String, EntityRenderer<? extends Player>> playerRenderers = ImmutableMap.of();
	public final TextureManager textureManager;
	private Level level;
	public Camera camera;
	private Quaternion cameraOrientation;
	public Entity crosshairPickEntity;
	private final ItemRenderer itemRenderer;
	private final Font font;
	public final Options options;
	private final EntityModelSet entityModels;
	private boolean shouldRenderShadow = true;
	private boolean renderHitBoxes;

	public <E extends Entity> int getPackedLightCoords(E entity, float f) {
		return this.getRenderer(entity).getPackedLightCoords(entity, f);
	}

	public EntityRenderDispatcher(TextureManager textureManager, ItemRenderer itemRenderer, Font font, Options options, EntityModelSet entityModelSet) {
		this.textureManager = textureManager;
		this.itemRenderer = itemRenderer;
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

	public void overrideCameraOrientation(Quaternion quaternion) {
		this.cameraOrientation = quaternion;
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
				this.renderFlame(poseStack, multiBufferSource, entity);
			}

			poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
			if (this.options.entityShadows && this.shouldRenderShadow && entityRenderer.shadowRadius > 0.0F && !entity.isInvisible()) {
				double m = this.distanceToSqr(entity.getX(), entity.getY(), entity.getZ());
				float n = (float)((1.0 - m / 256.0) * (double)entityRenderer.shadowStrength);
				if (n > 0.0F) {
					renderShadow(poseStack, multiBufferSource, entity, n, h, this.level, entityRenderer.shadowRadius);
				}
			}

			if (this.renderHitBoxes && !entity.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
				renderHitbox(poseStack, multiBufferSource.getBuffer(RenderType.lines()), entity, h);
			}

			poseStack.popPose();
		} catch (Throwable var24) {
			CrashReport crashReport = CrashReport.forThrowable(var24, "Rendering entity in world");
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
		poseStack.mulPose(Vector3f.YP.rotationDegrees(-this.camera.getYRot()));
		poseStack.translate(0.0, 0.0, (double)(-0.3F + (float)((int)i) * 0.02F));
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
		if (entity instanceof Mob) {
			Mob mob = (Mob)entity;
			if (mob.isBaby()) {
				i = h * 0.5F;
			}
		}

		double d = Mth.lerp((double)g, entity.xOld, entity.getX());
		double e = Mth.lerp((double)g, entity.yOld, entity.getY());
		double j = Mth.lerp((double)g, entity.zOld, entity.getZ());
		int k = Mth.floor(d - (double)i);
		int l = Mth.floor(d + (double)i);
		int m = Mth.floor(e - (double)i);
		int n = Mth.floor(e);
		int o = Mth.floor(j - (double)i);
		int p = Mth.floor(j + (double)i);
		PoseStack.Pose pose = poseStack.last();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(SHADOW_RENDER_TYPE);

		for (BlockPos blockPos : BlockPos.betweenClosed(new BlockPos(k, m, o), new BlockPos(l, n, p))) {
			renderBlockShadow(pose, vertexConsumer, levelReader, blockPos, d, e, j, i, f);
		}
	}

	private static void renderBlockShadow(
		PoseStack.Pose pose, VertexConsumer vertexConsumer, LevelReader levelReader, BlockPos blockPos, double d, double e, double f, float g, float h
	) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState = levelReader.getBlockState(blockPos2);
		if (blockState.getRenderShape() != RenderShape.INVISIBLE && levelReader.getMaxLocalRawBrightness(blockPos) > 3) {
			if (blockState.isCollisionShapeFullBlock(levelReader, blockPos2)) {
				VoxelShape voxelShape = blockState.getShape(levelReader, blockPos.below());
				if (!voxelShape.isEmpty()) {
					float i = (float)(((double)h - (e - (double)blockPos.getY()) / 2.0) * 0.5 * (double)levelReader.getBrightness(blockPos));
					if (i >= 0.0F) {
						if (i > 1.0F) {
							i = 1.0F;
						}

						AABB aABB = voxelShape.bounds();
						double j = (double)blockPos.getX() + aABB.minX;
						double k = (double)blockPos.getX() + aABB.maxX;
						double l = (double)blockPos.getY() + aABB.minY;
						double m = (double)blockPos.getZ() + aABB.minZ;
						double n = (double)blockPos.getZ() + aABB.maxZ;
						float o = (float)(j - d);
						float p = (float)(k - d);
						float q = (float)(l - e);
						float r = (float)(m - f);
						float s = (float)(n - f);
						float t = -o / 2.0F / g + 0.5F;
						float u = -p / 2.0F / g + 0.5F;
						float v = -r / 2.0F / g + 0.5F;
						float w = -s / 2.0F / g + 0.5F;
						shadowVertex(pose, vertexConsumer, i, o, q, r, t, v);
						shadowVertex(pose, vertexConsumer, i, o, q, s, t, w);
						shadowVertex(pose, vertexConsumer, i, p, q, s, u, w);
						shadowVertex(pose, vertexConsumer, i, p, q, r, u, v);
					}
				}
			}
		}
	}

	private static void shadowVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k) {
		vertexConsumer.vertex(pose.pose(), g, h, i)
			.color(1.0F, 1.0F, 1.0F, f)
			.uv(j, k)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(15728880)
			.normal(pose.normal(), 0.0F, 1.0F, 0.0F)
			.endVertex();
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

	public Quaternion cameraOrientation() {
		return this.cameraOrientation;
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		EntityRendererProvider.Context context = new EntityRendererProvider.Context(this, this.itemRenderer, resourceManager, this.entityModels, this.font);
		this.renderers = EntityRenderers.createEntityRenderers(context);
		this.playerRenderers = EntityRenderers.createPlayerRenderers(context);
	}
}
