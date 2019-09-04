package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.culling.Culler;
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

	public boolean shouldRender(T mob, Culler culler, double d, double e, double f) {
		if (super.shouldRender(mob, culler, d, e, f)) {
			return true;
		} else {
			Entity entity = mob.getLeashHolder();
			return entity != null ? culler.isVisible(entity.getBoundingBoxForCulling()) : false;
		}
	}

	public void render(T mob, double d, double e, double f, float g, float h) {
		super.render(mob, d, e, f, g, h);
		if (!this.solidRender) {
			this.renderLeash(mob, d, e, f, g, h);
		}
	}

	protected void renderLeash(T mob, double d, double e, double f, float g, float h) {
		Entity entity = mob.getLeashHolder();
		if (entity != null) {
			e -= (1.6 - (double)mob.getBbHeight()) * 0.5;
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			double i = (double)(Mth.lerp(h * 0.5F, entity.yRot, entity.yRotO) * (float) (Math.PI / 180.0));
			double j = (double)(Mth.lerp(h * 0.5F, entity.xRot, entity.xRotO) * (float) (Math.PI / 180.0));
			double k = Math.cos(i);
			double l = Math.sin(i);
			double m = Math.sin(j);
			if (entity instanceof HangingEntity) {
				k = 0.0;
				l = 0.0;
				m = -1.0;
			}

			double n = Math.cos(j);
			double o = Mth.lerp((double)h, entity.xo, entity.x) - k * 0.7 - l * 0.5 * n;
			double p = Mth.lerp((double)h, entity.yo + (double)entity.getEyeHeight() * 0.7, entity.y + (double)entity.getEyeHeight() * 0.7) - m * 0.5 - 0.25;
			double q = Mth.lerp((double)h, entity.zo, entity.z) - l * 0.7 + k * 0.5 * n;
			double r = (double)(Mth.lerp(h, mob.yBodyRot, mob.yBodyRotO) * (float) (Math.PI / 180.0)) + (Math.PI / 2);
			k = Math.cos(r) * (double)mob.getBbWidth() * 0.4;
			l = Math.sin(r) * (double)mob.getBbWidth() * 0.4;
			double s = Mth.lerp((double)h, mob.xo, mob.x) + k;
			double t = Mth.lerp((double)h, mob.yo, mob.y);
			double u = Mth.lerp((double)h, mob.zo, mob.z) + l;
			d += k;
			f += l;
			double v = (double)((float)(o - s));
			double w = (double)((float)(p - t));
			double x = (double)((float)(q - u));
			RenderSystem.disableTexture();
			RenderSystem.disableLighting();
			RenderSystem.disableCull();
			int y = 24;
			double z = 0.025;
			bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);

			for (int aa = 0; aa <= 24; aa++) {
				float ab = 0.5F;
				float ac = 0.4F;
				float ad = 0.3F;
				if (aa % 2 == 0) {
					ab *= 0.7F;
					ac *= 0.7F;
					ad *= 0.7F;
				}

				float ae = (float)aa / 24.0F;
				bufferBuilder.vertex(d + v * (double)ae + 0.0, e + w * (double)(ae * ae + ae) * 0.5 + (double)((24.0F - (float)aa) / 18.0F + 0.125F), f + x * (double)ae)
					.color(ab, ac, ad, 1.0F)
					.endVertex();
				bufferBuilder.vertex(
						d + v * (double)ae + 0.025, e + w * (double)(ae * ae + ae) * 0.5 + (double)((24.0F - (float)aa) / 18.0F + 0.125F) + 0.025, f + x * (double)ae
					)
					.color(ab, ac, ad, 1.0F)
					.endVertex();
			}

			tesselator.end();
			bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);

			for (int aa = 0; aa <= 24; aa++) {
				float ab = 0.5F;
				float ac = 0.4F;
				float ad = 0.3F;
				if (aa % 2 == 0) {
					ab *= 0.7F;
					ac *= 0.7F;
					ad *= 0.7F;
				}

				float ae = (float)aa / 24.0F;
				bufferBuilder.vertex(
						d + v * (double)ae + 0.0, e + w * (double)(ae * ae + ae) * 0.5 + (double)((24.0F - (float)aa) / 18.0F + 0.125F) + 0.025, f + x * (double)ae
					)
					.color(ab, ac, ad, 1.0F)
					.endVertex();
				bufferBuilder.vertex(
						d + v * (double)ae + 0.025, e + w * (double)(ae * ae + ae) * 0.5 + (double)((24.0F - (float)aa) / 18.0F + 0.125F), f + x * (double)ae + 0.025
					)
					.color(ab, ac, ad, 1.0F)
					.endVertex();
			}

			tesselator.end();
			RenderSystem.enableLighting();
			RenderSystem.enableTexture();
			RenderSystem.enableCull();
		}
	}
}
