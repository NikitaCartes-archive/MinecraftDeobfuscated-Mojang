package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class AssignProfessionFromJobSite {
	public static BehaviorControl<Villager> create() {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.present(MemoryModuleType.POTENTIAL_JOB_SITE), instance.registered(MemoryModuleType.JOB_SITE))
					.apply(
						instance,
						(memoryAccessor, memoryAccessor2) -> (serverLevel, villager, l) -> {
								GlobalPos globalPos = instance.get(memoryAccessor);
								if (!globalPos.pos().closerToCenterThan(villager.position(), 2.0) && !villager.assignProfessionWhenSpawned()) {
									return false;
								} else {
									memoryAccessor.erase();
									memoryAccessor2.set(globalPos);
									serverLevel.broadcastEntityEvent(villager, (byte)14);
									if (villager.getVillagerData().getProfession() != VillagerProfession.NONE) {
										return true;
									} else {
										MinecraftServer minecraftServer = serverLevel.getServer();
										Optional.ofNullable(minecraftServer.getLevel(globalPos.dimension()))
											.flatMap(serverLevelx -> serverLevelx.getPoiManager().getType(globalPos.pos()))
											.flatMap(
												holder -> BuiltInRegistries.VILLAGER_PROFESSION.stream().filter(villagerProfession -> villagerProfession.heldJobSite().test(holder)).findFirst()
											)
											.ifPresent(villagerProfession -> {
												villager.setVillagerData(villager.getVillagerData().setProfession(villagerProfession));
												villager.refreshBrain(serverLevel);
											});
										return true;
									}
								}
							}
					)
		);
	}
}
