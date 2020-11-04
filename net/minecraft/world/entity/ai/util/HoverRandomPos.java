/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HoverRandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j, double d, double e, float f, int k, int l) {
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
        return RandomPos.generateRandomPos(pathfinderMob, () -> {
            BlockPos blockPos2 = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), i, j, 0, d, e, f);
            if (blockPos2 == null) {
                return null;
            }
            BlockPos blockPos22 = LandRandomPos.generateRandomPosTowardDirection(pathfinderMob, i, bl, blockPos2);
            if (blockPos22 == null) {
                return null;
            }
            if (GoalUtils.isWater(pathfinderMob, blockPos22 = RandomPos.moveUpToAboveSolid(blockPos22, pathfinderMob.getRandom().nextInt(k - l + 1) + l, pathfinderMob.level.getMaxBuildHeight(), blockPos -> GoalUtils.isSolid(pathfinderMob, blockPos))) || GoalUtils.hasMalus(pathfinderMob, blockPos22)) {
                return null;
            }
            return blockPos22;
        });
    }
}

