package net.minecraft.world.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.flag.FeatureFlags;

class BadOmenMobEffect extends MobEffect {
	protected BadOmenMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int i, int j) {
		return true;
	}

	@Override
	public boolean applyEffectTick(LivingEntity livingEntity, int i) {
		if (livingEntity instanceof ServerPlayer serverPlayer && !serverPlayer.isSpectator()) {
			ServerLevel serverLevel = serverPlayer.serverLevel();
			if (!serverLevel.enabledFeatures().contains(FeatureFlags.UPDATE_1_21)) {
				return this.legacyApplyEffectTick(serverPlayer, serverLevel);
			}

			if (serverLevel.getDifficulty() != Difficulty.PEACEFUL && serverLevel.isVillage(serverPlayer.blockPosition())) {
				Raid raid = serverLevel.getRaidAt(serverPlayer.blockPosition());
				if (raid == null || raid.getRaidOmenLevel() < raid.getMaxRaidOmenLevel()) {
					serverPlayer.addEffect(new MobEffectInstance(MobEffects.RAID_OMEN, 600, i));
					serverPlayer.setRaidOmenPosition(serverPlayer.blockPosition());
					return false;
				}
			}
		}

		return true;
	}

	private boolean legacyApplyEffectTick(ServerPlayer serverPlayer, ServerLevel serverLevel) {
		BlockPos blockPos = serverPlayer.blockPosition();
		return serverLevel.getDifficulty() != Difficulty.PEACEFUL && serverLevel.isVillage(blockPos)
			? serverLevel.getRaids().createOrExtendRaid(serverPlayer, blockPos) == null
			: true;
	}
}
