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
import net.minecraft.core.HolderSet;
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
			.filter(pair -> ((TagNetworkSerialization.NetworkPayload)pair.getSecond()).size() > 0)
			.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	private static <T> TagNetworkSerialization.NetworkPayload serializeToNetwork(Registry<T> registry) {
		Map<ResourceLocation, IntList> map = new HashMap();
		registry.getTags().forEach(pair -> {
			HolderSet<T> holderSet = (HolderSet<T>)pair.getSecond();
			IntList intList = new IntArrayList(holderSet.size());

			for (Holder<T> holder : holderSet) {
				if (holder.kind() != Holder.Kind.REFERENCE) {
					throw new IllegalStateException("Can't serialize unregistered value " + holder);
				}

				intList.add(registry.getId(holder.value()));
			}

			map.put(((TagKey)pair.getFirst()).location(), intList);
		});
		return new TagNetworkSerialization.NetworkPayload(map);
	}

	static <T> void deserializeTagsFromNetwork(
		ResourceKey<? extends Registry<T>> resourceKey,
		Registry<T> registry,
		TagNetworkSerialization.NetworkPayload networkPayload,
		TagNetworkSerialization.TagOutput<T> tagOutput
	) {
		networkPayload.tags.forEach((resourceLocation, intList) -> {
			TagKey<T> tagKey = TagKey.create(resourceKey, resourceLocation);
			List<Holder<T>> list = (List<Holder<T>>)intList.intStream().mapToObj(registry::getHolder).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
			tagOutput.accept(tagKey, list);
		});
	}

	public static final class NetworkPayload {
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

		public int size() {
			return this.tags.size();
		}

		public <T> void applyToRegistry(Registry<T> registry) {
			if (this.size() != 0) {
				Map<TagKey<T>, List<Holder<T>>> map = new HashMap(this.size());
				TagNetworkSerialization.deserializeTagsFromNetwork(registry.key(), registry, this, map::put);
				registry.bindTags(map);
			}
		}
	}

	@FunctionalInterface
	public interface TagOutput<T> {
		void accept(TagKey<T> tagKey, List<Holder<T>> list);
	}
}
