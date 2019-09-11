/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.SweepingEdgeEnchantment;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public class EnchantmentHelper {
    public static int getItemEnchantmentLevel(Enchantment enchantment, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        ResourceLocation resourceLocation = Registry.ENCHANTMENT.getKey(enchantment);
        ListTag listTag = itemStack.getEnchantmentTags();
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            ResourceLocation resourceLocation2 = ResourceLocation.tryParse(compoundTag.getString("id"));
            if (resourceLocation2 == null || !resourceLocation2.equals(resourceLocation)) continue;
            return compoundTag.getInt("lvl");
        }
        return 0;
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack itemStack) {
        ListTag listTag = itemStack.getItem() == Items.ENCHANTED_BOOK ? EnchantedBookItem.getEnchantments(itemStack) : itemStack.getEnchantmentTags();
        return EnchantmentHelper.deserializeEnchantments(listTag);
    }

    public static Map<Enchantment, Integer> deserializeEnchantments(ListTag listTag) {
        LinkedHashMap<Enchantment, Integer> map = Maps.newLinkedHashMap();
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(compoundTag.getString("id"))).ifPresent(enchantment -> map.put((Enchantment)enchantment, compoundTag.getInt("lvl")));
        }
        return map;
    }

    public static void setEnchantments(Map<Enchantment, Integer> map, ItemStack itemStack) {
        ListTag listTag = new ListTag();
        for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment == null) continue;
            int i = entry.getValue();
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getKey(enchantment)));
            compoundTag.putShort("lvl", (short)i);
            listTag.add(compoundTag);
            if (itemStack.getItem() != Items.ENCHANTED_BOOK) continue;
            EnchantedBookItem.addEnchantment(itemStack, new EnchantmentInstance(enchantment, i));
        }
        if (listTag.isEmpty()) {
            itemStack.removeTagKey("Enchantments");
        } else if (itemStack.getItem() != Items.ENCHANTED_BOOK) {
            itemStack.addTagElement("Enchantments", listTag);
        }
    }

    private static void runIterationOnItem(EnchantmentVisitor enchantmentVisitor, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return;
        }
        ListTag listTag = itemStack.getEnchantmentTags();
        for (int i = 0; i < listTag.size(); ++i) {
            String string = listTag.getCompound(i).getString("id");
            int j = listTag.getCompound(i).getInt("lvl");
            Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(string)).ifPresent(enchantment -> enchantmentVisitor.accept((Enchantment)enchantment, j));
        }
    }

    private static void runIterationOnInventory(EnchantmentVisitor enchantmentVisitor, Iterable<ItemStack> iterable) {
        for (ItemStack itemStack : iterable) {
            EnchantmentHelper.runIterationOnItem(enchantmentVisitor, itemStack);
        }
    }

    public static int getDamageProtection(Iterable<ItemStack> iterable, DamageSource damageSource) {
        MutableInt mutableInt = new MutableInt();
        EnchantmentHelper.runIterationOnInventory((enchantment, i) -> mutableInt.add(enchantment.getDamageProtection(i, damageSource)), iterable);
        return mutableInt.intValue();
    }

    public static float getDamageBonus(ItemStack itemStack, MobType mobType) {
        MutableFloat mutableFloat = new MutableFloat();
        EnchantmentHelper.runIterationOnItem((enchantment, i) -> mutableFloat.add(enchantment.getDamageBonus(i, mobType)), itemStack);
        return mutableFloat.floatValue();
    }

    public static float getSweepingDamageRatio(LivingEntity livingEntity) {
        int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, livingEntity);
        if (i > 0) {
            return SweepingEdgeEnchantment.getSweepingDamageRatio(i);
        }
        return 0.0f;
    }

    public static void doPostHurtEffects(LivingEntity livingEntity, Entity entity) {
        EnchantmentVisitor enchantmentVisitor = (enchantment, i) -> enchantment.doPostHurt(livingEntity, entity, i);
        if (livingEntity != null) {
            EnchantmentHelper.runIterationOnInventory(enchantmentVisitor, livingEntity.getAllSlots());
        }
        if (entity instanceof Player) {
            EnchantmentHelper.runIterationOnItem(enchantmentVisitor, livingEntity.getMainHandItem());
        }
    }

    public static void doPostDamageEffects(LivingEntity livingEntity, Entity entity) {
        EnchantmentVisitor enchantmentVisitor = (enchantment, i) -> enchantment.doPostAttack(livingEntity, entity, i);
        if (livingEntity != null) {
            EnchantmentHelper.runIterationOnInventory(enchantmentVisitor, livingEntity.getAllSlots());
        }
        if (livingEntity instanceof Player) {
            EnchantmentHelper.runIterationOnItem(enchantmentVisitor, livingEntity.getMainHandItem());
        }
    }

    public static int getEnchantmentLevel(Enchantment enchantment, LivingEntity livingEntity) {
        Collection<ItemStack> iterable = enchantment.getSlotItems(livingEntity).values();
        if (iterable == null) {
            return 0;
        }
        int i = 0;
        for (ItemStack itemStack : iterable) {
            int j = EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack);
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    public static int getKnockbackBonus(LivingEntity livingEntity) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, livingEntity);
    }

    public static int getFireAspect(LivingEntity livingEntity) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, livingEntity);
    }

    public static int getRespiration(LivingEntity livingEntity) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.RESPIRATION, livingEntity);
    }

    public static int getDepthStrider(LivingEntity livingEntity) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, livingEntity);
    }

    public static int getBlockEfficiency(LivingEntity livingEntity) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, livingEntity);
    }

    public static int getFishingLuckBonus(ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FISHING_LUCK, itemStack);
    }

    public static int getFishingSpeedBonus(ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FISHING_SPEED, itemStack);
    }

    public static int getMobLooting(LivingEntity livingEntity) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.MOB_LOOTING, livingEntity);
    }

    public static boolean hasAquaAffinity(LivingEntity livingEntity) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.AQUA_AFFINITY, livingEntity) > 0;
    }

    public static boolean hasFrostWalker(LivingEntity livingEntity) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.FROST_WALKER, livingEntity) > 0;
    }

    public static boolean hasBindingCurse(ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, itemStack) > 0;
    }

    public static boolean hasVanishingCurse(ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, itemStack) > 0;
    }

    public static int getLoyalty(ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.LOYALTY, itemStack);
    }

    public static int getRiptide(ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.RIPTIDE, itemStack);
    }

    public static boolean hasChanneling(ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.CHANNELING, itemStack) > 0;
    }

    @Nullable
    public static Map.Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment enchantment, LivingEntity livingEntity) {
        Map<EquipmentSlot, ItemStack> map = enchantment.getSlotItems(livingEntity);
        if (map.isEmpty()) {
            return null;
        }
        ArrayList<Map.Entry<EquipmentSlot, ItemStack>> list = Lists.newArrayList();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
            ItemStack itemStack = entry.getValue();
            if (itemStack.isEmpty() || EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack) <= 0) continue;
            list.add(entry);
        }
        return list.isEmpty() ? null : (Map.Entry)list.get(livingEntity.getRandom().nextInt(list.size()));
    }

    public static int getEnchantmentCost(Random random, int i, int j, ItemStack itemStack) {
        Item item = itemStack.getItem();
        int k = item.getEnchantmentValue();
        if (k <= 0) {
            return 0;
        }
        if (j > 15) {
            j = 15;
        }
        int l = random.nextInt(8) + 1 + (j >> 1) + random.nextInt(j + 1);
        if (i == 0) {
            return Math.max(l / 3, 1);
        }
        if (i == 1) {
            return l * 2 / 3 + 1;
        }
        return Math.max(l, j * 2);
    }

    public static ItemStack enchantItem(Random random, ItemStack itemStack, int i, boolean bl) {
        boolean bl2;
        List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(random, itemStack, i, bl);
        boolean bl3 = bl2 = itemStack.getItem() == Items.BOOK;
        if (bl2) {
            itemStack = new ItemStack(Items.ENCHANTED_BOOK);
        }
        for (EnchantmentInstance enchantmentInstance : list) {
            if (bl2) {
                EnchantedBookItem.addEnchantment(itemStack, enchantmentInstance);
                continue;
            }
            itemStack.enchant(enchantmentInstance.enchantment, enchantmentInstance.level);
        }
        return itemStack;
    }

    public static List<EnchantmentInstance> selectEnchantment(Random random, ItemStack itemStack, int i, boolean bl) {
        ArrayList<EnchantmentInstance> list = Lists.newArrayList();
        Item item = itemStack.getItem();
        int j = item.getEnchantmentValue();
        if (j <= 0) {
            return list;
        }
        i += 1 + random.nextInt(j / 4 + 1) + random.nextInt(j / 4 + 1);
        float f = (random.nextFloat() + random.nextFloat() - 1.0f) * 0.15f;
        List<EnchantmentInstance> list2 = EnchantmentHelper.getAvailableEnchantmentResults(i = Mth.clamp(Math.round((float)i + (float)i * f), 1, Integer.MAX_VALUE), itemStack, bl);
        if (!list2.isEmpty()) {
            list.add(WeighedRandom.getRandomItem(random, list2));
            while (random.nextInt(50) <= i) {
                EnchantmentHelper.filterCompatibleEnchantments(list2, Util.lastOf(list));
                if (list2.isEmpty()) break;
                list.add(WeighedRandom.getRandomItem(random, list2));
                i /= 2;
            }
        }
        return list;
    }

    public static void filterCompatibleEnchantments(List<EnchantmentInstance> list, EnchantmentInstance enchantmentInstance) {
        Iterator<EnchantmentInstance> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (enchantmentInstance.enchantment.isCompatibleWith(iterator.next().enchantment)) continue;
            iterator.remove();
        }
    }

    public static boolean isEnchantmentCompatible(Collection<Enchantment> collection, Enchantment enchantment) {
        for (Enchantment enchantment2 : collection) {
            if (enchantment2.isCompatibleWith(enchantment)) continue;
            return false;
        }
        return true;
    }

    public static List<EnchantmentInstance> getAvailableEnchantmentResults(int i, ItemStack itemStack, boolean bl) {
        ArrayList<EnchantmentInstance> list = Lists.newArrayList();
        Item item = itemStack.getItem();
        boolean bl2 = itemStack.getItem() == Items.BOOK;
        block0: for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (enchantment.isTreasureOnly() && !bl || !enchantment.category.canEnchant(item) && !bl2) continue;
            for (int j = enchantment.getMaxLevel(); j > enchantment.getMinLevel() - 1; --j) {
                if (i < enchantment.getMinCost(j) || i > enchantment.getMaxCost(j)) continue;
                list.add(new EnchantmentInstance(enchantment, j));
                continue block0;
            }
        }
        return list;
    }

    @FunctionalInterface
    static interface EnchantmentVisitor {
        public void accept(Enchantment var1, int var2);
    }
}

