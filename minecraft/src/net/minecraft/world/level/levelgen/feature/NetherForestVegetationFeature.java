package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class NetherForestVegetationFeature extends Feature<BlockPileConfiguration> {
	public NetherForestVegetationFeature(Function<Dynamic<?>, ? extends BlockPileConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		BlockPileConfiguration blockPileConfiguration
	) {
		return place(levelAccessor, random, blockPos, blockPileConfiguration, 8, 4);
	}

	public static boolean place(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockPileConfiguration blockPileConfiguration, int i, int j) {
		for (Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
			!block.is(BlockTags.NYLIUM) && blockPos.getY() > 0;
			block = levelAccessor.getBlockState(blockPos).getBlock()
		) {
			blockPos = blockPos.below();
		}

		int k = blockPos.getY();
		if (k >= 1 && k + 1 < 256) {
			int l = 0;

			for (int m = 0; m < i * i; m++) {
				BlockPos blockPos2 = blockPos.offset(random.nextInt(i) - random.nextInt(i), random.nextInt(j) - random.nextInt(j), random.nextInt(i) - random.nextInt(i));
				BlockState blockState = blockPileConfiguration.stateProvider.getState(random, blockPos2);
				if (levelAccessor.isEmptyBlock(blockPos2) && blockPos2.getY() > 0 && blockState.canSurvive(levelAccessor, blockPos2)) {
					levelAccessor.setBlock(blockPos2, blockState, 2);
					l++;
				}
			}

			return l > 0;
		} else {
			return false;
		}
	}
}
