package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class CaveDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Map<BlockPos, BlockPos> tunnelsList = Maps.<BlockPos, BlockPos>newHashMap();
	private final Map<BlockPos, Float> thicknessMap = Maps.<BlockPos, Float>newHashMap();
	private final List<BlockPos> startPoses = Lists.<BlockPos>newArrayList();

	public void addTunnel(BlockPos blockPos, List<BlockPos> list, List<Float> list2) {
		for (int i = 0; i < list.size(); i++) {
			this.tunnelsList.put(list.get(i), blockPos);
			this.thicknessMap.put(list.get(i), list2.get(i));
		}

		this.startPoses.add(blockPos);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		RenderSystem.pushMatrix();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		BlockPos blockPos = new BlockPos(d, 0.0, f);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);

		for (Entry<BlockPos, BlockPos> entry : this.tunnelsList.entrySet()) {
			BlockPos blockPos2 = (BlockPos)entry.getKey();
			BlockPos blockPos3 = (BlockPos)entry.getValue();
			float g = (float)(blockPos3.getX() * 128 % 256) / 256.0F;
			float h = (float)(blockPos3.getY() * 128 % 256) / 256.0F;
			float i = (float)(blockPos3.getZ() * 128 % 256) / 256.0F;
			float j = (Float)this.thicknessMap.get(blockPos2);
			if (blockPos.closerThan(blockPos2, 160.0)) {
				LevelRenderer.addChainedFilledBoxVertices(
					bufferBuilder,
					(double)((float)blockPos2.getX() + 0.5F) - d - (double)j,
					(double)((float)blockPos2.getY() + 0.5F) - e - (double)j,
					(double)((float)blockPos2.getZ() + 0.5F) - f - (double)j,
					(double)((float)blockPos2.getX() + 0.5F) - d + (double)j,
					(double)((float)blockPos2.getY() + 0.5F) - e + (double)j,
					(double)((float)blockPos2.getZ() + 0.5F) - f + (double)j,
					g,
					h,
					i,
					0.5F
				);
			}
		}

		for (BlockPos blockPos4 : this.startPoses) {
			if (blockPos.closerThan(blockPos4, 160.0)) {
				LevelRenderer.addChainedFilledBoxVertices(
					bufferBuilder,
					(double)blockPos4.getX() - d,
					(double)blockPos4.getY() - e,
					(double)blockPos4.getZ() - f,
					(double)((float)blockPos4.getX() + 1.0F) - d,
					(double)((float)blockPos4.getY() + 1.0F) - e,
					(double)((float)blockPos4.getZ() + 1.0F) - f,
					1.0F,
					1.0F,
					1.0F,
					1.0F
				);
			}
		}

		tesselator.end();
		RenderSystem.enableDepthTest();
		RenderSystem.enableTexture();
		RenderSystem.popMatrix();
	}
}
