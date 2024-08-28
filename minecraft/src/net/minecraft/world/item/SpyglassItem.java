package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SpyglassItem extends Item {
	public static final int USE_DURATION = 1200;
	public static final float ZOOM_FOV_MODIFIER = 0.1F;

	public SpyglassItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
		return 1200;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
		return ItemUseAnimation.SPYGLASS;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
		player.awardStat(Stats.ITEM_USED.get(this));
		return ItemUtils.startUsingInstantly(level, player, interactionHand);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		this.stopUsing(livingEntity);
		return itemStack;
	}

	@Override
	public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
		this.stopUsing(livingEntity);
		return true;
	}

	private void stopUsing(LivingEntity livingEntity) {
		livingEntity.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
	}
}
