/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.placement.CarvingMaskDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class CarvingMaskDecorator
extends FeatureDecorator<CarvingMaskDecoratorConfiguration> {
    public CarvingMaskDecorator(Codec<CarvingMaskDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, CarvingMaskDecoratorConfiguration carvingMaskDecoratorConfiguration, BlockPos blockPos) {
        ChunkAccess chunkAccess = levelAccessor.getChunk(blockPos);
        ChunkPos chunkPos = chunkAccess.getPos();
        BitSet bitSet = ((ProtoChunk)chunkAccess).getCarvingMask(carvingMaskDecoratorConfiguration.step);
        if (bitSet == null) {
            return Stream.empty();
        }
        return IntStream.range(0, bitSet.length()).filter(i -> bitSet.get(i) && random.nextFloat() < carvingMaskDecoratorConfiguration.probability).mapToObj(i -> {
            int j = i & 0xF;
            int k = i >> 4 & 0xF;
            int l = i >> 8;
            return new BlockPos(chunkPos.getMinBlockX() + j, l, chunkPos.getMinBlockZ() + k);
        });
    }
}

