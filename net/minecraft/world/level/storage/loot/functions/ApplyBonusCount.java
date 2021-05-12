/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyBonusCount
extends LootItemConditionalFunction {
    static final Map<ResourceLocation, FormulaDeserializer> FORMULAS = Maps.newHashMap();
    final Enchantment enchantment;
    final Formula formula;

    ApplyBonusCount(LootItemCondition[] lootItemConditions, Enchantment enchantment, Formula formula) {
        super(lootItemConditions);
        this.enchantment = enchantment;
        this.formula = formula;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.APPLY_BONUS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        ItemStack itemStack2 = lootContext.getParamOrNull(LootContextParams.TOOL);
        if (itemStack2 != null) {
            int i = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemStack2);
            int j = this.formula.calculateNewCount(lootContext.getRandom(), itemStack.getCount(), i);
            itemStack.setCount(j);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> addBonusBinomialDistributionCount(Enchantment enchantment, float f, int i) {
        return ApplyBonusCount.simpleBuilder(lootItemConditions -> new ApplyBonusCount((LootItemCondition[])lootItemConditions, enchantment, new BinomialWithBonusCount(i, f)));
    }

    public static LootItemConditionalFunction.Builder<?> addOreBonusCount(Enchantment enchantment) {
        return ApplyBonusCount.simpleBuilder(lootItemConditions -> new ApplyBonusCount((LootItemCondition[])lootItemConditions, enchantment, new OreDrops()));
    }

    public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment enchantment) {
        return ApplyBonusCount.simpleBuilder(lootItemConditions -> new ApplyBonusCount((LootItemCondition[])lootItemConditions, enchantment, new UniformBonusCount(1)));
    }

    public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment enchantment, int i) {
        return ApplyBonusCount.simpleBuilder(lootItemConditions -> new ApplyBonusCount((LootItemCondition[])lootItemConditions, enchantment, new UniformBonusCount(i)));
    }

    static {
        FORMULAS.put(BinomialWithBonusCount.TYPE, BinomialWithBonusCount::deserialize);
        FORMULAS.put(OreDrops.TYPE, OreDrops::deserialize);
        FORMULAS.put(UniformBonusCount.TYPE, UniformBonusCount::deserialize);
    }

    static interface Formula {
        public int calculateNewCount(Random var1, int var2, int var3);

        public void serializeParams(JsonObject var1, JsonSerializationContext var2);

        public ResourceLocation getType();
    }

    static final class UniformBonusCount
    implements Formula {
        public static final ResourceLocation TYPE = new ResourceLocation("uniform_bonus_count");
        private final int bonusMultiplier;

        public UniformBonusCount(int i) {
            this.bonusMultiplier = i;
        }

        @Override
        public int calculateNewCount(Random random, int i, int j) {
            return i + random.nextInt(this.bonusMultiplier * j + 1);
        }

        @Override
        public void serializeParams(JsonObject jsonObject, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("bonusMultiplier", this.bonusMultiplier);
        }

        public static Formula deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            int i = GsonHelper.getAsInt(jsonObject, "bonusMultiplier");
            return new UniformBonusCount(i);
        }

        @Override
        public ResourceLocation getType() {
            return TYPE;
        }
    }

    static final class OreDrops
    implements Formula {
        public static final ResourceLocation TYPE = new ResourceLocation("ore_drops");

        OreDrops() {
        }

        @Override
        public int calculateNewCount(Random random, int i, int j) {
            if (j > 0) {
                int k = random.nextInt(j + 2) - 1;
                if (k < 0) {
                    k = 0;
                }
                return i * (k + 1);
            }
            return i;
        }

        @Override
        public void serializeParams(JsonObject jsonObject, JsonSerializationContext jsonSerializationContext) {
        }

        public static Formula deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new OreDrops();
        }

        @Override
        public ResourceLocation getType() {
            return TYPE;
        }
    }

    static final class BinomialWithBonusCount
    implements Formula {
        public static final ResourceLocation TYPE = new ResourceLocation("binomial_with_bonus_count");
        private final int extraRounds;
        private final float probability;

        public BinomialWithBonusCount(int i, float f) {
            this.extraRounds = i;
            this.probability = f;
        }

        @Override
        public int calculateNewCount(Random random, int i, int j) {
            for (int k = 0; k < j + this.extraRounds; ++k) {
                if (!(random.nextFloat() < this.probability)) continue;
                ++i;
            }
            return i;
        }

        @Override
        public void serializeParams(JsonObject jsonObject, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("extra", this.extraRounds);
            jsonObject.addProperty("probability", Float.valueOf(this.probability));
        }

        public static Formula deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            int i = GsonHelper.getAsInt(jsonObject, "extra");
            float f = GsonHelper.getAsFloat(jsonObject, "probability");
            return new BinomialWithBonusCount(i, f);
        }

        @Override
        public ResourceLocation getType() {
            return TYPE;
        }
    }

    static interface FormulaDeserializer {
        public Formula deserialize(JsonObject var1, JsonDeserializationContext var2);
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<ApplyBonusCount> {
        @Override
        public void serialize(JsonObject jsonObject, ApplyBonusCount applyBonusCount, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, applyBonusCount, jsonSerializationContext);
            jsonObject.addProperty("enchantment", Registry.ENCHANTMENT.getKey(applyBonusCount.enchantment).toString());
            jsonObject.addProperty("formula", applyBonusCount.formula.getType().toString());
            JsonObject jsonObject2 = new JsonObject();
            applyBonusCount.formula.serializeParams(jsonObject2, jsonSerializationContext);
            if (jsonObject2.size() > 0) {
                jsonObject.add("parameters", jsonObject2);
            }
        }

        @Override
        public ApplyBonusCount deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "enchantment"));
            Enchantment enchantment = Registry.ENCHANTMENT.getOptional(resourceLocation).orElseThrow(() -> new JsonParseException("Invalid enchantment id: " + resourceLocation));
            ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "formula"));
            FormulaDeserializer formulaDeserializer = FORMULAS.get(resourceLocation2);
            if (formulaDeserializer == null) {
                throw new JsonParseException("Invalid formula id: " + resourceLocation2);
            }
            Formula formula = jsonObject.has("parameters") ? formulaDeserializer.deserialize(GsonHelper.getAsJsonObject(jsonObject, "parameters"), jsonDeserializationContext) : formulaDeserializer.deserialize(new JsonObject(), jsonDeserializationContext);
            return new ApplyBonusCount(lootItemConditions, enchantment, formula);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }
}

