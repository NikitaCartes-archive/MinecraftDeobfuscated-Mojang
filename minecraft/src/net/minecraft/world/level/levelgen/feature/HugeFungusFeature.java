package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.material.Material;

public class HugeFungusFeature extends Feature<HugeFungusConfiguration> {
	public HugeFungusFeature(Function<Dynamic<?>, ? extends HugeFungusConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		HugeFungusConfiguration hugeFungusConfiguration
	) {
		Block block = hugeFungusConfiguration.validBaseState.getBlock();
		BlockPos blockPos2 = null;
		if (hugeFungusConfiguration.planted) {
			Block block2 = levelAccessor.getBlockState(blockPos.below()).getBlock();
			if (block2 == block) {
				blockPos2 = blockPos;
			}
		} else {
			blockPos2 = findOnNyliumPosition(levelAccessor, blockPos, block);
		}

		if (blockPos2 == null) {
			return false;
		} else {
			int i = Mth.nextInt(random, 4, 13);
			if (random.nextInt(12) == 0) {
				i *= 2;
			}

			if (!hugeFungusConfiguration.planted) {
				int j = levelAccessor.getHeight();
				if (blockPos2.getY() + i + 1 >= j) {
					return false;
				}
			}

			boolean bl = !hugeFungusConfiguration.planted && random.nextFloat() < 0.06F;
			levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 4);
			this.placeHat(levelAccessor, random, hugeFungusConfiguration, blockPos2, i, bl);
			this.placeStem(levelAccessor, random, hugeFungusConfiguration, blockPos2, i, bl);
			return true;
		}
	}

	public static boolean isReplaceablePlant(LevelAccessor levelAccessor, BlockPos blockPos) {
		return levelAccessor.isStateAtPosition(blockPos, blockState -> {
			Material material = blockState.getMaterial();
			return material == Material.REPLACEABLE_PLANT;
		});
	}

	private static boolean isReplaceable(LevelAccessor levelAccessor, BlockPos blockPos) {
		return levelAccessor.getBlockState(blockPos).isAir() || !levelAccessor.getFluidState(blockPos).isEmpty() || isReplaceablePlant(levelAccessor, blockPos);
	}

	private void placeStem(LevelAccessor levelAccessor, Random random, HugeFungusConfiguration hugeFungusConfiguration, BlockPos blockPos, int i, boolean bl) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockState blockState = hugeFungusConfiguration.stemState;
		int j = bl ? 1 : 0;

		for (int k = -j; k <= j; k++) {
			for (int l = -j; l <= j; l++) {
				boolean bl2 = bl && Mth.abs(k) == j && Mth.abs(l) == j;

				for (int m = 0; m < i; m++) {
					mutableBlockPos.setWithOffset(blockPos, k, m, l);
					if (isReplaceable(levelAccessor, mutableBlockPos)) {
						if (hugeFungusConfiguration.planted) {
							if (!levelAccessor.getBlockState(mutableBlockPos.below()).isAir()) {
								levelAccessor.destroyBlock(mutableBlockPos, true);
							}

							levelAccessor.setBlock(mutableBlockPos, blockState, 3);
						} else if (bl2) {
							if (random.nextFloat() < 0.1F) {
								this.setBlock(levelAccessor, mutableBlockPos, blockState);
							}
						} else {
							this.setBlock(levelAccessor, mutableBlockPos, blockState);
						}
					}
				}
			}
		}
	}

	private void placeHat(LevelAccessor levelAccessor, Random random, HugeFungusConfiguration hugeFungusConfiguration, BlockPos blockPos, int i, boolean bl) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		boolean bl2 = hugeFungusConfiguration.hatState.getBlock() == Blocks.NETHER_WART_BLOCK;
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
					mutableBlockPos.setWithOffset(blockPos, n, l, o);
					if (isReplaceable(levelAccessor, mutableBlockPos)) {
						if (hugeFungusConfiguration.planted && !levelAccessor.getBlockState(mutableBlockPos.below()).isAir()) {
							levelAccessor.destroyBlock(mutableBlockPos, true);
						}

						if (bl7) {
							if (!bl5) {
								this.placeHatDropBlock(levelAccessor, random, mutableBlockPos, hugeFungusConfiguration.hatState, bl2);
							}
						} else if (bl5) {
							this.placeHatBlock(levelAccessor, random, hugeFungusConfiguration, mutableBlockPos, 0.1F, 0.2F, bl2 ? 0.1F : 0.0F);
						} else if (bl6) {
							this.placeHatBlock(levelAccessor, random, hugeFungusConfiguration, mutableBlockPos, 0.01F, 0.7F, bl2 ? 0.083F : 0.0F);
						} else {
							this.placeHatBlock(levelAccessor, random, hugeFungusConfiguration, mutableBlockPos, 5.0E-4F, 0.98F, bl2 ? 0.07F : 0.0F);
						}
					}
				}
			}
		}
	}

	private void placeHatBlock(
		LevelAccessor levelAccessor,
		Random random,
		HugeFungusConfiguration hugeFungusConfiguration,
		BlockPos.MutableBlockPos mutableBlockPos,
		float f,
		float g,
		float h
	) {
		if (random.nextFloat() < f) {
			this.setBlock(levelAccessor, mutableBlockPos, hugeFungusConfiguration.decorState);
		} else if (random.nextFloat() < g) {
			this.setBlock(levelAccessor, mutableBlockPos, hugeFungusConfiguration.hatState);
			if (random.nextFloat() < h) {
				tryPlaceWeepingVines(mutableBlockPos, levelAccessor, random);
			}
		}
	}

	private void placeHatDropBlock(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState, boolean bl) {
		if (levelAccessor.getBlockState(blockPos.below()).getBlock() == blockState.getBlock()) {
			this.setBlock(levelAccessor, blockPos, blockState);
		} else if ((double)random.nextFloat() < 0.15) {
			this.setBlock(levelAccessor, blockPos, blockState);
			if (bl && random.nextInt(11) == 0) {
				tryPlaceWeepingVines(blockPos, levelAccessor, random);
			}
		}
	}

	@Nullable
	private static BlockPos.MutableBlockPos findOnNyliumPosition(LevelAccessor levelAccessor, BlockPos blockPos, Block block) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = blockPos.getY(); i >= 1; i--) {
			mutableBlockPos.setY(i);
			Block block2 = levelAccessor.getBlockState(mutableBlockPos.below()).getBlock();
			if (block2 == block) {
				return mutableBlockPos;
			}
		}

		return null;
	}

	private static void tryPlaceWeepingVines(BlockPos blockPos, LevelAccessor levelAccessor, Random random) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.DOWN);
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