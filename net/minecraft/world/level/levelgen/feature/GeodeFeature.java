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
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;

public class GeodeFeature
extends Feature<GeodeConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public GeodeFeature(Codec<GeodeConfiguration> codec) {
        super(codec);
    }

    /*
     * Could not resolve type clashes
     */
    @Override
    public boolean place(FeaturePlaceContext<GeodeConfiguration> featurePlaceContext) {
        BlockState blockState;
        int o;
        int n;
        GeodeConfiguration geodeConfiguration = featurePlaceContext.config();
        Random random = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        int i = geodeConfiguration.minGenOffset;
        int j = geodeConfiguration.maxGenOffset;
        LinkedList<Pair<BlockPos, Integer>> list = Lists.newLinkedList();
        int k = geodeConfiguration.distributionPoints.sample(random);
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(worldGenLevel.getSeed()));
        NormalNoise normalNoise = NormalNoise.createLegacy((RandomSource)worldgenRandom, -4, 1.0);
        LinkedList<BlockPos> list2 = Lists.newLinkedList();
        double d = (double)k / (double)geodeConfiguration.outerWallDistance.getMaxValue();
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
            o = geodeConfiguration.outerWallDistance.sample(random);
            BlockPos blockPos2 = blockPos.offset(o, p = geodeConfiguration.outerWallDistance.sample(random), q = geodeConfiguration.outerWallDistance.sample(random));
            blockState = worldGenLevel.getBlockState(blockPos2);
            if ((blockState.isAir() || blockState.is(BlockTags.GEODE_INVALID_BLOCKS)) && ++m > geodeConfiguration.invalidBlocksThreshold) {
                return false;
            }
            list.add(Pair.of(blockPos2, geodeConfiguration.pointOffset.sample(random)));
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
        Predicate<BlockState> predicate = GeodeFeature.isReplaceable(geodeConfiguration.geodeBlockSettings.cannotReplace);
        for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos.offset(i, i, i), blockPos.offset(j, j, j))) {
            double r = normalNoise.getValue(blockPos3.getX(), blockPos3.getY(), blockPos3.getZ()) * geodeConfiguration.noiseMultiplier;
            double s = 0.0;
            double t = 0.0;
            for (Pair pair : list) {
                s += Mth.fastInvSqrt(blockPos3.distSqr((Vec3i)pair.getFirst()) + (double)((Integer)pair.getSecond()).intValue()) + r;
            }
            for (BlockPos blockPos4 : list2) {
                t += Mth.fastInvSqrt(blockPos3.distSqr(blockPos4) + (double)geodeCrackSettings.crackPointOffset) + r;
            }
            if (s < h) continue;
            if (bl && t >= l && s < e) {
                this.safeSetBlock(worldGenLevel, blockPos3, Blocks.AIR.defaultBlockState(), predicate);
                for (Direction direction : DIRECTIONS) {
                    BlockPos blockPos5 = blockPos3.relative(direction);
                    FluidState fluidState = worldGenLevel.getFluidState(blockPos5);
                    if (fluidState.isEmpty()) continue;
                    worldGenLevel.getLiquidTicks().scheduleTick(blockPos5, fluidState.getType(), 0);
                }
                continue;
            }
            if (s >= e) {
                this.safeSetBlock(worldGenLevel, blockPos3, geodeBlockSettings.fillingProvider.getState(random, blockPos3), predicate);
                continue;
            }
            if (s >= f) {
                boolean bl2;
                boolean bl3 = bl2 = (double)random.nextFloat() < geodeConfiguration.useAlternateLayer0Chance;
                if (bl2) {
                    this.safeSetBlock(worldGenLevel, blockPos3, geodeBlockSettings.alternateInnerLayerProvider.getState(random, blockPos3), predicate);
                } else {
                    this.safeSetBlock(worldGenLevel, blockPos3, geodeBlockSettings.innerLayerProvider.getState(random, blockPos3), predicate);
                }
                if (geodeConfiguration.placementsRequireLayer0Alternate && !bl2 || !((double)random.nextFloat() < geodeConfiguration.usePotentialPlacementsChance)) continue;
                list3.add(blockPos3.immutable());
                continue;
            }
            if (s >= g) {
                this.safeSetBlock(worldGenLevel, blockPos3, geodeBlockSettings.middleLayerProvider.getState(random, blockPos3), predicate);
                continue;
            }
            if (!(s >= h)) continue;
            this.safeSetBlock(worldGenLevel, blockPos3, geodeBlockSettings.outerLayerProvider.getState(random, blockPos3), predicate);
        }
        List<BlockState> list4 = geodeBlockSettings.innerPlacements;
        block5: for (BlockPos blockPos2 : list3) {
            blockState = Util.getRandom(list4, random);
            for (Direction direction2 : DIRECTIONS) {
                if (blockState.hasProperty(BlockStateProperties.FACING)) {
                    blockState = (BlockState)blockState.setValue(BlockStateProperties.FACING, direction2);
                }
                BlockPos blockPos6 = blockPos2.relative(direction2);
                BlockState blockState2 = worldGenLevel.getBlockState(blockPos6);
                if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    blockState = (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, blockState2.getFluidState().isSource());
                }
                if (!BuddingAmethystBlock.canClusterGrowAtState(blockState2)) continue;
                this.safeSetBlock(worldGenLevel, blockPos6, blockState, predicate);
                continue block5;
            }
        }
        return true;
    }
}

