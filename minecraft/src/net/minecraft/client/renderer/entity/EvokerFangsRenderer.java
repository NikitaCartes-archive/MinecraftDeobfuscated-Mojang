package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;

@Environment(EnvType.CLIENT)
public class EvokerFangsRenderer extends EntityRenderer<EvokerFangs> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/evoker_fangs.png");
	private final EvokerFangsModel<EvokerFangs> model = new EvokerFangsModel<>();

	public EvokerFangsRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(EvokerFangs evokerFangs, double d, double e, double f, float g, float h) {
		float i = evokerFangs.getAnimationProgress(h);
		if (i != 0.0F) {
			float j = 2.0F;
			if (i > 0.9F) {
				j = (float)((double)j * ((1.0 - (double)i) / 0.1F));
			}

			RenderSystem.pushMatrix();
			RenderSystem.disableCull();
			RenderSystem.enableAlphaTest();
			this.bindTexture(evokerFangs);
			RenderSystem.translatef((float)d, (float)e, (float)f);
			RenderSystem.rotatef(90.0F - evokerFangs.yRot, 0.0F, 1.0F, 0.0F);
			RenderSystem.scalef(-j, -j, j);
			float k = 0.03125F;
			RenderSystem.translatef(0.0F, -0.626F, 0.0F);
			this.model.render(evokerFangs, i, 0.0F, 0.0F, evokerFangs.yRot, evokerFangs.xRot, 0.03125F);
			RenderSystem.popMatrix();
			RenderSystem.enableCull();
			super.render(evokerFangs, d, e, f, g, h);
		}
	}

	protected ResourceLocation getTextureLocation(EvokerFangs evokerFangs) {
		return TEXTURE_LOCATION;
	}
}
