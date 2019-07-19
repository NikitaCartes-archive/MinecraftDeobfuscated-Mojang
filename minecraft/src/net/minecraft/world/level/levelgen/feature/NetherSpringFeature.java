package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.material.Fluids;

public class NetherSpringFeature extends Feature<HellSpringConfiguration> {
	private static final BlockState NETHERRACK = Blocks.NETHERRACK.defaultBlockState();

	public NetherSpringFeature(Function<Dynamic<?>, ? extends HellSpringConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		HellSpringConfiguration hellSpringConfiguration
	) {
		if (levelAccessor.getBlockState(blockPos.above()) != NETHERRACK) {
			return false;
		} else if (!levelAccessor.getBlockState(blockPos).isAir() && levelAccessor.getBlockState(blockPos) != NETHERRACK) {
			return false;
		} else {
			int i = 0;
			if (levelAccessor.getBlockState(blockPos.west()) == NETHERRACK) {
				i++;
			}

			if (levelAccessor.getBlockState(blockPos.east()) == NETHERRACK) {
				i++;
			}

			if (levelAccessor.getBlockState(blockPos.north()) == NETHERRACK) {
				i++;
			}

			if (levelAccessor.getBlockState(blockPos.south()) == NETHERRACK) {
				i++;
			}

			if (levelAccessor.getBlockState(blockPos.below()) == NETHERRACK) {
				i++;
			}

			int j = 0;
			if (levelAccessor.isEmptyBlock(blockPos.west())) {
				j++;
			}

			if (levelAccessor.isEmptyBlock(blockPos.east())) {
				j++;
			}

			if (levelAccessor.isEmptyBlock(blockPos.north())) {
				j++;
			}

			if (levelAccessor.isEmptyBlock(blockPos.south())) {
				j++;
			}

			if (levelAccessor.isEmptyBlock(blockPos.below())) {
				j++;
			}

			if (!hellSpringConfiguration.insideRock && i == 4 && j == 1 || i == 5) {
				levelAccessor.setBlock(blockPos, Blocks.LAVA.defaultBlockState(), 2);
				levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.LAVA, 0);
			}

			return true;
		}
	}
}
