package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class AnimalPanic extends Behavior<PathfinderMob> {
	private static final int PANIC_MIN_DURATION = 100;
	private static final int PANIC_MAX_DURATION = 120;
	private static final int PANIC_DISTANCE_HORIZONTAL = 5;
	private static final int PANIC_DISTANCE_VERTICAL = 4;
	private final float speedMultiplier;

	public AnimalPanic(float f) {
		super(ImmutableMap.of(MemoryModuleType.IS_PANICKING, MemoryStatus.REGISTERED, MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT), 100, 120);
		this.speedMultiplier = f;
	}

	protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		return true;
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		pathfinderMob.getBrain().setMemory(MemoryModuleType.IS_PANICKING, true);
		pathfinderMob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
	}

	protected void stop(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		Brain<?> brain = pathfinderMob.getBrain();
		brain.eraseMemory(MemoryModuleType.IS_PANICKING);
	}

	protected void tick(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		if (pathfinderMob.getNavigation().isDone()) {
			Vec3 vec3 = this.getPanicPos(pathfinderMob, serverLevel);
			if (vec3 != null) {
				pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speedMultiplier, 0));
			}
		}
	}

	@Nullable
	private Vec3 getPanicPos(PathfinderMob pathfinderMob, ServerLevel serverLevel) {
		if (pathfinderMob.isOnFire()) {
			Optional<Vec3> optional = this.lookForWater(serverLevel, pathfinderMob).map(Vec3::atBottomCenterOf);
			if (optional.isPresent()) {
				return (Vec3)optional.get();
			}
		}

		return LandRandomPos.getPos(pathfinderMob, 5, 4);
	}

	private Optional<BlockPos> lookForWater(BlockGetter blockGetter, Entity entity) {
		BlockPos blockPos = entity.blockPosition();
		return !blockGetter.getBlockState(blockPos).getCollisionShape(blockGetter, blockPos).isEmpty()
			? Optional.empty()
			: BlockPos.findClosestMatch(blockPos, 5, 1, blockPosx -> blockGetter.getFluidState(blockPosx).is(FluidTags.WATER));
	}
}
