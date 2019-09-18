package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;

public class CommandStorage {
	private final Map<String, CommandStorage.Container> namespaces = Maps.<String, CommandStorage.Container>newHashMap();
	private final DimensionDataStorage storage;

	public CommandStorage(DimensionDataStorage dimensionDataStorage) {
		this.storage = dimensionDataStorage;
	}

	private CommandStorage.Container newStorage(String string, String string2) {
		CommandStorage.Container container = new CommandStorage.Container(string2);
		this.namespaces.put(string, container);
		return container;
	}

	public CompoundTag get(ResourceLocation resourceLocation) {
		String string = resourceLocation.getNamespace();
		String string2 = createId(string);
		CommandStorage.Container container = this.storage.get(() -> this.newStorage(string, string2), string2);
		return container != null ? container.get(resourceLocation.getPath()) : new CompoundTag();
	}

	public void set(ResourceLocation resourceLocation, CompoundTag compoundTag) {
		String string = resourceLocation.getNamespace();
		String string2 = createId(string);
		this.storage.<CommandStorage.Container>computeIfAbsent(() -> this.newStorage(string, string2), string2).put(resourceLocation.getPath(), compoundTag);
	}

	public Stream<ResourceLocation> keys() {
		return this.namespaces.entrySet().stream().flatMap(entry -> ((CommandStorage.Container)entry.getValue()).getKeys((String)entry.getKey()));
	}

	private static String createId(String string) {
		return "command_storage_" + string;
	}

	static class Container extends SavedData {
		private final Map<String, CompoundTag> storage = Maps.<String, CompoundTag>newHashMap();

		public Container(String string) {
			super(string);
		}

		@Override
		public void load(CompoundTag compoundTag) {
			CompoundTag compoundTag2 = compoundTag.getCompound("contents");

			for (String string : compoundTag2.getAllKeys()) {
				this.storage.put(string, compoundTag2.getCompound(string));
			}
		}

		@Override
		public CompoundTag save(CompoundTag compoundTag) {
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
			return this.storage.keySet().stream().map(string2 -> new ResourceLocation(string, string2));
		}
	}
}
