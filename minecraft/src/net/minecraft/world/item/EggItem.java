package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;

public class EggItem extends Item {
	public EggItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		level.playSound(
			null, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F)
		);
		if (!level.isClientSide) {
			ThrownEgg thrownEgg = new ThrownEgg(level, player);
			thrownEgg.setItem(itemStack);
			thrownEgg.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 1.0F);
			level.addFreshEntity(thrownEgg);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		if (!player.abilities.instabuild) {
			itemStack.shrink(1);
		}

		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}
}
