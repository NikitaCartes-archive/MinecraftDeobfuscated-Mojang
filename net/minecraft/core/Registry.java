/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public interface Registry<T>
extends Keyable,
IdMap<T> {
    public ResourceKey<? extends Registry<T>> key();

    default public Codec<T> byNameCodec() {
        Codec<Object> codec = ResourceLocation.CODEC.flatXmap(resourceLocation -> Optional.ofNullable(this.get((ResourceLocation)resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + this.key() + ": " + resourceLocation)), object -> this.getResourceKey(object).map(ResourceKey::location).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry element in " + this.key() + ":" + object)));
        Codec<Object> codec2 = ExtraCodecs.idResolverCodec(object -> this.getResourceKey(object).isPresent() ? this.getId(object) : -1, this::byId, -1);
        return ExtraCodecs.overrideLifecycle(ExtraCodecs.orCompressed(codec, codec2), this::lifecycle, this::lifecycle);
    }

    default public Codec<Holder<T>> holderByNameCodec() {
        Codec<Holder> codec = ResourceLocation.CODEC.flatXmap(resourceLocation -> this.getHolder(ResourceKey.create(this.key(), resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + this.key() + ": " + resourceLocation)), holder -> holder.unwrapKey().map(ResourceKey::location).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry element in " + this.key() + ":" + holder)));
        return ExtraCodecs.overrideLifecycle(codec, holder -> this.lifecycle(holder.value()), holder -> this.lifecycle(holder.value()));
    }

    default public <U> Stream<U> keys(DynamicOps<U> dynamicOps) {
        return this.keySet().stream().map(resourceLocation -> dynamicOps.createString(resourceLocation.toString()));
    }

    @Nullable
    public ResourceLocation getKey(T var1);

    public Optional<ResourceKey<T>> getResourceKey(T var1);

    @Override
    public int getId(@Nullable T var1);

    @Nullable
    public T get(@Nullable ResourceKey<T> var1);

    @Nullable
    public T get(@Nullable ResourceLocation var1);

    public Lifecycle lifecycle(T var1);

    public Lifecycle registryLifecycle();

    default public Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
        return Optional.ofNullable(this.get(resourceLocation));
    }

    default public Optional<T> getOptional(@Nullable ResourceKey<T> resourceKey) {
        return Optional.ofNullable(this.get(resourceKey));
    }

    default public T getOrThrow(ResourceKey<T> resourceKey) {
        T object = this.get(resourceKey);
        if (object == null) {
            throw new IllegalStateException("Missing key in " + this.key() + ": " + resourceKey);
        }
        return object;
    }

    public Set<ResourceLocation> keySet();

    public Set<Map.Entry<ResourceKey<T>, T>> entrySet();

    public Set<ResourceKey<T>> registryKeySet();

    public Optional<Holder.Reference<T>> getRandom(RandomSource var1);

    default public Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public boolean containsKey(ResourceLocation var1);

    public boolean containsKey(ResourceKey<T> var1);

    public static <T> T register(Registry<? super T> registry, String string, T object) {
        return Registry.register(registry, new ResourceLocation(string), object);
    }

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation resourceLocation, T object) {
        return Registry.register(registry, ResourceKey.create(registry.key(), resourceLocation), object);
    }

    public static <V, T extends V> T register(Registry<V> registry, ResourceKey<V> resourceKey, T object) {
        ((WritableRegistry)registry).register(resourceKey, object, Lifecycle.stable());
        return object;
    }

    public static <T> Holder.Reference<T> registerForHolder(Registry<T> registry, ResourceKey<T> resourceKey, T object) {
        return ((WritableRegistry)registry).register(resourceKey, object, Lifecycle.stable());
    }

    public static <T> Holder.Reference<T> registerForHolder(Registry<T> registry, ResourceLocation resourceLocation, T object) {
        return Registry.registerForHolder(registry, ResourceKey.create(registry.key(), resourceLocation), object);
    }

    public static <V, T extends V> T registerMapping(Registry<V> registry, int i, String string, T object) {
        ((WritableRegistry)registry).registerMapping(i, ResourceKey.create(registry.key(), new ResourceLocation(string)), object, Lifecycle.stable());
        return object;
    }

    public Registry<T> freeze();

    public Holder.Reference<T> createIntrusiveHolder(T var1);

    public Optional<Holder.Reference<T>> getHolder(int var1);

    public Optional<Holder.Reference<T>> getHolder(ResourceKey<T> var1);

    public Holder<T> wrapAsHolder(T var1);

    default public Holder.Reference<T> getHolderOrThrow(ResourceKey<T> resourceKey) {
        return this.getHolder(resourceKey).orElseThrow(() -> new IllegalStateException("Missing key in " + this.key() + ": " + resourceKey));
    }

    public Stream<Holder.Reference<T>> holders();

    public Optional<HolderSet.Named<T>> getTag(TagKey<T> var1);

    default public Iterable<Holder<T>> getTagOrEmpty(TagKey<T> tagKey) {
        return DataFixUtils.orElse(this.getTag(tagKey), List.of());
    }

    public HolderSet.Named<T> getOrCreateTag(TagKey<T> var1);

    public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags();

    public Stream<TagKey<T>> getTagNames();

    public void resetTags();

    public void bindTags(Map<TagKey<T>, List<Holder<T>>> var1);

    default public IdMap<Holder<T>> asHolderIdMap() {
        return new IdMap<Holder<T>>(){

            @Override
            public int getId(Holder<T> holder) {
                return Registry.this.getId(holder.value());
            }

            @Override
            @Nullable
            public Holder<T> byId(int i) {
                return Registry.this.getHolder(i).orElse(null);
            }

            @Override
            public int size() {
                return Registry.this.size();
            }

            @Override
            public Iterator<Holder<T>> iterator() {
                return Registry.this.holders().map(reference -> reference).iterator();
            }

            @Override
            @Nullable
            public /* synthetic */ Object byId(int i) {
                return this.byId(i);
            }
        };
    }

    public HolderOwner<T> holderOwner();

    public HolderLookup.RegistryLookup<T> asLookup();

    default public HolderLookup.RegistryLookup<T> asTagAddingLookup() {
        return new HolderLookup.RegistryLookup.Delegate<T>(){

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

