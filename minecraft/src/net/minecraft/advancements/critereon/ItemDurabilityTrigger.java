package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger implements CriterionTrigger<ItemDurabilityTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("item_durability_changed");
	private final Map<PlayerAdvancements, ItemDurabilityTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, ItemDurabilityTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> listener) {
		ItemDurabilityTrigger.PlayerListeners playerListeners = (ItemDurabilityTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new ItemDurabilityTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> listener) {
		ItemDurabilityTrigger.PlayerListeners playerListeners = (ItemDurabilityTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public ItemDurabilityTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("durability"));
		MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject.get("delta"));
		return new ItemDurabilityTrigger.TriggerInstance(itemPredicate, ints, ints2);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, int i) {
		ItemDurabilityTrigger.PlayerListeners playerListeners = (ItemDurabilityTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(itemStack, i);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ItemStack itemStack, int i) {
			List<CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(itemStack, i)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;
		private final MinMaxBounds.Ints durability;
		private final MinMaxBounds.Ints delta;

		public TriggerInstance(ItemPredicate itemPredicate, MinMaxBounds.Ints ints, MinMaxBounds.Ints ints2) {
			super(ItemDurabilityTrigger.ID);
			this.item = itemPredicate;
			this.durability = ints;
			this.delta = ints2;
		}

		public static ItemDurabilityTrigger.TriggerInstance changedDurability(ItemPredicate itemPredicate, MinMaxBounds.Ints ints) {
			return new ItemDurabilityTrigger.TriggerInstance(itemPredicate, ints, MinMaxBounds.Ints.ANY);
		}

		public boolean matches(ItemStack itemStack, int i) {
			if (!this.item.matches(itemStack)) {
				return false;
			} else {
				return !this.durability.matches(itemStack.getMaxDamage() - i) ? false : this.delta.matches(itemStack.getDamageValue() - i);
			}
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("item", this.item.serializeToJson());
			jsonObject.add("durability", this.durability.serializeToJson());
			jsonObject.add("delta", this.delta.serializeToJson());
			return jsonObject;
		}
	}
}
