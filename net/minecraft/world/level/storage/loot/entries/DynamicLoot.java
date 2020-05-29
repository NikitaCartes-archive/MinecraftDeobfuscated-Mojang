/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DynamicLoot
extends LootPoolSingletonContainer {
    private final ResourceLocation name;

    private DynamicLoot(ResourceLocation resourceLocation, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
        super(i, j, lootItemConditions, lootItemFunctions);
        this.name = resourceLocation;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.DYNAMIC;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        lootContext.addDynamicDrops(this.name, consumer);
    }

    public static LootPoolSingletonContainer.Builder<?> dynamicEntry(ResourceLocation resourceLocation) {
        return DynamicLoot.simpleBuilder((i, j, lootItemConditions, lootItemFunctions) -> new DynamicLoot(resourceLocation, i, j, lootItemConditions, lootItemFunctions));
    }

    public static class Serializer
    extends LootPoolSingletonContainer.Serializer<DynamicLoot> {
        @Override
        public void serializeCustom(JsonObject jsonObject, DynamicLoot dynamicLoot, JsonSerializationContext jsonSerializationContext) {
            super.serializeCustom(jsonObject, dynamicLoot, jsonSerializationContext);
            jsonObject.addProperty("name", dynamicLoot.name.toString());
        }

        @Override
        protected DynamicLoot deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
            return new DynamicLoot(resourceLocation, i, j, lootItemConditions, lootItemFunctions);
        }

        @Override
        protected /* synthetic */ LootPoolSingletonContainer deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, i, j, lootItemConditions, lootItemFunctions);
        }
    }
}

