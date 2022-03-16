/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

public interface StructureAccess {
    @Nullable
    public StructureStart getStartForStructure(Structure var1);

    public void setStartForStructure(Structure var1, StructureStart var2);

    public LongSet getReferencesForStructure(Structure var1);

    public void addReferenceForStructure(Structure var1, long var2);

    public Map<Structure, LongSet> getAllReferences();

    public void setAllReferences(Map<Structure, LongSet> var1);
}

