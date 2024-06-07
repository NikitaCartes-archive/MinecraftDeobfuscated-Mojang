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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class ChorusFruitItem extends Item {
	public ChorusFruitItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		ItemStack itemStack2 = super.finishUsingItem(itemStack, level, livingEntity);
		if (!level.isClientSide) {
			for (int i = 0; i < 16; i++) {
				double d = livingEntity.getX() + (livingEntity.getRandom().nextDouble() - 0.5) * 16.0;
				double e = Mth.clamp(
					livingEntity.getY() + (double)(livingEntity.getRandom().nextInt(16) - 8),
					(double)level.getMinBuildHeight(),
					(double)(level.getMinBuildHeight() + ((ServerLevel)level).getLogicalHeight() - 1)
				);
				double f = livingEntity.getZ() + (livingEntity.getRandom().nextDouble() - 0.5) * 16.0;
				if (livingEntity.isPassenger()) {
					livingEntity.stopRiding();
				}

				Vec3 vec3 = livingEntity.position();
				if (livingEntity.randomTeleport(d, e, f, true)) {
					level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(livingEntity));
					SoundSource soundSource;
					SoundEvent soundEvent;
					if (livingEntity instanceof Fox) {
						soundEvent = SoundEvents.FOX_TELEPORT;
						soundSource = SoundSource.NEUTRAL;
					} else {
						soundEvent = SoundEvents.CHORUS_FRUIT_TELEPORT;
						soundSource = SoundSource.PLAYERS;
					}

					level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), soundEvent, soundSource);
					livingEntity.resetFallDistance();
					break;
				}
			}

			if (livingEntity instanceof Player player) {
				player.resetCurrentImpulseContext();
				player.getCooldowns().addCooldown(this, 20);
			}
		}

		return itemStack2;
	}
}
