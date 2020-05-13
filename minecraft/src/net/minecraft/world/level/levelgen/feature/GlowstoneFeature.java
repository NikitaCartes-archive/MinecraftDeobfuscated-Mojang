package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GlowstoneFeature extends Feature<NoneFeatureConfiguration> {
	public GlowstoneFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		if (!worldGenLevel.isEmptyBlock(blockPos)) {
			return false;
		} else {
			BlockState blockState = worldGenLevel.getBlockState(blockPos.above());
			if (!blockState.is(Blocks.NETHERRACK) && !blockState.is(Blocks.BASALT) && !blockState.is(Blocks.BLACKSTONE)) {
				return false;
			} else {
				worldGenLevel.setBlock(blockPos, Blocks.GLOWSTONE.defaultBlockState(), 2);

				for (int i = 0; i < 1500; i++) {
					BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), -random.nextInt(12), random.nextInt(8) - random.nextInt(8));
					if (worldGenLevel.getBlockState(blockPos2).isAir()) {
						int j = 0;

						for (Direction direction : Direction.values()) {
							if (worldGenLevel.getBlockState(blockPos2.relative(direction)).is(Blocks.GLOWSTONE)) {
								j++;
							}

							if (j > 1) {
								break;
							}
						}

						if (j == 1) {
							worldGenLevel.setBlock(blockPos2, Blocks.GLOWSTONE.defaultBlockState(), 2);
						}
					}
				}

				return true;
			}
		}
	}
}
