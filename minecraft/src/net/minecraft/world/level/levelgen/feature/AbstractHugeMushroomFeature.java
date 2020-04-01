package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public abstract class AbstractHugeMushroomFeature extends Feature<HugeMushroomFeatureConfiguration> {
	public AbstractHugeMushroomFeature(
		Function<Dynamic<?>, ? extends HugeMushroomFeatureConfiguration> function, Function<Random, ? extends HugeMushroomFeatureConfiguration> function2
	) {
		super(function, function2);
	}

	protected void placeTrunk(
		LevelAccessor levelAccessor,
		Random random,
		BlockPos blockPos,
		HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration,
		int i,
		BlockPos.MutableBlockPos mutableBlockPos
	) {
		for (int j = 0; j < i; j++) {
			mutableBlockPos.set(blockPos).move(Direction.UP, j);
			if (!levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) {
				this.setBlock(levelAccessor, mutableBlockPos, hugeMushroomFeatureConfiguration.stemProvider.getState(random, blockPos));
			}
		}
	}

	protected int getTreeHeight(Random random) {
		int i = random.nextInt(3) + 4;
		if (random.nextInt(12) == 0) {
			i *= 2;
		}

		return i;
	}

	protected boolean isValidPosition(
		LevelAccessor levelAccessor,
		BlockPos blockPos,
		int i,
		BlockPos.MutableBlockPos mutableBlockPos,
		HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration
	) {
		int j = blockPos.getY();
		if (j >= 1 && j + i + 1 < 256) {
			Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
			if (!isDirt(block)) {
				return false;
			} else {
				for (int k = 0; k <= i; k++) {
					int l = this.getTreeRadiusForHeight(-1, -1, hugeMushroomFeatureConfiguration.foliageRadius, k);

					for (int m = -l; m <= l; m++) {
						for (int n = -l; n <= l; n++) {
							BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.setWithOffset(blockPos, m, k, n));
							if (!blockState.isAir() && !blockState.is(BlockTags.LEAVES)) {
								return false;
							}
						}
					}
				}

				return true;
			}
		} else {
			return false;
		}
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration
	) {
		int i = this.getTreeHeight(random);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		if (!this.isValidPosition(levelAccessor, blockPos, i, mutableBlockPos, hugeMushroomFeatureConfiguration)) {
			return false;
		} else {
			this.makeCap(levelAccessor, random, blockPos, i, mutableBlockPos, hugeMushroomFeatureConfiguration);
			this.placeTrunk(levelAccessor, random, blockPos, hugeMushroomFeatureConfiguration, i, mutableBlockPos);
			return true;
		}
	}

	protected abstract int getTreeRadiusForHeight(int i, int j, int k, int l);

	protected abstract void makeCap(
		LevelAccessor levelAccessor,
		Random random,
		BlockPos blockPos,
		int i,
		BlockPos.MutableBlockPos mutableBlockPos,
		HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration
	);
}
