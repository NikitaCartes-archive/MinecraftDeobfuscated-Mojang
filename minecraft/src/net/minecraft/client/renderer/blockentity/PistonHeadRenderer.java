package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;

@Environment(EnvType.CLIENT)
public class PistonHeadRenderer extends BlockEntityRenderer<PistonMovingBlockEntity> {
	private final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

	public PistonHeadRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(
		PistonMovingBlockEntity pistonMovingBlockEntity,
		double d,
		double e,
		double f,
		float g,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j
	) {
		Level level = pistonMovingBlockEntity.getLevel();
		if (level != null) {
			BlockPos blockPos = pistonMovingBlockEntity.getBlockPos().relative(pistonMovingBlockEntity.getMovementDirection().getOpposite());
			BlockState blockState = pistonMovingBlockEntity.getMovedState();
			if (!blockState.isAir() && !(pistonMovingBlockEntity.getProgress(g) >= 1.0F)) {
				ModelBlockRenderer.enableCaching();
				poseStack.pushPose();
				poseStack.translate(
					(double)((float)(-(blockPos.getX() & 15)) + pistonMovingBlockEntity.getXOff(g)),
					(double)((float)(-(blockPos.getY() & 15)) + pistonMovingBlockEntity.getYOff(g)),
					(double)((float)(-(blockPos.getZ() & 15)) + pistonMovingBlockEntity.getZOff(g))
				);
				if (blockState.getBlock() == Blocks.PISTON_HEAD && pistonMovingBlockEntity.getProgress(g) <= 4.0F) {
					blockState = blockState.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(true));
					this.renderBlock(blockPos, blockState, poseStack, multiBufferSource, level, false, j);
				} else if (pistonMovingBlockEntity.isSourcePiston() && !pistonMovingBlockEntity.isExtending()) {
					PistonType pistonType = blockState.getBlock() == Blocks.STICKY_PISTON ? PistonType.STICKY : PistonType.DEFAULT;
					BlockState blockState2 = Blocks.PISTON_HEAD
						.defaultBlockState()
						.setValue(PistonHeadBlock.TYPE, pistonType)
						.setValue(PistonHeadBlock.FACING, blockState.getValue(PistonBaseBlock.FACING));
					blockState2 = blockState2.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(pistonMovingBlockEntity.getProgress(g) >= 0.5F));
					this.renderBlock(blockPos, blockState2, poseStack, multiBufferSource, level, false, j);
					BlockPos blockPos2 = blockPos.relative(pistonMovingBlockEntity.getMovementDirection());
					poseStack.popPose();
					poseStack.translate((double)(-(blockPos2.getX() & 15)), (double)(-(blockPos2.getY() & 15)), (double)(-(blockPos2.getZ() & 15)));
					blockState = blockState.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true));
					this.renderBlock(blockPos2, blockState, poseStack, multiBufferSource, level, true, j);
					poseStack.pushPose();
				} else {
					this.renderBlock(blockPos, blockState, poseStack, multiBufferSource, level, false, j);
				}

				poseStack.popPose();
				ModelBlockRenderer.clearCache();
			}
		}
	}

	private void renderBlock(BlockPos blockPos, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, Level level, boolean bl, int i) {
		RenderType renderType = RenderType.getChunkRenderType(blockState);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
		this.blockRenderer
			.getModelRenderer()
			.tesselateBlock(
				level, this.blockRenderer.getBlockModel(blockState), blockState, blockPos, poseStack, vertexConsumer, bl, new Random(), blockState.getSeed(blockPos), i
			);
	}
}
