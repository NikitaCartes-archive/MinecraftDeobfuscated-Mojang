/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;
import net.minecraft.world.level.storage.loot.RandomIntGenerators;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetItemCountFunction
extends LootItemConditionalFunction {
    private final RandomIntGenerator value;

    private SetItemCountFunction(LootItemCondition[] lootItemConditions, RandomIntGenerator randomIntGenerator) {
        super(lootItemConditions);
        this.value = randomIntGenerator;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_COUNT;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.setCount(this.value.getInt(lootContext.getRandom()));
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setCount(RandomIntGenerator randomIntGenerator) {
        return SetItemCountFunction.simpleBuilder(lootItemConditions -> new SetItemCountFunction((LootItemCondition[])lootItemConditions, randomIntGenerator));
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetItemCountFunction> {
        @Override
        public void serialize(JsonObject jsonObject, SetItemCountFunction setItemCountFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setItemCountFunction, jsonSerializationContext);
            jsonObject.add("count", RandomIntGenerators.serialize(setItemCountFunction.value, jsonSerializationContext));
        }

        @Override
        public SetItemCountFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            RandomIntGenerator randomIntGenerator = RandomIntGenerators.deserialize(jsonObject.get("count"), jsonDeserializationContext);
            return new SetItemCountFunction(lootItemConditions, randomIntGenerator);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }
}

