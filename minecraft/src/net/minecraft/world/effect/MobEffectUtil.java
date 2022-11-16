package net.minecraft.world.effect;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class MobEffectUtil {
	public static String formatDuration(MobEffectInstance mobEffectInstance, float f) {
		int i = Mth.floor((float)mobEffectInstance.getDuration() * f);
		return StringUtil.formatTickDuration(i);
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

	public static List<ServerPlayer> addEffectToPlayersAround(
		ServerLevel serverLevel, @Nullable Entity entity, Vec3 vec3, double d, MobEffectInstance mobEffectInstance, int i
	) {
		MobEffect mobEffect = mobEffectInstance.getEffect();
		List<ServerPlayer> list = serverLevel.getPlayers(
			serverPlayer -> serverPlayer.gameMode.isSurvival()
					&& (entity == null || !entity.isAlliedTo(serverPlayer))
					&& vec3.closerThan(serverPlayer.position(), d)
					&& (
						!serverPlayer.hasEffect(mobEffect)
							|| serverPlayer.getEffect(mobEffect).getAmplifier() < mobEffectInstance.getAmplifier()
							|| serverPlayer.getEffect(mobEffect).getDuration() < i
					)
		);
		list.forEach(serverPlayer -> serverPlayer.addEffect(new MobEffectInstance(mobEffectInstance), entity));
		return list;
	}
}
