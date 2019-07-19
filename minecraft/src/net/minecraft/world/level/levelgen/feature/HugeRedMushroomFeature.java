package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class HugeRedMushroomFeature extends Feature<HugeMushroomFeatureConfig> {
	public HugeRedMushroomFeature(Function<Dynamic<?>, ? extends HugeMushroomFeatureConfig> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		HugeMushroomFeatureConfig hugeMushroomFeatureConfig
	) {
		int i = random.nextInt(3) + 4;
		if (random.nextInt(12) == 0) {
			i *= 2;
		}

		int j = blockPos.getY();
		if (j >= 1 && j + i + 1 < 256) {
			Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
			if (!Block.equalsDirt(block) && block != Blocks.GRASS_BLOCK && block != Blocks.MYCELIUM) {
				return false;
			} else {
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int k = 0; k <= i; k++) {
					int l = 0;
					if (k < i && k >= i - 3) {
						l = 2;
					} else if (k == i) {
						l = 1;
					}

					for (int m = -l; m <= l; m++) {
						for (int n = -l; n <= l; n++) {
							BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.set(blockPos).move(m, k, n));
							if (!blockState.isAir() && !blockState.is(BlockTags.LEAVES)) {
								return false;
							}
						}
					}
				}

				BlockState blockState2 = Blocks.RED_MUSHROOM_BLOCK.defaultBlockState().setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false));

				for (int l = i - 3; l <= i; l++) {
					int m = l < i ? 2 : 1;
					int nx = 0;

					for (int o = -m; o <= m; o++) {
						for (int p = -m; p <= m; p++) {
							boolean bl = o == -m;
							boolean bl2 = o == m;
							boolean bl3 = p == -m;
							boolean bl4 = p == m;
							boolean bl5 = bl || bl2;
							boolean bl6 = bl3 || bl4;
							if (l >= i || bl5 != bl6) {
								mutableBlockPos.set(blockPos).move(o, l, p);
								if (!levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) {
									this.setBlock(
										levelAccessor,
										mutableBlockPos,
										blockState2.setValue(HugeMushroomBlock.UP, Boolean.valueOf(l >= i - 1))
											.setValue(HugeMushroomBlock.WEST, Boolean.valueOf(o < 0))
											.setValue(HugeMushroomBlock.EAST, Boolean.valueOf(o > 0))
											.setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(p < 0))
											.setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(p > 0))
									);
								}
							}
						}
					}
				}

				BlockState blockState3 = Blocks.MUSHROOM_STEM
					.defaultBlockState()
					.setValue(HugeMushroomBlock.UP, Boolean.valueOf(false))
					.setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false));

				for (int m = 0; m < i; m++) {
					mutableBlockPos.set(blockPos).move(Direction.UP, m);
					if (!levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) {
						if (hugeMushroomFeatureConfig.planted) {
							levelAccessor.setBlock(mutableBlockPos, blockState3, 3);
						} else {
							this.setBlock(levelAccessor, mutableBlockPos, blockState3);
						}
					}
				}

				return true;
			}
		} else {
			return false;
		}
	}
}
