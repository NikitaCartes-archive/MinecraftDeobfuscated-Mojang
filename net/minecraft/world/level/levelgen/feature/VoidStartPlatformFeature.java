/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VoidStartPlatformFeature
extends Feature<NoneFeatureConfiguration> {
    private static final BlockPos PLATFORM_ORIGIN = new BlockPos(8, 3, 8);
    private static final ChunkPos PLATFORM_ORIGIN_CHUNK = new ChunkPos(PLATFORM_ORIGIN);

    public VoidStartPlatformFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    private static int checkerboardDistance(int i, int j, int k, int l) {
        return Math.max(Math.abs(i - k), Math.abs(j - l));
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        if (VoidStartPlatformFeature.checkerboardDistance(chunkPos.x, chunkPos.z, VoidStartPlatformFeature.PLATFORM_ORIGIN_CHUNK.x, VoidStartPlatformFeature.PLATFORM_ORIGIN_CHUNK.z) > 1) {
            return true;
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = chunkPos.getMinBlockZ(); i <= chunkPos.getMaxBlockZ(); ++i) {
            for (int j = chunkPos.getMinBlockX(); j <= chunkPos.getMaxBlockX(); ++j) {
                if (VoidStartPlatformFeature.checkerboardDistance(PLATFORM_ORIGIN.getX(), PLATFORM_ORIGIN.getZ(), j, i) > 16) continue;
                mutableBlockPos.set(j, PLATFORM_ORIGIN.getY(), i);
                if (mutableBlockPos.equals(PLATFORM_ORIGIN)) {
                    levelAccessor.setBlock(mutableBlockPos, Blocks.COBBLESTONE.defaultBlockState(), 2);
                    continue;
                }
                levelAccessor.setBlock(mutableBlockPos, Blocks.STONE.defaultBlockState(), 2);
            }
        }
        return true;
    }
}

