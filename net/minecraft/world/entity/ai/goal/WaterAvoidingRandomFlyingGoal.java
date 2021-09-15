/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WaterAvoidingRandomFlyingGoal
extends WaterAvoidingRandomStrollGoal {
    public WaterAvoidingRandomFlyingGoal(PathfinderMob pathfinderMob, double d) {
        super(pathfinderMob, d);
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        Vec3 vec3 = this.mob.getViewVector(0.0f);
        int i = 8;
        Vec3 vec32 = HoverRandomPos.getPos(this.mob, 8, 7, vec3.x, vec3.z, 1.5707964f, 3, 1);
        if (vec32 != null) {
            return vec32;
        }
        return AirAndWaterRandomPos.getPos(this.mob, 8, 4, -2, vec3.x, vec3.z, 1.5707963705062866);
    }
}

