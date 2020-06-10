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
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class GoToPotentialJobSite
extends Behavior<Villager> {
    final float speedModifier;

    public GoToPotentialJobSite(float f) {
        super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT), 1200);
        this.speedModifier = f;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
        return villager.getBrain().getActiveNonCoreActivity().map(activity -> activity == Activity.IDLE || activity == Activity.WORK || activity == Activity.PLAY).orElse(true);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
        return villager.getBrain().hasMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE);
    }

    @Override
    protected void tick(ServerLevel serverLevel, Villager villager, long l) {
        BehaviorUtils.setWalkAndLookTargetMemories((LivingEntity)villager, villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos(), this.speedModifier, 1);
    }

    @Override
    protected void stop(ServerLevel serverLevel, Villager villager, long l) {
        Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
        optional.ifPresent(globalPos -> {
            BlockPos blockPos = globalPos.pos();
            ServerLevel serverLevel2 = serverLevel.getServer().getLevel(globalPos.dimension());
            if (serverLevel2 == null) {
                return;
            }
            PoiManager poiManager = serverLevel2.getPoiManager();
            if (poiManager.exists(blockPos, poiType -> true)) {
                poiManager.release(blockPos);
            }
            DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos);
        });
        villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Villager)livingEntity, l);
    }
}

