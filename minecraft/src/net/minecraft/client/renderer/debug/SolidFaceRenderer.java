package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(EnvType.CLIENT)
public class SolidFaceRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;

	public SolidFaceRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		BlockGetter blockGetter = this.minecraft.player.level;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.lineWidth(2.0F);
		RenderSystem.disableTexture();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BlockPos blockPos = new BlockPos(d, e, f);

		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-6, -6, -6), blockPos.offset(6, 6, 6))) {
			BlockState blockState = blockGetter.getBlockState(blockPos2);
			if (!blockState.is(Blocks.AIR)) {
				VoxelShape voxelShape = blockState.getShape(blockGetter, blockPos2);

				for (AABB aABB : voxelShape.toAabbs()) {
					AABB aABB2 = aABB.move(blockPos2).inflate(0.002).move(-d, -e, -f);
					double g = aABB2.minX;
					double h = aABB2.minY;
					double i = aABB2.minZ;
					double j = aABB2.maxX;
					double k = aABB2.maxY;
					double l = aABB2.maxZ;
					float m = 1.0F;
					float n = 0.0F;
					float o = 0.0F;
					float p = 0.5F;
					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.WEST)) {
						Tesselator tesselator = Tesselator.getInstance();
						BufferBuilder bufferBuilder = tesselator.getBuilder();
						bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
						bufferBuilder.vertex(g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(g, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(g, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(g, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						tesselator.end();
					}

					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.SOUTH)) {
						Tesselator tesselator = Tesselator.getInstance();
						BufferBuilder bufferBuilder = tesselator.getBuilder();
						bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
						bufferBuilder.vertex(g, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(g, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(j, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(j, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						tesselator.end();
					}

					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.EAST)) {
						Tesselator tesselator = Tesselator.getInstance();
						BufferBuilder bufferBuilder = tesselator.getBuilder();
						bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
						bufferBuilder.vertex(j, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(j, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(j, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(j, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						tesselator.end();
					}

					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.NORTH)) {
						Tesselator tesselator = Tesselator.getInstance();
						BufferBuilder bufferBuilder = tesselator.getBuilder();
						bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
						bufferBuilder.vertex(j, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(j, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(g, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						tesselator.end();
					}

					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.DOWN)) {
						Tesselator tesselator = Tesselator.getInstance();
						BufferBuilder bufferBuilder = tesselator.getBuilder();
						bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
						bufferBuilder.vertex(g, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(j, h, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(g, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(j, h, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						tesselator.end();
					}

					if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.UP)) {
						Tesselator tesselator = Tesselator.getInstance();
						BufferBuilder bufferBuilder = tesselator.getBuilder();
						bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
						bufferBuilder.vertex(g, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(g, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(j, k, i).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						bufferBuilder.vertex(j, k, l).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
						tesselator.end();
					}
				}
			}
		}

		RenderSystem.depthMask(true);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
}
