/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.metadata;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public interface MetadataSectionType<T>
extends MetadataSectionSerializer<T> {
    public JsonObject toJson(T var1);

    public static <T> MetadataSectionType<T> fromCodec(final String string, final Codec<T> codec) {
        return new MetadataSectionType<T>(){

            @Override
            public String getMetadataSectionName() {
                return string;
            }

            @Override
            public T fromJson(JsonObject jsonObject) {
                return codec.parse(JsonOps.INSTANCE, jsonObject).getOrThrow(false, string -> {});
            }

            @Override
            public JsonObject toJson(T object) {
                return codec.encodeStart(JsonOps.INSTANCE, object).getOrThrow(false, string -> {}).getAsJsonObject();
            }
        };
    }
}

