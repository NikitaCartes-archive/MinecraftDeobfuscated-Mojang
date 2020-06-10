/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SaddleItem
extends Item {
    public SaddleItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        Saddleable saddleable;
        if (livingEntity instanceof Saddleable && livingEntity.isAlive() && !(saddleable = (Saddleable)((Object)livingEntity)).isSaddled() && saddleable.isSaddleable()) {
            if (!player.level.isClientSide) {
                saddleable.equipSaddle(SoundSource.NEUTRAL);
                itemStack.shrink(1);
            }
            return InteractionResult.sidedSuccess(player.level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}

