package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;

public class PiglinBruteAi {
	private static final int ANGER_DURATION = 600;
	private static final int MELEE_ATTACK_COOLDOWN = 20;
	private static final double ACTIVITY_SOUND_LIKELIHOOD_PER_TICK = 0.0125;
	private static final int MAX_LOOK_DIST = 8;
	private static final int INTERACTION_RANGE = 8;
	private static final double TARGETING_RANGE = 12.0;
	private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6F;
	private static final int HOME_CLOSE_ENOUGH_DISTANCE = 2;
	private static final int HOME_TOO_FAR_DISTANCE = 100;
	private static final int HOME_STROLL_AROUND_DISTANCE = 5;

	protected static Brain<?> makeBrain(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
		initCoreActivity(piglinBrute, brain);
		initIdleActivity(piglinBrute, brain);
		initFightActivity(piglinBrute, brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}

	protected static void initMemories(PiglinBrute piglinBrute) {
		GlobalPos globalPos = GlobalPos.of(piglinBrute.level().dimension(), piglinBrute.blockPosition());
		piglinBrute.getBrain().setMemory(MemoryModuleType.HOME, globalPos);
	}

	private static void initCoreActivity(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
		brain.addActivity(
			Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), InteractWithDoor.create(), StopBeingAngryIfTargetDead.create())
		);
	}

	private static void initIdleActivity(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
		brain.addActivity(
			Activity.IDLE,
			10,
			ImmutableList.of(
				StartAttacking.<PiglinBrute>create(PiglinBruteAi::findNearestValidAttackTarget),
				createIdleLookBehaviors(),
				createIdleMovementBehaviors(),
				SetLookAndInteract.create(EntityType.PLAYER, 4)
			)
		);
	}

	private static void initFightActivity(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.FIGHT,
			10,
			ImmutableList.of(
				StopAttackingIfTargetInvalid.<Mob>create((Predicate<LivingEntity>)(livingEntity -> !isNearestValidAttackTarget(piglinBrute, livingEntity))),
				SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
				MeleeAttack.create(20)
			),
			MemoryModuleType.ATTACK_TARGET
		);
	}

	private static RunOne<PiglinBrute> createIdleLookBehaviors() {
		return new RunOne<>(
			ImmutableList.of(
				Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1),
				Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0F), 1),
				Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN_BRUTE, 8.0F), 1),
				Pair.of(SetEntityLookTarget.create(8.0F), 1),
				Pair.of(new DoNothing(30, 60), 1)
			)
		);
	}

	private static RunOne<PiglinBrute> createIdleMovementBehaviors() {
		return new RunOne<>(
			ImmutableList.of(
				Pair.of(RandomStroll.stroll(0.6F), 2),
				Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
				Pair.of(InteractWith.of(EntityType.PIGLIN_BRUTE, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
				Pair.of(StrollToPoi.create(MemoryModuleType.HOME, 0.6F, 2, 100), 2),
				Pair.of(StrollAroundPoi.create(MemoryModuleType.HOME, 0.6F, 5), 2),
				Pair.of(new DoNothing(30, 60), 1)
			)
		);
	}

	protected static void updateActivity(PiglinBrute piglinBrute) {
		Brain<PiglinBrute> brain = piglinBrute.getBrain();
		Activity activity = (Activity)brain.getActiveNonCoreActivity().orElse(null);
		brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
		Activity activity2 = (Activity)brain.getActiveNonCoreActivity().orElse(null);
		if (activity != activity2) {
			playActivitySound(piglinBrute);
		}

		piglinBrute.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
	}

	private static boolean isNearestValidAttackTarget(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
		return findNearestValidAttackTarget(abstractPiglin).filter(livingEntity2 -> livingEntity2 == livingEntity).isPresent();
	}

	private static Optional<? extends LivingEntity> findNearestValidAttackTarget(AbstractPiglin abstractPiglin) {
		Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(abstractPiglin, MemoryModuleType.ANGRY_AT);
		if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(abstractPiglin, (LivingEntity)optional.get())) {
			return optional;
		} else {
			Optional<? extends LivingEntity> optional2 = getTargetIfWithinRange(abstractPiglin, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
			return optional2.isPresent() ? optional2 : abstractPiglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
		}
	}

	private static Optional<? extends LivingEntity> getTargetIfWithinRange(
		AbstractPiglin abstractPiglin, MemoryModuleType<? extends LivingEntity> memoryModuleType
	) {
		return abstractPiglin.getBrain().getMemory(memoryModuleType).filter(livingEntity -> livingEntity.closerThan(abstractPiglin, 12.0));
	}

	protected static void wasHurtBy(PiglinBrute piglinBrute, LivingEntity livingEntity) {
		if (!(livingEntity instanceof AbstractPiglin)) {
			PiglinAi.maybeRetaliate(piglinBrute, livingEntity);
		}
	}

	protected static void setAngerTarget(PiglinBrute piglinBrute, LivingEntity livingEntity) {
		piglinBrute.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		piglinBrute.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, livingEntity.getUUID(), 600L);
	}

	protected static void maybePlayActivitySound(PiglinBrute piglinBrute) {
		if ((double)piglinBrute.level().random.nextFloat() < 0.0125) {
			playActivitySound(piglinBrute);
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
