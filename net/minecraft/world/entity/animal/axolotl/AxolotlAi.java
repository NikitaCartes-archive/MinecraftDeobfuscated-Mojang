/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IntRange;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.CountDownTemptationTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RandomSwim;
import net.minecraft.world.entity.ai.behavior.RunIf;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.TryFindWater;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.PlayDead;
import net.minecraft.world.entity.animal.axolotl.ValidatePlayDead;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.crafting.Ingredient;

public class AxolotlAi {
    private static final IntRange ADULT_FOLLOW_RANGE = IntRange.of(5, 16);

    protected static Brain<?> makeBrain(Brain<Axolotl> brain) {
        AxolotlAi.initCoreActivity(brain);
        AxolotlAi.initIdleActivity(brain);
        AxolotlAi.initFightActivity(brain);
        AxolotlAi.initPlayDeadActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initPlayDeadActivity(Brain<Axolotl> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.PLAY_DEAD, ImmutableList.of(Pair.of(0, new PlayDead()), Pair.of(1, new EraseMemoryIf<Axolotl>(AxolotlAi::isBreeding, MemoryModuleType.PLAY_DEAD_TICKS))), ImmutableSet.of(Pair.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryStatus.VALUE_PRESENT)), ImmutableSet.of(MemoryModuleType.PLAY_DEAD_TICKS));
    }

    private static void initFightActivity(Brain<Axolotl> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0, ImmutableList.of(new StopAttackingIfTargetInvalid(), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(AxolotlAi::getSpeedModifierChasing), new MeleeAttack(20), new EraseMemoryIf<Axolotl>(AxolotlAi::isBreeding, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCoreActivity(Brain<Axolotl> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), new ValidatePlayDead(), new CountDownTemptationTicks()));
    }

    private static void initIdleActivity(Brain<Axolotl> brain) {
        brain.addActivity(Activity.IDLE, ImmutableList.of(Pair.of(0, new RunSometimes<LivingEntity>(new SetEntityLookTarget(EntityType.PLAYER, 6.0f), IntRange.of(30, 60))), Pair.of(1, new RunOne(ImmutableList.of(Pair.of(new AnimalMakeLove(EntityType.AXOLOTL, 0.2f), 1), Pair.of(new FollowTemptation(AxolotlAi::getSpeedModifier), 1), Pair.of(new BabyFollowAdult(ADULT_FOLLOW_RANGE, AxolotlAi::getSpeedModifierFollowingAdult), 1)))), Pair.of(2, new StartAttacking<Axolotl>(AxolotlAi::findNearestValidAttackTarget)), Pair.of(2, new TryFindWater(6, 0.15f)), Pair.of(3, new GateBehavior(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), ImmutableSet.of(), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.TRY_ALL, ImmutableList.of(Pair.of(new RandomSwim(0.5f), 2), Pair.of(new RandomStroll(0.15f), 2), Pair.of(new SetWalkTargetFromLookTarget(AxolotlAi::getSpeedModifier, 3), 3), Pair.of(new RunIf<LivingEntity>(Entity::isInWaterOrBubble, new DoNothing(30, 60)), 5), Pair.of(new RunIf<LivingEntity>(Entity::isOnGround, new DoNothing(200, 400)), 5))))));
    }

    public static void updateActivity(Axolotl axolotl) {
        Brain<Axolotl> brain = axolotl.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != Activity.PLAY_DEAD) {
            brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.PLAY_DEAD, Activity.FIGHT, Activity.IDLE));
        }
    }

    private static float getSpeedModifierChasing(LivingEntity livingEntity) {
        return livingEntity.isInWaterOrBubble() ? 0.6f : 0.15f;
    }

    private static float getSpeedModifierFollowingAdult(LivingEntity livingEntity) {
        return livingEntity.isInWaterOrBubble() ? 0.6f : 0.15f;
    }

    private static float getSpeedModifier(LivingEntity livingEntity) {
        return livingEntity.isInWaterOrBubble() ? 0.5f : 0.15f;
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Axolotl axolotl) {
        if (AxolotlAi.isBreeding(axolotl)) {
            return Optional.empty();
        }
        return axolotl.getBrain().getMemory(MemoryModuleType.NEAREST_HOSTILE);
    }

    private static boolean isBreeding(Axolotl axolotl) {
        return axolotl.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }

    public static Ingredient getTemptations() {
        return Ingredient.of(ItemTags.AXOLOTL_TEMPT_ITEMS);
    }
}

