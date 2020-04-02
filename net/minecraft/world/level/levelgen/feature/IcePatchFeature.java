/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureRadiusConfiguration;

public class IcePatchFeature
extends Feature<FeatureRadiusConfiguration> {
    private final Block block = Blocks.PACKED_ICE;

    public IcePatchFeature(Function<Dynamic<?>, ? extends FeatureRadiusConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, FeatureRadiusConfiguration featureRadiusConfiguration) {
        while (levelAccessor.isEmptyBlock(blockPos) && blockPos.getY() > 2) {
            blockPos = blockPos.below();
        }
        if (levelAccessor.getBlockState(blockPos).getBlock() != Blocks.SNOW_BLOCK) {
            return false;
        }
        int i = random.nextInt(featureRadiusConfiguration.radius) + 2;
        boolean j = true;
        for (int k = blockPos.getX() - i; k <= blockPos.getX() + i; ++k) {
            for (int l = blockPos.getZ() - i; l <= blockPos.getZ() + i; ++l) {
                int n;
                int m = k - blockPos.getX();
                if (m * m + (n = l - blockPos.getZ()) * n > i * i) continue;
                for (int o = blockPos.getY() - 1; o <= blockPos.getY() + 1; ++o) {
                    BlockPos blockPos2 = new BlockPos(k, o, l);
                    Block block = levelAccessor.getBlockState(blockPos2).getBlock();
                    if (!IcePatchFeature.isDirt(block) && block != Blocks.SNOW_BLOCK && block != Blocks.ICE) continue;
                    levelAccessor.setBlock(blockPos2, this.block.defaultBlockState(), 2);
                }
            }
        }
        return true;
    }
}

