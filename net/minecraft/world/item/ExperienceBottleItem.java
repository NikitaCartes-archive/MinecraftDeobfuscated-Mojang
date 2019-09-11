/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ExperienceBottleItem
extends Item {
    public ExperienceBottleItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        level.playSound(null, player.x, player.y, player.z, SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));
        if (!level.isClientSide) {
            ThrownExperienceBottle thrownExperienceBottle = new ThrownExperienceBottle(level, player);
            thrownExperienceBottle.setItem(itemStack);
            thrownExperienceBottle.shootFromRotation(player, player.xRot, player.yRot, -20.0f, 0.7f, 1.0f);
            level.addFreshEntity(thrownExperienceBottle);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.abilities.instabuild) {
            itemStack.shrink(1);
        }
        return InteractionResultHolder.success(itemStack);
    }
}

