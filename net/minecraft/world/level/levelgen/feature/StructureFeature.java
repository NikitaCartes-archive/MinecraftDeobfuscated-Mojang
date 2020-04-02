/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class StructureFeature<C extends FeatureConfiguration>
extends Feature<C> {
    private static final Logger LOGGER = LogManager.getLogger();

    public StructureFeature(Function<Dynamic<?>, ? extends C> function) {
        super(function);
    }

    @Override
    public ConfiguredFeature<C, ? extends StructureFeature<C>> configured(C featureConfiguration) {
        return new ConfiguredFeature<C, StructureFeature>(this, featureConfiguration);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, C featureConfiguration) {
        if (!levelAccessor.getLevelData().isGenerateMapFeatures()) {
            return false;
        }
        int i = blockPos.getX() >> 4;
        int j = blockPos.getZ() >> 4;
        int k = i << 4;
        int l = j << 4;
        return structureFeatureManager.startsForFeature(SectionPos.of(blockPos), this, levelAccessor).map(structureStart -> {
            structureStart.postProcess(levelAccessor, structureFeatureManager, chunkGenerator, random, new BoundingBox(k, l, k + 15, l + 15), new ChunkPos(i, j));
            return null;
        }).count() != 0L;
    }

    protected StructureStart getStructureAt(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, BlockPos blockPos, boolean bl) {
        return structureFeatureManager.startsForFeature(SectionPos.of(blockPos), this, levelAccessor).filter(structureStart -> structureStart.getBoundingBox().isInside(blockPos)).filter(structureStart -> !bl || structureStart.getPieces().stream().anyMatch(structurePiece -> structurePiece.getBoundingBox().isInside(blockPos))).findFirst().orElse(StructureStart.INVALID_START);
    }

    public boolean isInsideBoundingFeature(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, BlockPos blockPos) {
        return this.getStructureAt(levelAccessor, structureFeatureManager, blockPos, false).isValid();
    }

    public boolean isInsideFeature(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, BlockPos blockPos) {
        return this.getStructureAt(levelAccessor, structureFeatureManager, blockPos, true).isValid();
    }

    @Nullable
    public BlockPos getNearestGeneratedFeature(ServerLevel serverLevel, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, BlockPos blockPos, int i, boolean bl) {
        if (!chunkGenerator.getBiomeSource().canGenerateStructure(this)) {
            return null;
        }
        StructureFeatureManager structureFeatureManager = serverLevel.structureFeatureManager();
        int j = blockPos.getX() >> 4;
        int k = blockPos.getZ() >> 4;
        WorldgenRandom worldgenRandom = new WorldgenRandom();
        block0: for (int l = 0; l <= i; ++l) {
            for (int m = -l; m <= l; ++m) {
                boolean bl2 = m == -l || m == l;
                for (int n = -l; n <= l; ++n) {
                    boolean bl3;
                    boolean bl4 = bl3 = n == -l || n == l;
                    if (!bl2 && !bl3) continue;
                    ChunkPos chunkPos = this.getPotentialFeatureChunkFromLocationWithOffset(chunkGenerator, worldgenRandom, j, k, m, n);
                    ChunkAccess chunkAccess = serverLevel.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
                    StructureStart structureStart = structureFeatureManager.getStartForFeature(SectionPos.of(chunkAccess.getPos(), 0), this, chunkAccess);
                    if (structureStart != null && structureStart.isValid()) {
                        if (bl && structureStart.canBeReferenced()) {
                            structureStart.addReference();
                            return structureStart.getLocatePos();
                        }
                        if (!bl) {
                            return structureStart.getLocatePos();
                        }
                    }
                    if (l == 0) break;
                }
                if (l == 0) continue block0;
            }
        }
        return null;
    }

    protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> chunkGenerator, Random random, int i, int j, int k, int l) {
        return new ChunkPos(i + k, j + l);
    }

    public abstract boolean isFeatureChunk(BiomeManager var1, ChunkGenerator<?> var2, Random var3, int var4, int var5, Biome var6);

    public abstract StructureStartFactory getStartFactory();

    public abstract String getFeatureName();

    public abstract int getLookupRange();

    public static interface StructureStartFactory {
        public StructureStart create(StructureFeature<?> var1, int var2, int var3, BoundingBox var4, int var5, long var6);
    }
}

