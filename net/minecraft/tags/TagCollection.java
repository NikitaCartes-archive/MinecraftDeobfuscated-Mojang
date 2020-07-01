/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SetTag;
import net.minecraft.tags.Tag;
import org.jetbrains.annotations.Nullable;

public interface TagCollection<T> {
    public Map<ResourceLocation, Tag<T>> getAllTags();

    @Nullable
    default public Tag<T> getTag(ResourceLocation resourceLocation) {
        return this.getAllTags().get(resourceLocation);
    }

    public Tag<T> getTagOrEmpty(ResourceLocation var1);

    @Nullable
    public ResourceLocation getId(Tag<T> var1);

    default public ResourceLocation getIdOrThrow(Tag<T> tag) {
        ResourceLocation resourceLocation = this.getId(tag);
        if (resourceLocation == null) {
            throw new IllegalStateException("Unrecognized tag");
        }
        return resourceLocation;
    }

    default public Collection<ResourceLocation> getAvailableTags() {
        return this.getAllTags().keySet();
    }

    @Environment(value=EnvType.CLIENT)
    default public Collection<ResourceLocation> getMatchingTags(T object) {
        ArrayList<ResourceLocation> list = Lists.newArrayList();
        for (Map.Entry<ResourceLocation, Tag<T>> entry : this.getAllTags().entrySet()) {
            if (!entry.getValue().contains(object)) continue;
            list.add(entry.getKey());
        }
        return list;
    }

    default public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf, DefaultedRegistry<T> defaultedRegistry) {
        Map<ResourceLocation, Tag<T>> map = this.getAllTags();
        friendlyByteBuf.writeVarInt(map.size());
        for (Map.Entry<ResourceLocation, Tag<T>> entry : map.entrySet()) {
            friendlyByteBuf.writeResourceLocation(entry.getKey());
            friendlyByteBuf.writeVarInt(entry.getValue().getValues().size());
            for (T object : entry.getValue().getValues()) {
                friendlyByteBuf.writeVarInt(defaultedRegistry.getId(object));
            }
        }
    }

    public static <T> TagCollection<T> loadFromNetwork(FriendlyByteBuf friendlyByteBuf, Registry<T> registry) {
        HashMap<ResourceLocation, Tag<T>> map = Maps.newHashMap();
        int i = friendlyByteBuf.readVarInt();
        for (int j = 0; j < i; ++j) {
            ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
            int k = friendlyByteBuf.readVarInt();
            ImmutableSet.Builder builder = ImmutableSet.builder();
            for (int l = 0; l < k; ++l) {
                builder.add(registry.byId(friendlyByteBuf.readVarInt()));
            }
            map.put(resourceLocation, Tag.fromSet(builder.build()));
        }
        return TagCollection.of(map);
    }

    public static <T> TagCollection<T> empty() {
        return TagCollection.of(ImmutableBiMap.of());
    }

    public static <T> TagCollection<T> of(Map<ResourceLocation, Tag<T>> map) {
        final ImmutableBiMap<ResourceLocation, Tag<T>> biMap = ImmutableBiMap.copyOf(map);
        return new TagCollection<T>(){
            private final Tag<T> empty = SetTag.empty();

            @Override
            public Tag<T> getTagOrEmpty(ResourceLocation resourceLocation) {
                return biMap.getOrDefault(resourceLocation, this.empty);
            }

            @Override
            @Nullable
            public ResourceLocation getId(Tag<T> tag) {
                if (tag instanceof Tag.Named) {
                    return ((Tag.Named)tag).getName();
                }
                return (ResourceLocation)biMap.inverse().get(tag);
            }

            @Override
            public Map<ResourceLocation, Tag<T>> getAllTags() {
                return biMap;
            }
        };
    }
}

