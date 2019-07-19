/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class VillagerPanicTrigger
extends Behavior<Villager> {
    public VillagerPanicTrigger() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
        return VillagerPanicTrigger.isHurt(villager) || VillagerPanicTrigger.hasHostile(villager);
    }

    @Override
    protected void start(ServerLevel serverLevel, Villager villager, long l) {
        if (VillagerPanicTrigger.isHurt(villager) || VillagerPanicTrigger.hasHostile(villager)) {
            Brain<Villager> brain = villager.getBrain();
            if (!brain.isActive(Activity.PANIC)) {
                brain.eraseMemory(MemoryModuleType.PATH);
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
                brain.eraseMemory(MemoryModuleType.BREED_TARGET);
                brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
            }
            brain.setActivity(Activity.PANIC);
        }
    }

    @Override
    protected void tick(ServerLevel serverLevel, Villager villager, long l) {
        if (l % 100L == 0L) {
            villager.spawnGolemIfNeeded(l, 3);
        }
    }

    public static boolean hasHostile(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE);
    }

    public static boolean isHurt(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}

