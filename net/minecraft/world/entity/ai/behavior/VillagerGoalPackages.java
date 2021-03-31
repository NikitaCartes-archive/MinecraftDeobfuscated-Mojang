/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.CelebrateVillagersSurvivedRaid;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.behavior.GoOutsideToCelebrate;
import net.minecraft.world.entity.ai.behavior.GoToClosestVillage;
import net.minecraft.world.entity.ai.behavior.GoToPotentialJobSite;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.behavior.InsideBrownianWalk;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.JumpOnBed;
import net.minecraft.world.entity.ai.behavior.LocateHidingPlace;
import net.minecraft.world.entity.ai.behavior.LocateHidingPlaceDuringRaid;
import net.minecraft.world.entity.ai.behavior.LookAndFollowTradingPlayerSink;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PlayTagWithOtherKids;
import net.minecraft.world.entity.ai.behavior.PoiCompetitorScan;
import net.minecraft.world.entity.ai.behavior.ReactToBell;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.ai.behavior.ResetRaidStatus;
import net.minecraft.world.entity.ai.behavior.RingBell;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetClosestHomeAsWalkTarget;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetHiddenState;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetRaidStatus;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromBlockMemory;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.ai.behavior.SleepInBed;
import net.minecraft.world.entity.ai.behavior.SocializeAtBell;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoiList;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.TradeWithVillager;
import net.minecraft.world.entity.ai.behavior.UpdateActivityFromSchedule;
import net.minecraft.world.entity.ai.behavior.UseBonemeal;
import net.minecraft.world.entity.ai.behavior.ValidateNearbyPoi;
import net.minecraft.world.entity.ai.behavior.VictoryStroll;
import net.minecraft.world.entity.ai.behavior.VillageBoundRandomStroll;
import net.minecraft.world.entity.ai.behavior.VillagerCalmDown;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.ai.behavior.VillagerPanicTrigger;
import net.minecraft.world.entity.ai.behavior.WakeUp;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import net.minecraft.world.entity.ai.behavior.YieldJobSite;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class VillagerGoalPackages {
    private static final float STROLL_SPEED_MODIFIER = 0.4f;

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getCorePackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of(Pair.of(0, new Swim(0.8f)), Pair.of(0, new InteractWithDoor()), Pair.of(0, new LookAtTargetSink(45, 90)), Pair.of(0, new VillagerPanicTrigger()), Pair.of(0, new WakeUp()), Pair.of(0, new ReactToBell()), Pair.of(0, new SetRaidStatus()), Pair.of(0, new ValidateNearbyPoi(villagerProfession.getJobPoiType(), MemoryModuleType.JOB_SITE)), Pair.of(0, new ValidateNearbyPoi(villagerProfession.getJobPoiType(), MemoryModuleType.POTENTIAL_JOB_SITE)), Pair.of(1, new MoveToTargetSink()), Pair.of(2, new PoiCompetitorScan(villagerProfession)), Pair.of(3, new LookAndFollowTradingPlayerSink(f)), new Pair[]{Pair.of(5, new GoToWantedItem(f, false, 4)), Pair.of(6, new AcquirePoi(villagerProfession.getJobPoiType(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())), Pair.of(7, new GoToPotentialJobSite(f)), Pair.of(8, new YieldJobSite(f)), Pair.of(10, new AcquirePoi(PoiType.HOME, MemoryModuleType.HOME, false, Optional.of((byte)14))), Pair.of(10, new AcquirePoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))), Pair.of(10, new AssignProfessionFromJobSite()), Pair.of(10, new ResetProfession())});
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getWorkPackage(VillagerProfession villagerProfession, float f) {
        WorkAtPoi workAtPoi = villagerProfession == VillagerProfession.FARMER ? new WorkAtComposter() : new WorkAtPoi();
        return ImmutableList.of(VillagerGoalPackages.getMinimalLookBehavior(), Pair.of(5, new RunOne(ImmutableList.of(Pair.of(workAtPoi, 7), Pair.of(new StrollAroundPoi(MemoryModuleType.JOB_SITE, 0.4f, 4), 2), Pair.of(new StrollToPoi(MemoryModuleType.JOB_SITE, 0.4f, 1, 10), 5), Pair.of(new StrollToPoiList(MemoryModuleType.SECONDARY_JOB_SITE, f, 1, 6, MemoryModuleType.JOB_SITE), 5), Pair.of(new HarvestFarmland(), villagerProfession == VillagerProfession.FARMER ? 2 : 5), Pair.of(new UseBonemeal(), villagerProfession == VillagerProfession.FARMER ? 4 : 7)))), Pair.of(10, new ShowTradesToPlayer(400, 1600)), Pair.of(10, new SetLookAndInteract(EntityType.PLAYER, 4)), Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.JOB_SITE, f, 9, 100, 1200)), Pair.of(3, new GiveGiftToHero(100)), Pair.of(99, new UpdateActivityFromSchedule()));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getPlayPackage(float f) {
        return ImmutableList.of(Pair.of(0, new MoveToTargetSink(80, 120)), VillagerGoalPackages.getFullLookBehavior(), Pair.of(5, new PlayTagWithOtherKids()), Pair.of(5, new RunOne(ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryStatus.VALUE_ABSENT), ImmutableList.of(Pair.of(InteractWith.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, f, 2), 2), Pair.of(InteractWith.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, f, 2), 1), Pair.of(new VillageBoundRandomStroll(f), 1), Pair.of(new SetWalkTargetFromLookTarget(f, 2), 1), Pair.of(new JumpOnBed(f), 2), Pair.of(new DoNothing(20, 40), 2)))), Pair.of(99, new UpdateActivityFromSchedule()));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getRestPackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of(Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.HOME, f, 1, 150, 1200)), Pair.of(3, new ValidateNearbyPoi(PoiType.HOME, MemoryModuleType.HOME)), Pair.of(3, new SleepInBed()), Pair.of(5, new RunOne(ImmutableMap.of(MemoryModuleType.HOME, MemoryStatus.VALUE_ABSENT), ImmutableList.of(Pair.of(new SetClosestHomeAsWalkTarget(f), 1), Pair.of(new InsideBrownianWalk(f), 4), Pair.of(new GoToClosestVillage(f, 4), 2), Pair.of(new DoNothing(20, 40), 2)))), VillagerGoalPackages.getMinimalLookBehavior(), Pair.of(99, new UpdateActivityFromSchedule()));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getMeetPackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of(Pair.of(2, new RunOne(ImmutableList.of(Pair.of(new StrollAroundPoi(MemoryModuleType.MEETING_POINT, 0.4f, 40), 2), Pair.of(new SocializeAtBell(), 2)))), Pair.of(10, new ShowTradesToPlayer(400, 1600)), Pair.of(10, new SetLookAndInteract(EntityType.PLAYER, 4)), Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.MEETING_POINT, f, 6, 100, 200)), Pair.of(3, new GiveGiftToHero(100)), Pair.of(3, new ValidateNearbyPoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT)), Pair.of(3, new GateBehavior(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of(Pair.of(new TradeWithVillager(), 1)))), VillagerGoalPackages.getFullLookBehavior(), Pair.of(99, new UpdateActivityFromSchedule()));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getIdlePackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of(Pair.of(2, new RunOne(ImmutableList.of(Pair.of(InteractWith.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, f, 2), 2), Pair.of(new InteractWith<Villager, AgeableMob>(EntityType.VILLAGER, 8, AgeableMob::canBreed, AgeableMob::canBreed, MemoryModuleType.BREED_TARGET, f, 2), 1), Pair.of(InteractWith.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, f, 2), 1), Pair.of(new VillageBoundRandomStroll(f), 1), Pair.of(new SetWalkTargetFromLookTarget(f, 2), 1), Pair.of(new JumpOnBed(f), 1), Pair.of(new DoNothing(30, 60), 1)))), Pair.of(3, new GiveGiftToHero(100)), Pair.of(3, new SetLookAndInteract(EntityType.PLAYER, 4)), Pair.of(3, new ShowTradesToPlayer(400, 1600)), Pair.of(3, new GateBehavior(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of(Pair.of(new TradeWithVillager(), 1)))), Pair.of(3, new GateBehavior(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.BREED_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of(Pair.of(new VillagerMakeLove(), 1)))), VillagerGoalPackages.getFullLookBehavior(), Pair.of(99, new UpdateActivityFromSchedule()));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getPanicPackage(VillagerProfession villagerProfession, float f) {
        float g = f * 1.5f;
        return ImmutableList.of(Pair.of(0, new VillagerCalmDown()), Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_HOSTILE, g, 6, false)), Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.HURT_BY_ENTITY, g, 6, false)), Pair.of(3, new VillageBoundRandomStroll(g, 2, 2)), VillagerGoalPackages.getMinimalLookBehavior());
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getPreRaidPackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of(Pair.of(0, new RingBell()), Pair.of(0, new RunOne(ImmutableList.of(Pair.of(new SetWalkTargetFromBlockMemory(MemoryModuleType.MEETING_POINT, f * 1.5f, 2, 150, 200), 6), Pair.of(new VillageBoundRandomStroll(f * 1.5f), 2)))), VillagerGoalPackages.getMinimalLookBehavior(), Pair.of(99, new ResetRaidStatus()));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getRaidPackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of(Pair.of(0, new RunOne(ImmutableList.of(Pair.of(new GoOutsideToCelebrate(f), 5), Pair.of(new VictoryStroll(f * 1.1f), 2)))), Pair.of(0, new CelebrateVillagersSurvivedRaid(600, 600)), Pair.of(2, new LocateHidingPlaceDuringRaid(24, f * 1.4f)), VillagerGoalPackages.getMinimalLookBehavior(), Pair.of(99, new ResetRaidStatus()));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getHidePackage(VillagerProfession villagerProfession, float f) {
        int i = 2;
        return ImmutableList.of(Pair.of(0, new SetHiddenState(15, 3)), Pair.of(1, new LocateHidingPlace(32, f * 1.25f, 2)), VillagerGoalPackages.getMinimalLookBehavior());
    }

    private static Pair<Integer, Behavior<LivingEntity>> getFullLookBehavior() {
        return Pair.of(5, new RunOne(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.CAT, 8.0f), 8), Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, 8.0f), 2), Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0f), 2), Pair.of(new SetEntityLookTarget(MobCategory.CREATURE, 8.0f), 1), Pair.of(new SetEntityLookTarget(MobCategory.WATER_CREATURE, 8.0f), 1), Pair.of(new SetEntityLookTarget(MobCategory.UNDERGROUND_WATER_CREATURE, 8.0f), 1), Pair.of(new SetEntityLookTarget(MobCategory.WATER_AMBIENT, 8.0f), 1), Pair.of(new SetEntityLookTarget(MobCategory.MONSTER, 8.0f), 1), Pair.of(new DoNothing(30, 60), 2))));
    }

    private static Pair<Integer, Behavior<LivingEntity>> getMinimalLookBehavior() {
        return Pair.of(5, new RunOne(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, 8.0f), 2), Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0f), 2), Pair.of(new DoNothing(30, 60), 8))));
    }
}

