package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;

public class EggItem extends Item implements ProjectileItem {
	public EggItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		level.playSound(
			null, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
		);
		if (!level.isClientSide) {
			ThrownEgg thrownEgg = new ThrownEgg(level, player);
			thrownEgg.setItem(itemStack);
			thrownEgg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
			level.addFreshEntity(thrownEgg);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		itemStack.consume(1, player);
		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
		ThrownEgg thrownEgg = new ThrownEgg(level, position.x(), position.y(), position.z());
		thrownEgg.setItem(itemStack);
		return thrownEgg;
	}
}
