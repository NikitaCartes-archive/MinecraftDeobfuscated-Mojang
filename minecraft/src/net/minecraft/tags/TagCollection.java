package net.minecraft.tags;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface TagCollection<T> {
	Map<ResourceLocation, Tag<T>> getAllTags();

	@Nullable
	default Tag<T> getTag(ResourceLocation resourceLocation) {
		return (Tag<T>)this.getAllTags().get(resourceLocation);
	}

	Tag<T> getTagOrEmpty(ResourceLocation resourceLocation);

	@Nullable
	default ResourceLocation getId(Tag.Named<T> named) {
		return named.getName();
	}

	@Nullable
	ResourceLocation getId(Tag<T> tag);

	default boolean hasTag(ResourceLocation resourceLocation) {
		return this.getAllTags().containsKey(resourceLocation);
	}

	default Collection<ResourceLocation> getAvailableTags() {
		return this.getAllTags().keySet();
	}

	default Collection<ResourceLocation> getMatchingTags(T object) {
		List<ResourceLocation> list = Lists.<ResourceLocation>newArrayList();

		for (Entry<ResourceLocation, Tag<T>> entry : this.getAllTags().entrySet()) {
			if (((Tag)entry.getValue()).contains(object)) {
				list.add(entry.getKey());
			}
		}

		return list;
	}

	default TagCollection.NetworkPayload serializeToNetwork(Registry<T> registry) {
		Map<ResourceLocation, Tag<T>> map = this.getAllTags();
		Map<ResourceLocation, IntList> map2 = Maps.<ResourceLocation, IntList>newHashMapWithExpectedSize(map.size());
		map.forEach((resourceLocation, tag) -> {
			List<T> list = tag.getValues();
			IntList intList = new IntArrayList(list.size());

			for (T object : list) {
				intList.add(registry.getId(object));
			}

			map2.put(resourceLocation, intList);
		});
		return new TagCollection.NetworkPayload(map2);
	}

	static <T> TagCollection<T> createFromNetwork(TagCollection.NetworkPayload networkPayload, Registry<? extends T> registry) {
		Map<ResourceLocation, Tag<T>> map = Maps.<ResourceLocation, Tag<T>>newHashMapWithExpectedSize(networkPayload.tags.size());
		networkPayload.tags.forEach((resourceLocation, intList) -> {
			Builder<T> builder = ImmutableSet.builder();

			for (int i : intList) {
				builder.add((T)registry.byId(i));
			}

			map.put(resourceLocation, Tag.fromSet(builder.build()));
		});
		return of(map);
	}

	static <T> TagCollection<T> empty() {
		return of(ImmutableBiMap.of());
	}

	static <T> TagCollection<T> of(Map<ResourceLocation, Tag<T>> map) {
		final BiMap<ResourceLocation, Tag<T>> biMap = ImmutableBiMap.copyOf(map);
		return new TagCollection<T>() {
			private final Tag<T> empty = SetTag.empty();

			@Override
			public Tag<T> getTagOrEmpty(ResourceLocation resourceLocation) {
				return (Tag<T>)biMap.getOrDefault(resourceLocation, this.empty);
			}

			@Nullable
			@Override
			public ResourceLocation getId(Tag<T> tag) {
				return tag instanceof Tag.Named ? ((Tag.Named)tag).getName() : (ResourceLocation)biMap.inverse().get(tag);
			}

			@Override
			public Map<ResourceLocation, Tag<T>> getAllTags() {
				return biMap;
			}
		};
	}

	public static class NetworkPayload {
		private final Map<ResourceLocation, IntList> tags;

		private NetworkPayload(Map<ResourceLocation, IntList> map) {
			this.tags = map;
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeMap(this.tags, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeIntIdList);
		}

		public static TagCollection.NetworkPayload read(FriendlyByteBuf friendlyByteBuf) {
			return new TagCollection.NetworkPayload(friendlyByteBuf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readIntIdList));
		}
	}
}
