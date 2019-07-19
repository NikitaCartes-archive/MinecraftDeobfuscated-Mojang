package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public abstract class FlowerFeature extends Feature<NoneFeatureConfiguration> {
	public FlowerFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function, false);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		BlockState blockState = this.getRandomFlower(random, blockPos);
		int i = 0;

		for (int j = 0; j < 64; j++) {
			BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
			if (levelAccessor.isEmptyBlock(blockPos2) && blockPos2.getY() < 255 && blockState.canSurvive(levelAccessor, blockPos2)) {
				levelAccessor.setBlock(blockPos2, blockState, 2);
				i++;
			}
		}

		return i > 0;
	}

	public abstract BlockState getRandomFlower(Random random, BlockPos blockPos);
}
