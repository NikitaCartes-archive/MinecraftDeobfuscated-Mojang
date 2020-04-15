package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;

public class DeltaFeature extends Feature<DeltaFeatureConfiguration> {
	private static final Direction[] DIRECTIONS = Direction.values();

	private static int calculateRadius(Random random, DeltaFeatureConfiguration deltaFeatureConfiguration) {
		return deltaFeatureConfiguration.minimumRadius + random.nextInt(deltaFeatureConfiguration.maximumRadius - deltaFeatureConfiguration.minimumRadius + 1);
	}

	private static int calculateRimSize(Random random, DeltaFeatureConfiguration deltaFeatureConfiguration) {
		return random.nextInt(deltaFeatureConfiguration.maximumRimSize + 1);
	}

	public DeltaFeature(Function<Dynamic<?>, ? extends DeltaFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		DeltaFeatureConfiguration deltaFeatureConfiguration
	) {
		BlockPos blockPos2 = findDeltaLevel(levelAccessor, blockPos.mutable().clamp(Direction.Axis.Y, 1, levelAccessor.getMaxBuildHeight() - 1));
		if (blockPos2 == null) {
			return false;
		} else {
			boolean bl = false;
			boolean bl2 = random.nextDouble() < 0.9;
			int i = bl2 ? calculateRimSize(random, deltaFeatureConfiguration) : 0;
			int j = bl2 ? calculateRimSize(random, deltaFeatureConfiguration) : 0;
			boolean bl3 = bl2 && i != 0 && j != 0;
			int k = calculateRadius(random, deltaFeatureConfiguration);
			int l = calculateRadius(random, deltaFeatureConfiguration);
			int m = Math.max(k, l);

			for (BlockPos blockPos3 : BlockPos.withinManhattan(blockPos2, k, 0, l)) {
				if (blockPos3.distManhattan(blockPos2) > m) {
					break;
				}

				if (isClear(levelAccessor, blockPos3, deltaFeatureConfiguration)) {
					if (bl3) {
						bl = true;
						this.setBlock(levelAccessor, blockPos3, deltaFeatureConfiguration.rim);
					}

					BlockPos blockPos4 = blockPos3.offset(i, 0, j);
					if (isClear(levelAccessor, blockPos4, deltaFeatureConfiguration)) {
						bl = true;
						this.setBlock(levelAccessor, blockPos4, deltaFeatureConfiguration.contents);
					}
				}
			}

			return bl;
		}
	}

	private static boolean isClear(LevelAccessor levelAccessor, BlockPos blockPos, DeltaFeatureConfiguration deltaFeatureConfiguration) {
		if (levelAccessor.getBlockState(blockPos).getBlock() == deltaFeatureConfiguration.contents.getBlock()) {
			return false;
		} else {
			for (Direction direction : DIRECTIONS) {
				boolean bl = levelAccessor.getBlockState(blockPos.relative(direction)).isAir();
				if (bl && direction != Direction.UP || !bl && direction == Direction.UP) {
					return false;
				}
			}

			return true;
		}
	}

	@Nullable
	private static BlockPos findDeltaLevel(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos) {
		while (mutableBlockPos.getY() > 1) {
			if (levelAccessor.getBlockState(mutableBlockPos).isAir()) {
				BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.move(Direction.DOWN));
				mutableBlockPos.move(Direction.UP);
				Block block = blockState.getBlock();
				if (block != Blocks.LAVA && block != Blocks.BEDROCK && !blockState.isAir()) {
					return mutableBlockPos;
				}
			}

			mutableBlockPos.move(Direction.DOWN);
		}

		return null;
	}
}
