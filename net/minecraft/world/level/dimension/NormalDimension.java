/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class NormalDimension
extends Dimension {
    public NormalDimension(Level level, DimensionType dimensionType) {
        super(level, dimensionType, 0.0f);
    }

    @Override
    public DimensionType getType() {
        return DimensionType.OVERWORLD;
    }

    public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
        return this.level.getLevelData().getGeneratorProvider().create(this.level);
    }

    @Override
    @Nullable
    public BlockPos getSpawnPosInChunk(ChunkPos chunkPos, boolean bl) {
        for (int i = chunkPos.getMinBlockX(); i <= chunkPos.getMaxBlockX(); ++i) {
            for (int j = chunkPos.getMinBlockZ(); j <= chunkPos.getMaxBlockZ(); ++j) {
                BlockPos blockPos = this.getValidSpawnPosition(i, j, bl);
                if (blockPos == null) continue;
                return blockPos;
            }
        }
        return null;
    }

    @Override
    @Nullable
    public BlockPos getValidSpawnPosition(int i, int j, boolean bl) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, 0, j);
        Biome biome = this.level.getBiome(mutableBlockPos);
        BlockState blockState = biome.getSurfaceBuilderConfig().getTopMaterial();
        if (bl && !blockState.getBlock().is(BlockTags.VALID_SPAWN)) {
            return null;
        }
        LevelChunk levelChunk = this.level.getChunk(i >> 4, j >> 4);
        int k = levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i & 0xF, j & 0xF);
        if (k < 0) {
            return null;
        }
        if (levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i & 0xF, j & 0xF) > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, i & 0xF, j & 0xF)) {
            return null;
        }
        for (int l = k + 1; l >= 0; --l) {
            mutableBlockPos.set(i, l, j);
            BlockState blockState2 = this.level.getBlockState(mutableBlockPos);
            if (!blockState2.getFluidState().isEmpty()) break;
            if (!blockState2.equals(blockState)) continue;
            return mutableBlockPos.above().immutable();
        }
        return null;
    }

    @Override
    public float getTimeOfDay(long l, float f) {
        double d = Mth.frac((double)l / 24000.0 - 0.25);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float)(d * 2.0 + e) / 3.0f;
    }

    @Override
    public boolean isNaturalDimension() {
        return true;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
        return vec3.multiply(f * 0.94f + 0.06f, f * 0.94f + 0.06f, f * 0.91f + 0.09f);
    }

    @Override
    public boolean mayRespawn() {
        return true;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean isFoggyAt(int i, int j) {
        return false;
    }
}

