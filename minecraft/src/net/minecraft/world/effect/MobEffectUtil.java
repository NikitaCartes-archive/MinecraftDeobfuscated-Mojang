package net.minecraft.world.effect;

import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;

public final class MobEffectUtil {
	public static String formatDuration(MobEffectInstance mobEffectInstance, float f) {
		if (mobEffectInstance.isNoCounter()) {
			return "**:**";
		} else {
			int i = Mth.floor((float)mobEffectInstance.getDuration() * f);
			return StringUtil.formatTickDuration(i);
		}
	}

	public static boolean hasDigSpeed(LivingEntity livingEntity) {
		return livingEntity.hasEffect(MobEffects.DIG_SPEED) || livingEntity.hasEffect(MobEffects.CONDUIT_POWER);
	}

	public static int getDigSpeedAmplification(LivingEntity livingEntity) {
		int i = 0;
		int j = 0;
		if (livingEntity.hasEffect(MobEffects.DIG_SPEED)) {
			i = livingEntity.getEffect(MobEffects.DIG_SPEED).getAmplifier();
		}

		if (livingEntity.hasEffect(MobEffects.CONDUIT_POWER)) {
			j = livingEntity.getEffect(MobEffects.CONDUIT_POWER).getAmplifier();
		}

		return Math.max(i, j);
	}

	public static boolean hasWaterBreathing(LivingEntity livingEntity) {
		return livingEntity.hasEffect(MobEffects.WATER_BREATHING) || livingEntity.hasEffect(MobEffects.CONDUIT_POWER);
	}
}
