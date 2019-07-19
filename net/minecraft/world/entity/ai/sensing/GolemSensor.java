/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class GolemSensor
extends Sensor<LivingEntity> {
    public GolemSensor() {
        this(200);
    }

    public GolemSensor(int i) {
        super(i);
    }

    @Override
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        GolemSensor.checkForNearbyGolem(serverLevel.getGameTime(), livingEntity);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.LIVING_ENTITIES);
    }

    public static void checkForNearbyGolem(long l, LivingEntity livingEntity2) {
        Brain<?> brain = livingEntity2.getBrain();
        Optional<List<LivingEntity>> optional = brain.getMemory(MemoryModuleType.LIVING_ENTITIES);
        if (!optional.isPresent()) {
            return;
        }
        boolean bl = optional.get().stream().anyMatch(livingEntity -> livingEntity.getType().equals(EntityType.IRON_GOLEM));
        if (bl) {
            brain.setMemory(MemoryModuleType.GOLEM_LAST_SEEN_TIME, l);
        }
    }
}

