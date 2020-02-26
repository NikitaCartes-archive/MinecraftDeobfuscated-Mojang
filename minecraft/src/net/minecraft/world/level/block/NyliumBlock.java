package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LayerLightEngine;

public class NyliumBlock extends Block {
	protected NyliumBlock(Block.Properties properties) {
		super(properties);
	}

	private static boolean canBeNylium(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		int i = LayerLightEngine.getLightBlockInto(
			levelReader, blockState, blockPos, blockState2, blockPos2, Direction.UP, blockState2.getLightBlock(levelReader, blockPos2)
		);
		return i < levelReader.getMaxLightLevel();
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!canBeNylium(blockState, serverLevel, blockPos)) {
			serverLevel.setBlockAndUpdate(blockPos, Blocks.NETHERRACK.defaultBlockState());
		}
	}
}
