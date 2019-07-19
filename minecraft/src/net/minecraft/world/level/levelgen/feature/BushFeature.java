package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class BushFeature extends Feature<BushConfiguration> {
	public BushFeature(Function<Dynamic<?>, ? extends BushConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		BushConfiguration bushConfiguration
	) {
		int i = 0;
		BlockState blockState = bushConfiguration.state;

		for (int j = 0; j < 64; j++) {
			BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
			if (levelAccessor.isEmptyBlock(blockPos2)
				&& (!levelAccessor.getDimension().isHasCeiling() || blockPos2.getY() < 255)
				&& blockState.canSurvive(levelAccessor, blockPos2)) {
				levelAccessor.setBlock(blockPos2, blockState, 2);
				i++;
			}
		}

		return i > 0;
	}
}
