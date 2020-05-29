/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class BonusLevelTableCondition
implements LootItemCondition {
    private final Enchantment enchantment;
    private final float[] values;

    private BonusLevelTableCondition(Enchantment enchantment, float[] fs) {
        this.enchantment = enchantment;
        this.values = fs;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.TABLE_BONUS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = lootContext.getParamOrNull(LootContextParams.TOOL);
        int i = itemStack != null ? EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemStack) : 0;
        float f = this.values[Math.min(i, this.values.length - 1)];
        return lootContext.getRandom().nextFloat() < f;
    }

    public static LootItemCondition.Builder bonusLevelFlatChance(Enchantment enchantment, float ... fs) {
        return () -> new BonusLevelTableCondition(enchantment, fs);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<BonusLevelTableCondition> {
        @Override
        public void serialize(JsonObject jsonObject, BonusLevelTableCondition bonusLevelTableCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("enchantment", Registry.ENCHANTMENT.getKey(bonusLevelTableCondition.enchantment).toString());
            jsonObject.add("chances", jsonSerializationContext.serialize(bonusLevelTableCondition.values));
        }

        @Override
        public BonusLevelTableCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "enchantment"));
            Enchantment enchantment = Registry.ENCHANTMENT.getOptional(resourceLocation).orElseThrow(() -> new JsonParseException("Invalid enchantment id: " + resourceLocation));
            float[] fs = GsonHelper.getAsObject(jsonObject, "chances", jsonDeserializationContext, float[].class);
            return new BonusLevelTableCondition(enchantment, fs);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

