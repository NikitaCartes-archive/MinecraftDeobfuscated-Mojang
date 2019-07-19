package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class GoToClosestVillage extends Behavior<Villager> {
	private final float speed;
	private final int closeEnoughDistance;

	public GoToClosestVillage(float f, int i) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
		this.speed = f;
		this.closeEnoughDistance = i;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		return !serverLevel.isVillage(new BlockPos(villager));
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		PoiManager poiManager = serverLevel.getPoiManager();
		int i = poiManager.sectionsToVillage(SectionPos.of(new BlockPos(villager)));
		Vec3 vec3 = null;

		for (int j = 0; j < 5; j++) {
			Vec3 vec32 = RandomPos.getLandPos(villager, 15, 7, blockPos -> (double)(-serverLevel.sectionsToVillage(SectionPos.of(blockPos))));
			if (vec32 != null) {
				int k = poiManager.sectionsToVillage(SectionPos.of(new BlockPos(vec32)));
				if (k < i) {
					vec3 = vec32;
					break;
				}

				if (k == i) {
					vec3 = vec32;
				}
			}
		}

		if (vec3 != null) {
			villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speed, this.closeEnoughDistance));
		}
	}
}
