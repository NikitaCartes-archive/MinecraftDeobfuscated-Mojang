package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class GoToClosestVillage {
	public static BehaviorControl<Villager> create(float f, int i) {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET)).apply(instance, memoryAccessor -> (serverLevel, villager, l) -> {
						if (serverLevel.isVillage(villager.blockPosition())) {
							return false;
						} else {
							PoiManager poiManager = serverLevel.getPoiManager();
							int j = poiManager.sectionsToVillage(SectionPos.of(villager.blockPosition()));
							Vec3 vec3 = null;

							for (int k = 0; k < 5; k++) {
								Vec3 vec32 = LandRandomPos.getPos(villager, 15, 7, blockPos -> (double)(-poiManager.sectionsToVillage(SectionPos.of(blockPos))));
								if (vec32 != null) {
									int m = poiManager.sectionsToVillage(SectionPos.of(new BlockPos(vec32)));
									if (m < j) {
										vec3 = vec32;
										break;
									}

									if (m == j) {
										vec3 = vec32;
									}
								}
							}

							if (vec3 != null) {
								memoryAccessor.set(new WalkTarget(vec3, f, i));
							}

							return true;
						}
					})
		);
	}
}
