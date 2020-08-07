package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;

public class SnowballItem extends Item {
	public SnowballItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		level.playSound(
			null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F)
		);
		if (!level.isClientSide) {
			Snowball snowball = new Snowball(level, player);
			snowball.setItem(itemStack);
			snowball.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 1.0F);
			level.addFreshEntity(snowball);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		if (!player.abilities.instabuild) {
			itemStack.shrink(1);
		}

		player.getCooldowns().addCooldown(this, 4);
		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}
}
