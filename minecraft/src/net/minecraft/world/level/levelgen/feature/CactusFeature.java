package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class CactusFeature extends Feature<NoneFeatureConfiguration> {
	public CactusFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		for (int i = 0; i < 10; i++) {
			BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
			if (levelAccessor.isEmptyBlock(blockPos2)) {
				int j = 1 + random.nextInt(random.nextInt(3) + 1);

				for (int k = 0; k < j; k++) {
					if (Blocks.CACTUS.defaultBlockState().canSurvive(levelAccessor, blockPos2)) {
						levelAccessor.setBlock(blockPos2.above(k), Blocks.CACTUS.defaultBlockState(), 2);
					}
				}
			}
		}

		return true;
	}
}
