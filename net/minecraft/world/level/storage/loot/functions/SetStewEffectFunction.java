/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetStewEffectFunction
extends LootItemConditionalFunction {
    private final Map<MobEffect, RandomValueBounds> effectDurationMap;

    private SetStewEffectFunction(LootItemCondition[] lootItemConditions, Map<MobEffect, RandomValueBounds> map) {
        super(lootItemConditions);
        this.effectDurationMap = ImmutableMap.copyOf(map);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_STEW_EFFECT;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.getItem() != Items.SUSPICIOUS_STEW || this.effectDurationMap.isEmpty()) {
            return itemStack;
        }
        Random random = lootContext.getRandom();
        int i = random.nextInt(this.effectDurationMap.size());
        Map.Entry<MobEffect, RandomValueBounds> entry = Iterables.get(this.effectDurationMap.entrySet(), i);
        MobEffect mobEffect = entry.getKey();
        int j = entry.getValue().getInt(random);
        if (!mobEffect.isInstantenous()) {
            j *= 20;
        }
        SuspiciousStewItem.saveMobEffect(itemStack, mobEffect, j);
        return itemStack;
    }

    public static Builder stewEffect() {
        return new Builder();
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetStewEffectFunction> {
        @Override
        public void serialize(JsonObject jsonObject, SetStewEffectFunction setStewEffectFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setStewEffectFunction, jsonSerializationContext);
            if (!setStewEffectFunction.effectDurationMap.isEmpty()) {
                JsonArray jsonArray = new JsonArray();
                for (MobEffect mobEffect : setStewEffectFunction.effectDurationMap.keySet()) {
                    JsonObject jsonObject2 = new JsonObject();
                    ResourceLocation resourceLocation = Registry.MOB_EFFECT.getKey(mobEffect);
                    if (resourceLocation == null) {
                        throw new IllegalArgumentException("Don't know how to serialize mob effect " + mobEffect);
                    }
                    jsonObject2.add("type", new JsonPrimitive(resourceLocation.toString()));
                    jsonObject2.add("duration", jsonSerializationContext.serialize(setStewEffectFunction.effectDurationMap.get(mobEffect)));
                    jsonArray.add(jsonObject2);
                }
                jsonObject.add("effects", jsonArray);
            }
        }

        @Override
        public SetStewEffectFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            HashMap<MobEffect, RandomValueBounds> map = Maps.newHashMap();
            if (jsonObject.has("effects")) {
                JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "effects");
                for (JsonElement jsonElement : jsonArray) {
                    String string = GsonHelper.getAsString(jsonElement.getAsJsonObject(), "type");
                    MobEffect mobEffect = Registry.MOB_EFFECT.getOptional(new ResourceLocation(string)).orElseThrow(() -> new JsonSyntaxException("Unknown mob effect '" + string + "'"));
                    RandomValueBounds randomValueBounds = GsonHelper.getAsObject(jsonElement.getAsJsonObject(), "duration", jsonDeserializationContext, RandomValueBounds.class);
                    map.put(mobEffect, randomValueBounds);
                }
            }
            return new SetStewEffectFunction(lootItemConditions, map);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final Map<MobEffect, RandomValueBounds> effectDurationMap = Maps.newHashMap();

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder withEffect(MobEffect mobEffect, RandomValueBounds randomValueBounds) {
            this.effectDurationMap.put(mobEffect, randomValueBounds);
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetStewEffectFunction(this.getConditions(), this.effectDurationMap);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

