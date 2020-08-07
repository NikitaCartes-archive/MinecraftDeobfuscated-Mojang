package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TradeWithVillager extends Behavior<Villager> {
	private Set<Item> trades = ImmutableSet.of();

	public TradeWithVillager() {
		super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		return BehaviorUtils.targetIsValid(villager.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
	}

	protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
		return this.checkExtraStartConditions(serverLevel, villager);
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		Villager villager2 = (Villager)villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
		BehaviorUtils.lockGazeAndWalkToEachOther(villager, villager2, 0.5F);
		this.trades = figureOutWhatIAmWillingToTrade(villager, villager2);
	}

	protected void tick(ServerLevel serverLevel, Villager villager, long l) {
		Villager villager2 = (Villager)villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
		if (!(villager.distanceToSqr(villager2) > 5.0)) {
			BehaviorUtils.lockGazeAndWalkToEachOther(villager, villager2, 0.5F);
			villager.gossip(serverLevel, villager2, l);
			if (villager.hasExcessFood() && (villager.getVillagerData().getProfession() == VillagerProfession.FARMER || villager2.wantsMoreFood())) {
				throwHalfStack(villager, Villager.FOOD_POINTS.keySet(), villager2);
			}

			if (villager2.getVillagerData().getProfession() == VillagerProfession.FARMER
				&& villager.getInventory().countItem(Items.WHEAT) > Items.WHEAT.getMaxStackSize() / 2) {
				throwHalfStack(villager, ImmutableSet.of(Items.WHEAT), villager2);
			}

			if (!this.trades.isEmpty() && villager.getInventory().hasAnyOf(this.trades)) {
				throwHalfStack(villager, this.trades, villager2);
			}
		}
	}

	protected void stop(ServerLevel serverLevel, Villager villager, long l) {
		villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
	}

	private static Set<Item> figureOutWhatIAmWillingToTrade(Villager villager, Villager villager2) {
		ImmutableSet<Item> immutableSet = villager2.getVillagerData().getProfession().getRequestedItems();
		ImmutableSet<Item> immutableSet2 = villager.getVillagerData().getProfession().getRequestedItems();
		return (Set<Item>)immutableSet.stream().filter(item -> !immutableSet2.contains(item)).collect(Collectors.toSet());
	}

	private static void throwHalfStack(Villager villager, Set<Item> set, LivingEntity livingEntity) {
		SimpleContainer simpleContainer = villager.getInventory();
		ItemStack itemStack = ItemStack.EMPTY;
		int i = 0;

		while (i < simpleContainer.getContainerSize()) {
			ItemStack itemStack2;
			Item item;
			int j;
			label28: {
				itemStack2 = simpleContainer.getItem(i);
				if (!itemStack2.isEmpty()) {
					item = itemStack2.getItem();
					if (set.contains(item)) {
						if (itemStack2.getCount() > itemStack2.getMaxStackSize() / 2) {
							j = itemStack2.getCount() / 2;
							break label28;
						}

						if (itemStack2.getCount() > 24) {
							j = itemStack2.getCount() - 24;
							break label28;
						}
					}
				}

				i++;
				continue;
			}

			itemStack2.shrink(j);
			itemStack = new ItemStack(item, j);
			break;
		}

		if (!itemStack.isEmpty()) {
			BehaviorUtils.throwItem(villager, itemStack, livingEntity.position());
		}
	}
}
