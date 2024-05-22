package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ChunkBorderRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private static final int CELL_BORDER = FastColor.ARGB32.color(255, 0, 155, 155);
	private static final int YELLOW = FastColor.ARGB32.color(255, 255, 255, 0);

	public ChunkBorderRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
		float g = (float)((double)this.minecraft.level.getMinBuildHeight() - e);
		float h = (float)((double)this.minecraft.level.getMaxBuildHeight() - e);
		ChunkPos chunkPos = entity.chunkPosition();
		float i = (float)((double)chunkPos.getMinBlockX() - d);
		float j = (float)((double)chunkPos.getMinBlockZ() - f);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(1.0));
		Matrix4f matrix4f = poseStack.last().pose();

		for (int k = -16; k <= 32; k += 16) {
			for (int l = -16; l <= 32; l += 16) {
				vertexConsumer.addVertex(matrix4f, i + (float)k, g, j + (float)l).setColor(1.0F, 0.0F, 0.0F, 0.0F);
				vertexConsumer.addVertex(matrix4f, i + (float)k, g, j + (float)l).setColor(1.0F, 0.0F, 0.0F, 0.5F);
				vertexConsumer.addVertex(matrix4f, i + (float)k, h, j + (float)l).setColor(1.0F, 0.0F, 0.0F, 0.5F);
				vertexConsumer.addVertex(matrix4f, i + (float)k, h, j + (float)l).setColor(1.0F, 0.0F, 0.0F, 0.0F);
			}
		}

		for (int k = 2; k < 16; k += 2) {
			int l = k % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, i + (float)k, g, j).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, i + (float)k, g, j).setColor(l);
			vertexConsumer.addVertex(matrix4f, i + (float)k, h, j).setColor(l);
			vertexConsumer.addVertex(matrix4f, i + (float)k, h, j).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, i + (float)k, g, j + 16.0F).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, i + (float)k, g, j + 16.0F).setColor(l);
			vertexConsumer.addVertex(matrix4f, i + (float)k, h, j + 16.0F).setColor(l);
			vertexConsumer.addVertex(matrix4f, i + (float)k, h, j + 16.0F).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}

		for (int k = 2; k < 16; k += 2) {
			int l = k % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, i, g, j + (float)k).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, i, g, j + (float)k).setColor(l);
			vertexConsumer.addVertex(matrix4f, i, h, j + (float)k).setColor(l);
			vertexConsumer.addVertex(matrix4f, i, h, j + (float)k).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, i + 16.0F, g, j + (float)k).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, i + 16.0F, g, j + (float)k).setColor(l);
			vertexConsumer.addVertex(matrix4f, i + 16.0F, h, j + (float)k).setColor(l);
			vertexConsumer.addVertex(matrix4f, i + 16.0F, h, j + (float)k).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}

		for (int k = this.minecraft.level.getMinBuildHeight(); k <= this.minecraft.level.getMaxBuildHeight(); k += 2) {
			float m = (float)((double)k - e);
			int n = k % 8 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, i, m, j).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, i, m, j).setColor(n);
			vertexConsumer.addVertex(matrix4f, i, m, j + 16.0F).setColor(n);
			vertexConsumer.addVertex(matrix4f, i + 16.0F, m, j + 16.0F).setColor(n);
			vertexConsumer.addVertex(matrix4f, i + 16.0F, m, j).setColor(n);
			vertexConsumer.addVertex(matrix4f, i, m, j).setColor(n);
			vertexConsumer.addVertex(matrix4f, i, m, j).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}

		vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0));

		for (int k = 0; k <= 16; k += 16) {
			for (int l = 0; l <= 16; l += 16) {
				vertexConsumer.addVertex(matrix4f, i + (float)k, g, j + (float)l).setColor(0.25F, 0.25F, 1.0F, 0.0F);
				vertexConsumer.addVertex(matrix4f, i + (float)k, g, j + (float)l).setColor(0.25F, 0.25F, 1.0F, 1.0F);
				vertexConsumer.addVertex(matrix4f, i + (float)k, h, j + (float)l).setColor(0.25F, 0.25F, 1.0F, 1.0F);
				vertexConsumer.addVertex(matrix4f, i + (float)k, h, j + (float)l).setColor(0.25F, 0.25F, 1.0F, 0.0F);
			}
		}

		for (int k = this.minecraft.level.getMinBuildHeight(); k <= this.minecraft.level.getMaxBuildHeight(); k += 16) {
			float m = (float)((double)k - e);
			vertexConsumer.addVertex(matrix4f, i, m, j).setColor(0.25F, 0.25F, 1.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, i, m, j).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, i, m, j + 16.0F).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, i + 16.0F, m, j + 16.0F).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, i + 16.0F, m, j).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, i, m, j).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, i, m, j).setColor(0.25F, 0.25F, 1.0F, 0.0F);
		}
	}
}
