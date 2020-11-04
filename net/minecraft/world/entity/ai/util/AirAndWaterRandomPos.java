/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AirAndWaterRandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j, int k, double d, double e, double f) {
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
        return RandomPos.generateRandomPos(pathfinderMob, () -> AirAndWaterRandomPos.generateRandomPos(pathfinderMob, i, j, k, d, e, f, bl));
    }

    @Nullable
    public static BlockPos generateRandomPos(PathfinderMob pathfinderMob, int i, int j, int k, double d, double e, double f, boolean bl) {
        BlockPos blockPos2 = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), i, j, k, d, e, f);
        if (blockPos2 == null) {
            return null;
        }
        BlockPos blockPos22 = RandomPos.generateRandomPosTowardDirection(pathfinderMob, i, pathfinderMob.getRandom(), blockPos2);
        if (GoalUtils.isOutsideLimits(blockPos22, pathfinderMob) || GoalUtils.isRestricted(bl, pathfinderMob, blockPos22)) {
            return null;
        }
        if (GoalUtils.hasMalus(pathfinderMob, blockPos22 = RandomPos.moveUpOutOfSolid(blockPos22, pathfinderMob.level.getMaxBuildHeight(), blockPos -> GoalUtils.isSolid(pathfinderMob, blockPos)))) {
            return null;
        }
        return blockPos22;
    }
}

