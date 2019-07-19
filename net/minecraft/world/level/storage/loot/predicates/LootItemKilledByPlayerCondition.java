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
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemKilledByPlayerCondition
implements LootItemCondition {
    private static final LootItemKilledByPlayerCondition INSTANCE = new LootItemKilledByPlayerCondition();

    private LootItemKilledByPlayerCondition() {
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.hasParam(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    public static LootItemCondition.Builder killedByPlayer() {
        return () -> INSTANCE;
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    extends LootItemCondition.Serializer<LootItemKilledByPlayerCondition> {
        protected Serializer() {
            super(new ResourceLocation("killed_by_player"), LootItemKilledByPlayerCondition.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, LootItemKilledByPlayerCondition lootItemKilledByPlayerCondition, JsonSerializationContext jsonSerializationContext) {
        }

        @Override
        public LootItemKilledByPlayerCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return INSTANCE;
        }

        @Override
        public /* synthetic */ LootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

