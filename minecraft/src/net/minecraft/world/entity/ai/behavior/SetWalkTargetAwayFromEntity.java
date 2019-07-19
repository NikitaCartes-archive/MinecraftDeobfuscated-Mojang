package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetAwayFromEntity extends Behavior<PathfinderMob> {
	private final MemoryModuleType<? extends Entity> memory;
	private final float speed;

	public SetWalkTargetAwayFromEntity(MemoryModuleType<? extends Entity> memoryModuleType, float f) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, memoryModuleType, MemoryStatus.VALUE_PRESENT));
		this.memory = memoryModuleType;
		this.speed = f;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		Entity entity = (Entity)pathfinderMob.getBrain().getMemory(this.memory).get();
		return pathfinderMob.distanceToSqr(entity) < 36.0;
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		Entity entity = (Entity)pathfinderMob.getBrain().getMemory(this.memory).get();
		moveAwayFromMob(pathfinderMob, entity, this.speed);
	}

	public static void moveAwayFromMob(PathfinderMob pathfinderMob, Entity entity, float f) {
		for (int i = 0; i < 10; i++) {
			Vec3 vec3 = new Vec3(entity.x, entity.y, entity.z);
			Vec3 vec32 = RandomPos.getLandPosAvoid(pathfinderMob, 16, 7, vec3);
			if (vec32 != null) {
				pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec32, f, 0));
				return;
			}
		}
	}
}
