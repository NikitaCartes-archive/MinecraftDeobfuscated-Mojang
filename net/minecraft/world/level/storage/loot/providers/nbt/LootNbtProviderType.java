/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.providers.nbt;

import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.SerializerType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;

public class LootNbtProviderType
extends SerializerType<NbtProvider> {
    public LootNbtProviderType(Serializer<? extends NbtProvider> serializer) {
        super(serializer);
    }
}

