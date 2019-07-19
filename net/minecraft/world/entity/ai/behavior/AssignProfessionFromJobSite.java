/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
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
        super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
        return villager.getVillagerData().getProfession() == VillagerProfession.NONE;
    }

    @Override
    protected void start(ServerLevel serverLevel, Villager villager, long l) {
        GlobalPos globalPos = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
        MinecraftServer minecraftServer = serverLevel.getServer();
        minecraftServer.getLevel(globalPos.dimension()).getPoiManager().getType(globalPos.pos()).ifPresent(poiType -> Registry.VILLAGER_PROFESSION.stream().filter(villagerProfession -> villagerProfession.getJobPoiType() == poiType).findFirst().ifPresent(villagerProfession -> {
            villager.setVillagerData(villager.getVillagerData().setProfession((VillagerProfession)villagerProfession));
            villager.refreshBrain(serverLevel);
        }));
    }
}

