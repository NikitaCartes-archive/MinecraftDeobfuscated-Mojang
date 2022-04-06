/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.allay;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.FlyingRandomStroll;
import net.minecraft.world.entity.ai.behavior.GoAndGiveItemsToTarget;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StayCloseToTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class AllayAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_DEPOSIT_TARGET = 1.25f;
    private static final float SPEED_MULTIPLIER_WHEN_RETRIEVING_ITEM = 2.0f;
    private static final int MAX_DISTANCE_FOLLOW_TARGET = 16;
    private static final int MAX_LOOK_DISTANCE = 6;
    private static final int MIN_WAIT_DURATION = 30;
    private static final int MAX_WAIT_DURATION = 60;
    private static final int TIME_TO_FORGET_NOTEBLOCK = 600;

    protected static Brain<?> makeBrain(Brain<Allay> brain) {
        AllayAi.initCoreActivity(brain);
        AllayAi.initIdleActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Allay> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8f), new LookAtTargetSink(45, 90), new MoveToTargetSink(), new CountDownCooldownTicks(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS), new CountDownCooldownTicks(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<Allay> brain) {
        brain.addActivityWithConditions(Activity.IDLE, ImmutableList.of(Pair.of(0, new GoToWantedItem<Allay>(allay -> true, 2.0f, true, 9)), Pair.of(1, new GoAndGiveItemsToTarget(AllayAi::getItemDepositPosition, 1.25f)), Pair.of(2, new StayCloseToTarget(AllayAi::getItemDepositPosition, 16, 1.25f)), Pair.of(3, new RunSometimes<LivingEntity>(new SetEntityLookTarget(livingEntity -> true, 6.0f), UniformInt.of(30, 60))), Pair.of(4, new RunOne(ImmutableList.of(Pair.of(new FlyingRandomStroll(1.0f), 2), Pair.of(new SetWalkTargetFromLookTarget(1.0f, 3), 2), Pair.of(new DoNothing(30, 60), 1))))), ImmutableSet.of());
    }

    public static void updateActivity(Allay allay) {
        allay.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }

    public static void hearNoteblock(LivingEntity livingEntity, BlockPos blockPos) {
        Brain<?> brain = livingEntity.getBrain();
        GlobalPos globalPos = GlobalPos.of(livingEntity.getLevel().dimension(), blockPos);
        Optional<GlobalPos> optional = brain.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
        if (optional.isEmpty()) {
            brain.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION, globalPos);
            brain.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, 600);
        } else if (optional.get().equals(globalPos)) {
            brain.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, 600);
        }
    }

    private static Optional<PositionTracker> getItemDepositPosition(LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        Optional<GlobalPos> optional = brain.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
        if (optional.isPresent()) {
            BlockPos blockPos = optional.get().pos();
            if (AllayAi.shouldDepositItemsAtLikedNoteblock(livingEntity, brain, blockPos)) {
                return Optional.of(new BlockPosTracker(blockPos.above()));
            }
            brain.eraseMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
        }
        return AllayAi.getLikedPlayerPositionTracker(livingEntity);
    }

    private static boolean shouldDepositItemsAtLikedNoteblock(LivingEntity livingEntity, Brain<?> brain, BlockPos blockPos) {
        Optional<Integer> optional = brain.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS);
        return livingEntity.getLevel().getBlockState(blockPos).is(Blocks.NOTE_BLOCK) && optional.isPresent();
    }

    private static Optional<PositionTracker> getLikedPlayerPositionTracker(LivingEntity livingEntity) {
        return AllayAi.getLikedPlayer(livingEntity).map(serverPlayer -> new EntityTracker((Entity)serverPlayer, true));
    }

    public static Optional<ServerPlayer> getLikedPlayer(LivingEntity livingEntity) {
        Level level = livingEntity.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Optional<UUID> optional = livingEntity.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
            if (optional.isPresent()) {
                Optional<ServerPlayer> optional2;
                Entity entity = serverLevel.getEntity(optional.get());
                if (entity instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)entity;
                    optional2 = Optional.of(serverPlayer);
                } else {
                    optional2 = Optional.empty();
                }
                return optional2;
            }
        }
        return Optional.empty();
    }
}

