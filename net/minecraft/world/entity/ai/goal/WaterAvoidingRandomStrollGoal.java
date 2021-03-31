/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WaterAvoidingRandomStrollGoal
extends RandomStrollGoal {
    public static final float PROBABILITY = 0.001f;
    protected final float probability;

    public WaterAvoidingRandomStrollGoal(PathfinderMob pathfinderMob, double d) {
        this(pathfinderMob, d, 0.001f);
    }

    public WaterAvoidingRandomStrollGoal(PathfinderMob pathfinderMob, double d, float f) {
        super(pathfinderMob, d);
        this.probability = f;
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        if (this.mob.isInWaterOrBubble()) {
            Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
            return vec3 == null ? super.getPosition() : vec3;
        }
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return LandRandomPos.getPos(this.mob, 10, 7);
        }
        return super.getPosition();
    }
}

