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
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

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
			this.renderLeash(mob, g, poseStack, multiBufferSource, entity);
		}
	}

	private <E extends Entity> void renderLeash(T mob, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, E entity) {
		poseStack.pushPose();
		Vec3 vec3 = entity.getRopeHoldPosition(f);
		double d = (double)(Mth.lerp(f, mob.yBodyRot, mob.yBodyRotO) * (float) (Math.PI / 180.0)) + (Math.PI / 2);
		Vec3 vec32 = mob.getLeashOffset();
		double e = Math.cos(d) * vec32.z + Math.sin(d) * vec32.x;
		double g = Math.sin(d) * vec32.z - Math.cos(d) * vec32.x;
		double h = Mth.lerp((double)f, mob.xo, mob.getX()) + e;
		double i = Mth.lerp((double)f, mob.yo, mob.getY()) + vec32.y;
		double j = Mth.lerp((double)f, mob.zo, mob.getZ()) + g;
		poseStack.translate(e, vec32.y, g);
		float k = (float)(vec3.x - h);
		float l = (float)(vec3.y - i);
		float m = (float)(vec3.z - j);
		float n = 0.025F;
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.leash());
		Matrix4f matrix4f = poseStack.last().pose();
		float o = Mth.fastInvSqrt(k * k + m * m) * 0.025F / 2.0F;
		float p = m * o;
		float q = k * o;
		BlockPos blockPos = new BlockPos(mob.getEyePosition(f));
		BlockPos blockPos2 = new BlockPos(entity.getEyePosition(f));
		int r = this.getBlockLightLevel(mob, blockPos);
		int s = this.entityRenderDispatcher.getRenderer(entity).getBlockLightLevel(entity, blockPos2);
		int t = mob.level.getBrightness(LightLayer.SKY, blockPos);
		int u = mob.level.getBrightness(LightLayer.SKY, blockPos2);
		renderSide(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.025F, p, q);
		renderSide(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.0F, p, q);
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
		float u = g > 0.0F ? g * s * s : g - g * (1.0F - s) * (1.0F - s);
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
