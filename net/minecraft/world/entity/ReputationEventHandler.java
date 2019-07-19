/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.ReputationEventType;

public interface ReputationEventHandler {
    public void onReputationEventFrom(ReputationEventType var1, Entity var2);
}

