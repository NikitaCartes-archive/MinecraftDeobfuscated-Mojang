/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSpheroidConfiguration;
import org.jetbrains.annotations.Nullable;

public class ReplaceBlobsFeature
extends Feature<ReplaceSpheroidConfiguration> {
    public ReplaceBlobsFeature(Codec<ReplaceSpheroidConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, ReplaceSpheroidConfiguration replaceSpheroidConfiguration) {
        Block block = replaceSpheroidConfiguration.targetState.getBlock();
        BlockPos blockPos2 = ReplaceBlobsFeature.findTarget(worldGenLevel, blockPos.mutable().clamp(Direction.Axis.Y, 1, worldGenLevel.getMaxBuildHeight() - 1), block);
        if (blockPos2 == null) {
            return false;
        }
        Vec3i vec3i = ReplaceBlobsFeature.calculateReach(random, replaceSpheroidConfiguration);
        int i = Math.max(vec3i.getX(), Math.max(vec3i.getY(), vec3i.getZ()));
        boolean bl = false;
        for (BlockPos blockPos3 : BlockPos.withinManhattan(blockPos2, vec3i.getX(), vec3i.getY(), vec3i.getZ())) {
            if (blockPos3.distManhattan(blockPos2) > i) break;
            BlockState blockState = worldGenLevel.getBlockState(blockPos3);
            if (!blockState.is(block)) continue;
            this.setBlock(worldGenLevel, blockPos3, replaceSpheroidConfiguration.replaceState);
            bl = true;
        }
        return bl;
    }

    @Nullable
    private static BlockPos findTarget(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos, Block block) {
        while (mutableBlockPos.getY() > 1) {
            BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
            if (blockState.is(block)) {
                return mutableBlockPos;
            }
            mutableBlockPos.move(Direction.DOWN);
        }
        return null;
    }

    private static Vec3i calculateReach(Random random, ReplaceSpheroidConfiguration replaceSpheroidConfiguration) {
        return new Vec3i(replaceSpheroidConfiguration.minimumReach.getX() + random.nextInt(replaceSpheroidConfiguration.maximumReach.getX() - replaceSpheroidConfiguration.minimumReach.getX() + 1), replaceSpheroidConfiguration.minimumReach.getY() + random.nextInt(replaceSpheroidConfiguration.maximumReach.getY() - replaceSpheroidConfiguration.minimumReach.getY() + 1), replaceSpheroidConfiguration.minimumReach.getZ() + random.nextInt(replaceSpheroidConfiguration.maximumReach.getZ() - replaceSpheroidConfiguration.minimumReach.getZ() + 1));
    }
}

