package net.minecraft.world.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

class RaidOmenMobEffect extends MobEffect {
	protected RaidOmenMobEffect(MobEffectCategory mobEffectCategory, int i, ParticleOptions particleOptions) {
		super(mobEffectCategory, i, particleOptions);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int i, int j) {
		return i == 1;
	}

	@Override
	public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity livingEntity, int i) {
		if (livingEntity instanceof ServerPlayer serverPlayer && !livingEntity.isSpectator()) {
			BlockPos blockPos = serverPlayer.getRaidOmenPosition();
			if (blockPos != null) {
				serverLevel.getRaids().createOrExtendRaid(serverPlayer, blockPos);
				serverPlayer.clearRaidOmenPosition();
				return false;
			}
		}

		return true;
	}
}
