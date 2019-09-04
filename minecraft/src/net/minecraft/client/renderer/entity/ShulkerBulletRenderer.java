package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;

@Environment(EnvType.CLIENT)
public class ShulkerBulletRenderer extends EntityRenderer<ShulkerBullet> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/shulker/spark.png");
	private final ShulkerBulletModel<ShulkerBullet> model = new ShulkerBulletModel<>();

	public ShulkerBulletRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	private float rotlerp(float f, float g, float h) {
		float i = g - f;

		while (i < -180.0F) {
			i += 360.0F;
		}

		while (i >= 180.0F) {
			i -= 360.0F;
		}

		return f + h * i;
	}

	public void render(ShulkerBullet shulkerBullet, double d, double e, double f, float g, float h) {
		RenderSystem.pushMatrix();
		float i = this.rotlerp(shulkerBullet.yRotO, shulkerBullet.yRot, h);
		float j = Mth.lerp(h, shulkerBullet.xRotO, shulkerBullet.xRot);
		float k = (float)shulkerBullet.tickCount + h;
		RenderSystem.translatef((float)d, (float)e + 0.15F, (float)f);
		RenderSystem.rotatef(Mth.sin(k * 0.1F) * 180.0F, 0.0F, 1.0F, 0.0F);
		RenderSystem.rotatef(Mth.cos(k * 0.1F) * 180.0F, 1.0F, 0.0F, 0.0F);
		RenderSystem.rotatef(Mth.sin(k * 0.15F) * 360.0F, 0.0F, 0.0F, 1.0F);
		float l = 0.03125F;
		RenderSystem.enableRescaleNormal();
		RenderSystem.scalef(-1.0F, -1.0F, 1.0F);
		this.bindTexture(shulkerBullet);
		this.model.render(shulkerBullet, 0.0F, 0.0F, 0.0F, i, j, 0.03125F);
		RenderSystem.enableBlend();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.5F);
		RenderSystem.scalef(1.5F, 1.5F, 1.5F);
		this.model.render(shulkerBullet, 0.0F, 0.0F, 0.0F, i, j, 0.03125F);
		RenderSystem.disableBlend();
		RenderSystem.popMatrix();
		super.render(shulkerBullet, d, e, f, g, h);
	}

	protected ResourceLocation getTextureLocation(ShulkerBullet shulkerBullet) {
		return TEXTURE_LOCATION;
	}
}
