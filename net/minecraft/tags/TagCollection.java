/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

    default public NetworkPayload serializeToNetwork(Registry<T> registry) {
        Map<ResourceLocation, Tag<T>> map = this.getAllTags();
        HashMap map2 = Maps.newHashMapWithExpectedSize(map.size());
        map.forEach((resourceLocation, tag) -> {
            List list = tag.getValues();
            IntArrayList intList = new IntArrayList(list.size());
            for (Object object : list) {
                intList.add(registry.getId(object));
            }
            map2.put(resourceLocation, intList);
        });
        return new NetworkPayload(map2);
    }

    @Environment(value=EnvType.CLIENT)
    public static <T> TagCollection<T> createFromNetwork(NetworkPayload networkPayload, Registry<? extends T> registry) {
        HashMap map = Maps.newHashMapWithExpectedSize(networkPayload.tags.size());
        networkPayload.tags.forEach((resourceLocation, intList) -> {
            ImmutableSet.Builder builder = ImmutableSet.builder();
            IntListIterator intListIterator = intList.iterator();
            while (intListIterator.hasNext()) {
                int i = (Integer)intListIterator.next();
                builder.add(registry.byId(i));
            }
            map.put((ResourceLocation)resourceLocation, Tag.fromSet(builder.build()));
        });
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

    public static class NetworkPayload {
        private final Map<ResourceLocation, IntList> tags;

        private NetworkPayload(Map<ResourceLocation, IntList> map) {
            this.tags = map;
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeVarInt(this.tags.size());
            for (Map.Entry<ResourceLocation, IntList> entry : this.tags.entrySet()) {
                friendlyByteBuf.writeResourceLocation(entry.getKey());
                friendlyByteBuf.writeVarInt(entry.getValue().size());
                entry.getValue().forEach(friendlyByteBuf::writeVarInt);
            }
        }

        public static NetworkPayload read(FriendlyByteBuf friendlyByteBuf) {
            HashMap<ResourceLocation, IntList> map = Maps.newHashMap();
            int i = friendlyByteBuf.readVarInt();
            for (int j = 0; j < i; ++j) {
                ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
                int k = friendlyByteBuf.readVarInt();
                IntArrayList intList = new IntArrayList(k);
                for (int l = 0; l < k; ++l) {
                    intList.add(friendlyByteBuf.readVarInt());
                }
                map.put(resourceLocation, intList);
            }
            return new NetworkPayload(map);
        }
    }
}

