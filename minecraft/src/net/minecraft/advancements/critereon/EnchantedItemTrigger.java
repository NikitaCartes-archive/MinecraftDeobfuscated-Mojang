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

public class EnchantedItemTrigger implements CriterionTrigger<EnchantedItemTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("enchanted_item");
	private final Map<PlayerAdvancements, EnchantedItemTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, EnchantedItemTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> listener) {
		EnchantedItemTrigger.PlayerListeners playerListeners = (EnchantedItemTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new EnchantedItemTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> listener) {
		EnchantedItemTrigger.PlayerListeners playerListeners = (EnchantedItemTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public EnchantedItemTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("levels"));
		return new EnchantedItemTrigger.TriggerInstance(itemPredicate, ints);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, int i) {
		EnchantedItemTrigger.PlayerListeners playerListeners = (EnchantedItemTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(itemStack, i);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ItemStack itemStack, int i) {
			List<CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(itemStack, i)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;
		private final MinMaxBounds.Ints levels;

		public TriggerInstance(ItemPredicate itemPredicate, MinMaxBounds.Ints ints) {
			super(EnchantedItemTrigger.ID);
			this.item = itemPredicate;
			this.levels = ints;
		}

		public static EnchantedItemTrigger.TriggerInstance enchantedItem() {
			return new EnchantedItemTrigger.TriggerInstance(ItemPredicate.ANY, MinMaxBounds.Ints.ANY);
		}

		public boolean matches(ItemStack itemStack, int i) {
			return !this.item.matches(itemStack) ? false : this.levels.matches(i);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("item", this.item.serializeToJson());
			jsonObject.add("levels", this.levels.serializeToJson());
			return jsonObject;
		}
	}
}
