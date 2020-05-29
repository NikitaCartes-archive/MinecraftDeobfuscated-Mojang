/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class InvertedLootItemCondition
implements LootItemCondition {
    private final LootItemCondition term;

    private InvertedLootItemCondition(LootItemCondition lootItemCondition) {
        this.term = lootItemCondition;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.INVERTED;
    }

    @Override
    public final boolean test(LootContext lootContext) {
        return !this.term.test(lootContext);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.term.getReferencedContextParams();
    }

    @Override
    public void validate(ValidationContext validationContext) {
        LootItemCondition.super.validate(validationContext);
        this.term.validate(validationContext);
    }

    public static LootItemCondition.Builder invert(LootItemCondition.Builder builder) {
        InvertedLootItemCondition invertedLootItemCondition = new InvertedLootItemCondition(builder.build());
        return () -> invertedLootItemCondition;
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<InvertedLootItemCondition> {
        @Override
        public void serialize(JsonObject jsonObject, InvertedLootItemCondition invertedLootItemCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("term", jsonSerializationContext.serialize(invertedLootItemCondition.term));
        }

        @Override
        public InvertedLootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            LootItemCondition lootItemCondition = GsonHelper.getAsObject(jsonObject, "term", jsonDeserializationContext, LootItemCondition.class);
            return new InvertedLootItemCondition(lootItemCondition);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

