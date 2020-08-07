package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class PoiCompetitorScan extends Behavior<Villager> {
	final VillagerProfession profession;

	public PoiCompetitorScan(VillagerProfession villagerProfession) {
		super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
		this.profession = villagerProfession;
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		GlobalPos globalPos = (GlobalPos)villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
		serverLevel.getPoiManager()
			.getType(globalPos.pos())
			.ifPresent(
				poiType -> BehaviorUtils.getNearbyVillagersWithCondition(villager, villagerxx -> this.competesForSameJobsite(globalPos, poiType, villagerxx))
						.reduce(villager, PoiCompetitorScan::selectWinner)
			);
	}

	private static Villager selectWinner(Villager villager, Villager villager2) {
		Villager villager3;
		Villager villager4;
		if (villager.getVillagerXp() > villager2.getVillagerXp()) {
			villager3 = villager;
			villager4 = villager2;
		} else {
			villager3 = villager2;
			villager4 = villager;
		}

		villager4.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
		return villager3;
	}

	private boolean competesForSameJobsite(GlobalPos globalPos, PoiType poiType, Villager villager) {
		return this.hasJobSite(villager)
			&& globalPos.equals(villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).get())
			&& this.hasMatchingProfession(poiType, villager.getVillagerData().getProfession());
	}

	private boolean hasMatchingProfession(PoiType poiType, VillagerProfession villagerProfession) {
		return villagerProfession.getJobPoiType().getPredicate().test(poiType);
	}

	private boolean hasJobSite(Villager villager) {
		return villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).isPresent();
	}
}
