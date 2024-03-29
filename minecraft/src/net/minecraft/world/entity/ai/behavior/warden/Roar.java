package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;

public class Roar extends Behavior<Warden> {
	private static final int TICKS_BEFORE_PLAYING_ROAR_SOUND = 25;
	private static final int ROAR_ANGER_INCREASE = 20;

	public Roar() {
		super(
			ImmutableMap.of(
				MemoryModuleType.ROAR_TARGET,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.ROAR_SOUND_COOLDOWN,
				MemoryStatus.REGISTERED,
				MemoryModuleType.ROAR_SOUND_DELAY,
				MemoryStatus.REGISTERED
			),
			WardenAi.ROAR_DURATION
		);
	}

	protected void start(ServerLevel serverLevel, Warden warden, long l) {
		Brain<Warden> brain = warden.getBrain();
		brain.setMemoryWithExpiry(MemoryModuleType.ROAR_SOUND_DELAY, Unit.INSTANCE, 25L);
		brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		LivingEntity livingEntity = (LivingEntity)warden.getBrain().getMemory(MemoryModuleType.ROAR_TARGET).get();
		BehaviorUtils.lookAtEntity(warden, livingEntity);
		warden.setPose(Pose.ROARING);
		warden.increaseAngerAt(livingEntity, 20, false);
	}

	protected boolean canStillUse(ServerLevel serverLevel, Warden warden, long l) {
		return true;
	}

	protected void tick(ServerLevel serverLevel, Warden warden, long l) {
		if (!warden.getBrain().hasMemoryValue(MemoryModuleType.ROAR_SOUND_DELAY) && !warden.getBrain().hasMemoryValue(MemoryModuleType.ROAR_SOUND_COOLDOWN)) {
			warden.getBrain().setMemoryWithExpiry(MemoryModuleType.ROAR_SOUND_COOLDOWN, Unit.INSTANCE, (long)(WardenAi.ROAR_DURATION - 25));
			warden.playSound(SoundEvents.WARDEN_ROAR, 3.0F, 1.0F);
		}
	}

	protected void stop(ServerLevel serverLevel, Warden warden, long l) {
		if (warden.hasPose(Pose.ROARING)) {
			warden.setPose(Pose.STANDING);
		}

		warden.getBrain().getMemory(MemoryModuleType.ROAR_TARGET).ifPresent(warden::setAttackTarget);
		warden.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
	}
}
