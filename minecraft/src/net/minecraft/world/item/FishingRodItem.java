package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class FishingRodItem extends Item implements Vanishable {
	public FishingRodItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (player.fishing != null) {
			if (!level.isClientSide) {
				int i = player.fishing.retrieve(itemStack);
				itemStack.hurtAndBreak(i, player, playerx -> playerx.broadcastBreakEvent(interactionHand));
			}

			level.playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.FISHING_BOBBER_RETRIEVE,
				SoundSource.NEUTRAL,
				1.0F,
				0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
			);
			player.gameEvent(GameEvent.FISHING_ROD_REEL_IN);
		} else {
			level.playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.FISHING_BOBBER_THROW,
				SoundSource.NEUTRAL,
				0.5F,
				0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
			);
			if (!level.isClientSide) {
				int i = EnchantmentHelper.getFishingSpeedBonus(itemStack);
				int j = EnchantmentHelper.getFishingLuckBonus(itemStack);
				level.addFreshEntity(new FishingHook(player, level, j, i));
			}

			player.awardStat(Stats.ITEM_USED.get(this));
			player.gameEvent(GameEvent.FISHING_ROD_CAST);
		}

		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}
}
