/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemRandomChanceCondition
implements LootItemCondition {
    private final float probability;

    private LootItemRandomChanceCondition(float f) {
        this.probability = f;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.getRandom().nextFloat() < this.probability;
    }

    public static LootItemCondition.Builder randomChance(float f) {
        return () -> new LootItemRandomChanceCondition(f);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    extends LootItemCondition.Serializer<LootItemRandomChanceCondition> {
        protected Serializer() {
            super(new ResourceLocation("random_chance"), LootItemRandomChanceCondition.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, LootItemRandomChanceCondition lootItemRandomChanceCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("chance", Float.valueOf(lootItemRandomChanceCondition.probability));
        }

        @Override
        public LootItemRandomChanceCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootItemRandomChanceCondition(GsonHelper.getAsFloat(jsonObject, "chance"));
        }

        @Override
        public /* synthetic */ LootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

