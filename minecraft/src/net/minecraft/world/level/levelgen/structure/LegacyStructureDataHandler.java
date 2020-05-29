package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class LegacyStructureDataHandler {
	private static final Map<String, String> CURRENT_TO_LEGACY_MAP = Util.make(Maps.<String, String>newHashMap(), hashMap -> {
		hashMap.put("Village", "Village");
		hashMap.put("Mineshaft", "Mineshaft");
		hashMap.put("Mansion", "Mansion");
		hashMap.put("Igloo", "Temple");
		hashMap.put("Desert_Pyramid", "Temple");
		hashMap.put("Jungle_Pyramid", "Temple");
		hashMap.put("Swamp_Hut", "Temple");
		hashMap.put("Stronghold", "Stronghold");
		hashMap.put("Monument", "Monument");
		hashMap.put("Fortress", "Fortress");
		hashMap.put("EndCity", "EndCity");
	});
	private static final Map<String, String> LEGACY_TO_CURRENT_MAP = Util.make(Maps.<String, String>newHashMap(), hashMap -> {
		hashMap.put("Iglu", "Igloo");
		hashMap.put("TeDP", "Desert_Pyramid");
		hashMap.put("TeJP", "Jungle_Pyramid");
		hashMap.put("TeSH", "Swamp_Hut");
	});
	private final boolean hasLegacyData;
	private final Map<String, Long2ObjectMap<CompoundTag>> dataMap = Maps.<String, Long2ObjectMap<CompoundTag>>newHashMap();
	private final Map<String, StructureFeatureIndexSavedData> indexMap = Maps.<String, StructureFeatureIndexSavedData>newHashMap();
	private final List<String> legacyKeys;
	private final List<String> currentKeys;

	public LegacyStructureDataHandler(@Nullable DimensionDataStorage dimensionDataStorage, List<String> list, List<String> list2) {
		this.legacyKeys = list;
		this.currentKeys = list2;
		this.populateCaches(dimensionDataStorage);
		boolean bl = false;

		for (String string : this.currentKeys) {
			bl |= this.dataMap.get(string) != null;
		}

		this.hasLegacyData = bl;
	}

	public void removeIndex(long l) {
		for (String string : this.legacyKeys) {
			StructureFeatureIndexSavedData structureFeatureIndexSavedData = (StructureFeatureIndexSavedData)this.indexMap.get(string);
			if (structureFeatureIndexSavedData != null && structureFeatureIndexSavedData.hasUnhandledIndex(l)) {
				structureFeatureIndexSavedData.removeIndex(l);
				structureFeatureIndexSavedData.setDirty();
			}
		}
	}

	public CompoundTag updateFromLegacy(CompoundTag compoundTag) {
		CompoundTag compoundTag2 = compoundTag.getCompound("Level");
		ChunkPos chunkPos = new ChunkPos(compoundTag2.getInt("xPos"), compoundTag2.getInt("zPos"));
		if (this.isUnhandledStructureStart(chunkPos.x, chunkPos.z)) {
			compoundTag = this.updateStructureStart(compoundTag, chunkPos);
		}

		CompoundTag compoundTag3 = compoundTag2.getCompound("Structures");
		CompoundTag compoundTag4 = compoundTag3.getCompound("References");

		for (String string : this.currentKeys) {
			StructureFeature<?> structureFeature = (StructureFeature<?>)StructureFeature.STRUCTURES_REGISTRY.get(string.toLowerCase(Locale.ROOT));
			if (!compoundTag4.contains(string, 12) && structureFeature != null) {
				int i = 8;
				LongList longList = new LongArrayList();

				for (int j = chunkPos.x - 8; j <= chunkPos.x + 8; j++) {
					for (int k = chunkPos.z - 8; k <= chunkPos.z + 8; k++) {
						if (this.hasLegacyStart(j, k, string)) {
							longList.add(ChunkPos.asLong(j, k));
						}
					}
				}

				compoundTag4.putLongArray(string, longList);
			}
		}

		compoundTag3.put("References", compoundTag4);
		compoundTag2.put("Structures", compoundTag3);
		compoundTag.put("Level", compoundTag2);
		return compoundTag;
	}

	private boolean hasLegacyStart(int i, int j, String string) {
		return !this.hasLegacyData
			? false
			: this.dataMap.get(string) != null
				&& ((StructureFeatureIndexSavedData)this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string))).hasStartIndex(ChunkPos.asLong(i, j));
	}

	private boolean isUnhandledStructureStart(int i, int j) {
		if (!this.hasLegacyData) {
			return false;
		} else {
			for (String string : this.currentKeys) {
				if (this.dataMap.get(string) != null
					&& ((StructureFeatureIndexSavedData)this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string))).hasUnhandledIndex(ChunkPos.asLong(i, j))) {
					return true;
				}
			}

			return false;
		}
	}

	private CompoundTag updateStructureStart(CompoundTag compoundTag, ChunkPos chunkPos) {
		CompoundTag compoundTag2 = compoundTag.getCompound("Level");
		CompoundTag compoundTag3 = compoundTag2.getCompound("Structures");
		CompoundTag compoundTag4 = compoundTag3.getCompound("Starts");

		for (String string : this.currentKeys) {
			Long2ObjectMap<CompoundTag> long2ObjectMap = (Long2ObjectMap<CompoundTag>)this.dataMap.get(string);
			if (long2ObjectMap != null) {
				long l = chunkPos.toLong();
				if (((StructureFeatureIndexSavedData)this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string))).hasUnhandledIndex(l)) {
					CompoundTag compoundTag5 = long2ObjectMap.get(l);
					if (compoundTag5 != null) {
						compoundTag4.put(string, compoundTag5);
					}
				}
			}
		}

		compoundTag3.put("Starts", compoundTag4);
		compoundTag2.put("Structures", compoundTag3);
		compoundTag.put("Level", compoundTag2);
		return compoundTag;
	}

	private void populateCaches(@Nullable DimensionDataStorage dimensionDataStorage) {
		if (dimensionDataStorage != null) {
			for (String string : this.legacyKeys) {
				CompoundTag compoundTag = new CompoundTag();

				try {
					compoundTag = dimensionDataStorage.readTagFromDisk(string, 1493).getCompound("data").getCompound("Features");
					if (compoundTag.isEmpty()) {
						continue;
					}
				} catch (IOException var13) {
				}

				for (String string2 : compoundTag.getAllKeys()) {
					CompoundTag compoundTag2 = compoundTag.getCompound(string2);
					long l = ChunkPos.asLong(compoundTag2.getInt("ChunkX"), compoundTag2.getInt("ChunkZ"));
					ListTag listTag = compoundTag2.getList("Children", 10);
					if (!listTag.isEmpty()) {
						String string3 = listTag.getCompound(0).getString("id");
						String string4 = (String)LEGACY_TO_CURRENT_MAP.get(string3);
						if (string4 != null) {
							compoundTag2.putString("id", string4);
						}
					}

					String string3 = compoundTag2.getString("id");
					((Long2ObjectMap)this.dataMap.computeIfAbsent(string3, stringx -> new Long2ObjectOpenHashMap())).put(l, compoundTag2);
				}

				String string5 = string + "_index";
				StructureFeatureIndexSavedData structureFeatureIndexSavedData = dimensionDataStorage.computeIfAbsent(
					() -> new StructureFeatureIndexSavedData(string5), string5
				);
				if (!structureFeatureIndexSavedData.getAll().isEmpty()) {
					this.indexMap.put(string, structureFeatureIndexSavedData);
				} else {
					StructureFeatureIndexSavedData structureFeatureIndexSavedData2 = new StructureFeatureIndexSavedData(string5);
					this.indexMap.put(string, structureFeatureIndexSavedData2);

					for (String string6 : compoundTag.getAllKeys()) {
						CompoundTag compoundTag3 = compoundTag.getCompound(string6);
						structureFeatureIndexSavedData2.addIndex(ChunkPos.asLong(compoundTag3.getInt("ChunkX"), compoundTag3.getInt("ChunkZ")));
					}

					structureFeatureIndexSavedData2.setDirty();
				}
			}
		}
	}

	public static LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> resourceKey, @Nullable DimensionDataStorage dimensionDataStorage) {
		if (resourceKey == Level.OVERWORLD) {
			return new LegacyStructureDataHandler(
				dimensionDataStorage,
				ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"),
				ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument")
			);
		} else if (resourceKey == Level.NETHER) {
			List<String> list = ImmutableList.of("Fortress");
			return new LegacyStructureDataHandler(dimensionDataStorage, list, list);
		} else if (resourceKey == Level.END) {
			List<String> list = ImmutableList.of("EndCity");
			return new LegacyStructureDataHandler(dimensionDataStorage, list, list);
		} else {
			throw new RuntimeException(String.format("Unknown dimension type : %s", resourceKey));
		}
	}
}
