/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RandomSwim
extends RandomStroll {
    public static final int[][] XY_DISTANCE_TIERS = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

    public RandomSwim(float f) {
        super(f);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        return pathfinderMob.isInWaterOrBubble();
    }

    @Override
    @Nullable
    protected Vec3 getTargetPos(PathfinderMob pathfinderMob) {
        Vec3 vec3 = null;
        Vec3 vec32 = null;
        for (int[] is : XY_DISTANCE_TIERS) {
            vec32 = vec3 == null ? BehaviorUtils.getRandomSwimmablePos(pathfinderMob, is[0], is[1]) : pathfinderMob.position().add(pathfinderMob.position().vectorTo(vec3).normalize().multiply(is[0], is[1], is[0]));
            if (vec32 == null || pathfinderMob.level.getFluidState(new BlockPos(vec32)).isEmpty()) {
                return vec3;
            }
            vec3 = vec32;
        }
        return vec32;
    }
}

