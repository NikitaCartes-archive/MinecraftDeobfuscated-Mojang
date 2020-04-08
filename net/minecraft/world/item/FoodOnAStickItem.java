/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FoodOnAStickItem<T extends Entity>
extends Item {
    private final EntityType<T> canInteractWith;
    private final int consumeItemDamage;

    public FoodOnAStickItem(Item.Properties properties, EntityType<T> entityType, int i) {
        super(properties);
        this.canInteractWith = entityType;
        this.consumeItemDamage = i;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player2, InteractionHand interactionHand) {
        ItemSteerable itemSteerable;
        ItemStack itemStack = player2.getItemInHand(interactionHand);
        if (level.isClientSide) {
            return InteractionResultHolder.pass(itemStack);
        }
        Entity entity = player2.getVehicle();
        if (player2.isPassenger() && entity instanceof ItemSteerable && entity.getType() == this.canInteractWith && (itemSteerable = (ItemSteerable)((Object)entity)).boost()) {
            itemStack.hurtAndBreak(this.consumeItemDamage, player2, player -> player.broadcastBreakEvent(interactionHand));
            player2.swing(interactionHand, true);
            if (itemStack.isEmpty()) {
                ItemStack itemStack2 = new ItemStack(Items.FISHING_ROD);
                itemStack2.setTag(itemStack.getTag());
                return InteractionResultHolder.consume(itemStack2);
            }
            return InteractionResultHolder.consume(itemStack);
        }
        player2.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.pass(itemStack);
    }
}

