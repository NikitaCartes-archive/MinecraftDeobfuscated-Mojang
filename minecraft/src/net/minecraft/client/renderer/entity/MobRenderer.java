package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.LightLayer;

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

	public void render(T mob, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		super.render(mob, f, g, poseStack, multiBufferSource, i);
		Entity entity = mob.getLeashHolder();
		if (entity != null) {
			renderLeash(mob, g, poseStack, multiBufferSource, entity);
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
		Matrix4f matrix4f = poseStack.last().pose();
		float v = Mth.fastInvSqrt(r * r + t * t) * 0.025F / 2.0F;
		float w = t * v;
		float x = r * v;
		int y = mob.getBlockLightLevel();
		int z = entity.getBlockLightLevel();
		int aa = mob.level.getBrightness(LightLayer.SKY, new BlockPos(mob));
		int ab = mob.level.getBrightness(LightLayer.SKY, new BlockPos(entity));
		renderSide(vertexConsumer, matrix4f, r, s, t, y, z, aa, ab, 0.025F, 0.025F, w, x);
		renderSide(vertexConsumer, matrix4f, r, s, t, y, z, aa, ab, 0.025F, 0.0F, w, x);
		poseStack.popPose();
	}

	public static void renderSide(
		VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, int i, int j, int k, int l, float m, float n, float o, float p
	) {
		int q = 24;

		for (int r = 0; r < 24; r++) {
			float s = (float)r / 23.0F;
			int t = (int)Mth.lerp(s, (float)i, (float)j);
			int u = (int)Mth.lerp(s, (float)k, (float)l);
			int v = LightTexture.pack(t, u);
			addVertexPair(vertexConsumer, matrix4f, v, f, g, h, m, n, 24, r, false, o, p);
			addVertexPair(vertexConsumer, matrix4f, v, f, g, h, m, n, 24, r + 1, true, o, p);
		}
	}

	public static void addVertexPair(
		VertexConsumer vertexConsumer, Matrix4f matrix4f, int i, float f, float g, float h, float j, float k, int l, int m, boolean bl, float n, float o
	) {
		float p = 0.5F;
		float q = 0.4F;
		float r = 0.3F;
		if (m % 2 == 0) {
			p *= 0.7F;
			q *= 0.7F;
			r *= 0.7F;
		}

		float s = (float)m / (float)l;
		float t = f * s;
		float u = g * (s * s + s) * 0.5F + ((float)l - (float)m) / ((float)l * 0.75F) + 0.125F;
		float v = h * s;
		if (!bl) {
			vertexConsumer.vertex(matrix4f, t + n, u + j - k, v - o).color(p, q, r, 1.0F).uv2(i).endVertex();
		}

		vertexConsumer.vertex(matrix4f, t - n, u + k, v + o).color(p, q, r, 1.0F).uv2(i).endVertex();
		if (bl) {
			vertexConsumer.vertex(matrix4f, t + n, u + j - k, v - o).color(p, q, r, 1.0F).uv2(i).endVertex();
		}
	}
}
