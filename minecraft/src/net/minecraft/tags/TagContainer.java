package net.minecraft.tags;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagContainer {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final TagContainer EMPTY = new TagContainer(ImmutableMap.of());
	private final Map<ResourceKey<? extends Registry<?>>, TagCollection<?>> collections;

	private TagContainer(Map<ResourceKey<? extends Registry<?>>, TagCollection<?>> map) {
		this.collections = map;
	}

	@Nullable
	private <T> TagCollection<T> get(ResourceKey<? extends Registry<T>> resourceKey) {
		return (TagCollection<T>)this.collections.get(resourceKey);
	}

	public <T> TagCollection<T> getOrEmpty(ResourceKey<? extends Registry<T>> resourceKey) {
		return (TagCollection<T>)this.collections.getOrDefault(resourceKey, TagCollection.empty());
	}

	public <T, E extends Exception> Tag<T> getTagOrThrow(
		ResourceKey<? extends Registry<T>> resourceKey, ResourceLocation resourceLocation, Function<ResourceLocation, E> function
	) throws E {
		TagCollection<T> tagCollection = this.get(resourceKey);
		if (tagCollection == null) {
			throw (Exception)function.apply(resourceLocation);
		} else {
			Tag<T> tag = tagCollection.getTag(resourceLocation);
			if (tag == null) {
				throw (Exception)function.apply(resourceLocation);
			} else {
				return tag;
			}
		}
	}

	public <T, E extends Exception> ResourceLocation getIdOrThrow(ResourceKey<? extends Registry<T>> resourceKey, Tag<T> tag, Supplier<E> supplier) throws E {
		TagCollection<T> tagCollection = this.get(resourceKey);
		if (tagCollection == null) {
			throw (Exception)supplier.get();
		} else {
			ResourceLocation resourceLocation = tagCollection.getId(tag);
			if (resourceLocation == null) {
				throw (Exception)supplier.get();
			} else {
				return resourceLocation;
			}
		}
	}

	public void getAll(TagContainer.CollectionConsumer collectionConsumer) {
		this.collections.forEach((resourceKey, tagCollection) -> acceptCap(collectionConsumer, resourceKey, tagCollection));
	}

	private static <T> void acceptCap(
		TagContainer.CollectionConsumer collectionConsumer, ResourceKey<? extends Registry<?>> resourceKey, TagCollection<?> tagCollection
	) {
		collectionConsumer.accept(resourceKey, tagCollection);
	}

	public void bindToGlobal() {
		StaticTags.resetAll(this);
		Blocks.rebuildCache();
	}

	public Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> serializeToNetwork(RegistryAccess registryAccess) {
		final Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> map = Maps.<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload>newHashMap();
		this.getAll(new TagContainer.CollectionConsumer() {
			@Override
			public <T> void accept(ResourceKey<? extends Registry<T>> resourceKey, TagCollection<T> tagCollection) {
				Optional<? extends Registry<T>> optional = registryAccess.registry(resourceKey);
				if (optional.isPresent()) {
					map.put(resourceKey, tagCollection.serializeToNetwork((Registry<T>)optional.get()));
				} else {
					TagContainer.LOGGER.error("Unknown registry {}", resourceKey);
				}
			}
		});
		return map;
	}

	public static TagContainer deserializeFromNetwork(RegistryAccess registryAccess, Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> map) {
		TagContainer.Builder builder = new TagContainer.Builder();
		map.forEach((resourceKey, networkPayload) -> addTagsFromPayload(registryAccess, builder, resourceKey, networkPayload));
		return builder.build();
	}

	private static <T> void addTagsFromPayload(
		RegistryAccess registryAccess,
		TagContainer.Builder builder,
		ResourceKey<? extends Registry<? extends T>> resourceKey,
		TagCollection.NetworkPayload networkPayload
	) {
		Optional<? extends Registry<? extends T>> optional = registryAccess.registry(resourceKey);
		if (optional.isPresent()) {
			builder.add(resourceKey, TagCollection.createFromNetwork(networkPayload, (Registry<? extends T>)optional.get()));
		} else {
			LOGGER.error("Unknown registry {}", resourceKey);
		}
	}

	public static class Builder {
		private final ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, TagCollection<?>> result = ImmutableMap.builder();

		public <T> TagContainer.Builder add(ResourceKey<? extends Registry<? extends T>> resourceKey, TagCollection<T> tagCollection) {
			this.result.put(resourceKey, tagCollection);
			return this;
		}

		public TagContainer build() {
			return new TagContainer(this.result.build());
		}
	}

	@FunctionalInterface
	interface CollectionConsumer {
		<T> void accept(ResourceKey<? extends Registry<T>> resourceKey, TagCollection<T> tagCollection);
	}
}
