package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

@Deprecated
public class SetEntityLookTargetSometimes {
	public static BehaviorControl<LivingEntity> create(float f, UniformInt uniformInt) {
		return create(f, uniformInt, livingEntity -> true);
	}

	public static BehaviorControl<LivingEntity> create(EntityType<?> entityType, float f, UniformInt uniformInt) {
		return create(f, uniformInt, livingEntity -> entityType.equals(livingEntity.getType()));
	}

	private static BehaviorControl<LivingEntity> create(float f, UniformInt uniformInt, Predicate<LivingEntity> predicate) {
		float g = f * f;
		SetEntityLookTargetSometimes.Ticker ticker = new SetEntityLookTargetSometimes.Ticker(uniformInt);
		return BehaviorBuilder.create(
			instance -> instance.group(instance.absent(MemoryModuleType.LOOK_TARGET), instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
					.apply(
						instance,
						(memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
								Optional<LivingEntity> optional = instance.<NearestVisibleLivingEntities>get(memoryAccessor2)
									.findClosest(predicate.and(livingEntity2 -> livingEntity2.distanceToSqr(livingEntity) <= (double)g));
								if (optional.isEmpty()) {
									return false;
								} else if (!ticker.tickDownAndCheck(serverLevel.random)) {
									return false;
								} else {
									memoryAccessor.set(new EntityTracker((Entity)optional.get(), true));
									return true;
								}
							}
					)
		);
	}

	public static final class Ticker {
		private final UniformInt interval;
		private int ticksUntilNextStart;

		public Ticker(UniformInt uniformInt) {
			if (uniformInt.getMinValue() <= 1) {
				throw new IllegalArgumentException();
			} else {
				this.interval = uniformInt;
			}
		}

		public boolean tickDownAndCheck(RandomSource randomSource) {
			if (this.ticksUntilNextStart == 0) {
				this.ticksUntilNextStart = this.interval.sample(randomSource) - 1;
				return false;
			} else {
				return --this.ticksUntilNextStart == 0;
			}
		}
	}
}
