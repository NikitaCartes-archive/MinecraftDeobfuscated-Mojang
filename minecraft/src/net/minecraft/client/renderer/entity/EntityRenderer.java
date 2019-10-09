package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
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

	public Vec3 getRenderOffset(T entity, double d, double e, double f, float g) {
		return Vec3.ZERO;
	}

	public void render(T entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		if (this.shouldShowName(entity)) {
			this.renderNameTag(entity, entity.getDisplayName().getColoredString(), poseStack, multiBufferSource);
		}
	}

	protected boolean shouldShowName(T entity) {
		return entity.shouldShowName() && entity.hasCustomName();
	}

	public abstract ResourceLocation getTextureLocation(T entity);

	public Font getFont() {
		return this.entityRenderDispatcher.getFont();
	}

	protected void renderNameTag(T entity, String string, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		double d = this.entityRenderDispatcher.distanceToSqr(entity);
		if (!(d > 4096.0)) {
			int i = entity.getLightColor();
			if (entity.isOnFire()) {
				i = 15728880;
			}

			boolean bl = !entity.isDiscrete();
			float f = entity.getBbHeight() + 0.5F;
			int j = "deadmau5".equals(string) ? -10 : 0;
			poseStack.pushPose();
			poseStack.translate(0.0, (double)f, 0.0);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(-this.entityRenderDispatcher.playerRotY));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(this.entityRenderDispatcher.playerRotX));
			poseStack.scale(-0.025F, -0.025F, 0.025F);
			Matrix4f matrix4f = poseStack.getPose();
			float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
			int k = (int)(g * 255.0F) << 24;
			Font font = this.getFont();
			float h = (float)(-font.width(string) / 2);
			font.drawInBatch(string, h, (float)j, 553648127, false, matrix4f, multiBufferSource, bl, k, i);
			if (bl) {
				font.drawInBatch(string, h, (float)j, -1, false, matrix4f, multiBufferSource, false, 0, i);
			}

			poseStack.popPose();
		}
	}

	public EntityRenderDispatcher getDispatcher() {
		return this.entityRenderDispatcher;
	}
}
