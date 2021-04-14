/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class HoglinSpecificSensor
extends Sensor<Hoglin> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, new MemoryModuleType[0]);
    }

    @Override
    protected void doTick(ServerLevel serverLevel, Hoglin hoglin) {
        Brain<Hoglin> brain = hoglin.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, this.findNearestRepellent(serverLevel, hoglin));
        Optional<Object> optional = Optional.empty();
        int i = 0;
        ArrayList<Hoglin> list = Lists.newArrayList();
        List list2 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList());
        for (LivingEntity livingEntity : list2) {
            if (livingEntity instanceof Piglin && !livingEntity.isBaby()) {
                ++i;
                if (!optional.isPresent()) {
                    optional = Optional.of((Piglin)livingEntity);
                }
            }
            if (!(livingEntity instanceof Hoglin) || livingEntity.isBaby()) continue;
            list.add((Hoglin)livingEntity);
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, optional);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, list);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, i);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, list.size());
    }

    private Optional<BlockPos> findNearestRepellent(ServerLevel serverLevel, Hoglin hoglin) {
        return BlockPos.findClosestMatch(hoglin.blockPosition(), 8, 4, blockPos -> serverLevel.getBlockState((BlockPos)blockPos).is(BlockTags.HOGLIN_REPELLENTS));
    }
}

