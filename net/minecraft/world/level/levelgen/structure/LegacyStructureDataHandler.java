/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureFeatureIndexSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;

public class LegacyStructureDataHandler {
    private static final Map<String, String> CURRENT_TO_LEGACY_MAP = Util.make(Maps.newHashMap(), hashMap -> {
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
    private static final Map<String, String> LEGACY_TO_CURRENT_MAP = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("Iglu", "Igloo");
        hashMap.put("TeDP", "Desert_Pyramid");
        hashMap.put("TeJP", "Jungle_Pyramid");
        hashMap.put("TeSH", "Swamp_Hut");
    });
    private static final Set<String> OLD_STRUCTURE_REGISTRY_KEYS = Set.of("pillager_outpost", "mineshaft", "mansion", "jungle_pyramid", "desert_pyramid", "igloo", "ruined_portal", "shipwreck", "swamp_hut", "stronghold", "monument", "ocean_ruin", "fortress", "endcity", "buried_treasure", "village", "nether_fossil", "bastion_remnant");
    private final boolean hasLegacyData;
    private final Map<String, Long2ObjectMap<CompoundTag>> dataMap = Maps.newHashMap();
    private final Map<String, StructureFeatureIndexSavedData> indexMap = Maps.newHashMap();
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
            StructureFeatureIndexSavedData structureFeatureIndexSavedData = this.indexMap.get(string);
            if (structureFeatureIndexSavedData == null || !structureFeatureIndexSavedData.hasUnhandledIndex(l)) continue;
            structureFeatureIndexSavedData.removeIndex(l);
            structureFeatureIndexSavedData.setDirty();
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
            boolean bl = OLD_STRUCTURE_REGISTRY_KEYS.contains(string.toLowerCase(Locale.ROOT));
            if (compoundTag4.contains(string, 12) || !bl) continue;
            int i = 8;
            LongArrayList longList = new LongArrayList();
            for (int j = chunkPos.x - 8; j <= chunkPos.x + 8; ++j) {
                for (int k = chunkPos.z - 8; k <= chunkPos.z + 8; ++k) {
                    if (!this.hasLegacyStart(j, k, string)) continue;
                    longList.add(ChunkPos.asLong(j, k));
                }
            }
            compoundTag4.putLongArray(string, longList);
        }
        compoundTag3.put("References", compoundTag4);
        compoundTag2.put("Structures", compoundTag3);
        compoundTag.put("Level", compoundTag2);
        return compoundTag;
    }

    private boolean hasLegacyStart(int i, int j, String string) {
        if (!this.hasLegacyData) {
            return false;
        }
        return this.dataMap.get(string) != null && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string)).hasStartIndex(ChunkPos.asLong(i, j));
    }

    private boolean isUnhandledStructureStart(int i, int j) {
        if (!this.hasLegacyData) {
            return false;
        }
        for (String string : this.currentKeys) {
            if (this.dataMap.get(string) == null || !this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string)).hasUnhandledIndex(ChunkPos.asLong(i, j))) continue;
            return true;
        }
        return false;
    }

    private CompoundTag updateStructureStart(CompoundTag compoundTag, ChunkPos chunkPos) {
        CompoundTag compoundTag2 = compoundTag.getCompound("Level");
        CompoundTag compoundTag3 = compoundTag2.getCompound("Structures");
        CompoundTag compoundTag4 = compoundTag3.getCompound("Starts");
        for (String string : this.currentKeys) {
            CompoundTag compoundTag5;
            Long2ObjectMap<CompoundTag> long2ObjectMap = this.dataMap.get(string);
            if (long2ObjectMap == null) continue;
            long l = chunkPos.toLong();
            if (!this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string)).hasUnhandledIndex(l) || (compoundTag5 = (CompoundTag)long2ObjectMap.get(l)) == null) continue;
            compoundTag4.put(string, compoundTag5);
        }
        compoundTag3.put("Starts", compoundTag4);
        compoundTag2.put("Structures", compoundTag3);
        compoundTag.put("Level", compoundTag2);
        return compoundTag;
    }

    private void populateCaches(@Nullable DimensionDataStorage dimensionDataStorage) {
        if (dimensionDataStorage == null) {
            return;
        }
        for (String string2 : this.legacyKeys) {
            CompoundTag compoundTag = new CompoundTag();
            try {
                compoundTag = dimensionDataStorage.readTagFromDisk(string2, 1493).getCompound("data").getCompound("Features");
                if (compoundTag.isEmpty()) {
                    continue;
                }
            } catch (IOException iOException) {
                // empty catch block
            }
            for (String string22 : compoundTag.getAllKeys()) {
                String string3;
                String string4;
                CompoundTag compoundTag2 = compoundTag.getCompound(string22);
                long l = ChunkPos.asLong(compoundTag2.getInt("ChunkX"), compoundTag2.getInt("ChunkZ"));
                ListTag listTag = compoundTag2.getList("Children", 10);
                if (!listTag.isEmpty() && (string4 = LEGACY_TO_CURRENT_MAP.get(string3 = listTag.getCompound(0).getString("id"))) != null) {
                    compoundTag2.putString("id", string4);
                }
                string3 = compoundTag2.getString("id");
                this.dataMap.computeIfAbsent(string3, string -> new Long2ObjectOpenHashMap()).put(l, compoundTag2);
            }
            String string5 = string2 + "_index";
            StructureFeatureIndexSavedData structureFeatureIndexSavedData = dimensionDataStorage.computeIfAbsent(StructureFeatureIndexSavedData::load, StructureFeatureIndexSavedData::new, string5);
            if (structureFeatureIndexSavedData.getAll().isEmpty()) {
                StructureFeatureIndexSavedData structureFeatureIndexSavedData2 = new StructureFeatureIndexSavedData();
                this.indexMap.put(string2, structureFeatureIndexSavedData2);
                for (String string6 : compoundTag.getAllKeys()) {
                    CompoundTag compoundTag3 = compoundTag.getCompound(string6);
                    structureFeatureIndexSavedData2.addIndex(ChunkPos.asLong(compoundTag3.getInt("ChunkX"), compoundTag3.getInt("ChunkZ")));
                }
                structureFeatureIndexSavedData2.setDirty();
                continue;
            }
            this.indexMap.put(string2, structureFeatureIndexSavedData);
        }
    }

    public static LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> resourceKey, @Nullable DimensionDataStorage dimensionDataStorage) {
        if (resourceKey == Level.OVERWORLD) {
            return new LegacyStructureDataHandler(dimensionDataStorage, ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"), ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument"));
        }
        if (resourceKey == Level.NETHER) {
            ImmutableList<String> list = ImmutableList.of("Fortress");
            return new LegacyStructureDataHandler(dimensionDataStorage, list, list);
        }
        if (resourceKey == Level.END) {
            ImmutableList<String> list = ImmutableList.of("EndCity");
            return new LegacyStructureDataHandler(dimensionDataStorage, list, list);
        }
        throw new RuntimeException(String.format(Locale.ROOT, "Unknown dimension type : %s", resourceKey));
    }
}

