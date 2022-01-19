/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.slf4j.Logger;

public class SetItemDamageFunction
extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    final NumberProvider damage;
    final boolean add;

    SetItemDamageFunction(LootItemCondition[] lootItemConditions, NumberProvider numberProvider, boolean bl) {
        super(lootItemConditions);
        this.damage = numberProvider;
        this.add = bl;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_DAMAGE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.damage.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.isDamageableItem()) {
            int i = itemStack.getMaxDamage();
            float f = this.add ? 1.0f - (float)itemStack.getDamageValue() / (float)i : 0.0f;
            float g = 1.0f - Mth.clamp(this.damage.getFloat(lootContext) + f, 0.0f, 1.0f);
            itemStack.setDamageValue(Mth.floor(g * (float)i));
        } else {
            LOGGER.warn("Couldn't set damage of loot item {}", (Object)itemStack);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider numberProvider) {
        return SetItemDamageFunction.simpleBuilder(lootItemConditions -> new SetItemDamageFunction((LootItemCondition[])lootItemConditions, numberProvider, false));
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider numberProvider, boolean bl) {
        return SetItemDamageFunction.simpleBuilder(lootItemConditions -> new SetItemDamageFunction((LootItemCondition[])lootItemConditions, numberProvider, bl));
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetItemDamageFunction> {
        @Override
        public void serialize(JsonObject jsonObject, SetItemDamageFunction setItemDamageFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setItemDamageFunction, jsonSerializationContext);
            jsonObject.add("damage", jsonSerializationContext.serialize(setItemDamageFunction.damage));
            jsonObject.addProperty("add", setItemDamageFunction.add);
        }

        @Override
        public SetItemDamageFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "damage", jsonDeserializationContext, NumberProvider.class);
            boolean bl = GsonHelper.getAsBoolean(jsonObject, "add", false);
            return new SetItemDamageFunction(lootItemConditions, numberProvider, bl);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }
}

