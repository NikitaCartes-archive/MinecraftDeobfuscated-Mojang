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
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class GeodeFeature
extends Feature<GeodeConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public GeodeFeature(Codec<GeodeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, GeodeConfiguration geodeConfiguration) {
        int n;
        int m;
        int i = geodeConfiguration.minGenOffset;
        int j = geodeConfiguration.maxGenOffset;
        if (worldGenLevel.getFluidState(blockPos.offset(0, j / 3, 0)).isSource()) {
            return false;
        }
        LinkedList<Pair<BlockPos, Integer>> list = Lists.newLinkedList();
        int k = geodeConfiguration.minDistributionPoints + random.nextInt(geodeConfiguration.maxDistributionPoints - geodeConfiguration.minDistributionPoints);
        WorldgenRandom worldgenRandom = new WorldgenRandom(worldGenLevel.getSeed());
        NormalNoise normalNoise = NormalNoise.create(worldgenRandom, -4, 1.0);
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
        for (m = 0; m < k; ++m) {
            n = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance);
            int o = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance);
            int p = geodeConfiguration.minOuterWallDistance + random.nextInt(geodeConfiguration.maxOuterWallDistance - geodeConfiguration.minOuterWallDistance);
            list.add(Pair.of(blockPos.offset(n, o, p), geodeConfiguration.minPointOffset + random.nextInt(geodeConfiguration.maxPointOffset - geodeConfiguration.minPointOffset)));
        }
        if (bl) {
            m = random.nextInt(4);
            n = k * 2 + 1;
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
        ArrayList<BlockPos> list3 = Lists.newArrayList();
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(i, i, i), blockPos.offset(j, j, j))) {
            double q = normalNoise.getValue(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ()) * geodeConfiguration.noiseMultiplier;
            double r = 0.0;
            double s = 0.0;
            for (Pair pair : list) {
                r += Mth.fastInvSqrt(blockPos2.distSqr((Vec3i)pair.getFirst()) + (double)((Integer)pair.getSecond()).intValue()) + q;
            }
            for (BlockPos blockPos3 : list2) {
                s += Mth.fastInvSqrt(blockPos2.distSqr(blockPos3) + (double)geodeCrackSettings.crackPointOffset) + q;
            }
            if (r < h) continue;
            if (bl && s >= l && r < e) {
                if (!worldGenLevel.getFluidState(blockPos2).isEmpty()) continue;
                worldGenLevel.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 2);
                continue;
            }
            if (r >= e) {
                worldGenLevel.setBlock(blockPos2, geodeBlockSettings.fillingProvider.getState(random, blockPos2), 2);
                continue;
            }
            if (r >= f) {
                boolean bl2;
                boolean bl3 = bl2 = (double)random.nextFloat() < geodeConfiguration.useAlternateLayer0Chance;
                if (bl2) {
                    worldGenLevel.setBlock(blockPos2, geodeBlockSettings.alternateInnerLayerProvider.getState(random, blockPos2), 2);
                } else {
                    worldGenLevel.setBlock(blockPos2, geodeBlockSettings.innerLayerProvider.getState(random, blockPos2), 2);
                }
                if (geodeConfiguration.placementsRequireLayer0Alternate && !bl2 || !((double)random.nextFloat() < geodeConfiguration.usePotentialPlacementsChance)) continue;
                list3.add(blockPos2.immutable());
                continue;
            }
            if (r >= g) {
                worldGenLevel.setBlock(blockPos2, geodeBlockSettings.middleLayerProvider.getState(random, blockPos2), 2);
                continue;
            }
            if (!(r >= h)) continue;
            worldGenLevel.setBlock(blockPos2, geodeBlockSettings.outerLayerProvider.getState(random, blockPos2), 2);
        }
        List<BlockState> list4 = geodeBlockSettings.innerPlacements;
        block4: for (BlockPos blockPos4 : list3) {
            BlockState blockState = list4.get(random.nextInt(list4.size()));
            for (Direction direction : DIRECTIONS) {
                if (blockState.hasProperty(BlockStateProperties.FACING)) {
                    blockState = (BlockState)blockState.setValue(BlockStateProperties.FACING, direction);
                }
                BlockPos blockPos5 = blockPos4.relative(direction);
                BlockState blockState2 = worldGenLevel.getBlockState(blockPos5);
                if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    blockState = (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, blockState2.getFluidState().isSource());
                }
                if (!BuddingAmethystBlock.canClusterGrowAtState(blockState2)) continue;
                worldGenLevel.setBlock(blockPos5, blockState, 2);
                continue block4;
            }
        }
        return true;
    }
}

