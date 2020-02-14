package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.IntRange;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CopyMemoryWithExpiry<E extends Mob, T> extends Behavior<E> {
	private final Predicate<E> predicate;
	private final MemoryModuleType<? extends T> sourceMemory;
	private final MemoryModuleType<T> targetMemory;
	private final IntRange durationOfCopy;

	public CopyMemoryWithExpiry(Predicate<E> predicate, MemoryModuleType<? extends T> memoryModuleType, MemoryModuleType<T> memoryModuleType2, IntRange intRange) {
		super(ImmutableMap.of(memoryModuleType2, MemoryStatus.VALUE_PRESENT, memoryModuleType, MemoryStatus.VALUE_ABSENT));
		this.predicate = predicate;
		this.sourceMemory = memoryModuleType;
		this.targetMemory = memoryModuleType2;
		this.durationOfCopy = intRange;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E mob) {
		return this.predicate.test(mob);
	}

	protected void start(ServerLevel serverLevel, E mob, long l) {
		Brain<?> brain = mob.getBrain();
		brain.setMemoryWithExpiry(this.targetMemory, (T)brain.getMemory(this.sourceMemory).get(), l, (long)this.durationOfCopy.randomValue(serverLevel.random));
	}
}
