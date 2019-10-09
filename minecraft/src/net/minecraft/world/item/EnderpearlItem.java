package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;

public class EnderpearlItem extends Item {
	public EnderpearlItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		level.playSound(
			null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F)
		);
		player.getCooldowns().addCooldown(this, 20);
		if (!level.isClientSide) {
			ThrownEnderpearl thrownEnderpearl = new ThrownEnderpearl(level, player);
			thrownEnderpearl.setItem(itemStack);
			thrownEnderpearl.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 1.0F);
			level.addFreshEntity(thrownEnderpearl);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		if (!player.abilities.instabuild) {
			itemStack.shrink(1);
		}

		return InteractionResultHolder.success(itemStack);
	}
}
