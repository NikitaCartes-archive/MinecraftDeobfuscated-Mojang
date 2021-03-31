/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetEnchantmentsFunction
extends LootItemConditionalFunction {
    private final Map<Enchantment, NumberProvider> enchantments;
    private final boolean add;

    private SetEnchantmentsFunction(LootItemCondition[] lootItemConditions, Map<Enchantment, NumberProvider> map, boolean bl) {
        super(lootItemConditions);
        this.enchantments = ImmutableMap.copyOf(map);
        this.add = bl;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_ENCHANTMENTS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.enchantments.values().stream().flatMap(numberProvider -> numberProvider.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Object2IntOpenHashMap<Enchantment> object2IntMap = new Object2IntOpenHashMap<Enchantment>();
        this.enchantments.forEach((enchantment, numberProvider) -> object2IntMap.put((Enchantment)enchantment, numberProvider.getInt(lootContext)));
        if (itemStack.getItem() == Items.BOOK) {
            ItemStack itemStack2 = new ItemStack(Items.ENCHANTED_BOOK);
            object2IntMap.forEach((enchantment, integer) -> EnchantedBookItem.addEnchantment(itemStack2, new EnchantmentInstance((Enchantment)enchantment, (int)integer)));
            return itemStack2;
        }
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemStack);
        if (this.add) {
            object2IntMap.forEach((enchantment, integer) -> SetEnchantmentsFunction.updateEnchantment(map, enchantment, Math.max(map.getOrDefault(enchantment, 0) + integer, 0)));
        } else {
            object2IntMap.forEach((enchantment, integer) -> SetEnchantmentsFunction.updateEnchantment(map, enchantment, Math.max(integer, 0)));
        }
        EnchantmentHelper.setEnchantments(map, itemStack);
        return itemStack;
    }

    private static void updateEnchantment(Map<Enchantment, Integer> map, Enchantment enchantment, int i) {
        if (i == 0) {
            map.remove(enchantment);
        } else {
            map.put(enchantment, i);
        }
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetEnchantmentsFunction> {
        @Override
        public void serialize(JsonObject jsonObject, SetEnchantmentsFunction setEnchantmentsFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setEnchantmentsFunction, jsonSerializationContext);
            JsonObject jsonObject2 = new JsonObject();
            setEnchantmentsFunction.enchantments.forEach((enchantment, numberProvider) -> {
                ResourceLocation resourceLocation = Registry.ENCHANTMENT.getKey((Enchantment)enchantment);
                if (resourceLocation == null) {
                    throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
                }
                jsonObject2.add(resourceLocation.toString(), jsonSerializationContext.serialize(numberProvider));
            });
            jsonObject.add("enchantments", jsonObject2);
            jsonObject.addProperty("add", setEnchantmentsFunction.add);
        }

        @Override
        public SetEnchantmentsFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            HashMap<Enchantment, NumberProvider> map = Maps.newHashMap();
            if (jsonObject.has("enchantments")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "enchantments");
                for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
                    String string = entry.getKey();
                    JsonElement jsonElement = entry.getValue();
                    Enchantment enchantment = Registry.ENCHANTMENT.getOptional(new ResourceLocation(string)).orElseThrow(() -> new JsonSyntaxException("Unknown enchantment '" + string + "'"));
                    NumberProvider numberProvider = (NumberProvider)jsonDeserializationContext.deserialize(jsonElement, (Type)((Object)NumberProvider.class));
                    map.put(enchantment, numberProvider);
                }
            }
            boolean bl = GsonHelper.getAsBoolean(jsonObject, "add", false);
            return new SetEnchantmentsFunction(lootItemConditions, map, bl);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final Map<Enchantment, NumberProvider> enchantments = Maps.newHashMap();
        private final boolean add;

        public Builder() {
            this(false);
        }

        public Builder(boolean bl) {
            this.add = bl;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder withEnchantment(Enchantment enchantment, NumberProvider numberProvider) {
            this.enchantments.put(enchantment, numberProvider);
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetEnchantmentsFunction(this.getConditions(), this.enchantments, this.add);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

