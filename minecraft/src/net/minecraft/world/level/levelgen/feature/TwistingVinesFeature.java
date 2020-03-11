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
	public TwistingVinesFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		if (this.isInvalidPlacementLocation(levelAccessor, blockPos)) {
			return false;
		} else {
			this.placeTwistingVines(levelAccessor, random, blockPos);
			return true;
		}
	}

	private void placeTwistingVines(LevelAccessor levelAccessor, Random random, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int i = 0; i < 100; i++) {
			mutableBlockPos.set(blockPos).move(Mth.nextInt(random, -8, 8), Mth.nextInt(random, -2, 7), Mth.nextInt(random, -8, 8));

			while (levelAccessor.getBlockState(mutableBlockPos.below()).isAir()) {
				mutableBlockPos.move(0, -1, 0);
			}

			if (!this.isInvalidPlacementLocation(levelAccessor, mutableBlockPos)) {
				int j = Mth.nextInt(random, 1, 8);
				if (random.nextInt(6) == 0) {
					j *= 2;
				}

				if (random.nextInt(5) == 0) {
					j = 1;
				}

				int k = 17;
				int l = 25;
				placeWeepingVinesColumn(levelAccessor, random, mutableBlockPos, j, 17, 25);
			}
		}
	}

	public static void placeWeepingVinesColumn(LevelAccessor levelAccessor, Random random, BlockPos.MutableBlockPos mutableBlockPos, int i, int j, int k) {
		for (int l = 0; l <= i; l++) {
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

	private boolean isInvalidPlacementLocation(LevelAccessor levelAccessor, BlockPos blockPos) {
		if (!levelAccessor.isEmptyBlock(blockPos)) {
			return true;
		} else {
			Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
			return block != Blocks.NETHERRACK && block != Blocks.WARPED_NYLIUM && block != Blocks.WARPED_WART_BLOCK;
		}
	}
}
