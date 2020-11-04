/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AirRandomPos {
    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, int k, Vec3 vec3, double d) {
        Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
        boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
        return RandomPos.generateRandomPos(pathfinderMob, () -> {
            BlockPos blockPos = AirAndWaterRandomPos.generateRandomPos(pathfinderMob, i, j, k, vec3.x, vec3.z, d, bl);
            if (blockPos == null || GoalUtils.isWater(pathfinderMob, blockPos)) {
                return null;
            }
            return blockPos;
        });
    }
}

