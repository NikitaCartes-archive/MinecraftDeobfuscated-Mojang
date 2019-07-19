package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.fishing.FishingHook;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class FishingRodHookedTrigger implements CriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("fishing_rod_hooked");
	private final Map<PlayerAdvancements, FishingRodHookedTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, FishingRodHookedTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> listener) {
		FishingRodHookedTrigger.PlayerListeners playerListeners = (FishingRodHookedTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new FishingRodHookedTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> listener) {
		FishingRodHookedTrigger.PlayerListeners playerListeners = (FishingRodHookedTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public FishingRodHookedTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("rod"));
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("entity"));
		ItemPredicate itemPredicate2 = ItemPredicate.fromJson(jsonObject.get("item"));
		return new FishingRodHookedTrigger.TriggerInstance(itemPredicate, entityPredicate, itemPredicate2);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, FishingHook fishingHook, Collection<ItemStack> collection) {
		FishingRodHookedTrigger.PlayerListeners playerListeners = (FishingRodHookedTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, itemStack, fishingHook, collection);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, FishingHook fishingHook, Collection<ItemStack> collection) {
			List<CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, itemStack, fishingHook, collection)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<FishingRodHookedTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate rod;
		private final EntityPredicate entity;
		private final ItemPredicate item;

		public TriggerInstance(ItemPredicate itemPredicate, EntityPredicate entityPredicate, ItemPredicate itemPredicate2) {
			super(FishingRodHookedTrigger.ID);
			this.rod = itemPredicate;
			this.entity = entityPredicate;
			this.item = itemPredicate2;
		}

		public static FishingRodHookedTrigger.TriggerInstance fishedItem(ItemPredicate itemPredicate, EntityPredicate entityPredicate, ItemPredicate itemPredicate2) {
			return new FishingRodHookedTrigger.TriggerInstance(itemPredicate, entityPredicate, itemPredicate2);
		}

		public boolean matches(ServerPlayer serverPlayer, ItemStack itemStack, FishingHook fishingHook, Collection<ItemStack> collection) {
			if (!this.rod.matches(itemStack)) {
				return false;
			} else if (!this.entity.matches(serverPlayer, fishingHook.hookedIn)) {
				return false;
			} else {
				if (this.item != ItemPredicate.ANY) {
					boolean bl = false;
					if (fishingHook.hookedIn instanceof ItemEntity) {
						ItemEntity itemEntity = (ItemEntity)fishingHook.hookedIn;
						if (this.item.matches(itemEntity.getItem())) {
							bl = true;
						}
					}

					for (ItemStack itemStack2 : collection) {
						if (this.item.matches(itemStack2)) {
							bl = true;
							break;
						}
					}

					if (!bl) {
						return false;
					}
				}

				return true;
			}
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("rod", this.rod.serializeToJson());
			jsonObject.add("entity", this.entity.serializeToJson());
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}
