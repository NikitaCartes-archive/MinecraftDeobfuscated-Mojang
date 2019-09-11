package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class HoneyBottleItem extends Item {
	public HoneyBottleItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		super.finishUsingItem(itemStack, level, livingEntity);
		if (livingEntity instanceof ServerPlayer) {
			ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStack);
			serverPlayer.awardStat(Stats.ITEM_USED.get(this));
		}

		if (!level.isClientSide) {
			livingEntity.removeEffect(MobEffects.POISON);
		}

		return itemStack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : itemStack;
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 40;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.DRINK;
	}

	@Override
	public SoundEvent getDrinkingSound() {
		return SoundEvents.HONEY_DRINK;
	}

	@Override
	public SoundEvent getEatingSound() {
		return SoundEvents.HONEY_DRINK;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		player.startUsingItem(interactionHand);
		return InteractionResultHolder.success(player.getItemInHand(interactionHand));
	}
}
