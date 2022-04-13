package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.warden.Digging;
import net.minecraft.world.entity.ai.behavior.warden.Emerging;
import net.minecraft.world.entity.ai.behavior.warden.Roar;
import net.minecraft.world.entity.ai.behavior.warden.SetRoarTarget;
import net.minecraft.world.entity.ai.behavior.warden.SetWardenLookTarget;
import net.minecraft.world.entity.ai.behavior.warden.Sniffing;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.behavior.warden.TryToSniff;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class WardenAi {
	private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.5F;
	private static final float SPEED_MULTIPLIER_WHEN_INVESTIGATING = 0.7F;
	private static final float SPEED_MULTIPLIER_WHEN_FIGHTING = 1.2F;
	private static final int MELEE_ATTACK_COOLDOWN = 18;
	private static final int DIGGING_DURATION = Mth.ceil(100.0F);
	public static final int EMERGE_DURATION = Mth.ceil(133.59999F);
	public static final int ROAR_DURATION = Mth.ceil(84.0F);
	private static final int SNIFFING_DURATION = Mth.ceil(83.2F);
	public static final int DIGGING_COOLDOWN = 1200;
	private static final int DISTURBANCE_LOCATION_EXPIRY_TIME = 100;
	private static final List<SensorType<? extends Sensor<? super Warden>>> SENSOR_TYPES = List.of(SensorType.NEAREST_PLAYERS, SensorType.WARDEN_ENTITY_SENSOR);
	private static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(
		MemoryModuleType.NEAREST_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.ATTACK_COOLING_DOWN,
		MemoryModuleType.NEAREST_ATTACKABLE,
		MemoryModuleType.ROAR_TARGET,
		MemoryModuleType.DISTURBANCE_LOCATION,
		MemoryModuleType.RECENT_PROJECTILE,
		MemoryModuleType.IS_SNIFFING,
		MemoryModuleType.IS_EMERGING,
		MemoryModuleType.ROAR_SOUND_DELAY,
		MemoryModuleType.DIG_COOLDOWN,
		MemoryModuleType.ROAR_SOUND_COOLDOWN,
		MemoryModuleType.SNIFF_COOLDOWN,
		MemoryModuleType.TOUCH_COOLDOWN,
		MemoryModuleType.VIBRATION_COOLDOWN,
		MemoryModuleType.SONIC_BOOM_COOLDOWN,
		MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN,
		MemoryModuleType.SONIC_BOOM_SOUND_DELAY
	);
	private static final Behavior<Warden> DIG_COOLDOWN_SETTER = new Behavior<Warden>(ImmutableMap.of(MemoryModuleType.DIG_COOLDOWN, MemoryStatus.REGISTERED)) {
		protected void start(ServerLevel serverLevel, Warden warden, long l) {
			WardenAi.setDigCooldown(warden);
		}
	};

	public static void updateActivity(Warden warden) {
		warden.getBrain()
			.setActiveActivityToFirstValid(
				ImmutableList.of(Activity.EMERGE, Activity.DIG, Activity.ROAR, Activity.FIGHT, Activity.INVESTIGATE, Activity.SNIFF, Activity.IDLE)
			);
	}

	protected static Brain<?> makeBrain(Warden warden, Dynamic<?> dynamic) {
		Brain.Provider<Warden> provider = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
		Brain<Warden> brain = provider.makeBrain(dynamic);
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
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F), new SetWardenLookTarget(), new LookAtTargetSink(45, 90), new MoveToTargetSink()));
	}

	private static void initEmergeActivity(Brain<Warden> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(Activity.EMERGE, 5, ImmutableList.of(new Emerging<>(EMERGE_DURATION)), MemoryModuleType.IS_EMERGING);
	}

	private static void initDiggingActivity(Brain<Warden> brain) {
		brain.addActivityWithConditions(
			Activity.DIG,
			ImmutableList.of(Pair.of(0, new Digging<>(DIGGING_DURATION))),
			ImmutableSet.of(Pair.of(MemoryModuleType.ROAR_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.DIG_COOLDOWN, MemoryStatus.VALUE_ABSENT))
		);
	}

	private static void initIdleActivity(Brain<Warden> brain) {
		brain.addActivity(Activity.IDLE, 10, ImmutableList.of(new SetRoarTarget<>(Warden::getEntityAngryAt), new TryToSniff(), createIdleMovementBehaviors()));
	}

	private static void initInvestigateActivity(Brain<Warden> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.INVESTIGATE,
			5,
			ImmutableList.of(
				new SetRoarTarget<>(Warden::getEntityAngryAt), new GoToTargetLocation(MemoryModuleType.DISTURBANCE_LOCATION, 2, 0.7F), new DoNothing(10, 20)
			),
			MemoryModuleType.DISTURBANCE_LOCATION
		);
	}

	private static void initSniffingActivity(Brain<Warden> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.SNIFF, 5, ImmutableList.of(new SetRoarTarget<>(Warden::getEntityAngryAt), new Sniffing(SNIFFING_DURATION)), MemoryModuleType.IS_SNIFFING
		);
	}

	private static void initRoarActivity(Brain<Warden> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(Activity.ROAR, 10, ImmutableList.of(new Roar()), MemoryModuleType.ROAR_TARGET);
	}

	private static void initFightActivity(Warden warden, Brain<Warden> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.FIGHT,
			10,
			ImmutableList.of(
				DIG_COOLDOWN_SETTER,
				new StopAttackingIfTargetInvalid<>(
					livingEntity -> warden.getAngerLevel() != AngerLevel.ANGRY || !warden.canTargetEntity(livingEntity), WardenAi::onTargetInvalid, false
				),
				new SetEntityLookTarget(livingEntity -> isTarget(warden, livingEntity), (float)warden.getAttributeValue(Attributes.FOLLOW_RANGE)),
				new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.2F),
				new SonicBoom(),
				new MeleeAttack(18)
			),
			MemoryModuleType.ATTACK_TARGET
		);
	}

	private static boolean isTarget(Warden warden, LivingEntity livingEntity) {
		return warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(livingEntity2 -> livingEntity2 == livingEntity).isPresent();
	}

	private static void onTargetInvalid(Warden warden, LivingEntity livingEntity) {
		if (!warden.canTargetEntity(livingEntity)) {
			warden.clearAnger(livingEntity);
		}

		setDigCooldown(warden);
	}

	private static RunOne<Warden> createIdleMovementBehaviors() {
		return new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(0.5F), 2), Pair.of(new DoNothing(30, 60), 1)));
	}

	public static void setDigCooldown(LivingEntity livingEntity) {
		if (livingEntity.getBrain().hasMemoryValue(MemoryModuleType.DIG_COOLDOWN)) {
			livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
		}
	}

	public static void setDisturbanceLocation(Warden warden, BlockPos blockPos) {
		if (shouldInvestigate(warden)) {
			setDigCooldown(warden);
			warden.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 100L);
			warden.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockPos), 100L);
			warden.getBrain().setMemoryWithExpiry(MemoryModuleType.DISTURBANCE_LOCATION, blockPos, 100L);
			warden.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		}
	}

	private static boolean shouldInvestigate(Warden warden) {
		return warden.getEntityAngryAt().isEmpty() && warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty();
	}
}
