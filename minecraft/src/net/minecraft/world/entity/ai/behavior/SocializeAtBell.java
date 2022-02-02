package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SocializeAtBell extends Behavior<LivingEntity> {
	private static final float SPEED_MODIFIER = 0.3F;

	public SocializeAtBell() {
		super(
			ImmutableMap.of(
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.MEETING_POINT,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.INTERACTION_TARGET,
				MemoryStatus.VALUE_ABSENT
			)
		);
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
		Brain<?> brain = livingEntity.getBrain();
		Optional<GlobalPos> optional = brain.getMemory(MemoryModuleType.MEETING_POINT);
		return serverLevel.getRandom().nextInt(100) == 0
			&& optional.isPresent()
			&& serverLevel.dimension() == ((GlobalPos)optional.get()).dimension()
			&& ((GlobalPos)optional.get()).pos().closerToCenterThan(livingEntity.position(), 4.0)
			&& ((NearestVisibleLivingEntities)brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get())
				.contains(livingEntityx -> EntityType.VILLAGER.equals(livingEntityx.getType()));
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		Brain<?> brain = livingEntity.getBrain();
		brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
			.flatMap(
				nearestVisibleLivingEntities -> nearestVisibleLivingEntities.findClosest(
						livingEntity2 -> EntityType.VILLAGER.equals(livingEntity2.getType()) && livingEntity2.distanceToSqr(livingEntity) <= 32.0
					)
			)
			.ifPresent(livingEntityx -> {
				brain.setMemory(MemoryModuleType.INTERACTION_TARGET, livingEntityx);
				brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntityx, true));
				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(livingEntityx, false), 0.3F, 1));
			});
	}
}
