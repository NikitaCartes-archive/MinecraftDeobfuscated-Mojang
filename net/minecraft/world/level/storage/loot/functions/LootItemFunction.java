/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public interface LootItemFunction
extends LootContextUser,
BiFunction<ItemStack, LootContext, ItemStack> {
    public LootItemFunctionType getType();

    public static Consumer<ItemStack> decorate(BiFunction<ItemStack, LootContext, ItemStack> biFunction, Consumer<ItemStack> consumer, LootContext lootContext) {
        return itemStack -> consumer.accept((ItemStack)biFunction.apply((ItemStack)itemStack, lootContext));
    }

    public static interface Builder {
        public LootItemFunction build();
    }
}

