package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.CountFeatureConfiguration;

public class SeaPickleFeature extends Feature<CountFeatureConfiguration> {
	public SeaPickleFeature(Function<Dynamic<?>, ? extends CountFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, Random random, BlockPos blockPos, CountFeatureConfiguration countFeatureConfiguration
	) {
		int i = 0;

		for (int j = 0; j < countFeatureConfiguration.count; j++) {
			int k = random.nextInt(8) - random.nextInt(8);
			int l = random.nextInt(8) - random.nextInt(8);
			int m = levelAccessor.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX() + k, blockPos.getZ() + l);
			BlockPos blockPos2 = new BlockPos(blockPos.getX() + k, m, blockPos.getZ() + l);
			BlockState blockState = Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(random.nextInt(4) + 1));
			if (levelAccessor.getBlockState(blockPos2).getBlock() == Blocks.WATER && blockState.canSurvive(levelAccessor, blockPos2)) {
				levelAccessor.setBlock(blockPos2, blockState, 2);
				i++;
			}
		}

		return i > 0;
	}
}
