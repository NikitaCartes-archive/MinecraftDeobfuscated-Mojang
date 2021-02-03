package net.minecraft.world.level;

import com.mojang.datafixers.DataFixUtils;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

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
		} else {
			return new StructureFeatureManager(worldGenRegion, this.worldGenSettings);
		}
	}

	public Stream<? extends StructureStart<?>> startsForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature) {
		return this.level
			.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES)
			.getReferencesForFeature(structureFeature)
			.stream()
			.map(long_ -> SectionPos.of(new ChunkPos(long_), this.level.getMinSection()))
			.map(
				sectionPosx -> this.getStartForFeature(sectionPosx, structureFeature, this.level.getChunk(sectionPosx.x(), sectionPosx.z(), ChunkStatus.STRUCTURE_STARTS))
			)
			.filter(structureStart -> structureStart != null && structureStart.isValid());
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

	public StructureStart<?> getStructureAt(BlockPos blockPos, boolean bl, StructureFeature<?> structureFeature) {
		return DataFixUtils.orElse(
			this.startsForFeature(SectionPos.of(blockPos), structureFeature)
				.filter(structureStart -> structureStart.getBoundingBox().isInside(blockPos))
				.filter(structureStart -> !bl || structureStart.getPieces().stream().anyMatch(structurePiece -> structurePiece.getBoundingBox().isInside(blockPos)))
				.findFirst(),
			StructureStart.INVALID_START
		);
	}
}
