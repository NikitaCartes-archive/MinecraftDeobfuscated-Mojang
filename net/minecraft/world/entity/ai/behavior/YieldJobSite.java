/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.pathfinder.Path;

public class YieldJobSite {
    public static BehaviorControl<Villager> create(float f) {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.POTENTIAL_JOB_SITE), instance.absent(MemoryModuleType.JOB_SITE), instance.present(MemoryModuleType.NEAREST_LIVING_ENTITIES), instance.registered(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleType.LOOK_TARGET)).apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4, memoryAccessor5) -> (serverLevel, villager2, l) -> {
            if (villager2.isBaby()) {
                return false;
            }
            if (villager2.getVillagerData().getProfession() == VillagerProfession.NONE) {
                return false;
            }
            BlockPos blockPos = ((GlobalPos)instance.get(memoryAccessor)).pos();
            Optional<Holder<PoiType>> optional = serverLevel.getPoiManager().getType(blockPos);
            if (optional.isEmpty()) {
                return true;
            }
            ((List)instance.get(memoryAccessor3)).stream().filter(livingEntity -> livingEntity instanceof Villager && livingEntity != villager2).map(livingEntity -> (Villager)livingEntity).filter(LivingEntity::isAlive).filter(villager -> YieldJobSite.nearbyWantsJobsite((Holder)optional.get(), villager, blockPos)).findFirst().ifPresent(villager -> {
                memoryAccessor4.erase();
                memoryAccessor5.erase();
                memoryAccessor.erase();
                if (villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).isEmpty()) {
                    BehaviorUtils.setWalkAndLookTargetMemories((LivingEntity)villager, blockPos, f, 1);
                    villager.getBrain().setMemory(MemoryModuleType.POTENTIAL_JOB_SITE, GlobalPos.of(serverLevel.dimension(), blockPos));
                    DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos);
                }
            });
            return true;
        }));
    }

    private static boolean nearbyWantsJobsite(Holder<PoiType> holder, Villager villager, BlockPos blockPos) {
        boolean bl = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).isPresent();
        if (bl) {
            return false;
        }
        Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        VillagerProfession villagerProfession = villager.getVillagerData().getProfession();
        if (villagerProfession.heldJobSite().test(holder)) {
            if (optional.isEmpty()) {
                return YieldJobSite.canReachPos(villager, blockPos, holder.value());
            }
            return optional.get().pos().equals(blockPos);
        }
        return false;
    }

    private static boolean canReachPos(PathfinderMob pathfinderMob, BlockPos blockPos, PoiType poiType) {
        Path path = pathfinderMob.getNavigation().createPath(blockPos, poiType.validRange());
        return path != null && path.canReach();
    }
}

