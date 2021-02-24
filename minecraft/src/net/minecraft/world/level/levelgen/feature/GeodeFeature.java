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
		int m = 0;

		for (int n = 0; n < k; n++) {
			int o = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance);
			int p = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance);
			int q = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance);
			BlockPos blockPos2 = blockPos.offset(o, p, q);
			BlockState blockState = worldGenLevel.getBlockState(blockPos2);
			if (blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.LAVA)) {
				if (++m > geodeConfiguration.invalidBlocksThreshold) {
					return false;
				}
			}

			list.add(Pair.of(blockPos2, geodeConfiguration.minPointOffset + random.nextInt(geodeConfiguration.maxPointOffset - geodeConfiguration.minPointOffset)));
		}

		if (bl) {
			int n = random.nextInt(4);
			int o = k * 2 + 1;
			if (n == 0) {
				list2.add(blockPos.offset(o, 7, 0));
				list2.add(blockPos.offset(o, 5, 0));
				list2.add(blockPos.offset(o, 1, 0));
			} else if (n == 1) {
				list2.add(blockPos.offset(0, 7, o));
				list2.add(blockPos.offset(0, 5, o));
				list2.add(blockPos.offset(0, 1, o));
			} else if (n == 2) {
				list2.add(blockPos.offset(o, 7, o));
				list2.add(blockPos.offset(o, 5, o));
				list2.add(blockPos.offset(o, 1, o));
			} else {
				list2.add(blockPos.offset(0, 7, 0));
				list2.add(blockPos.offset(0, 5, 0));
				list2.add(blockPos.offset(0, 1, 0));
			}
		}

		List<BlockPos> list3 = Lists.<BlockPos>newArrayList();

		for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos.offset(i, i, i), blockPos.offset(j, j, j))) {
			double r = normalNoise.getValue((double)blockPos3.getX(), (double)blockPos3.getY(), (double)blockPos3.getZ()) * geodeConfiguration.noiseMultiplier;
			double s = 0.0;
			double t = 0.0;

			for (Pair<BlockPos, Integer> pair : list) {
				s += Mth.fastInvSqrt(blockPos3.distSqr(pair.getFirst()) + (double)pair.getSecond().intValue()) + r;
			}

			for (BlockPos blockPos4 : list2) {
				t += Mth.fastInvSqrt(blockPos3.distSqr(blockPos4) + (double)geodeCrackSettings.crackPointOffset) + r;
			}

			if (!(s < h)) {
				if (bl && t >= l && s < e) {
					if (worldGenLevel.getFluidState(blockPos3).isEmpty()) {
						worldGenLevel.setBlock(blockPos3, Blocks.AIR.defaultBlockState(), 2);
					}
				} else if (s >= e) {
					worldGenLevel.setBlock(blockPos3, geodeBlockSettings.fillingProvider.getState(random, blockPos3), 2);
				} else if (s >= f) {
					boolean bl2 = (double)random.nextFloat() < geodeConfiguration.useAlternateLayer0Chance;
					if (bl2) {
						worldGenLevel.setBlock(blockPos3, geodeBlockSettings.alternateInnerLayerProvider.getState(random, blockPos3), 2);
					} else {
						worldGenLevel.setBlock(blockPos3, geodeBlockSettings.innerLayerProvider.getState(random, blockPos3), 2);
					}

					if ((!geodeConfiguration.placementsRequireLayer0Alternate || bl2) && (double)random.nextFloat() < geodeConfiguration.usePotentialPlacementsChance) {
						list3.add(blockPos3.immutable());
					}
				} else if (s >= g) {
					worldGenLevel.setBlock(blockPos3, geodeBlockSettings.middleLayerProvider.getState(random, blockPos3), 2);
				} else if (s >= h) {
					worldGenLevel.setBlock(blockPos3, geodeBlockSettings.outerLayerProvider.getState(random, blockPos3), 2);
				}
			}
		}

		List<BlockState> list4 = geodeBlockSettings.innerPlacements;

		for (BlockPos blockPos5 : list3) {
			BlockState blockState2 = (BlockState)list4.get(random.nextInt(list4.size()));

			for (Direction direction : DIRECTIONS) {
				if (blockState2.hasProperty(BlockStateProperties.FACING)) {
					blockState2 = blockState2.setValue(BlockStateProperties.FACING, direction);
				}

				BlockPos blockPos6 = blockPos5.relative(direction);
				BlockState blockState3 = worldGenLevel.getBlockState(blockPos6);
				if (blockState2.hasProperty(BlockStateProperties.WATERLOGGED)) {
					blockState2 = blockState2.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(blockState3.getFluidState().isSource()));
				}

				if (BuddingAmethystBlock.canClusterGrowAtState(blockState3)) {
					worldGenLevel.setBlock(blockPos6, blockState2, 2);
					break;
				}
			}
		}

		return true;
	}
}
