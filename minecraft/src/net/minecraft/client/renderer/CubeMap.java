package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

@Environment(EnvType.CLIENT)
public class CubeMap {
	private static final int SIDES = 6;
	private final ResourceLocation[] images = new ResourceLocation[6];

	public CubeMap(ResourceLocation resourceLocation) {
		for (int i = 0; i < 6; i++) {
			this.images[i] = resourceLocation.withPath(resourceLocation.getPath() + "_" + i + ".png");
		}
	}

	public void render(Minecraft minecraft, float f, float g, float h) {
		Tesselator tesselator = Tesselator.getInstance();
		Matrix4f matrix4f = new Matrix4f()
			.setPerspective(1.4835298F, (float)minecraft.getWindow().getWidth() / (float)minecraft.getWindow().getHeight(), 0.05F, 10.0F);
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.DISTANCE_TO_ORIGIN);
		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();
		matrix4fStack.rotationX((float) Math.PI);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		int i = 2;

		for (int j = 0; j < 4; j++) {
			matrix4fStack.pushMatrix();
			float k = ((float)(j % 2) / 2.0F - 0.5F) / 256.0F;
			float l = ((float)(j / 2) / 2.0F - 0.5F) / 256.0F;
			float m = 0.0F;
			matrix4fStack.translate(k, l, 0.0F);
			matrix4fStack.rotateX(f * (float) (Math.PI / 180.0));
			matrix4fStack.rotateY(g * (float) (Math.PI / 180.0));

			for (int n = 0; n < 6; n++) {
				RenderSystem.setShaderTexture(0, this.images[n]);
				BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
				int o = Math.round(255.0F * h) / (j + 1);
				if (n == 0) {
					bufferBuilder.addVertex(-1.0F, -1.0F, 1.0F).setUv(0.0F, 0.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(-1.0F, 1.0F, 1.0F).setUv(0.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(1.0F, 1.0F, 1.0F).setUv(1.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(1.0F, -1.0F, 1.0F).setUv(1.0F, 0.0F).setWhiteAlpha(o);
				}

				if (n == 1) {
					bufferBuilder.addVertex(1.0F, -1.0F, 1.0F).setUv(0.0F, 0.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(1.0F, 1.0F, 1.0F).setUv(0.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(1.0F, 1.0F, -1.0F).setUv(1.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(1.0F, -1.0F, -1.0F).setUv(1.0F, 0.0F).setWhiteAlpha(o);
				}

				if (n == 2) {
					bufferBuilder.addVertex(1.0F, -1.0F, -1.0F).setUv(0.0F, 0.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(1.0F, 1.0F, -1.0F).setUv(0.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(-1.0F, 1.0F, -1.0F).setUv(1.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(-1.0F, -1.0F, -1.0F).setUv(1.0F, 0.0F).setWhiteAlpha(o);
				}

				if (n == 3) {
					bufferBuilder.addVertex(-1.0F, -1.0F, -1.0F).setUv(0.0F, 0.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(-1.0F, 1.0F, -1.0F).setUv(0.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(-1.0F, 1.0F, 1.0F).setUv(1.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(-1.0F, -1.0F, 1.0F).setUv(1.0F, 0.0F).setWhiteAlpha(o);
				}

				if (n == 4) {
					bufferBuilder.addVertex(-1.0F, -1.0F, -1.0F).setUv(0.0F, 0.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(-1.0F, -1.0F, 1.0F).setUv(0.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(1.0F, -1.0F, 1.0F).setUv(1.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(1.0F, -1.0F, -1.0F).setUv(1.0F, 0.0F).setWhiteAlpha(o);
				}

				if (n == 5) {
					bufferBuilder.addVertex(-1.0F, 1.0F, 1.0F).setUv(0.0F, 0.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(-1.0F, 1.0F, -1.0F).setUv(0.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(1.0F, 1.0F, -1.0F).setUv(1.0F, 1.0F).setWhiteAlpha(o);
					bufferBuilder.addVertex(1.0F, 1.0F, 1.0F).setUv(1.0F, 0.0F).setWhiteAlpha(o);
				}

				BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
			}

			matrix4fStack.popMatrix();
			RenderSystem.colorMask(true, true, true, false);
		}

		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.restoreProjectionMatrix();
		matrix4fStack.popMatrix();
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
