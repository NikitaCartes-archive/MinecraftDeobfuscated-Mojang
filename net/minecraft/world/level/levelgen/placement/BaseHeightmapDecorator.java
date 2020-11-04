/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.EdgeDecorator;

public abstract class BaseHeightmapDecorator<DC extends DecoratorConfiguration>
extends EdgeDecorator<DC> {
    public BaseHeightmapDecorator(Codec<DC> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DC decoratorConfiguration, BlockPos blockPos) {
        int i = blockPos.getX();
        int j = blockPos.getZ();
        int k = decorationContext.getHeight(this.type(decoratorConfiguration), i, j);
        if (k > decorationContext.getMinBuildHeight()) {
            return Stream.of(new BlockPos(i, k, j));
        }
        return Stream.of(new BlockPos[0]);
    }
}

