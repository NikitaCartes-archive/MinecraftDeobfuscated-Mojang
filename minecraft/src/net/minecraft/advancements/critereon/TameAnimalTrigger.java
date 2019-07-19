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
import net.minecraft.world.entity.animal.Animal;

public class TameAnimalTrigger implements CriterionTrigger<TameAnimalTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("tame_animal");
	private final Map<PlayerAdvancements, TameAnimalTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, TameAnimalTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TameAnimalTrigger.TriggerInstance> listener) {
		TameAnimalTrigger.PlayerListeners playerListeners = (TameAnimalTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new TameAnimalTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TameAnimalTrigger.TriggerInstance> listener) {
		TameAnimalTrigger.PlayerListeners playerListeners = (TameAnimalTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public TameAnimalTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("entity"));
		return new TameAnimalTrigger.TriggerInstance(entityPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, Animal animal) {
		TameAnimalTrigger.PlayerListeners playerListeners = (TameAnimalTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, animal);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<TameAnimalTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<TameAnimalTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<TameAnimalTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<TameAnimalTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, Animal animal) {
			List<CriterionTrigger.Listener<TameAnimalTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<TameAnimalTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, animal)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<TameAnimalTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<TameAnimalTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate entity;

		public TriggerInstance(EntityPredicate entityPredicate) {
			super(TameAnimalTrigger.ID);
			this.entity = entityPredicate;
		}

		public static TameAnimalTrigger.TriggerInstance tamedAnimal() {
			return new TameAnimalTrigger.TriggerInstance(EntityPredicate.ANY);
		}

		public static TameAnimalTrigger.TriggerInstance tamedAnimal(EntityPredicate entityPredicate) {
			return new TameAnimalTrigger.TriggerInstance(entityPredicate);
		}

		public boolean matches(ServerPlayer serverPlayer, Animal animal) {
			return this.entity.matches(serverPlayer, animal);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("entity", this.entity.serializeToJson());
			return jsonObject;
		}
	}
}
