package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
	public InventoryChangeTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "slots", new JsonObject());
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject2.get("occupied"));
		MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject2.get("full"));
		MinMaxBounds.Ints ints3 = MinMaxBounds.Ints.fromJson(jsonObject2.get("empty"));
		List<ItemPredicate> list = ItemPredicate.fromJsonArray(jsonObject.get("items"));
		return new InventoryChangeTrigger.TriggerInstance(optional, ints, ints2, ints3, list);
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

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Ints slotsOccupied;
		private final MinMaxBounds.Ints slotsFull;
		private final MinMaxBounds.Ints slotsEmpty;
		private final List<ItemPredicate> predicates;

		public TriggerInstance(
			Optional<ContextAwarePredicate> optional, MinMaxBounds.Ints ints, MinMaxBounds.Ints ints2, MinMaxBounds.Ints ints3, List<ItemPredicate> list
		) {
			super(optional);
			this.slotsOccupied = ints;
			this.slotsFull = ints2;
			this.slotsEmpty = ints3;
			this.predicates = list;
		}

		public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate.Builder... builders) {
			return hasItems((ItemPredicate[])Stream.of(builders).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
		}

		public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate... itemPredicates) {
			return CriteriaTriggers.INVENTORY_CHANGED
				.createCriterion(
					new InventoryChangeTrigger.TriggerInstance(Optional.empty(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, List.of(itemPredicates))
				);
		}

		public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemLike... itemLikes) {
			ItemPredicate[] itemPredicates = new ItemPredicate[itemLikes.length];

			for (int i = 0; i < itemLikes.length; i++) {
				itemPredicates[i] = new ItemPredicate(
					Optional.empty(),
					Optional.of(HolderSet.direct(itemLikes[i].asItem().builtInRegistryHolder())),
					MinMaxBounds.Ints.ANY,
					MinMaxBounds.Ints.ANY,
					List.of(),
					List.of(),
					Optional.empty(),
					Optional.empty()
				);
			}

			return hasItems(itemPredicates);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			if (!this.slotsOccupied.isAny() || !this.slotsFull.isAny() || !this.slotsEmpty.isAny()) {
				JsonObject jsonObject2 = new JsonObject();
				jsonObject2.add("occupied", this.slotsOccupied.serializeToJson());
				jsonObject2.add("full", this.slotsFull.serializeToJson());
				jsonObject2.add("empty", this.slotsEmpty.serializeToJson());
				jsonObject.add("slots", jsonObject2);
			}

			if (!this.predicates.isEmpty()) {
				jsonObject.add("items", ItemPredicate.serializeToJsonArray(this.predicates));
			}

			return jsonObject;
		}

		public boolean matches(Inventory inventory, ItemStack itemStack, int i, int j, int k) {
			if (!this.slotsFull.matches(i)) {
				return false;
			} else if (!this.slotsEmpty.matches(j)) {
				return false;
			} else if (!this.slotsOccupied.matches(k)) {
				return false;
			} else if (this.predicates.isEmpty()) {
				return true;
			} else if (this.predicates.size() != 1) {
				List<ItemPredicate> list = new ObjectArrayList<>(this.predicates);
				int l = inventory.getContainerSize();

				for (int m = 0; m < l; m++) {
					if (list.isEmpty()) {
						return true;
					}

					ItemStack itemStack2 = inventory.getItem(m);
					if (!itemStack2.isEmpty()) {
						list.removeIf(itemPredicate -> itemPredicate.matches(itemStack2));
					}
				}

				return list.isEmpty();
			} else {
				return !itemStack.isEmpty() && ((ItemPredicate)this.predicates.get(0)).matches(itemStack);
			}
		}
	}
}
