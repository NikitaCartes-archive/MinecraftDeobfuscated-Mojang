package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class TwistingVinesFeature extends Feature<NoneFeatureConfiguration> {
	public TwistingVinesFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, Function<Random, ? extends NoneFeatureConfiguration> function2) {
		super(function, function2);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		return place(levelAccessor, random, blockPos, 8, 4, 8);
	}

	public static boolean place(LevelAccessor levelAccessor, Random random, BlockPos blockPos, int i, int j, int k) {
		if (isInvalidPlacementLocation(levelAccessor, blockPos)) {
			return false;
		} else {
			placeTwistingVines(levelAccessor, random, blockPos, i, j, k);
			return true;
		}
	}

	private static void placeTwistingVines(LevelAccessor levelAccessor, Random random, BlockPos blockPos, int i, int j, int k) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int l = 0; l < i * i; l++) {
			mutableBlockPos.set(blockPos).move(Mth.nextInt(random, -i, i), Mth.nextInt(random, -j, j), Mth.nextInt(random, -i, i));

			while (levelAccessor.getBlockState(mutableBlockPos.below()).isAir()) {
				mutableBlockPos.move(0, -1, 0);
			}

			if (!isInvalidPlacementLocation(levelAccessor, mutableBlockPos)) {
				int m = Mth.nextInt(random, 1, k);
				if (random.nextInt(6) == 0) {
					m *= 2;
				}

				if (random.nextInt(5) == 0) {
					m = 1;
				}

				int n = 17;
				int o = 25;
				placeWeepingVinesColumn(levelAccessor, random, mutableBlockPos, m, 17, 25);
			}
		}
	}

	public static void placeWeepingVinesColumn(LevelAccessor levelAccessor, Random random, BlockPos.MutableBlockPos mutableBlockPos, int i, int j, int k) {
		for (int l = 1; l <= i; l++) {
			if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
				if (l == i || !levelAccessor.isEmptyBlock(mutableBlockPos.above())) {
					levelAccessor.setBlock(
						mutableBlockPos, Blocks.TWISTING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(random, j, k))), 2
					);
					break;
				}

				levelAccessor.setBlock(mutableBlockPos, Blocks.TWISTING_VINES_PLANT.defaultBlockState(), 2);
			}

			mutableBlockPos.move(Direction.UP);
		}
	}

	private static boolean isInvalidPlacementLocation(LevelAccessor levelAccessor, BlockPos blockPos) {
		if (!levelAccessor.isEmptyBlock(blockPos)) {
			return true;
		} else {
			Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
			return block != Blocks.NETHERRACK && block != Blocks.WARPED_NYLIUM && block != Blocks.WARPED_WART_BLOCK;
		}
	}
}
