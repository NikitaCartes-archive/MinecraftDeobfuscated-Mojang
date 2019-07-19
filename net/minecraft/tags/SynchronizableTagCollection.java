/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;

public class SynchronizableTagCollection<T>
extends TagCollection<T> {
    private final Registry<T> registry;

    public SynchronizableTagCollection(Registry<T> registry, String string, String string2) {
        super(registry::getOptional, string, false, string2);
        this.registry = registry;
    }

    public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        Map map = this.getAllTags();
        friendlyByteBuf.writeVarInt(map.size());
        for (Map.Entry entry : map.entrySet()) {
            friendlyByteBuf.writeResourceLocation(entry.getKey());
            friendlyByteBuf.writeVarInt(entry.getValue().getValues().size());
            for (Object object : entry.getValue().getValues()) {
                friendlyByteBuf.writeVarInt(this.registry.getId(object));
            }
        }
    }

    public void loadFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        HashMap map = Maps.newHashMap();
        int i = friendlyByteBuf.readVarInt();
        for (int j = 0; j < i; ++j) {
            ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
            int k = friendlyByteBuf.readVarInt();
            Tag.Builder builder = Tag.Builder.tag();
            for (int l = 0; l < k; ++l) {
                builder.add(this.registry.byId(friendlyByteBuf.readVarInt()));
            }
            map.put(resourceLocation, builder.build(resourceLocation));
        }
        this.replace(map);
    }
}

