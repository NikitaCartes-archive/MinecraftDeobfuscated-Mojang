package net.minecraft.world.entity.monster.breeze;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class Slide extends Behavior<Breeze> {
	public Slide() {
		super(
			Map.of(
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.BREEZE_JUMP_COOLDOWN,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.BREEZE_SHOOT,
				MemoryStatus.VALUE_ABSENT
			)
		);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Breeze breeze) {
		return breeze.onGround() && !breeze.isInWater() && breeze.getPose() == Pose.STANDING;
	}

	protected void start(ServerLevel serverLevel, Breeze breeze, long l) {
		LivingEntity livingEntity = (LivingEntity)breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
		if (livingEntity != null) {
			boolean bl = breeze.withinInnerCircleRange(livingEntity.position());
			Vec3 vec3 = null;
			if (bl) {
				Vec3 vec32 = DefaultRandomPos.getPosAway(breeze, 5, 5, livingEntity.position());
				if (vec32 != null && BreezeUtil.hasLineOfSight(breeze, vec32) && livingEntity.distanceToSqr(vec32.x, vec32.y, vec32.z) > livingEntity.distanceToSqr(breeze)
					)
				 {
					vec3 = vec32;
				}
			}

			if (vec3 == null) {
				vec3 = breeze.getRandom().nextBoolean()
					? BreezeUtil.randomPointBehindTarget(livingEntity, breeze.getRandom())
					: randomPointInMiddleCircle(breeze, livingEntity);
			}

			breeze.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(BlockPos.containing(vec3), 0.6F, 1));
		}
	}

	private static Vec3 randomPointInMiddleCircle(Breeze breeze, LivingEntity livingEntity) {
		Vec3 vec3 = livingEntity.position().subtract(breeze.position());
		double d = vec3.length() - Mth.lerp(breeze.getRandom().nextDouble(), 8.0, 4.0);
		Vec3 vec32 = vec3.normalize().multiply(d, d, d);
		return breeze.position().add(vec32);
	}
}
