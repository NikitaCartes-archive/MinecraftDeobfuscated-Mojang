package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public class LocateHidingPlace {
	public static OneShot<LivingEntity> create(int i, float f, int j) {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.absent(MemoryModuleType.WALK_TARGET),
						instance.registered(MemoryModuleType.HOME),
						instance.registered(MemoryModuleType.HIDING_PLACE),
						instance.registered(MemoryModuleType.PATH),
						instance.registered(MemoryModuleType.LOOK_TARGET),
						instance.registered(MemoryModuleType.BREED_TARGET),
						instance.registered(MemoryModuleType.INTERACTION_TARGET)
					)
					.apply(
						instance,
						(memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4, memoryAccessor5, memoryAccessor6, memoryAccessor7) -> (serverLevel, livingEntity, l) -> {
								serverLevel.getPoiManager()
									.find(holder -> holder.is(PoiTypes.HOME), blockPos -> true, livingEntity.blockPosition(), j + 1, PoiManager.Occupancy.ANY)
									.filter(blockPos -> blockPos.closerToCenterThan(livingEntity.position(), (double)j))
									.or(
										() -> serverLevel.getPoiManager()
												.getRandom(
													holder -> holder.is(PoiTypes.HOME), blockPos -> true, PoiManager.Occupancy.ANY, livingEntity.blockPosition(), i, livingEntity.getRandom()
												)
									)
									.or(() -> instance.tryGet(memoryAccessor2).map(GlobalPos::pos))
									.ifPresent(blockPos -> {
										memoryAccessor4.erase();
										memoryAccessor5.erase();
										memoryAccessor6.erase();
										memoryAccessor7.erase();
										memoryAccessor3.set(GlobalPos.of(serverLevel.dimension(), blockPos));
										if (!blockPos.closerToCenterThan(livingEntity.position(), (double)j)) {
											memoryAccessor.set(new WalkTarget(blockPos, f, j));
										}
									});
								return true;
							}
					)
		);
	}
}
