package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class SolidFaceRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;

	public SolidFaceRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		Matrix4f matrix4f = poseStack.last().pose();
		BlockGetter blockGetter = this.minecraft.player.level;
		BlockPos blockPos = new BlockPos(d, e, f);

		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-6, -6, -6), blockPos.offset(6, 6, 6))) {
			BlockState blockState = blockGetter.getBlockState(blockPos2);
			if (!blockState.is(Blocks.AIR)) {
				VoxelShape voxelShape = blockState.getShape(blockGetter, blockPos2);

				for (AABB aABB : voxelShape.toAabbs()) {
					AABB aABB2 = aABB.move(blockPos2).inflate(0.002);
					float g = (float)(aABB2.minX - d);
					float h = (float)(aABB2.minY - e);
					float i = (float)(aABB2.minZ - f);
					float j = (float)(aABB2.maxX - d);
					float k = (float)(aABB2.maxY - e);
					float l = (float)(aABB2.maxZ - f);
					float m = 1.0F;
					float n = 0.0F;
					float o = 0.0F;
					float p = 0.5F;
					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.WEST)) {
						VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
						vertexConsumer.vertex(matrix4f, g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, g, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, g, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, g, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
					}

					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.SOUTH)) {
						VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
						vertexConsumer.vertex(matrix4f, g, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, g, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, j, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, j, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
					}

					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.EAST)) {
						VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
						vertexConsumer.vertex(matrix4f, j, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, j, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, j, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, j, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
					}

					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.NORTH)) {
						VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
						vertexConsumer.vertex(matrix4f, j, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, j, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, g, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
					}

					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.DOWN)) {
						VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
						vertexConsumer.vertex(matrix4f, g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, j, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, g, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, j, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
					}

					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.UP)) {
						VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
						vertexConsumer.vertex(matrix4f, g, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, g, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, j, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						vertexConsumer.vertex(matrix4f, j, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
					}
				}
			}
		}
	}
}
