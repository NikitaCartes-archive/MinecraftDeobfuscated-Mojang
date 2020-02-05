package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class HugeFungiFeature extends Feature<HugeFungiConfiguration> {
	public HugeFungiFeature(Function<Dynamic<?>, ? extends HugeFungiConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		HugeFungiConfiguration hugeFungiConfiguration
	) {
		BlockPos.MutableBlockPos mutableBlockPos = findOnNyliumPosition(levelAccessor, blockPos);
		if (mutableBlockPos == null) {
			return false;
		} else {
			int i = Mth.nextInt(random, 4, 13);
			if (random.nextInt(12) == 0) {
				i *= 2;
			}

			if (mutableBlockPos.getY() + i + 1 >= 256) {
				return false;
			} else {
				boolean bl = !hugeFungiConfiguration.planted && random.nextFloat() < 0.06F;
				this.placeHat(levelAccessor, random, hugeFungiConfiguration, mutableBlockPos, i, bl);
				this.placeStem(levelAccessor, random, hugeFungiConfiguration, mutableBlockPos, i, bl);
				return true;
			}
		}
	}

	private void placeStem(
		LevelAccessor levelAccessor, Random random, HugeFungiConfiguration hugeFungiConfiguration, BlockPos.MutableBlockPos mutableBlockPos, int i, boolean bl
	) {
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
		BlockState blockState = hugeFungiConfiguration.stemState;
		int j = bl ? 1 : 0;

		for (int k = -j; k <= j; k++) {
			for (int l = -j; l <= j; l++) {
				boolean bl2 = bl && Mth.abs(k) == j && Mth.abs(l) == j;

				for (int m = 0; m < i; m++) {
					mutableBlockPos2.set(mutableBlockPos).move(k, m, l);
					if (!levelAccessor.getBlockState(mutableBlockPos2).isSolidRender(levelAccessor, mutableBlockPos2)) {
						if (hugeFungiConfiguration.planted) {
							levelAccessor.setBlock(mutableBlockPos2, blockState, 3);
						} else if (bl2) {
							if (random.nextFloat() < 0.1F) {
								this.setBlock(levelAccessor, mutableBlockPos2, blockState);
							}
						} else {
							this.setBlock(levelAccessor, mutableBlockPos2, blockState);
						}
					}
				}
			}
		}
	}

	private void placeHat(
		LevelAccessor levelAccessor, Random random, HugeFungiConfiguration hugeFungiConfiguration, BlockPos.MutableBlockPos mutableBlockPos, int i, boolean bl
	) {
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
		boolean bl2 = hugeFungiConfiguration.hatState.getBlock() == Blocks.NETHER_WART_BLOCK;
		int j = Math.min(random.nextInt(1 + i / 3) + 5, i);
		int k = i - j;

		for (int l = k; l <= i; l++) {
			int m = l < i - random.nextInt(3) ? 2 : 1;
			if (j > 8 && l < k + 4) {
				m = 3;
			}

			if (bl) {
				m++;
			}

			for (int n = -m; n <= m; n++) {
				for (int o = -m; o <= m; o++) {
					boolean bl3 = n == -m || n == m;
					boolean bl4 = o == -m || o == m;
					boolean bl5 = !bl3 && !bl4 && l != i;
					boolean bl6 = bl3 && bl4;
					boolean bl7 = l < k + 3;
					mutableBlockPos2.set(mutableBlockPos).move(n, l, o);
					if (!levelAccessor.getBlockState(mutableBlockPos2).isSolidRender(levelAccessor, mutableBlockPos2)) {
						if (bl7) {
							if (!bl5) {
								this.placeHatDropBlock(levelAccessor, random, mutableBlockPos2, hugeFungiConfiguration.hatState, bl2);
							}
						} else if (bl5) {
							this.placeHatBlock(levelAccessor, random, hugeFungiConfiguration, mutableBlockPos2, 0.1F, 0.2F, bl2 ? 0.1F : 0.0F);
						} else if (bl6) {
							this.placeHatBlock(levelAccessor, random, hugeFungiConfiguration, mutableBlockPos2, 0.01F, 0.7F, bl2 ? 0.083F : 0.0F);
						} else {
							this.placeHatBlock(levelAccessor, random, hugeFungiConfiguration, mutableBlockPos2, 5.0E-4F, 0.98F, bl2 ? 0.07F : 0.0F);
						}
					}
				}
			}
		}
	}

	private void placeHatBlock(
		LevelAccessor levelAccessor,
		Random random,
		HugeFungiConfiguration hugeFungiConfiguration,
		BlockPos.MutableBlockPos mutableBlockPos,
		float f,
		float g,
		float h
	) {
		if (random.nextFloat() < f) {
			this.setBlock(levelAccessor, mutableBlockPos, hugeFungiConfiguration.decorState);
		} else if (random.nextFloat() < g) {
			this.setBlock(levelAccessor, mutableBlockPos, hugeFungiConfiguration.hatState);
			if (random.nextFloat() < h) {
				this.tryPlaceWeepingVines(mutableBlockPos, levelAccessor, random);
			}
		}
	}

	private void placeHatDropBlock(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState, boolean bl) {
		if (levelAccessor.getBlockState(blockPos.below()).getBlock() == blockState.getBlock()) {
			this.setBlock(levelAccessor, blockPos, blockState);
		} else if ((double)random.nextFloat() < 0.15) {
			this.setBlock(levelAccessor, blockPos, blockState);
			if (bl && random.nextInt(11) == 0) {
				this.tryPlaceWeepingVines(blockPos, levelAccessor, random);
			}
		}
	}

	@Nullable
	private static BlockPos.MutableBlockPos findOnNyliumPosition(LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos);

		for (int i = blockPos.getY(); i >= 1; i--) {
			mutableBlockPos.setY(i);
			Block block = levelAccessor.getBlockState(mutableBlockPos.below()).getBlock();
			if (block.is(BlockTags.NYLIUM)) {
				return mutableBlockPos;
			}
		}

		return null;
	}

	private void tryPlaceWeepingVines(BlockPos blockPos, LevelAccessor levelAccessor, Random random) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos).move(Direction.DOWN);
		if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
			int i = Mth.nextInt(random, 1, 5);
			if (random.nextInt(7) == 0) {
				i *= 2;
			}

			int j = 23;
			int k = 25;
			WeepingVinesFeature.placeWeepingVinesColumn(levelAccessor, random, mutableBlockPos, i, 23, 25);
		}
	}
}
