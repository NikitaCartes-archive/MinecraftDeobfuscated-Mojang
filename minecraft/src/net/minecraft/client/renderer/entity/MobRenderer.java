package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.HangingEntity;

@Environment(EnvType.CLIENT)
public abstract class MobRenderer<T extends Mob, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
	public MobRenderer(EntityRenderDispatcher entityRenderDispatcher, M entityModel, float f) {
		super(entityRenderDispatcher, entityModel, f);
	}

	protected boolean shouldShowName(T mob) {
		return super.shouldShowName(mob) && (mob.shouldShowName() || mob.hasCustomName() && mob == this.entityRenderDispatcher.crosshairPickEntity);
	}

	public boolean shouldRender(T mob, Frustum frustum, double d, double e, double f) {
		if (super.shouldRender(mob, frustum, d, e, f)) {
			return true;
		} else {
			Entity entity = mob.getLeashHolder();
			return entity != null ? frustum.isVisible(entity.getBoundingBoxForCulling()) : false;
		}
	}

	public void render(T mob, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		super.render(mob, d, e, f, g, h, poseStack, multiBufferSource);
		Entity entity = mob.getLeashHolder();
		if (entity != null) {
			renderLeash(mob, h, poseStack, multiBufferSource, entity);
		}
	}

	public static void renderLeash(Mob mob, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity) {
		poseStack.pushPose();
		double d = (double)(Mth.lerp(f * 0.5F, entity.yRot, entity.yRotO) * (float) (Math.PI / 180.0));
		double e = (double)(Mth.lerp(f * 0.5F, entity.xRot, entity.xRotO) * (float) (Math.PI / 180.0));
		double g = Math.cos(d);
		double h = Math.sin(d);
		double i = Math.sin(e);
		if (entity instanceof HangingEntity) {
			g = 0.0;
			h = 0.0;
			i = -1.0;
		}

		double j = Math.cos(e);
		double k = Mth.lerp((double)f, entity.xo, entity.getX()) - g * 0.7 - h * 0.5 * j;
		double l = Mth.lerp((double)f, entity.yo + (double)entity.getEyeHeight() * 0.7, entity.getY() + (double)entity.getEyeHeight() * 0.7) - i * 0.5 - 0.25;
		double m = Mth.lerp((double)f, entity.zo, entity.getZ()) - h * 0.7 + g * 0.5 * j;
		double n = (double)(Mth.lerp(f, mob.yBodyRot, mob.yBodyRotO) * (float) (Math.PI / 180.0)) + (Math.PI / 2);
		g = Math.cos(n) * (double)mob.getBbWidth() * 0.4;
		h = Math.sin(n) * (double)mob.getBbWidth() * 0.4;
		double o = Mth.lerp((double)f, mob.xo, mob.getX()) + g;
		double p = Mth.lerp((double)f, mob.yo, mob.getY());
		double q = Mth.lerp((double)f, mob.zo, mob.getZ()) + h;
		poseStack.translate(g, -(1.6 - (double)mob.getBbHeight()) * 0.5, h);
		float r = (float)(k - o);
		float s = (float)(l - p);
		float t = (float)(m - q);
		float u = 0.025F;
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.leash());
		Matrix4f matrix4f = poseStack.getPose();
		float v = Mth.fastInvSqrt(r * r + t * t) * 0.025F / 2.0F;
		float w = t * v;
		float x = r * v;
		renderSide(vertexConsumer, matrix4f, r, s, t, 0.025F, 0.025F, w, x);
		renderSide(vertexConsumer, matrix4f, r, s, t, 0.025F, 0.0F, w, x);
		poseStack.popPose();
	}

	public static void renderSide(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, float i, float j, float k, float l) {
		int m = 24;

		for (int n = 0; n < 24; n++) {
			addVertexPair(vertexConsumer, matrix4f, f, g, h, i, j, 24, n, false, k, l);
			addVertexPair(vertexConsumer, matrix4f, f, g, h, i, j, 24, n + 1, true, k, l);
		}
	}

	public static void addVertexPair(
		VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, float i, float j, int k, int l, boolean bl, float m, float n
	) {
		float o = 0.5F;
		float p = 0.4F;
		float q = 0.3F;
		if (l % 2 == 0) {
			o *= 0.7F;
			p *= 0.7F;
			q *= 0.7F;
		}

		float r = (float)l / (float)k;
		float s = f * r;
		float t = g * (r * r + r) * 0.5F + ((float)k - (float)l) / ((float)k * 0.75F) + 0.125F;
		float u = h * r;
		if (!bl) {
			vertexConsumer.vertex(matrix4f, s + m, t + i - j, u - n).color(o, p, q, 1.0F).endVertex();
		}

		vertexConsumer.vertex(matrix4f, s - m, t + j, u + n).color(o, p, q, 1.0F).endVertex();
		if (bl) {
			vertexConsumer.vertex(matrix4f, s + m, t + i - j, u - n).color(o, p, q, 1.0F).endVertex();
		}
	}
}
