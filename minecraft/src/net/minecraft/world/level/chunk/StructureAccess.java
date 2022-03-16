package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface StructureAccess {
	@Nullable
	StructureStart getStartForStructure(Structure structure);

	void setStartForStructure(Structure structure, StructureStart structureStart);

	LongSet getReferencesForStructure(Structure structure);

	void addReferenceForStructure(Structure structure, long l);

	Map<Structure, LongSet> getAllReferences();

	void setAllReferences(Map<Structure, LongSet> map);
}
