/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndIslandFeature
extends Feature<NoneFeatureConfiguration> {
    public EndIslandFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        float f = random.nextInt(3) + 4;
        int i = 0;
        while (f > 0.5f) {
            for (int j = Mth.floor(-f); j <= Mth.ceil(f); ++j) {
                for (int k = Mth.floor(-f); k <= Mth.ceil(f); ++k) {
                    if (!((float)(j * j + k * k) <= (f + 1.0f) * (f + 1.0f))) continue;
                    this.setBlock(worldGenLevel, blockPos.offset(j, i, k), Blocks.END_STONE.defaultBlockState());
                }
            }
            f = (float)((double)f - ((double)random.nextInt(2) + 0.5));
            --i;
        }
        return true;
    }
}

