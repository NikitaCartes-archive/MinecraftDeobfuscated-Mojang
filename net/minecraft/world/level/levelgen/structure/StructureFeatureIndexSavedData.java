/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class StructureFeatureIndexSavedData
extends SavedData {
    private static final String TAG_REMAINING_INDEXES = "Remaining";
    private static final String TAG_All_INDEXES = "All";
    private final LongSet all;
    private final LongSet remaining;

    private StructureFeatureIndexSavedData(LongSet longSet, LongSet longSet2) {
        this.all = longSet;
        this.remaining = longSet2;
    }

    public StructureFeatureIndexSavedData() {
        this(new LongOpenHashSet(), new LongOpenHashSet());
    }

    public static StructureFeatureIndexSavedData load(CompoundTag compoundTag) {
        return new StructureFeatureIndexSavedData(new LongOpenHashSet(compoundTag.getLongArray(TAG_All_INDEXES)), new LongOpenHashSet(compoundTag.getLongArray(TAG_REMAINING_INDEXES)));
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putLongArray(TAG_All_INDEXES, this.all.toLongArray());
        compoundTag.putLongArray(TAG_REMAINING_INDEXES, this.remaining.toLongArray());
        return compoundTag;
    }

    public void addIndex(long l) {
        this.all.add(l);
        this.remaining.add(l);
    }

    public boolean hasStartIndex(long l) {
        return this.all.contains(l);
    }

    public boolean hasUnhandledIndex(long l) {
        return this.remaining.contains(l);
    }

    public void removeIndex(long l) {
        this.remaining.remove(l);
    }

    public LongSet getAll() {
        return this.all;
    }
}

