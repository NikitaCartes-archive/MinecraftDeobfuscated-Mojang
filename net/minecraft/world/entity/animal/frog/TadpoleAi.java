/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RandomSwim;
import net.minecraft.world.entity.ai.behavior.RunIf;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.TryFindWater;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.schedule.Activity;

public class TadpoleAi {
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0f;
    private static final float SPEED_MULTIPLIER_ON_LAND = 0.15f;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING_IN_WATER = 0.5f;

    protected static Brain<?> makeBrain(Brain<Tadpole> brain) {
        TadpoleAi.initCoreActivity(brain);
        TadpoleAi.initIdleActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Tadpole> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new AnimalPanic(2.0f), new LookAtTargetSink(45, 90), new MoveToTargetSink(), new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<Tadpole> brain) {
        brain.addActivity(Activity.IDLE, ImmutableList.of(Pair.of(0, new RunSometimes<LivingEntity>(new SetEntityLookTarget(EntityType.PLAYER, 6.0f), UniformInt.of(30, 60))), Pair.of(3, new TryFindWater(6, 0.15f)), Pair.of(4, new GateBehavior(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), ImmutableSet.of(), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.TRY_ALL, ImmutableList.of(Pair.of(new RandomSwim(0.5f), 2), Pair.of(new RandomStroll(0.15f), 2), Pair.of(new SetWalkTargetFromLookTarget(0.5f, 3), 3), Pair.of(new RunIf<LivingEntity>(Entity::isInWaterOrBubble, new DoNothing(30, 60)), 5))))));
    }

    public static void updateActivity(Tadpole tadpole) {
        tadpole.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }
}

