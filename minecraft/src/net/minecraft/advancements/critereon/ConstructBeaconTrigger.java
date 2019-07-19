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
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class ConstructBeaconTrigger implements CriterionTrigger<ConstructBeaconTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("construct_beacon");
	private final Map<PlayerAdvancements, ConstructBeaconTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, ConstructBeaconTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> listener) {
		ConstructBeaconTrigger.PlayerListeners playerListeners = (ConstructBeaconTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new ConstructBeaconTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> listener) {
		ConstructBeaconTrigger.PlayerListeners playerListeners = (ConstructBeaconTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public ConstructBeaconTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("level"));
		return new ConstructBeaconTrigger.TriggerInstance(ints);
	}

	public void trigger(ServerPlayer serverPlayer, BeaconBlockEntity beaconBlockEntity) {
		ConstructBeaconTrigger.PlayerListeners playerListeners = (ConstructBeaconTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(beaconBlockEntity);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(BeaconBlockEntity beaconBlockEntity) {
			List<CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(beaconBlockEntity)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Ints level;

		public TriggerInstance(MinMaxBounds.Ints ints) {
			super(ConstructBeaconTrigger.ID);
			this.level = ints;
		}

		public static ConstructBeaconTrigger.TriggerInstance constructedBeacon(MinMaxBounds.Ints ints) {
			return new ConstructBeaconTrigger.TriggerInstance(ints);
		}

		public boolean matches(BeaconBlockEntity beaconBlockEntity) {
			return this.level.matches(beaconBlockEntity.getLevels());
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("level", this.level.serializeToJson());
			return jsonObject;
		}
	}
}
