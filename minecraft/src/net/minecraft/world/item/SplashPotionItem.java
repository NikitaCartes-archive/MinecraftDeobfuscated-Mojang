package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;

public class SplashPotionItem extends PotionItem {
	public SplashPotionItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		ItemStack itemStack2 = player.abilities.instabuild ? itemStack.copy() : itemStack.split(1);
		level.playSound(null, player.x, player.y, player.z, SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
		if (!level.isClientSide) {
			ThrownPotion thrownPotion = new ThrownPotion(level, player);
			thrownPotion.setItem(itemStack2);
			thrownPotion.shootFromRotation(player, player.xRot, player.yRot, -20.0F, 0.5F, 1.0F);
			level.addFreshEntity(thrownPotion);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
	}
}
