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
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger implements CriterionTrigger<UsedTotemTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("used_totem");
	private final Map<PlayerAdvancements, UsedTotemTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, UsedTotemTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> listener) {
		UsedTotemTrigger.PlayerListeners playerListeners = (UsedTotemTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new UsedTotemTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> listener) {
		UsedTotemTrigger.PlayerListeners playerListeners = (UsedTotemTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public UsedTotemTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new UsedTotemTrigger.TriggerInstance(itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		UsedTotemTrigger.PlayerListeners playerListeners = (UsedTotemTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(itemStack);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ItemStack itemStack) {
			List<CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(itemStack)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;

		public TriggerInstance(ItemPredicate itemPredicate) {
			super(UsedTotemTrigger.ID);
			this.item = itemPredicate;
		}

		public static UsedTotemTrigger.TriggerInstance usedTotem(ItemLike itemLike) {
			return new UsedTotemTrigger.TriggerInstance(ItemPredicate.Builder.item().of(itemLike).build());
		}

		public boolean matches(ItemStack itemStack) {
			return this.item.matches(itemStack);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}
