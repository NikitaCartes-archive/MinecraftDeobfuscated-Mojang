package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface FeatureAccess extends BlockGetter {
	@Nullable
	StructureStart getStartForFeature(String string);

	void setStartForFeature(String string, StructureStart structureStart);

	LongSet getReferencesForFeature(String string);

	void addReferenceForFeature(String string, long l);

	Map<String, LongSet> getAllReferences();

	void setAllReferences(Map<String, LongSet> map);
}
