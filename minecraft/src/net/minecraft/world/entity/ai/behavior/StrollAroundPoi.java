package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollAroundPoi {
	private static final int MIN_TIME_BETWEEN_STROLLS = 180;
	private static final int STROLL_MAX_XZ_DIST = 8;
	private static final int STROLL_MAX_Y_DIST = 6;

	public static OneShot<PathfinderMob> create(MemoryModuleType<GlobalPos> memoryModuleType, float f, int i) {
		MutableLong mutableLong = new MutableLong(0L);
		return BehaviorBuilder.create(
			instance -> instance.group(instance.registered(MemoryModuleType.WALK_TARGET), instance.present(memoryModuleType))
					.apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, pathfinderMob, l) -> {
							GlobalPos globalPos = instance.get(memoryAccessor2);
							if (serverLevel.dimension() != globalPos.dimension() || !globalPos.pos().closerToCenterThan(pathfinderMob.position(), (double)i)) {
								return false;
							} else if (l <= mutableLong.getValue()) {
								return true;
							} else {
								Optional<Vec3> optional = Optional.ofNullable(LandRandomPos.getPos(pathfinderMob, 8, 6));
								memoryAccessor.setOrErase(optional.map(vec3 -> new WalkTarget(vec3, f, 1)));
								mutableLong.setValue(l + 180L);
								return true;
							}
						})
		);
	}
}
