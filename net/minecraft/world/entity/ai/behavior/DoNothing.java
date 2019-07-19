/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;

public class DoNothing
extends Behavior<LivingEntity> {
    public DoNothing(int i, int j) {
        super(ImmutableMap.of(), i, j);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return true;
    }
}

