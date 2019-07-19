package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class SpringFeature extends Feature<SpringConfiguration> {
	public SpringFeature(Function<Dynamic<?>, ? extends SpringConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		SpringConfiguration springConfiguration
	) {
		if (!Block.equalsStone(levelAccessor.getBlockState(blockPos.above()).getBlock())) {
			return false;
		} else if (!Block.equalsStone(levelAccessor.getBlockState(blockPos.below()).getBlock())) {
			return false;
		} else {
			BlockState blockState = levelAccessor.getBlockState(blockPos);
			if (!blockState.isAir() && !Block.equalsStone(blockState.getBlock())) {
				return false;
			} else {
				int i = 0;
				int j = 0;
				if (Block.equalsStone(levelAccessor.getBlockState(blockPos.west()).getBlock())) {
					j++;
				}

				if (Block.equalsStone(levelAccessor.getBlockState(blockPos.east()).getBlock())) {
					j++;
				}

				if (Block.equalsStone(levelAccessor.getBlockState(blockPos.north()).getBlock())) {
					j++;
				}

				if (Block.equalsStone(levelAccessor.getBlockState(blockPos.south()).getBlock())) {
					j++;
				}

				int k = 0;
				if (levelAccessor.isEmptyBlock(blockPos.west())) {
					k++;
				}

				if (levelAccessor.isEmptyBlock(blockPos.east())) {
					k++;
				}

				if (levelAccessor.isEmptyBlock(blockPos.north())) {
					k++;
				}

				if (levelAccessor.isEmptyBlock(blockPos.south())) {
					k++;
				}

				if (j == 3 && k == 1) {
					levelAccessor.setBlock(blockPos, springConfiguration.state.createLegacyBlock(), 2);
					levelAccessor.getLiquidTicks().scheduleTick(blockPos, springConfiguration.state.getType(), 0);
					i++;
				}

				return i > 0;
			}
		}
	}
}
