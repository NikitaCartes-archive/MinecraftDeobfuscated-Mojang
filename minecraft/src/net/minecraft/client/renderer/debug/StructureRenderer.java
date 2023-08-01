package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

@Environment(EnvType.CLIENT)
public class StructureRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private final Map<ResourceKey<Level>, Map<String, BoundingBox>> postMainBoxes = Maps.<ResourceKey<Level>, Map<String, BoundingBox>>newIdentityHashMap();
	private final Map<ResourceKey<Level>, Map<String, StructuresDebugPayload.PieceInfo>> postPieces = Maps.<ResourceKey<Level>, Map<String, StructuresDebugPayload.PieceInfo>>newIdentityHashMap();
	private static final int MAX_RENDER_DIST = 500;

	public StructureRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		Camera camera = this.minecraft.gameRenderer.getMainCamera();
		ResourceKey<Level> resourceKey = this.minecraft.level.dimension();
		BlockPos blockPos = BlockPos.containing(camera.getPosition().x, 0.0, camera.getPosition().z);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
		if (this.postMainBoxes.containsKey(resourceKey)) {
			for (BoundingBox boundingBox : ((Map)this.postMainBoxes.get(resourceKey)).values()) {
				if (blockPos.closerThan(boundingBox.getCenter(), 500.0)) {
					LevelRenderer.renderLineBox(
						poseStack,
						vertexConsumer,
						(double)boundingBox.minX() - d,
						(double)boundingBox.minY() - e,
						(double)boundingBox.minZ() - f,
						(double)(boundingBox.maxX() + 1) - d,
						(double)(boundingBox.maxY() + 1) - e,
						(double)(boundingBox.maxZ() + 1) - f,
						1.0F,
						1.0F,
						1.0F,
						1.0F,
						1.0F,
						1.0F,
						1.0F
					);
				}
			}
		}

		Map<String, StructuresDebugPayload.PieceInfo> map = (Map<String, StructuresDebugPayload.PieceInfo>)this.postPieces.get(resourceKey);
		if (map != null) {
			for (StructuresDebugPayload.PieceInfo pieceInfo : map.values()) {
				BoundingBox boundingBox2 = pieceInfo.boundingBox();
				if (blockPos.closerThan(boundingBox2.getCenter(), 500.0)) {
					if (pieceInfo.isStart()) {
						LevelRenderer.renderLineBox(
							poseStack,
							vertexConsumer,
							(double)boundingBox2.minX() - d,
							(double)boundingBox2.minY() - e,
							(double)boundingBox2.minZ() - f,
							(double)(boundingBox2.maxX() + 1) - d,
							(double)(boundingBox2.maxY() + 1) - e,
							(double)(boundingBox2.maxZ() + 1) - f,
							0.0F,
							1.0F,
							0.0F,
							1.0F,
							0.0F,
							1.0F,
							0.0F
						);
					} else {
						LevelRenderer.renderLineBox(
							poseStack,
							vertexConsumer,
							(double)boundingBox2.minX() - d,
							(double)boundingBox2.minY() - e,
							(double)boundingBox2.minZ() - f,
							(double)(boundingBox2.maxX() + 1) - d,
							(double)(boundingBox2.maxY() + 1) - e,
							(double)(boundingBox2.maxZ() + 1) - f,
							0.0F,
							0.0F,
							1.0F,
							1.0F,
							0.0F,
							0.0F,
							1.0F
						);
					}
				}
			}
		}
	}

	public void addBoundingBox(BoundingBox boundingBox, List<StructuresDebugPayload.PieceInfo> list, ResourceKey<Level> resourceKey) {
		((Map)this.postMainBoxes.computeIfAbsent(resourceKey, resourceKeyx -> new HashMap())).put(boundingBox.toString(), boundingBox);
		Map<String, StructuresDebugPayload.PieceInfo> map = (Map<String, StructuresDebugPayload.PieceInfo>)this.postPieces
			.computeIfAbsent(resourceKey, resourceKeyx -> new HashMap());

		for (StructuresDebugPayload.PieceInfo pieceInfo : list) {
			map.put(pieceInfo.boundingBox().toString(), pieceInfo);
		}
	}

	@Override
	public void clear() {
		this.postMainBoxes.clear();
		this.postPieces.clear();
	}
}
