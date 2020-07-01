/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

public class PiglinBruteSpecificSensor
extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEARBY_ADULT_PIGLINS);
    }

    @Override
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        Optional<Object> optional = Optional.empty();
        ArrayList<AbstractPiglin> list = Lists.newArrayList();
        List list2 = brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(ImmutableList.of());
        for (LivingEntity livingEntity2 : list2) {
            if (!(livingEntity2 instanceof WitherSkeleton) && !(livingEntity2 instanceof WitherBoss)) continue;
            optional = Optional.of((Mob)livingEntity2);
            break;
        }
        List list3 = brain.getMemory(MemoryModuleType.LIVING_ENTITIES).orElse(ImmutableList.of());
        for (LivingEntity livingEntity3 : list3) {
            if (!(livingEntity3 instanceof AbstractPiglin) || !((AbstractPiglin)livingEntity3).isAdult()) continue;
            list.add((AbstractPiglin)livingEntity3);
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
        brain.setMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS, list);
    }
}

