/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SaddleItem
extends Item {
    public SaddleItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean interactEnemy(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        if (livingEntity instanceof Pig) {
            Pig pig = (Pig)livingEntity;
            if (pig.isAlive() && !pig.hasSaddle() && !pig.isBaby()) {
                pig.setSaddle(true);
                pig.level.playSound(player, pig.x, pig.y, pig.z, SoundEvents.PIG_SADDLE, SoundSource.NEUTRAL, 0.5f, 1.0f);
                itemStack.shrink(1);
            }
            return true;
        }
        return false;
    }
}

