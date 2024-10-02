package net.minecraft.tags;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;

public class TagNetworkSerialization {
	public static Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> serializeTagsToNetwork(
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess
	) {
		return (Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload>)RegistrySynchronization.networkSafeRegistries(layeredRegistryAccess)
			.map(registryEntry -> Pair.of(registryEntry.key(), serializeToNetwork(registryEntry.value())))
			.filter(pair -> !((TagNetworkSerialization.NetworkPayload)pair.getSecond()).isEmpty())
			.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	private static <T> TagNetworkSerialization.NetworkPayload serializeToNetwork(Registry<T> registry) {
		Map<ResourceLocation, IntList> map = new HashMap();
		registry.getTags().forEach(named -> {
			IntList intList = new IntArrayList(named.size());

			for (Holder<T> holder : named) {
				if (holder.kind() != Holder.Kind.REFERENCE) {
					throw new IllegalStateException("Can't serialize unregistered value " + holder);
				}

				intList.add(registry.getId(holder.value()));
			}

			map.put(named.key().location(), intList);
		});
		return new TagNetworkSerialization.NetworkPayload(map);
	}

	static <T> TagLoader.LoadResult<T> deserializeTagsFromNetwork(Registry<T> registry, TagNetworkSerialization.NetworkPayload networkPayload) {
		ResourceKey<? extends Registry<T>> resourceKey = registry.key();
		Map<TagKey<T>, List<Holder<T>>> map = new HashMap();
		networkPayload.tags.forEach((resourceLocation, intList) -> {
			TagKey<T> tagKey = TagKey.create(resourceKey, resourceLocation);
			List<Holder<T>> list = (List<Holder<T>>)intList.intStream().mapToObj(registry::get).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
			map.put(tagKey, list);
		});
		return new TagLoader.LoadResult<>(resourceKey, map);
	}

	public static final class NetworkPayload {
		public static final TagNetworkSerialization.NetworkPayload EMPTY = new TagNetworkSerialization.NetworkPayload(Map.of());
		final Map<ResourceLocation, IntList> tags;

		NetworkPayload(Map<ResourceLocation, IntList> map) {
			this.tags = map;
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeMap(this.tags, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeIntIdList);
		}

		public static TagNetworkSerialization.NetworkPayload read(FriendlyByteBuf friendlyByteBuf) {
			return new TagNetworkSerialization.NetworkPayload(friendlyByteBuf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readIntIdList));
		}

		public boolean isEmpty() {
			return this.tags.isEmpty();
		}

		public int size() {
			return this.tags.size();
		}

		public <T> TagLoader.LoadResult<T> resolve(Registry<T> registry) {
			return TagNetworkSerialization.deserializeTagsFromNetwork(registry, this);
		}
	}
}
