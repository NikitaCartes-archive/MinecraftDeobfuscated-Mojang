package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;

public class ResetProfession {
	public static BehaviorControl<Villager> create() {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.absent(MemoryModuleType.JOB_SITE))
					.apply(
						instance,
						memoryAccessor -> (serverLevel, villager, l) -> {
								VillagerData villagerData = villager.getVillagerData();
								if (villagerData.getProfession() != VillagerProfession.NONE
									&& villagerData.getProfession() != VillagerProfession.NITWIT
									&& villager.getVillagerXp() == 0
									&& villagerData.getLevel() <= 1) {
									villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.NONE));
									villager.refreshBrain(serverLevel);
									return true;
								} else {
									return false;
								}
							}
					)
		);
	}
}
