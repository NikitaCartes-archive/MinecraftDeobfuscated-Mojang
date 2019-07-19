package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith<E extends LivingEntity, T extends LivingEntity> extends Behavior<E> {
	private final int maxDist;
	private final float speed;
	private final EntityType<? extends T> type;
	private final int interactionRangeSqr;
	private final Predicate<T> targetFilter;
	private final Predicate<E> selfFilter;
	private final MemoryModuleType<T> memory;

	public InteractWith(
		EntityType<? extends T> entityType, int i, Predicate<E> predicate, Predicate<T> predicate2, MemoryModuleType<T> memoryModuleType, float f, int j
	) {
		super(
			ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				memoryModuleType,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.VISIBLE_LIVING_ENTITIES,
				MemoryStatus.VALUE_PRESENT
			)
		);
		this.type = entityType;
		this.speed = f;
		this.interactionRangeSqr = i * i;
		this.maxDist = j;
		this.targetFilter = predicate2;
		this.selfFilter = predicate;
		this.memory = memoryModuleType;
	}

	public static <T extends LivingEntity> InteractWith<LivingEntity, T> of(
		EntityType<? extends T> entityType, int i, MemoryModuleType<T> memoryModuleType, float f, int j
	) {
		return new InteractWith<>(entityType, i, livingEntity -> true, livingEntity -> true, memoryModuleType, f, j);
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
		return this.selfFilter.test(livingEntity)
			&& ((List)livingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get())
				.stream()
				.anyMatch(livingEntityx -> this.type.equals(livingEntityx.getType()) && this.targetFilter.test(livingEntityx));
	}

	@Override
	protected void start(ServerLevel serverLevel, E livingEntity, long l) {
		Brain<?> brain = livingEntity.getBrain();
		brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES)
			.ifPresent(
				list -> list.stream()
						.filter(livingEntityxx -> this.type.equals(livingEntityxx.getType()))
						.map(livingEntityxx -> livingEntityxx)
						.filter(livingEntity2 -> livingEntity2.distanceToSqr(livingEntity) <= (double)this.interactionRangeSqr)
						.filter(this.targetFilter)
						.findFirst()
						.ifPresent(livingEntityxx -> {
							brain.setMemory(this.memory, (T)livingEntityxx);
							brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(livingEntityxx));
							brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityPosWrapper(livingEntityxx), this.speed, this.maxDist));
						})
			);
	}
}
