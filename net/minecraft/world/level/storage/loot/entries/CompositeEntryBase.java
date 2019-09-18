/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase
extends LootPoolEntryContainer {
    protected final LootPoolEntryContainer[] children;
    private final ComposableEntryContainer composedChildren;

    protected CompositeEntryBase(LootPoolEntryContainer[] lootPoolEntryContainers, LootItemCondition[] lootItemConditions) {
        super(lootItemConditions);
        this.children = lootPoolEntryContainers;
        this.composedChildren = this.compose(lootPoolEntryContainers);
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        if (this.children.length == 0) {
            validationContext.reportProblem("Empty children list");
        }
        for (int i = 0; i < this.children.length; ++i) {
            this.children[i].validate(validationContext.forChild(".entry[" + i + "]"));
        }
    }

    protected abstract ComposableEntryContainer compose(ComposableEntryContainer[] var1);

    @Override
    public final boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (!this.canRun(lootContext)) {
            return false;
        }
        return this.composedChildren.expand(lootContext, consumer);
    }

    public static <T extends CompositeEntryBase> Serializer<T> createSerializer(ResourceLocation resourceLocation, Class<T> class_, final CompositeEntryConstructor<T> compositeEntryConstructor) {
        return new Serializer<T>(resourceLocation, class_){

            @Override
            protected T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootPoolEntryContainer[] lootPoolEntryContainers, LootItemCondition[] lootItemConditions) {
                return compositeEntryConstructor.create(lootPoolEntryContainers, lootItemConditions);
            }
        };
    }

    public static abstract class Serializer<T extends CompositeEntryBase>
    extends LootPoolEntryContainer.Serializer<T> {
        public Serializer(ResourceLocation resourceLocation, Class<T> class_) {
            super(resourceLocation, class_);
        }

        @Override
        public void serialize(JsonObject jsonObject, T compositeEntryBase, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("children", jsonSerializationContext.serialize(((CompositeEntryBase)compositeEntryBase).children));
        }

        @Override
        public final T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            LootPoolEntryContainer[] lootPoolEntryContainers = GsonHelper.getAsObject(jsonObject, "children", jsonDeserializationContext, LootPoolEntryContainer[].class);
            return this.deserialize(jsonObject, jsonDeserializationContext, lootPoolEntryContainers, lootItemConditions);
        }

        protected abstract T deserialize(JsonObject var1, JsonDeserializationContext var2, LootPoolEntryContainer[] var3, LootItemCondition[] var4);

        @Override
        public /* synthetic */ LootPoolEntryContainer deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }

    @FunctionalInterface
    public static interface CompositeEntryConstructor<T extends CompositeEntryBase> {
        public T create(LootPoolEntryContainer[] var1, LootItemCondition[] var2);
    }
}

