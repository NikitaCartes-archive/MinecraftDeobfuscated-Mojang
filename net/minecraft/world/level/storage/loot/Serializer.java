/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

public interface Serializer<T> {
    public void serialize(JsonObject var1, T var2, JsonSerializationContext var3);

    public T deserialize(JsonObject var1, JsonDeserializationContext var2);
}

