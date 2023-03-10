/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.Nullable;

public abstract class Enchantment {
    private final EquipmentSlot[] slots;
    private final Rarity rarity;
    public final EnchantmentCategory category;
    @Nullable
    protected String descriptionId;

    @Nullable
    public static Enchantment byId(int i) {
        return (Enchantment)BuiltInRegistries.ENCHANTMENT.byId(i);
    }

    protected Enchantment(Rarity rarity, EnchantmentCategory enchantmentCategory, EquipmentSlot[] equipmentSlots) {
        this.rarity = rarity;
        this.category = enchantmentCategory;
        this.slots = equipmentSlots;
    }

    public Map<EquipmentSlot, ItemStack> getSlotItems(LivingEntity livingEntity) {
        EnumMap<EquipmentSlot, ItemStack> map = Maps.newEnumMap(EquipmentSlot.class);
        for (EquipmentSlot equipmentSlot : this.slots) {
            ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
            if (itemStack.isEmpty()) continue;
            map.put(equipmentSlot, itemStack);
        }
        return map;
    }

    public Rarity getRarity() {
        return this.rarity;
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 1;
    }

    public int getMinCost(int i) {
        return 1 + i * 10;
    }

    public int getMaxCost(int i) {
        return this.getMinCost(i) + 5;
    }

    public int getDamageProtection(int i, DamageSource damageSource) {
        return 0;
    }

    public float getDamageBonus(int i, MobType mobType) {
        return 0.0f;
    }

    public final boolean isCompatibleWith(Enchantment enchantment) {
        return this.checkCompatibility(enchantment) && enchantment.checkCompatibility(this);
    }

    protected boolean checkCompatibility(Enchantment enchantment) {
        return this != enchantment;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(this));
        }
        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public Component getFullname(int i) {
        MutableComponent mutableComponent = Component.translatable(this.getDescriptionId());
        if (this.isCurse()) {
            mutableComponent.withStyle(ChatFormatting.RED);
        } else {
            mutableComponent.withStyle(ChatFormatting.GRAY);
        }
        if (i != 1 || this.getMaxLevel() != 1) {
            mutableComponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + i));
        }
        return mutableComponent;
    }

    public boolean canEnchant(ItemStack itemStack) {
        return this.category.canEnchant(itemStack.getItem());
    }

    public void doPostAttack(LivingEntity livingEntity, Entity entity, int i) {
    }

    public void doPostHurt(LivingEntity livingEntity, Entity entity, int i) {
    }

    public boolean isTreasureOnly() {
        return false;
    }

    public boolean isCurse() {
        return false;
    }

    public boolean isTradeable() {
        return true;
    }

    public boolean isDiscoverable() {
        return true;
    }

    public static enum Rarity {
        COMMON(10),
        UNCOMMON(5),
        RARE(2),
        VERY_RARE(1);

        private final int weight;

        private Rarity(int j) {
            this.weight = j;
        }

        public int getWeight() {
            return this.weight;
        }
    }
}

