package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class ChunkBorderRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;

	public ChunkBorderRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		RenderSystem.enableDepthTest();
		RenderSystem.shadeModel(7425);
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		double g = 0.0 - e;
		double h = 256.0 - e;
		RenderSystem.disableTexture();
		RenderSystem.disableBlend();
		double i = (double)(entity.xChunk << 4) - d;
		double j = (double)(entity.zChunk << 4) - f;
		RenderSystem.lineWidth(1.0F);
		bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);

		for (int k = -16; k <= 32; k += 16) {
			for (int l = -16; l <= 32; l += 16) {
				bufferBuilder.vertex(i + (double)k, g, j + (double)l).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
				bufferBuilder.vertex(i + (double)k, g, j + (double)l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
				bufferBuilder.vertex(i + (double)k, h, j + (double)l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
				bufferBuilder.vertex(i + (double)k, h, j + (double)l).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
			}
		}

		for (int k = 2; k < 16; k += 2) {
			bufferBuilder.vertex(i + (double)k, g, j).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
			bufferBuilder.vertex(i + (double)k, g, j).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i + (double)k, h, j).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i + (double)k, h, j).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
			bufferBuilder.vertex(i + (double)k, g, j + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
			bufferBuilder.vertex(i + (double)k, g, j + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i + (double)k, h, j + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i + (double)k, h, j + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
		}

		for (int k = 2; k < 16; k += 2) {
			bufferBuilder.vertex(i, g, j + (double)k).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
			bufferBuilder.vertex(i, g, j + (double)k).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i, h, j + (double)k).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i, h, j + (double)k).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
			bufferBuilder.vertex(i + 16.0, g, j + (double)k).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
			bufferBuilder.vertex(i + 16.0, g, j + (double)k).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i + 16.0, h, j + (double)k).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i + 16.0, h, j + (double)k).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
		}

		for (int k = 0; k <= 256; k += 2) {
			double m = (double)k - e;
			bufferBuilder.vertex(i, m, j).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
			bufferBuilder.vertex(i, m, j).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i, m, j + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i + 16.0, m, j + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i + 16.0, m, j).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i, m, j).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i, m, j).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
		}

		tesselator.end();
		RenderSystem.lineWidth(2.0F);
		bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);

		for (int k = 0; k <= 16; k += 16) {
			for (int l = 0; l <= 16; l += 16) {
				bufferBuilder.vertex(i + (double)k, g, j + (double)l).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
				bufferBuilder.vertex(i + (double)k, g, j + (double)l).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
				bufferBuilder.vertex(i + (double)k, h, j + (double)l).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
				bufferBuilder.vertex(i + (double)k, h, j + (double)l).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
			}
		}

		for (int k = 0; k <= 256; k += 16) {
			double m = (double)k - e;
			bufferBuilder.vertex(i, m, j).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
			bufferBuilder.vertex(i, m, j).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i, m, j + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i + 16.0, m, j + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i + 16.0, m, j).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i, m, j).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
			bufferBuilder.vertex(i, m, j).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
		}

		tesselator.end();
		RenderSystem.lineWidth(1.0F);
		RenderSystem.enableBlend();
		RenderSystem.enableTexture();
		RenderSystem.shadeModel(7424);
	}
}
