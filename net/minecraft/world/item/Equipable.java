/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface Equipable
extends Vanishable {
    public EquipmentSlot getEquipmentSlot();

    default public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    default public InteractionResultHolder<ItemStack> swapWithEquipmentSlot(Item item, Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
        ItemStack itemStack2 = player.getItemBySlot(equipmentSlot);
        if (EnchantmentHelper.hasBindingCurse(itemStack2) || ItemStack.matches(itemStack, itemStack2)) {
            return InteractionResultHolder.fail(itemStack);
        }
        player.setItemSlot(equipmentSlot, itemStack.copy());
        if (!level.isClientSide()) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
        if (itemStack2.isEmpty()) {
            itemStack.setCount(0);
        } else {
            player.setItemInHand(interactionHand, itemStack2.copy());
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Nullable
    public static Equipable get(ItemStack itemStack) {
        BlockItem blockItem;
        Item item = itemStack.getItem();
        if (item instanceof Equipable) {
            Equipable equipable = (Equipable)((Object)item);
            return equipable;
        }
        FeatureElement featureElement = itemStack.getItem();
        if (featureElement instanceof BlockItem && (featureElement = (blockItem = (BlockItem)featureElement).getBlock()) instanceof Equipable) {
            Equipable equipable2 = (Equipable)((Object)featureElement);
            return equipable2;
        }
        return null;
    }
}

