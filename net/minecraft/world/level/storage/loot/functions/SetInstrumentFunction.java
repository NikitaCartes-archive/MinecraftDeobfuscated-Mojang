/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetInstrumentFunction
extends LootItemConditionalFunction {
    final TagKey<Instrument> options;

    SetInstrumentFunction(LootItemCondition[] lootItemConditions, TagKey<Instrument> tagKey) {
        super(lootItemConditions);
        this.options = tagKey;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_INSTRUMENT;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        InstrumentItem.setRandom(itemStack, this.options, lootContext.getRandom());
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setInstrumentOptions(TagKey<Instrument> tagKey) {
        return SetInstrumentFunction.simpleBuilder(lootItemConditions -> new SetInstrumentFunction((LootItemCondition[])lootItemConditions, tagKey));
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetInstrumentFunction> {
        @Override
        public void serialize(JsonObject jsonObject, SetInstrumentFunction setInstrumentFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setInstrumentFunction, jsonSerializationContext);
            jsonObject.addProperty("options", "#" + setInstrumentFunction.options.location());
        }

        @Override
        public SetInstrumentFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            String string = GsonHelper.getAsString(jsonObject, "options");
            if (!string.startsWith("#")) {
                throw new JsonSyntaxException("Inline tag value not supported: " + string);
            }
            return new SetInstrumentFunction(lootItemConditions, TagKey.create(Registries.INSTRUMENT, new ResourceLocation(string.substring(1))));
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }
}

