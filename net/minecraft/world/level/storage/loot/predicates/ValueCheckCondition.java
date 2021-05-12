/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class ValueCheckCondition
implements LootItemCondition {
    final NumberProvider provider;
    final IntRange range;

    ValueCheckCondition(NumberProvider numberProvider, IntRange intRange) {
        this.provider = numberProvider;
        this.range = intRange;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.VALUE_CHECK;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.provider.getReferencedContextParams(), this.range.getReferencedContextParams());
    }

    @Override
    public boolean test(LootContext lootContext) {
        return this.range.test(lootContext, this.provider.getInt(lootContext));
    }

    public static LootItemCondition.Builder hasValue(NumberProvider numberProvider, IntRange intRange) {
        return () -> new ValueCheckCondition(numberProvider, intRange);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<ValueCheckCondition> {
        @Override
        public void serialize(JsonObject jsonObject, ValueCheckCondition valueCheckCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("value", jsonSerializationContext.serialize(valueCheckCondition.provider));
            jsonObject.add("range", jsonSerializationContext.serialize(valueCheckCondition.range));
        }

        @Override
        public ValueCheckCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "value", jsonDeserializationContext, NumberProvider.class);
            IntRange intRange = GsonHelper.getAsObject(jsonObject, "range", jsonDeserializationContext, IntRange.class);
            return new ValueCheckCondition(numberProvider, intRange);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

