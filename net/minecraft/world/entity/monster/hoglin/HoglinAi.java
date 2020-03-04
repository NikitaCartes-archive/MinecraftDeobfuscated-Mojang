/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
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

    protected static Brain<?> makeBrain(Hoglin hoglin, Dynamic<?> dynamic) {
        Brain<Hoglin> brain = new Brain<Hoglin>(Hoglin.MEMORY_TYPES, Hoglin.SENSOR_TYPES, dynamic);
        HoglinAi.initCoreActivity(hoglin, brain);
        HoglinAi.initIdleActivity(hoglin, brain);
        HoglinAi.initFightActivity(hoglin, brain);
        HoglinAi.initRetreatActivity(hoglin, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Hoglin hoglin, Brain<Hoglin> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(200)));
    }

    private static void initIdleActivity(Hoglin hoglin, Brain<Hoglin> brain) {
        float f = hoglin.getMovementSpeed();
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_REPELLENT, 200), new AnimalMakeLove(EntityType.HOGLIN), SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, f * 1.8f, 8, true), new StartAttacking<Hoglin>(HoglinAi::findNearestValidAttackTarget), new RunIf<PathfinderMob>(Hoglin::isAdult, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, f, 8, false)), new RunSometimes<LivingEntity>(new SetEntityLookTarget(8.0f), IntRange.of(30, 60)), HoglinAi.createIdleMovementBehaviors(f)));
    }

    private static void initFightActivity(Hoglin hoglin, Brain<Hoglin> brain) {
        float f = hoglin.getMovementSpeed();
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_REPELLENT, 200), new AnimalMakeLove(EntityType.HOGLIN), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(f * 1.8f), new RunIf<Mob>(Hoglin::isAdult, new MeleeAttack(1.5, 40)), new RunIf<Mob>(AgableMob::isBaby, new MeleeAttack(1.5, 15)), new StopAttackingIfTargetInvalid()), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initRetreatActivity(Hoglin hoglin, Brain<Hoglin> brain) {
        float f = hoglin.getMovementSpeed() * 2.0f;
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, f, 15, false), HoglinAi.createIdleMovementBehaviors(hoglin.getMovementSpeed()), new RunSometimes<LivingEntity>(new SetEntityLookTarget(8.0f), IntRange.of(30, 60)), new EraseMemoryIf<Hoglin>(HoglinAi::hoglinsOutnumberPiglins, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
    }

    private static RunOne<Hoglin> createIdleMovementBehaviors(float f) {
        return new RunOne(ImmutableList.of(Pair.of(new RandomStroll(f), 2), Pair.of(new SetWalkTargetFromLookTarget(f, 3), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    protected static void updateActivity(Hoglin hoglin) {
        Brain<Hoglin> brain = hoglin.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
        Activity activity2 = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != activity2) {
            HoglinAi.playActivitySound(hoglin);
        }
        hoglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    protected static void onHitTarget(Hoglin hoglin, LivingEntity livingEntity) {
        if (hoglin.isBaby()) {
            return;
        }
        if (livingEntity.getType() == EntityType.PIGLIN && !HoglinAi.hoglinsOutnumberPiglins(hoglin)) {
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

    private static boolean hoglinsOutnumberPiglins(Hoglin hoglin) {
        if (hoglin.isBaby()) {
            return false;
        }
        int i = hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
        int j = hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1;
        return j > i;
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
        hoglin.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        hoglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, livingEntity, 200L);
    }

    private static void broadcastAttackTarget(Hoglin hoglin2, LivingEntity livingEntity) {
        HoglinAi.getVisibleAdultHoglins(hoglin2).forEach(hoglin -> HoglinAi.setAttackTargetIfCloserThanCurrent(hoglin, livingEntity));
    }

    private static void setAttackTargetIfCloserThanCurrent(Hoglin hoglin, LivingEntity livingEntity) {
        Optional<LivingEntity> optional = hoglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        LivingEntity livingEntity2 = BehaviorUtils.getNearestTarget(hoglin, optional, livingEntity);
        HoglinAi.setAttackTarget(hoglin, livingEntity2);
    }

    private static void playActivitySound(Hoglin hoglin) {
        hoglin.getBrain().getActiveNonCoreActivity().ifPresent(activity -> {
            if (activity == Activity.AVOID) {
                hoglin.playRetreatSound();
            } else if (activity == Activity.FIGHT) {
                hoglin.playAngrySound();
            }
        });
    }

    protected static void maybePlayActivitySound(Hoglin hoglin) {
        if ((double)hoglin.level.random.nextFloat() < 0.0125) {
            HoglinAi.playActivitySound(hoglin);
        }
    }

    private static List<Hoglin> getVisibleAdultHoglins(Hoglin hoglin) {
        return hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
    }

    private static boolean isBreeding(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }

    protected static boolean isPacified(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
    }

    protected static boolean isIdle(Hoglin hoglin) {
        return hoglin.getBrain().isActive(Activity.IDLE);
    }
}

