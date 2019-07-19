/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class DummySensor
extends Sensor<LivingEntity> {
    @Override
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of();
    }
}

