package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface FeatureAccess {
	@Nullable
	StructureStart getStartForFeature(ConfiguredStructureFeature<?, ?> configuredStructureFeature);

	void setStartForFeature(ConfiguredStructureFeature<?, ?> configuredStructureFeature, StructureStart structureStart);

	LongSet getReferencesForFeature(ConfiguredStructureFeature<?, ?> configuredStructureFeature);

	void addReferenceForFeature(ConfiguredStructureFeature<?, ?> configuredStructureFeature, long l);

	Map<ConfiguredStructureFeature<?, ?>, LongSet> getAllReferences();

	void setAllReferences(Map<ConfiguredStructureFeature<?, ?>, LongSet> map);
}
