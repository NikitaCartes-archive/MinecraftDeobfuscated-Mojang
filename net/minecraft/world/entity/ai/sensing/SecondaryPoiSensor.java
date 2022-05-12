/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

public class SecondaryPoiSensor
extends Sensor<Villager> {
    private static final int SCAN_RATE = 40;

    public SecondaryPoiSensor() {
        super(40);
    }

    @Override
    protected void doTick(ServerLevel serverLevel, Villager villager) {
        ResourceKey<Level> resourceKey = serverLevel.dimension();
        BlockPos blockPos = villager.blockPosition();
        ArrayList<GlobalPos> list = Lists.newArrayList();
        int i = 4;
        for (int j = -4; j <= 4; ++j) {
            for (int k = -2; k <= 2; ++k) {
                for (int l = -4; l <= 4; ++l) {
                    BlockPos blockPos2 = blockPos.offset(j, k, l);
                    if (!villager.getVillagerData().getProfession().secondaryPoi().contains(serverLevel.getBlockState(blockPos2).getBlock())) continue;
                    list.add(GlobalPos.of(resourceKey, blockPos2));
                }
            }
        }
        Brain<Villager> brain = villager.getBrain();
        if (!list.isEmpty()) {
            brain.setMemory(MemoryModuleType.SECONDARY_JOB_SITE, list);
        } else {
            brain.eraseMemory(MemoryModuleType.SECONDARY_JOB_SITE);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.SECONDARY_JOB_SITE);
    }
}

