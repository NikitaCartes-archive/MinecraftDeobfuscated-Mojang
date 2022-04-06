package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Material;

@Deprecated
public class LakeFeature extends Feature<LakeFeature.Configuration> {
	private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

	public LakeFeature(Codec<LakeFeature.Configuration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<LakeFeature.Configuration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		RandomSource randomSource = featurePlaceContext.random();
		LakeFeature.Configuration configuration = featurePlaceContext.config();
		if (blockPos.getY() <= worldGenLevel.getMinBuildHeight() + 4) {
			return false;
		} else {
			blockPos = blockPos.below(4);
			boolean[] bls = new boolean[2048];
			int i = randomSource.nextInt(4) + 4;

			for (int j = 0; j < i; j++) {
				double d = randomSource.nextDouble() * 6.0 + 3.0;
				double e = randomSource.nextDouble() * 4.0 + 2.0;
				double f = randomSource.nextDouble() * 6.0 + 3.0;
				double g = randomSource.nextDouble() * (16.0 - d - 2.0) + 1.0 + d / 2.0;
				double h = randomSource.nextDouble() * (8.0 - e - 4.0) + 2.0 + e / 2.0;
				double k = randomSource.nextDouble() * (16.0 - f - 2.0) + 1.0 + f / 2.0;

				for (int l = 1; l < 15; l++) {
					for (int m = 1; m < 15; m++) {
						for (int n = 1; n < 7; n++) {
							double o = ((double)l - g) / (d / 2.0);
							double p = ((double)n - h) / (e / 2.0);
							double q = ((double)m - k) / (f / 2.0);
							double r = o * o + p * p + q * q;
							if (r < 1.0) {
								bls[(l * 16 + m) * 8 + n] = true;
							}
						}
					}
				}
			}

			BlockState blockState = configuration.fluid().getState(randomSource, blockPos);

			for (int s = 0; s < 16; s++) {
				for (int t = 0; t < 16; t++) {
					for (int u = 0; u < 8; u++) {
						boolean bl = !bls[(s * 16 + t) * 8 + u]
							&& (
								s < 15 && bls[((s + 1) * 16 + t) * 8 + u]
									|| s > 0 && bls[((s - 1) * 16 + t) * 8 + u]
									|| t < 15 && bls[(s * 16 + t + 1) * 8 + u]
									|| t > 0 && bls[(s * 16 + (t - 1)) * 8 + u]
									|| u < 7 && bls[(s * 16 + t) * 8 + u + 1]
									|| u > 0 && bls[(s * 16 + t) * 8 + (u - 1)]
							);
						if (bl) {
							Material material = worldGenLevel.getBlockState(blockPos.offset(s, u, t)).getMaterial();
							if (u >= 4 && material.isLiquid()) {
								return false;
							}

							if (u < 4 && !material.isSolid() && worldGenLevel.getBlockState(blockPos.offset(s, u, t)) != blockState) {
								return false;
							}
						}
					}
				}
			}

			for (int s = 0; s < 16; s++) {
				for (int t = 0; t < 16; t++) {
					for (int ux = 0; ux < 8; ux++) {
						if (bls[(s * 16 + t) * 8 + ux]) {
							BlockPos blockPos2 = blockPos.offset(s, ux, t);
							if (this.canReplaceBlock(worldGenLevel.getBlockState(blockPos2))) {
								boolean bl2 = ux >= 4;
								worldGenLevel.setBlock(blockPos2, bl2 ? AIR : blockState, 2);
								if (bl2) {
									worldGenLevel.scheduleTick(blockPos2, AIR.getBlock(), 0);
									this.markAboveForPostProcessing(worldGenLevel, blockPos2);
								}
							}
						}
					}
				}
			}

			BlockState blockState2 = configuration.barrier().getState(randomSource, blockPos);
			if (!blockState2.isAir()) {
				for (int t = 0; t < 16; t++) {
					for (int uxx = 0; uxx < 16; uxx++) {
						for (int v = 0; v < 8; v++) {
							boolean bl2 = !bls[(t * 16 + uxx) * 8 + v]
								&& (
									t < 15 && bls[((t + 1) * 16 + uxx) * 8 + v]
										|| t > 0 && bls[((t - 1) * 16 + uxx) * 8 + v]
										|| uxx < 15 && bls[(t * 16 + uxx + 1) * 8 + v]
										|| uxx > 0 && bls[(t * 16 + (uxx - 1)) * 8 + v]
										|| v < 7 && bls[(t * 16 + uxx) * 8 + v + 1]
										|| v > 0 && bls[(t * 16 + uxx) * 8 + (v - 1)]
								);
							if (bl2 && (v < 4 || randomSource.nextInt(2) != 0)) {
								BlockState blockState3 = worldGenLevel.getBlockState(blockPos.offset(t, v, uxx));
								if (blockState3.getMaterial().isSolid() && !blockState3.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
									BlockPos blockPos3 = blockPos.offset(t, v, uxx);
									worldGenLevel.setBlock(blockPos3, blockState2, 2);
									this.markAboveForPostProcessing(worldGenLevel, blockPos3);
								}
							}
						}
					}
				}
			}

			if (blockState.getFluidState().is(FluidTags.WATER)) {
				for (int t = 0; t < 16; t++) {
					for (int uxx = 0; uxx < 16; uxx++) {
						int vx = 4;
						BlockPos blockPos4 = blockPos.offset(t, 4, uxx);
						if (worldGenLevel.getBiome(blockPos4).value().shouldFreeze(worldGenLevel, blockPos4, false)
							&& this.canReplaceBlock(worldGenLevel.getBlockState(blockPos4))) {
							worldGenLevel.setBlock(blockPos4, Blocks.ICE.defaultBlockState(), 2);
						}
					}
				}
			}

			return true;
		}
	}

	private boolean canReplaceBlock(BlockState blockState) {
		return !blockState.is(BlockTags.FEATURES_CANNOT_REPLACE);
	}

	public static record Configuration(BlockStateProvider fluid, BlockStateProvider barrier) implements FeatureConfiguration {
		public static final Codec<LakeFeature.Configuration> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						BlockStateProvider.CODEC.fieldOf("fluid").forGetter(LakeFeature.Configuration::fluid),
						BlockStateProvider.CODEC.fieldOf("barrier").forGetter(LakeFeature.Configuration::barrier)
					)
					.apply(instance, LakeFeature.Configuration::new)
		);
	}
}
