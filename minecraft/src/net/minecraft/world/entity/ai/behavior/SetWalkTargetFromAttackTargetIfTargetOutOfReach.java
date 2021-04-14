package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach extends Behavior<Mob> {
	private static final int PROJECTILE_ATTACK_RANGE_BUFFER = 1;
	private final Function<LivingEntity, Float> speedModifier;

	public SetWalkTargetFromAttackTargetIfTargetOutOfReach(float f) {
		this(livingEntity -> f);
	}

	public SetWalkTargetFromAttackTargetIfTargetOutOfReach(Function<LivingEntity, Float> function) {
		super(
			ImmutableMap.of(
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
				MemoryStatus.REGISTERED
			)
		);
		this.speedModifier = function;
	}

	protected void start(ServerLevel serverLevel, Mob mob, long l) {
		LivingEntity livingEntity = (LivingEntity)mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
		if (BehaviorUtils.canSee(mob, livingEntity) && BehaviorUtils.isWithinAttackRange(mob, livingEntity, 1)) {
			this.clearWalkTarget(mob);
		} else {
			this.setWalkAndLookTarget(mob, livingEntity);
		}
	}

	private void setWalkAndLookTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
		Brain<?> brain = livingEntity.getBrain();
		brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity2, true));
		WalkTarget walkTarget = new WalkTarget(new EntityTracker(livingEntity2, false), (Float)this.speedModifier.apply(livingEntity), 0);
		brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
	}

	private void clearWalkTarget(LivingEntity livingEntity) {
		livingEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
	}
}
