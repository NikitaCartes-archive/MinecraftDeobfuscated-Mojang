package net.minecraft.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ChorusFruitItem extends Item {
	public ChorusFruitItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		ItemStack itemStack2 = super.finishUsingItem(itemStack, level, livingEntity);
		if (!level.isClientSide) {
			double d = livingEntity.getX();
			double e = livingEntity.getY();
			double f = livingEntity.getZ();

			for (int i = 0; i < 16; i++) {
				double g = livingEntity.getX() + (livingEntity.getRandom().nextDouble() - 0.5) * 16.0;
				double h = Mth.clamp(
					livingEntity.getY() + (double)(livingEntity.getRandom().nextInt(16) - 8),
					(double)level.getMinBuildHeight(),
					(double)(level.getMinBuildHeight() + ((ServerLevel)level).getLogicalHeight() - 1)
				);
				double j = livingEntity.getZ() + (livingEntity.getRandom().nextDouble() - 0.5) * 16.0;
				if (livingEntity.isPassenger()) {
					livingEntity.stopRiding();
				}

				if (livingEntity.randomTeleport(g, h, j, true)) {
					SoundEvent soundEvent = livingEntity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
					level.playSound(null, d, e, f, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
					livingEntity.playSound(soundEvent, 1.0F, 1.0F);
					break;
				}
			}

			if (livingEntity instanceof Player) {
				((Player)livingEntity).getCooldowns().addCooldown(this, 20);
			}
		}

		return itemStack2;
	}
}
