package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class BreezeAi {
	public static final float SPEED_MULTIPLIER_WHEN_SLIDING = 0.6F;
	public static final float JUMP_CIRCLE_INNER_RADIUS = 4.0F;
	public static final float JUMP_CIRCLE_MIDDLE_RADIUS = 8.0F;
	public static final float JUMP_CIRCLE_OUTER_RADIUS = 24.0F;
	static final List<SensorType<? extends Sensor<? super Breeze>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_PLAYERS, SensorType.BREEZE_ATTACK_ENTITY_SENSOR
	);
	static final List<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_ATTACKABLE,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.BREEZE_JUMP_COOLDOWN,
		MemoryModuleType.BREEZE_JUMP_INHALING,
		MemoryModuleType.BREEZE_SHOOT,
		MemoryModuleType.BREEZE_SHOOT_CHARGING,
		MemoryModuleType.BREEZE_SHOOT_RECOVERING,
		MemoryModuleType.BREEZE_SHOOT_COOLDOWN,
		MemoryModuleType.BREEZE_JUMP_TARGET,
		MemoryModuleType.BREEZE_LEAVING_WATER,
		MemoryModuleType.HURT_BY,
		MemoryModuleType.HURT_BY_ENTITY,
		MemoryModuleType.PATH
	);
	private static final int TICKS_TO_REMEMBER_SEEN_TARGET = 100;

	protected static Brain<?> makeBrain(Breeze breeze, Brain<Breeze> brain) {
		initCoreActivity(brain);
		initIdleActivity(brain);
		initFightActivity(breeze, brain);
		brain.setCoreActivities(Set.of(Activity.CORE));
		brain.setDefaultActivity(Activity.FIGHT);
		brain.useDefaultActivity();
		return brain;
	}

	private static void initCoreActivity(Brain<Breeze> brain) {
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim<>(0.8F), new LookAtTargetSink(45, 90)));
	}

	private static void initIdleActivity(Brain<Breeze> brain) {
		brain.addActivity(
			Activity.IDLE,
			ImmutableList.of(
				Pair.of(0, StartAttacking.create((serverLevel, breeze) -> breeze.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))),
				Pair.of(1, StartAttacking.create((serverLevel, breeze) -> breeze.getHurtBy())),
				Pair.of(2, new BreezeAi.SlideToTargetSink(20, 40)),
				Pair.of(3, new RunOne<>(ImmutableList.of(Pair.of(new DoNothing(20, 100), 1), Pair.of(RandomStroll.stroll(0.6F), 2))))
			)
		);
	}

	private static void initFightActivity(Breeze breeze, Brain<Breeze> brain) {
		brain.addActivityWithConditions(
			Activity.FIGHT,
			ImmutableList.of(
				Pair.of(0, StopAttackingIfTargetInvalid.create(Sensor.wasEntityAttackableLastNTicks(breeze, 100).negate()::test)),
				Pair.of(1, new Shoot()),
				Pair.of(2, new LongJump()),
				Pair.of(3, new ShootWhenStuck()),
				Pair.of(4, new Slide())
			),
			ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT))
		);
	}

	static void updateActivity(Breeze breeze) {
		breeze.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
	}

	public static class SlideToTargetSink extends MoveToTargetSink {
		@VisibleForTesting
		public SlideToTargetSink(int i, int j) {
			super(i, j);
		}

		@Override
		protected void start(ServerLevel serverLevel, Mob mob, long l) {
			super.start(serverLevel, mob, l);
			mob.playSound(SoundEvents.BREEZE_SLIDE);
			mob.setPose(Pose.SLIDING);
		}

		@Override
		protected void stop(ServerLevel serverLevel, Mob mob, long l) {
			super.stop(serverLevel, mob, l);
			mob.setPose(Pose.STANDING);
			if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
				mob.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 60L);
			}
		}
	}
}
