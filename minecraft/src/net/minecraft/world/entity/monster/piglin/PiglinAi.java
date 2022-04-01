package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BackUpIfTooClose;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.CopyMemoryWithExpiry;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.entity.ai.behavior.DismountOrSkipMounting;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.GoToCelebrateLocation;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.Mount;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunIf;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StartCelebratingIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class PiglinAi {
	public static final int REPELLENT_DETECTION_RANGE_HORIZONTAL = 8;
	public static final int REPELLENT_DETECTION_RANGE_VERTICAL = 4;
	public static final Item BARTERING_ITEM = Items.GOLD_INGOT;
	private static final int PLAYER_ANGER_RANGE = 16;
	private static final int ANGER_DURATION = 600;
	private static final int ADMIRE_DURATION = 120;
	private static final int MAX_DISTANCE_TO_WALK_TO_ITEM = 9;
	private static final int MAX_TIME_TO_WALK_TO_ITEM = 200;
	private static final int HOW_LONG_TIME_TO_DISABLE_ADMIRE_WALKING_IF_CANT_REACH_ITEM = 200;
	private static final int CELEBRATION_TIME = 300;
	private static final UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
	private static final int BABY_FLEE_DURATION_AFTER_GETTING_HIT = 100;
	private static final int HIT_BY_PLAYER_MEMORY_TIMEOUT = 400;
	private static final int MAX_WALK_DISTANCE_TO_START_RIDING = 8;
	private static final UniformInt RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
	private static final UniformInt RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
	private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
	private static final int MELEE_ATTACK_COOLDOWN = 20;
	private static final int EAT_COOLDOWN = 200;
	private static final int DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING = 12;
	private static final int MAX_LOOK_DIST = 8;
	private static final int MAX_LOOK_DIST_FOR_PLAYER_HOLDING_LOVED_ITEM = 14;
	private static final int INTERACTION_RANGE = 8;
	private static final int MIN_DESIRED_DIST_FROM_TARGET_WHEN_HOLDING_CROSSBOW = 5;
	private static final float SPEED_WHEN_STRAFING_BACK_FROM_TARGET = 0.75F;
	private static final int DESIRED_DISTANCE_FROM_ZOMBIFIED = 6;
	private static final UniformInt AVOID_ZOMBIFIED_DURATION = TimeUtil.rangeOfSeconds(5, 7);
	private static final UniformInt BABY_AVOID_NEMESIS_DURATION = TimeUtil.rangeOfSeconds(5, 7);
	private static final float PROBABILITY_OF_CELEBRATION_DANCE = 0.1F;
	private static final float SPEED_MULTIPLIER_WHEN_AVOIDING = 1.0F;
	private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.0F;
	private static final float SPEED_MULTIPLIER_WHEN_MOUNTING = 0.8F;
	private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_WANTED_ITEM = 1.0F;
	private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_CELEBRATE_LOCATION = 1.0F;
	private static final float SPEED_MULTIPLIER_WHEN_DANCING = 0.6F;
	private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6F;

	protected static Brain<?> makeBrain(Piglin piglin, Brain<Piglin> brain) {
		initCoreActivity(brain);
		initIdleActivity(brain);
		initAdmireItemActivity(brain);
		initFightActivity(piglin, brain);
		initCelebrateActivity(brain);
		initRetreatActivity(brain);
		initRideHoglinActivity(brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}

	protected static void initMemories(Piglin piglin) {
		int i = TIME_BETWEEN_HUNTS.sample(piglin.level.random);
		piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)i);
	}

	private static void initCoreActivity(Brain<Piglin> brain) {
		brain.addActivity(
			Activity.CORE,
			0,
			ImmutableList.of(
				new LookAtTargetSink(45, 90),
				new MoveToTargetSink(),
				new InteractWithDoor(),
				babyAvoidNemesis(),
				avoidZombified(),
				new StopHoldingItemIfNoLongerAdmiring(),
				new StartAdmiringItemIfSeen(120),
				new StartCelebratingIfTargetDead(300, PiglinAi::wantsToDance),
				new StopBeingAngryIfTargetDead()
			)
		);
	}

	private static void initIdleActivity(Brain<Piglin> brain) {
		brain.addActivity(
			Activity.IDLE,
			10,
			ImmutableList.of(
				new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
				new StartAttacking<>(AbstractPiglin::isAdult, PiglinAi::findNearestValidAttackTarget),
				new RunIf(Piglin::canHunt, new StartHuntingHoglin<>()),
				avoidRepellent(),
				babySometimesRideBabyHoglin(),
				createIdleLookBehaviors(),
				createIdleMovementBehaviors(),
				new SetLookAndInteract(EntityType.PLAYER, 4)
			)
		);
	}

	private static void initFightActivity(Piglin piglin, Brain<Piglin> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.FIGHT,
			10,
			ImmutableList.of(
				new StopAttackingIfTargetInvalid<>((Predicate<LivingEntity>)(livingEntity -> !isNearestValidAttackTarget(piglin, livingEntity))),
				new RunIf(PiglinAi::hasCrossbow, new BackUpIfTooClose<>(5, 0.75F)),
				new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F),
				new MeleeAttack(20),
				new CrossbowAttack(),
				new RememberIfHoglinWasKilled(),
				new EraseMemoryIf(PiglinAi::isNearZombified, MemoryModuleType.ATTACK_TARGET)
			),
			MemoryModuleType.ATTACK_TARGET
		);
	}

	private static void initCelebrateActivity(Brain<Piglin> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.CELEBRATE,
			10,
			ImmutableList.of(
				avoidRepellent(),
				new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
				new StartAttacking(AbstractPiglin::isAdult, PiglinAi::findNearestValidAttackTarget),
				new RunIf((Predicate)(piglin -> !piglin.isDancing()), new GoToCelebrateLocation(2, 1.0F)),
				new RunIf(Piglin::isDancing, new GoToCelebrateLocation(4, 0.6F)),
				new RunOne(
					ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0F), 1), Pair.of(new RandomStroll(0.6F, 2, 1), 1), Pair.of(new DoNothing(10, 20), 1))
				)
			),
			MemoryModuleType.CELEBRATE_LOCATION
		);
	}

	private static void initAdmireItemActivity(Brain<Piglin> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.ADMIRE_ITEM,
			10,
			ImmutableList.of(
				new GoToWantedItem<>(PiglinAi::isNotHoldingLovedItemInOffHand, 1.0F, true, 9),
				new StopAdmiringIfItemTooFarAway(9),
				new StopAdmiringIfTiredOfTryingToReachItem(200, 200)
			),
			MemoryModuleType.ADMIRING_ITEM
		);
	}

	private static void initRetreatActivity(Brain<Piglin> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.AVOID,
			10,
			ImmutableList.of(
				SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true),
				createIdleLookBehaviors(),
				createIdleMovementBehaviors(),
				new EraseMemoryIf(PiglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
			),
			MemoryModuleType.AVOID_TARGET
		);
	}

	private static void initRideHoglinActivity(Brain<Piglin> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.RIDE,
			10,
			ImmutableList.of(
				new Mount<>(0.8F),
				new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 8.0F),
				new RunIf(Entity::isPassenger, createIdleLookBehaviors()),
				new DismountOrSkipMounting(8, PiglinAi::wantsToStopRiding)
			),
			MemoryModuleType.RIDE_TARGET
		);
	}

	private static RunOne<Piglin> createIdleLookBehaviors() {
		return new RunOne<>(
			ImmutableList.of(
				Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 1),
				Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0F), 1),
				Pair.of(new SetEntityLookTarget(8.0F), 1),
				Pair.of(new DoNothing(30, 60), 1)
			)
		);
	}

	private static RunOne<Piglin> createIdleMovementBehaviors() {
		return new RunOne<>(
			ImmutableList.of(
				Pair.of(new RandomStroll(0.6F), 2),
				Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
				Pair.of(new RunIf<>(PiglinAi::doesntSeeAnyPlayerHoldingLovedItem, new SetWalkTargetFromLookTarget(0.6F, 3)), 2),
				Pair.of(new DoNothing(30, 60), 1)
			)
		);
	}

	private static SetWalkTargetAwayFrom<BlockPos> avoidRepellent() {
		return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false);
	}

	private static CopyMemoryWithExpiry<Piglin, LivingEntity> babyAvoidNemesis() {
		return new CopyMemoryWithExpiry<>(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
	}

	private static CopyMemoryWithExpiry<Piglin, LivingEntity> avoidZombified() {
		return new CopyMemoryWithExpiry<>(
			PiglinAi::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION
		);
	}

	protected static void updateActivity(Piglin piglin) {
		Brain<Piglin> brain = piglin.getBrain();
		Activity activity = (Activity)brain.getActiveNonCoreActivity().orElse(null);
		brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
		Activity activity2 = (Activity)brain.getActiveNonCoreActivity().orElse(null);
		if (activity != activity2) {
			getSoundForCurrentActivity(piglin).ifPresent(piglin::playSound);
		}

		piglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
		if (!brain.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && isBabyRidingBaby(piglin)) {
			piglin.stopRiding();
		}

		if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
			brain.eraseMemory(MemoryModuleType.DANCING);
		}

		piglin.setDancing(brain.hasMemoryValue(MemoryModuleType.DANCING));
	}

	private static boolean isBabyRidingBaby(Piglin piglin) {
		if (!piglin.isBaby()) {
			return false;
		} else {
			Entity entity = piglin.getVehicle();
			return entity instanceof Piglin && ((Piglin)entity).isBaby() || entity instanceof Hoglin && ((Hoglin)entity).isBaby();
		}
	}

	protected static void pickUpItem(Piglin piglin, ItemEntity itemEntity) {
		stopWalking(piglin);
		ItemStack itemStack;
		if (itemEntity.getItem().is(Items.GOLD_NUGGET)) {
			piglin.take(itemEntity, itemEntity.getItem().getCount());
			itemStack = itemEntity.getItem();
			itemEntity.discard();
		} else {
			piglin.take(itemEntity, 1);
			itemStack = removeOneItemFromItemEntity(itemEntity);
		}

		if (isLovedItem(itemStack)) {
			piglin.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
			holdInOffhand(piglin, itemStack);
			admireGoldItem(piglin);
		} else if (isFood(itemStack) && !hasEatenRecently(piglin)) {
			eat(piglin);
		} else {
			boolean bl = piglin.equipItemIfPossible(itemStack);
			if (!bl) {
				putInInventory(piglin, itemStack);
			}
		}
	}

	private static void holdInOffhand(Piglin piglin, ItemStack itemStack) {
		if (isHoldingItemInOffHand(piglin)) {
			piglin.spawnAtLocation(piglin.getItemInHand(InteractionHand.OFF_HAND));
		}

		piglin.holdInOffHand(itemStack);
	}

	private static ItemStack removeOneItemFromItemEntity(ItemEntity itemEntity) {
		ItemStack itemStack = itemEntity.getItem();
		ItemStack itemStack2 = itemStack.split(1);
		if (itemStack.isEmpty()) {
			itemEntity.discard();
		} else {
			itemEntity.setItem(itemStack);
		}

		return itemStack2;
	}

	protected static void stopHoldingOffHandItem(Piglin piglin, boolean bl) {
		ItemStack itemStack = piglin.getItemInHand(InteractionHand.OFF_HAND);
		piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		if (piglin.isAdult()) {
			boolean bl2 = isBarterCurrency(itemStack);
			if (bl && bl2) {
				throwItems(piglin, getBarterResponseItems(piglin));
			} else if (!bl2) {
				boolean bl3 = piglin.equipItemIfPossible(itemStack);
				if (!bl3) {
					putInInventory(piglin, itemStack);
				}
			}
		} else {
			boolean bl2 = piglin.equipItemIfPossible(itemStack);
			if (!bl2) {
				ItemStack itemStack2 = piglin.getMainHandItem();
				if (isLovedItem(itemStack2)) {
					putInInventory(piglin, itemStack2);
				} else {
					throwItems(piglin, Collections.singletonList(itemStack2));
				}

				piglin.holdInMainHand(itemStack);
			}
		}
	}

	protected static void cancelAdmiring(Piglin piglin) {
		if (isAdmiringItem(piglin) && !piglin.getOffhandItem().isEmpty()) {
			piglin.spawnAtLocation(piglin.getOffhandItem());
			piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		}
	}

	private static void putInInventory(Piglin piglin, ItemStack itemStack) {
		ItemStack itemStack2 = piglin.addToInventory(itemStack);
		throwItemsTowardRandomPos(piglin, Collections.singletonList(itemStack2));
	}

	private static void throwItems(Piglin piglin, List<ItemStack> list) {
		Optional<Player> optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
		if (optional.isPresent()) {
			throwItemsTowardPlayer(piglin, (Player)optional.get(), list);
		} else {
			throwItemsTowardRandomPos(piglin, list);
		}
	}

	private static void throwItemsTowardRandomPos(Piglin piglin, List<ItemStack> list) {
		throwItemsTowardPos(piglin, list, getRandomNearbyPos(piglin));
	}

	private static void throwItemsTowardPlayer(Piglin piglin, Player player, List<ItemStack> list) {
		throwItemsTowardPos(piglin, list, player.position());
	}

	private static void throwItemsTowardPos(Piglin piglin, List<ItemStack> list, Vec3 vec3) {
		if (!list.isEmpty()) {
			piglin.swing(InteractionHand.OFF_HAND);

			for (ItemStack itemStack : list) {
				BehaviorUtils.throwItem(piglin, itemStack, vec3.add(0.0, 1.0, 0.0));
			}
		}
	}

	private static List<ItemStack> getBarterResponseItems(Piglin piglin) {
		LootTable lootTable = piglin.level.getServer().getLootTables().get(BuiltInLootTables.PIGLIN_BARTERING);
		return lootTable.getRandomItems(
			new LootContext.Builder((ServerLevel)piglin.level)
				.withParameter(LootContextParams.THIS_ENTITY, piglin)
				.withRandom(piglin.level.random)
				.create(LootContextParamSets.PIGLIN_BARTER)
		);
	}

	private static boolean wantsToDance(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return livingEntity2.getType() != EntityType.HOGLIN ? false : new Random(livingEntity.level.getGameTime()).nextFloat() < 0.1F;
	}

	protected static boolean wantsToPickup(Piglin piglin, ItemStack itemStack) {
		if (piglin.isBaby() && itemStack.is(ItemTags.IGNORED_BY_PIGLIN_BABIES)) {
			return false;
		} else if (itemStack.is(ItemTags.PIGLIN_REPELLENTS)) {
			return false;
		} else if (isAdmiringDisabled(piglin) && piglin.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
			return false;
		} else if (isBarterCurrency(itemStack)) {
			return isNotHoldingLovedItemInOffHand(piglin);
		} else {
			boolean bl = piglin.canAddToInventory(itemStack);
			if (itemStack.is(Items.GOLD_NUGGET)) {
				return bl;
			} else if (isFood(itemStack)) {
				return !hasEatenRecently(piglin) && bl;
			} else {
				return !isLovedItem(itemStack) ? piglin.canReplaceCurrentItem(itemStack) : isNotHoldingLovedItemInOffHand(piglin) && bl;
			}
		}
	}

	protected static boolean isLovedItem(ItemStack itemStack) {
		return itemStack.is(ItemTags.PIGLIN_LOVED);
	}

	private static boolean wantsToStopRiding(Piglin piglin, Entity entity) {
		return !(entity instanceof Mob mob)
			? false
			: !mob.isBaby() || !mob.isAlive() || wasHurtRecently(piglin) || wasHurtRecently(mob) || mob instanceof Piglin && mob.getVehicle() == null;
	}

	private static boolean isNearestValidAttackTarget(Piglin piglin, LivingEntity livingEntity) {
		return findNearestValidAttackTarget(piglin).filter(livingEntity2 -> livingEntity2 == livingEntity).isPresent();
	}

	private static boolean isNearZombified(Piglin piglin) {
		Brain<Piglin> brain = piglin.getBrain();
		if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
			LivingEntity livingEntity = (LivingEntity)brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
			return piglin.closerThan(livingEntity, 6.0);
		} else {
			return false;
		}
	}

	private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Piglin piglin) {
		Brain<Piglin> brain = piglin.getBrain();
		if (isNearZombified(piglin)) {
			return Optional.empty();
		} else {
			Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(piglin, MemoryModuleType.ANGRY_AT);
			if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(piglin, (LivingEntity)optional.get())) {
				return optional;
			} else {
				if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
					Optional<Player> optional2 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
					if (optional2.isPresent()) {
						return optional2;
					}
				}

				Optional<Mob> optional2 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
				if (optional2.isPresent()) {
					return optional2;
				} else {
					Optional<Player> optional3 = brain.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
					return optional3.isPresent() && Sensor.isEntityAttackable(piglin, (LivingEntity)optional3.get()) ? optional3 : Optional.empty();
				}
			}
		}
	}

	public static void angerNearbyPiglins(Player player, boolean bl) {
		List<Piglin> list = player.level.getEntitiesOfClass(Piglin.class, player.getBoundingBox().inflate(16.0));
		list.stream().filter(PiglinAi::isIdle).filter(piglin -> !bl || BehaviorUtils.canSee(piglin, player)).forEach(piglin -> {
			if (piglin.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
				setAngerTargetToNearestTargetablePlayerIfFound(piglin, player);
			} else {
				setAngerTarget(piglin, player);
			}
		});
	}

	public static InteractionResult mobInteract(Piglin piglin, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (canAdmire(piglin, itemStack)) {
			ItemStack itemStack2 = itemStack.split(1);
			holdInOffhand(piglin, itemStack2);
			admireGoldItem(piglin);
			stopWalking(piglin);
			return InteractionResult.CONSUME;
		} else {
			return InteractionResult.PASS;
		}
	}

	protected static boolean canAdmire(Piglin piglin, ItemStack itemStack) {
		return !isAdmiringDisabled(piglin) && !isAdmiringItem(piglin) && piglin.isAdult() && isBarterCurrency(itemStack);
	}

	protected static void wasHurtBy(Piglin piglin, LivingEntity livingEntity) {
		if (!(livingEntity instanceof Piglin)) {
			if (isHoldingItemInOffHand(piglin)) {
				stopHoldingOffHandItem(piglin, false);
			}

			Brain<Piglin> brain = piglin.getBrain();
			brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
			brain.eraseMemory(MemoryModuleType.DANCING);
			brain.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
			if (livingEntity instanceof Player) {
				brain.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
			}

			getAvoidTarget(piglin).ifPresent(livingEntity2 -> {
				if (livingEntity2.getType() != livingEntity.getType()) {
					brain.eraseMemory(MemoryModuleType.AVOID_TARGET);
				}
			});
			if (piglin.isBaby()) {
				brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, 100L);
				if (Sensor.isEntityAttackableIgnoringLineOfSight(piglin, livingEntity)) {
					broadcastAngerTarget(piglin, livingEntity);
				}
			} else if (livingEntity.getType() == EntityType.HOGLIN && hoglinsOutnumberPiglins(piglin)) {
				setAvoidTargetAndDontHuntForAWhile(piglin, livingEntity);
				broadcastRetreat(piglin, livingEntity);
			} else {
				maybeRetaliate(piglin, livingEntity);
			}
		}
	}

	protected static void maybeRetaliate(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
		if (!abstractPiglin.getBrain().isActive(Activity.AVOID)) {
			if (Sensor.isEntityAttackableIgnoringLineOfSight(abstractPiglin, livingEntity)) {
				if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(abstractPiglin, livingEntity, 4.0)) {
					if (livingEntity.getType() == EntityType.PLAYER && abstractPiglin.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
						setAngerTargetToNearestTargetablePlayerIfFound(abstractPiglin, livingEntity);
						broadcastUniversalAnger(abstractPiglin);
					} else {
						setAngerTarget(abstractPiglin, livingEntity);
						broadcastAngerTarget(abstractPiglin, livingEntity);
					}
				}
			}
		}
	}

	public static Optional<SoundEvent> getSoundForCurrentActivity(Piglin piglin) {
		return piglin.getBrain().getActiveNonCoreActivity().map(activity -> getSoundForActivity(piglin, activity));
	}

	private static SoundEvent getSoundForActivity(Piglin piglin, Activity activity) {
		if (activity == Activity.FIGHT) {
			return SoundEvents.PIGLIN_ANGRY;
		} else if (piglin.isConverting()) {
			return SoundEvents.PIGLIN_RETREAT;
		} else if (activity == Activity.AVOID && isNearAvoidTarget(piglin)) {
			return SoundEvents.PIGLIN_RETREAT;
		} else if (activity == Activity.ADMIRE_ITEM) {
			return SoundEvents.PIGLIN_ADMIRING_ITEM;
		} else if (activity == Activity.CELEBRATE) {
			return SoundEvents.PIGLIN_CELEBRATE;
		} else if (seesPlayerHoldingLovedItem(piglin)) {
			return SoundEvents.PIGLIN_JEALOUS;
		} else {
			return isNearRepellent(piglin) ? SoundEvents.PIGLIN_RETREAT : SoundEvents.PIGLIN_AMBIENT;
		}
	}

	private static boolean isNearAvoidTarget(Piglin piglin) {
		Brain<Piglin> brain = piglin.getBrain();
		return !brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)
			? false
			: ((LivingEntity)brain.getMemory(MemoryModuleType.AVOID_TARGET).get()).closerThan(piglin, 12.0);
	}

	protected static boolean hasAnyoneNearbyHuntedRecently(Piglin piglin) {
		return piglin.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY)
			|| getVisibleAdultPiglins(piglin).stream().anyMatch(abstractPiglin -> abstractPiglin.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY));
	}

	private static List<AbstractPiglin> getVisibleAdultPiglins(Piglin piglin) {
		return (List<AbstractPiglin>)piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
	}

	private static List<AbstractPiglin> getAdultPiglins(AbstractPiglin abstractPiglin) {
		return (List<AbstractPiglin>)abstractPiglin.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
	}

	public static boolean isWearingGold(LivingEntity livingEntity) {
		for (ItemStack itemStack : livingEntity.getArmorSlots()) {
			Item item = itemStack.getItem();
			if (item instanceof ArmorItem && ((ArmorItem)item).getMaterial() == ArmorMaterials.GOLD) {
				return true;
			}
		}

		return false;
	}

	private static void stopWalking(Piglin piglin) {
		piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		piglin.getNavigation().stop();
	}

	private static RunSometimes<Piglin> babySometimesRideBabyHoglin() {
		return new RunSometimes<>(
			new CopyMemoryWithExpiry<>(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION), RIDE_START_INTERVAL
		);
	}

	protected static void broadcastAngerTarget(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
		getAdultPiglins(abstractPiglin).forEach(abstractPiglinx -> {
			if (livingEntity.getType() != EntityType.HOGLIN || abstractPiglinx.canHunt() && ((Hoglin)livingEntity).canBeHunted()) {
				setAngerTargetIfCloserThanCurrent(abstractPiglinx, livingEntity);
			}
		});
	}

	protected static void broadcastUniversalAnger(AbstractPiglin abstractPiglin) {
		getAdultPiglins(abstractPiglin)
			.forEach(abstractPiglinx -> getNearestVisibleTargetablePlayer(abstractPiglinx).ifPresent(player -> setAngerTarget(abstractPiglinx, player)));
	}

	protected static void broadcastDontKillAnyMoreHoglinsForAWhile(Piglin piglin) {
		getVisibleAdultPiglins(piglin).forEach(PiglinAi::dontKillAnyMoreHoglinsForAWhile);
	}

	protected static void setAngerTarget(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
		if (Sensor.isEntityAttackableIgnoringLineOfSight(abstractPiglin, livingEntity)) {
			abstractPiglin.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
			abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, livingEntity.getUUID(), 600L);
			if (livingEntity.getType() == EntityType.HOGLIN && abstractPiglin.canHunt()) {
				dontKillAnyMoreHoglinsForAWhile(abstractPiglin);
			}

			if (livingEntity.getType() == EntityType.PLAYER && abstractPiglin.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
				abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
			}
		}
	}

	private static void setAngerTargetToNearestTargetablePlayerIfFound(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
		Optional<Player> optional = getNearestVisibleTargetablePlayer(abstractPiglin);
		if (optional.isPresent()) {
			setAngerTarget(abstractPiglin, (LivingEntity)optional.get());
		} else {
			setAngerTarget(abstractPiglin, livingEntity);
		}
	}

	private static void setAngerTargetIfCloserThanCurrent(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
		Optional<LivingEntity> optional = getAngerTarget(abstractPiglin);
		LivingEntity livingEntity2 = BehaviorUtils.getNearestTarget(abstractPiglin, optional, livingEntity);
		if (!optional.isPresent() || optional.get() != livingEntity2) {
			setAngerTarget(abstractPiglin, livingEntity2);
		}
	}

	private static Optional<LivingEntity> getAngerTarget(AbstractPiglin abstractPiglin) {
		return BehaviorUtils.getLivingEntityFromUUIDMemory(abstractPiglin, MemoryModuleType.ANGRY_AT);
	}

	public static Optional<LivingEntity> getAvoidTarget(Piglin piglin) {
		return piglin.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? piglin.getBrain().getMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
	}

	public static Optional<Player> getNearestVisibleTargetablePlayer(AbstractPiglin abstractPiglin) {
		return abstractPiglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
			? abstractPiglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
			: Optional.empty();
	}

	private static void broadcastRetreat(Piglin piglin, LivingEntity livingEntity) {
		getVisibleAdultPiglins(piglin)
			.stream()
			.filter(abstractPiglin -> abstractPiglin instanceof Piglin)
			.forEach(abstractPiglin -> retreatFromNearestTarget((Piglin)abstractPiglin, livingEntity));
	}

	private static void retreatFromNearestTarget(Piglin piglin, LivingEntity livingEntity) {
		Brain<Piglin> brain = piglin.getBrain();
		LivingEntity livingEntity2 = BehaviorUtils.getNearestTarget(piglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), livingEntity);
		livingEntity2 = BehaviorUtils.getNearestTarget(piglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), livingEntity2);
		setAvoidTargetAndDontHuntForAWhile(piglin, livingEntity2);
	}

	private static boolean wantsToStopFleeing(Piglin piglin) {
		Brain<Piglin> brain = piglin.getBrain();
		if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
			return true;
		} else {
			LivingEntity livingEntity = (LivingEntity)brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
			EntityType<?> entityType = livingEntity.getType();
			if (entityType == EntityType.HOGLIN) {
				return piglinsEqualOrOutnumberHoglins(piglin);
			} else {
				return isZombified(entityType) ? !brain.isMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, livingEntity) : false;
			}
		}
	}

	private static boolean piglinsEqualOrOutnumberHoglins(Piglin piglin) {
		return !hoglinsOutnumberPiglins(piglin);
	}

	private static boolean hoglinsOutnumberPiglins(Piglin piglin) {
		int i = (Integer)piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
		int j = (Integer)piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
		return j > i;
	}

	private static void setAvoidTargetAndDontHuntForAWhile(Piglin piglin, LivingEntity livingEntity) {
		piglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
		piglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
		piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, (long)RETREAT_DURATION.sample(piglin.level.random));
		dontKillAnyMoreHoglinsForAWhile(piglin);
	}

	protected static void dontKillAnyMoreHoglinsForAWhile(AbstractPiglin abstractPiglin) {
		abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)TIME_BETWEEN_HUNTS.sample(abstractPiglin.level.random));
	}

	private static boolean seesPlayerHoldingWantedItem(Piglin piglin) {
		return piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
	}

	private static void eat(Piglin piglin) {
		piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
	}

	private static Vec3 getRandomNearbyPos(Piglin piglin) {
		Vec3 vec3 = LandRandomPos.getPos(piglin, 4, 2);
		return vec3 == null ? piglin.position() : vec3;
	}

	private static boolean hasEatenRecently(Piglin piglin) {
		return piglin.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
	}

	protected static boolean isIdle(AbstractPiglin abstractPiglin) {
		return abstractPiglin.getBrain().isActive(Activity.IDLE);
	}

	private static boolean hasCrossbow(LivingEntity livingEntity) {
		return livingEntity.isHolding(Items.CROSSBOW);
	}

	private static void admireGoldItem(LivingEntity livingEntity) {
		livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 120L);
	}

	private static boolean isAdmiringItem(Piglin piglin) {
		return piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
	}

	private static boolean isBarterCurrency(ItemStack itemStack) {
		return itemStack.is(BARTERING_ITEM);
	}

	private static boolean isFood(ItemStack itemStack) {
		return itemStack.is(ItemTags.PIGLIN_FOOD);
	}

	private static boolean isNearRepellent(Piglin piglin) {
		return piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
	}

	private static boolean seesPlayerHoldingLovedItem(LivingEntity livingEntity) {
		return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
	}

	private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity livingEntity) {
		return !seesPlayerHoldingLovedItem(livingEntity);
	}

	public static boolean isPlayerHoldingLovedItem(LivingEntity livingEntity) {
		return livingEntity.getType() == EntityType.PLAYER && livingEntity.isHolding(PiglinAi::isLovedItem);
	}

	private static boolean isAdmiringDisabled(Piglin piglin) {
		return piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
	}

	private static boolean wasHurtRecently(LivingEntity livingEntity) {
		return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
	}

	private static boolean isHoldingItemInOffHand(Piglin piglin) {
		return !piglin.getOffhandItem().isEmpty();
	}

	private static boolean isNotHoldingLovedItemInOffHand(Piglin piglin) {
		return piglin.getOffhandItem().isEmpty() || !isLovedItem(piglin.getOffhandItem());
	}

	public static boolean isZombified(EntityType<?> entityType) {
		return entityType == EntityType.ZOMBIFIED_PIGLIN || entityType == EntityType.ZOGLIN;
	}
}
