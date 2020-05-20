/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;

public class EmptyMapItem
extends ComplexItem {
    public EmptyMapItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = MapItem.create(level, Mth.floor(player.getX()), Mth.floor(player.getZ()), (byte)0, true, false);
        ItemStack itemStack2 = player.getItemInHand(interactionHand);
        if (!player.abilities.instabuild) {
            itemStack2.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0f, 1.0f);
        if (itemStack2.isEmpty()) {
            return InteractionResultHolder.success(itemStack);
        }
        if (!player.inventory.add(itemStack.copy())) {
            player.drop(itemStack, false);
        }
        return InteractionResultHolder.success(itemStack2);
    }
}

