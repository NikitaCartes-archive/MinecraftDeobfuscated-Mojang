package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;

class BadOmenMobEffect extends MobEffect {
	protected BadOmenMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int i, int j) {
		return true;
	}

	@Override
	public void applyEffectTick(LivingEntity livingEntity, int i) {
		super.applyEffectTick(livingEntity, i);
		if (livingEntity instanceof ServerPlayer serverPlayer && !livingEntity.isSpectator()) {
			ServerLevel serverLevel = serverPlayer.serverLevel();
			if (serverLevel.getDifficulty() == Difficulty.PEACEFUL) {
				return;
			}

			if (serverLevel.isVillage(livingEntity.blockPosition())) {
				serverLevel.getRaids().createOrExtendRaid(serverPlayer);
			}
		}
	}
}
