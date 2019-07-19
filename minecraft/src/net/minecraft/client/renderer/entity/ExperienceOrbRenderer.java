package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;

@Environment(EnvType.CLIENT)
public class ExperienceOrbRenderer extends EntityRenderer<ExperienceOrb> {
	private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");

	public ExperienceOrbRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.15F;
		this.shadowStrength = 0.75F;
	}

	public void render(ExperienceOrb experienceOrb, double d, double e, double f, float g, float h) {
		if (!this.solidRender && Minecraft.getInstance().getEntityRenderDispatcher().options != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef((float)d, (float)e, (float)f);
			this.bindTexture(experienceOrb);
			Lighting.turnOn();
			int i = experienceOrb.getIcon();
			float j = (float)(i % 4 * 16 + 0) / 64.0F;
			float k = (float)(i % 4 * 16 + 16) / 64.0F;
			float l = (float)(i / 4 * 16 + 0) / 64.0F;
			float m = (float)(i / 4 * 16 + 16) / 64.0F;
			float n = 1.0F;
			float o = 0.5F;
			float p = 0.25F;
			int q = experienceOrb.getLightColor();
			int r = q % 65536;
			int s = q / 65536;
			GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)r, (float)s);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			float t = 255.0F;
			float u = ((float)experienceOrb.tickCount + h) / 2.0F;
			int v = (int)((Mth.sin(u + 0.0F) + 1.0F) * 0.5F * 255.0F);
			int w = 255;
			int x = (int)((Mth.sin(u + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * 255.0F);
			GlStateManager.translatef(0.0F, 0.1F, 0.0F);
			GlStateManager.rotatef(180.0F - this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotatef(
				(float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX, 1.0F, 0.0F, 0.0F
			);
			float y = 0.3F;
			GlStateManager.scalef(0.3F, 0.3F, 0.3F);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
			bufferBuilder.vertex(-0.5, -0.25, 0.0).uv((double)j, (double)m).color(v, 255, x, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
			bufferBuilder.vertex(0.5, -0.25, 0.0).uv((double)k, (double)m).color(v, 255, x, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
			bufferBuilder.vertex(0.5, 0.75, 0.0).uv((double)k, (double)l).color(v, 255, x, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
			bufferBuilder.vertex(-0.5, 0.75, 0.0).uv((double)j, (double)l).color(v, 255, x, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
			tesselator.end();
			GlStateManager.disableBlend();
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
			super.render(experienceOrb, d, e, f, g, h);
		}
	}

	protected ResourceLocation getTextureLocation(ExperienceOrb experienceOrb) {
		return EXPERIENCE_ORB_LOCATION;
	}
}
