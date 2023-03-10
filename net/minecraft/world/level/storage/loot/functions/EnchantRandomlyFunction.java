/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class EnchantRandomlyFunction
extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    final List<Enchantment> enchantments;

    EnchantRandomlyFunction(LootItemCondition[] lootItemConditions, Collection<Enchantment> collection) {
        super(lootItemConditions);
        this.enchantments = ImmutableList.copyOf(collection);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.ENCHANT_RANDOMLY;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Enchantment enchantment2;
        RandomSource randomSource = lootContext.getRandom();
        if (this.enchantments.isEmpty()) {
            boolean bl = itemStack.is(Items.BOOK);
            List list = BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isDiscoverable).filter(enchantment -> bl || enchantment.canEnchant(itemStack)).collect(Collectors.toList());
            if (list.isEmpty()) {
                LOGGER.warn("Couldn't find a compatible enchantment for {}", (Object)itemStack);
                return itemStack;
            }
            enchantment2 = (Enchantment)list.get(randomSource.nextInt(list.size()));
        } else {
            enchantment2 = this.enchantments.get(randomSource.nextInt(this.enchantments.size()));
        }
        return EnchantRandomlyFunction.enchantItem(itemStack, enchantment2, randomSource);
    }

    private static ItemStack enchantItem(ItemStack itemStack, Enchantment enchantment, RandomSource randomSource) {
        int i = Mth.nextInt(randomSource, enchantment.getMinLevel(), enchantment.getMaxLevel());
        if (itemStack.is(Items.BOOK)) {
            itemStack = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(itemStack, new EnchantmentInstance(enchantment, i));
        } else {
            itemStack.enchant(enchantment, i);
        }
        return itemStack;
    }

    public static Builder randomEnchantment() {
        return new Builder();
    }

    public static LootItemConditionalFunction.Builder<?> randomApplicableEnchantment() {
        return EnchantRandomlyFunction.simpleBuilder(lootItemConditions -> new EnchantRandomlyFunction((LootItemCondition[])lootItemConditions, (Collection<Enchantment>)ImmutableList.of()));
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final Set<Enchantment> enchantments = Sets.newHashSet();

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder withEnchantment(Enchantment enchantment) {
            this.enchantments.add(enchantment);
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new EnchantRandomlyFunction(this.getConditions(), this.enchantments);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<EnchantRandomlyFunction> {
        @Override
        public void serialize(JsonObject jsonObject, EnchantRandomlyFunction enchantRandomlyFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, enchantRandomlyFunction, jsonSerializationContext);
            if (!enchantRandomlyFunction.enchantments.isEmpty()) {
                JsonArray jsonArray = new JsonArray();
                for (Enchantment enchantment : enchantRandomlyFunction.enchantments) {
                    ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
                    if (resourceLocation == null) {
                        throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
                    }
                    jsonArray.add(new JsonPrimitive(resourceLocation.toString()));
                }
                jsonObject.add("enchantments", jsonArray);
            }
        }

        @Override
        public EnchantRandomlyFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            ArrayList<Enchantment> list = Lists.newArrayList();
            if (jsonObject.has("enchantments")) {
                JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "enchantments");
                for (JsonElement jsonElement : jsonArray) {
                    String string = GsonHelper.convertToString(jsonElement, "enchantment");
                    Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(new ResourceLocation(string)).orElseThrow(() -> new JsonSyntaxException("Unknown enchantment '" + string + "'"));
                    list.add(enchantment);
                }
            }
            return new EnchantRandomlyFunction(lootItemConditions, list);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }
}

