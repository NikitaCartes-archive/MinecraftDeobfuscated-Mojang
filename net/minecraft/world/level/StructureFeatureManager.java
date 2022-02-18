/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

public class StructureFeatureManager {
    private final LevelAccessor level;
    private final WorldGenSettings worldGenSettings;
    private final StructureCheck structureCheck;

    public StructureFeatureManager(LevelAccessor levelAccessor, WorldGenSettings worldGenSettings, StructureCheck structureCheck) {
        this.level = levelAccessor;
        this.worldGenSettings = worldGenSettings;
        this.structureCheck = structureCheck;
    }

    public StructureFeatureManager forWorldGenRegion(WorldGenRegion worldGenRegion) {
        if (worldGenRegion.getLevel() != this.level) {
            throw new IllegalStateException("Using invalid feature manager (source level: " + worldGenRegion.getLevel() + ", region: " + worldGenRegion);
        }
        return new StructureFeatureManager(worldGenRegion, this.worldGenSettings, this.structureCheck);
    }

    public List<StructureStart> startsForFeature(SectionPos sectionPos, Predicate<ConfiguredStructureFeature<?, ?>> predicate) {
        Map<ConfiguredStructureFeature<?, ?>, LongSet> map = this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
        ImmutableList.Builder builder = ImmutableList.builder();
        for (Map.Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : map.entrySet()) {
            ConfiguredStructureFeature<?, ?> configuredStructureFeature = entry.getKey();
            if (!predicate.test(configuredStructureFeature)) continue;
            this.fillStartsForFeature(configuredStructureFeature, entry.getValue(), builder::add);
        }
        return builder.build();
    }

    public List<StructureStart> startsForFeature(SectionPos sectionPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
        LongSet longSet = this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(configuredStructureFeature);
        ImmutableList.Builder builder = ImmutableList.builder();
        this.fillStartsForFeature(configuredStructureFeature, longSet, builder::add);
        return builder.build();
    }

    public void fillStartsForFeature(ConfiguredStructureFeature<?, ?> configuredStructureFeature, LongSet longSet, Consumer<StructureStart> consumer) {
        LongIterator longIterator = longSet.iterator();
        while (longIterator.hasNext()) {
            long l = (Long)longIterator.next();
            SectionPos sectionPos = SectionPos.of(new ChunkPos(l), this.level.getMinSection());
            StructureStart structureStart = this.getStartForFeature(sectionPos, configuredStructureFeature, this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_STARTS));
            if (structureStart == null || !structureStart.isValid()) continue;
            consumer.accept(structureStart);
        }
    }

    @Nullable
    public StructureStart getStartForFeature(SectionPos sectionPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature, FeatureAccess featureAccess) {
        return featureAccess.getStartForFeature(configuredStructureFeature);
    }

    public void setStartForFeature(SectionPos sectionPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature, StructureStart structureStart, FeatureAccess featureAccess) {
        featureAccess.setStartForFeature(configuredStructureFeature, structureStart);
    }

    public void addReferenceForFeature(SectionPos sectionPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature, long l, FeatureAccess featureAccess) {
        featureAccess.addReferenceForFeature(configuredStructureFeature, l);
    }

    public boolean shouldGenerateFeatures() {
        return this.worldGenSettings.generateFeatures();
    }

    public StructureStart getStructureAt(BlockPos blockPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
        for (StructureStart structureStart : this.startsForFeature(SectionPos.of(blockPos), configuredStructureFeature)) {
            if (!structureStart.getBoundingBox().isInside(blockPos)) continue;
            return structureStart;
        }
        return StructureStart.INVALID_START;
    }

    public StructureStart getStructureWithPieceAt(BlockPos blockPos, ResourceKey<ConfiguredStructureFeature<?, ?>> resourceKey) {
        ConfiguredStructureFeature<?, ?> configuredStructureFeature = this.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).get(resourceKey);
        if (configuredStructureFeature == null) {
            return StructureStart.INVALID_START;
        }
        return this.getStructureWithPieceAt(blockPos, configuredStructureFeature);
    }

    public StructureStart getStructureWithPieceAt(BlockPos blockPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
        for (StructureStart structureStart : this.startsForFeature(SectionPos.of(blockPos), configuredStructureFeature)) {
            if (!this.structureHasPieceAt(blockPos, structureStart)) continue;
            return structureStart;
        }
        return StructureStart.INVALID_START;
    }

    public boolean structureHasPieceAt(BlockPos blockPos, StructureStart structureStart) {
        for (StructurePiece structurePiece : structureStart.getPieces()) {
            if (!structurePiece.getBoundingBox().isInside(blockPos)) continue;
            return true;
        }
        return false;
    }

    public boolean hasAnyStructureAt(BlockPos blockPos) {
        SectionPos sectionPos = SectionPos.of(blockPos);
        return this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
    }

    public Map<ConfiguredStructureFeature<?, ?>, LongSet> getAllStructuresAt(BlockPos blockPos) {
        SectionPos sectionPos = SectionPos.of(blockPos);
        return this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
    }

    public StructureCheckResult checkStructurePresence(ChunkPos chunkPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature, boolean bl) {
        return this.structureCheck.checkStart(chunkPos, configuredStructureFeature, bl);
    }

    public void addReference(StructureStart structureStart) {
        structureStart.addReference();
        this.structureCheck.incrementReference(structureStart.getChunkPos(), structureStart.getFeature());
    }

    public RegistryAccess registryAccess() {
        return this.level.registryAccess();
    }
}

