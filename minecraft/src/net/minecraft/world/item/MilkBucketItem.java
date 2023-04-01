package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class MilkBucketItem extends SolidBucketItem {
	private static final int DRINK_DURATION = 32;

	public MilkBucketItem(Item.Properties properties) {
		super(Blocks.CHEESE, SoundEvents.FUNGUS_PLACE, properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		return !Rules.INSTACHEESE.get() ? InteractionResult.PASS : super.useOn(useOnContext);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		if (livingEntity instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStack);
			serverPlayer.awardStat(Stats.ITEM_USED.get(this));
		}

		if (livingEntity instanceof Player && !((Player)livingEntity).getAbilities().instabuild) {
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
		return ItemUtils.startUsingInstantly(level, player, interactionHand);
	}
}
