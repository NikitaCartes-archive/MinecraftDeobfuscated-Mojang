/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.tags.TagKey;

public class TagNetworkSerialization {
    public static Map<ResourceKey<? extends Registry<?>>, NetworkPayload> serializeTagsToNetwork(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
        return RegistrySynchronization.networkSafeRegistries(layeredRegistryAccess).map(registryEntry -> Pair.of(registryEntry.key(), TagNetworkSerialization.serializeToNetwork(registryEntry.value()))).filter(pair -> !((NetworkPayload)pair.getSecond()).isEmpty()).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private static <T> NetworkPayload serializeToNetwork(Registry<T> registry) {
        HashMap<ResourceLocation, IntList> map = new HashMap<ResourceLocation, IntList>();
        registry.getTags().forEach(pair -> {
            HolderSet holderSet = (HolderSet)pair.getSecond();
            IntArrayList intList = new IntArrayList(holderSet.size());
            for (Holder holder : holderSet) {
                if (holder.kind() != Holder.Kind.REFERENCE) {
                    throw new IllegalStateException("Can't serialize unregistered value " + holder);
                }
                intList.add(registry.getId(holder.value()));
            }
            map.put(((TagKey)pair.getFirst()).location(), intList);
        });
        return new NetworkPayload(map);
    }

    public static <T> void deserializeTagsFromNetwork(ResourceKey<? extends Registry<T>> resourceKey, Registry<T> registry, NetworkPayload networkPayload, TagOutput<T> tagOutput) {
        networkPayload.tags.forEach((resourceLocation, intList) -> {
            TagKey tagKey = TagKey.create(resourceKey, resourceLocation);
            List list = intList.intStream().mapToObj(registry::getHolder).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
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

        public static NetworkPayload read(FriendlyByteBuf friendlyByteBuf) {
            return new NetworkPayload(friendlyByteBuf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readIntIdList));
        }

        public boolean isEmpty() {
            return this.tags.isEmpty();
        }
    }

    @FunctionalInterface
    public static interface TagOutput<T> {
        public void accept(TagKey<T> var1, List<Holder<T>> var2);
    }
}

