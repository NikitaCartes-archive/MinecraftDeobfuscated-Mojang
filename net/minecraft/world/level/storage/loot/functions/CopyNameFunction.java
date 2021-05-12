/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyNameFunction
extends LootItemConditionalFunction {
    final NameSource source;

    CopyNameFunction(LootItemCondition[] lootItemConditions, NameSource nameSource) {
        super(lootItemConditions);
        this.source = nameSource;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.COPY_NAME;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.source.param);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Nameable nameable;
        Object object = lootContext.getParamOrNull(this.source.param);
        if (object instanceof Nameable && (nameable = (Nameable)object).hasCustomName()) {
            itemStack.setHoverName(nameable.getDisplayName());
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> copyName(NameSource nameSource) {
        return CopyNameFunction.simpleBuilder(lootItemConditions -> new CopyNameFunction((LootItemCondition[])lootItemConditions, nameSource));
    }

    public static enum NameSource {
        THIS("this", LootContextParams.THIS_ENTITY),
        KILLER("killer", LootContextParams.KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER),
        BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY);

        public final String name;
        public final LootContextParam<?> param;

        private NameSource(String string2, LootContextParam<?> lootContextParam) {
            this.name = string2;
            this.param = lootContextParam;
        }

        public static NameSource getByName(String string) {
            for (NameSource nameSource : NameSource.values()) {
                if (!nameSource.name.equals(string)) continue;
                return nameSource;
            }
            throw new IllegalArgumentException("Invalid name source " + string);
        }
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<CopyNameFunction> {
        @Override
        public void serialize(JsonObject jsonObject, CopyNameFunction copyNameFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, copyNameFunction, jsonSerializationContext);
            jsonObject.addProperty("source", copyNameFunction.source.name);
        }

        @Override
        public CopyNameFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            NameSource nameSource = NameSource.getByName(GsonHelper.getAsString(jsonObject, "source"));
            return new CopyNameFunction(lootItemConditions, nameSource);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }
}

