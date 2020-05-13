package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

@Environment(EnvType.CLIENT)
public class StructureRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private final Map<DimensionType, Map<String, BoundingBox>> postMainBoxes = Maps.<DimensionType, Map<String, BoundingBox>>newIdentityHashMap();
	private final Map<DimensionType, Map<String, BoundingBox>> postPiecesBoxes = Maps.<DimensionType, Map<String, BoundingBox>>newIdentityHashMap();
	private final Map<DimensionType, Map<String, Boolean>> startPiecesMap = Maps.<DimensionType, Map<String, Boolean>>newIdentityHashMap();

	public StructureRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		Camera camera = this.minecraft.gameRenderer.getMainCamera();
		LevelAccessor levelAccessor = this.minecraft.level;
		DimensionType dimensionType = levelAccessor.dimensionType();
		BlockPos blockPos = new BlockPos(camera.getPosition().x, 0.0, camera.getPosition().z);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
		if (this.postMainBoxes.containsKey(dimensionType)) {
			for (BoundingBox boundingBox : ((Map)this.postMainBoxes.get(dimensionType)).values()) {
				if (blockPos.closerThan(boundingBox.getCenter(), 500.0)) {
					LevelRenderer.renderLineBox(
						poseStack,
						vertexConsumer,
						(double)boundingBox.x0 - d,
						(double)boundingBox.y0 - e,
						(double)boundingBox.z0 - f,
						(double)(boundingBox.x1 + 1) - d,
						(double)(boundingBox.y1 + 1) - e,
						(double)(boundingBox.z1 + 1) - f,
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

		if (this.postPiecesBoxes.containsKey(dimensionType)) {
			for (Entry<String, BoundingBox> entry : ((Map)this.postPiecesBoxes.get(dimensionType)).entrySet()) {
				String string = (String)entry.getKey();
				BoundingBox boundingBox2 = (BoundingBox)entry.getValue();
				Boolean boolean_ = (Boolean)((Map)this.startPiecesMap.get(dimensionType)).get(string);
				if (blockPos.closerThan(boundingBox2.getCenter(), 500.0)) {
					if (boolean_) {
						LevelRenderer.renderLineBox(
							poseStack,
							vertexConsumer,
							(double)boundingBox2.x0 - d,
							(double)boundingBox2.y0 - e,
							(double)boundingBox2.z0 - f,
							(double)(boundingBox2.x1 + 1) - d,
							(double)(boundingBox2.y1 + 1) - e,
							(double)(boundingBox2.z1 + 1) - f,
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
							(double)boundingBox2.x0 - d,
							(double)boundingBox2.y0 - e,
							(double)boundingBox2.z0 - f,
							(double)(boundingBox2.x1 + 1) - d,
							(double)(boundingBox2.y1 + 1) - e,
							(double)(boundingBox2.z1 + 1) - f,
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

	public void addBoundingBox(BoundingBox boundingBox, List<BoundingBox> list, List<Boolean> list2, DimensionType dimensionType) {
		if (!this.postMainBoxes.containsKey(dimensionType)) {
			this.postMainBoxes.put(dimensionType, Maps.newHashMap());
		}

		if (!this.postPiecesBoxes.containsKey(dimensionType)) {
			this.postPiecesBoxes.put(dimensionType, Maps.newHashMap());
			this.startPiecesMap.put(dimensionType, Maps.newHashMap());
		}

		((Map)this.postMainBoxes.get(dimensionType)).put(boundingBox.toString(), boundingBox);

		for (int i = 0; i < list.size(); i++) {
			BoundingBox boundingBox2 = (BoundingBox)list.get(i);
			Boolean boolean_ = (Boolean)list2.get(i);
			((Map)this.postPiecesBoxes.get(dimensionType)).put(boundingBox2.toString(), boundingBox2);
			((Map)this.startPiecesMap.get(dimensionType)).put(boundingBox2.toString(), boolean_);
		}
	}

	@Override
	public void clear() {
		this.postMainBoxes.clear();
		this.postPiecesBoxes.clear();
		this.startPiecesMap.clear();
	}
}
