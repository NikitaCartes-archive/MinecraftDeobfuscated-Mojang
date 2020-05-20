/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class RandomPatchFeature
extends Feature<RandomPatchConfiguration> {
    public RandomPatchFeature(Codec<RandomPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, RandomPatchConfiguration randomPatchConfiguration) {
        BlockState blockState = randomPatchConfiguration.stateProvider.getState(random, blockPos);
        BlockPos blockPos2 = randomPatchConfiguration.project ? worldGenLevel.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, blockPos) : blockPos;
        int i = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int j = 0; j < randomPatchConfiguration.tries; ++j) {
            mutableBlockPos.setWithOffset(blockPos2, random.nextInt(randomPatchConfiguration.xspread + 1) - random.nextInt(randomPatchConfiguration.xspread + 1), random.nextInt(randomPatchConfiguration.yspread + 1) - random.nextInt(randomPatchConfiguration.yspread + 1), random.nextInt(randomPatchConfiguration.zspread + 1) - random.nextInt(randomPatchConfiguration.zspread + 1));
            Vec3i blockPos3 = mutableBlockPos.below();
            BlockState blockState2 = worldGenLevel.getBlockState((BlockPos)blockPos3);
            if (!worldGenLevel.isEmptyBlock(mutableBlockPos) && (!randomPatchConfiguration.canReplace || !worldGenLevel.getBlockState(mutableBlockPos).getMaterial().isReplaceable()) || !blockState.canSurvive(worldGenLevel, mutableBlockPos) || !randomPatchConfiguration.whitelist.isEmpty() && !randomPatchConfiguration.whitelist.contains(blockState2.getBlock()) || randomPatchConfiguration.blacklist.contains(blockState2) || randomPatchConfiguration.needWater && !worldGenLevel.getFluidState(((BlockPos)blockPos3).west()).is(FluidTags.WATER) && !worldGenLevel.getFluidState(((BlockPos)blockPos3).east()).is(FluidTags.WATER) && !worldGenLevel.getFluidState(((BlockPos)blockPos3).north()).is(FluidTags.WATER) && !worldGenLevel.getFluidState(((BlockPos)blockPos3).south()).is(FluidTags.WATER)) continue;
            randomPatchConfiguration.blockPlacer.place(worldGenLevel, mutableBlockPos, blockState, random);
            ++i;
        }
        return i > 0;
    }
}

