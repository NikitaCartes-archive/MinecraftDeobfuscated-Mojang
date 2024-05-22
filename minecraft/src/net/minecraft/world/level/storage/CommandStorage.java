package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class CommandStorage {
	private static final String ID_PREFIX = "command_storage_";
	private final Map<String, CommandStorage.Container> namespaces = Maps.<String, CommandStorage.Container>newHashMap();
	private final DimensionDataStorage storage;

	public CommandStorage(DimensionDataStorage dimensionDataStorage) {
		this.storage = dimensionDataStorage;
	}

	private CommandStorage.Container newStorage(String string) {
		CommandStorage.Container container = new CommandStorage.Container();
		this.namespaces.put(string, container);
		return container;
	}

	private SavedData.Factory<CommandStorage.Container> factory(String string) {
		return new SavedData.Factory<>(
			() -> this.newStorage(string), (compoundTag, provider) -> this.newStorage(string).load(compoundTag), DataFixTypes.SAVED_DATA_COMMAND_STORAGE
		);
	}

	public CompoundTag get(ResourceLocation resourceLocation) {
		String string = resourceLocation.getNamespace();
		CommandStorage.Container container = this.storage.get(this.factory(string), createId(string));
		return container != null ? container.get(resourceLocation.getPath()) : new CompoundTag();
	}

	public void set(ResourceLocation resourceLocation, CompoundTag compoundTag) {
		String string = resourceLocation.getNamespace();
		this.storage.computeIfAbsent(this.factory(string), createId(string)).put(resourceLocation.getPath(), compoundTag);
	}

	public Stream<ResourceLocation> keys() {
		return this.namespaces.entrySet().stream().flatMap(entry -> ((CommandStorage.Container)entry.getValue()).getKeys((String)entry.getKey()));
	}

	private static String createId(String string) {
		return "command_storage_" + string;
	}

	static class Container extends SavedData {
		private static final String TAG_CONTENTS = "contents";
		private final Map<String, CompoundTag> storage = Maps.<String, CompoundTag>newHashMap();

		CommandStorage.Container load(CompoundTag compoundTag) {
			CompoundTag compoundTag2 = compoundTag.getCompound("contents");

			for (String string : compoundTag2.getAllKeys()) {
				this.storage.put(string, compoundTag2.getCompound(string));
			}

			return this;
		}

		@Override
		public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
			CompoundTag compoundTag2 = new CompoundTag();
			this.storage.forEach((string, compoundTag2x) -> compoundTag2.put(string, compoundTag2x.copy()));
			compoundTag.put("contents", compoundTag2);
			return compoundTag;
		}

		public CompoundTag get(String string) {
			CompoundTag compoundTag = (CompoundTag)this.storage.get(string);
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
			return this.storage.keySet().stream().map(string2 -> ResourceLocation.fromNamespaceAndPath(string, string2));
		}
	}
}
