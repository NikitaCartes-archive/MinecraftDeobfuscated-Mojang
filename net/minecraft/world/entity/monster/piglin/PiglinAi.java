/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SerializableBoolean;
import net.minecraft.core.SerializableUUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IntRange;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BackUpIfTooClose;
import net.minecraft.world.entity.ai.behavior.Behavior;
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
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.RememberIfHoglinWasKilled;
import net.minecraft.world.entity.monster.piglin.StartAdmiringItemIfSeen;
import net.minecraft.world.entity.monster.piglin.StartHuntingHoglin;
import net.minecraft.world.entity.monster.piglin.StopAdmiringIfItemTooFarAway;
import net.minecraft.world.entity.monster.piglin.StopHoldingItemIfNoLongerAdmiring;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class PiglinAi {
    private static final IntRange RANDOM_STROLL_INTERVAL_WHEN_ADMIRING = IntRange.of(10, 20);
    private static final IntRange TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
    private static final IntRange RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
    private static final IntRange RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
    private static final IntRange RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final Set FOOD_ITEMS = ImmutableSet.of(Items.PORKCHOP, Items.COOKED_PORKCHOP);
    private static final Set<Item> LOVED_ITEMS_IN_ADDITION_TO_GOLD_TIER_AND_GOLD_MATERIAL = ImmutableSet.of(Items.GOLD_INGOT, Items.GOLDEN_APPLE, Items.GOLDEN_HORSE_ARMOR, Items.GOLDEN_CARROT, Items.GOLD_BLOCK, Items.GOLD_ORE, new Item[]{Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_HORSE_ARMOR, Items.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.BELL, Items.GLISTERING_MELON_SLICE, Items.CLOCK, Items.NETHER_GOLD_ORE});

    protected static Brain<?> makeBrain(Piglin piglin, Dynamic<?> dynamic) {
        Brain<Piglin> brain = new Brain<Piglin>(Piglin.MEMORY_TYPES, Piglin.SENSOR_TYPES, dynamic);
        PiglinAi.initCoreActivity(piglin, brain);
        PiglinAi.initIdleActivity(piglin, brain);
        PiglinAi.initAdmireItemActivity(piglin, brain);
        PiglinAi.initFightActivity(piglin, brain);
        PiglinAi.initCelebrateActivity(piglin, brain);
        PiglinAi.initRetreatActivity(piglin, brain);
        PiglinAi.initRideHoglinActivity(piglin, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    protected static void initMemories(Piglin piglin) {
        int i = TIME_BETWEEN_HUNTS.randomValue(piglin.level.random);
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, SerializableBoolean.of(true), i);
    }

    private static void initCoreActivity(Piglin piglin, Brain<Piglin> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(200), new InteractWithDoor(), new StopHoldingItemIfNoLongerAdmiring(), new StartAdmiringItemIfSeen(120), new StartCelebratingIfTargetDead(300), new StopBeingAngryIfTargetDead()));
    }

    private static void initIdleActivity(Piglin piglin, Brain<Piglin> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 14.0f), new StartAttacking<Piglin>(Piglin::isAdult, PiglinAi::findNearestValidAttackTarget), new StartHuntingHoglin(), PiglinAi.avoidZombifiedPiglin(), PiglinAi.avoidRepellent(), PiglinAi.babySometimesRideBabyHoglin(), PiglinAi.createIdleLookBehaviors(), PiglinAi.createIdleMovementBehaviors(), new SetLookAndInteract(EntityType.PLAYER, 4)));
    }

    private static void initFightActivity(Piglin piglin, Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(new StopAttackingIfTargetInvalid(livingEntity -> !PiglinAi.isNearestValidAttackTarget(piglin, livingEntity)), new RunIf<Piglin>(PiglinAi::hasCrossbow, new BackUpIfTooClose(5, 0.75f)), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0f), new MeleeAttack(20), new CrossbowAttack(), new RememberIfHoglinWasKilled()), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCelebrateActivity(Piglin piglin, Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.CELEBRATE, 10, ImmutableList.of(PiglinAi.avoidZombifiedPiglin(), PiglinAi.avoidRepellent(), new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 14.0f), new StartAttacking<Piglin>(Piglin::isAdult, PiglinAi::findNearestValidAttackTarget), new GoToCelebrateLocation(2, 1.0f), new RunOne(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0f), 1), Pair.of(new RandomStroll(0.6f, 2, 1), 1), Pair.of(new DoNothing(10, 20), 1)))), MemoryModuleType.CELEBRATE_LOCATION);
    }

    private static void initAdmireItemActivity(Piglin piglin, Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.ADMIRE_ITEM, 10, ImmutableList.of(new GoToWantedItem<Piglin>(PiglinAi::isNotHoldingLovedItemInOffHand, 1.0f, true, 9), new RunIf<Piglin>(PiglinAi::isHoldingItemInOffHand, PiglinAi.admireHeldItem()), new StopAdmiringIfItemTooFarAway(9)), MemoryModuleType.ADMIRING_ITEM);
    }

    private static void initRetreatActivity(Piglin piglin, Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.1f, 6, false), PiglinAi.createIdleLookBehaviors(), PiglinAi.createIdleMovementBehaviors(), new EraseMemoryIf<Piglin>(PiglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
    }

    private static void initRideHoglinActivity(Piglin piglin, Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.RIDE, 10, ImmutableList.of(new Mount(0.8f), new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 8.0f), new RunIf<Piglin>(Entity::isPassenger, PiglinAi.createIdleLookBehaviors()), new DismountOrSkipMounting(8, PiglinAi::wantsToStopRiding)), MemoryModuleType.RIDE_TARGET);
    }

    private static RunOne<Piglin> createIdleLookBehaviors() {
        return new RunOne(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0f), 1), Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0f), 1), Pair.of(new SetEntityLookTarget(8.0f), 1), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<Piglin> createIdleMovementBehaviors() {
        return new RunOne(ImmutableList.of(Pair.of(new RandomStroll(0.6f), 2), Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6f, 2), 2), Pair.of(new RunIf<LivingEntity>(PiglinAi::doesntSeeAnyPlayerHoldingLovedItem, new SetWalkTargetFromLookTarget(0.6f, 3)), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static Behavior<Piglin> admireHeldItem() {
        return new RunSometimes<PathfinderMob>(new RandomStroll(0.3f, 1, 0), RANDOM_STROLL_INTERVAL_WHEN_ADMIRING);
    }

    private static SetWalkTargetAwayFrom<BlockPos> avoidRepellent() {
        return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.1f, 8, false);
    }

    private static SetWalkTargetAwayFrom<?> avoidZombifiedPiglin() {
        return SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED_PIGLIN, 1.1f, 10, false);
    }

    protected static void updateActivity(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
        Activity activity2 = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != activity2) {
            PiglinAi.playActivitySound(piglin);
        }
        piglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        if (!brain.hasMemoryValue(MemoryModuleType.RIDE_TARGET)) {
            piglin.stopRiding();
        }
        if (piglin.isPassenger() && PiglinAi.seesPlayerHoldingWantedItem(piglin)) {
            piglin.stopRiding();
            piglin.getBrain().eraseMemory(MemoryModuleType.RIDE_TARGET);
        }
    }

    protected static void pickUpItem(Piglin piglin, ItemEntity itemEntity) {
        PiglinAi.stopWalking(piglin);
        piglin.take(itemEntity, 1);
        ItemStack itemStack = PiglinAi.removeOneItemFromItemEntity(itemEntity);
        Item item = itemStack.getItem();
        if (PiglinAi.isLovedItem(item)) {
            if (PiglinAi.isHoldingItemInOffHand(piglin)) {
                piglin.spawnAtLocation(piglin.getItemInHand(InteractionHand.OFF_HAND));
            }
            piglin.holdInOffHand(itemStack);
            PiglinAi.admireGoldItem(piglin);
            return;
        }
        if (PiglinAi.isFood(item) && !PiglinAi.hasEatenRecently(piglin)) {
            PiglinAi.eat(piglin);
            return;
        }
        boolean bl = piglin.equipItemIfPossible(itemStack);
        if (bl) {
            return;
        }
        PiglinAi.putInInventory(piglin, itemStack);
    }

    private static ItemStack removeOneItemFromItemEntity(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        ItemStack itemStack2 = itemStack.split(1);
        if (itemStack.isEmpty()) {
            itemEntity.remove();
        } else {
            itemEntity.setItem(itemStack);
        }
        return itemStack2;
    }

    protected static void stopHoldingOffHandItem(Piglin piglin, boolean bl) {
        ItemStack itemStack = piglin.getItemInHand(InteractionHand.OFF_HAND);
        piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (piglin.isAdult()) {
            if (bl && PiglinAi.isBarterCurrency(itemStack.getItem())) {
                PiglinAi.throwItem(piglin, PiglinAi.getBarterResponseItem(piglin));
            } else {
                boolean bl2 = piglin.equipItemIfPossible(itemStack);
                if (!bl2) {
                    PiglinAi.putInInventory(piglin, itemStack);
                }
            }
        } else {
            boolean bl2 = piglin.equipItemIfPossible(itemStack);
            if (!bl2) {
                ItemStack itemStack2 = piglin.getMainHandItem();
                if (PiglinAi.isLovedItem(itemStack2.getItem())) {
                    PiglinAi.putInInventory(piglin, itemStack2);
                } else {
                    PiglinAi.throwItem(piglin, itemStack2);
                }
                piglin.holdInMainHand(itemStack);
            }
        }
    }

    protected static void cancelAdmiring(Piglin piglin) {
        if (PiglinAi.isAdmiringItem(piglin) && !piglin.getOffhandItem().isEmpty()) {
            piglin.spawnAtLocation(piglin.getOffhandItem());
            piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
    }

    private static void putInInventory(Piglin piglin, ItemStack itemStack) {
        ItemStack itemStack2 = piglin.addToInventory(itemStack);
        PiglinAi.throwItemTowardRandomPos(piglin, itemStack2);
    }

    private static void throwItem(Piglin piglin, ItemStack itemStack) {
        Optional<Player> optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (optional.isPresent()) {
            PiglinAi.throwItemTowardPlayer(piglin, optional.get(), itemStack);
        } else {
            PiglinAi.throwItemTowardRandomPos(piglin, itemStack);
        }
    }

    private static void throwItemTowardRandomPos(Piglin piglin, ItemStack itemStack) {
        PiglinAi.throwItemTowardPos(piglin, itemStack, PiglinAi.getRandomNearbyPos(piglin));
    }

    private static void throwItemTowardPlayer(Piglin piglin, Player player, ItemStack itemStack) {
        PiglinAi.throwItemTowardPos(piglin, itemStack, player.position());
    }

    private static void throwItemTowardPos(Piglin piglin, ItemStack itemStack, Vec3 vec3) {
        if (!itemStack.isEmpty()) {
            piglin.swing(InteractionHand.OFF_HAND);
            BehaviorUtils.throwItem(piglin, itemStack, vec3.add(0.0, 1.0, 0.0));
        }
    }

    private static ItemStack getBarterResponseItem(Piglin piglin) {
        LootTable lootTable = piglin.level.getServer().getLootTables().get(BuiltInLootTables.PIGLIN_BARTERING);
        List<ItemStack> list = lootTable.getRandomItems(new LootContext.Builder((ServerLevel)piglin.level).withParameter(LootContextParams.THIS_ENTITY, piglin).withRandom(piglin.level.random).create(LootContextParamSets.PIGLIN_BARTER));
        return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
    }

    protected static boolean wantsToPickup(Piglin piglin, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item == Items.GOLD_NUGGET) {
            return true;
        }
        if (item.is(ItemTags.PIGLIN_REPELLENTS)) {
            return false;
        }
        if (PiglinAi.isAdmiringDisabled(piglin) && piglin.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        }
        if (PiglinAi.isFood(item)) {
            return !PiglinAi.hasEatenRecently(piglin);
        }
        if (PiglinAi.isLovedItem(item)) {
            return PiglinAi.isNotHoldingLovedItemInOffHand(piglin);
        }
        return piglin.canReplaceCurrentItem(itemStack);
    }

    protected static boolean isLovedItem(Item item) {
        return LOVED_ITEMS_IN_ADDITION_TO_GOLD_TIER_AND_GOLD_MATERIAL.contains(item) || item instanceof TieredItem && ((TieredItem)item).getTier() == Tiers.GOLD || item instanceof ArmorItem && ((ArmorItem)item).getMaterial() == ArmorMaterials.GOLD;
    }

    private static boolean wantsToStopRiding(Piglin piglin, Entity entity) {
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            return !mob.isBaby() || !mob.isAlive() || PiglinAi.wasHurtRecently(piglin) || PiglinAi.wasHurtRecently(mob) || mob instanceof Piglin && mob.getVehicle() == null;
        }
        return false;
    }

    private static boolean isNearestValidAttackTarget(Piglin piglin, LivingEntity livingEntity) {
        return PiglinAi.findNearestValidAttackTarget(piglin).filter(livingEntity2 -> livingEntity2 == livingEntity).isPresent();
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(piglin, MemoryModuleType.ANGRY_AT);
        if (optional.isPresent() && PiglinAi.isAttackAllowed(optional.get())) {
            return optional;
        }
        Optional<WitherSkeleton> optional2 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_WITHER_SKELETON);
        if (optional2.isPresent()) {
            return optional2;
        }
        Optional<Player> optional3 = brain.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
        if (optional3.isPresent() && PiglinAi.isAttackAllowed(optional3.get())) {
            return optional3;
        }
        return Optional.empty();
    }

    public static void angerNearbyPiglinsThatSee(Player player) {
        if (!PiglinAi.isAttackAllowed(player)) {
            return;
        }
        List<Piglin> list = player.level.getEntitiesOfClass(Piglin.class, player.getBoundingBox().inflate(16.0));
        list.stream().filter(PiglinAi::isIdle).filter(piglin -> BehaviorUtils.canSee(piglin, player)).forEach(piglin -> PiglinAi.setAngerTarget(piglin, player));
    }

    public static boolean mobInteract(Piglin piglin, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        Item item = itemStack.getItem();
        if (!PiglinAi.isAdmiringItem(piglin) && piglin.isAdult() && PiglinAi.isBarterCurrency(item) && !PiglinAi.isAdmiringDisabled(piglin)) {
            ItemStack itemStack2 = itemStack.split(1);
            piglin.holdInOffHand(itemStack2);
            PiglinAi.admireGoldItem(piglin);
            return true;
        }
        return false;
    }

    protected static void wasHurtBy(Piglin piglin, LivingEntity livingEntity) {
        if (livingEntity instanceof Piglin) {
            return;
        }
        if (PiglinAi.isHoldingItemInOffHand(piglin)) {
            PiglinAi.stopHoldingOffHandItem(piglin, false);
        }
        Brain<Piglin> brain = piglin.getBrain();
        brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
        brain.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
        if (livingEntity instanceof Player) {
            brain.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, SerializableBoolean.of(true), 400L);
        }
        if (piglin.isBaby()) {
            brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, 100L);
            if (PiglinAi.isAttackAllowed(livingEntity)) {
                PiglinAi.broadcastAngerTarget(piglin, livingEntity);
            }
            return;
        }
        if (livingEntity.getType() == EntityType.HOGLIN && PiglinAi.hoglinsOutnumberPiglins(piglin)) {
            PiglinAi.setAvoidTargetAndDontHuntForAWhile(piglin, livingEntity);
            PiglinAi.broadcastRetreat(piglin, livingEntity);
            return;
        }
        PiglinAi.maybeRetaliate(piglin, livingEntity);
    }

    private static void maybeRetaliate(Piglin piglin, LivingEntity livingEntity) {
        if (piglin.getBrain().isActive(Activity.AVOID) && livingEntity.getType() == EntityType.HOGLIN) {
            return;
        }
        if (!PiglinAi.isAttackAllowed(livingEntity)) {
            return;
        }
        if (BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(piglin, livingEntity, 4.0)) {
            return;
        }
        PiglinAi.setAngerTarget(piglin, livingEntity);
        PiglinAi.broadcastAngerTarget(piglin, livingEntity);
    }

    private static void playActivitySound(Piglin piglin) {
        piglin.getBrain().getActiveNonCoreActivity().ifPresent(activity -> {
            if (activity == Activity.FIGHT) {
                piglin.playAngrySound();
            } else if (activity == Activity.AVOID || piglin.isConverting()) {
                piglin.playRetreatSound();
            } else if (activity == Activity.ADMIRE_ITEM) {
                piglin.playAdmiringSound();
            } else if (activity == Activity.CELEBRATE) {
                piglin.playCelebrateSound();
            } else if (PiglinAi.seesPlayerHoldingLovedItem(piglin)) {
                piglin.playJealousSound();
            } else if (PiglinAi.seesZombifiedPiglin(piglin) || PiglinAi.seesRepellent(piglin)) {
                piglin.playRetreatSound();
            }
        });
    }

    protected static void maybePlayActivitySound(Piglin piglin) {
        if ((double)piglin.level.random.nextFloat() < 0.0125) {
            PiglinAi.playActivitySound(piglin);
        }
    }

    protected static boolean hasAnyoneNearbyHuntedRecently(Piglin piglin2) {
        return piglin2.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY) || PiglinAi.getVisibleAdultPiglins(piglin2).stream().anyMatch(piglin -> piglin.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY));
    }

    private static List<Piglin> getVisibleAdultPiglins(Piglin piglin) {
        return piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    public static boolean isWearingGold(LivingEntity livingEntity) {
        Iterable<ItemStack> iterable = livingEntity.getArmorSlots();
        for (ItemStack itemStack : iterable) {
            Item item = itemStack.getItem();
            if (!(item instanceof ArmorItem) || ((ArmorItem)item).getMaterial() != ArmorMaterials.GOLD) continue;
            return true;
        }
        return false;
    }

    private static void stopWalking(Piglin piglin) {
        piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        piglin.getNavigation().stop();
    }

    private static RunSometimes<Piglin> babySometimesRideBabyHoglin() {
        return new RunSometimes<Piglin>(new CopyMemoryWithExpiry<Piglin, Entity>(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION), RIDE_START_INTERVAL);
    }

    protected static void broadcastAngerTarget(Piglin piglin2, LivingEntity livingEntity) {
        PiglinAi.getVisibleAdultPiglins(piglin2).forEach(piglin -> PiglinAi.setAngerTargetIfCloserThanCurrent(piglin, livingEntity));
    }

    protected static void broadcastDontKillAnyMoreHoglinsForAWhile(Piglin piglin) {
        PiglinAi.getVisibleAdultPiglins(piglin).forEach(PiglinAi::dontKillAnyMoreHoglinsForAWhile);
    }

    protected static void setAngerTarget(Piglin piglin, LivingEntity livingEntity) {
        piglin.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, new SerializableUUID(livingEntity.getUUID()), 600L);
        if (livingEntity.getType() == EntityType.HOGLIN) {
            PiglinAi.dontKillAnyMoreHoglinsForAWhile(piglin);
        }
    }

    private static void setAngerTargetIfCloserThanCurrent(Piglin piglin, LivingEntity livingEntity) {
        Optional<LivingEntity> optional = PiglinAi.getAngerTarget(piglin);
        LivingEntity livingEntity2 = BehaviorUtils.getNearestTarget(piglin, optional, livingEntity);
        if (optional.isPresent() && optional.get() == livingEntity2) {
            return;
        }
        PiglinAi.setAngerTarget(piglin, livingEntity2);
    }

    private static Optional<LivingEntity> getAngerTarget(Piglin piglin) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(piglin, MemoryModuleType.ANGRY_AT);
    }

    private static void broadcastRetreat(Piglin piglin2, LivingEntity livingEntity) {
        PiglinAi.getVisibleAdultPiglins(piglin2).forEach(piglin -> PiglinAi.retreatFromNearestTarget(piglin, livingEntity));
    }

    private static void retreatFromNearestTarget(Piglin piglin, LivingEntity livingEntity) {
        Brain<Piglin> brain = piglin.getBrain();
        LivingEntity livingEntity2 = livingEntity;
        livingEntity2 = BehaviorUtils.getNearestTarget(piglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), livingEntity2);
        livingEntity2 = BehaviorUtils.getNearestTarget(piglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), livingEntity2);
        PiglinAi.setAvoidTargetAndDontHuntForAWhile(piglin, livingEntity2);
    }

    private static boolean wantsToStopFleeing(Piglin piglin) {
        return piglin.isAdult() && PiglinAi.piglinsEqualOrOutnumberHoglins(piglin);
    }

    private static boolean piglinsEqualOrOutnumberHoglins(Piglin piglin) {
        return !PiglinAi.hoglinsOutnumberPiglins(piglin);
    }

    private static boolean hoglinsOutnumberPiglins(Piglin piglin) {
        int i = piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
        int j = piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
        return j > i;
    }

    private static void setAvoidTargetAndDontHuntForAWhile(Piglin piglin, LivingEntity livingEntity) {
        piglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        piglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, RETREAT_DURATION.randomValue(piglin.level.random));
        PiglinAi.dontKillAnyMoreHoglinsForAWhile(piglin);
    }

    protected static void dontKillAnyMoreHoglinsForAWhile(Piglin piglin) {
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, SerializableBoolean.of(true), TIME_BETWEEN_HUNTS.randomValue(piglin.level.random));
    }

    private static boolean seesPlayerHoldingWantedItem(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static void eat(Piglin piglin) {
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }

    private static Vec3 getRandomNearbyPos(Piglin piglin) {
        Vec3 vec3 = RandomPos.getLandPos(piglin, 4, 2);
        return vec3 == null ? piglin.position() : vec3;
    }

    private static boolean hasEatenRecently(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
    }

    protected static boolean isIdle(Piglin piglin) {
        return piglin.getBrain().isActive(Activity.IDLE);
    }

    private static boolean hasCrossbow(LivingEntity livingEntity) {
        return livingEntity.isHolding(Items.CROSSBOW);
    }

    private static void admireGoldItem(LivingEntity livingEntity) {
        livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, SerializableBoolean.of(true), 120L);
    }

    private static boolean isAdmiringItem(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
    }

    private static boolean isBarterCurrency(Item item) {
        return item == Items.GOLD_INGOT;
    }

    private static boolean isFood(Item item) {
        return FOOD_ITEMS.contains(item);
    }

    private static boolean isAttackAllowed(LivingEntity livingEntity) {
        return EntitySelector.ATTACK_ALLOWED.test(livingEntity);
    }

    private static boolean seesRepellent(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean seesZombifiedPiglin(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED_PIGLIN);
    }

    private static boolean seesPlayerHoldingLovedItem(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity livingEntity) {
        return !PiglinAi.seesPlayerHoldingLovedItem(livingEntity);
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
        return piglin.getOffhandItem().isEmpty() || !PiglinAi.isLovedItem(piglin.getOffhandItem().getItem());
    }
}

