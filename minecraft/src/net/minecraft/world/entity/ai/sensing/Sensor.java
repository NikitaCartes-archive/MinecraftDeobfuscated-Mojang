package net.minecraft.world.entity.ai.sensing;

import java.util.Random;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class Sensor<E extends LivingEntity> {
	private static final Random RANDOM = new Random();
	private static final int DEFAULT_SCAN_RATE = 20;
	protected static final int TARGETING_RANGE = 16;
	private static final TargetingConditions TARGET_CONDITIONS = TargetingConditions.forNonCombat().range(16.0);
	private static final TargetingConditions TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forNonCombat()
		.range(16.0)
		.ignoreInvisibilityTesting();
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

	protected static boolean isEntityTargetable(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return livingEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, livingEntity2)
			? TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(livingEntity, livingEntity2)
			: TARGET_CONDITIONS.test(livingEntity, livingEntity2);
	}
}
