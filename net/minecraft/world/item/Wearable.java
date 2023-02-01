/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;

public interface Wearable
extends Vanishable {
    default public InteractionResultHolder<ItemStack> swapWithEquipmentSlot(Item item, Level level, Player player, InteractionHand interactionHand) {
        EquipmentSlot equipmentSlot;
        ItemStack itemStack2;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (ItemStack.matches(itemStack, itemStack2 = player.getItemBySlot(equipmentSlot = Mob.getEquipmentSlotForItem(itemStack)))) {
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
}

