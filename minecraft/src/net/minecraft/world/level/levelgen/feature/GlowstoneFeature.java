package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GlowstoneFeature extends Feature<NoneFeatureConfiguration> {
	public GlowstoneFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		if (!levelAccessor.isEmptyBlock(blockPos)) {
			return false;
		} else if (levelAccessor.getBlockState(blockPos.above()).getBlock() != Blocks.NETHERRACK) {
			return false;
		} else {
			levelAccessor.setBlock(blockPos, Blocks.GLOWSTONE.defaultBlockState(), 2);

			for (int i = 0; i < 1500; i++) {
				BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), -random.nextInt(12), random.nextInt(8) - random.nextInt(8));
				if (levelAccessor.getBlockState(blockPos2).isAir()) {
					int j = 0;

					for (Direction direction : Direction.values()) {
						if (levelAccessor.getBlockState(blockPos2.relative(direction)).getBlock() == Blocks.GLOWSTONE) {
							j++;
						}

						if (j > 1) {
							break;
						}
					}

					if (j == 1) {
						levelAccessor.setBlock(blockPos2, Blocks.GLOWSTONE.defaultBlockState(), 2);
					}
				}
			}

			return true;
		}
	}
}
