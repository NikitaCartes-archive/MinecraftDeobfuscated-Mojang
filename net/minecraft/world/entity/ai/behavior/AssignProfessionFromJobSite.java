/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class AssignProfessionFromJobSite
extends Behavior<Villager> {
    public AssignProfessionFromJobSite() {
        super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
        BlockPos blockPos = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos();
        return blockPos.closerThan(villager.position(), 2.0) || villager.assignProfessionWhenSpawned();
    }

    @Override
    protected void start(ServerLevel serverLevel, Villager villager, long l) {
        GlobalPos globalPos = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get();
        villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
        villager.getBrain().setMemory(MemoryModuleType.JOB_SITE, globalPos);
        if (villager.getVillagerData().getProfession() != VillagerProfession.NONE) {
            return;
        }
        MinecraftServer minecraftServer = serverLevel.getServer();
        minecraftServer.getLevel(globalPos.dimension()).getPoiManager().getType(globalPos.pos()).ifPresent(poiType -> Registry.VILLAGER_PROFESSION.stream().filter(villagerProfession -> villagerProfession.getJobPoiType() == poiType).findFirst().ifPresent(villagerProfession -> {
            villager.setVillagerData(villager.getVillagerData().setProfession((VillagerProfession)villagerProfession));
            villager.refreshBrain(serverLevel);
        }));
    }
}

