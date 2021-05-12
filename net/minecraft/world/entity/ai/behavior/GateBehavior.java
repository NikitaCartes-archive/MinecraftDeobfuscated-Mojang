/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GateBehavior<E extends LivingEntity>
extends Behavior<E> {
    private final Set<MemoryModuleType<?>> exitErasedMemories;
    private final OrderPolicy orderPolicy;
    private final RunningPolicy runningPolicy;
    private final ShufflingList<Behavior<? super E>> behaviors = new ShufflingList();

    public GateBehavior(Map<MemoryModuleType<?>, MemoryStatus> map, Set<MemoryModuleType<?>> set, OrderPolicy orderPolicy, RunningPolicy runningPolicy, List<Pair<Behavior<? super E>, Integer>> list) {
        super(map);
        this.exitErasedMemories = set;
        this.orderPolicy = orderPolicy;
        this.runningPolicy = runningPolicy;
        list.forEach(pair -> this.behaviors.add((Behavior)pair.getFirst(), (Integer)pair.getSecond()));
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, E livingEntity, long l) {
        return this.behaviors.stream().filter(behavior -> behavior.getStatus() == Behavior.Status.RUNNING).anyMatch(behavior -> behavior.canStillUse(serverLevel, livingEntity, l));
    }

    @Override
    protected boolean timedOut(long l) {
        return false;
    }

    @Override
    protected void start(ServerLevel serverLevel, E livingEntity, long l) {
        this.orderPolicy.apply(this.behaviors);
        this.runningPolicy.apply(this.behaviors.stream(), serverLevel, livingEntity, l);
    }

    @Override
    protected void tick(ServerLevel serverLevel, E livingEntity, long l) {
        this.behaviors.stream().filter(behavior -> behavior.getStatus() == Behavior.Status.RUNNING).forEach(behavior -> behavior.tickOrStop(serverLevel, livingEntity, l));
    }

    @Override
    protected void stop(ServerLevel serverLevel, E livingEntity, long l) {
        this.behaviors.stream().filter(behavior -> behavior.getStatus() == Behavior.Status.RUNNING).forEach(behavior -> behavior.doStop(serverLevel, livingEntity, l));
        this.exitErasedMemories.forEach(((LivingEntity)livingEntity).getBrain()::eraseMemory);
    }

    @Override
    public String toString() {
        Set set = this.behaviors.stream().filter(behavior -> behavior.getStatus() == Behavior.Status.RUNNING).collect(Collectors.toSet());
        return "(" + this.getClass().getSimpleName() + "): " + set;
    }

    public static enum OrderPolicy {
        ORDERED(shufflingList -> {}),
        SHUFFLED(ShufflingList::shuffle);

        private final Consumer<ShufflingList<?>> consumer;

        private OrderPolicy(Consumer<ShufflingList<?>> consumer) {
            this.consumer = consumer;
        }

        public void apply(ShufflingList<?> shufflingList) {
            this.consumer.accept(shufflingList);
        }
    }

    public static enum RunningPolicy {
        RUN_ONE{

            @Override
            public <E extends LivingEntity> void apply(Stream<Behavior<? super E>> stream, ServerLevel serverLevel, E livingEntity, long l) {
                stream.filter(behavior -> behavior.getStatus() == Behavior.Status.STOPPED).filter(behavior -> behavior.tryStart(serverLevel, livingEntity, l)).findFirst();
            }
        }
        ,
        TRY_ALL{

            @Override
            public <E extends LivingEntity> void apply(Stream<Behavior<? super E>> stream, ServerLevel serverLevel, E livingEntity, long l) {
                stream.filter(behavior -> behavior.getStatus() == Behavior.Status.STOPPED).forEach(behavior -> behavior.tryStart(serverLevel, livingEntity, l));
            }
        };


        public abstract <E extends LivingEntity> void apply(Stream<Behavior<? super E>> var1, ServerLevel var2, E var3, long var4);
    }
}

