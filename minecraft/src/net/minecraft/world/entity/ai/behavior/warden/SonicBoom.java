package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public class SonicBoom extends Behavior<Warden> {
	private static final int DISTANCE_XZ = 15;
	private static final int DISTANCE_Y = 20;
	private static final double KNOCKBACK_VERTICAL = 0.5;
	private static final double KNOCKBACK_HORIZONTAL = 2.5;
	public static final int COOLDOWN = 40;
	private static final int TICKS_BEFORE_PLAYING_SOUND = Mth.ceil(34.0);
	private static final int DURATION = Mth.ceil(60.0F);

	public SonicBoom() {
		super(
			ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.SONIC_BOOM_COOLDOWN,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN,
				MemoryStatus.REGISTERED,
				MemoryModuleType.SONIC_BOOM_SOUND_DELAY,
				MemoryStatus.REGISTERED
			),
			DURATION
		);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Warden warden) {
		return warden.closerThan((Entity)warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get(), 15.0, 20.0);
	}

	protected boolean canStillUse(ServerLevel serverLevel, Warden warden, long l) {
		return true;
	}

	protected void start(ServerLevel serverLevel, Warden warden, long l) {
		warden.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)DURATION);
		warden.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_DELAY, Unit.INSTANCE, (long)TICKS_BEFORE_PLAYING_SOUND);
		serverLevel.broadcastEntityEvent(warden, (byte)62);
		warden.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 1.0F);
	}

	protected void tick(ServerLevel serverLevel, Warden warden, long l) {
		warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(livingEntity -> warden.getLookControl().setLookAt(livingEntity.position()));
		if (!warden.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_DELAY)
			&& !warden.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN)) {
			warden.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, Unit.INSTANCE, (long)(DURATION - TICKS_BEFORE_PLAYING_SOUND));
			warden.getBrain()
				.getMemory(MemoryModuleType.ATTACK_TARGET)
				.filter(warden::canTargetEntity)
				.filter(livingEntity -> warden.closerThan(livingEntity, 15.0, 20.0))
				.ifPresent(livingEntity -> {
					Vec3 vec3 = warden.position().add(warden.getAttachments().get(EntityAttachment.WARDEN_CHEST, 0, warden.getYRot()));
					Vec3 vec32 = livingEntity.getEyePosition().subtract(vec3);
					Vec3 vec33 = vec32.normalize();
					int i = Mth.floor(vec32.length()) + 7;

					for (int j = 1; j < i; j++) {
						Vec3 vec34 = vec3.add(vec33.scale((double)j));
						serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, vec34.x, vec34.y, vec34.z, 1, 0.0, 0.0, 0.0, 0.0);
					}

					warden.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
					livingEntity.hurt(serverLevel.damageSources().sonicBoom(warden), 10.0F);
					double d = 0.5 * (1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
					double e = 2.5 * (1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
					livingEntity.push(vec33.x() * e, vec33.y() * d, vec33.z() * e);
				});
		}
	}

	protected void stop(ServerLevel serverLevel, Warden warden, long l) {
		setCooldown(warden, 40);
	}

	public static void setCooldown(LivingEntity livingEntity, int i) {
		livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_COOLDOWN, Unit.INSTANCE, (long)i);
	}
}
