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

public class ConsumeItemTrigger implements CriterionTrigger<ConsumeItemTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("consume_item");
	private final Map<PlayerAdvancements, ConsumeItemTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, ConsumeItemTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ConsumeItemTrigger.TriggerInstance> listener) {
		ConsumeItemTrigger.PlayerListeners playerListeners = (ConsumeItemTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new ConsumeItemTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ConsumeItemTrigger.TriggerInstance> listener) {
		ConsumeItemTrigger.PlayerListeners playerListeners = (ConsumeItemTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public ConsumeItemTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		return new ConsumeItemTrigger.TriggerInstance(ItemPredicate.fromJson(jsonObject.get("item")));
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		ConsumeItemTrigger.PlayerListeners playerListeners = (ConsumeItemTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(itemStack);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<ConsumeItemTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<ConsumeItemTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<ConsumeItemTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<ConsumeItemTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ItemStack itemStack) {
			List<CriterionTrigger.Listener<ConsumeItemTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<ConsumeItemTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(itemStack)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<ConsumeItemTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<ConsumeItemTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;

		public TriggerInstance(ItemPredicate itemPredicate) {
			super(ConsumeItemTrigger.ID);
			this.item = itemPredicate;
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem() {
			return new ConsumeItemTrigger.TriggerInstance(ItemPredicate.ANY);
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem(ItemLike itemLike) {
			return new ConsumeItemTrigger.TriggerInstance(
				new ItemPredicate(
					null, itemLike.asItem(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.ANY
				)
			);
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
