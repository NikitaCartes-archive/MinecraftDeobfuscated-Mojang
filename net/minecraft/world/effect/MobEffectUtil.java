/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.effect;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class MobEffectUtil {
    public static Component formatDuration(MobEffectInstance mobEffectInstance, float f) {
        if (mobEffectInstance.isInfiniteDuration()) {
            return Component.translatable("effect.duration.infinite");
        }
        int i = Mth.floor((float)mobEffectInstance.getDuration() * f);
        return Component.literal(StringUtil.formatTickDuration(i));
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

    public static List<ServerPlayer> addEffectToPlayersAround(ServerLevel serverLevel, @Nullable Entity entity, Vec3 vec3, double d, MobEffectInstance mobEffectInstance, int i) {
        MobEffect mobEffect = mobEffectInstance.getEffect();
        List<ServerPlayer> list = serverLevel.getPlayers(serverPlayer -> !(!serverPlayer.gameMode.isSurvival() || entity != null && entity.isAlliedTo((Entity)serverPlayer) || !vec3.closerThan(serverPlayer.position(), d) || serverPlayer.hasEffect(mobEffect) && serverPlayer.getEffect(mobEffect).getAmplifier() >= mobEffectInstance.getAmplifier() && !serverPlayer.getEffect(mobEffect).endsWithin(i - 1)));
        list.forEach(serverPlayer -> serverPlayer.addEffect(new MobEffectInstance(mobEffectInstance), entity));
        return list;
    }
}

