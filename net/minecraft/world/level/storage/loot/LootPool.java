/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
    private final LootPoolEntryContainer[] entries;
    private final LootItemCondition[] conditions;
    private final Predicate<LootContext> compositeCondition;
    private final LootItemFunction[] functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final NumberProvider rolls;
    private final NumberProvider bonusRolls;

    private LootPool(LootPoolEntryContainer[] lootPoolEntryContainers, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions, NumberProvider numberProvider, NumberProvider numberProvider2) {
        this.entries = lootPoolEntryContainers;
        this.conditions = lootItemConditions;
        this.compositeCondition = LootItemConditions.andConditions(lootItemConditions);
        this.functions = lootItemFunctions;
        this.compositeFunction = LootItemFunctions.compose(lootItemFunctions);
        this.rolls = numberProvider;
        this.bonusRolls = numberProvider2;
    }

    private void addRandomItem(Consumer<ItemStack> consumer, LootContext lootContext) {
        Random random = lootContext.getRandom();
        ArrayList<LootPoolEntry> list = Lists.newArrayList();
        MutableInt mutableInt = new MutableInt();
        for (LootPoolEntryContainer lootPoolEntryContainer : this.entries) {
            lootPoolEntryContainer.expand(lootContext, lootPoolEntry -> {
                int i = lootPoolEntry.getWeight(lootContext.getLuck());
                if (i > 0) {
                    list.add(lootPoolEntry);
                    mutableInt.add(i);
                }
            });
        }
        int i = list.size();
        if (mutableInt.intValue() == 0 || i == 0) {
            return;
        }
        if (i == 1) {
            ((LootPoolEntry)list.get(0)).createItemStack(consumer, lootContext);
            return;
        }
        int j = random.nextInt(mutableInt.intValue());
        for (LootPoolEntry lootPoolEntry2 : list) {
            if ((j -= lootPoolEntry2.getWeight(lootContext.getLuck())) >= 0) continue;
            lootPoolEntry2.createItemStack(consumer, lootContext);
            return;
        }
    }

    public void addRandomItems(Consumer<ItemStack> consumer, LootContext lootContext) {
        if (!this.compositeCondition.test(lootContext)) {
            return;
        }
        Consumer<ItemStack> consumer2 = LootItemFunction.decorate(this.compositeFunction, consumer, lootContext);
        int i = this.rolls.getInt(lootContext) + Mth.floor(this.bonusRolls.getFloat(lootContext) * lootContext.getLuck());
        for (int j = 0; j < i; ++j) {
            this.addRandomItem(consumer2, lootContext);
        }
    }

    public void validate(ValidationContext validationContext) {
        int i;
        for (i = 0; i < this.conditions.length; ++i) {
            this.conditions[i].validate(validationContext.forChild(".condition[" + i + "]"));
        }
        for (i = 0; i < this.functions.length; ++i) {
            this.functions[i].validate(validationContext.forChild(".functions[" + i + "]"));
        }
        for (i = 0; i < this.entries.length; ++i) {
            this.entries[i].validate(validationContext.forChild(".entries[" + i + "]"));
        }
        this.rolls.validate(validationContext.forChild(".rolls"));
        this.bonusRolls.validate(validationContext.forChild(".bonusRolls"));
    }

    public static Builder lootPool() {
        return new Builder();
    }

    public static class Serializer
    implements JsonDeserializer<LootPool>,
    JsonSerializer<LootPool> {
        @Override
        public LootPool deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "loot pool");
            LootPoolEntryContainer[] lootPoolEntryContainers = GsonHelper.getAsObject(jsonObject, "entries", jsonDeserializationContext, LootPoolEntryContainer[].class);
            LootItemCondition[] lootItemConditions = GsonHelper.getAsObject(jsonObject, "conditions", new LootItemCondition[0], jsonDeserializationContext, LootItemCondition[].class);
            LootItemFunction[] lootItemFunctions = GsonHelper.getAsObject(jsonObject, "functions", new LootItemFunction[0], jsonDeserializationContext, LootItemFunction[].class);
            NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "rolls", jsonDeserializationContext, NumberProvider.class);
            NumberProvider numberProvider2 = GsonHelper.getAsObject(jsonObject, "bonus_rolls", ConstantValue.exactly(0.0f), jsonDeserializationContext, NumberProvider.class);
            return new LootPool(lootPoolEntryContainers, lootItemConditions, lootItemFunctions, numberProvider, numberProvider2);
        }

        @Override
        public JsonElement serialize(LootPool lootPool, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("rolls", jsonSerializationContext.serialize(lootPool.rolls));
            jsonObject.add("bonus_rolls", jsonSerializationContext.serialize(lootPool.bonusRolls));
            jsonObject.add("entries", jsonSerializationContext.serialize(lootPool.entries));
            if (!ArrayUtils.isEmpty(lootPool.conditions)) {
                jsonObject.add("conditions", jsonSerializationContext.serialize(lootPool.conditions));
            }
            if (!ArrayUtils.isEmpty(lootPool.functions)) {
                jsonObject.add("functions", jsonSerializationContext.serialize(lootPool.functions));
            }
            return jsonObject;
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((LootPool)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }

    public static class Builder
    implements FunctionUserBuilder<Builder>,
    ConditionUserBuilder<Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();
        private final List<LootItemCondition> conditions = Lists.newArrayList();
        private final List<LootItemFunction> functions = Lists.newArrayList();
        private NumberProvider rolls = ConstantValue.exactly(1.0f);
        private NumberProvider bonusRolls = ConstantValue.exactly(0.0f);

        public Builder setRolls(NumberProvider numberProvider) {
            this.rolls = numberProvider;
            return this;
        }

        @Override
        public Builder unwrap() {
            return this;
        }

        public Builder add(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add(builder.build());
            return this;
        }

        @Override
        public Builder when(LootItemCondition.Builder builder) {
            this.conditions.add(builder.build());
            return this;
        }

        @Override
        public Builder apply(LootItemFunction.Builder builder) {
            this.functions.add(builder.build());
            return this;
        }

        public LootPool build() {
            if (this.rolls == null) {
                throw new IllegalArgumentException("Rolls not set");
            }
            return new LootPool(this.entries.toArray(new LootPoolEntryContainer[0]), this.conditions.toArray(new LootItemCondition[0]), this.functions.toArray(new LootItemFunction[0]), this.rolls, this.bonusRolls);
        }

        @Override
        public /* synthetic */ Object unwrap() {
            return this.unwrap();
        }

        @Override
        public /* synthetic */ Object apply(LootItemFunction.Builder builder) {
            return this.apply(builder);
        }

        @Override
        public /* synthetic */ Object when(LootItemCondition.Builder builder) {
            return this.when(builder);
        }
    }
}

