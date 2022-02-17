package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetWardenLookTarget extends Behavior<Warden> {
	public static final int RECENTLY_HEARD_ENTITY_CUTOFF = 40;

	public SetWardenLookTarget() {
		super(
			ImmutableMap.of(
				MemoryModuleType.LAST_DISTURBANCE,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.DISTURBANCE_LOCATION,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_ABSENT
			)
		);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Warden warden) {
		Brain<Warden> brain = warden.getBrain();
		long l = (Long)brain.getMemory(MemoryModuleType.LAST_DISTURBANCE).get();
		return serverLevel.getGameTime() - l < 40L;
	}

	protected void start(ServerLevel serverLevel, Warden warden, long l) {
		Brain<Warden> brain = warden.getBrain();
		brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker((BlockPos)brain.getMemory(MemoryModuleType.DISTURBANCE_LOCATION).get()));
	}
}
