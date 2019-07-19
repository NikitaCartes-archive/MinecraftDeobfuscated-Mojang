/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class MatchTool
implements LootItemCondition {
    private final ItemPredicate predicate;

    public MatchTool(ItemPredicate itemPredicate) {
        this.predicate = itemPredicate;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = lootContext.getParamOrNull(LootContextParams.TOOL);
        return itemStack != null && this.predicate.matches(itemStack);
    }

    public static LootItemCondition.Builder toolMatches(ItemPredicate.Builder builder) {
        return () -> new MatchTool(builder.build());
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    extends LootItemCondition.Serializer<MatchTool> {
        protected Serializer() {
            super(new ResourceLocation("match_tool"), MatchTool.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, MatchTool matchTool, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("predicate", matchTool.predicate.serializeToJson());
        }

        @Override
        public MatchTool deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("predicate"));
            return new MatchTool(itemPredicate);
        }

        @Override
        public /* synthetic */ LootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

