package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;

public class ResetProfession extends Behavior<Villager> {
	public ResetProfession() {
		super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_ABSENT));
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		VillagerData villagerData = villager.getVillagerData();
		return villagerData.getProfession() != VillagerProfession.NONE
			&& villagerData.getProfession() != VillagerProfession.NITWIT
			&& villager.getVillagerXp() == 0
			&& villagerData.getLevel() <= 1;
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.NONE));
		villager.refreshBrain(serverLevel);
	}
}
