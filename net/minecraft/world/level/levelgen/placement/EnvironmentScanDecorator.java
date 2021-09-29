/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class EnvironmentScanDecorator
extends FeatureDecorator<EnvironmentScanConfiguration> {
    public EnvironmentScanDecorator(Codec<EnvironmentScanConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, EnvironmentScanConfiguration environmentScanConfiguration, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        WorldGenLevel worldGenLevel = decorationContext.getLevel();
        for (int i = 0; i < environmentScanConfiguration.maxSteps() && !worldGenLevel.isOutsideBuildHeight(mutableBlockPos.getY()); ++i) {
            if (environmentScanConfiguration.targetCondition().test(worldGenLevel, mutableBlockPos)) {
                return Stream.of(mutableBlockPos);
            }
            mutableBlockPos.move(environmentScanConfiguration.directionOfSearch());
        }
        return Stream.of(new BlockPos[0]);
    }

    @Override
    public /* synthetic */ Stream getPositions(DecorationContext decorationContext, Random random, DecoratorConfiguration decoratorConfiguration, BlockPos blockPos) {
        return this.getPositions(decorationContext, random, (EnvironmentScanConfiguration)decoratorConfiguration, blockPos);
    }
}

