/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.GlowLichenConfiguration;

public class GlowLichenFeature
extends Feature<GlowLichenConfiguration> {
    public GlowLichenFeature(Codec<GlowLichenConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GlowLichenConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        Random random = featurePlaceContext.random();
        GlowLichenConfiguration glowLichenConfiguration = featurePlaceContext.config();
        if (!GlowLichenFeature.isAirOrWater(worldGenLevel.getBlockState(blockPos))) {
            return false;
        }
        List<Direction> list = GlowLichenFeature.getShuffledDirections(glowLichenConfiguration, random);
        if (GlowLichenFeature.placeGlowLichenIfPossible(worldGenLevel, blockPos, worldGenLevel.getBlockState(blockPos), glowLichenConfiguration, random, list)) {
            return true;
        }
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        block0: for (Direction direction : list) {
            mutableBlockPos.set(blockPos);
            List<Direction> list2 = GlowLichenFeature.getShuffledDirectionsExcept(glowLichenConfiguration, random, direction.getOpposite());
            for (int i = 0; i < glowLichenConfiguration.searchRange; ++i) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
                BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
                if (!GlowLichenFeature.isAirOrWater(blockState) && !blockState.is(Blocks.GLOW_LICHEN)) continue block0;
                if (!GlowLichenFeature.placeGlowLichenIfPossible(worldGenLevel, mutableBlockPos, blockState, glowLichenConfiguration, random, list2)) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean placeGlowLichenIfPossible(WorldGenLevel worldGenLevel, BlockPos blockPos, BlockState blockState, GlowLichenConfiguration glowLichenConfiguration, Random random, List<Direction> list) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (Direction direction : list) {
            BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos, direction));
            if (!glowLichenConfiguration.canBePlacedOn.contains(blockState2.getBlock())) continue;
            GlowLichenBlock glowLichenBlock = (GlowLichenBlock)Blocks.GLOW_LICHEN;
            BlockState blockState3 = glowLichenBlock.getStateForPlacement(blockState, worldGenLevel, blockPos, direction);
            if (blockState3 == null) {
                return false;
            }
            worldGenLevel.setBlock(blockPos, blockState3, 3);
            worldGenLevel.getChunk(blockPos).markPosForPostprocessing(blockPos);
            if (random.nextFloat() < glowLichenConfiguration.chanceOfSpreading) {
                glowLichenBlock.spreadFromFaceTowardRandomDirection(blockState3, worldGenLevel, blockPos, direction, random, true);
            }
            return true;
        }
        return false;
    }

    public static List<Direction> getShuffledDirections(GlowLichenConfiguration glowLichenConfiguration, Random random) {
        ArrayList<Direction> list = Lists.newArrayList(glowLichenConfiguration.validDirections);
        Collections.shuffle(list, random);
        return list;
    }

    public static List<Direction> getShuffledDirectionsExcept(GlowLichenConfiguration glowLichenConfiguration, Random random, Direction direction) {
        List<Direction> list = glowLichenConfiguration.validDirections.stream().filter(direction2 -> direction2 != direction).collect(Collectors.toList());
        Collections.shuffle(list, random);
        return list;
    }

    private static boolean isAirOrWater(BlockState blockState) {
        return blockState.isAir() || blockState.is(Blocks.WATER);
    }
}

