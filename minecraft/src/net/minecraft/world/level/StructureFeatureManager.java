package net.minecraft.world.level;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureFeatureManager {
	private final ServerLevel level;
	private final WorldGenSettings worldGenSettings;

	public StructureFeatureManager(ServerLevel serverLevel, WorldGenSettings worldGenSettings) {
		this.level = serverLevel;
		this.worldGenSettings = worldGenSettings;
	}

	public Stream<StructureStart> startsForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature) {
		return this.level
			.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES)
			.getReferencesForFeature(structureFeature.getFeatureName())
			.stream()
			.map(long_ -> SectionPos.of(new ChunkPos(long_), 0))
			.map(
				sectionPosx -> this.getStartForFeature(sectionPosx, structureFeature, this.level.getChunk(sectionPosx.x(), sectionPosx.z(), ChunkStatus.STRUCTURE_STARTS))
			)
			.filter(structureStart -> structureStart != null && structureStart.isValid());
	}

	@Nullable
	public StructureStart getStartForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, FeatureAccess featureAccess) {
		return featureAccess.getStartForFeature(structureFeature.getFeatureName());
	}

	public void setStartForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, StructureStart structureStart, FeatureAccess featureAccess) {
		featureAccess.setStartForFeature(structureFeature.getFeatureName(), structureStart);
	}

	public void addReferenceForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, long l, FeatureAccess featureAccess) {
		featureAccess.addReferenceForFeature(structureFeature.getFeatureName(), l);
	}

	public boolean shouldGenerateFeatures() {
		return this.worldGenSettings.generateFeatures();
	}
}
