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

public class RandomSwim
extends RandomStroll {
    public RandomSwim(float f) {
        super(f);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        return pathfinderMob.isInWaterOrBubble();
    }

    @Override
    protected Vec3 getTargetPos(PathfinderMob pathfinderMob) {
        Vec3 vec3 = BehaviorUtils.getRandomSwimmablePos(pathfinderMob, this.maxHorizontalDistance, this.maxVerticalDistance);
        if (vec3 != null && pathfinderMob.level.getFluidState(new BlockPos(vec3)).isEmpty()) {
            return null;
        }
        return vec3;
    }
}

