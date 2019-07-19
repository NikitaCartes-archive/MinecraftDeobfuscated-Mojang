/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PotionItem
extends Item {
    public PotionItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public ItemStack getDefaultInstance() {
        return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        Player player;
        Player player2 = player = livingEntity instanceof Player ? (Player)livingEntity : null;
        if (player == null || !player.abilities.instabuild) {
            itemStack.shrink(1);
        }
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, itemStack);
        }
        if (!level.isClientSide) {
            List<MobEffectInstance> list = PotionUtils.getMobEffects(itemStack);
            for (MobEffectInstance mobEffectInstance : list) {
                if (mobEffectInstance.getEffect().isInstantenous()) {
                    mobEffectInstance.getEffect().applyInstantenousEffect(player, player, livingEntity, mobEffectInstance.getAmplifier(), 1.0);
                    continue;
                }
                livingEntity.addEffect(new MobEffectInstance(mobEffectInstance));
            }
        }
        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (player == null || !player.abilities.instabuild) {
            if (itemStack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            if (player != null) {
                player.inventory.add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        return itemStack;
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        player.startUsingItem(interactionHand);
        return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, player.getItemInHand(interactionHand));
    }

    @Override
    public String getDescriptionId(ItemStack itemStack) {
        return PotionUtils.getPotion(itemStack).getName(this.getDescriptionId() + ".effect.");
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        PotionUtils.addPotionTooltip(itemStack, list, 1.0f);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean isFoil(ItemStack itemStack) {
        return super.isFoil(itemStack) || !PotionUtils.getMobEffects(itemStack).isEmpty();
    }

    @Override
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        if (this.allowdedIn(creativeModeTab)) {
            for (Potion potion : Registry.POTION) {
                if (potion == Potions.EMPTY) continue;
                nonNullList.add(PotionUtils.setPotion(new ItemStack(this), potion));
            }
        }
    }
}

