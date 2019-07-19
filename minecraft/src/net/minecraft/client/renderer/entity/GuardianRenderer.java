package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class GuardianRenderer extends MobRenderer<Guardian, GuardianModel> {
	private static final ResourceLocation GUARDIAN_LOCATION = new ResourceLocation("textures/entity/guardian.png");
	private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");

	public GuardianRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		this(entityRenderDispatcher, 0.5F);
	}

	protected GuardianRenderer(EntityRenderDispatcher entityRenderDispatcher, float f) {
		super(entityRenderDispatcher, new GuardianModel(), f);
	}

	public boolean shouldRender(Guardian guardian, Culler culler, double d, double e, double f) {
		if (super.shouldRender(guardian, culler, d, e, f)) {
			return true;
		} else {
			if (guardian.hasActiveAttackTarget()) {
				LivingEntity livingEntity = guardian.getActiveAttackTarget();
				if (livingEntity != null) {
					Vec3 vec3 = this.getPosition(livingEntity, (double)livingEntity.getBbHeight() * 0.5, 1.0F);
					Vec3 vec32 = this.getPosition(guardian, (double)guardian.getEyeHeight(), 1.0F);
					if (culler.isVisible(new AABB(vec32.x, vec32.y, vec32.z, vec3.x, vec3.y, vec3.z))) {
						return true;
					}
				}
			}

			return false;
		}
	}

	private Vec3 getPosition(LivingEntity livingEntity, double d, float f) {
		double e = Mth.lerp((double)f, livingEntity.xOld, livingEntity.x);
		double g = Mth.lerp((double)f, livingEntity.yOld, livingEntity.y) + d;
		double h = Mth.lerp((double)f, livingEntity.zOld, livingEntity.z);
		return new Vec3(e, g, h);
	}

	public void render(Guardian guardian, double d, double e, double f, float g, float h) {
		super.render(guardian, d, e, f, g, h);
		LivingEntity livingEntity = guardian.getActiveAttackTarget();
		if (livingEntity != null) {
			float i = guardian.getAttackAnimationScale(h);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			this.bindTexture(GUARDIAN_BEAM_LOCATION);
			GlStateManager.texParameter(3553, 10242, 10497);
			GlStateManager.texParameter(3553, 10243, 10497);
			GlStateManager.disableLighting();
			GlStateManager.disableCull();
			GlStateManager.disableBlend();
			GlStateManager.depthMask(true);
			float j = 240.0F;
			GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240.0F, 240.0F);
			GlStateManager.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
			float k = (float)guardian.level.getGameTime() + h;
			float l = k * 0.5F % 1.0F;
			float m = guardian.getEyeHeight();
			GlStateManager.pushMatrix();
			GlStateManager.translatef((float)d, (float)e + m, (float)f);
			Vec3 vec3 = this.getPosition(livingEntity, (double)livingEntity.getBbHeight() * 0.5, h);
			Vec3 vec32 = this.getPosition(guardian, (double)m, h);
			Vec3 vec33 = vec3.subtract(vec32);
			double n = vec33.length() + 1.0;
			vec33 = vec33.normalize();
			float o = (float)Math.acos(vec33.y);
			float p = (float)Math.atan2(vec33.z, vec33.x);
			GlStateManager.rotatef(((float) (Math.PI / 2) - p) * (180.0F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
			GlStateManager.rotatef(o * (180.0F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
			int q = 1;
			double r = (double)k * 0.05 * -1.5;
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			float s = i * i;
			int t = 64 + (int)(s * 191.0F);
			int u = 32 + (int)(s * 191.0F);
			int v = 128 - (int)(s * 64.0F);
			double w = 0.2;
			double x = 0.282;
			double y = 0.0 + Math.cos(r + (Math.PI * 3.0 / 4.0)) * 0.282;
			double z = 0.0 + Math.sin(r + (Math.PI * 3.0 / 4.0)) * 0.282;
			double aa = 0.0 + Math.cos(r + (Math.PI / 4)) * 0.282;
			double ab = 0.0 + Math.sin(r + (Math.PI / 4)) * 0.282;
			double ac = 0.0 + Math.cos(r + (Math.PI * 5.0 / 4.0)) * 0.282;
			double ad = 0.0 + Math.sin(r + (Math.PI * 5.0 / 4.0)) * 0.282;
			double ae = 0.0 + Math.cos(r + (Math.PI * 7.0 / 4.0)) * 0.282;
			double af = 0.0 + Math.sin(r + (Math.PI * 7.0 / 4.0)) * 0.282;
			double ag = 0.0 + Math.cos(r + Math.PI) * 0.2;
			double ah = 0.0 + Math.sin(r + Math.PI) * 0.2;
			double ai = 0.0 + Math.cos(r + 0.0) * 0.2;
			double aj = 0.0 + Math.sin(r + 0.0) * 0.2;
			double ak = 0.0 + Math.cos(r + (Math.PI / 2)) * 0.2;
			double al = 0.0 + Math.sin(r + (Math.PI / 2)) * 0.2;
			double am = 0.0 + Math.cos(r + (Math.PI * 3.0 / 2.0)) * 0.2;
			double an = 0.0 + Math.sin(r + (Math.PI * 3.0 / 2.0)) * 0.2;
			double ap = 0.0;
			double aq = 0.4999;
			double ar = (double)(-1.0F + l);
			double as = n * 2.5 + ar;
			bufferBuilder.vertex(ag, n, ah).uv(0.4999, as).color(t, u, v, 255).endVertex();
			bufferBuilder.vertex(ag, 0.0, ah).uv(0.4999, ar).color(t, u, v, 255).endVertex();
			bufferBuilder.vertex(ai, 0.0, aj).uv(0.0, ar).color(t, u, v, 255).endVertex();
			bufferBuilder.vertex(ai, n, aj).uv(0.0, as).color(t, u, v, 255).endVertex();
			bufferBuilder.vertex(ak, n, al).uv(0.4999, as).color(t, u, v, 255).endVertex();
			bufferBuilder.vertex(ak, 0.0, al).uv(0.4999, ar).color(t, u, v, 255).endVertex();
			bufferBuilder.vertex(am, 0.0, an).uv(0.0, ar).color(t, u, v, 255).endVertex();
			bufferBuilder.vertex(am, n, an).uv(0.0, as).color(t, u, v, 255).endVertex();
			double at = 0.0;
			if (guardian.tickCount % 2 == 0) {
				at = 0.5;
			}

			bufferBuilder.vertex(y, n, z).uv(0.5, at + 0.5).color(t, u, v, 255).endVertex();
			bufferBuilder.vertex(aa, n, ab).uv(1.0, at + 0.5).color(t, u, v, 255).endVertex();
			bufferBuilder.vertex(ae, n, af).uv(1.0, at).color(t, u, v, 255).endVertex();
			bufferBuilder.vertex(ac, n, ad).uv(0.5, at).color(t, u, v, 255).endVertex();
			tesselator.end();
			GlStateManager.popMatrix();
		}
	}

	protected ResourceLocation getTextureLocation(Guardian guardian) {
		return GUARDIAN_LOCATION;
	}
}
