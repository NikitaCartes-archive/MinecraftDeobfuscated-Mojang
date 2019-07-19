/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.metadata;

import com.google.gson.JsonObject;

public interface MetadataSectionSerializer<T> {
    public String getMetadataSectionName();

    public T fromJson(JsonObject var1);
}

