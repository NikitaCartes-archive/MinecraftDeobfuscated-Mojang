package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Material;

public class BlueIceFeature extends Feature<NoneFeatureConfiguration> {
	public BlueIceFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		if (blockPos.getY() > levelAccessor.getSeaLevel() - 1) {
			return false;
		} else if (!levelAccessor.getBlockState(blockPos).is(Blocks.WATER) && !levelAccessor.getBlockState(blockPos.below()).is(Blocks.WATER)) {
			return false;
		} else {
			boolean bl = false;

			for (Direction direction : Direction.values()) {
				if (direction != Direction.DOWN && levelAccessor.getBlockState(blockPos.relative(direction)).is(Blocks.PACKED_ICE)) {
					bl = true;
					break;
				}
			}

			if (!bl) {
				return false;
			} else {
				levelAccessor.setBlock(blockPos, Blocks.BLUE_ICE.defaultBlockState(), 2);

				for (int i = 0; i < 200; i++) {
					int j = random.nextInt(5) - random.nextInt(6);
					int k = 3;
					if (j < 2) {
						k += j / 2;
					}

					if (k >= 1) {
						BlockPos blockPos2 = blockPos.offset(random.nextInt(k) - random.nextInt(k), j, random.nextInt(k) - random.nextInt(k));
						BlockState blockState = levelAccessor.getBlockState(blockPos2);
						if (blockState.getMaterial() == Material.AIR || blockState.is(Blocks.WATER) || blockState.is(Blocks.PACKED_ICE) || blockState.is(Blocks.ICE)) {
							for (Direction direction2 : Direction.values()) {
								BlockState blockState2 = levelAccessor.getBlockState(blockPos2.relative(direction2));
								if (blockState2.is(Blocks.BLUE_ICE)) {
									levelAccessor.setBlock(blockPos2, Blocks.BLUE_ICE.defaultBlockState(), 2);
									break;
								}
							}
						}
					}
				}

				return true;
			}
		}
	}
}
