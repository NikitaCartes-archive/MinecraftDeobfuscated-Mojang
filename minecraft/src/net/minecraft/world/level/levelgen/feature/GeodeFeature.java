package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class GeodeFeature extends Feature<GeodeConfiguration> {
	private static final Direction[] DIRECTIONS = Direction.values();

	public GeodeFeature(Codec<GeodeConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<GeodeConfiguration> featurePlaceContext) {
		GeodeConfiguration geodeConfiguration = featurePlaceContext.config();
		Random random = featurePlaceContext.random();
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		int i = geodeConfiguration.minGenOffset;
		int j = geodeConfiguration.maxGenOffset;
		if (worldGenLevel.getFluidState(blockPos.offset(0, j / 3, 0)).isSource()) {
			return false;
		} else {
			List<Pair<BlockPos, Integer>> list = Lists.<Pair<BlockPos, Integer>>newLinkedList();
			int k = geodeConfiguration.minDistributionPoints + random.nextInt(geodeConfiguration.maxDistributionPoints - geodeConfiguration.minDistributionPoints);
			WorldgenRandom worldgenRandom = new WorldgenRandom(worldGenLevel.getSeed());
			NormalNoise normalNoise = NormalNoise.create(worldgenRandom, -4, 1.0);
			List<BlockPos> list2 = Lists.<BlockPos>newLinkedList();
			double d = (double)k / (double)geodeConfiguration.maxOuterWallDistance;
			GeodeLayerSettings geodeLayerSettings = geodeConfiguration.geodeLayerSettings;
			GeodeBlockSettings geodeBlockSettings = geodeConfiguration.geodeBlockSettings;
			GeodeCrackSettings geodeCrackSettings = geodeConfiguration.geodeCrackSettings;
			double e = 1.0 / Math.sqrt(geodeLayerSettings.filling);
			double f = 1.0 / Math.sqrt(geodeLayerSettings.innerLayer + d);
			double g = 1.0 / Math.sqrt(geodeLayerSettings.middleLayer + d);
			double h = 1.0 / Math.sqrt(geodeLayerSettings.outerLayer + d);
			double l = 1.0 / Math.sqrt(geodeCrackSettings.baseCrackSize + random.nextDouble() / 2.0 + (k > 3 ? d : 0.0));
			boolean bl = (double)random.nextFloat() < geodeCrackSettings.generateCrackChance;

			for (int m = 0; m < k; m++) {
				int n = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance);
				int o = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance);
				int p = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance);
				list.add(
					Pair.of(
						blockPos.offset(n, o, p), geodeConfiguration.minPointOffset + random.nextInt(geodeConfiguration.maxPointOffset - geodeConfiguration.minPointOffset)
					)
				);
			}

			if (bl) {
				int m = random.nextInt(4);
				int n = k * 2 + 1;
				if (m == 0) {
					list2.add(blockPos.offset(n, 7, 0));
					list2.add(blockPos.offset(n, 5, 0));
					list2.add(blockPos.offset(n, 1, 0));
				} else if (m == 1) {
					list2.add(blockPos.offset(0, 7, n));
					list2.add(blockPos.offset(0, 5, n));
					list2.add(blockPos.offset(0, 1, n));
				} else if (m == 2) {
					list2.add(blockPos.offset(n, 7, n));
					list2.add(blockPos.offset(n, 5, n));
					list2.add(blockPos.offset(n, 1, n));
				} else {
					list2.add(blockPos.offset(0, 7, 0));
					list2.add(blockPos.offset(0, 5, 0));
					list2.add(blockPos.offset(0, 1, 0));
				}
			}

			List<BlockPos> list3 = Lists.<BlockPos>newArrayList();

			for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(i, i, i), blockPos.offset(j, j, j))) {
				double q = normalNoise.getValue((double)blockPos2.getX(), (double)blockPos2.getY(), (double)blockPos2.getZ()) * geodeConfiguration.noiseMultiplier;
				double r = 0.0;
				double s = 0.0;

				for (Pair<BlockPos, Integer> pair : list) {
					r += Mth.fastInvSqrt(blockPos2.distSqr(pair.getFirst()) + (double)pair.getSecond().intValue()) + q;
				}

				for (BlockPos blockPos3 : list2) {
					s += Mth.fastInvSqrt(blockPos2.distSqr(blockPos3) + (double)geodeCrackSettings.crackPointOffset) + q;
				}

				if (!(r < h)) {
					if (bl && s >= l && r < e) {
						if (worldGenLevel.getFluidState(blockPos2).isEmpty()) {
							worldGenLevel.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 2);
						}
					} else if (r >= e) {
						worldGenLevel.setBlock(blockPos2, geodeBlockSettings.fillingProvider.getState(random, blockPos2), 2);
					} else if (r >= f) {
						boolean bl2 = (double)random.nextFloat() < geodeConfiguration.useAlternateLayer0Chance;
						if (bl2) {
							worldGenLevel.setBlock(blockPos2, geodeBlockSettings.alternateInnerLayerProvider.getState(random, blockPos2), 2);
						} else {
							worldGenLevel.setBlock(blockPos2, geodeBlockSettings.innerLayerProvider.getState(random, blockPos2), 2);
						}

						if ((!geodeConfiguration.placementsRequireLayer0Alternate || bl2) && (double)random.nextFloat() < geodeConfiguration.usePotentialPlacementsChance) {
							list3.add(blockPos2.immutable());
						}
					} else if (r >= g) {
						worldGenLevel.setBlock(blockPos2, geodeBlockSettings.middleLayerProvider.getState(random, blockPos2), 2);
					} else if (r >= h) {
						worldGenLevel.setBlock(blockPos2, geodeBlockSettings.outerLayerProvider.getState(random, blockPos2), 2);
					}
				}
			}

			List<BlockState> list4 = geodeBlockSettings.innerPlacements;

			for (BlockPos blockPos4 : list3) {
				BlockState blockState = (BlockState)list4.get(random.nextInt(list4.size()));

				for (Direction direction : DIRECTIONS) {
					if (blockState.hasProperty(BlockStateProperties.FACING)) {
						blockState = blockState.setValue(BlockStateProperties.FACING, direction);
					}

					BlockPos blockPos5 = blockPos4.relative(direction);
					BlockState blockState2 = worldGenLevel.getBlockState(blockPos5);
					if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
						blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(blockState2.getFluidState().isSource()));
					}

					if (BuddingAmethystBlock.canClusterGrowAtState(blockState2)) {
						worldGenLevel.setBlock(blockPos5, blockState, 2);
						break;
					}
				}
			}

			return true;
		}
	}
}
