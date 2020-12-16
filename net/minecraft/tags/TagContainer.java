/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StaticTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class TagContainer {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final TagContainer EMPTY = new TagContainer(ImmutableMap.of());
    private final Map<ResourceKey<? extends Registry<?>>, TagCollection<?>> collections;

    private TagContainer(Map<ResourceKey<? extends Registry<?>>, TagCollection<?>> map) {
        this.collections = map;
    }

    @Nullable
    private <T> TagCollection<T> get(ResourceKey<? extends Registry<T>> resourceKey) {
        return this.collections.get(resourceKey);
    }

    public <T> TagCollection<T> getOrEmpty(ResourceKey<? extends Registry<T>> resourceKey) {
        return this.collections.getOrDefault(resourceKey, TagCollection.empty());
    }

    public <T, E extends Exception> Tag<T> getTagOrThrow(ResourceKey<? extends Registry<T>> resourceKey, ResourceLocation resourceLocation, Function<ResourceLocation, E> function) throws E {
        TagCollection<T> tagCollection = this.get(resourceKey);
        if (tagCollection == null) {
            throw (Exception)function.apply(resourceLocation);
        }
        Tag<T> tag = tagCollection.getTag(resourceLocation);
        if (tag == null) {
            throw (Exception)function.apply(resourceLocation);
        }
        return tag;
    }

    public <T, E extends Exception> ResourceLocation getIdOrThrow(ResourceKey<? extends Registry<T>> resourceKey, Tag<T> tag, Supplier<E> supplier) throws E {
        TagCollection<T> tagCollection = this.get(resourceKey);
        if (tagCollection == null) {
            throw (Exception)supplier.get();
        }
        ResourceLocation resourceLocation = tagCollection.getId(tag);
        if (resourceLocation == null) {
            throw (Exception)supplier.get();
        }
        return resourceLocation;
    }

    public void getAll(CollectionConsumer collectionConsumer) {
        this.collections.forEach((resourceKey, tagCollection) -> TagContainer.acceptCap(collectionConsumer, resourceKey, tagCollection));
    }

    private static <T> void acceptCap(CollectionConsumer collectionConsumer, ResourceKey<? extends Registry<?>> resourceKey, TagCollection<?> tagCollection) {
        collectionConsumer.accept(resourceKey, tagCollection);
    }

    public void bindToGlobal() {
        StaticTags.resetAll(this);
        Blocks.rebuildCache();
    }

    public Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> serializeToNetwork(final RegistryAccess registryAccess) {
        final HashMap<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> map = Maps.newHashMap();
        this.getAll(new CollectionConsumer(){

            @Override
            public <T> void accept(ResourceKey<? extends Registry<T>> resourceKey, TagCollection<T> tagCollection) {
                Optional optional = registryAccess.registry(resourceKey);
                if (optional.isPresent()) {
                    map.put(resourceKey, tagCollection.serializeToNetwork(optional.get()));
                } else {
                    LOGGER.error("Unknown registry {}", (Object)resourceKey);
                }
            }
        });
        return map;
    }

    @Environment(value=EnvType.CLIENT)
    public static TagContainer deserializeFromNetwork(RegistryAccess registryAccess, Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> map) {
        Builder builder = new Builder();
        map.forEach((resourceKey, networkPayload) -> TagContainer.addTagsFromPayload(registryAccess, builder, resourceKey, networkPayload));
        return builder.build();
    }

    @Environment(value=EnvType.CLIENT)
    private static <T> void addTagsFromPayload(RegistryAccess registryAccess, Builder builder, ResourceKey<? extends Registry<? extends T>> resourceKey, TagCollection.NetworkPayload networkPayload) {
        Optional optional = registryAccess.registry(resourceKey);
        if (optional.isPresent()) {
            builder.add(resourceKey, TagCollection.createFromNetwork(networkPayload, optional.get()));
        } else {
            LOGGER.error("Unknown registry {}", (Object)resourceKey);
        }
    }

    public static class Builder {
        private final ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, TagCollection<?>> result = ImmutableMap.builder();

        public <T> Builder add(ResourceKey<? extends Registry<? extends T>> resourceKey, TagCollection<T> tagCollection) {
            this.result.put(resourceKey, tagCollection);
            return this;
        }

        public TagContainer build() {
            return new TagContainer(this.result.build());
        }
    }

    @FunctionalInterface
    static interface CollectionConsumer {
        public <T> void accept(ResourceKey<? extends Registry<T>> var1, TagCollection<T> var2);
    }
}

