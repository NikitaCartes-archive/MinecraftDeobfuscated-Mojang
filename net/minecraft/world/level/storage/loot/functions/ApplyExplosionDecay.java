/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyExplosionDecay
extends LootItemConditionalFunction {
    private ApplyExplosionDecay(LootItemCondition[] lootItemConditions) {
        super(lootItemConditions);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Float float_ = lootContext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
        if (float_ != null) {
            Random random = lootContext.getRandom();
            float f = 1.0f / float_.floatValue();
            int i = itemStack.getCount();
            int j = 0;
            for (int k = 0; k < i; ++k) {
                if (!(random.nextFloat() <= f)) continue;
                ++j;
            }
            itemStack.setCount(j);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> explosionDecay() {
        return ApplyExplosionDecay.simpleBuilder(ApplyExplosionDecay::new);
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<ApplyExplosionDecay> {
        protected Serializer() {
            super(new ResourceLocation("explosion_decay"), ApplyExplosionDecay.class);
        }

        @Override
        public ApplyExplosionDecay deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return new ApplyExplosionDecay(lootItemConditions);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }
}

