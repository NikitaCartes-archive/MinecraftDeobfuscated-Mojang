package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class ForcedChunksSavedData extends SavedData {
	private LongSet chunks = new LongOpenHashSet();

	public ForcedChunksSavedData() {
		super("chunks");
	}

	@Override
	public void load(CompoundTag compoundTag) {
		this.chunks = new LongOpenHashSet(compoundTag.getLongArray("Forced"));
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		compoundTag.putLongArray("Forced", this.chunks.toLongArray());
		return compoundTag;
	}

	public LongSet getChunks() {
		return this.chunks;
	}
}
