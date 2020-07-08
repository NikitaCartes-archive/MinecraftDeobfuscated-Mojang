package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class AssignProfessionFromJobSite extends Behavior<Villager> {
	public AssignProfessionFromJobSite() {
		super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT));
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		BlockPos blockPos = ((GlobalPos)villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get()).pos();
		return blockPos.closerThan(villager.position(), 2.0) || villager.assignProfessionWhenSpawned();
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		GlobalPos globalPos = (GlobalPos)villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get();
		villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
		villager.getBrain().setMemory(MemoryModuleType.JOB_SITE, globalPos);
		serverLevel.broadcastEntityEvent(villager, (byte)14);
		if (villager.getVillagerData().getProfession() == VillagerProfession.NONE) {
			MinecraftServer minecraftServer = serverLevel.getServer();
			Optional.ofNullable(minecraftServer.getLevel(globalPos.dimension()))
				.flatMap(serverLevelx -> serverLevelx.getPoiManager().getType(globalPos.pos()))
				.flatMap(poiType -> Registry.VILLAGER_PROFESSION.stream().filter(villagerProfession -> villagerProfession.getJobPoiType() == poiType).findFirst())
				.ifPresent(villagerProfession -> {
					villager.setVillagerData(villager.getVillagerData().setProfession(villagerProfession));
					villager.refreshBrain(serverLevel);
				});
		}
	}
}
