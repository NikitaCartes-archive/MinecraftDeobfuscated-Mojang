package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MilkBucketItem extends Item {
	public MilkBucketItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		if (livingEntity instanceof ServerPlayer) {
			ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStack);
			serverPlayer.awardStat(Stats.ITEM_USED.get(this));
		}

		if (livingEntity instanceof Player && !((Player)livingEntity).abilities.instabuild) {
			itemStack.shrink(1);
		}

		if (!level.isClientSide) {
			livingEntity.removeAllEffects();
		}

		return itemStack.isEmpty() ? new ItemStack(Items.BUCKET) : itemStack;
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
		return InteractionResultHolder.success(player.getItemInHand(interactionHand));
	}
}
