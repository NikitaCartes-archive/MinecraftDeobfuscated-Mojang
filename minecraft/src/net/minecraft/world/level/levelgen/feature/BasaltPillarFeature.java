package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BasaltPillarFeature extends Feature<NoneFeatureConfiguration> {
	public BasaltPillarFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		if (levelAccessor.isEmptyBlock(blockPos) && !levelAccessor.isEmptyBlock(blockPos.above())) {
			BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
			BlockPos.MutableBlockPos mutableBlockPos2 = blockPos.mutable();
			boolean bl = true;
			boolean bl2 = true;
			boolean bl3 = true;
			boolean bl4 = true;

			while (levelAccessor.isEmptyBlock(mutableBlockPos)) {
				levelAccessor.setBlock(mutableBlockPos, Blocks.BASALT.defaultBlockState(), 2);
				bl = bl && this.placeHangOff(levelAccessor, random, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.NORTH));
				bl2 = bl2 && this.placeHangOff(levelAccessor, random, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.SOUTH));
				bl3 = bl3 && this.placeHangOff(levelAccessor, random, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.WEST));
				bl4 = bl4 && this.placeHangOff(levelAccessor, random, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.EAST));
				mutableBlockPos.move(Direction.DOWN);
			}

			mutableBlockPos.move(Direction.UP);
			this.placeBaseHangOff(levelAccessor, random, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.NORTH));
			this.placeBaseHangOff(levelAccessor, random, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.SOUTH));
			this.placeBaseHangOff(levelAccessor, random, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.WEST));
			this.placeBaseHangOff(levelAccessor, random, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.EAST));
			BlockPos.MutableBlockPos mutableBlockPos3 = new BlockPos.MutableBlockPos();

			for (int i = -3; i < 4; i++) {
				for (int j = -3; j < 4; j++) {
					int k = Mth.abs(i) * Mth.abs(j);
					if (random.nextInt(10) < 10 - k) {
						mutableBlockPos3.set(mutableBlockPos.offset(i, 0, j));
						int l = 3;

						while (levelAccessor.isEmptyBlock(mutableBlockPos2.setWithOffset(mutableBlockPos3, Direction.DOWN))) {
							mutableBlockPos3.move(Direction.DOWN);
							if (--l <= 0) {
								break;
							}
						}

						if (!levelAccessor.isEmptyBlock(mutableBlockPos2.setWithOffset(mutableBlockPos3, Direction.DOWN))) {
							levelAccessor.setBlock(mutableBlockPos3, Blocks.BASALT.defaultBlockState(), 2);
						}
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

	private void placeBaseHangOff(LevelAccessor levelAccessor, Random random, BlockPos blockPos) {
		if (random.nextBoolean()) {
			levelAccessor.setBlock(blockPos, Blocks.BASALT.defaultBlockState(), 2);
		}
	}

	private boolean placeHangOff(LevelAccessor levelAccessor, Random random, BlockPos blockPos) {
		if (random.nextInt(10) != 0) {
			levelAccessor.setBlock(blockPos, Blocks.BASALT.defaultBlockState(), 2);
			return true;
		} else {
			return false;
		}
	}
}
