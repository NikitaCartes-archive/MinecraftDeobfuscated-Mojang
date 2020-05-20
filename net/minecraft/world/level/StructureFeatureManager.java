/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.mojang.datafixers.DataFixUtils;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

public class StructureFeatureManager {
    private final ServerLevel level;
    private final WorldGenSettings worldGenSettings;

    public StructureFeatureManager(ServerLevel serverLevel, WorldGenSettings worldGenSettings) {
        this.level = serverLevel;
        this.worldGenSettings = worldGenSettings;
    }

    public Stream<? extends StructureStart<?>> startsForFeature(SectionPos sectionPos2, StructureFeature<?> structureFeature) {
        return this.level.getChunk(sectionPos2.x(), sectionPos2.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(structureFeature.getFeatureName()).stream().map(long_ -> SectionPos.of(new ChunkPos((long)long_), 0)).map(sectionPos -> this.getStartForFeature((SectionPos)sectionPos, structureFeature, this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_STARTS))).filter(structureStart -> structureStart != null && structureStart.isValid());
    }

    @Nullable
    public StructureStart<?> getStartForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, FeatureAccess featureAccess) {
        return featureAccess.getStartForFeature(structureFeature.getFeatureName());
    }

    public void setStartForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, StructureStart<?> structureStart, FeatureAccess featureAccess) {
        featureAccess.setStartForFeature(structureFeature.getFeatureName(), structureStart);
    }

    public void addReferenceForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, long l, FeatureAccess featureAccess) {
        featureAccess.addReferenceForFeature(structureFeature.getFeatureName(), l);
    }

    public boolean shouldGenerateFeatures() {
        return this.worldGenSettings.generateFeatures();
    }

    public StructureStart<?> getStructureAt(BlockPos blockPos, boolean bl, StructureFeature<?> structureFeature) {
        return DataFixUtils.orElse(this.startsForFeature(SectionPos.of(blockPos), structureFeature).filter(structureStart -> structureStart.getBoundingBox().isInside(blockPos)).filter(structureStart -> !bl || structureStart.getPieces().stream().anyMatch(structurePiece -> structurePiece.getBoundingBox().isInside(blockPos))).findFirst(), StructureStart.INVALID_START);
    }
}

