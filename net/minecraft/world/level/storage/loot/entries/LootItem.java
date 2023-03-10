/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItem
extends LootPoolSingletonContainer {
    final Item item;

    LootItem(Item item, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
        super(i, j, lootItemConditions, lootItemFunctions);
        this.item = item;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.ITEM;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        consumer.accept(new ItemStack(this.item));
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike itemLike) {
        return LootItem.simpleBuilder((i, j, lootItemConditions, lootItemFunctions) -> new LootItem(itemLike.asItem(), i, j, lootItemConditions, lootItemFunctions));
    }

    public static class Serializer
    extends LootPoolSingletonContainer.Serializer<LootItem> {
        @Override
        public void serializeCustom(JsonObject jsonObject, LootItem lootItem, JsonSerializationContext jsonSerializationContext) {
            super.serializeCustom(jsonObject, lootItem, jsonSerializationContext);
            ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(lootItem.item);
            if (resourceLocation == null) {
                throw new IllegalArgumentException("Can't serialize unknown item " + lootItem.item);
            }
            jsonObject.addProperty("name", resourceLocation.toString());
        }

        @Override
        protected LootItem deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
            Item item = GsonHelper.getAsItem(jsonObject, "name");
            return new LootItem(item, i, j, lootItemConditions, lootItemFunctions);
        }

        @Override
        protected /* synthetic */ LootPoolSingletonContainer deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, i, j, lootItemConditions, lootItemFunctions);
        }
    }
}

