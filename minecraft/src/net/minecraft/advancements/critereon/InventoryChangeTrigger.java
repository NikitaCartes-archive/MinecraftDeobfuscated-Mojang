package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("inventory_changed");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public InventoryChangeTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "slots", new JsonObject());
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject2.get("occupied"));
		MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject2.get("full"));
		MinMaxBounds.Ints ints3 = MinMaxBounds.Ints.fromJson(jsonObject2.get("empty"));
		ItemPredicate[] itemPredicates = ItemPredicate.fromJsonArray(jsonObject.get("items"));
		return new InventoryChangeTrigger.TriggerInstance(composite, ints, ints2, ints3, itemPredicates);
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
		private final ItemPredicate[] predicates;

		public TriggerInstance(
			EntityPredicate.Composite composite, MinMaxBounds.Ints ints, MinMaxBounds.Ints ints2, MinMaxBounds.Ints ints3, ItemPredicate[] itemPredicates
		) {
			super(InventoryChangeTrigger.ID, composite);
			this.slotsOccupied = ints;
			this.slotsFull = ints2;
			this.slotsEmpty = ints3;
			this.predicates = itemPredicates;
		}

		public static InventoryChangeTrigger.TriggerInstance hasItems(ItemPredicate... itemPredicates) {
			return new InventoryChangeTrigger.TriggerInstance(
				EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, itemPredicates
			);
		}

		public static InventoryChangeTrigger.TriggerInstance hasItems(ItemLike... itemLikes) {
			ItemPredicate[] itemPredicates = new ItemPredicate[itemLikes.length];

			for (int i = 0; i < itemLikes.length; i++) {
				itemPredicates[i] = new ItemPredicate(
					null,
					ImmutableSet.of(itemLikes[i].asItem()),
					MinMaxBounds.Ints.ANY,
					MinMaxBounds.Ints.ANY,
					EnchantmentPredicate.NONE,
					EnchantmentPredicate.NONE,
					null,
					NbtPredicate.ANY
				);
			}

			return hasItems(itemPredicates);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			if (!this.slotsOccupied.isAny() || !this.slotsFull.isAny() || !this.slotsEmpty.isAny()) {
				JsonObject jsonObject2 = new JsonObject();
				jsonObject2.add("occupied", this.slotsOccupied.serializeToJson());
				jsonObject2.add("full", this.slotsFull.serializeToJson());
				jsonObject2.add("empty", this.slotsEmpty.serializeToJson());
				jsonObject.add("slots", jsonObject2);
			}

			if (this.predicates.length > 0) {
				JsonArray jsonArray = new JsonArray();

				for (ItemPredicate itemPredicate : this.predicates) {
					jsonArray.add(itemPredicate.serializeToJson());
				}

				jsonObject.add("items", jsonArray);
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
			} else {
				int l = this.predicates.length;
				if (l == 0) {
					return true;
				} else if (l != 1) {
					List<ItemPredicate> list = new ObjectArrayList<>(this.predicates);
					int m = inventory.getContainerSize();

					for (int n = 0; n < m; n++) {
						if (list.isEmpty()) {
							return true;
						}

						ItemStack itemStack2 = inventory.getItem(n);
						if (!itemStack2.isEmpty()) {
							list.removeIf(itemPredicate -> itemPredicate.matches(itemStack2));
						}
					}

					return list.isEmpty();
				} else {
					return !itemStack.isEmpty() && this.predicates[0].matches(itemStack);
				}
			}
		}
	}
}
