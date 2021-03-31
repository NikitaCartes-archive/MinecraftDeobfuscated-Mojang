/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class InteractGoal
extends LookAtPlayerGoal {
    public InteractGoal(Mob mob, Class<? extends LivingEntity> class_, float f) {
        super(mob, class_, f);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }

    public InteractGoal(Mob mob, Class<? extends LivingEntity> class_, float f, float g) {
        super(mob, class_, f, g);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }
}

