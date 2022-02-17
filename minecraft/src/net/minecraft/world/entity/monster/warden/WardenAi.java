package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.Digging;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.Emerging;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.Sniffing;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;

public class WardenAi {
	private static final int MAX_LOOK_DIST = 8;
	private static final int INTERACTION_RANGE = 8;
	private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.5F;
	public static final float SPEED_MULTIPLIER_WHEN_INVESTIGATING = 0.7F;
	public static final int MELEE_ATTACK_COOLDOWN = 18;
	public static final int MELEE_ATTACK_ANIMATION = 15;
	public static final int ROAR_DURATION = Mth.ceil(84.0F);
	public static final int SNIFFING_DURATION = Mth.ceil(68.0F);
	public static final int EMERGE_DURATION = Mth.ceil(134.0F);
	public static final int DIGGING_DURATION = Mth.ceil(110.0F);
	public static final int TIME_REQUIRED_UNTIL_DIG = 1200;
	public static final int DISTURBANCE_LOCATION_EXPIRY_TIME = 100;

	protected static Brain<?> makeBrain(Warden warden, Brain<Warden> brain) {
		initCoreActivity(brain);
		initEmergeActivity(brain);
		initDiggingActivity(brain);
		initIdleActivity(brain);
		initRoarActivity(brain);
		initFightActivity(warden, brain);
		initInvestigateActivity(brain);
		initSniffingActivity(brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}

	private static void initCoreActivity(Brain<Warden> brain) {
		brain.addActivity(
			Activity.CORE,
			0,
			ImmutableList.of(
				new SetWardenLookTarget(),
				new LookAtTargetSink(45, 90),
				new MoveToTargetSink(),
				new StartAttacking<>(warden -> true, WardenAi::entitiesToAttackImmediately)
			)
		);
	}

	private static void initEmergeActivity(Brain<Warden> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(Activity.EMERGE, 5, ImmutableList.of(new Emerging<>(EMERGE_DURATION)), MemoryModuleType.IS_EMERGING);
	}

	private static void initDiggingActivity(Brain<Warden> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(Activity.DIG, 5, ImmutableList.of(new Digging<>(DIGGING_DURATION)), MemoryModuleType.IS_DIGGING);
	}

	private static void initIdleActivity(Brain<Warden> brain) {
		brain.addActivity(
			Activity.IDLE,
			10,
			ImmutableList.of(
				new SetRoarTarget<>(WardenAi::shouldRoar, WardenAi::findEntityAngryAt),
				new TryToSniff(),
				createIdleMovementBehaviors(),
				new SetLookAndInteract(EntityType.PLAYER, 4)
			)
		);
	}

	private static void initInvestigateActivity(Brain<Warden> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.INVESTIGATE,
			5,
			ImmutableList.of(
				new SetRoarTarget<>(WardenAi::shouldRoar, WardenAi::findEntityAngryAt),
				new GoToTargetLocation(MemoryModuleType.DISTURBANCE_LOCATION, 2, 0.7F),
				new DoNothing(10, 20)
			),
			MemoryModuleType.DISTURBANCE_LOCATION
		);
	}

