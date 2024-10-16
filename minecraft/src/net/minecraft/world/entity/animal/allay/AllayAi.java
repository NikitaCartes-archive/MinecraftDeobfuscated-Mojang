package net.minecraft.world.entity.animal.allay;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.GoAndGiveItemsToTarget;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StayCloseToTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class AllayAi {
	private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
	private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_DEPOSIT_TARGET = 2.25F;
	private static final float SPEED_MULTIPLIER_WHEN_RETRIEVING_ITEM = 1.75F;
	private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.5F;
	private static final int CLOSE_ENOUGH_TO_TARGET = 4;
	private static final int TOO_FAR_FROM_TARGET = 16;
	private static final int MAX_LOOK_DISTANCE = 6;
	private static final int MIN_WAIT_DURATION = 30;
	private static final int MAX_WAIT_DURATION = 60;
	private static final int TIME_TO_FORGET_NOTEBLOCK = 600;
	private static final int DISTANCE_TO_WANTED_ITEM = 32;
	private static final int GIVE_ITEM_TIMEOUT_DURATION = 20;

	protected static Brain<?> makeBrain(Brain<Allay> brain) {
		initCoreActivity(brain);
		initIdleActivity(brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}

	private static void initCoreActivity(Brain<Allay> brain) {
		brain.addActivity(
			Activity.CORE,
			0,
			ImmutableList.of(
				new Swim<>(0.8F),
				new AnimalPanic(2.5F),
				new LookAtTargetSink(45, 90),
				new MoveToTargetSink(),
				new CountDownCooldownTicks(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS),
				new CountDownCooldownTicks(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)
			)
		);
	}

	private static void initIdleActivity(Brain<Allay> brain) {
		brain.addActivityWithConditions(
			Activity.IDLE,
			ImmutableList.of(
				Pair.of(0, GoToWantedItem.create(allay -> true, 1.75F, true, 32)),
				Pair.of(1, new GoAndGiveItemsToTarget<>(AllayAi::getItemDepositPosition, 2.25F, 20)),
				Pair.of(2, StayCloseToTarget.create(AllayAi::getItemDepositPosition, Predicate.not(AllayAi::hasWantedItem), 4, 16, 2.25F)),
				Pair.of(3, SetEntityLookTargetSometimes.create(6.0F, UniformInt.of(30, 60))),
				Pair.of(
					4,
					new RunOne<>(
						ImmutableList.of(Pair.of(RandomStroll.fly(1.0F), 2), Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2), Pair.of(new DoNothing(30, 60), 1))
					)
				)
			),
			ImmutableSet.of()
		);
	}

	public static void updateActivity(Allay allay) {
		allay.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
	}

	public static void hearNoteblock(LivingEntity livingEntity, BlockPos blockPos) {
		Brain<?> brain = livingEntity.getBrain();
		GlobalPos globalPos = GlobalPos.of(livingEntity.level().dimension(), blockPos);
		Optional<GlobalPos> optional = brain.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
		if (optional.isEmpty()) {
			brain.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION, globalPos);
			brain.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, 600);
		} else if (((GlobalPos)optional.get()).equals(globalPos)) {
			brain.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, 600);
		}
	}

	private static Optional<PositionTracker> getItemDepositPosition(LivingEntity livingEntity) {
		Brain<?> brain = livingEntity.getBrain();
		Optional<GlobalPos> optional = brain.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
		if (optional.isPresent()) {
			GlobalPos globalPos = (GlobalPos)optional.get();
			if (shouldDepositItemsAtLikedNoteblock(livingEntity, brain, globalPos)) {
				return Optional.of(new BlockPosTracker(globalPos.pos().above()));
			}

			brain.eraseMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
		}

		return getLikedPlayerPositionTracker(livingEntity);
	}

	private static boolean hasWantedItem(LivingEntity livingEntity) {
		Brain<?> brain = livingEntity.getBrain();
		return brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
	}

	private static boolean shouldDepositItemsAtLikedNoteblock(LivingEntity livingEntity, Brain<?> brain, GlobalPos globalPos) {
		Optional<Integer> optional = brain.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS);
		Level level = livingEntity.level();
		return globalPos.isCloseEnough(level.dimension(), livingEntity.blockPosition(), 1024)
			&& level.getBlockState(globalPos.pos()).is(Blocks.NOTE_BLOCK)
			&& optional.isPresent();
	}

	private static Optional<PositionTracker> getLikedPlayerPositionTracker(LivingEntity livingEntity) {
		return getLikedPlayer(livingEntity).map(serverPlayer -> new EntityTracker(serverPlayer, true));
	}

	public static Optional<ServerPlayer> getLikedPlayer(LivingEntity livingEntity) {
		Level level = livingEntity.level();
		if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
			Optional<UUID> optional = livingEntity.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
			if (optional.isPresent()) {
				if (serverLevel.getEntity((UUID)optional.get()) instanceof ServerPlayer serverPlayer
					&& (serverPlayer.gameMode.isSurvival() || serverPlayer.gameMode.isCreative())
					&& serverPlayer.closerThan(livingEntity, 64.0)) {
					return Optional.of(serverPlayer);
				}

				return Optional.empty();
			}
		}

		return Optional.empty();
	}
}
