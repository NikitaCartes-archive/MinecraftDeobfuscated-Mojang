package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class MoveToTargetSink extends Behavior<Mob> {
	@Nullable
	private Path path;
	@Nullable
	private BlockPos lastTargetPos;
	private float speedModifier;
	private int remainingDelay;

	public MoveToTargetSink(int i) {
		super(
			ImmutableMap.of(
				MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
				MemoryStatus.REGISTERED,
				MemoryModuleType.PATH,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_PRESENT
			),
			i
		);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Mob mob) {
		Brain<?> brain = mob.getBrain();
		WalkTarget walkTarget = (WalkTarget)brain.getMemory(MemoryModuleType.WALK_TARGET).get();
		boolean bl = this.reachedTarget(mob, walkTarget);
		if (!bl && this.tryComputePath(mob, walkTarget, serverLevel.getGameTime())) {
			this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();
			return true;
		} else {
			brain.eraseMemory(MemoryModuleType.WALK_TARGET);
			if (bl) {
				brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
			}

			return false;
		}
	}

	protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
		if (this.path != null && this.lastTargetPos != null) {
			Optional<WalkTarget> optional = mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
			PathNavigation pathNavigation = mob.getNavigation();
			return !pathNavigation.isDone() && optional.isPresent() && !this.reachedTarget(mob, (WalkTarget)optional.get());
		} else {
			return false;
		}
	}

	protected void stop(ServerLevel serverLevel, Mob mob, long l) {
		mob.getNavigation().stop();
		mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		mob.getBrain().eraseMemory(MemoryModuleType.PATH);
		this.path = null;
	}

	protected void start(ServerLevel serverLevel, Mob mob, long l) {
		mob.getBrain().setMemory(MemoryModuleType.PATH, this.path);
		mob.getNavigation().moveTo(this.path, (double)this.speedModifier);
		this.remainingDelay = serverLevel.getRandom().nextInt(10);
	}

	protected void tick(ServerLevel serverLevel, Mob mob, long l) {
		this.remainingDelay--;
		if (this.remainingDelay <= 0) {
			Path path = mob.getNavigation().getPath();
			Brain<?> brain = mob.getBrain();
			if (this.path != path) {
				this.path = path;
				brain.setMemory(MemoryModuleType.PATH, path);
			}

			if (path != null && this.lastTargetPos != null) {
				WalkTarget walkTarget = (WalkTarget)brain.getMemory(MemoryModuleType.WALK_TARGET).get();
				if (walkTarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0 && this.tryComputePath(mob, walkTarget, serverLevel.getGameTime())) {
					this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();
					this.start(serverLevel, mob, l);
				}
			}
		}
	}

	private boolean tryComputePath(Mob mob, WalkTarget walkTarget, long l) {
		BlockPos blockPos = walkTarget.getTarget().currentBlockPosition();
		this.path = mob.getNavigation().createPath(blockPos, 0);
		this.speedModifier = walkTarget.getSpeedModifier();
		Brain<?> brain = mob.getBrain();
		if (this.reachedTarget(mob, walkTarget)) {
			brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		} else {
			boolean bl = this.path != null && this.path.canReach();
			if (bl) {
				brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
			} else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
				brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, l);
			}

			if (this.path != null) {
				return true;
			}

			Vec3 vec3 = RandomPos.getPosTowards((PathfinderMob)mob, 10, 7, Vec3.atBottomCenterOf(blockPos));
			if (vec3 != null) {
				this.path = mob.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
				return this.path != null;
			}
		}

		return false;
	}

	private boolean reachedTarget(Mob mob, WalkTarget walkTarget) {
		return walkTarget.getTarget().currentBlockPosition().distManhattan(mob.blockPosition()) <= walkTarget.getCloseEnoughDist();
	}
}
