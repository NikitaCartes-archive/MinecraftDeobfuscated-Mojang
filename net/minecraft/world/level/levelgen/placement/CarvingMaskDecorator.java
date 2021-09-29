/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.CarvingMaskDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class CarvingMaskDecorator
extends FeatureDecorator<CarvingMaskDecoratorConfiguration> {
    public CarvingMaskDecorator(Codec<CarvingMaskDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, CarvingMaskDecoratorConfiguration carvingMaskDecoratorConfiguration, BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        return decorationContext.getCarvingMask(chunkPos, carvingMaskDecoratorConfiguration.step).stream(chunkPos);
    }

    @Override
    public /* synthetic */ Stream getPositions(DecorationContext decorationContext, Random random, DecoratorConfiguration decoratorConfiguration, BlockPos blockPos) {
        return this.getPositions(decorationContext, random, (CarvingMaskDecoratorConfiguration)decoratorConfiguration, blockPos);
    }
}

