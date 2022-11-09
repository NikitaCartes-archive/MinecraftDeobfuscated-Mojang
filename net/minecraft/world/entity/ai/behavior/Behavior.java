/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public abstract class Behavior<E extends LivingEntity>
implements BehaviorControl<E> {
    public static final int DEFAULT_DURATION = 60;
    protected final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
    private Status status = Status.STOPPED;
    private long endTimestamp;
    private final int minDuration;
    private final int maxDuration;

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> map) {
        this(map, 60);
    }

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> map, int i) {
        this(map, i, i);
    }

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> map, int i, int j) {
        this.minDuration = i;
        this.maxDuration = j;
        this.entryCondition = map;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public final boolean tryStart(ServerLevel serverLevel, E livingEntity, long l) {
        if (this.hasRequiredMemories(livingEntity) && this.checkExtraStartConditions(serverLevel, livingEntity)) {
            this.status = Status.RUNNING;
            int i = this.minDuration + serverLevel.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
            this.endTimestamp = l + (long)i;
            this.start(serverLevel, livingEntity, l);
            return true;
        }
        return false;
    }

    protected void start(ServerLevel serverLevel, E livingEntity, long l) {
    }

    @Override
    public final void tickOrStop(ServerLevel serverLevel, E livingEntity, long l) {
        if (!this.timedOut(l) && this.canStillUse(serverLevel, livingEntity, l)) {
            this.tick(serverLevel, livingEntity, l);
        } else {
            this.doStop(serverLevel, livingEntity, l);
        }
    }

    protected void tick(ServerLevel serverLevel, E livingEntity, long l) {
    }

    @Override
    public final void doStop(ServerLevel serverLevel, E livingEntity, long l) {
        this.status = Status.STOPPED;
        this.stop(serverLevel, livingEntity, l);
    }

    protected void stop(ServerLevel serverLevel, E livingEntity, long l) {
    }

    protected boolean canStillUse(ServerLevel serverLevel, E livingEntity, long l) {
        return false;
    }

    protected boolean timedOut(long l) {
        return l > this.endTimestamp;
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
        return true;
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }

    protected boolean hasRequiredMemories(E livingEntity) {
        for (Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : this.entryCondition.entrySet()) {
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            MemoryStatus memoryStatus = entry.getValue();
            if (((LivingEntity)livingEntity).getBrain().checkMemory(memoryModuleType, memoryStatus)) continue;
            return false;
        }
        return true;
    }

    public static enum Status {
        STOPPED,
        RUNNING;

    }
}

