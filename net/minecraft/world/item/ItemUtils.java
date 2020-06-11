/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemUtils {
    public static InteractionResultHolder<ItemStack> useDrink(Level level, Player player, InteractionHand interactionHand) {
        player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(player.getItemInHand(interactionHand));
    }

    public static ItemStack createBucketResult(ItemStack itemStack, Player player, ItemStack itemStack2) {
        if (player.abilities.instabuild) {
            if (!player.inventory.contains(itemStack2)) {
                player.inventory.add(itemStack2);
            }
            return itemStack;
        }
        itemStack.shrink(1);
        if (itemStack.isEmpty()) {
            return itemStack2;
        }
        if (!player.inventory.add(itemStack2)) {
            player.drop(itemStack2, false);
        }
        return itemStack;
    }
}

