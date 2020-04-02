package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class IceSpikeFeature extends Feature<NoneFeatureConfiguration> {
	public IceSpikeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		while (levelAccessor.isEmptyBlock(blockPos) && blockPos.getY() > 2) {
			blockPos = blockPos.below();
		}

		if (levelAccessor.getBlockState(blockPos).getBlock() != Blocks.SNOW_BLOCK) {
			return false;
		} else {
			blockPos = blockPos.above(random.nextInt(4));
			int i = random.nextInt(4) + 7;
			int j = i / 4 + random.nextInt(2);
			if (j > 1 && random.nextInt(60) == 0) {
				blockPos = blockPos.above(10 + random.nextInt(30));
			}

			for (int k = 0; k < i; k++) {
				float f = (1.0F - (float)k / (float)i) * (float)j;
				int l = Mth.ceil(f);

				for (int m = -l; m <= l; m++) {
					float g = (float)Mth.abs(m) - 0.25F;

					for (int n = -l; n <= l; n++) {
						float h = (float)Mth.abs(n) - 0.25F;
						if ((m == 0 && n == 0 || !(g * g + h * h > f * f)) && (m != -l && m != l && n != -l && n != l || !(random.nextFloat() > 0.75F))) {
							BlockState blockState = levelAccessor.getBlockState(blockPos.offset(m, k, n));
							Block block = blockState.getBlock();
							if (blockState.isAir() || isDirt(block) || block == Blocks.SNOW_BLOCK || block == Blocks.ICE) {
								this.setBlock(levelAccessor, blockPos.offset(m, k, n), Blocks.PACKED_ICE.defaultBlockState());
							}

							if (k != 0 && l > 1) {
								blockState = levelAccessor.getBlockState(blockPos.offset(m, -k, n));
								block = blockState.getBlock();
								if (blockState.isAir() || isDirt(block) || block == Blocks.SNOW_BLOCK || block == Blocks.ICE) {
									this.setBlock(levelAccessor, blockPos.offset(m, -k, n), Blocks.PACKED_ICE.defaultBlockState());
								}
							}
						}
					}
				}
			}

			int k = j - 1;
			if (k < 0) {
				k = 0;
			} else if (k > 1) {
				k = 1;
			}

			for (int o = -k; o <= k; o++) {
				for (int l = -k; l <= k; l++) {
					BlockPos blockPos2 = blockPos.offset(o, -1, l);
					int p = 50;
					if (Math.abs(o) == 1 && Math.abs(l) == 1) {
						p = random.nextInt(5);
					}

					while (blockPos2.getY() > 50) {
						BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
						Block block2 = blockState2.getBlock();
						if (!blockState2.isAir() && !isDirt(block2) && block2 != Blocks.SNOW_BLOCK && block2 != Blocks.ICE && block2 != Blocks.PACKED_ICE) {
							break;
						}

						this.setBlock(levelAccessor, blockPos2, Blocks.PACKED_ICE.defaultBlockState());
						blockPos2 = blockPos2.below();
						if (--p <= 0) {
							blockPos2 = blockPos2.below(random.nextInt(5) + 1);
							p = random.nextInt(5);
						}
					}
				}
			}

			return true;
		}
	}
}
