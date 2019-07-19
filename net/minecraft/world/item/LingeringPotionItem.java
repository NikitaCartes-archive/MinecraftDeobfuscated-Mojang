/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LingeringPotionItem
extends PotionItem {
    public LingeringPotionItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        PotionUtils.addPotionTooltip(itemStack, list, 0.25f);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        ItemStack itemStack2 = player.abilities.instabuild ? itemStack.copy() : itemStack.split(1);
        level.playSound(null, player.x, player.y, player.z, SoundEvents.LINGERING_POTION_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));
        if (!level.isClientSide) {
            ThrownPotion thrownPotion = new ThrownPotion(level, player);
            thrownPotion.setItem(itemStack2);
            thrownPotion.shootFromRotation(player, player.xRot, player.yRot, -20.0f, 0.5f, 1.0f);
            level.addFreshEntity(thrownPotion);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStack);
    }
}

