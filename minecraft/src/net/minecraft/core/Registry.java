package net.minecraft.core;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public interface Registry<T> extends Keyable, HolderLookup.RegistryLookup<T>, IdMap<T> {
	@Override
	ResourceKey<? extends Registry<T>> key();

	default Codec<T> byNameCodec() {
		return this.referenceHolderWithLifecycle().flatComapMap(Holder.Reference::value, object -> this.safeCastToReference(this.wrapAsHolder((T)object)));
	}

	default Codec<Holder<T>> holderByNameCodec() {
		return this.referenceHolderWithLifecycle().flatComapMap(reference -> reference, this::safeCastToReference);
	}

	private Codec<Holder.Reference<T>> referenceHolderWithLifecycle() {
		Codec<Holder.Reference<T>> codec = ResourceLocation.CODEC
			.comapFlatMap(
				resourceLocation -> (DataResult)this.get(resourceLocation)
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + this.key() + ": " + resourceLocation)),
				reference -> reference.key().location()
			);
		return ExtraCodecs.overrideLifecycle(
			codec, reference -> (Lifecycle)this.registrationInfo(reference.key()).map(RegistrationInfo::lifecycle).orElse(Lifecycle.experimental())
		);
	}

	private DataResult<Holder.Reference<T>> safeCastToReference(Holder<T> holder) {
		return holder instanceof Holder.Reference<T> reference
			? DataResult.success(reference)
			: DataResult.error(() -> "Unregistered holder in " + this.key() + ": " + holder);
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
	T getValue(@Nullable ResourceKey<T> resourceKey);

	@Nullable
	T getValue(@Nullable ResourceLocation resourceLocation);

	Optional<RegistrationInfo> registrationInfo(ResourceKey<T> resourceKey);

	default Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
		return Optional.ofNullable(this.getValue(resourceLocation));
	}

	default Optional<T> getOptional(@Nullable ResourceKey<T> resourceKey) {
		return Optional.ofNullable(this.getValue(resourceKey));
	}

	Optional<Holder.Reference<T>> getAny();

	default T getValueOrThrow(ResourceKey<T> resourceKey) {
		T object = this.getValue(resourceKey);
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
		return register(registry, ResourceLocation.parse(string), object);
	}

	static <V, T extends V> T register(Registry<V> registry, ResourceLocation resourceLocation, T object) {
		return register(registry, ResourceKey.create(registry.key(), resourceLocation), object);
	}

	static <V, T extends V> T register(Registry<V> registry, ResourceKey<V> resourceKey, T object) {
		((WritableRegistry)registry).register(resourceKey, (V)object, RegistrationInfo.BUILT_IN);
		return object;
	}

	static <T> Holder.Reference<T> registerForHolder(Registry<T> registry, ResourceKey<T> resourceKey, T object) {
		return ((WritableRegistry)registry).register(resourceKey, object, RegistrationInfo.BUILT_IN);
	}

	static <T> Holder.Reference<T> registerForHolder(Registry<T> registry, ResourceLocation resourceLocation, T object) {
		return registerForHolder(registry, ResourceKey.create(registry.key(), resourceLocation), object);
	}

	Registry<T> freeze();

	Holder.Reference<T> createIntrusiveHolder(T object);

	Optional<Holder.Reference<T>> get(int i);

	Optional<Holder.Reference<T>> get(ResourceLocation resourceLocation);

	Holder<T> wrapAsHolder(T object);

	default Iterable<Holder<T>> getTagOrEmpty(TagKey<T> tagKey) {
		return DataFixUtils.orElse(this.get(tagKey), List.of());
	}

	default Optional<Holder<T>> getRandomElementOf(TagKey<T> tagKey, RandomSource randomSource) {
		return this.get(tagKey).flatMap(named -> named.getRandomElement(randomSource));
	}

	Stream<HolderSet.Named<T>> getTags();

	default IdMap<Holder<T>> asHolderIdMap() {
		return new IdMap<Holder<T>>() {
			public int getId(Holder<T> holder) {
				return Registry.this.getId(holder.value());
			}

			@Nullable
			public Holder<T> byId(int i) {
				return (Holder<T>)Registry.this.get(i).orElse(null);
			}

			@Override
			public int size() {
				return Registry.this.size();
			}

			public Iterator<Holder<T>> iterator() {
				return Registry.this.listElements().map(reference -> reference).iterator();
			}
		};
	}

	Registry.PendingTags<T> prepareTagReload(TagLoader.LoadResult<T> loadResult);

	public interface PendingTags<T> {
		ResourceKey<? extends Registry<? extends T>> key();

		HolderLookup.RegistryLookup<T> lookup();

		void apply();

		int size();
	}
}
