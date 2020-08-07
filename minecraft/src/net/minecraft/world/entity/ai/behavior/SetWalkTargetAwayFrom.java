package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetAwayFrom<T> extends Behavior<PathfinderMob> {
	private final MemoryModuleType<T> walkAwayFromMemory;
	private final float speedModifier;
	private final int desiredDistance;
	private final Function<T, Vec3> toPosition;

	public SetWalkTargetAwayFrom(MemoryModuleType<T> memoryModuleType, float f, int i, boolean bl, Function<T, Vec3> function) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, bl ? MemoryStatus.REGISTERED : MemoryStatus.VALUE_ABSENT, memoryModuleType, MemoryStatus.VALUE_PRESENT));
		this.walkAwayFromMemory = memoryModuleType;
		this.speedModifier = f;
		this.desiredDistance = i;
		this.toPosition = function;
	}

	public static SetWalkTargetAwayFrom<BlockPos> pos(MemoryModuleType<BlockPos> memoryModuleType, float f, int i, boolean bl) {
		return new SetWalkTargetAwayFrom<>(memoryModuleType, f, i, bl, Vec3::atBottomCenterOf);
	}

	public static SetWalkTargetAwayFrom<? extends Entity> entity(MemoryModuleType<? extends Entity> memoryModuleType, float f, int i, boolean bl) {
		return new SetWalkTargetAwayFrom<>(memoryModuleType, f, i, bl, Entity::position);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		return this.alreadyWalkingAwayFromPosWithSameSpeed(pathfinderMob)
			? false
			: pathfinderMob.position().closerThan(this.getPosToAvoid(pathfinderMob), (double)this.desiredDistance);
	}

	private Vec3 getPosToAvoid(PathfinderMob pathfinderMob) {
		return (Vec3)this.toPosition.apply(pathfinderMob.getBrain().getMemory(this.walkAwayFromMemory).get());
	}

	private boolean alreadyWalkingAwayFromPosWithSameSpeed(PathfinderMob pathfinderMob) {
		if (!pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
			return false;
		} else {
			WalkTarget walkTarget = (WalkTarget)pathfinderMob.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get();
			if (walkTarget.getSpeedModifier() != this.speedModifier) {
				return false;
			} else {
				Vec3 vec3 = walkTarget.getTarget().currentPosition().subtract(pathfinderMob.position());
				Vec3 vec32 = this.getPosToAvoid(pathfinderMob).subtract(pathfinderMob.position());
				return vec3.dot(vec32) < 0.0;
			}
		}
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		moveAwayFrom(pathfinderMob, this.getPosToAvoid(pathfinderMob), this.speedModifier);
	}

	private static void moveAwayFrom(PathfinderMob pathfinderMob, Vec3 vec3, float f) {
		for (int i = 0; i < 10; i++) {
			Vec3 vec32 = RandomPos.getLandPosAvoid(pathfinderMob, 16, 7, vec3);
			if (vec32 != null) {
				pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec32, f, 0));
				return;
			}
		}
	}
}
