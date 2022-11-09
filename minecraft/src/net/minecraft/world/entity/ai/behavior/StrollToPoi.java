package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollToPoi {
	public static BehaviorControl<PathfinderMob> create(MemoryModuleType<GlobalPos> memoryModuleType, float f, int i, int j) {
		MutableLong mutableLong = new MutableLong(0L);
		return BehaviorBuilder.create(
			instance -> instance.group(instance.registered(MemoryModuleType.WALK_TARGET), instance.present(memoryModuleType))
					.apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, pathfinderMob, l) -> {
							GlobalPos globalPos = instance.get(memoryAccessor2);
							if (serverLevel.dimension() != globalPos.dimension() || !globalPos.pos().closerToCenterThan(pathfinderMob.position(), (double)j)) {
								return false;
							} else if (l <= mutableLong.getValue()) {
								return true;
							} else {
								memoryAccessor.set(new WalkTarget(globalPos.pos(), f, i));
								mutableLong.setValue(l + 80L);
								return true;
							}
						})
		);
	}
}
