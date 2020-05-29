package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface FeatureAccess {
	@Nullable
	StructureStart<?> getStartForFeature(StructureFeature<?> structureFeature);

	void setStartForFeature(StructureFeature<?> structureFeature, StructureStart<?> structureStart);

	LongSet getReferencesForFeature(StructureFeature<?> structureFeature);

	void addReferenceForFeature(StructureFeature<?> structureFeature, long l);

	Map<StructureFeature<?>, LongSet> getAllReferences();

	void setAllReferences(Map<StructureFeature<?>, LongSet> map);
}
