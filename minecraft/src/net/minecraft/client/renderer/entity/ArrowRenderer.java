package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;

@Environment(EnvType.CLIENT)
public abstract class ArrowRenderer<T extends AbstractArrow> extends EntityRenderer<T> {
	public ArrowRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(T abstractArrow, double d, double e, double f, float g, float h) {
		this.bindTexture(abstractArrow);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RenderSystem.disableLighting();
		RenderSystem.translatef((float)d, (float)e, (float)f);
		RenderSystem.rotatef(Mth.lerp(h, abstractArrow.yRotO, abstractArrow.yRot) - 90.0F, 0.0F, 1.0F, 0.0F);
		RenderSystem.rotatef(Mth.lerp(h, abstractArrow.xRotO, abstractArrow.xRot), 0.0F, 0.0F, 1.0F);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		int i = 0;
		float j = 0.0F;
		float k = 0.5F;
		float l = 0.0F;
		float m = 0.15625F;
		float n = 0.0F;
		float o = 0.15625F;
		float p = 0.15625F;
		float q = 0.3125F;
		float r = 0.05625F;
		RenderSystem.enableRescaleNormal();
		float s = (float)abstractArrow.shakeTime - h;
		if (s > 0.0F) {
			float t = -Mth.sin(s * 3.0F) * s;
			RenderSystem.rotatef(t, 0.0F, 0.0F, 1.0F);
		}

		RenderSystem.rotatef(45.0F, 1.0F, 0.0F, 0.0F);
		RenderSystem.scalef(0.05625F, 0.05625F, 0.05625F);
		RenderSystem.translatef(-4.0F, 0.0F, 0.0F);
		if (this.solidRender) {
			RenderSystem.enableColorMaterial();
			RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(abstractArrow));
		}

		RenderSystem.normal3f(0.05625F, 0.0F, 0.0F);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(-7.0, -2.0, -2.0).uv(0.0, 0.15625).endVertex();
		bufferBuilder.vertex(-7.0, -2.0, 2.0).uv(0.15625, 0.15625).endVertex();
		bufferBuilder.vertex(-7.0, 2.0, 2.0).uv(0.15625, 0.3125).endVertex();
		bufferBuilder.vertex(-7.0, 2.0, -2.0).uv(0.0, 0.3125).endVertex();
		tesselator.end();
		RenderSystem.normal3f(-0.05625F, 0.0F, 0.0F);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(-7.0, 2.0, -2.0).uv(0.0, 0.15625).endVertex();
		bufferBuilder.vertex(-7.0, 2.0, 2.0).uv(0.15625, 0.15625).endVertex();
		bufferBuilder.vertex(-7.0, -2.0, 2.0).uv(0.15625, 0.3125).endVertex();
		bufferBuilder.vertex(-7.0, -2.0, -2.0).uv(0.0, 0.3125).endVertex();
		tesselator.end();

		for (int u = 0; u < 4; u++) {
			RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
			RenderSystem.normal3f(0.0F, 0.0F, 0.05625F);
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.vertex(-8.0, -2.0, 0.0).uv(0.0, 0.0).endVertex();
			bufferBuilder.vertex(8.0, -2.0, 0.0).uv(0.5, 0.0).endVertex();
			bufferBuilder.vertex(8.0, 2.0, 0.0).uv(0.5, 0.15625).endVertex();
			bufferBuilder.vertex(-8.0, 2.0, 0.0).uv(0.0, 0.15625).endVertex();
			tesselator.end();
		}

		if (this.solidRender) {
			RenderSystem.tearDownSolidRenderingTextureCombine();
			RenderSystem.disableColorMaterial();
		}

		RenderSystem.disableRescaleNormal();
		RenderSystem.enableLighting();
		RenderSystem.popMatrix();
		super.render(abstractArrow, d, e, f, g, h);
	}
}
