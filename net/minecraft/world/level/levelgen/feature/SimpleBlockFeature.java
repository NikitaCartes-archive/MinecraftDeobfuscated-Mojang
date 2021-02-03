/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class SimpleBlockFeature
extends Feature<SimpleBlockConfiguration> {
    public SimpleBlockFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> featurePlaceContext) {
        BlockPos blockPos;
        SimpleBlockConfiguration simpleBlockConfiguration = featurePlaceContext.config();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        if (simpleBlockConfiguration.placeOn.contains(worldGenLevel.getBlockState((blockPos = featurePlaceContext.origin()).below())) && simpleBlockConfiguration.placeIn.contains(worldGenLevel.getBlockState(blockPos)) && simpleBlockConfiguration.placeUnder.contains(worldGenLevel.getBlockState(blockPos.above()))) {
            worldGenLevel.setBlock(blockPos, simpleBlockConfiguration.toPlace, 2);
            return true;
        }
        return false;
    }
}

