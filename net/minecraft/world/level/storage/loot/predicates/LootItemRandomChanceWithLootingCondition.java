/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemRandomChanceWithLootingCondition
implements LootItemCondition {
    private final float percent;
    private final float lootingMultiplier;

    private LootItemRandomChanceWithLootingCondition(float f, float g) {
        this.percent = f;
        this.lootingMultiplier = g;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
        int i = 0;
        if (entity instanceof LivingEntity) {
            i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
        }
        return lootContext.getRandom().nextFloat() < this.percent + (float)i * this.lootingMultiplier;
    }

    public static LootItemCondition.Builder randomChanceAndLootingBoost(float f, float g) {
        return () -> new LootItemRandomChanceWithLootingCondition(f, g);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    extends LootItemCondition.Serializer<LootItemRandomChanceWithLootingCondition> {
        protected Serializer() {
            super(new ResourceLocation("random_chance_with_looting"), LootItemRandomChanceWithLootingCondition.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, LootItemRandomChanceWithLootingCondition lootItemRandomChanceWithLootingCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("chance", Float.valueOf(lootItemRandomChanceWithLootingCondition.percent));
            jsonObject.addProperty("looting_multiplier", Float.valueOf(lootItemRandomChanceWithLootingCondition.lootingMultiplier));
        }

        @Override
        public LootItemRandomChanceWithLootingCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootItemRandomChanceWithLootingCondition(GsonHelper.getAsFloat(jsonObject, "chance"), GsonHelper.getAsFloat(jsonObject, "looting_multiplier"));
        }

        @Override
        public /* synthetic */ LootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

