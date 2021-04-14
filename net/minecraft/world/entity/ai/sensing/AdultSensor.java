/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class AdultSensor
extends Sensor<AgeableMob> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

    @Override
    protected void doTick(ServerLevel serverLevel, AgeableMob ageableMob) {
        ageableMob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent(list -> this.setNearestVisibleAdult(ageableMob, (List<LivingEntity>)list));
    }

    private void setNearestVisibleAdult(AgeableMob ageableMob2, List<LivingEntity> list) {
        Optional<AgeableMob> optional = list.stream().filter(livingEntity -> livingEntity.getType() == ageableMob2.getType()).map(livingEntity -> (AgeableMob)livingEntity).filter(ageableMob -> !ageableMob.isBaby()).findFirst();
        ageableMob2.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, optional);
    }
}

