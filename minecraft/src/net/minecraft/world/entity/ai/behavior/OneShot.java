package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public abstract class OneShot<E extends LivingEntity> extends Behavior<E> {
	public OneShot(Map<MemoryModuleType<?>, MemoryStatus> map) {
		super(map);
	}

	public OneShot(Map<MemoryModuleType<?>, MemoryStatus> map, int i) {
		super(map, i);
	}

	public OneShot(Map<MemoryModuleType<?>, MemoryStatus> map, int i, int j) {
		super(map, i, j);
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
		return true;
	}

	@Override
	protected abstract void start(ServerLevel serverLevel, E livingEntity, long l);

	@Override
	protected final boolean canStillUse(ServerLevel serverLevel, E livingEntity, long l) {
		return false;
	}

	@Override
	protected final void tick(ServerLevel serverLevel, E livingEntity, long l) {
	}

	@Override
	protected void stop(ServerLevel serverLevel, E livingEntity, long l) {
	}

	@Override
	protected final boolean timedOut(long l) {
		return false;
	}
}
