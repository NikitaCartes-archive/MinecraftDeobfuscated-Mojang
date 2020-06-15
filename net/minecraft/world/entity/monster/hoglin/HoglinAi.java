/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.IntRange;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.BecomePassiveIfMemoryPresent;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunIf;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.schedule.Activity;

public class HoglinAi {
    private static final IntRange RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final IntRange ADULT_FOLLOW_RANGE = IntRange.of(5, 16);

    protected static Brain<?> makeBrain(Brain<Hoglin> brain) {
        HoglinAi.initCoreActivity(brain);
        HoglinAi.initIdleActivity(brain);
        HoglinAi.initFightActivity(brain);
        HoglinAi.initRetreatActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Hoglin> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(200)));
    }

    private static void initIdleActivity(Brain<Hoglin> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_REPELLENT, 200), new AnimalMakeLove(EntityType.HOGLIN, 0.6f), SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0f, 8, true), new StartAttacking<Hoglin>(HoglinAi::findNearestValidAttackTarget), new RunIf<PathfinderMob>(Hoglin::isAdult, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, 0.4f, 8, false)), new RunSometimes<LivingEntity>(new SetEntityLookTarget(8.0f), IntRange.of(30, 60)), new BabyFollowAdult(ADULT_FOLLOW_RANGE, 0.6f), HoglinAi.createIdleMovementBehaviors()));
    }

    private static void initFightActivity(Brain<Hoglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_REPELLENT, 200), new AnimalMakeLove(EntityType.HOGLIN, 0.6f), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0f), new RunIf<Mob>(Hoglin::isAdult, new MeleeAttack(40)), new RunIf<Mob>(AgableMob::isBaby, new MeleeAttack(15)), new StopAttackingIfTargetInvalid(), new EraseMemoryIf<Hoglin>(HoglinAi::isBreeding, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initRetreatActivity(Brain<Hoglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.3f, 15, false), HoglinAi.createIdleMovementBehaviors(), new RunSometimes<LivingEntity>(new SetEntityLookTarget(8.0f), IntRange.of(30, 60)), new EraseMemoryIf<Hoglin>(HoglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
    }

    private static RunOne<Hoglin> createIdleMovementBehaviors() {
        return new RunOne(ImmutableList.of(Pair.of(new RandomStroll(0.4f), 2), Pair.of(new SetWalkTargetFromLookTarget(0.4f, 3), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    protected static void updateActivity(Hoglin hoglin) {
        Brain<Hoglin> brain = hoglin.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
        Activity activity2 = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != activity2) {
            HoglinAi.getSoundForCurrentActivity(hoglin).ifPresent(hoglin::playSound);
        }
        hoglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    protected static void onHitTarget(Hoglin hoglin, LivingEntity livingEntity) {
        if (hoglin.isBaby()) {
            return;
        }
        if (livingEntity.getType() == EntityType.PIGLIN && HoglinAi.piglinsOutnumberHoglins(hoglin)) {
            HoglinAi.setAvoidTarget(hoglin, livingEntity);
            HoglinAi.broadcastRetreat(hoglin, livingEntity);
            return;
        }
        HoglinAi.broadcastAttackTarget(hoglin, livingEntity);
    }

    private static void broadcastRetreat(Hoglin hoglin2, LivingEntity livingEntity) {
        HoglinAi.getVisibleAdultHoglins(hoglin2).forEach(hoglin -> HoglinAi.retreatFromNearestTarget(hoglin, livingEntity));
    }

    private static void retreatFromNearestTarget(Hoglin hoglin, LivingEntity livingEntity) {
        LivingEntity livingEntity2 = livingEntity;
        Brain<Hoglin> brain = hoglin.getBrain();
        livingEntity2 = BehaviorUtils.getNearestTarget(hoglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), livingEntity2);
        livingEntity2 = BehaviorUtils.getNearestTarget(hoglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), livingEntity2);
        HoglinAi.setAvoidTarget(hoglin, livingEntity2);
    }

    private static void setAvoidTarget(Hoglin hoglin, LivingEntity livingEntity) {
        hoglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        hoglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        hoglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, RETREAT_DURATION.randomValue(hoglin.level.random));
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Hoglin hoglin) {
        if (HoglinAi.isPacified(hoglin) || HoglinAi.isBreeding(hoglin)) {
            return Optional.empty();
        }
        return hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
    }

    static boolean isPosNearNearestRepellent(Hoglin hoglin, BlockPos blockPos) {
        Optional<BlockPos> optional = hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_REPELLENT);
        return optional.isPresent() && optional.get().closerThan(blockPos, 8.0);
    }

    private static boolean wantsToStopFleeing(Hoglin hoglin) {
        return hoglin.isAdult() && !HoglinAi.piglinsOutnumberHoglins(hoglin);
    }

    private static boolean piglinsOutnumberHoglins(Hoglin hoglin) {
        int j;
        if (hoglin.isBaby()) {
            return false;
        }
        int i = hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
        return i > (j = hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1);
    }

    protected static void wasHurtBy(Hoglin hoglin, LivingEntity livingEntity) {
        Brain<Hoglin> brain = hoglin.getBrain();
        brain.eraseMemory(MemoryModuleType.PACIFIED);
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        if (hoglin.isBaby()) {
            HoglinAi.retreatFromNearestTarget(hoglin, livingEntity);
            return;
        }
        HoglinAi.maybeRetaliate(hoglin, livingEntity);
    }

    private static void maybeRetaliate(Hoglin hoglin, LivingEntity livingEntity) {
        if (hoglin.getBrain().isActive(Activity.AVOID) && livingEntity.getType() == EntityType.PIGLIN) {
            return;
        }
        if (!EntitySelector.ATTACK_ALLOWED.test(livingEntity)) {
            return;
        }
        if (livingEntity.getType() == EntityType.HOGLIN) {
            return;
        }
        if (BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(hoglin, livingEntity, 4.0)) {
            return;
        }
        HoglinAi.setAttackTarget(hoglin, livingEntity);
        HoglinAi.broadcastAttackTarget(hoglin, livingEntity);
    }

    private static void setAttackTarget(Hoglin hoglin, LivingEntity livingEntity) {
        Brain<Hoglin> brain = hoglin.getBrain();
        brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, livingEntity, 200L);
    }

    private static void broadcastAttackTarget(Hoglin hoglin2, LivingEntity livingEntity) {
        HoglinAi.getVisibleAdultHoglins(hoglin2).forEach(hoglin -> HoglinAi.setAttackTargetIfCloserThanCurrent(hoglin, livingEntity));
    }

    private static void setAttackTargetIfCloserThanCurrent(Hoglin hoglin, LivingEntity livingEntity) {
        if (HoglinAi.isPacified(hoglin)) {
            return;
        }
        Optional<LivingEntity> optional = hoglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        LivingEntity livingEntity2 = BehaviorUtils.getNearestTarget(hoglin, optional, livingEntity);
        HoglinAi.setAttackTarget(hoglin, livingEntity2);
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Hoglin hoglin) {
        return hoglin.getBrain().getActiveNonCoreActivity().map(activity -> HoglinAi.getSoundForActivity(hoglin, activity));
    }

    private static SoundEvent getSoundForActivity(Hoglin hoglin, Activity activity) {
        if (activity == Activity.AVOID || hoglin.isConverting()) {
            return SoundEvents.HOGLIN_RETREAT;
        }
        if (activity == Activity.FIGHT) {
            return SoundEvents.HOGLIN_ANGRY;
        }
        if (HoglinAi.isNearRepellent(hoglin)) {
            return SoundEvents.HOGLIN_RETREAT;
        }
        return SoundEvents.HOGLIN_AMBIENT;
    }

    private static List<Hoglin> getVisibleAdultHoglins(Hoglin hoglin) {
        return hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
    }

    private static boolean isNearRepellent(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean isBreeding(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }

    protected static boolean isPacified(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
    }
}

