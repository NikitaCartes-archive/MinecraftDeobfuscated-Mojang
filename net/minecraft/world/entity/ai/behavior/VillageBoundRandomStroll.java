/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class VillageBoundRandomStroll {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;

    public static OneShot<PathfinderMob> create(float f) {
        return VillageBoundRandomStroll.create(f, 10, 7);
    }

    public static OneShot<PathfinderMob> create(float f, int i, int j) {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET)).apply(instance, memoryAccessor -> (serverLevel, pathfinderMob, l) -> {
            SectionPos sectionPos;
            SectionPos sectionPos2;
            BlockPos blockPos = pathfinderMob.blockPosition();
            Vec3 vec32 = serverLevel.isVillage(blockPos) ? LandRandomPos.getPos(pathfinderMob, i, j) : ((sectionPos2 = BehaviorUtils.findSectionClosestToVillage(serverLevel, sectionPos = SectionPos.of(blockPos), 2)) != sectionPos ? DefaultRandomPos.getPosTowards(pathfinderMob, i, j, Vec3.atBottomCenterOf(sectionPos2.center()), 1.5707963705062866) : LandRandomPos.getPos(pathfinderMob, i, j));
            memoryAccessor.setOrErase(Optional.ofNullable(vec32).map(vec3 -> new WalkTarget((Vec3)vec3, f, 0)));
            return true;
        }));
    }
}

