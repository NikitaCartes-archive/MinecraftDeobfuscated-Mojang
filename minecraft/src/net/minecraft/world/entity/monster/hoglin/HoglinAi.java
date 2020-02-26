package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.IntRange;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BecomePassiveIfMemoryPresent;
import net.minecraft.world.entity.ai.behavior.Behavior;
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
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.schedule.Activity;

public class HoglinAi {
	private static final IntRange RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);

	protected static Brain<?> makeBrain(Hoglin hoglin, Dynamic<?> dynamic) {
		Brain<Hoglin> brain = new Brain<>(
			(Collection<MemoryModuleType<?>>)Hoglin.MEMORY_TYPES, (Collection<SensorType<? extends Sensor<? super Hoglin>>>)Hoglin.SENSOR_TYPES, dynamic
		);
		initCoreActivity(hoglin, brain);
		initIdleActivity(hoglin, brain);
		initFightActivity(hoglin, brain);
		initRetreatActivity(hoglin, brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}

	private static void initCoreActivity(Hoglin hoglin, Brain<Hoglin> brain) {
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(200)));
	}

	private static void initIdleActivity(Hoglin hoglin, Brain<Hoglin> brain) {
		float f = getMovementSpeed(hoglin);
		brain.addActivity(
			Activity.IDLE,
			10,
			ImmutableList.of(
				new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_WARPED_FUNGUS, 200),
				new AnimalMakeLove(EntityType.HOGLIN),
				SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_WARPED_FUNGUS, f * 1.8F, 8, true),
				new StartAttacking(HoglinAi::findNearestValidAttackTarget),
				new RunIf<PathfinderMob>(
					Hoglin::isAdult, (Behavior<? super PathfinderMob>)SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, f, 8, false)
				),
				new RunSometimes<LivingEntity>(new SetEntityLookTarget(8.0F), IntRange.of(30, 60)),
				createIdleMovementBehaviors(f)
			)
		);
	}

	private static void initFightActivity(Hoglin hoglin, Brain<Hoglin> brain) {
		float f = getMovementSpeed(hoglin);
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.FIGHT,
			10,
			ImmutableList.of(
				new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_WARPED_FUNGUS, 200),
				new AnimalMakeLove(EntityType.HOGLIN),
				new SetWalkTargetFromAttackTargetIfTargetOutOfReach(f * 1.8F),
				new RunIf<>(Hoglin::isAdult, new MeleeAttack(1.5, 40)),
				new RunIf<>(AgableMob::isBaby, new MeleeAttack(1.5, 15)),
				new StopAttackingIfTargetInvalid()
			),
			MemoryModuleType.ATTACK_TARGET
		);
	}

	private static void initRetreatActivity(Hoglin hoglin, Brain<Hoglin> brain) {
		float f = getMovementSpeed(hoglin) * 2.0F;
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.AVOID,
			10,
			ImmutableList.of(
				SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, f, 15, false),
				createIdleMovementBehaviors(getMovementSpeed(hoglin)),
				new RunSometimes<LivingEntity>(new SetEntityLookTarget(8.0F), IntRange.of(30, 60)),
				new EraseMemoryIf(HoglinAi::hoglinsOutnumberPiglins, MemoryModuleType.AVOID_TARGET)
			),
			MemoryModuleType.AVOID_TARGET
		);
	}

	private static RunOne<Hoglin> createIdleMovementBehaviors(float f) {
		return new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(f), 2), Pair.of(new SetWalkTargetFromLookTarget(f, 3), 2), Pair.of(new DoNothing(30, 60), 1)));
	}

	protected static void updateActivity(Hoglin hoglin) {
		Brain<Hoglin> brain = hoglin.getBrain();
		Activity activity = (Activity)brain.getActiveNonCoreActivity().orElse(null);
		brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
		Activity activity2 = (Activity)brain.getActiveNonCoreActivity().orElse(null);
		if (activity != activity2) {
			playActivitySound(hoglin);
		}

		hoglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
	}

	protected static void onHitTarget(Hoglin hoglin, LivingEntity livingEntity) {
		if (!hoglin.isBaby()) {
			if (livingEntity.getType() == EntityType.PIGLIN && !hoglinsOutnumberPiglins(hoglin)) {
				setAvoidTarget(hoglin, livingEntity);
				broadcastRetreat(hoglin, livingEntity);
			} else {
				broadcastAttackTarget(hoglin, livingEntity);
			}
		}
	}

	private static void broadcastRetreat(Hoglin hoglin, LivingEntity livingEntity) {
		getVisibleAdultHoglins(hoglin).forEach(hoglinx -> retreatFromNearestTarget(hoglinx, livingEntity));
	}

	private static void retreatFromNearestTarget(Hoglin hoglin, LivingEntity livingEntity) {
		Brain<Hoglin> brain = hoglin.getBrain();
		LivingEntity livingEntity2 = BehaviorUtils.getNearestTarget(hoglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), livingEntity);
		livingEntity2 = BehaviorUtils.getNearestTarget(hoglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), livingEntity2);
		setAvoidTarget(hoglin, livingEntity2);
	}

	private static void setAvoidTarget(Hoglin hoglin, LivingEntity livingEntity) {
		hoglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
		hoglin.getBrain()
			.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, hoglin.level.getGameTime(), (long)RETREAT_DURATION.randomValue(hoglin.level.random));
	}

	private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Hoglin hoglin) {
		return !isPacified(hoglin) && !isBreeding(hoglin) ? hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER) : Optional.empty();
	}

	static boolean isPosNearNearestWarpedFungus(Hoglin hoglin, BlockPos blockPos) {
		Optional<BlockPos> optional = hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_WARPED_FUNGUS);
		return optional.isPresent() && ((BlockPos)optional.get()).closerThan(blockPos, 8.0);
	}

	private static boolean hoglinsOutnumberPiglins(Hoglin hoglin) {
		if (hoglin.isBaby()) {
			return false;
		} else {
			int i = (Integer)hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
			int j = (Integer)hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1;
			return j > i;
		}
	}

	protected static void wasHurtBy(Hoglin hoglin, LivingEntity livingEntity) {
		Brain<Hoglin> brain = hoglin.getBrain();
		brain.eraseMemory(MemoryModuleType.PACIFIED);
		brain.eraseMemory(MemoryModuleType.BREED_TARGET);
		if (hoglin.isBaby()) {
			retreatFromNearestTarget(hoglin, livingEntity);
		} else {
			maybeRetaliate(hoglin, livingEntity);
		}
	}

	private static void maybeRetaliate(Hoglin hoglin, LivingEntity livingEntity) {
		if (!hoglin.getBrain().isActive(Activity.AVOID) || livingEntity.getType() != EntityType.PIGLIN) {
			if (EntitySelector.ATTACK_ALLOWED.test(livingEntity)) {
				if (livingEntity.getType() != EntityType.HOGLIN) {
					if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(hoglin, livingEntity, 4.0)) {
						setAttackTarget(hoglin, livingEntity);
						broadcastAttackTarget(hoglin, livingEntity);
					}
				}
			}
		}
	}

	private static void setAttackTarget(Hoglin hoglin, LivingEntity livingEntity) {
		hoglin.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		hoglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, livingEntity, hoglin.level.getGameTime(), 200L);
	}

	private static void broadcastAttackTarget(Hoglin hoglin, LivingEntity livingEntity) {
		getVisibleAdultHoglins(hoglin).forEach(hoglinx -> setAttackTargetIfCloserThanCurrent(hoglinx, livingEntity));
	}

	private static void setAttackTargetIfCloserThanCurrent(Hoglin hoglin, LivingEntity livingEntity) {
		Optional<LivingEntity> optional = hoglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
		LivingEntity livingEntity2 = BehaviorUtils.getNearestTarget(hoglin, optional, livingEntity);
		setAttackTarget(hoglin, livingEntity2);
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
			playActivitySound(hoglin);
		}
	}

	private static List<Hoglin> getVisibleAdultHoglins(Hoglin hoglin) {
		return (List<Hoglin>)(hoglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS)
			? (List)hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).get()
			: Lists.<Hoglin>newArrayList());
	}

	public static float getMovementSpeed(Hoglin hoglin) {
		return (float)hoglin.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
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
