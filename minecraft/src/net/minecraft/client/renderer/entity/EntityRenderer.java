package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
	protected final EntityRenderDispatcher entityRenderDispatcher;
	protected float shadowRadius;
	protected float shadowStrength = 1.0F;

	protected EntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		this.entityRenderDispatcher = entityRenderDispatcher;
	}

	public final int getPackedLightCoords(T entity, float f) {
		BlockPos blockPos = new BlockPos(entity.getEyePosition(f));
		return LightTexture.pack(this.getBlockLightLevel(entity, blockPos), this.getSkyLightLevel(entity, blockPos));
	}

	protected int getSkyLightLevel(T entity, BlockPos blockPos) {
		return entity.level.getBrightness(LightLayer.SKY, blockPos);
	}

	protected int getBlockLightLevel(T entity, BlockPos blockPos) {
		return entity.isOnFire() ? 15 : entity.level.getBrightness(LightLayer.BLOCK, blockPos);
	}

	public boolean shouldRender(T entity, Frustum frustum, double d, double e, double f) {
		if (!entity.shouldRender(d, e, f)) {
			return false;
		} else if (entity.noCulling) {
			return true;
		} else {
			AABB aABB = entity.getBoundingBoxForCulling().inflate(0.5);
			if (aABB.hasNaN() || aABB.getSize() == 0.0) {
				aABB = new AABB(entity.getX() - 2.0, entity.getY() - 2.0, entity.getZ() - 2.0, entity.getX() + 2.0, entity.getY() + 2.0, entity.getZ() + 2.0);
			}

			return frustum.isVisible(aABB);
		}
	}

	public Vec3 getRenderOffset(T entity, float f) {
		return Vec3.ZERO;
	}

	public void render(T entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (this.shouldShowName(entity)) {
			this.renderNameTag(entity, entity.getDisplayName(), poseStack, multiBufferSource, i);
		}
	}

	protected boolean shouldShowName(T entity) {
		return entity.shouldShowName() && entity.hasCustomName();
	}

	public abstract ResourceLocation getTextureLocation(T entity);

	public Font getFont() {
		return this.entityRenderDispatcher.getFont();
	}

	protected void renderNameTag(T entity, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		double d = this.entityRenderDispatcher.distanceToSqr(entity);
		if (!(d > 4096.0)) {
			boolean bl = !entity.isDiscrete();
			float f = entity.getBbHeight() + 0.5F;
			int j = "deadmau5".equals(component.getString()) ? -10 : 0;
			poseStack.pushPose();
			poseStack.translate(0.0, (double)f, 0.0);
			poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
			poseStack.scale(-0.025F, -0.025F, 0.025F);
			Matrix4f matrix4f = poseStack.last().pose();
			float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
			int k = (int)(g * 255.0F) << 24;
			Font font = this.getFont();
			float h = (float)(-font.width(component) / 2);
			font.drawInBatch(component, h, (float)j, 553648127, false, matrix4f, multiBufferSource, bl, k, i);
			if (bl) {
				font.drawInBatch(component, h, (float)j, -1, false, matrix4f, multiBufferSource, false, 0, i);
			}

			poseStack.popPose();
		}
	}

	public EntityRenderDispatcher getDispatcher() {
		return this.entityRenderDispatcher;
	}
}
