package net.minecraft.world.level.saveddata;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;

public abstract class SavedData {
	private boolean dirty;

	public abstract CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider);

	public void setDirty() {
		this.setDirty(true);
	}

	public void setDirty(boolean bl) {
		this.dirty = bl;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public CompoundTag save(HolderLookup.Provider provider) {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.put("data", this.save(new CompoundTag(), provider));
		NbtUtils.addCurrentDataVersion(compoundTag);
		this.setDirty(false);
		return compoundTag;
	}

	public static record Factory<T extends SavedData>(Supplier<T> constructor, BiFunction<CompoundTag, HolderLookup.Provider, T> deserializer, DataFixTypes type) {
	}
}
