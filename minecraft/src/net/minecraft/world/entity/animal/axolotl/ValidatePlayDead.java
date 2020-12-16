package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class ValidatePlayDead extends Behavior<Axolotl> {
	public ValidatePlayDead() {
		super(ImmutableMap.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryStatus.VALUE_PRESENT));
	}

	protected void start(ServerLevel serverLevel, Axolotl axolotl, long l) {
		Brain<Axolotl> brain = axolotl.getBrain();
		int i = (Integer)brain.getMemory(MemoryModuleType.PLAY_DEAD_TICKS).get();
		if (i <= 0) {
			brain.eraseMemory(MemoryModuleType.PLAY_DEAD_TICKS);
			brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
			brain.useDefaultActivity();
		} else {
			brain.setMemory(MemoryModuleType.PLAY_DEAD_TICKS, i - 1);
		}
	}
}
