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

public class HugeBrownMushroomFeature extends Feature<HugeMushroomFeatureConfig> {
	public HugeBrownMushroomFeature(Function<Dynamic<?>, ? extends HugeMushroomFeatureConfig> function) {
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

				for (int k = 0; k <= 1 + i; k++) {
					int l = k <= 3 ? 0 : 3;

					for (int m = -l; m <= l; m++) {
						for (int n = -l; n <= l; n++) {
							BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.set(blockPos).move(m, k, n));
							if (!blockState.isAir() && !blockState.is(BlockTags.LEAVES)) {
								return false;
							}
						}
					}
				}

				BlockState blockState2 = Blocks.BROWN_MUSHROOM_BLOCK
					.defaultBlockState()
					.setValue(HugeMushroomBlock.UP, Boolean.valueOf(true))
					.setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false));
				int l = 3;

				for (int m = -3; m <= 3; m++) {
					for (int nx = -3; nx <= 3; nx++) {
						boolean bl = m == -3;
						boolean bl2 = m == 3;
						boolean bl3 = nx == -3;
						boolean bl4 = nx == 3;
						boolean bl5 = bl || bl2;
						boolean bl6 = bl3 || bl4;
						if (!bl5 || !bl6) {
							mutableBlockPos.set(blockPos).move(m, i, nx);
							if (!levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) {
								boolean bl7 = bl || bl6 && m == -2;
								boolean bl8 = bl2 || bl6 && m == 2;
								boolean bl9 = bl3 || bl5 && nx == -2;
								boolean bl10 = bl4 || bl5 && nx == 2;
								this.setBlock(
									levelAccessor,
									mutableBlockPos,
									blockState2.setValue(HugeMushroomBlock.WEST, Boolean.valueOf(bl7))
										.setValue(HugeMushroomBlock.EAST, Boolean.valueOf(bl8))
										.setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(bl9))
										.setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(bl10))
								);
							}
						}
					}
				}

				BlockState blockState3 = Blocks.MUSHROOM_STEM
					.defaultBlockState()
					.setValue(HugeMushroomBlock.UP, Boolean.valueOf(false))
					.setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false));

				for (int nxx = 0; nxx < i; nxx++) {
					mutableBlockPos.set(blockPos).move(Direction.UP, nxx);
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
