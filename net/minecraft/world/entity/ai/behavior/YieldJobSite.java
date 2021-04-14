/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.pathfinder.Path;

public class YieldJobSite
extends Behavior<Villager> {
    private final float speedModifier;

    public YieldJobSite(float f) {
        super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
        this.speedModifier = f;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
        if (villager.isBaby()) {
            return false;
        }
        return villager.getVillagerData().getProfession() == VillagerProfession.NONE;
    }

    @Override
    protected void start(ServerLevel serverLevel, Villager villager3, long l) {
        BlockPos blockPos = villager3.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos();
        Optional<PoiType> optional = serverLevel.getPoiManager().getType(blockPos);
        if (!optional.isPresent()) {
            return;
        }
        BehaviorUtils.getNearbyVillagersWithCondition(villager3, villager -> this.nearbyWantsJobsite((PoiType)optional.get(), (Villager)villager, blockPos)).findFirst().ifPresent(villager2 -> this.yieldJobSite(serverLevel, villager3, (Villager)villager2, blockPos, villager2.getBrain().getMemory(MemoryModuleType.JOB_SITE).isPresent()));
    }

    private boolean nearbyWantsJobsite(PoiType poiType, Villager villager, BlockPos blockPos) {
        boolean bl = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).isPresent();
        if (bl) {
            return false;
        }
        Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        VillagerProfession villagerProfession = villager.getVillagerData().getProfession();
        if (villager.getVillagerData().getProfession() != VillagerProfession.NONE && villagerProfession.getJobPoiType().getPredicate().test(poiType)) {
            if (!optional.isPresent()) {
                return this.canReachPos(villager, blockPos, poiType);
            }
            return optional.get().pos().equals(blockPos);
        }
        return false;
    }

    private void yieldJobSite(ServerLevel serverLevel, Villager villager, Villager villager2, BlockPos blockPos, boolean bl) {
        this.eraseMemories(villager);
        if (!bl) {
            BehaviorUtils.setWalkAndLookTargetMemories((LivingEntity)villager2, blockPos, this.speedModifier, 1);
            villager2.getBrain().setMemory(MemoryModuleType.POTENTIAL_JOB_SITE, GlobalPos.of(serverLevel.dimension(), blockPos));
            DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos);
        }
    }

    private boolean canReachPos(Villager villager, BlockPos blockPos, PoiType poiType) {
        Path path = villager.getNavigation().createPath(blockPos, poiType.getValidRange());
        return path != null && path.canReach();
    }

    private void eraseMemories(Villager villager) {
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
    }
}

