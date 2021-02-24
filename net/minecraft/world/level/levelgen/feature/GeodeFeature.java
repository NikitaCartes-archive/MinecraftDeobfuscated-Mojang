/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class GeodeFeature
extends Feature<GeodeConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public GeodeFeature(Codec<GeodeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GeodeConfiguration> featurePlaceContext) {
        int o;
        int n;
        GeodeConfiguration geodeConfiguration = featurePlaceContext.config();
        Random random = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        int i = geodeConfiguration.minGenOffset;
        int j = geodeConfiguration.maxGenOffset;
        LinkedList<Pair<BlockPos, Integer>> list = Lists.newLinkedList();
        int k = geodeConfiguration.minDistributionPoints + random.nextInt(geodeConfiguration.maxDistributionPoints - geodeConfiguration.minDistributionPoints);
        WorldgenRandom worldgenRandom = new WorldgenRandom(worldGenLevel.getSeed());
        NormalNoise normalNoise = NormalNoise.create((RandomSource)worldgenRandom, -4, 1.0);
        LinkedList<BlockPos> list2 = Lists.newLinkedList();
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
        for (n = 0; n < k; ++n) {
            int q;
            int p;
            o = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance);
            BlockPos blockPos2 = blockPos.offset(o, p = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance), q = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance));
            BlockState blockState = worldGenLevel.getBlockState(blockPos2);
            if ((blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.LAVA)) && ++m > geodeConfiguration.invalidBlocksThreshold) {
                return false;
            }
            list.add(Pair.of(blockPos2, geodeConfiguration.minPointOffset + random.nextInt(geodeConfiguration.maxPointOffset - geodeConfiguration.minPointOffset)));
        }
        if (bl) {
            n = random.nextInt(4);
            o = k * 2 + 1;
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
        ArrayList<BlockPos> list3 = Lists.newArrayList();
        for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos.offset(i, i, i), blockPos.offset(j, j, j))) {
            double r = normalNoise.getValue(blockPos3.getX(), blockPos3.getY(), blockPos3.getZ()) * geodeConfiguration.noiseMultiplier;
            double s = 0.0;
            double t = 0.0;
            for (Pair pair : list) {
                s += Mth.fastInvSqrt(blockPos3.distSqr((Vec3i)pair.getFirst()) + (double)((Integer)pair.getSecond()).intValue()) + r;
            }
            for (BlockPos blockPos2 : list2) {
                t += Mth.fastInvSqrt(blockPos3.distSqr(blockPos2) + (double)geodeCrackSettings.crackPointOffset) + r;
            }
            if (s < h) continue;
            if (bl && t >= l && s < e) {
                if (!worldGenLevel.getFluidState(blockPos3).isEmpty()) continue;
                worldGenLevel.setBlock(blockPos3, Blocks.AIR.defaultBlockState(), 2);
                continue;
            }
            if (s >= e) {
                worldGenLevel.setBlock(blockPos3, geodeBlockSettings.fillingProvider.getState(random, blockPos3), 2);
                continue;
            }
            if (s >= f) {
                boolean bl2;
                boolean bl3 = bl2 = (double)random.nextFloat() < geodeConfiguration.useAlternateLayer0Chance;
                if (bl2) {
                    worldGenLevel.setBlock(blockPos3, geodeBlockSettings.alternateInnerLayerProvider.getState(random, blockPos3), 2);
                } else {
                    worldGenLevel.setBlock(blockPos3, geodeBlockSettings.innerLayerProvider.getState(random, blockPos3), 2);
                }
                if (geodeConfiguration.placementsRequireLayer0Alternate && !bl2 || !((double)random.nextFloat() < geodeConfiguration.usePotentialPlacementsChance)) continue;
                list3.add(blockPos3.immutable());
                continue;
            }
            if (s >= g) {
                worldGenLevel.setBlock(blockPos3, geodeBlockSettings.middleLayerProvider.getState(random, blockPos3), 2);
                continue;
            }
            if (!(s >= h)) continue;
            worldGenLevel.setBlock(blockPos3, geodeBlockSettings.outerLayerProvider.getState(random, blockPos3), 2);
        }
        List<BlockState> list4 = geodeBlockSettings.innerPlacements;
        block4: for (BlockPos blockPos5 : list3) {
            BlockState blockState2 = list4.get(random.nextInt(list4.size()));
            for (Direction direction : DIRECTIONS) {
                if (blockState2.hasProperty(BlockStateProperties.FACING)) {
                    blockState2 = (BlockState)blockState2.setValue(BlockStateProperties.FACING, direction);
                }
                BlockPos blockPos6 = blockPos5.relative(direction);
                BlockState blockState = worldGenLevel.getBlockState(blockPos6);
                if (blockState2.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    blockState2 = (BlockState)blockState2.setValue(BlockStateProperties.WATERLOGGED, blockState.getFluidState().isSource());
                }
                if (!BuddingAmethystBlock.canClusterGrowAtState(blockState)) continue;
                worldGenLevel.setBlock(blockPos6, blockState2, 2);
                continue block4;
            }
        }
        return true;
    }
}

