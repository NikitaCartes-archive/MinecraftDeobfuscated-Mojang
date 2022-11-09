/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;

public interface BehaviorControl<E extends LivingEntity> {
    public Behavior.Status getStatus();

    public boolean tryStart(ServerLevel var1, E var2, long var3);

    public void tickOrStop(ServerLevel var1, E var2, long var3);

    public void doStop(ServerLevel var1, E var2, long var3);

    public String debugString();
}

