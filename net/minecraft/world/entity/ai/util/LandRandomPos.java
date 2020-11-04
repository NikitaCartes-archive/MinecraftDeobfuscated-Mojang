/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.util;

import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LandRandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j) {
        return LandRandomPos.getPos(pathfinderMob, i, j, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j, ToDoubleFunction<BlockPos> toDoubleFunction) {
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
        return RandomPos.generateRandomPos(() -> {
            BlockPos blockPos = RandomPos.generateRandomDirection(pathfinderMob.getRandom(), i, j);
            BlockPos blockPos2 = LandRandomPos.generateRandomPosTowardDirection(pathfinderMob, i, bl, blockPos);
            if (blockPos2 == null) {
                return null;
            }
            return LandRandomPos.movePosUpOutOfSolid(pathfinderMob, blockPos2);
        }, toDoubleFunction);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
        Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
        return LandRandomPos.getPosInDirection(pathfinderMob, i, j, vec32, bl);
    }

    @Nullable
    public static Vec3 getPosAway(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
        Vec3 vec32 = pathfinderMob.position().subtract(vec3);
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
        return LandRandomPos.getPosInDirection(pathfinderMob, i, j, vec32, bl);
    }

    @Nullable
    private static Vec3 getPosInDirection(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3, boolean bl) {
        return RandomPos.generateRandomPos(pathfinderMob, () -> {
            BlockPos blockPos = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), i, j, 0, vec3.x, vec3.z, 1.5707963705062866);
            if (blockPos == null) {
                return null;
            }
            BlockPos blockPos2 = LandRandomPos.generateRandomPosTowardDirection(pathfinderMob, i, bl, blockPos);
            if (blockPos2 == null) {
                return null;
            }
            return LandRandomPos.movePosUpOutOfSolid(pathfinderMob, blockPos2);
        });
    }

    @Nullable
    public static BlockPos movePosUpOutOfSolid(PathfinderMob pathfinderMob, BlockPos blockPos2) {
        if (GoalUtils.isWater(pathfinderMob, blockPos2 = RandomPos.moveUpOutOfSolid(blockPos2, pathfinderMob.level.getMaxBuildHeight(), blockPos -> GoalUtils.isSolid(pathfinderMob, blockPos))) || GoalUtils.hasMalus(pathfinderMob, blockPos2)) {
            return null;
        }
        return blockPos2;
    }

    @Nullable
    public static BlockPos generateRandomPosTowardDirection(PathfinderMob pathfinderMob, int i, boolean bl, BlockPos blockPos) {
        BlockPos blockPos2 = RandomPos.generateRandomPosTowardDirection(pathfinderMob, i, pathfinderMob.getRandom(), blockPos);
        if (GoalUtils.isOutsideLimits(blockPos2, pathfinderMob) || GoalUtils.isRestricted(bl, pathfinderMob, blockPos2) || GoalUtils.isNotStable(pathfinderMob.getNavigation(), blockPos2)) {
            return null;
        }
        return blockPos2;
    }
}

