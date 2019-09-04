/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

public interface FeatureAccess {
    @Nullable
    public StructureStart getStartForFeature(String var1);

    public void setStartForFeature(String var1, StructureStart var2);

    public LongSet getReferencesForFeature(String var1);

    public void addReferenceForFeature(String var1, long var2);

    public Map<String, LongSet> getAllReferences();

    public void setAllReferences(Map<String, LongSet> var1);
}

