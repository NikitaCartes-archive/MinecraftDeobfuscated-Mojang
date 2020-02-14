package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class EraseMemoryIf<E extends LivingEntity> extends Behavior<E> {
	private final Predicate<E> predicate;
	private final MemoryModuleType<?> memoryType;

	public EraseMemoryIf(Predicate<E> predicate, MemoryModuleType<?> memoryModuleType) {
		super(ImmutableMap.of(memoryModuleType, MemoryStatus.VALUE_PRESENT));
		this.predicate = predicate;
		this.memoryType = memoryModuleType;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
		return this.predicate.test(livingEntity);
	}

	@Override
	protected void start(ServerLevel serverLevel, E livingEntity, long l) {
		livingEntity.getBrain().eraseMemory(this.memoryType);
	}
}
