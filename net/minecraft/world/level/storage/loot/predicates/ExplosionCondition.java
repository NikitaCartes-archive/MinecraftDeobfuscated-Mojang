/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ExplosionCondition
implements LootItemCondition {
    private static final ExplosionCondition INSTANCE = new ExplosionCondition();

    private ExplosionCondition() {
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.EXPLOSION_RADIUS);
    }

    @Override
    public boolean test(LootContext lootContext) {
        Float float_ = lootContext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
        if (float_ != null) {
            Random random = lootContext.getRandom();
            float f = 1.0f / float_.floatValue();
            return random.nextFloat() <= f;
        }
        return true;
    }

    public static LootItemCondition.Builder survivesExplosion() {
        return () -> INSTANCE;
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    extends LootItemCondition.Serializer<ExplosionCondition> {
        protected Serializer() {
            super(new ResourceLocation("survives_explosion"), ExplosionCondition.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, ExplosionCondition explosionCondition, JsonSerializationContext jsonSerializationContext) {
        }

        @Override
        public ExplosionCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return INSTANCE;
        }

        @Override
        public /* synthetic */ LootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

