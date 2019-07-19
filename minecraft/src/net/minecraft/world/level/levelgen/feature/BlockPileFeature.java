package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public abstract class BlockPileFeature extends Feature<NoneFeatureConfiguration> {
	public BlockPileFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		if (blockPos.getY() < 5) {
			return false;
		} else {
			int i = 2 + random.nextInt(2);
			int j = 2 + random.nextInt(2);

			for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-i, 0, -j), blockPos.offset(i, 1, j))) {
				int k = blockPos.getX() - blockPos2.getX();
				int l = blockPos.getZ() - blockPos2.getZ();
				if ((float)(k * k + l * l) <= random.nextFloat() * 10.0F - random.nextFloat() * 6.0F) {
					this.tryPlaceBlock(levelAccessor, blockPos2, random);
				} else if ((double)random.nextFloat() < 0.031) {
					this.tryPlaceBlock(levelAccessor, blockPos2, random);
				}
			}

			return true;
		}
	}

	private boolean mayPlaceOn(LevelAccessor levelAccessor, BlockPos blockPos, Random random) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState = levelAccessor.getBlockState(blockPos2);
		return blockState.getBlock() == Blocks.GRASS_PATH ? random.nextBoolean() : blockState.isFaceSturdy(levelAccessor, blockPos2, Direction.UP);
	}

	private void tryPlaceBlock(LevelAccessor levelAccessor, BlockPos blockPos, Random random) {
		if (levelAccessor.isEmptyBlock(blockPos) && this.mayPlaceOn(levelAccessor, blockPos, random)) {
			levelAccessor.setBlock(blockPos, this.getBlockState(levelAccessor), 4);
		}
	}

	protected abstract BlockState getBlockState(LevelAccessor levelAccessor);
}
