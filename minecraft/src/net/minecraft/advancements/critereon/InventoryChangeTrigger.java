package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger implements CriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("inventory_changed");
	private final Map<PlayerAdvancements, InventoryChangeTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, InventoryChangeTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance> listener) {
		InventoryChangeTrigger.PlayerListeners playerListeners = (InventoryChangeTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new InventoryChangeTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance> listener) {
		InventoryChangeTrigger.PlayerListeners playerListeners = (InventoryChangeTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners != null) {
			playerListeners.removeListener(listener);
			if (playerListeners.isEmpty()) {
				this.players.remove(playerAdvancements);
			}
		}
	}

	@Override
	public void removePlayerListeners(PlayerAdvancements playerAdvancements) {
		this.players.remove(playerAdvancements);
	}

	public InventoryChangeTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "slots", new JsonObject());
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject2.get("occupied"));
		MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject2.get("full"));
		MinMaxBounds.Ints ints3 = MinMaxBounds.Ints.fromJson(jsonObject2.get("empty"));
		ItemPredicate[] itemPredicates = ItemPredicate.fromJsonArray(jsonObject.get("items"));
		return new InventoryChangeTrigger.TriggerInstance(ints, ints2, ints3, itemPredicates);
	}

	public void trigger(ServerPlayer serverPlayer, Inventory inventory) {
		InventoryChangeTrigger.PlayerListeners playerListeners = (InventoryChangeTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(inventory);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(Inventory inventory) {
			List<CriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(inventory)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Ints slotsOccupied;
		private final MinMaxBounds.Ints slotsFull;
		private final MinMaxBounds.Ints slotsEmpty;
		private final ItemPredicate[] predicates;

		public TriggerInstance(MinMaxBounds.Ints ints, MinMaxBounds.Ints ints2, MinMaxBounds.Ints ints3, ItemPredicate[] itemPredicates) {
			super(InventoryChangeTrigger.ID);
			this.slotsOccupied = ints;
			this.slotsFull = ints2;
			this.slotsEmpty = ints3;
			this.predicates = itemPredicates;
		}

		public static InventoryChangeTrigger.TriggerInstance hasItem(ItemPredicate... itemPredicates) {
			return new InventoryChangeTrigger.TriggerInstance(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, itemPredicates);
		}

		public static InventoryChangeTrigger.TriggerInstance hasItem(ItemLike... itemLikes) {
			ItemPredicate[] itemPredicates = new ItemPredicate[itemLikes.length];

			for (int i = 0; i < itemLikes.length; i++) {
				itemPredicates[i] = new ItemPredicate(
					null, itemLikes[i].asItem(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, new EnchantmentPredicate[0], null, NbtPredicate.ANY
				);
			}

			return hasItem(itemPredicates);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
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

		public boolean matches(Inventory inventory) {
			int i = 0;
			int j = 0;
			int k = 0;
			List<ItemPredicate> list = Lists.<ItemPredicate>newArrayList(this.predicates);

			for (int l = 0; l < inventory.getContainerSize(); l++) {
				ItemStack itemStack = inventory.getItem(l);
				if (itemStack.isEmpty()) {
					j++;
				} else {
					k++;
					if (itemStack.getCount() >= itemStack.getMaxStackSize()) {
						i++;
					}

					Iterator<ItemPredicate> iterator = list.iterator();

					while (iterator.hasNext()) {
						ItemPredicate itemPredicate = (ItemPredicate)iterator.next();
						if (itemPredicate.matches(itemStack)) {
							iterator.remove();
						}
					}
				}
			}

			if (!this.slotsFull.matches(i)) {
				return false;
			} else if (!this.slotsEmpty.matches(j)) {
				return false;
			} else {
				return !this.slotsOccupied.matches(k) ? false : list.isEmpty();
			}
		}
	}
}
