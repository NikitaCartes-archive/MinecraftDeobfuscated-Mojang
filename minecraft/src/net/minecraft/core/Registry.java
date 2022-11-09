package net.minecraft.core;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public interface Registry<T> extends Keyable, IdMap<T> {
	ResourceKey<? extends Registry<T>> key();

	default Codec<T> byNameCodec() {
		Codec<T> codec = ResourceLocation.CODEC
			.flatXmap(
				resourceLocation -> (DataResult)Optional.ofNullable(this.get(resourceLocation))
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error("Unknown registry key in " + this.key() + ": " + resourceLocation)),
				object -> (DataResult)this.getResourceKey((T)object)
						.map(ResourceKey::location)
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error("Unknown registry element in " + this.key() + ":" + object))
			);
		Codec<T> codec2 = ExtraCodecs.idResolverCodec(object -> this.getResourceKey((T)object).isPresent() ? this.getId((T)object) : -1, this::byId, -1);
		return ExtraCodecs.overrideLifecycle(ExtraCodecs.orCompressed(codec, codec2), this::lifecycle, this::lifecycle);
	}

	default Codec<Holder<T>> holderByNameCodec() {
		Codec<Holder<T>> codec = ResourceLocation.CODEC
			.flatXmap(
				resourceLocation -> (DataResult)this.getHolder(ResourceKey.create(this.key(), resourceLocation))
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error("Unknown registry key in " + this.key() + ": " + resourceLocation)),
				holder -> (DataResult)holder.unwrapKey()
						.map(ResourceKey::location)
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error("Unknown registry element in " + this.key() + ":" + holder))
			);
		return ExtraCodecs.overrideLifecycle(codec, holder -> this.lifecycle((T)holder.value()), holder -> this.lifecycle((T)holder.value()));
	}

	@Override
	default <U> Stream<U> keys(DynamicOps<U> dynamicOps) {
		return this.keySet().stream().map(resourceLocation -> dynamicOps.createString(resourceLocation.toString()));
	}

	@Nullable
	ResourceLocation getKey(T object);

	Optional<ResourceKey<T>> getResourceKey(T object);

	@Override
	int getId(@Nullable T object);

	@Nullable
	T get(@Nullable ResourceKey<T> resourceKey);

	@Nullable
	T get(@Nullable ResourceLocation resourceLocation);

	Lifecycle lifecycle(T object);

	Lifecycle registryLifecycle();

	default Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
		return Optional.ofNullable(this.get(resourceLocation));
	}

	default Optional<T> getOptional(@Nullable ResourceKey<T> resourceKey) {
		return Optional.ofNullable(this.get(resourceKey));
	}

	default T getOrThrow(ResourceKey<T> resourceKey) {
		T object = this.get(resourceKey);
		if (object == null) {
			throw new IllegalStateException("Missing key in " + this.key() + ": " + resourceKey);
		} else {
			return object;
		}
	}

	Set<ResourceLocation> keySet();

	Set<Entry<ResourceKey<T>, T>> entrySet();

	Set<ResourceKey<T>> registryKeySet();

	Optional<Holder.Reference<T>> getRandom(RandomSource randomSource);

	default Stream<T> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}

	boolean containsKey(ResourceLocation resourceLocation);

	boolean containsKey(ResourceKey<T> resourceKey);

	static <T> T register(Registry<? super T> registry, String string, T object) {
		return register(registry, new ResourceLocation(string), object);
	}

	static <V, T extends V> T register(Registry<V> registry, ResourceLocation resourceLocation, T object) {
		return register(registry, ResourceKey.create(registry.key(), resourceLocation), object);
	}

	static <V, T extends V> T register(Registry<V> registry, ResourceKey<V> resourceKey, T object) {
		((WritableRegistry)registry).register(resourceKey, (V)object, Lifecycle.stable());
		return object;
	}

	static <V, T extends V> T registerMapping(Registry<V> registry, int i, String string, T object) {
		((WritableRegistry)registry).registerMapping(i, ResourceKey.create(registry.key(), new ResourceLocation(string)), (V)object, Lifecycle.stable());
		return object;
	}

	Registry<T> freeze();

	Holder.Reference<T> createIntrusiveHolder(T object);

	Optional<Holder.Reference<T>> getHolder(int i);

	Optional<Holder.Reference<T>> getHolder(ResourceKey<T> resourceKey);

	default Holder.Reference<T> getHolderOrThrow(ResourceKey<T> resourceKey) {
		return (Holder.Reference<T>)this.getHolder(resourceKey).orElseThrow(() -> new IllegalStateException("Missing key in " + this.key() + ": " + resourceKey));
	}

	Stream<Holder.Reference<T>> holders();

	Optional<HolderSet.Named<T>> getTag(TagKey<T> tagKey);

	default Iterable<Holder<T>> getTagOrEmpty(TagKey<T> tagKey) {
		return DataFixUtils.orElse(this.getTag(tagKey), List.of());
	}

	HolderSet.Named<T> getOrCreateTag(TagKey<T> tagKey);

	Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags();

	Stream<TagKey<T>> getTagNames();

	void resetTags();

	void bindTags(Map<TagKey<T>, List<Holder<T>>> map);

	default IdMap<Holder<T>> asHolderIdMap() {
		return new IdMap<Holder<T>>() {
			public int getId(Holder<T> holder) {
				return Registry.this.getId(holder.value());
			}

			@Nullable
			public Holder<T> byId(int i) {
				return (Holder<T>)Registry.this.getHolder(i).orElse(null);
			}

			@Override
			public int size() {
				return Registry.this.size();
			}

			public Iterator<Holder<T>> iterator() {
				return Registry.this.holders().map(reference -> reference).iterator();
			}
		};
	}

	HolderOwner<T> holderOwner();

	HolderLookup.RegistryLookup<T> asLookup();

	default HolderLookup.RegistryLookup<T> asTagAddingLookup() {
		return new HolderLookup.RegistryLookup.Delegate<T>() {
			@Override
			protected HolderLookup.RegistryLookup<T> parent() {
				return Registry.this.asLookup();
			}

			@Override
			public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
				return Optional.of(this.getOrThrow(tagKey));
			}

			@Override
			public HolderSet.Named<T> getOrThrow(TagKey<T> tagKey) {
				return Registry.this.getOrCreateTag(tagKey);
			}
		};
	}
}
