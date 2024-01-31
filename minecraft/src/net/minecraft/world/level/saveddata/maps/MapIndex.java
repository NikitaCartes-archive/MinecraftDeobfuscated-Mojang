package net.minecraft.world.level.saveddata.maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class MapIndex extends SavedData {
	public static final String FILE_NAME = "idcounts";
	private final Object2IntMap<String> usedAuxIds = new Object2IntOpenHashMap<>();

	public static SavedData.Factory<MapIndex> factory() {
		return new SavedData.Factory<>(MapIndex::new, MapIndex::load, DataFixTypes.SAVED_DATA_MAP_INDEX);
	}

	public MapIndex() {
		this.usedAuxIds.defaultReturnValue(-1);
	}

	public static MapIndex load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		MapIndex mapIndex = new MapIndex();

		for (String string : compoundTag.getAllKeys()) {
			if (compoundTag.contains(string, 99)) {
				mapIndex.usedAuxIds.put(string, compoundTag.getInt(string));
			}
		}

		return mapIndex;
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
		for (Entry<String> entry : this.usedAuxIds.object2IntEntrySet()) {
			compoundTag.putInt((String)entry.getKey(), entry.getIntValue());
		}

		return compoundTag;
	}

	public MapId getFreeAuxValueForMap() {
		int i = this.usedAuxIds.getInt("map") + 1;
		this.usedAuxIds.put("map", i);
		this.setDirty();
		return new MapId(i);
	}
}
