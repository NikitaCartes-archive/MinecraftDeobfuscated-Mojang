/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import net.minecraft.world.level.storage.loot.Serializer;

public class SerializerType<T> {
    private final Serializer<? extends T> serializer;

    public SerializerType(Serializer<? extends T> serializer) {
        this.serializer = serializer;
    }

    public Serializer<? extends T> getSerializer() {
        return this.serializer;
    }
}

