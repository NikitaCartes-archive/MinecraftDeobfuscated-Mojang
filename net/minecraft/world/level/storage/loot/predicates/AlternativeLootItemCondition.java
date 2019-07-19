/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class AlternativeLootItemCondition
implements LootItemCondition {
    private final LootItemCondition[] terms;
    private final Predicate<LootContext> composedPredicate;

    private AlternativeLootItemCondition(LootItemCondition[] lootItemConditions) {
        this.terms = lootItemConditions;
        this.composedPredicate = LootItemConditions.orConditions(lootItemConditions);
    }

    @Override
    public final boolean test(LootContext lootContext) {
        return this.composedPredicate.test(lootContext);
    }

    @Override
    public void validate(LootTableProblemCollector lootTableProblemCollector, Function<ResourceLocation, LootTable> function, Set<ResourceLocation> set, LootContextParamSet lootContextParamSet) {
        LootItemCondition.super.validate(lootTableProblemCollector, function, set, lootContextParamSet);
        for (int i = 0; i < this.terms.length; ++i) {
            this.terms[i].validate(lootTableProblemCollector.forChild(".term[" + i + "]"), function, set, lootContextParamSet);
        }
    }

    public static Builder alternative(LootItemCondition.Builder ... builders) {
        return new Builder(builders);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    extends LootItemCondition.Serializer<AlternativeLootItemCondition> {
        public Serializer() {
            super(new ResourceLocation("alternative"), AlternativeLootItemCondition.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, AlternativeLootItemCondition alternativeLootItemCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("terms", jsonSerializationContext.serialize(alternativeLootItemCondition.terms));
        }

        @Override
        public AlternativeLootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            LootItemCondition[] lootItemConditions = GsonHelper.getAsObject(jsonObject, "terms", jsonDeserializationContext, LootItemCondition[].class);
            return new AlternativeLootItemCondition(lootItemConditions);
        }

        @Override
        public /* synthetic */ LootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }

    public static class Builder
    implements LootItemCondition.Builder {
        private final List<LootItemCondition> terms = Lists.newArrayList();

        public Builder(LootItemCondition.Builder ... builders) {
            for (LootItemCondition.Builder builder : builders) {
                this.terms.add(builder.build());
            }
        }

        @Override
        public Builder or(LootItemCondition.Builder builder) {
            this.terms.add(builder.build());
            return this;
        }

        @Override
        public LootItemCondition build() {
            return new AlternativeLootItemCondition(this.terms.toArray(new LootItemCondition[0]));
        }
    }
}

