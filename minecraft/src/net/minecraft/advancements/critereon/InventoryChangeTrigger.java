package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
	@Override
	public Codec<InventoryChangeTrigger.TriggerInstance> codec() {
		return InventoryChangeTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Inventory inventory, ItemStack itemStack) {
		int i = 0;
		int j = 0;
		int k = 0;

		for (int l = 0; l < inventory.getContainerSize(); l++) {
			ItemStack itemStack2 = inventory.getItem(l);
			if (itemStack2.isEmpty()) {
				j++;
			} else {
				k++;
				if (itemStack2.getCount() >= itemStack2.getMaxStackSize()) {
					i++;
				}
			}
		}

		this.trigger(serverPlayer, inventory, itemStack, i, j, k);
	}

	private void trigger(ServerPlayer serverPlayer, Inventory inventory, ItemStack itemStack, int i, int j, int k) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(inventory, itemStack, i, j, k));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, InventoryChangeTrigger.TriggerInstance.Slots slots, List<ItemPredicate> items)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<InventoryChangeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(InventoryChangeTrigger.TriggerInstance::player),
						InventoryChangeTrigger.TriggerInstance.Slots.CODEC
							.optionalFieldOf("slots", InventoryChangeTrigger.TriggerInstance.Slots.ANY)
							.forGetter(InventoryChangeTrigger.TriggerInstance::slots),
						ItemPredicate.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(InventoryChangeTrigger.TriggerInstance::items)
					)
					.apply(instance, InventoryChangeTrigger.TriggerInstance::new)
		);

		public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate.Builder... builders) {
			return hasItems((ItemPredicate[])Stream.of(builders).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
		}

		public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate... itemPredicates) {
			return CriteriaTriggers.INVENTORY_CHANGED
				.createCriterion(new InventoryChangeTrigger.TriggerInstance(Optional.empty(), InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of(itemPredicates)));
		}

		public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemLike... itemLikes) {
			ItemPredicate[] itemPredicates = new ItemPredicate[itemLikes.length];

			for (int i = 0; i < itemLikes.length; i++) {
				itemPredicates[i] = new ItemPredicate(
					Optional.of(HolderSet.direct(itemLikes[i].asItem().builtInRegistryHolder())), MinMaxBounds.Ints.ANY, DataComponentPredicate.EMPTY, Map.of()
				);
			}

			return hasItems(itemPredicates);
		}

		public boolean matches(Inventory inventory, ItemStack itemStack, int i, int j, int k) {
			if (!this.slots.matches(i, j, k)) {
				return false;
			} else if (this.items.isEmpty()) {
				return true;
			} else if (this.items.size() != 1) {
				List<ItemPredicate> list = new ObjectArrayList<>(this.items);
				int l = inventory.getContainerSize();

				for (int m = 0; m < l; m++) {
					if (list.isEmpty()) {
						return true;
					}

					ItemStack itemStack2 = inventory.getItem(m);
					if (!itemStack2.isEmpty()) {
						list.removeIf(itemPredicate -> itemPredicate.test(itemStack2));
					}
				}

				return list.isEmpty();
			} else {
				return !itemStack.isEmpty() && ((ItemPredicate)this.items.get(0)).test(itemStack);
			}
		}

		public static record Slots(MinMaxBounds.Ints occupied, MinMaxBounds.Ints full, MinMaxBounds.Ints empty) {
			public static final Codec<InventoryChangeTrigger.TriggerInstance.Slots> CODEC = RecordCodecBuilder.create(
				instance -> instance.group(
							MinMaxBounds.Ints.CODEC.optionalFieldOf("occupied", MinMaxBounds.Ints.ANY).forGetter(InventoryChangeTrigger.TriggerInstance.Slots::occupied),
							MinMaxBounds.Ints.CODEC.optionalFieldOf("full", MinMaxBounds.Ints.ANY).forGetter(InventoryChangeTrigger.TriggerInstance.Slots::full),
							MinMaxBounds.Ints.CODEC.optionalFieldOf("empty", MinMaxBounds.Ints.ANY).forGetter(InventoryChangeTrigger.TriggerInstance.Slots::empty)
						)
						.apply(instance, InventoryChangeTrigger.TriggerInstance.Slots::new)
			);
			public static final InventoryChangeTrigger.TriggerInstance.Slots ANY = new InventoryChangeTrigger.TriggerInstance.Slots(
				MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY
			);

			public boolean matches(int i, int j, int k) {
				if (!this.full.matches(i)) {
					return false;
				} else {
					return !this.empty.matches(j) ? false : this.occupied.matches(k);
				}
			}
		}
	}
}
