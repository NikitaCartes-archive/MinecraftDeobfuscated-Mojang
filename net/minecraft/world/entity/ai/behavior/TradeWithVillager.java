/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TradeWithVillager
extends Behavior<Villager> {
    private Set<Item> trades = ImmutableSet.of();

    public TradeWithVillager() {
        super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
        return BehaviorUtils.targetIsValid(villager.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
        return this.checkExtraStartConditions(serverLevel, villager);
    }

    @Override
    protected void start(ServerLevel serverLevel, Villager villager, long l) {
        Villager villager2 = (Villager)villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        BehaviorUtils.lockGazeAndWalkToEachOther(villager, villager2);
        this.trades = TradeWithVillager.figureOutWhatIAmWillingToTrade(villager, villager2);
    }

    @Override
    protected void tick(ServerLevel serverLevel, Villager villager, long l) {
        Villager villager2 = (Villager)villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        if (villager.distanceToSqr(villager2) > 5.0) {
            return;
        }
        BehaviorUtils.lockGazeAndWalkToEachOther(villager, villager2);
        villager.gossip(villager2, l);
        if (villager.hasExcessFood() && (villager.getVillagerData().getProfession() == VillagerProfession.FARMER || villager2.wantsMoreFood())) {
            TradeWithVillager.throwHalfStack(villager, Villager.FOOD_POINTS.keySet(), villager2);
        }
        if (!this.trades.isEmpty() && villager.getInventory().hasAnyOf(this.trades)) {
            TradeWithVillager.throwHalfStack(villager, this.trades, villager2);
        }
    }

    @Override
    protected void stop(ServerLevel serverLevel, Villager villager, long l) {
        villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }

    private static Set<Item> figureOutWhatIAmWillingToTrade(Villager villager, Villager villager2) {
        ImmutableSet<Item> immutableSet = villager2.getVillagerData().getProfession().getRequestedItems();
        ImmutableSet<Item> immutableSet2 = villager.getVillagerData().getProfession().getRequestedItems();
        return immutableSet.stream().filter(item -> !immutableSet2.contains(item)).collect(Collectors.toSet());
    }

    private static void throwHalfStack(Villager villager, Set<Item> set, LivingEntity livingEntity) {
        SimpleContainer simpleContainer = villager.getInventory();
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < simpleContainer.getContainerSize(); ++i) {
            int j;
            Item item;
            ItemStack itemStack2 = simpleContainer.getItem(i);
            if (itemStack2.isEmpty() || !set.contains(item = itemStack2.getItem())) continue;
            if (itemStack2.getCount() > itemStack2.getMaxStackSize() / 2) {
                j = itemStack2.getCount() / 2;
            } else {
                if (itemStack2.getCount() <= 24) continue;
                j = itemStack2.getCount() - 24;
            }
            itemStack2.shrink(j);
            itemStack = new ItemStack(item, j);
            break;
        }
        if (!itemStack.isEmpty()) {
            BehaviorUtils.throwItem(villager, itemStack, livingEntity);
        }
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}

