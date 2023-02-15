/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.sniffer;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.schedule.Activity;
import org.slf4j.Logger;

public class SnifferAi {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_LOOK_DISTANCE = 6;
    static final List<SensorType<? extends Sensor<? super Sniffer>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_PLAYERS);
    static final List<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.IS_PANICKING, MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryModuleType.SNIFFER_DIGGING, MemoryModuleType.SNIFFER_HAPPY, MemoryModuleType.SNIFF_COOLDOWN, MemoryModuleType.SNIFFER_EXPLORED_POSITIONS, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.BREED_TARGET, new MemoryModuleType[0]);
    private static final int SNIFFING_COOLDOWN_TICKS = 9600;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0f;
    private static final float SPEED_MULTIPLIER_WHEN_SNIFFING = 1.25f;

    protected static Brain<?> makeBrain(Brain<Sniffer> brain) {
        SnifferAi.initCoreActivity(brain);
        SnifferAi.initIdleActivity(brain);
        SnifferAi.initSniffingActivity(brain);
        SnifferAi.initDigActivity(brain);
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Sniffer> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8f), new AnimalPanic(2.0f){

            @Override
            protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
                pathfinderMob.getBrain().eraseMemory(MemoryModuleType.SNIFFER_DIGGING);
                pathfinderMob.getBrain().eraseMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
                ((Sniffer)pathfinderMob).transitionTo(Sniffer.State.IDLING);
                super.start(serverLevel, pathfinderMob, l);
            }

            @Override
            protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
                this.start(serverLevel, (PathfinderMob)livingEntity, l);
            }
        }, new MoveToTargetSink(10000, 15000)));
    }

    private static void initSniffingActivity(Brain<Sniffer> brain) {
        brain.addActivityWithConditions(Activity.SNIFF, ImmutableList.of(Pair.of(0, new Searching())), Set.of(Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT)));
    }

    private static void initDigActivity(Brain<Sniffer> brain) {
        brain.addActivityWithConditions(Activity.DIG, ImmutableList.of(Pair.of(0, new Digging(160, 180)), Pair.of(0, new FinishedDigging(40))), Set.of(Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_PRESENT)));
    }

    private static void initIdleActivity(Brain<Sniffer> brain) {
        brain.addActivityWithConditions(Activity.IDLE, ImmutableList.of(Pair.of(0, new LookAtTargetSink(45, 90)), Pair.of(0, new FeelingHappy(40, 100)), Pair.of(0, new RunOne(ImmutableList.of(Pair.of(SetWalkTargetFromLookTarget.create(1.0f, 3), 2), Pair.of(new Scenting(40, 80), 1), Pair.of(new Sniffing(40, 80), 1), Pair.of(new AnimalMakeLove(EntityType.SNIFFER, 1.0f), 1), Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 6.0f), 1), Pair.of(RandomStroll.stroll(1.0f), 1), Pair.of(new DoNothing(5, 20), 2))))), Set.of(Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_ABSENT)));
    }

    static void updateActivity(Sniffer sniffer) {
        sniffer.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.DIG, Activity.SNIFF, Activity.IDLE));
    }

    static class Searching
    extends Behavior<Sniffer> {
        Searching() {
            super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT, MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryStatus.VALUE_PRESENT), 600);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel serverLevel, Sniffer sniffer) {
            return !sniffer.isPanicking() && !sniffer.isInWater();
        }

        @Override
        protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
            if (sniffer.isPanicking() && !sniffer.isInWater()) {
                return false;
            }
            Optional<BlockPos> optional = sniffer.getBrain().getMemory(MemoryModuleType.WALK_TARGET).map(WalkTarget::getTarget).map(PositionTracker::currentBlockPosition);
            Optional<BlockPos> optional2 = sniffer.getBrain().getMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
            if (optional.isEmpty() || optional2.isEmpty()) {
                return false;
            }
            return optional2.get().equals(optional.get());
        }

        @Override
        protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
            sniffer.transitionTo(Sniffer.State.SEARCHING);
        }

        @Override
        protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
            if (sniffer.canDig()) {
                sniffer.getBrain().setMemory(MemoryModuleType.SNIFFER_DIGGING, true);
            }
            sniffer.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
        }

        @Override
        protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.stop(serverLevel, (Sniffer)livingEntity, l);
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.start(serverLevel, (Sniffer)livingEntity, l);
        }
    }

    static class Digging
    extends Behavior<Sniffer> {
        Digging(int i, int j) {
            super(Map.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_PRESENT, MemoryModuleType.SNIFF_COOLDOWN, MemoryStatus.VALUE_ABSENT), i, j);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel serverLevel, Sniffer sniffer) {
            return !sniffer.isPanicking() && !sniffer.isInWater();
        }

        @Override
        protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
            return sniffer.getBrain().getMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent() && !sniffer.isPanicking();
        }

        @Override
        protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
            sniffer.transitionTo(Sniffer.State.DIGGING);
        }

        @Override
        protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
            sniffer.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 9600L);
        }

        @Override
        protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.stop(serverLevel, (Sniffer)livingEntity, l);
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.start(serverLevel, (Sniffer)livingEntity, l);
        }
    }

    static class FinishedDigging
    extends Behavior<Sniffer> {
        FinishedDigging(int i) {
            super(Map.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_PRESENT, MemoryModuleType.SNIFF_COOLDOWN, MemoryStatus.VALUE_PRESENT), i, i);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel serverLevel, Sniffer sniffer) {
            return true;
        }

        @Override
        protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
            return sniffer.getBrain().getMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent();
        }

        @Override
        protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
            sniffer.transitionTo(Sniffer.State.RISING);
        }

        @Override
        protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
            boolean bl = this.timedOut(l);
            sniffer.transitionTo(Sniffer.State.IDLING).onDiggingComplete(bl);
            sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_DIGGING);
            sniffer.getBrain().setMemory(MemoryModuleType.SNIFFER_HAPPY, true);
        }

        @Override
        protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.stop(serverLevel, (Sniffer)livingEntity, l);
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.start(serverLevel, (Sniffer)livingEntity, l);
        }
    }

    static class FeelingHappy
    extends Behavior<Sniffer> {
        FeelingHappy(int i, int j) {
            super(Map.of(MemoryModuleType.SNIFFER_HAPPY, MemoryStatus.VALUE_PRESENT), i, j);
        }

        @Override
        protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
            return true;
        }

        @Override
        protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
            sniffer.transitionTo(Sniffer.State.FEELING_HAPPY);
        }

        @Override
        protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
            sniffer.transitionTo(Sniffer.State.IDLING);
            sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_HAPPY);
        }

        @Override
        protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.stop(serverLevel, (Sniffer)livingEntity, l);
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.start(serverLevel, (Sniffer)livingEntity, l);
        }
    }

    static class Scenting
    extends Behavior<Sniffer> {
        Scenting(int i, int j) {
            super(Map.of(MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_ABSENT, MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.SNIFFER_HAPPY, MemoryStatus.VALUE_ABSENT), i, j);
        }

        @Override
        protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
            return true;
        }

        @Override
        protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
            sniffer.transitionTo(Sniffer.State.SCENTING);
        }

        @Override
        protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
            sniffer.transitionTo(Sniffer.State.IDLING);
        }

        @Override
        protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.stop(serverLevel, (Sniffer)livingEntity, l);
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.start(serverLevel, (Sniffer)livingEntity, l);
        }
    }

    static class Sniffing
    extends Behavior<Sniffer> {
        Sniffing(int i, int j) {
            super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.SNIFF_COOLDOWN, MemoryStatus.VALUE_ABSENT), i, j);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel serverLevel, Sniffer sniffer) {
            return !sniffer.isBaby() && !sniffer.isInWater();
        }

        @Override
        protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
            return !sniffer.isPanicking();
        }

        @Override
        protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
            sniffer.transitionTo(Sniffer.State.SNIFFING);
        }

        @Override
        protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
            boolean bl = this.timedOut(l);
            sniffer.transitionTo(Sniffer.State.IDLING);
            if (bl) {
                sniffer.calculateDigPosition().ifPresent(blockPos -> {
                    sniffer.getBrain().setMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET, blockPos);
                    sniffer.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget((BlockPos)blockPos, 1.25f, 0));
                });
            }
        }

        @Override
        protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.stop(serverLevel, (Sniffer)livingEntity, l);
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.start(serverLevel, (Sniffer)livingEntity, l);
        }
    }
}

