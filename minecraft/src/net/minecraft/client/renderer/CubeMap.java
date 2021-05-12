package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class CubeMap {
	private static final int SIDES = 6;
	private final ResourceLocation[] images = new ResourceLocation[6];

	public CubeMap(ResourceLocation resourceLocation) {
		for (int i = 0; i < 6; i++) {
			this.images[i] = new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + "_" + i + ".png");
		}
	}

	public void render(Minecraft minecraft, float f, float g, float h) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		Matrix4f matrix4f = Matrix4f.perspective(85.0, (float)minecraft.getWindow().getWidth() / (float)minecraft.getWindow().getHeight(), 0.05F, 10.0F);
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(matrix4f);
		PoseStack poseStack = RenderSystem.getModelViewStack();
		poseStack.pushPose();
		poseStack.setIdentity();
		poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
		int i = 2;

		for (int j = 0; j < 4; j++) {
			poseStack.pushPose();
			float k = ((float)(j % 2) / 2.0F - 0.5F) / 256.0F;
			float l = ((float)(j / 2) / 2.0F - 0.5F) / 256.0F;
			float m = 0.0F;
			poseStack.translate((double)k, (double)l, 0.0);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(f));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(g));
			RenderSystem.applyModelViewMatrix();

			for (int n = 0; n < 6; n++) {
				RenderSystem.setShaderTexture(0, this.images[n]);
				bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
				int o = Math.round(255.0F * h) / (j + 1);
				if (n == 0) {
					bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, -1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}

				if (n == 1) {
					bufferBuilder.vertex(1.0, -1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}

				if (n == 2) {
					bufferBuilder.vertex(1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}

				if (n == 3) {
					bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}

				if (n == 4) {
					bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, -1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}

				if (n == 5) {
					bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}

				tesselator.end();
			}

			poseStack.popPose();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.colorMask(true, true, true, false);
		}

		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.restoreProjectionMatrix();
		poseStack.popPose();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
	}

	public CompletableFuture<Void> preload(TextureManager textureManager, Executor executor) {
		CompletableFuture<?>[] completableFutures = new CompletableFuture[6];

		for (int i = 0; i < completableFutures.length; i++) {
			completableFutures[i] = textureManager.preload(this.images[i], executor);
		}

		return CompletableFuture.allOf(completableFutures);
	}
}
