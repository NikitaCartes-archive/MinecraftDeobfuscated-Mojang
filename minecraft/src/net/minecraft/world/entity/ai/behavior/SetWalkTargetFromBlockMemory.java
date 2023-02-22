package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetFromBlockMemory {
	public static OneShot<Villager> create(MemoryModuleType<GlobalPos> memoryModuleType, float f, int i, int j, int k) {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE), instance.absent(MemoryModuleType.WALK_TARGET), instance.present(memoryModuleType)
					)
					.apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, villager, l) -> {
							GlobalPos globalPos = instance.get(memoryAccessor3);
							Optional<Long> optional = instance.tryGet(memoryAccessor);
							if (globalPos.dimension() == serverLevel.dimension() && (!optional.isPresent() || serverLevel.getGameTime() - (Long)optional.get() <= (long)k)) {
								if (globalPos.pos().distManhattan(villager.blockPosition()) > j) {
									Vec3 vec3 = null;
									int m = 0;
									int n = 1000;

									while (vec3 == null || BlockPos.containing(vec3).distManhattan(villager.blockPosition()) > j) {
										vec3 = DefaultRandomPos.getPosTowards(villager, 15, 7, Vec3.atBottomCenterOf(globalPos.pos()), (float) (Math.PI / 2));
										if (++m == 1000) {
											villager.releasePoi(memoryModuleType);
											memoryAccessor3.erase();
											memoryAccessor.set(l);
											return true;
										}
									}

									memoryAccessor2.set(new WalkTarget(vec3, f, i));
								} else if (globalPos.pos().distManhattan(villager.blockPosition()) > i) {
									memoryAccessor2.set(new WalkTarget(globalPos.pos(), f, i));
								}
							} else {
								villager.releasePoi(memoryModuleType);
								memoryAccessor3.erase();
								memoryAccessor.set(l);
							}

							return true;
						})
		);
	}
}
