package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class TaigaGrassFeature extends Feature<NoneFeatureConfiguration> {
	public TaigaGrassFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public BlockState getState(Random random) {
		return random.nextInt(5) > 0 ? Blocks.FERN.defaultBlockState() : Blocks.GRASS.defaultBlockState();
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		BlockState blockState = this.getState(random);

		for (BlockState blockState2 = levelAccessor.getBlockState(blockPos);
			(blockState2.isAir() || blockState2.is(BlockTags.LEAVES)) && blockPos.getY() > 0;
			blockState2 = levelAccessor.getBlockState(blockPos)
		) {
			blockPos = blockPos.below();
		}

		int i = 0;

		for (int j = 0; j < 128; j++) {
			BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
			if (levelAccessor.isEmptyBlock(blockPos2) && blockState.canSurvive(levelAccessor, blockPos2)) {
				levelAccessor.setBlock(blockPos2, blockState, 2);
				i++;
			}
		}

		return i > 0;
	}
}
