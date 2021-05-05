/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.goat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LongJumpMidJump;
import net.minecraft.world.entity.ai.behavior.LongJumpToRandomPos;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PrepareRamNearestTarget;
import net.minecraft.world.entity.ai.behavior.RamTarget;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class GoatAi {
    public static final int RAM_PREPARE_TIME = 20;
    public static final int RAM_MAX_DISTANCE = 7;
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 1.25f;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25f;
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0f;
    private static final float SPEED_MULTIPLIER_WHEN_PREPARING_TO_RAM = 1.25f;
    private static final UniformInt TIME_BETWEEN_LONG_JUMPS = UniformInt.of(600, 1200);
    public static final int MAX_LONG_JUMP_HEIGHT = 5;
    public static final int MAX_LONG_JUMP_WIDTH = 5;
    public static final float MAX_JUMP_VELOCITY = 1.5f;
    private static final UniformInt TIME_BETWEEN_RAMS = UniformInt.of(600, 6000);
    private static final UniformInt TIME_BETWEEN_RAMS_SCREAMER = UniformInt.of(100, 300);
    private static final TargetingConditions RAM_TARGET_CONDITIONS = new TargetingConditions().selector(livingEntity -> !livingEntity.getType().equals(EntityType.GOAT) && (livingEntity.level.getDifficulty() != Difficulty.PEACEFUL || !livingEntity.getType().equals(EntityType.PLAYER)));
    private static final float SPEED_MULTIPLIER_WHEN_RAMMING = 3.0f;
    public static final int RAM_MIN_DISTANCE = 4;
    private static final int ADULT_RAM_DAMAGE = 2;
    private static final int BABY_RAM_DAMAGE = 1;
    public static final float ADULT_RAM_KNOCKBACK_FORCE = 2.5f;
    public static final float BABY_RAM_KNOCKBACK_FORCE = 1.0f;

    protected static void initMemories(Goat goat) {
        goat.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, TIME_BETWEEN_LONG_JUMPS.sample(goat.level.random));
        goat.getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, TIME_BETWEEN_RAMS.sample(goat.level.random));
    }

    protected static Brain<?> makeBrain(Brain<Goat> brain) {
        GoatAi.initCoreActivity(brain);
        GoatAi.initIdleActivity(brain);
        GoatAi.initLongJumpActivity(brain);
        GoatAi.initRamActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Goat> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8f), new AnimalPanic(2.0f), new LookAtTargetSink(45, 90), new MoveToTargetSink(), new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS), new CountDownCooldownTicks(MemoryModuleType.RAM_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<Goat> brain) {
        brain.addActivityWithConditions(Activity.IDLE, ImmutableList.of(Pair.of(0, new RunSometimes<LivingEntity>(new SetEntityLookTarget(EntityType.PLAYER, 6.0f), UniformInt.of(30, 60))), Pair.of(0, new AnimalMakeLove(EntityType.GOAT, 1.0f)), Pair.of(1, new FollowTemptation(livingEntity -> Float.valueOf(1.25f))), Pair.of(2, new BabyFollowAdult(ADULT_FOLLOW_RANGE, 1.25f)), Pair.of(3, new RunOne(ImmutableList.of(Pair.of(new RandomStroll(1.0f), 2), Pair.of(new SetWalkTargetFromLookTarget(1.0f, 3), 2), Pair.of(new DoNothing(30, 60), 1))))), ImmutableSet.of(Pair.of(MemoryModuleType.RAM_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT)));
    }

    private static void initLongJumpActivity(Brain<Goat> brain) {
        brain.addActivityWithConditions(Activity.LONG_JUMP, ImmutableList.of(Pair.of(0, new LongJumpMidJump(TIME_BETWEEN_LONG_JUMPS, SoundEvents.GOAT_STEP)), Pair.of(1, new LongJumpToRandomPos<Goat>(TIME_BETWEEN_LONG_JUMPS, 5, 5, 1.5f, goat -> goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_LONG_JUMP : SoundEvents.GOAT_LONG_JUMP))), ImmutableSet.of(Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)));
    }

    private static void initRamActivity(Brain<Goat> brain) {
        brain.addActivityWithConditions(Activity.RAM, ImmutableList.of(Pair.of(0, new RamTarget<Goat>(goat -> goat.isScreamingGoat() ? TIME_BETWEEN_RAMS_SCREAMER : TIME_BETWEEN_RAMS, RAM_TARGET_CONDITIONS, goat -> goat.isBaby() ? 1 : 2, 3.0f, goat -> goat.isBaby() ? 1.0 : 2.5, goat -> goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_RAM_IMPACT : SoundEvents.GOAT_RAM_IMPACT)), Pair.of(1, new PrepareRamNearestTarget<Goat>(goat -> goat.isScreamingGoat() ? TIME_BETWEEN_RAMS_SCREAMER.getMinValue() : TIME_BETWEEN_RAMS.getMinValue(), 4, 7, 1.25f, RAM_TARGET_CONDITIONS, 20, goat -> goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_PREPARE_RAM : SoundEvents.GOAT_PREPARE_RAM))), ImmutableSet.of(Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)));
    }

    public static void updateActivity(Goat goat) {
        goat.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.RAM, Activity.LONG_JUMP, Activity.IDLE));
    }

    public static Ingredient getTemptations() {
        return Ingredient.of(Items.WHEAT);
    }
}

