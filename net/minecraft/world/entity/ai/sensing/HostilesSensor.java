/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

public abstract class HostilesSensor
extends Sensor<LivingEntity> {
    protected abstract Optional<LivingEntity> getNearestHostile(LivingEntity var1);

    protected abstract boolean isClose(LivingEntity var1, LivingEntity var2);

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_HOSTILE);
    }

    @Override
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        livingEntity.getBrain().setMemory(MemoryModuleType.NEAREST_HOSTILE, this.getNearestHostile(livingEntity));
    }

    protected Optional<List<LivingEntity>> getVisibleEntities(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
    }
}

