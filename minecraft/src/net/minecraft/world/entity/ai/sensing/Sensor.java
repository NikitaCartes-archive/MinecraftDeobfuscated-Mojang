package net.minecraft.world.entity.ai.sensing;

import java.util.Random;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public abstract class Sensor<E extends LivingEntity> {
	private static final Random RANDOM = new Random();
	private final int scanRate;
	private long timeToTick;

	public Sensor(int i) {
		this.scanRate = i;
		this.timeToTick = (long)RANDOM.nextInt(i);
	}

	public Sensor() {
		this(20);
	}

	public final void tick(ServerLevel serverLevel, E livingEntity) {
		if (--this.timeToTick <= 0L) {
			this.timeToTick = (long)this.scanRate;
			this.doTick(serverLevel, livingEntity);
		}
	}

	protected abstract void doTick(ServerLevel serverLevel, E livingEntity);

	public abstract Set<MemoryModuleType<?>> requires();
}
