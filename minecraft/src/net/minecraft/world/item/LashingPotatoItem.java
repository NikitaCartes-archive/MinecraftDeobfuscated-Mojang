package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LashingPotatoHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class LashingPotatoItem extends Item {
	public LashingPotatoItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		LashingPotatoHook lashingPotatoHook = player.grappling;
		if (lashingPotatoHook != null) {
			retrieve(level, player, lashingPotatoHook);
		} else {
			if (!level.isClientSide) {
				itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(interactionHand));
			}

			this.shoot(level, player);
		}

		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
	}

	private void shoot(Level level, Player player) {
		if (!level.isClientSide) {
			level.addFreshEntity(new LashingPotatoHook(level, player));
		}

		player.awardStat(Stats.ITEM_USED.get(this));
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
		player.gameEvent(GameEvent.ITEM_INTERACT_START);
	}

	private static void retrieve(Level level, Player player, LashingPotatoHook lashingPotatoHook) {
		if (!level.isClientSide()) {
			lashingPotatoHook.discard();
			player.grappling = null;
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
		player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
	}
}
