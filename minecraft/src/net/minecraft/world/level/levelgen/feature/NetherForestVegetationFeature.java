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
		for (Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
			!block.is(BlockTags.NYLIUM) && blockPos.getY() > 0;
			block = levelAccessor.getBlockState(blockPos).getBlock()
		) {
			blockPos = blockPos.below();
		}

		int i = blockPos.getY();
		if (i >= 1 && i + 1 < 256) {
			int j = 0;

			for (int k = 0; k < 64; k++) {
				BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
				BlockState blockState = blockPileConfiguration.stateProvider.getState(random, blockPos2);
				if (levelAccessor.isEmptyBlock(blockPos2) && blockPos2.getY() > 0 && blockState.canSurvive(levelAccessor, blockPos2)) {
					levelAccessor.setBlock(blockPos2, blockState, 2);
					j++;
				}
			}

			return j > 0;
		} else {
			return false;
		}
	}
}
