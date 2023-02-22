package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStroll {
	private static final int MAX_XZ_DIST = 10;
	private static final int MAX_Y_DIST = 7;
	private static final int[][] SWIM_XY_DISTANCE_TIERS = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

	public static OneShot<PathfinderMob> stroll(float f) {
		return stroll(f, true);
	}

	public static OneShot<PathfinderMob> stroll(float f, boolean bl) {
		return strollFlyOrSwim(
			f, pathfinderMob -> LandRandomPos.getPos(pathfinderMob, 10, 7), bl ? pathfinderMob -> true : pathfinderMob -> !pathfinderMob.isInWaterOrBubble()
		);
	}

	public static BehaviorControl<PathfinderMob> stroll(float f, int i, int j) {
		return strollFlyOrSwim(f, pathfinderMob -> LandRandomPos.getPos(pathfinderMob, i, j), pathfinderMob -> true);
	}

	public static BehaviorControl<PathfinderMob> fly(float f) {
		return strollFlyOrSwim(f, pathfinderMob -> getTargetFlyPos(pathfinderMob, 10, 7), pathfinderMob -> true);
	}

	public static BehaviorControl<PathfinderMob> swim(float f) {
		return strollFlyOrSwim(f, RandomStroll::getTargetSwimPos, Entity::isInWaterOrBubble);
	}

	private static OneShot<PathfinderMob> strollFlyOrSwim(float f, Function<PathfinderMob, Vec3> function, Predicate<PathfinderMob> predicate) {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET)).apply(instance, memoryAccessor -> (serverLevel, pathfinderMob, l) -> {
						if (!predicate.test(pathfinderMob)) {
							return false;
						} else {
							Optional<Vec3> optional = Optional.ofNullable((Vec3)function.apply(pathfinderMob));
							memoryAccessor.setOrErase(optional.map(vec3 -> new WalkTarget(vec3, f, 0)));
							return true;
						}
					})
		);
	}

	@Nullable
	private static Vec3 getTargetSwimPos(PathfinderMob pathfinderMob) {
		Vec3 vec3 = null;
		Vec3 vec32 = null;

		for (int[] is : SWIM_XY_DISTANCE_TIERS) {
			if (vec3 == null) {
				vec32 = BehaviorUtils.getRandomSwimmablePos(pathfinderMob, is[0], is[1]);
			} else {
				vec32 = pathfinderMob.position().add(pathfinderMob.position().vectorTo(vec3).normalize().multiply((double)is[0], (double)is[1], (double)is[0]));
			}

			if (vec32 == null || pathfinderMob.level.getFluidState(BlockPos.containing(vec32)).isEmpty()) {
				return vec3;
			}

			vec3 = vec32;
		}

		return vec32;
	}

	@Nullable
	private static Vec3 getTargetFlyPos(PathfinderMob pathfinderMob, int i, int j) {
		Vec3 vec3 = pathfinderMob.getViewVector(0.0F);
		return AirAndWaterRandomPos.getPos(pathfinderMob, i, j, -2, vec3.x, vec3.z, (float) (Math.PI / 2));
	}
}
