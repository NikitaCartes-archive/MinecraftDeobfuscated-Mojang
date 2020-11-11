/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.providers.number;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;

public interface NumberProvider
extends LootContextUser {
    public float getFloat(LootContext var1);

    default public int getInt(LootContext lootContext) {
        return Math.round(this.getFloat(lootContext));
    }

    public LootNumberProviderType getType();
}

