package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;

public class Sniffing<E extends Warden> extends Behavior<E> {
	private static final double ANGER_FROM_SNIFFING_MAX_DISTANCE_XZ = 6.0;
	private static final double ANGER_FROM_SNIFFING_MAX_DISTANCE_Y = 20.0;

	public Sniffing(int i) {
		super(
			ImmutableMap.of(
				MemoryModuleType.IS_SNIFFING,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.NEAREST_ATTACKABLE,
				MemoryStatus.REGISTERED,
				MemoryModuleType.DISTURBANCE_LOCATION,
				MemoryStatus.REGISTERED,
				MemoryModuleType.SNIFF_COOLDOWN,
				MemoryStatus.REGISTERED
			),
			i
		);
	}

	protected boolean canStillUse(ServerLevel serverLevel, E warden, long l) {
		return true;
	}

	protected void start(ServerLevel serverLevel, E warden, long l) {
		warden.playSound(SoundEvents.WARDEN_SNIFF, 5.0F, 1.0F);
	}

	protected void stop(ServerLevel serverLevel, E warden, long l) {
		if (warden.hasPose(Pose.SNIFFING)) {
			warden.setPose(Pose.STANDING);
		}

		warden.getBrain().eraseMemory(MemoryModuleType.IS_SNIFFING);
		warden.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).filter(warden::canTargetEntity).ifPresent(livingEntity -> {
			if (warden.closerThan(livingEntity, 6.0, 20.0)) {
				warden.increaseAngerAt(livingEntity);
			}

			if (!warden.getBrain().hasMemoryValue(MemoryModuleType.DISTURBANCE_LOCATION)) {
				WardenAi.setDisturbanceLocation(warden, livingEntity.blockPosition());
			}
		});
	}
}
