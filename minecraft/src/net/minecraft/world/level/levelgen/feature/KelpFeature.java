package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class KelpFeature extends Feature<NoneFeatureConfiguration> {
	public KelpFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		int i = 0;
		int j = levelAccessor.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX(), blockPos.getZ());
		BlockPos blockPos2 = new BlockPos(blockPos.getX(), j, blockPos.getZ());
		if (levelAccessor.getBlockState(blockPos2).getBlock() == Blocks.WATER) {
			BlockState blockState = Blocks.KELP.defaultBlockState();
			BlockState blockState2 = Blocks.KELP_PLANT.defaultBlockState();
			int k = 1 + random.nextInt(10);

			for (int l = 0; l <= k; l++) {
				if (levelAccessor.getBlockState(blockPos2).getBlock() == Blocks.WATER
					&& levelAccessor.getBlockState(blockPos2.above()).getBlock() == Blocks.WATER
					&& blockState2.canSurvive(levelAccessor, blockPos2)) {
					if (l == k) {
						levelAccessor.setBlock(blockPos2, blockState.setValue(KelpBlock.AGE, Integer.valueOf(random.nextInt(23))), 2);
						i++;
					} else {
						levelAccessor.setBlock(blockPos2, blockState2, 2);
					}
				} else if (l > 0) {
					BlockPos blockPos3 = blockPos2.below();
					if (blockState.canSurvive(levelAccessor, blockPos3) && levelAccessor.getBlockState(blockPos3.below()).getBlock() != Blocks.KELP) {
						levelAccessor.setBlock(blockPos3, blockState.setValue(KelpBlock.AGE, Integer.valueOf(random.nextInt(23))), 2);
						i++;
					}
					break;
				}

				blockPos2 = blockPos2.above();
			}
		}

		return i > 0;
	}
}
