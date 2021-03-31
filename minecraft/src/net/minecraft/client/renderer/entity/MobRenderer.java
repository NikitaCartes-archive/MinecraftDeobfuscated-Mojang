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
	public static final int LEASH_RENDER_STEPS = 24;

	public MobRenderer(EntityRendererProvider.Context context, M entityModel, float f) {
		super(context, entityModel, f);
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

		for (int v = 0; v <= 24; v++) {
			addVertexPair(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.025F, p, q, v, false);
		}

		for (int v = 24; v >= 0; v--) {
			addVertexPair(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.0F, p, q, v, true);
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
		vertexConsumer.vertex(matrix4f, z - o, aa + n, ab + p).color(w, x, y, 1.0F).uv2(u).endVertex();
		vertexConsumer.vertex(matrix4f, z + o, aa + m - n, ab - p).color(w, x, y, 1.0F).uv2(u).endVertex();
	}
}
