package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith<E extends LivingEntity, T extends LivingEntity> extends Behavior<E> {
	private final int maxDist;
	private final float speedModifier;
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
				MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
				MemoryStatus.VALUE_PRESENT
			)
		);
		this.type = entityType;
		this.speedModifier = f;
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

	public static <T extends LivingEntity> InteractWith<LivingEntity, T> of(
		EntityType<? extends T> entityType, int i, Predicate<T> predicate, MemoryModuleType<T> memoryModuleType, float f, int j
	) {
		return new InteractWith<>(entityType, i, livingEntity -> true, predicate, memoryModuleType, f, j);
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
		return this.selfFilter.test(livingEntity) && this.seesAtLeastOneValidTarget(livingEntity);
	}

	private boolean seesAtLeastOneValidTarget(E livingEntity) {
		NearestVisibleLivingEntities nearestVisibleLivingEntities = (NearestVisibleLivingEntities)livingEntity.getBrain()
			.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
			.get();
		return nearestVisibleLivingEntities.contains(this::isTargetValid);
	}

	private boolean isTargetValid(LivingEntity livingEntity) {
		return this.type.equals(livingEntity.getType()) && this.targetFilter.test(livingEntity);
	}

	@Override
	protected void start(ServerLevel serverLevel, E livingEntity, long l) {
		Brain<?> brain = livingEntity.getBrain();
		Optional<NearestVisibleLivingEntities> optional = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
		if (!optional.isEmpty()) {
			NearestVisibleLivingEntities nearestVisibleLivingEntities = (NearestVisibleLivingEntities)optional.get();
			nearestVisibleLivingEntities.findClosest(livingEntity2 -> this.canInteract(livingEntity, livingEntity2)).ifPresent(livingEntityx -> {
				brain.setMemory(this.memory, (T)livingEntityx);
				brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntityx, true));
				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(livingEntityx, false), this.speedModifier, this.maxDist));
			});
		}
	}

	private boolean canInteract(E livingEntity, LivingEntity livingEntity2) {
		return this.type.equals(livingEntity2.getType())
			&& livingEntity2.distanceToSqr(livingEntity) <= (double)this.interactionRangeSqr
			&& this.targetFilter.test(livingEntity2);
	}
}
