/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class AdultSensor
extends Sensor<AgeableMob> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

    @Override
    protected void doTick(ServerLevel serverLevel, AgeableMob ageableMob) {
        ageableMob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent(nearestVisibleLivingEntities -> this.setNearestVisibleAdult(ageableMob, (NearestVisibleLivingEntities)nearestVisibleLivingEntities));
    }

    private void setNearestVisibleAdult(AgeableMob ageableMob, NearestVisibleLivingEntities nearestVisibleLivingEntities) {
        Optional<AgeableMob> optional = nearestVisibleLivingEntities.findClosest(livingEntity -> livingEntity.getType() == ageableMob.getType() && !livingEntity.isBaby()).map(AgeableMob.class::cast);
        ageableMob.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, optional);
    }
}

