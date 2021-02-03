/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Material;

public class BlueIceFeature
extends Feature<NoneFeatureConfiguration> {
    public BlueIceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        Random random = featurePlaceContext.random();
        if (blockPos.getY() > worldGenLevel.getSeaLevel() - 1) {
            return false;
        }
        if (!worldGenLevel.getBlockState(blockPos).is(Blocks.WATER) && !worldGenLevel.getBlockState(blockPos.below()).is(Blocks.WATER)) {
            return false;
        }
        boolean bl = false;
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || !worldGenLevel.getBlockState(blockPos.relative(direction)).is(Blocks.PACKED_ICE)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return false;
        }
        worldGenLevel.setBlock(blockPos, Blocks.BLUE_ICE.defaultBlockState(), 2);
        block1: for (int i = 0; i < 200; ++i) {
            BlockPos blockPos2;
            BlockState blockState;
            int j = random.nextInt(5) - random.nextInt(6);
            int k = 3;
            if (j < 2) {
                k += j / 2;
            }
            if (k < 1 || (blockState = worldGenLevel.getBlockState(blockPos2 = blockPos.offset(random.nextInt(k) - random.nextInt(k), j, random.nextInt(k) - random.nextInt(k)))).getMaterial() != Material.AIR && !blockState.is(Blocks.WATER) && !blockState.is(Blocks.PACKED_ICE) && !blockState.is(Blocks.ICE)) continue;
            for (Direction direction2 : Direction.values()) {
                BlockState blockState2 = worldGenLevel.getBlockState(blockPos2.relative(direction2));
                if (!blockState2.is(Blocks.BLUE_ICE)) continue;
                worldGenLevel.setBlock(blockPos2, Blocks.BLUE_ICE.defaultBlockState(), 2);
                continue block1;
            }
        }
        return true;
    }
}

