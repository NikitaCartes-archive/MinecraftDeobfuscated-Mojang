package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public class LightSectionDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private static final Duration REFRESH_INTERVAL = Duration.ofMillis(500L);
	private static final int RADIUS = 10;
	private static final Vector4f LIGHT_AND_BLOCKS_COLOR = new Vector4f(1.0F, 1.0F, 0.0F, 0.25F);
	private static final Vector4f LIGHT_ONLY_COLOR = new Vector4f(0.25F, 0.125F, 0.0F, 0.125F);
	private final Minecraft minecraft;
	private final LightLayer lightLayer;
	private Instant lastUpdateTime = Instant.now();
	@Nullable
	private LightSectionDebugRenderer.SectionData data;

	public LightSectionDebugRenderer(Minecraft minecraft, LightLayer lightLayer) {
		this.minecraft = minecraft;
		this.lightLayer = lightLayer;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		Instant instant = Instant.now();
		if (this.data == null || Duration.between(this.lastUpdateTime, instant).compareTo(REFRESH_INTERVAL) > 0) {
			this.lastUpdateTime = instant;
			this.data = new LightSectionDebugRenderer.SectionData(
				this.minecraft.level.getLightEngine(), SectionPos.of(this.minecraft.player.blockPosition()), 10, this.lightLayer
			);
		}

		renderEdges(poseStack, this.data.lightAndBlocksShape, this.data.minPos, multiBufferSource, d, e, f, LIGHT_AND_BLOCKS_COLOR);
		renderEdges(poseStack, this.data.lightShape, this.data.minPos, multiBufferSource, d, e, f, LIGHT_ONLY_COLOR);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugSectionQuads());
		renderFaces(poseStack, this.data.lightAndBlocksShape, this.data.minPos, vertexConsumer, d, e, f, LIGHT_AND_BLOCKS_COLOR);
		renderFaces(poseStack, this.data.lightShape, this.data.minPos, vertexConsumer, d, e, f, LIGHT_ONLY_COLOR);
	}

	private static void renderFaces(
		PoseStack poseStack,
		DiscreteVoxelShape discreteVoxelShape,
		SectionPos sectionPos,
		VertexConsumer vertexConsumer,
		double d,
		double e,
		double f,
		Vector4f vector4f
	) {
		discreteVoxelShape.forAllFaces((direction, i, j, k) -> {
			int l = i + sectionPos.getX();
			int m = j + sectionPos.getY();
			int n = k + sectionPos.getZ();
			renderFace(poseStack, vertexConsumer, direction, d, e, f, l, m, n, vector4f);
		});
	}

	private static void renderEdges(
		PoseStack poseStack,
		DiscreteVoxelShape discreteVoxelShape,
		SectionPos sectionPos,
		MultiBufferSource multiBufferSource,
		double d,
		double e,
		double f,
		Vector4f vector4f
	) {
		discreteVoxelShape.forAllEdges((i, j, k, l, m, n) -> {
			int o = i + sectionPos.getX();
			int p = j + sectionPos.getY();
			int q = k + sectionPos.getZ();
			int r = l + sectionPos.getX();
			int s = m + sectionPos.getY();
			int t = n + sectionPos.getZ();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(1.0));
			renderEdge(poseStack, vertexConsumer, d, e, f, o, p, q, r, s, t, vector4f);
		}, true);
	}

	private static void renderFace(
		PoseStack poseStack, VertexConsumer vertexConsumer, Direction direction, double d, double e, double f, int i, int j, int k, Vector4f vector4f
	) {
		float g = (float)((double)SectionPos.sectionToBlockCoord(i) - d);
		float h = (float)((double)SectionPos.sectionToBlockCoord(j) - e);
		float l = (float)((double)SectionPos.sectionToBlockCoord(k) - f);
		float m = g + 16.0F;
		float n = h + 16.0F;
		float o = l + 16.0F;
		float p = vector4f.x();
		float q = vector4f.y();
		float r = vector4f.z();
		float s = vector4f.w();
		Matrix4f matrix4f = poseStack.last().pose();
		switch (direction) {
			case DOWN:
				vertexConsumer.addVertex(matrix4f, g, h, l).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, m, h, l).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, m, h, o).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, g, h, o).setColor(p, q, r, s);
				break;
			case UP:
				vertexConsumer.addVertex(matrix4f, g, n, l).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, g, n, o).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, m, n, o).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, m, n, l).setColor(p, q, r, s);
				break;
			case NORTH:
				vertexConsumer.addVertex(matrix4f, g, h, l).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, g, n, l).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, m, n, l).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, m, h, l).setColor(p, q, r, s);
				break;
			case SOUTH:
				vertexConsumer.addVertex(matrix4f, g, h, o).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, m, h, o).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, m, n, o).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, g, n, o).setColor(p, q, r, s);
				break;
			case WEST:
				vertexConsumer.addVertex(matrix4f, g, h, l).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, g, h, o).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, g, n, o).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, g, n, l).setColor(p, q, r, s);
				break;
			case EAST:
				vertexConsumer.addVertex(matrix4f, m, h, l).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, m, n, l).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, m, n, o).setColor(p, q, r, s);
				vertexConsumer.addVertex(matrix4f, m, h, o).setColor(p, q, r, s);
		}
	}

	private static void renderEdge(
		PoseStack poseStack, VertexConsumer vertexConsumer, double d, double e, double f, int i, int j, int k, int l, int m, int n, Vector4f vector4f
	) {
		float g = (float)((double)SectionPos.sectionToBlockCoord(i) - d);
		float h = (float)((double)SectionPos.sectionToBlockCoord(j) - e);
		float o = (float)((double)SectionPos.sectionToBlockCoord(k) - f);
		float p = (float)((double)SectionPos.sectionToBlockCoord(l) - d);
		float q = (float)((double)SectionPos.sectionToBlockCoord(m) - e);
		float r = (float)((double)SectionPos.sectionToBlockCoord(n) - f);
		Matrix4f matrix4f = poseStack.last().pose();
		vertexConsumer.addVertex(matrix4f, g, h, o).setColor(vector4f.x(), vector4f.y(), vector4f.z(), 1.0F);
		vertexConsumer.addVertex(matrix4f, p, q, r).setColor(vector4f.x(), vector4f.y(), vector4f.z(), 1.0F);
	}

	@Environment(EnvType.CLIENT)
	static final class SectionData {
		final DiscreteVoxelShape lightAndBlocksShape;
		final DiscreteVoxelShape lightShape;
		final SectionPos minPos;

		SectionData(LevelLightEngine levelLightEngine, SectionPos sectionPos, int i, LightLayer lightLayer) {
			int j = i * 2 + 1;
			this.lightAndBlocksShape = new BitSetDiscreteVoxelShape(j, j, j);
			this.lightShape = new BitSetDiscreteVoxelShape(j, j, j);

			for (int k = 0; k < j; k++) {
				for (int l = 0; l < j; l++) {
					for (int m = 0; m < j; m++) {
						SectionPos sectionPos2 = SectionPos.of(sectionPos.x() + m - i, sectionPos.y() + l - i, sectionPos.z() + k - i);
						LayerLightSectionStorage.SectionType sectionType = levelLightEngine.getDebugSectionType(lightLayer, sectionPos2);
						if (sectionType == LayerLightSectionStorage.SectionType.LIGHT_AND_DATA) {
							this.lightAndBlocksShape.fill(m, l, k);
							this.lightShape.fill(m, l, k);
						} else if (sectionType == LayerLightSectionStorage.SectionType.LIGHT_ONLY) {
							this.lightShape.fill(m, l, k);
						}
					}
				}
			}

			this.minPos = SectionPos.of(sectionPos.x() - i, sectionPos.y() - i, sectionPos.z() - i);
		}
	}
}
