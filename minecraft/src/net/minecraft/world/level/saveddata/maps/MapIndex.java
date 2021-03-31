package net.minecraft.world.level.saveddata.maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class MapIndex extends SavedData {
	public static final String FILE_NAME = "idcounts";
	private final Object2IntMap<String> usedAuxIds = new Object2IntOpenHashMap<>();

	public MapIndex() {
		this.usedAuxIds.defaultReturnValue(-1);
	}

	public static MapIndex load(CompoundTag compoundTag) {
		MapIndex mapIndex = new MapIndex();

		for (String string : compoundTag.getAllKeys()) {
			if (compoundTag.contains(string, 99)) {
				mapIndex.usedAuxIds.put(string, compoundTag.getInt(string));
			}
		}

		return mapIndex;
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		for (Entry<String> entry : this.usedAuxIds.object2IntEntrySet()) {
			compoundTag.putInt((String)entry.getKey(), entry.getIntValue());
		}

		return compoundTag;
	}

	public int getFreeAuxValueForMap() {
		int i = this.usedAuxIds.getInt("map") + 1;
		this.usedAuxIds.put("map", i);
		this.setDirty();
		return i;
	}
}
