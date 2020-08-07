package net.minecraft.tags;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.DefaultedRegistry;
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
	ResourceLocation getId(Tag<T> tag);

	default ResourceLocation getIdOrThrow(Tag<T> tag) {
		ResourceLocation resourceLocation = this.getId(tag);
		if (resourceLocation == null) {
			throw new IllegalStateException("Unrecognized tag");
		} else {
			return resourceLocation;
		}
	}

	default Collection<ResourceLocation> getAvailableTags() {
		return this.getAllTags().keySet();
	}

	@Environment(EnvType.CLIENT)
	default Collection<ResourceLocation> getMatchingTags(T object) {
		List<ResourceLocation> list = Lists.<ResourceLocation>newArrayList();

		for (Entry<ResourceLocation, Tag<T>> entry : this.getAllTags().entrySet()) {
			if (((Tag)entry.getValue()).contains(object)) {
				list.add(entry.getKey());
			}
		}

		return list;
	}

	default void serializeToNetwork(FriendlyByteBuf friendlyByteBuf, DefaultedRegistry<T> defaultedRegistry) {
		Map<ResourceLocation, Tag<T>> map = this.getAllTags();
		friendlyByteBuf.writeVarInt(map.size());

		for (Entry<ResourceLocation, Tag<T>> entry : map.entrySet()) {
			friendlyByteBuf.writeResourceLocation((ResourceLocation)entry.getKey());
			friendlyByteBuf.writeVarInt(((Tag)entry.getValue()).getValues().size());

			for (T object : ((Tag)entry.getValue()).getValues()) {
				friendlyByteBuf.writeVarInt(defaultedRegistry.getId(object));
			}
		}
	}

	static <T> TagCollection<T> loadFromNetwork(FriendlyByteBuf friendlyByteBuf, Registry<T> registry) {
		Map<ResourceLocation, Tag<T>> map = Maps.<ResourceLocation, Tag<T>>newHashMap();
		int i = friendlyByteBuf.readVarInt();

		for (int j = 0; j < i; j++) {
			ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
			int k = friendlyByteBuf.readVarInt();
			Builder<T> builder = ImmutableSet.builder();

			for (int l = 0; l < k; l++) {
				builder.add(registry.byId(friendlyByteBuf.readVarInt()));
			}

			map.put(resourceLocation, Tag.fromSet(builder.build()));
		}

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
}
