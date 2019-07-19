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
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;

public class TradeTrigger implements CriterionTrigger<TradeTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("villager_trade");
	private final Map<PlayerAdvancements, TradeTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, TradeTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TradeTrigger.TriggerInstance> listener) {
		TradeTrigger.PlayerListeners playerListeners = (TradeTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new TradeTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TradeTrigger.TriggerInstance> listener) {
		TradeTrigger.PlayerListeners playerListeners = (TradeTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public TradeTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("villager"));
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new TradeTrigger.TriggerInstance(entityPredicate, itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, AbstractVillager abstractVillager, ItemStack itemStack) {
		TradeTrigger.PlayerListeners playerListeners = (TradeTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, abstractVillager, itemStack);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<TradeTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<TradeTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<TradeTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<TradeTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, AbstractVillager abstractVillager, ItemStack itemStack) {
			List<CriterionTrigger.Listener<TradeTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<TradeTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, abstractVillager, itemStack)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<TradeTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<TradeTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate villager;
		private final ItemPredicate item;

		public TriggerInstance(EntityPredicate entityPredicate, ItemPredicate itemPredicate) {
			super(TradeTrigger.ID);
			this.villager = entityPredicate;
			this.item = itemPredicate;
		}

		public static TradeTrigger.TriggerInstance tradedWithVillager() {
			return new TradeTrigger.TriggerInstance(EntityPredicate.ANY, ItemPredicate.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, AbstractVillager abstractVillager, ItemStack itemStack) {
			return !this.villager.matches(serverPlayer, abstractVillager) ? false : this.item.matches(itemStack);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("item", this.item.serializeToJson());
			jsonObject.add("villager", this.villager.serializeToJson());
			return jsonObject;
		}
	}
}
