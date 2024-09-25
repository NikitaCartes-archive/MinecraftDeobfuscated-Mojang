package net.minecraft.world.entity.ai.sensing;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class Sensor<E extends LivingEntity> {
	private static final RandomSource RANDOM = RandomSource.createThreadSafe();
	private static final int DEFAULT_SCAN_RATE = 20;
	private static final int DEFAULT_TARGETING_RANGE = 16;
	private static final TargetingConditions TARGET_CONDITIONS = TargetingConditions.forNonCombat().range(16.0);
	private static final TargetingConditions TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forNonCombat()
		.range(16.0)
		.ignoreInvisibilityTesting();
	private static final TargetingConditions ATTACK_TARGET_CONDITIONS = TargetingConditions.forCombat().range(16.0);
	private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forCombat()
		.range(16.0)
		.ignoreInvisibilityTesting();
	private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT = TargetingConditions.forCombat().range(16.0).ignoreLineOfSight();
	private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT = TargetingConditions.forCombat()
		.range(16.0)
		.ignoreLineOfSight()
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
			this.updateTargetingConditionRanges(livingEntity);
			this.doTick(serverLevel, livingEntity);
		}
	}

	private void updateTargetingConditionRanges(E livingEntity) {
		double d = livingEntity.getAttributeValue(Attributes.FOLLOW_RANGE);
		TARGET_CONDITIONS.range(d);
		TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.range(d);
		ATTACK_TARGET_CONDITIONS.range(d);
		ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.range(d);
		ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.range(d);
		ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.range(d);
	}

	protected abstract void doTick(ServerLevel serverLevel, E livingEntity);

	public abstract Set<MemoryModuleType<?>> requires();

	public static boolean isEntityTargetable(ServerLevel serverLevel, LivingEntity livingEntity, LivingEntity livingEntity2) {
		return livingEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, livingEntity2)
			? TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(serverLevel, livingEntity, livingEntity2)
			: TARGET_CONDITIONS.test(serverLevel, livingEntity, livingEntity2);
	}

	public static boolean isEntityAttackable(ServerLevel serverLevel, LivingEntity livingEntity, LivingEntity livingEntity2) {
		return livingEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, livingEntity2)
			? ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(serverLevel, livingEntity, livingEntity2)
			: ATTACK_TARGET_CONDITIONS.test(serverLevel, livingEntity, livingEntity2);
	}

	public static BiPredicate<ServerLevel, LivingEntity> wasEntityAttackableLastNTicks(LivingEntity livingEntity, int i) {
		return rememberPositives(i, (serverLevel, livingEntity2) -> isEntityAttackable(serverLevel, livingEntity, livingEntity2));
	}

	public static boolean isEntityAttackableIgnoringLineOfSight(ServerLevel serverLevel, LivingEntity livingEntity, LivingEntity livingEntity2) {
		return livingEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, livingEntity2)
			? ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.test(serverLevel, livingEntity, livingEntity2)
			: ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.test(serverLevel, livingEntity, livingEntity2);
	}

	static <T, U> BiPredicate<T, U> rememberPositives(int i, BiPredicate<T, U> biPredicate) {
		AtomicInteger atomicInteger = new AtomicInteger(0);
		return (object, object2) -> {
			if (biPredicate.test(object, object2)) {
				atomicInteger.set(i);
				return true;
			} else {
				return atomicInteger.decrementAndGet() >= 0;
			}
		};
	}
}
