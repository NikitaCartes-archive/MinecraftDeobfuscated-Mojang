package net.minecraft.world.level.levelgen.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class StructureFeatureIndexSavedData extends SavedData {
	private static final String TAG_REMAINING_INDEXES = "Remaining";
	private static final String TAG_All_INDEXES = "All";
	private final LongSet all;
	private final LongSet remaining;

	public static SavedData.Factory<StructureFeatureIndexSavedData> factory() {
		return new SavedData.Factory<>(StructureFeatureIndexSavedData::new, StructureFeatureIndexSavedData::load, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES);
	}

	private StructureFeatureIndexSavedData(LongSet longSet, LongSet longSet2) {
		this.all = longSet;
		this.remaining = longSet2;
	}

	public StructureFeatureIndexSavedData() {
		this(new LongOpenHashSet(), new LongOpenHashSet());
	}

	public static StructureFeatureIndexSavedData load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		return new StructureFeatureIndexSavedData(new LongOpenHashSet(compoundTag.getLongArray("All")), new LongOpenHashSet(compoundTag.getLongArray("Remaining")));
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
		compoundTag.putLongArray("All", this.all.toLongArray());
		compoundTag.putLongArray("Remaining", this.remaining.toLongArray());
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
