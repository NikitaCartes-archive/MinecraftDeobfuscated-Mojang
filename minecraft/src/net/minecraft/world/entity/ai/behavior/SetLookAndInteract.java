package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetLookAndInteract extends Behavior<LivingEntity> {
	private final EntityType<?> type;
	private final int interactionRangeSqr;
	private final Predicate<LivingEntity> targetFilter;
	private final Predicate<LivingEntity> selfFilter;

	public SetLookAndInteract(EntityType<?> entityType, int i, Predicate<LivingEntity> predicate, Predicate<LivingEntity> predicate2) {
		super(
			ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.INTERACTION_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
				MemoryStatus.VALUE_PRESENT
			)
		);
		this.type = entityType;
		this.interactionRangeSqr = i * i;
		this.targetFilter = predicate2;
		this.selfFilter = predicate;
	}

	public SetLookAndInteract(EntityType<?> entityType, int i) {
		this(entityType, i, livingEntity -> true, livingEntity -> true);
	}

	@Override
	public boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
		return this.selfFilter.test(livingEntity) && this.getVisibleEntities(livingEntity).contains(this::isMatchingTarget);
	}

	@Override
	public void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		super.start(serverLevel, livingEntity, l);
		Brain<?> brain = livingEntity.getBrain();
		brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
			.flatMap(
				nearestVisibleLivingEntities -> nearestVisibleLivingEntities.findClosest(
						livingEntity2 -> livingEntity2.distanceToSqr(livingEntity) <= (double)this.interactionRangeSqr && this.isMatchingTarget(livingEntity2)
					)
			)
			.ifPresent(livingEntityx -> {
				brain.setMemory(MemoryModuleType.INTERACTION_TARGET, livingEntityx);
				brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntityx, true));
			});
	}

	private boolean isMatchingTarget(LivingEntity livingEntity) {
		return this.type.equals(livingEntity.getType()) && this.targetFilter.test(livingEntity);
	}

	private NearestVisibleLivingEntities getVisibleEntities(LivingEntity livingEntity) {
		return (NearestVisibleLivingEntities)livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
	}
}
