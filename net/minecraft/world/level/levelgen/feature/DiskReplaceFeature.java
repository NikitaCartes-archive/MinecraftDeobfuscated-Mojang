/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskReplaceFeature
extends Feature<DiskConfiguration> {
    public DiskReplaceFeature(Function<Dynamic<?>, ? extends DiskConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, DiskConfiguration diskConfiguration) {
        if (!levelAccessor.getFluidState(blockPos).is(FluidTags.WATER)) {
            return false;
        }
        int i = 0;
        int j = random.nextInt(diskConfiguration.radius - 2) + 2;
        for (int k = blockPos.getX() - j; k <= blockPos.getX() + j; ++k) {
            for (int l = blockPos.getZ() - j; l <= blockPos.getZ() + j; ++l) {
                int n;
                int m = k - blockPos.getX();
                if (m * m + (n = l - blockPos.getZ()) * n > j * j) continue;
                block2: for (int o = blockPos.getY() - diskConfiguration.ySize; o <= blockPos.getY() + diskConfiguration.ySize; ++o) {
                    BlockPos blockPos2 = new BlockPos(k, o, l);
                    BlockState blockState = levelAccessor.getBlockState(blockPos2);
                    for (BlockState blockState2 : diskConfiguration.targets) {
                        if (blockState2.getBlock() != blockState.getBlock()) continue;
                        levelAccessor.setBlock(blockPos2, diskConfiguration.state, 2);
                        ++i;
                        continue block2;
                    }
                }
            }
        }
        return i > 0;
    }
}

