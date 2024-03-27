package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

public class OminousBottleItem extends Item {
	private static final int DRINK_DURATION = 32;
	public static final int EFFECT_DURATION = 120000;
	public static final int MIN_AMPLIFIER = 0;
	public static final int MAX_AMPLIFIER = 4;

	public OminousBottleItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		if (livingEntity instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStack);
			serverPlayer.awardStat(Stats.ITEM_USED.get(this));
		}

		itemStack.consume(1, livingEntity);
		if (!level.isClientSide) {
			level.playSound(null, livingEntity.blockPosition(), SoundEvents.OMINOUS_BOTTLE_DISPOSE, livingEntity.getSoundSource(), 1.0F, 1.0F);
			Integer integer = itemStack.getOrDefault(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, Integer.valueOf(0));
			livingEntity.removeEffect(MobEffects.BAD_OMEN);
			livingEntity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 120000, integer, false, false, true));
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
		return ItemUtils.startUsingInstantly(level, player, interactionHand);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, level, list, tooltipFlag);
		Integer integer = itemStack.getOrDefault(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, Integer.valueOf(0));
		List<MobEffectInstance> list2 = List.of(new MobEffectInstance(MobEffects.BAD_OMEN, 120000, integer, false, false, true));
		PotionContents.addPotionTooltip(list2, list::add, 1.0F, level == null ? 20.0F : level.tickRateManager().tickrate());
	}
}
