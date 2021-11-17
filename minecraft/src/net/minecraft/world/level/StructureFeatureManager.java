package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

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
		} else {
			return new StructureFeatureManager(worldGenRegion, this.worldGenSettings, this.structureCheck);
		}
	}

	public List<? extends StructureStart<?>> startsForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature) {
		LongSet longSet = this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(structureFeature);
		Builder<StructureStart<?>> builder = ImmutableList.builder();
		LongIterator var5 = longSet.iterator();

		while (var5.hasNext()) {
			long l = (Long)var5.next();
			SectionPos sectionPos2 = SectionPos.of(new ChunkPos(l), this.level.getMinSection());
			StructureStart<?> structureStart = this.getStartForFeature(
				sectionPos2, structureFeature, this.level.getChunk(sectionPos2.x(), sectionPos2.z(), ChunkStatus.STRUCTURE_STARTS)
			);
			if (structureStart != null && structureStart.isValid()) {
				builder.add(structureStart);
			}
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
			if (structureStart.getBoundingBox().isInside(blockPos)) {
				return structureStart;
			}
		}

		return StructureStart.INVALID_START;
	}

	public StructureStart<?> getStructureWithPieceAt(BlockPos blockPos, StructureFeature<?> structureFeature) {
		for (StructureStart<?> structureStart : this.startsForFeature(SectionPos.of(blockPos), structureFeature)) {
			for (StructurePiece structurePiece : structureStart.getPieces()) {
				if (structurePiece.getBoundingBox().isInside(blockPos)) {
					return structureStart;
				}
			}
		}

		return StructureStart.INVALID_START;
	}

	public boolean hasAnyStructureAt(BlockPos blockPos) {
		SectionPos sectionPos = SectionPos.of(blockPos);
		return this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
	}

	public StructureCheckResult checkStructurePresence(ChunkPos chunkPos, StructureFeature<?> structureFeature, boolean bl) {
		return this.structureCheck.checkStart(chunkPos, structureFeature, bl);
	}

	public void addReference(StructureStart<?> structureStart) {
		structureStart.addReference();
		this.structureCheck.incrementReference(structureStart.getChunkPos(), structureStart.getFeature());
	}
}
