package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopRoaringIfTargetInvalid extends Behavior<Warden> {
	public StopRoaringIfTargetInvalid() {
		super(ImmutableMap.of(MemoryModuleType.ROAR_TARGET, MemoryStatus.VALUE_PRESENT));
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Warden warden) {
		Brain<Warden> brain = warden.getBrain();
		return !WardenAi.isValidRoarTarget((LivingEntity)brain.getMemory(MemoryModuleType.ROAR_TARGET).get());
	}

	protected void start(ServerLevel serverLevel, Warden warden, long l) {
		Brain<Warden> brain = warden.getBrain();
		LivingEntity livingEntity = (LivingEntity)brain.getMemory(MemoryModuleType.ROAR_TARGET).get();
		warden.getSuspectTracking().clearSuspicion(livingEntity.getUUID());
		brain.eraseMemory(MemoryModuleType.ROAR_TARGET);
		warden.setPose(Pose.STANDING);
	}
}
