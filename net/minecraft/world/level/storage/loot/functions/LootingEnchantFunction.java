/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class LootingEnchantFunction
extends LootItemConditionalFunction {
    public static final int NO_LIMIT = 0;
    final NumberProvider value;
    final int limit;

    LootingEnchantFunction(LootItemCondition[] lootItemConditions, NumberProvider numberProvider, int i) {
        super(lootItemConditions);
        this.value = numberProvider;
        this.limit = i;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.LOOTING_ENCHANT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(ImmutableSet.of(LootContextParams.KILLER_ENTITY), this.value.getReferencedContextParams());
    }

    boolean hasLimit() {
        return this.limit > 0;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
        if (entity instanceof LivingEntity) {
            int i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
            if (i == 0) {
                return itemStack;
            }
            float f = (float)i * this.value.getFloat(lootContext);
            itemStack.grow(Math.round(f));
            if (this.hasLimit() && itemStack.getCount() > this.limit) {
                itemStack.setCount(this.limit);
            }
        }
        return itemStack;
    }

    public static Builder lootingMultiplier(NumberProvider numberProvider) {
        return new Builder(numberProvider);
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final NumberProvider count;
        private int limit = 0;

        public Builder(NumberProvider numberProvider) {
            this.count = numberProvider;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder setLimit(int i) {
            this.limit = i;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootingEnchantFunction(this.getConditions(), this.count, this.limit);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<LootingEnchantFunction> {
        @Override
        public void serialize(JsonObject jsonObject, LootingEnchantFunction lootingEnchantFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, lootingEnchantFunction, jsonSerializationContext);
            jsonObject.add("count", jsonSerializationContext.serialize(lootingEnchantFunction.value));
            if (lootingEnchantFunction.hasLimit()) {
                jsonObject.add("limit", jsonSerializationContext.serialize(lootingEnchantFunction.limit));
            }
        }

        @Override
        public LootingEnchantFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            int i = GsonHelper.getAsInt(jsonObject, "limit", 0);
            return new LootingEnchantFunction(lootItemConditions, GsonHelper.getAsObject(jsonObject, "count", jsonDeserializationContext, NumberProvider.class), i);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }
}

