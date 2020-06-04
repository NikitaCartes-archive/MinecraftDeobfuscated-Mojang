/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public interface LevelReader
extends BlockAndTintGetter,
CollisionGetter,
BiomeManager.NoiseBiomeSource {
    @Nullable
    public ChunkAccess getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    @Deprecated
    public boolean hasChunk(int var1, int var2);

    public int getHeight(Heightmap.Types var1, int var2, int var3);

    public int getSkyDarken();

    public BiomeManager getBiomeManager();

    default public Biome getBiome(BlockPos blockPos) {
        return this.getBiomeManager().getBiome(blockPos);
    }

    default public Stream<BlockState> getBlockStatesIfLoaded(AABB aABB) {
        int n;
        int i = Mth.floor(aABB.minX);
        int j = Mth.floor(aABB.maxX);
        int k = Mth.floor(aABB.minY);
        int l = Mth.floor(aABB.maxY);
        int m = Mth.floor(aABB.minZ);
        if (this.hasChunksAt(i, k, m, j, l, n = Mth.floor(aABB.maxZ))) {
            return this.getBlockStates(aABB);
        }
        return Stream.empty();
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    default public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return colorResolver.getColor(this.getBiome(blockPos), blockPos.getX(), blockPos.getZ());
    }

    @Override
    default public Biome getNoiseBiome(int i, int j, int k) {
        ChunkAccess chunkAccess = this.getChunk(i >> 2, k >> 2, ChunkStatus.BIOMES, false);
        if (chunkAccess != null && chunkAccess.getBiomes() != null) {
            return chunkAccess.getBiomes().getNoiseBiome(i, j, k);
        }
        return this.getUncachedNoiseBiome(i, j, k);
    }

    public Biome getUncachedNoiseBiome(int var1, int var2, int var3);

    public boolean isClientSide();

    @Deprecated
    public int getSeaLevel();

    public DimensionType dimensionType();

    default public BlockPos getHeightmapPos(Heightmap.Types types, BlockPos blockPos) {
        return new BlockPos(blockPos.getX(), this.getHeight(types, blockPos.getX(), blockPos.getZ()), blockPos.getZ());
    }

    default public boolean isEmptyBlock(BlockPos blockPos) {
        return this.getBlockState(blockPos).isAir();
    }

    default public boolean canSeeSkyFromBelowWater(BlockPos blockPos) {
        if (blockPos.getY() >= this.getSeaLevel()) {
            return this.canSeeSky(blockPos);
        }
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), this.getSeaLevel(), blockPos.getZ());
        if (!this.canSeeSky(blockPos2)) {
            return false;
        }
        blockPos2 = blockPos2.below();
        while (blockPos2.getY() > blockPos.getY()) {
            BlockState blockState = this.getBlockState(blockPos2);
            if (blockState.getLightBlock(this, blockPos2) > 0 && !blockState.getMaterial().isLiquid()) {
                return false;
            }
            blockPos2 = blockPos2.below();
        }
        return true;
    }

    @Deprecated
    default public float getBrightness(BlockPos blockPos) {
        return this.dimensionType().brightness(this.getMaxLocalRawBrightness(blockPos));
    }

    default public int getDirectSignal(BlockPos blockPos, Direction direction) {
        return this.getBlockState(blockPos).getDirectSignal(this, blockPos, direction);
    }

    default public ChunkAccess getChunk(BlockPos blockPos) {
        return this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    default public ChunkAccess getChunk(int i, int j) {
        return this.getChunk(i, j, ChunkStatus.FULL, true);
    }

    default public ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus) {
        return this.getChunk(i, j, chunkStatus, true);
    }

    @Override
    @Nullable
    default public BlockGetter getChunkForCollisions(int i, int j) {
        return this.getChunk(i, j, ChunkStatus.EMPTY, false);
    }

    default public boolean isWaterAt(BlockPos blockPos) {
        return this.getFluidState(blockPos).is(FluidTags.WATER);
    }

    default public boolean containsAnyLiquid(AABB aABB) {
        int i = Mth.floor(aABB.minX);
        int j = Mth.ceil(aABB.maxX);
        int k = Mth.floor(aABB.minY);
        int l = Mth.ceil(aABB.maxY);
        int m = Mth.floor(aABB.minZ);
        int n = Mth.ceil(aABB.maxZ);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int o = i; o < j; ++o) {
            for (int p = k; p < l; ++p) {
                for (int q = m; q < n; ++q) {
                    BlockState blockState = this.getBlockState(mutableBlockPos.set(o, p, q));
                    if (blockState.getFluidState().isEmpty()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    default public int getMaxLocalRawBrightness(BlockPos blockPos) {
        return this.getMaxLocalRawBrightness(blockPos, this.getSkyDarken());
    }

    default public int getMaxLocalRawBrightness(BlockPos blockPos, int i) {
        if (blockPos.getX() < -30000000 || blockPos.getZ() < -30000000 || blockPos.getX() >= 30000000 || blockPos.getZ() >= 30000000) {
            return 15;
        }
        return this.getRawBrightness(blockPos, i);
    }

    @Deprecated
    default public boolean hasChunkAt(BlockPos blockPos) {
        return this.hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    @Deprecated
    default public boolean hasChunksAt(BlockPos blockPos, BlockPos blockPos2) {
        return this.hasChunksAt(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
    }

    @Deprecated
    default public boolean hasChunksAt(int i, int j, int k, int l, int m, int n) {
        if (m < 0 || j >= 256) {
            return false;
        }
        k >>= 4;
        l >>= 4;
        n >>= 4;
        for (int o = i >>= 4; o <= l; ++o) {
            for (int p = k; p <= n; ++p) {
                if (this.hasChunk(o, p)) continue;
                return false;
            }
        }
        return true;
    }
}

