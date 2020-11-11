/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class CommandStorage {
    private final Map<String, Container> namespaces = Maps.newHashMap();
    private final DimensionDataStorage storage;

    public CommandStorage(DimensionDataStorage dimensionDataStorage) {
        this.storage = dimensionDataStorage;
    }

    private Container newStorage(String string) {
        Container container = new Container();
        this.namespaces.put(string, container);
        return container;
    }

    public CompoundTag get(ResourceLocation resourceLocation) {
        String string = resourceLocation.getNamespace();
        Container container = this.storage.get(compoundTag -> this.newStorage(string).load(compoundTag), CommandStorage.createId(string));
        return container != null ? container.get(resourceLocation.getPath()) : new CompoundTag();
    }

    public void set(ResourceLocation resourceLocation, CompoundTag compoundTag2) {
        String string = resourceLocation.getNamespace();
        this.storage.computeIfAbsent(compoundTag -> this.newStorage(string).load(compoundTag), () -> this.newStorage(string), CommandStorage.createId(string)).put(resourceLocation.getPath(), compoundTag2);
    }

    public Stream<ResourceLocation> keys() {
        return this.namespaces.entrySet().stream().flatMap(entry -> ((Container)entry.getValue()).getKeys((String)entry.getKey()));
    }

    private static String createId(String string) {
        return "command_storage_" + string;
    }

    static class Container
    extends SavedData {
        private final Map<String, CompoundTag> storage = Maps.newHashMap();

        private Container() {
        }

        private Container load(CompoundTag compoundTag) {
            CompoundTag compoundTag2 = compoundTag.getCompound("contents");
            for (String string : compoundTag2.getAllKeys()) {
                this.storage.put(string, compoundTag2.getCompound(string));
            }
            return this;
        }

        @Override
        public CompoundTag save(CompoundTag compoundTag) {
            CompoundTag compoundTag22 = new CompoundTag();
            this.storage.forEach((string, compoundTag2) -> compoundTag22.put((String)string, compoundTag2.copy()));
            compoundTag.put("contents", compoundTag22);
            return compoundTag;
        }

        public CompoundTag get(String string) {
            CompoundTag compoundTag = this.storage.get(string);
            return compoundTag != null ? compoundTag : new CompoundTag();
        }

        public void put(String string, CompoundTag compoundTag) {
            if (compoundTag.isEmpty()) {
                this.storage.remove(string);
            } else {
                this.storage.put(string, compoundTag);
            }
            this.setDirty();
        }

        public Stream<ResourceLocation> getKeys(String string) {
            return this.storage.keySet().stream().map(string2 -> new ResourceLocation(string, (String)string2));
        }
    }
}

