package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetEntityLookTarget extends Behavior<LivingEntity> {
	private final Predicate<LivingEntity> predicate;
	private final float maxDistSqr;
	private Optional<LivingEntity> nearestEntityMatchingTest = Optional.empty();

	public SetEntityLookTarget(Tag<EntityType<?>> tag, float f) {
		this(livingEntity -> livingEntity.getType().is(tag), f);
	}

	public SetEntityLookTarget(MobCategory mobCategory, float f) {
		this(livingEntity -> mobCategory.equals(livingEntity.getType().getCategory()), f);
	}

	public SetEntityLookTarget(EntityType<?> entityType, float f) {
		this(livingEntity -> entityType.equals(livingEntity.getType()), f);
	}

	public SetEntityLookTarget(float f) {
		this(livingEntity -> true, f);
	}

	public SetEntityLookTarget(Predicate<LivingEntity> predicate, float f) {
		super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
		this.predicate = predicate;
		this.maxDistSqr = f * f;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
		NearestVisibleLivingEntities nearestVisibleLivingEntities = (NearestVisibleLivingEntities)livingEntity.getBrain()
			.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
			.get();
		this.nearestEntityMatchingTest = nearestVisibleLivingEntities.findClosest(
			this.predicate.and(livingEntity2 -> livingEntity2.distanceToSqr(livingEntity) <= (double)this.maxDistSqr)
		);
		return this.nearestEntityMatchingTest.isPresent();
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker((Entity)this.nearestEntityMatchingTest.get(), true));
		this.nearestEntityMatchingTest = Optional.empty();
	}
}
