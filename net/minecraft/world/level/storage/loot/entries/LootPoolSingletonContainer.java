/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootPoolSingletonContainer
extends LootPoolEntryContainer {
    protected final int weight;
    protected final int quality;
    protected final LootItemFunction[] functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final LootPoolEntry entry = new EntryBase(){

        @Override
        public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
            LootPoolSingletonContainer.this.createItemStack(LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, consumer, lootContext), lootContext);
        }
    };

    protected LootPoolSingletonContainer(int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
        super(lootItemConditions);
        this.weight = i;
        this.quality = j;
        this.functions = lootItemFunctions;
        this.compositeFunction = LootItemFunctions.compose(lootItemFunctions);
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        for (int i = 0; i < this.functions.length; ++i) {
            this.functions[i].validate(validationContext.forChild(".functions[" + i + "]"));
        }
    }

    protected abstract void createItemStack(Consumer<ItemStack> var1, LootContext var2);

    @Override
    public boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (this.canRun(lootContext)) {
            consumer.accept(this.entry);
            return true;
        }
        return false;
    }

    public static Builder<?> simpleBuilder(EntryConstructor entryConstructor) {
        return new DummyBuilder(entryConstructor);
    }

    public static abstract class Serializer<T extends LootPoolSingletonContainer>
    extends LootPoolEntryContainer.Serializer<T> {
        @Override
        public void serializeCustom(JsonObject jsonObject, T lootPoolSingletonContainer, JsonSerializationContext jsonSerializationContext) {
            if (((LootPoolSingletonContainer)lootPoolSingletonContainer).weight != 1) {
                jsonObject.addProperty("weight", ((LootPoolSingletonContainer)lootPoolSingletonContainer).weight);
            }
            if (((LootPoolSingletonContainer)lootPoolSingletonContainer).quality != 0) {
                jsonObject.addProperty("quality", ((LootPoolSingletonContainer)lootPoolSingletonContainer).quality);
            }
            if (!ArrayUtils.isEmpty(((LootPoolSingletonContainer)lootPoolSingletonContainer).functions)) {
                jsonObject.add("functions", jsonSerializationContext.serialize(((LootPoolSingletonContainer)lootPoolSingletonContainer).functions));
            }
        }

        @Override
        public final T deserializeCustom(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            int i = GsonHelper.getAsInt(jsonObject, "weight", 1);
            int j = GsonHelper.getAsInt(jsonObject, "quality", 0);
            LootItemFunction[] lootItemFunctions = GsonHelper.getAsObject(jsonObject, "functions", new LootItemFunction[0], jsonDeserializationContext, LootItemFunction[].class);
            return this.deserialize(jsonObject, jsonDeserializationContext, i, j, lootItemConditions, lootItemFunctions);
        }

        protected abstract T deserialize(JsonObject var1, JsonDeserializationContext var2, int var3, int var4, LootItemCondition[] var5, LootItemFunction[] var6);

        @Override
        public /* synthetic */ LootPoolEntryContainer deserializeCustom(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserializeCustom(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }

    static class DummyBuilder
    extends Builder<DummyBuilder> {
        private final EntryConstructor constructor;

        public DummyBuilder(EntryConstructor entryConstructor) {
            this.constructor = entryConstructor;
        }

        @Override
        protected DummyBuilder getThis() {
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return this.constructor.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
        }

        @Override
        protected /* synthetic */ LootPoolEntryContainer.Builder getThis() {
            return this.getThis();
        }
    }

    @FunctionalInterface
    public static interface EntryConstructor {
        public LootPoolSingletonContainer build(int var1, int var2, LootItemCondition[] var3, LootItemFunction[] var4);
    }

    public static abstract class Builder<T extends Builder<T>>
    extends LootPoolEntryContainer.Builder<T>
    implements FunctionUserBuilder<T> {
        protected int weight = 1;
        protected int quality = 0;
        private final List<LootItemFunction> functions = Lists.newArrayList();

        @Override
        public T apply(LootItemFunction.Builder builder) {
            this.functions.add(builder.build());
            return (T)((Builder)this.getThis());
        }

        protected LootItemFunction[] getFunctions() {
            return this.functions.toArray(new LootItemFunction[0]);
        }

        public T setWeight(int i) {
            this.weight = i;
            return (T)((Builder)this.getThis());
        }

        public T setQuality(int i) {
            this.quality = i;
            return (T)((Builder)this.getThis());
        }

        @Override
        public /* synthetic */ Object apply(LootItemFunction.Builder builder) {
            return this.apply(builder);
        }
    }

    public abstract class EntryBase
    implements LootPoolEntry {
        protected EntryBase() {
        }

        @Override
        public int getWeight(float f) {
            return Math.max(Mth.floor((float)LootPoolSingletonContainer.this.weight + (float)LootPoolSingletonContainer.this.quality * f), 0);
        }
    }
}

