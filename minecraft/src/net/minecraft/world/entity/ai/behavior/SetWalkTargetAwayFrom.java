package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetAwayFrom {
	public static BehaviorControl<PathfinderMob> pos(MemoryModuleType<BlockPos> memoryModuleType, float f, int i, boolean bl) {
		return create(memoryModuleType, f, i, bl, Vec3::atBottomCenterOf);
	}

	public static OneShot<PathfinderMob> entity(MemoryModuleType<? extends Entity> memoryModuleType, float f, int i, boolean bl) {
		return create(memoryModuleType, f, i, bl, Entity::position);
	}

	private static <T> OneShot<PathfinderMob> create(MemoryModuleType<T> memoryModuleType, float f, int i, boolean bl, Function<T, Vec3> function) {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.registered(MemoryModuleType.WALK_TARGET), instance.present(memoryModuleType))
					.apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, pathfinderMob, l) -> {
							Optional<WalkTarget> optional = instance.tryGet(memoryAccessor);
							if (optional.isPresent() && !bl) {
								return false;
							} else {
								Vec3 vec3 = pathfinderMob.position();
								Vec3 vec32 = (Vec3)function.apply(instance.get(memoryAccessor2));
								if (!vec3.closerThan(vec32, (double)i)) {
									return false;
								} else {
									if (optional.isPresent() && ((WalkTarget)optional.get()).getSpeedModifier() == f) {
										Vec3 vec33 = ((WalkTarget)optional.get()).getTarget().currentPosition().subtract(vec3);
										Vec3 vec34 = vec32.subtract(vec3);
										if (vec33.dot(vec34) < 0.0) {
											return false;
										}
									}

									for (int j = 0; j < 10; j++) {
										Vec3 vec34 = LandRandomPos.getPosAway(pathfinderMob, 16, 7, vec32);
										if (vec34 != null) {
											memoryAccessor.set(new WalkTarget(vec34, f, 0));
											break;
										}
									}

									return true;
								}
							}
						})
		);
	}
}
