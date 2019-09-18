package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
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
public class PistonHeadRenderer extends BatchedBlockEntityRenderer<PistonMovingBlockEntity> {
	private final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

	protected void renderToBuffer(
		PistonMovingBlockEntity pistonMovingBlockEntity,
		double d,
		double e,
		double f,
		float g,
		int i,
		RenderType renderType,
		BufferBuilder bufferBuilder,
		int j,
		int k
	) {
		BlockPos blockPos = pistonMovingBlockEntity.getBlockPos().relative(pistonMovingBlockEntity.getMovementDirection().getOpposite());
		BlockState blockState = pistonMovingBlockEntity.getMovedState();
		if (!blockState.isAir() && !(pistonMovingBlockEntity.getProgress(g) >= 1.0F)) {
			ModelBlockRenderer.enableCaching();
			bufferBuilder.offset(
				d - (double)blockPos.getX() + (double)pistonMovingBlockEntity.getXOff(g),
				e - (double)blockPos.getY() + (double)pistonMovingBlockEntity.getYOff(g),
				f - (double)blockPos.getZ() + (double)pistonMovingBlockEntity.getZOff(g)
			);
			Level level = this.getLevel();
			if (blockState.getBlock() == Blocks.PISTON_HEAD && pistonMovingBlockEntity.getProgress(g) <= 4.0F) {
				blockState = blockState.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(true));
				this.renderBlock(blockPos, blockState, bufferBuilder, level, false);
			} else if (pistonMovingBlockEntity.isSourcePiston() && !pistonMovingBlockEntity.isExtending()) {
				PistonType pistonType = blockState.getBlock() == Blocks.STICKY_PISTON ? PistonType.STICKY : PistonType.DEFAULT;
				BlockState blockState2 = Blocks.PISTON_HEAD
					.defaultBlockState()
					.setValue(PistonHeadBlock.TYPE, pistonType)
					.setValue(PistonHeadBlock.FACING, blockState.getValue(PistonBaseBlock.FACING));
				blockState2 = blockState2.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(pistonMovingBlockEntity.getProgress(g) >= 0.5F));
				this.renderBlock(blockPos, blockState2, bufferBuilder, level, false);
				BlockPos blockPos2 = blockPos.relative(pistonMovingBlockEntity.getMovementDirection());
				bufferBuilder.offset(d - (double)blockPos2.getX(), e - (double)blockPos2.getY(), f - (double)blockPos2.getZ());
				blockState = blockState.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true));
				this.renderBlock(blockPos2, blockState, bufferBuilder, level, true);
			} else {
				this.renderBlock(blockPos, blockState, bufferBuilder, level, false);
			}

			ModelBlockRenderer.clearCache();
		}
	}

	private boolean renderBlock(BlockPos blockPos, BlockState blockState, BufferBuilder bufferBuilder, Level level, boolean bl) {
		return this.blockRenderer
			.getModelRenderer()
			.tesselateBlock(level, this.blockRenderer.getBlockModel(blockState), blockState, blockPos, bufferBuilder, bl, new Random(), blockState.getSeed(blockPos));
	}
}
