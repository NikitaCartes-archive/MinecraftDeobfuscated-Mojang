/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetFromBlockMemory {
    public static OneShot<Villager> create(MemoryModuleType<GlobalPos> memoryModuleType, float f, int i, int j, int k) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE), instance.absent(MemoryModuleType.WALK_TARGET), instance.present(memoryModuleType)).apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, villager, l) -> {
            GlobalPos globalPos = (GlobalPos)instance.get(memoryAccessor3);
            Optional optional = instance.tryGet(memoryAccessor);
            if (globalPos.dimension() != serverLevel.dimension() || optional.isPresent() && serverLevel.getGameTime() - (Long)optional.get() > (long)k) {
                villager.releasePoi(memoryModuleType);
                memoryAccessor3.erase();
                memoryAccessor.set(l);
            } else if (globalPos.pos().distManhattan(villager.blockPosition()) > j) {
                Vec3 vec3 = null;
                int m = 0;
                int n = 1000;
                while (vec3 == null || BlockPos.containing(vec3).distManhattan(villager.blockPosition()) > j) {
                    vec3 = DefaultRandomPos.getPosTowards(villager, 15, 7, Vec3.atBottomCenterOf(globalPos.pos()), 1.5707963705062866);
                    if (++m != 1000) continue;
                    villager.releasePoi(memoryModuleType);
                    memoryAccessor3.erase();
                    memoryAccessor.set(l);
                    return true;
                }
                memoryAccessor2.set(new WalkTarget(vec3, f, i));
            } else if (globalPos.pos().distManhattan(villager.blockPosition()) > i) {
                memoryAccessor2.set(new WalkTarget(globalPos.pos(), f, i));
            }
            return true;
        }));
    }
}

