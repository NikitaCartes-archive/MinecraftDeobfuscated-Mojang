package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;

public class Digging<E extends Warden> extends Behavior<E> {
	public Digging(int i) {
		super(
			ImmutableMap.of(
				MemoryModuleType.IS_DIGGING,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED
			),
			i
		);
	}

	protected boolean canStillUse(ServerLevel serverLevel, E warden, long l) {
		return this.checkExtraStartConditions(serverLevel, warden);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E warden) {
		return (Boolean)warden.getBrain().getMemory(MemoryModuleType.IS_DIGGING).orElse(false);
	}

	protected void start(ServerLevel serverLevel, E warden, long l) {
		warden.setPose(Pose.DIGGING);
		warden.playSound(SoundEvents.WARDEN_DIG, 5.0F, 1.0F);
	}

	protected void stop(ServerLevel serverLevel, E warden, long l) {
		warden.remove(Entity.RemovalReason.DISCARDED);
	}
}
