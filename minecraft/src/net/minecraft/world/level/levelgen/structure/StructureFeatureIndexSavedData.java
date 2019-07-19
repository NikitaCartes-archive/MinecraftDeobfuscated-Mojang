package net.minecraft.world.level.levelgen.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class StructureFeatureIndexSavedData extends SavedData {
	private LongSet all = new LongOpenHashSet();
	private LongSet remaining = new LongOpenHashSet();

	public StructureFeatureIndexSavedData(String string) {
		super(string);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		this.all = new LongOpenHashSet(compoundTag.getLongArray("All"));
		this.remaining = new LongOpenHashSet(compoundTag.getLongArray("Remaining"));
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
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
