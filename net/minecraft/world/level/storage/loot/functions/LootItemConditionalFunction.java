/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootItemConditionalFunction
implements LootItemFunction {
    protected final LootItemCondition[] predicates;
    private final Predicate<LootContext> compositePredicates;

    protected LootItemConditionalFunction(LootItemCondition[] lootItemConditions) {
        this.predicates = lootItemConditions;
        this.compositePredicates = LootItemConditions.andConditions(lootItemConditions);
    }

    @Override
    public final ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        return this.compositePredicates.test(lootContext) ? this.run(itemStack, lootContext) : itemStack;
    }

    protected abstract ItemStack run(ItemStack var1, LootContext var2);

    @Override
    public void validate(LootTableProblemCollector lootTableProblemCollector, Function<ResourceLocation, LootTable> function, Set<ResourceLocation> set, LootContextParamSet lootContextParamSet) {
        LootItemFunction.super.validate(lootTableProblemCollector, function, set, lootContextParamSet);
        for (int i = 0; i < this.predicates.length; ++i) {
            this.predicates[i].validate(lootTableProblemCollector.forChild(".conditions[" + i + "]"), function, set, lootContextParamSet);
        }
    }

    protected static Builder<?> simpleBuilder(Function<LootItemCondition[], LootItemFunction> function) {
        return new DummyBuilder(function);
    }

    @Override
    public /* synthetic */ Object apply(Object object, Object object2) {
        return this.apply((ItemStack)object, (LootContext)object2);
    }

    public static abstract class Serializer<T extends LootItemConditionalFunction>
    extends LootItemFunction.Serializer<T> {
        public Serializer(ResourceLocation resourceLocation, Class<T> class_) {
            super(resourceLocation, class_);
        }

        @Override
        public void serialize(JsonObject jsonObject, T lootItemConditionalFunction, JsonSerializationContext jsonSerializationContext) {
            if (!ArrayUtils.isEmpty(((LootItemConditionalFunction)lootItemConditionalFunction).predicates)) {
                jsonObject.add("conditions", jsonSerializationContext.serialize(((LootItemConditionalFunction)lootItemConditionalFunction).predicates));
            }
        }

        @Override
        public final T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            LootItemCondition[] lootItemConditions = GsonHelper.getAsObject(jsonObject, "conditions", new LootItemCondition[0], jsonDeserializationContext, LootItemCondition[].class);
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }

        public abstract T deserialize(JsonObject var1, JsonDeserializationContext var2, LootItemCondition[] var3);

        @Override
        public /* synthetic */ LootItemFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }

    static final class DummyBuilder
    extends Builder<DummyBuilder> {
        private final Function<LootItemCondition[], LootItemFunction> constructor;

        public DummyBuilder(Function<LootItemCondition[], LootItemFunction> function) {
            this.constructor = function;
        }

        @Override
        protected DummyBuilder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return this.constructor.apply(this.getConditions());
        }

        @Override
        protected /* synthetic */ Builder getThis() {
            return this.getThis();
        }
    }

    public static abstract class Builder<T extends Builder<T>>
    implements LootItemFunction.Builder,
    ConditionUserBuilder<T> {
        private final List<LootItemCondition> conditions = Lists.newArrayList();

        @Override
        public T when(LootItemCondition.Builder builder) {
            this.conditions.add(builder.build());
            return this.getThis();
        }

        @Override
        public final T unwrap() {
            return this.getThis();
        }

        protected abstract T getThis();

        protected LootItemCondition[] getConditions() {
            return this.conditions.toArray(new LootItemCondition[0]);
        }

        @Override
        public /* synthetic */ Object unwrap() {
            return this.unwrap();
        }

        @Override
        public /* synthetic */ Object when(LootItemCondition.Builder builder) {
            return this.when(builder);
        }
    }
}

