package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public abstract class EntityRenderer<T extends Entity, S extends EntityRenderState> {
	protected static final float NAMETAG_SCALE = 0.025F;
	public static final int LEASH_RENDER_STEPS = 24;
	protected final EntityRenderDispatcher entityRenderDispatcher;
	private final Font font;
	protected float shadowRadius;
	protected float shadowStrength = 1.0F;
	private final S reusedState = this.createRenderState();

	protected EntityRenderer(EntityRendererProvider.Context context) {
		this.entityRenderDispatcher = context.getEntityRenderDispatcher();
		this.font = context.getFont();
	}

	public final int getPackedLightCoords(T entity, float f) {
		BlockPos blockPos = BlockPos.containing(entity.getLightProbePosition(f));
		return LightTexture.pack(this.getBlockLightLevel(entity, blockPos), this.getSkyLightLevel(entity, blockPos));
	}

	protected int getSkyLightLevel(T entity, BlockPos blockPos) {
		return entity.level().getBrightness(LightLayer.SKY, blockPos);
	}

	protected int getBlockLightLevel(T entity, BlockPos blockPos) {
		return entity.isOnFire() ? 15 : entity.level().getBrightness(LightLayer.BLOCK, blockPos);
	}

	public boolean shouldRender(T entity, Frustum frustum, double d, double e, double f) {
		if (!entity.shouldRender(d, e, f)) {
			return false;
		} else if (!this.affectedByCulling(entity)) {
			return true;
		} else {
			AABB aABB = this.getBoundingBoxForCulling(entity).inflate(0.5);
			if (aABB.hasNaN() || aABB.getSize() == 0.0) {
				aABB = new AABB(entity.getX() - 2.0, entity.getY() - 2.0, entity.getZ() - 2.0, entity.getX() + 2.0, entity.getY() + 2.0, entity.getZ() + 2.0);
			}

			if (frustum.isVisible(aABB)) {
				return true;
			} else {
				if (entity instanceof Leashable leashable) {
					Entity entity2 = leashable.getLeashHolder();
					if (entity2 != null) {
						return frustum.isVisible(this.entityRenderDispatcher.getRenderer(entity2).getBoundingBoxForCulling(entity2));
					}
				}

				return false;
			}
		}
	}

	protected AABB getBoundingBoxForCulling(T entity) {
		return entity.getBoundingBox();
	}

	protected boolean affectedByCulling(T entity) {
		return true;
	}

	public Vec3 getRenderOffset(S entityRenderState) {
		return entityRenderState.passengerOffset != null ? entityRenderState.passengerOffset : Vec3.ZERO;
	}

	public void render(S entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		EntityRenderState.LeashState leashState = entityRenderState.leashState;
		if (leashState != null) {
			renderLeash(poseStack, multiBufferSource, leashState);
		}

		if (entityRenderState.nameTag != null) {
			this.renderNameTag(entityRenderState, entityRenderState.nameTag, poseStack, multiBufferSource, i);
		}
	}

	private static void renderLeash(PoseStack poseStack, MultiBufferSource multiBufferSource, EntityRenderState.LeashState leashState) {
		float f = 0.025F;
		float g = (float)(leashState.end.x - leashState.start.x);
		float h = (float)(leashState.end.y - leashState.start.y);
		float i = (float)(leashState.end.z - leashState.start.z);
		float j = Mth.invSqrt(g * g + i * i) * 0.025F / 2.0F;
		float k = i * j;
		float l = g * j;
		poseStack.pushPose();
		poseStack.translate(leashState.offset);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.leash());
		Matrix4f matrix4f = poseStack.last().pose();

		for (int m = 0; m <= 24; m++) {
			addVertexPair(
				vertexConsumer,
				matrix4f,
				g,
				h,
				i,
				leashState.startBlockLight,
				leashState.endBlockLight,
				leashState.startSkyLight,
				leashState.endSkyLight,
				0.025F,
				0.025F,
				k,
				l,
				m,
				false
			);
		}

		for (int m = 24; m >= 0; m--) {
			addVertexPair(
				vertexConsumer,
				matrix4f,
				g,
				h,
				i,
				leashState.startBlockLight,
				leashState.endBlockLight,
				leashState.startSkyLight,
				leashState.endSkyLight,
				0.025F,
				0.0F,
				k,
				l,
				m,
				true
			);
		}

		poseStack.popPose();
	}

	private static void addVertexPair(
		VertexConsumer vertexConsumer,
		Matrix4f matrix4f,
		float f,
		float g,
		float h,
		int i,
		int j,
		int k,
		int l,
		float m,
		float n,
		float o,
		float p,
		int q,
		boolean bl
	) {
		float r = (float)q / 24.0F;
		int s = (int)Mth.lerp(r, (float)i, (float)j);
		int t = (int)Mth.lerp(r, (float)k, (float)l);
		int u = LightTexture.pack(s, t);
		float v = q % 2 == (bl ? 1 : 0) ? 0.7F : 1.0F;
		float w = 0.5F * v;
		float x = 0.4F * v;
		float y = 0.3F * v;
		float z = f * r;
		float aa = g > 0.0F ? g * r * r : g - g * (1.0F - r) * (1.0F - r);
		float ab = h * r;
		vertexConsumer.addVertex(matrix4f, z - o, aa + n, ab + p).setColor(w, x, y, 1.0F).setLight(u);
		vertexConsumer.addVertex(matrix4f, z + o, aa + m - n, ab - p).setColor(w, x, y, 1.0F).setLight(u);
	}

	protected boolean shouldShowName(T entity, double d) {
		return entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity;
	}

	public Font getFont() {
		return this.font;
	}

	protected void renderNameTag(S entityRenderState, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		Vec3 vec3 = entityRenderState.nameTagAttachment;
		if (vec3 != null) {
			boolean bl = !entityRenderState.isDiscrete;
			int j = "deadmau5".equals(component.getString()) ? -10 : 0;
			poseStack.pushPose();
			poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
			poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
			poseStack.scale(0.025F, -0.025F, 0.025F);
			Matrix4f matrix4f = poseStack.last().pose();
			float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
			int k = (int)(f * 255.0F) << 24;
			Font font = this.getFont();
			float g = (float)(-font.width(component) / 2);
			font.drawInBatch(component, g, (float)j, -2130706433, false, matrix4f, multiBufferSource, bl ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, k, i);
			if (bl) {
				font.drawInBatch(component, g, (float)j, -1, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, i);
			}

			poseStack.popPose();
		}
	}

	@Nullable
	protected Component getNameTag(T entity) {
		return entity.getDisplayName();
	}

	protected float getShadowRadius(S entityRenderState) {
		return this.shadowRadius;
	}

	public abstract S createRenderState();

	public final S createRenderState(T entity, float f) {
		S entityRenderState = this.reusedState;
		this.extractRenderState(entity, entityRenderState, f);
		return entityRenderState;
	}

	public void extractRenderState(T entity, S entityRenderState, float f) {
		entityRenderState.x = Mth.lerp((double)f, entity.xOld, entity.getX());
		entityRenderState.y = Mth.lerp((double)f, entity.yOld, entity.getY());
		entityRenderState.z = Mth.lerp((double)f, entity.zOld, entity.getZ());
		entityRenderState.isInvisible = entity.isInvisible();
		entityRenderState.ageInTicks = (float)entity.tickCount + f;
		entityRenderState.boundingBoxWidth = entity.getBbWidth();
		entityRenderState.boundingBoxHeight = entity.getBbHeight();
		entityRenderState.eyeHeight = entity.getEyeHeight();
		if (entity.isPassenger()
			&& entity.getVehicle() instanceof AbstractMinecart abstractMinecart
			&& abstractMinecart.getBehavior() instanceof NewMinecartBehavior newMinecartBehavior
			&& newMinecartBehavior.cartHasPosRotLerp()) {
			double d = Mth.lerp((double)f, abstractMinecart.xOld, abstractMinecart.getX());
			double e = Mth.lerp((double)f, abstractMinecart.yOld, abstractMinecart.getY());
			double g = Mth.lerp((double)f, abstractMinecart.zOld, abstractMinecart.getZ());
			entityRenderState.passengerOffset = newMinecartBehavior.getCartLerpPosition(f).subtract(new Vec3(d, e, g));
		} else {
			entityRenderState.passengerOffset = null;
		}

		entityRenderState.distanceToCameraSq = this.entityRenderDispatcher.distanceToSqr(entity);
		boolean bl = entityRenderState.distanceToCameraSq < 4096.0 && this.shouldShowName(entity, entityRenderState.distanceToCameraSq);
		if (bl) {
			entityRenderState.nameTag = this.getNameTag(entity);
			entityRenderState.nameTagAttachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(f));
		} else {
			entityRenderState.nameTag = null;
		}

		entityRenderState.isDiscrete = entity.isDiscrete();
		Entity entity2 = entity instanceof Leashable leashable ? leashable.getLeashHolder() : null;
		if (entity2 != null) {
			float h = entity.getPreciseBodyRotation(f) * (float) (Math.PI / 180.0);
			Vec3 vec3 = entity.getLeashOffset(f).yRot(-h);
			BlockPos blockPos = BlockPos.containing(entity.getEyePosition(f));
			BlockPos blockPos2 = BlockPos.containing(entity2.getEyePosition(f));
			if (entityRenderState.leashState == null) {
				entityRenderState.leashState = new EntityRenderState.LeashState();
			}

			EntityRenderState.LeashState leashState = entityRenderState.leashState;
			leashState.offset = vec3;
			leashState.start = entity.getPosition(f).add(vec3);
			leashState.end = entity2.getRopeHoldPosition(f);
			leashState.startBlockLight = this.getBlockLightLevel(entity, blockPos);
			leashState.endBlockLight = this.entityRenderDispatcher.getRenderer(entity2).getBlockLightLevel(entity2, blockPos2);
			leashState.startSkyLight = entity.level().getBrightness(LightLayer.SKY, blockPos);
			leashState.endSkyLight = entity.level().getBrightness(LightLayer.SKY, blockPos2);
		} else {
			entityRenderState.leashState = null;
		}

		entityRenderState.displayFireAnimation = entity.displayFireAnimation();
	}
}
