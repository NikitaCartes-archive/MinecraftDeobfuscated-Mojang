package net.minecraft.world.entity.animal.sniffer;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public class SnifferAi {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_LOOK_DISTANCE = 6;
	static final List<SensorType<? extends Sensor<? super Sniffer>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_PLAYERS, SensorType.SNIFFER_TEMPTATIONS
	);
	static final List<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.IS_PANICKING,
		MemoryModuleType.SNIFFER_SNIFFING_TARGET,
		MemoryModuleType.SNIFFER_DIGGING,
		MemoryModuleType.SNIFFER_HAPPY,
		MemoryModuleType.SNIFF_COOLDOWN,
		MemoryModuleType.SNIFFER_EXPLORED_POSITIONS,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.BREED_TARGET,
		MemoryModuleType.TEMPTING_PLAYER,
		MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
		MemoryModuleType.IS_TEMPTED
	);
	private static final int SNIFFING_COOLDOWN_TICKS = 9600;
	private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
	private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
	private static final float SPEED_MULTIPLIER_WHEN_SNIFFING = 1.25F;
	private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25F;

	public static Predicate<ItemStack> getTemptations() {
		return itemStack -> itemStack.is(ItemTags.SNIFFER_FOOD);
	}

	protected static Brain<?> makeBrain(Brain<Sniffer> brain) {
		initCoreActivity(brain);
		initIdleActivity(brain);
		initSniffingActivity(brain);
		initDigActivity(brain);
		brain.setCoreActivities(Set.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}

	static Sniffer resetSniffing(Sniffer sniffer) {
		sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_DIGGING);
		sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
		return sniffer.transitionTo(Sniffer.State.IDLING);
	}

	private static void initCoreActivity(Brain<Sniffer> brain) {
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim<>(0.8F), new AnimalPanic<Sniffer>(2.0F) {
			protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
				SnifferAi.resetSniffing(sniffer);
				super.start(serverLevel, sniffer, l);
			}
		}, new MoveToTargetSink(500, 700), new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
	}

	private static void initSniffingActivity(Brain<Sniffer> brain) {
		brain.addActivityWithConditions(
			Activity.SNIFF,
			ImmutableList.of(Pair.of(0, new SnifferAi.Searching())),
			Set.of(
				Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT),
				Pair.of(MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryStatus.VALUE_PRESENT),
				Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT)
			)
		);
	}

	private static void initDigActivity(Brain<Sniffer> brain) {
		brain.addActivityWithConditions(
			Activity.DIG,
			ImmutableList.of(Pair.of(0, new SnifferAi.Digging(160, 180)), Pair.of(0, new SnifferAi.FinishedDigging(40))),
			Set.of(
				Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT),
				Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
				Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_PRESENT)
			)
		);
	}

	private static void initIdleActivity(Brain<Sniffer> brain) {
		brain.addActivityWithConditions(
			Activity.IDLE,
			ImmutableList.of(
				Pair.of(0, new AnimalMakeLove(EntityType.SNIFFER) {
					@Override
					protected void start(ServerLevel serverLevel, Animal animal, long l) {
						SnifferAi.resetSniffing((Sniffer)animal);
						super.start(serverLevel, animal, l);
					}
				}),
				Pair.of(1, new FollowTemptation(livingEntity -> 1.25F, livingEntity -> livingEntity.isBaby() ? 2.5 : 3.5) {
					@Override
					protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
						SnifferAi.resetSniffing((Sniffer)pathfinderMob);
						super.start(serverLevel, pathfinderMob, l);
					}
				}),
				Pair.of(2, new LookAtTargetSink(45, 90)),
				Pair.of(3, new SnifferAi.FeelingHappy(40, 100)),
				Pair.of(
					4,
					new RunOne<>(
						ImmutableList.of(
							Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2),
							Pair.of(new SnifferAi.Scenting(40, 80), 1),
							Pair.of(new SnifferAi.Sniffing(40, 80), 1),
							Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 6.0F), 1),
							Pair.of(RandomStroll.stroll(1.0F), 1),
							Pair.of(new DoNothing(5, 20), 2)
						)
					)
				)
			),
			Set.of(Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_ABSENT))
		);
	}

	static void updateActivity(Sniffer sniffer) {
		sniffer.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.DIG, Activity.SNIFF, Activity.IDLE));
	}

	static class Digging extends Behavior<Sniffer> {
		Digging(int i, int j) {
			super(
				Map.of(
					MemoryModuleType.IS_PANICKING,
					MemoryStatus.VALUE_ABSENT,
					MemoryModuleType.WALK_TARGET,
					MemoryStatus.VALUE_ABSENT,
					MemoryModuleType.SNIFFER_DIGGING,
					MemoryStatus.VALUE_PRESENT,
					MemoryModuleType.SNIFF_COOLDOWN,
					MemoryStatus.VALUE_ABSENT
				),
				i,
				j
			);
		}

		protected boolean checkExtraStartConditions(ServerLevel serverLevel, Sniffer sniffer) {
			return sniffer.canSniff();
		}

		protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
			return sniffer.getBrain().getMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent() && sniffer.canDig() && !sniffer.isInLove();
		}

		protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
			sniffer.transitionTo(Sniffer.State.DIGGING);
		}

		protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
			boolean bl = this.timedOut(l);
			if (bl) {
				sniffer.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 9600L);
			} else {
				SnifferAi.resetSniffing(sniffer);
			}
		}
	}

	static class FeelingHappy extends Behavior<Sniffer> {
		FeelingHappy(int i, int j) {
			super(Map.of(MemoryModuleType.SNIFFER_HAPPY, MemoryStatus.VALUE_PRESENT), i, j);
		}

		protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
			return true;
		}

		protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
			sniffer.transitionTo(Sniffer.State.FEELING_HAPPY);
		}

		protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
			sniffer.transitionTo(Sniffer.State.IDLING);
			sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_HAPPY);
		}
	}

	static class FinishedDigging extends Behavior<Sniffer> {
		FinishedDigging(int i) {
			super(
				Map.of(
					MemoryModuleType.IS_PANICKING,
					MemoryStatus.VALUE_ABSENT,
					MemoryModuleType.WALK_TARGET,
					MemoryStatus.VALUE_ABSENT,
					MemoryModuleType.SNIFFER_DIGGING,
					MemoryStatus.VALUE_PRESENT,
					MemoryModuleType.SNIFF_COOLDOWN,
					MemoryStatus.VALUE_PRESENT
				),
				i,
				i
			);
		}

		protected boolean checkExtraStartConditions(ServerLevel serverLevel, Sniffer sniffer) {
			return true;
		}

		protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
			return sniffer.getBrain().getMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent();
		}

		protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
			sniffer.transitionTo(Sniffer.State.RISING);
		}

		protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
			boolean bl = this.timedOut(l);
			sniffer.transitionTo(Sniffer.State.IDLING).onDiggingComplete(bl);
			sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_DIGGING);
			sniffer.getBrain().setMemory(MemoryModuleType.SNIFFER_HAPPY, true);
		}
	}

	static class Scenting extends Behavior<Sniffer> {
		Scenting(int i, int j) {
			super(
				Map.of(
					MemoryModuleType.IS_PANICKING,
					MemoryStatus.VALUE_ABSENT,
					MemoryModuleType.SNIFFER_DIGGING,
					MemoryStatus.VALUE_ABSENT,
					MemoryModuleType.SNIFFER_SNIFFING_TARGET,
					MemoryStatus.VALUE_ABSENT,
					MemoryModuleType.SNIFFER_HAPPY,
					MemoryStatus.VALUE_ABSENT,
					MemoryModuleType.BREED_TARGET,
					MemoryStatus.VALUE_ABSENT
				),
				i,
				j
			);
		}

		protected boolean checkExtraStartConditions(ServerLevel serverLevel, Sniffer sniffer) {
			return !sniffer.isTempted();
		}

		protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
			return true;
		}

		protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
			sniffer.transitionTo(Sniffer.State.SCENTING);
		}

		protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
			sniffer.transitionTo(Sniffer.State.IDLING);
		}
	}

	static class Searching extends Behavior<Sniffer> {
		Searching() {
			super(
				Map.of(
					MemoryModuleType.WALK_TARGET,
					MemoryStatus.VALUE_PRESENT,
					MemoryModuleType.IS_PANICKING,
					MemoryStatus.VALUE_ABSENT,
					MemoryModuleType.SNIFFER_SNIFFING_TARGET,
					MemoryStatus.VALUE_PRESENT
				),
				600
			);
		}

		protected boolean checkExtraStartConditions(ServerLevel serverLevel, Sniffer sniffer) {
			return sniffer.canSniff();
		}

		protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
			if (!sniffer.canSniff()) {
				sniffer.transitionTo(Sniffer.State.IDLING);
				return false;
			} else {
				Optional<BlockPos> optional = sniffer.getBrain()
					.getMemory(MemoryModuleType.WALK_TARGET)
					.map(WalkTarget::getTarget)
					.map(PositionTracker::currentBlockPosition);
				Optional<BlockPos> optional2 = sniffer.getBrain().getMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
				return !optional.isEmpty() && !optional2.isEmpty() ? ((BlockPos)optional2.get()).equals(optional.get()) : false;
			}
		}

		protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
			sniffer.transitionTo(Sniffer.State.SEARCHING);
		}

		protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
			if (sniffer.canDig() && sniffer.canSniff()) {
				sniffer.getBrain().setMemory(MemoryModuleType.SNIFFER_DIGGING, true);
			}

			sniffer.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
			sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
		}
	}

	static class Sniffing extends Behavior<Sniffer> {
		Sniffing(int i, int j) {
			super(
				Map.of(
					MemoryModuleType.WALK_TARGET,
					MemoryStatus.VALUE_ABSENT,
					MemoryModuleType.SNIFFER_SNIFFING_TARGET,
					MemoryStatus.VALUE_ABSENT,
					MemoryModuleType.SNIFF_COOLDOWN,
					MemoryStatus.VALUE_ABSENT
				),
				i,
				j
			);
		}

		protected boolean checkExtraStartConditions(ServerLevel serverLevel, Sniffer sniffer) {
			return !sniffer.isBaby() && sniffer.canSniff();
		}

		protected boolean canStillUse(ServerLevel serverLevel, Sniffer sniffer, long l) {
			return sniffer.canSniff();
		}

		protected void start(ServerLevel serverLevel, Sniffer sniffer, long l) {
			sniffer.transitionTo(Sniffer.State.SNIFFING);
		}

		protected void stop(ServerLevel serverLevel, Sniffer sniffer, long l) {
			boolean bl = this.timedOut(l);
			sniffer.transitionTo(Sniffer.State.IDLING);
			if (bl) {
				sniffer.calculateDigPosition().ifPresent(blockPos -> {
					sniffer.getBrain().setMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET, blockPos);
					sniffer.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, 1.25F, 0));
				});
			}
		}
	}
}
