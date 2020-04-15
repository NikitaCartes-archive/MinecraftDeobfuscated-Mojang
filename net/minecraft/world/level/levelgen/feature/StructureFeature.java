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
import net.minecraft.world.level.dimension.DimensionType;
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
        int j = this.getSpacing(chunkGenerator.getDimensionType(), chunkGenerator.getSettings());
        int k = blockPos.getX() >> 4;
        int l = blockPos.getZ() >> 4;
        WorldgenRandom worldgenRandom = new WorldgenRandom();
        block0: for (int m = 0; m <= i; ++m) {
            for (int n = -m; n <= m; ++n) {
                boolean bl2 = n == -m || n == m;
                for (int o = -m; o <= m; ++o) {
                    boolean bl3;
                    boolean bl4 = bl3 = o == -m || o == m;
                    if (!bl2 && !bl3) continue;
                    int p = k + j * n;
                    int q = l + j * o;
                    ChunkPos chunkPos = this.getPotentialFeatureChunk(chunkGenerator, worldgenRandom, p, q);
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
                    if (m == 0) break;
                }
                if (m == 0) continue block0;
            }
        }
        return null;
    }

    protected int getSpacing(DimensionType dimensionType, ChunkGeneratorSettings chunkGeneratorSettings) {
        return 1;
    }

    protected int getSeparation(DimensionType dimensionType, ChunkGeneratorSettings chunkGeneratorSettings) {
        return 0;
    }

    protected int getRandomSalt(ChunkGeneratorSettings chunkGeneratorSettings) {
        return 0;
    }

    protected boolean linearSeparation() {
        return true;
    }

    public final ChunkPos getPotentialFeatureChunk(ChunkGenerator<?> chunkGenerator, WorldgenRandom worldgenRandom, int i, int j) {
        int p;
        int o;
        Object chunkGeneratorSettings = chunkGenerator.getSettings();
        DimensionType dimensionType = chunkGenerator.getDimensionType();
        int k = this.getSpacing(dimensionType, (ChunkGeneratorSettings)chunkGeneratorSettings);
        int l = this.getSeparation(dimensionType, (ChunkGeneratorSettings)chunkGeneratorSettings);
        int m = Math.floorDiv(i, k);
        int n = Math.floorDiv(j, k);
        worldgenRandom.setLargeFeatureWithSalt(chunkGenerator.getSeed(), m, n, this.getRandomSalt((ChunkGeneratorSettings)chunkGeneratorSettings));
        if (this.linearSeparation()) {
            o = worldgenRandom.nextInt(k - l);
            p = worldgenRandom.nextInt(k - l);
        } else {
            o = (worldgenRandom.nextInt(k - l) + worldgenRandom.nextInt(k - l)) / 2;
            p = (worldgenRandom.nextInt(k - l) + worldgenRandom.nextInt(k - l)) / 2;
        }
        return new ChunkPos(m * k + o, n * k + p);
    }

    public boolean featureChunk(BiomeManager biomeManager, ChunkGenerator<?> chunkGenerator, WorldgenRandom worldgenRandom, int i, int j, Biome biome) {
        ChunkPos chunkPos = this.getPotentialFeatureChunk(chunkGenerator, worldgenRandom, i, j);
        return i == chunkPos.x && j == chunkPos.z && chunkGenerator.isBiomeValidStartForStructure(biome, this) && this.isFeatureChunk(biomeManager, chunkGenerator, worldgenRandom, i, j, biome, chunkPos);
    }

    protected boolean isFeatureChunk(BiomeManager biomeManager, ChunkGenerator<?> chunkGenerator, WorldgenRandom worldgenRandom, int i, int j, Biome biome, ChunkPos chunkPos) {
        return true;
    }

    public abstract StructureStartFactory getStartFactory();

    public abstract String getFeatureName();

    public abstract int getLookupRange();

    public static interface StructureStartFactory {
        public StructureStart create(StructureFeature<?> var1, int var2, int var3, BoundingBox var4, int var5, long var6);
    }
}

