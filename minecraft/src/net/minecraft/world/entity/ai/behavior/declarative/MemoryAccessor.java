package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.K1;
import java.util.Optional;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public final class MemoryAccessor<F extends K1, Value> {
	private final Brain<?> brain;
	private final MemoryModuleType<Value> memoryType;
	private final App<F, Value> value;

	public MemoryAccessor(Brain<?> brain, MemoryModuleType<Value> memoryModuleType, App<F, Value> app) {
		this.brain = brain;
		this.memoryType = memoryModuleType;
		this.value = app;
	}

	public App<F, Value> value() {
		return this.value;
	}

	public void set(Value object) {
		this.brain.setMemory(this.memoryType, Optional.of(object));
	}

	public void setOrErase(Optional<Value> optional) {
		this.brain.setMemory(this.memoryType, optional);
	}

	public void setWithExpiry(Value object, long l) {
		this.brain.setMemoryWithExpiry(this.memoryType, object, l);
	}

	public void erase() {
		this.brain.eraseMemory(this.memoryType);
	}
}