	private static void initSniffingActivity(Brain<Warden> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.SNIFF,
			5,
			ImmutableList.of(new SetRoarTarget<>(WardenAi::shouldRoar, WardenAi::findEntityAngryAt), new Sniffing(SNIFFING_DURATION)),
			MemoryModuleType.IS_SNIFFING
		);
	}

	private static void initRoarActivity(Brain<Warden> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.ROAR,
			10,
			ImmutableList.of(new StopRoaringIfTargetInvalid(), new Roar(), new StartAttackingAfterTimeOut(warden -> true, WardenAi::findEntityAngryAt, ROAR_DURATION)),
			MemoryModuleType.ROAR_TARGET
		);
	}

	private static void initFightActivity(Warden warden, Brain<Warden> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.FIGHT,
			10,
			ImmutableList.of(
				new StopAttackingIfTargetInvalid<>(livingEntity -> !isValidAttackTarget(warden, livingEntity), WardenAi::onTargetInvalid, false),
				new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.2F),
				new SetEntityLookTarget(livingEntity -> isTarget(warden, livingEntity), 8.0F),
				new MeleeAttack(18)
			),
			MemoryModuleType.ATTACK_TARGET
		);
	}

	private static void onTargetInvalid(Warden warden) {
		LivingEntity livingEntity = (LivingEntity)warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
		if (livingEntity.isDeadOrDying()) {
			warden.getSuspectTracking().clearSuspicion(livingEntity.getUUID());
			warden.setPose(Pose.STANDING);
		}

		markDisturbed(warden);
	}

	private static boolean isTarget(Warden warden, LivingEntity livingEntity) {
		Optional<LivingEntity> optional = warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
		return optional.filter(livingEntity2 -> livingEntity2 == livingEntity).isPresent();
	}

	public static boolean isValidRoarTarget(LivingEntity livingEntity) {
		return EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingEntity) && !livingEntity.isDeadOrDying();
	}

	private static RunOne<Warden> createIdleMovementBehaviors() {
		return new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(0.5F), 2), Pair.of(new DoNothing(30, 60), 1)));
	}

	public static void updateActivity(Warden warden) {
		Brain<Warden> brain = warden.getBrain();
		Optional<Activity> optional = brain.getActiveNonCoreActivity();
		if (!shouldRoar(warden) && optional.filter(activityx -> activityx == Activity.ROAR).isPresent()) {
			brain.setActiveActivityIfPossible(Activity.FIGHT);
		}

		if (optional.isPresent() && canDig(warden)) {
			Activity activity = (Activity)optional.get();
			if (activity != Activity.ROAR) {
				brain.setMemoryWithExpiry(MemoryModuleType.IS_DIGGING, true, (long)DIGGING_DURATION);
			}
		}

		brain.setActiveActivityToFirstValid(
			ImmutableList.of(Activity.EMERGE, Activity.DIG, Activity.ROAR, Activity.FIGHT, Activity.INVESTIGATE, Activity.SNIFF, Activity.IDLE)
		);
	}

	public static void markDisturbed(Warden warden) {
		markDisturbed(warden, warden.getLevel().getGameTime());
	}

	public static void markDisturbed(Warden warden, long l) {
		warden.getBrain().setMemoryWithExpiry(MemoryModuleType.LAST_DISTURBANCE, l, 1200L);
	}

	public static void noticeSuspiciousLocation(Warden warden, BlockPos blockPos) {
		if (shouldInvestigate(warden)) {
			warden.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockPos), 100L);
			warden.getBrain().setMemoryWithExpiry(MemoryModuleType.DISTURBANCE_LOCATION, blockPos, 100L);
			warden.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		}
	}

	private static boolean isValidAttackTarget(Warden warden, LivingEntity livingEntity) {
		return entitiesToAttackImmediately(warden).filter(livingEntity2 -> livingEntity2 == livingEntity).isPresent()
			|| findEntityAngryAt(warden).filter(livingEntity2 -> livingEntity2 == livingEntity).isPresent();
	}

	private static boolean canDig(Warden warden) {
		return warden.getBrain().getMemory(MemoryModuleType.LAST_DISTURBANCE).isEmpty();
	}

	private static boolean shouldRoar(Warden warden) {
		return entitiesToAttackImmediately(warden).isEmpty() && warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty();
	}

	private static boolean shouldInvestigate(Warden warden) {
		return entitiesToAttackImmediately(warden).isEmpty() && warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty();
	}

	private static Optional<? extends LivingEntity> entitiesToAttackImmediately(Warden warden) {
		return warden.getBrain()
			.getMemory(MemoryModuleType.HURT_BY)
			.map(DamageSource::getEntity)
			.map(entity -> entity instanceof LivingEntity livingEntity ? livingEntity : null);
	}

	private static Optional<? extends LivingEntity> findEntityAngryAt(Warden warden) {
		SuspectTracking suspectTracking = warden.getSuspectTracking();
		if (suspectTracking.getActiveSuspect().isEmpty()) {
			return Optional.empty();
		} else if (suspectTracking.getActiveAnger() < 80) {
			return Optional.empty();
		} else {
			UUID uUID = (UUID)suspectTracking.getActiveSuspect().get();
			Entity entity = ((ServerLevel)warden.level).getEntity(uUID);
			return entity instanceof LivingEntity ? Optional.of((LivingEntity)entity) : Optional.empty();
		}
	}
}
