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
				MemoryModuleType.VISIBLE_LIVING_ENTITIES,
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
		return this.selfFilter.test(livingEntity) && this.getVisibleEntities(livingEntity).stream().anyMatch(this::isMatchingTarget);
	}

	@Override
	public void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		super.start(serverLevel, livingEntity, l);
		Brain<?> brain = livingEntity.getBrain();
		brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES)
			.ifPresent(
				list -> list.stream()
						.filter(livingEntity2 -> livingEntity2.distanceToSqr(livingEntity) <= (double)this.interactionRangeSqr)
						.filter(this::isMatchingTarget)
						.findFirst()
						.ifPresent(livingEntityxx -> {
							brain.setMemory(MemoryModuleType.INTERACTION_TARGET, livingEntityxx);
							brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntityxx, true));
						})
			);
	}

	private boolean isMatchingTarget(LivingEntity livingEntity) {
		return this.type.equals(livingEntity.getType()) && this.targetFilter.test(livingEntity);
	}

	private List<LivingEntity> getVisibleEntities(LivingEntity livingEntity) {
		return (List<LivingEntity>)livingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get();
	}
}
