package net.minecraft.world.entity.animal.allay;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class AllayAi {
	private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
	private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_DEPOSIT_TARGET = 1.25F;
	private static final float SPEED_MULTIPLIER_WHEN_RETRIEVING_ITEM = 2.0F;
	private static final int MAX_DISTANCE_FOLLOW_TARGET = 16;
	private static final int MAX_LOOK_DISTANCE = 6;
	private static final int MIN_WAIT_DURATION = 30;
	private static final int MAX_WAIT_DURATION = 60;
	private static final int TIME_TO_FORGET_NOTEBLOCK = 600;

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
				new Swim(0.8F),
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
				Pair.of(0, new GoToWantedItem<>(allay -> true, 2.0F, true, 9)),
				Pair.of(1, new GoAndGiveItemsToTarget<>(AllayAi::getItemDepositPosition, 1.25F)),
				Pair.of(2, new StayCloseToTarget<>(AllayAi::getItemDepositPosition, 16, 1.25F)),
				Pair.of(3, new RunSometimes<>(new SetEntityLookTarget(livingEntity -> true, 6.0F), UniformInt.of(30, 60))),
				Pair.of(
					4,
					new RunOne<>(
						ImmutableList.of(Pair.of(new FlyingRandomStroll(1.0F), 2), Pair.of(new SetWalkTargetFromLookTarget(1.0F, 3), 2), Pair.of(new DoNothing(30, 60), 1))
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
		GlobalPos globalPos = GlobalPos.of(livingEntity.getLevel().dimension(), blockPos);
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
			BlockPos blockPos = ((GlobalPos)optional.get()).pos();
			if (shouldDepositItemsAtLikedNoteblock(livingEntity, brain, blockPos)) {
				return Optional.of(new BlockPosTracker(blockPos.above()));
			}

			brain.eraseMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
		}

		return getLikedPlayerPositionTracker(livingEntity);
	}

	private static boolean shouldDepositItemsAtLikedNoteblock(LivingEntity livingEntity, Brain<?> brain, BlockPos blockPos) {
		Optional<Integer> optional = brain.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS);
		return livingEntity.getLevel().getBlockState(blockPos).is(Blocks.NOTE_BLOCK) && optional.isPresent();
	}

	private static Optional<PositionTracker> getLikedPlayerPositionTracker(LivingEntity livingEntity) {
		Level level = livingEntity.getLevel();
		if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
			Optional<UUID> optional = livingEntity.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
			if (optional.isPresent()) {
				Entity entity = serverLevel.getEntity((UUID)optional.get());
				return entity instanceof Player ? Optional.of(new EntityTracker(entity, true)) : Optional.empty();
			}
		}

		return Optional.empty();
	}
}
