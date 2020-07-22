/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.schedule.Activity;

public class PiglinBruteAi {
    protected static Brain<?> makeBrain(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
        PiglinBruteAi.initCoreActivity(piglinBrute, brain);
        PiglinBruteAi.initIdleActivity(piglinBrute, brain);
        PiglinBruteAi.initFightActivity(piglinBrute, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    protected static void initMemories(PiglinBrute piglinBrute) {
        GlobalPos globalPos = GlobalPos.of(piglinBrute.level.dimension(), piglinBrute.blockPosition());
        piglinBrute.getBrain().setMemory(MemoryModuleType.HOME, globalPos);
    }

    private static void initCoreActivity(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), new InteractWithDoor(), new StopBeingAngryIfTargetDead()));
    }

    private static void initIdleActivity(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(new StartAttacking<PiglinBrute>(PiglinBruteAi::findNearestValidAttackTarget), PiglinBruteAi.createIdleLookBehaviors(), PiglinBruteAi.createIdleMovementBehaviors(), new SetLookAndInteract(EntityType.PLAYER, 4)));
    }

    private static void initFightActivity(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(new StopAttackingIfTargetInvalid(livingEntity -> !PiglinBruteAi.isNearestValidAttackTarget(piglinBrute, livingEntity)), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0f), new MeleeAttack(20)), MemoryModuleType.ATTACK_TARGET);
    }

    private static RunOne<PiglinBrute> createIdleLookBehaviors() {
        return new RunOne(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0f), 1), Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0f), 1), Pair.of(new SetEntityLookTarget(EntityType.PIGLIN_BRUTE, 8.0f), 1), Pair.of(new SetEntityLookTarget(8.0f), 1), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<PiglinBrute> createIdleMovementBehaviors() {
        return new RunOne(ImmutableList.of(Pair.of(new RandomStroll(0.6f), 2), Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6f, 2), 2), Pair.of(InteractWith.of(EntityType.PIGLIN_BRUTE, 8, MemoryModuleType.INTERACTION_TARGET, 0.6f, 2), 2), Pair.of(new StrollToPoi(MemoryModuleType.HOME, 0.6f, 2, 100), 2), Pair.of(new StrollAroundPoi(MemoryModuleType.HOME, 0.6f, 5), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    protected static void updateActivity(PiglinBrute piglinBrute) {
        Brain<PiglinBrute> brain = piglinBrute.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        Activity activity2 = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != activity2) {
            PiglinBruteAi.playActivitySound(piglinBrute);
        }
        piglinBrute.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    private static boolean isNearestValidAttackTarget(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
        return PiglinBruteAi.findNearestValidAttackTarget(abstractPiglin).filter(livingEntity2 -> livingEntity2 == livingEntity).isPresent();
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(AbstractPiglin abstractPiglin) {
        Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(abstractPiglin, MemoryModuleType.ANGRY_AT);
        if (optional.isPresent() && PiglinBruteAi.isAttackAllowed(optional.get())) {
            return optional;
        }
        Optional<? extends LivingEntity> optional2 = PiglinBruteAi.getTargetIfWithinRange(abstractPiglin, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
        if (optional2.isPresent()) {
            return optional2;
        }
        return abstractPiglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
    }

    private static boolean isAttackAllowed(LivingEntity livingEntity) {
        return EntitySelector.ATTACK_ALLOWED.test(livingEntity);
    }

    private static Optional<? extends LivingEntity> getTargetIfWithinRange(AbstractPiglin abstractPiglin, MemoryModuleType<? extends LivingEntity> memoryModuleType) {
        return abstractPiglin.getBrain().getMemory(memoryModuleType).filter(livingEntity -> livingEntity.closerThan(abstractPiglin, 12.0));
    }

    protected static void wasHurtBy(PiglinBrute piglinBrute, LivingEntity livingEntity) {
        if (livingEntity instanceof AbstractPiglin) {
            return;
        }
        PiglinAi.maybeRetaliate(piglinBrute, livingEntity);
    }

    protected static void maybePlayActivitySound(PiglinBrute piglinBrute) {
        if ((double)piglinBrute.level.random.nextFloat() < 0.0125) {
            PiglinBruteAi.playActivitySound(piglinBrute);
        }
    }

    private static void playActivitySound(PiglinBrute piglinBrute) {
        piglinBrute.getBrain().getActiveNonCoreActivity().ifPresent(activity -> {
            if (activity == Activity.FIGHT) {
                piglinBrute.playAngrySound();
            }
        });
    }
}

