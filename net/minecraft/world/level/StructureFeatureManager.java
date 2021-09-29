/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

public class StructureFeatureManager {
    private final LevelAccessor level;
    private final WorldGenSettings worldGenSettings;

    public StructureFeatureManager(LevelAccessor levelAccessor, WorldGenSettings worldGenSettings) {
        this.level = levelAccessor;
        this.worldGenSettings = worldGenSettings;
    }

    public StructureFeatureManager forWorldGenRegion(WorldGenRegion worldGenRegion) {
        if (worldGenRegion.getLevel() != this.level) {
            throw new IllegalStateException("Using invalid feature manager (source level: " + worldGenRegion.getLevel() + ", region: " + worldGenRegion);
        }
        return new StructureFeatureManager(worldGenRegion, this.worldGenSettings);
    }

    public List<? extends StructureStart<?>> startsForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature) {
        LongSet longSet = this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(structureFeature);
        ImmutableList.Builder builder = ImmutableList.builder();
        LongIterator longIterator = longSet.iterator();
        while (longIterator.hasNext()) {
            long l = (Long)longIterator.next();
            SectionPos sectionPos2 = SectionPos.of(new ChunkPos(l), this.level.getMinSection());
            StructureStart<?> structureStart = this.getStartForFeature(sectionPos2, structureFeature, this.level.getChunk(sectionPos2.x(), sectionPos2.z(), ChunkStatus.STRUCTURE_STARTS));
            if (structureStart == null || !structureStart.isValid()) continue;
            builder.add(structureStart);
        }
        return builder.build();
    }

    @Nullable
    public StructureStart<?> getStartForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, FeatureAccess featureAccess) {
        return featureAccess.getStartForFeature(structureFeature);
    }

    public void setStartForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, StructureStart<?> structureStart, FeatureAccess featureAccess) {
        featureAccess.setStartForFeature(structureFeature, structureStart);
    }

    public void addReferenceForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, long l, FeatureAccess featureAccess) {
        featureAccess.addReferenceForFeature(structureFeature, l);
    }

    public boolean shouldGenerateFeatures() {
        return this.worldGenSettings.generateFeatures();
    }

    public StructureStart<?> getStructureAt(BlockPos blockPos, StructureFeature<?> structureFeature) {
        for (StructureStart<?> structureStart : this.startsForFeature(SectionPos.of(blockPos), structureFeature)) {
            if (!structureStart.getBoundingBox().isInside(blockPos)) continue;
            return structureStart;
        }
        return StructureStart.INVALID_START;
    }

    public StructureStart<?> getStructureWithPieceAt(BlockPos blockPos, StructureFeature<?> structureFeature) {
        for (StructureStart<?> structureStart : this.startsForFeature(SectionPos.of(blockPos), structureFeature)) {
            for (StructurePiece structurePiece : structureStart.getPieces()) {
                if (!structurePiece.getBoundingBox().isInside(blockPos)) continue;
                return structureStart;
            }
        }
        return StructureStart.INVALID_START;
    }

    public boolean hasAnyStructureAt(BlockPos blockPos) {
        SectionPos sectionPos = SectionPos.of(blockPos);
        return this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
    }
}

