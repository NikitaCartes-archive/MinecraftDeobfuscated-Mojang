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
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class LocationTrigger implements CriterionTrigger<LocationTrigger.TriggerInstance> {
	private final ResourceLocation id;
	private final Map<PlayerAdvancements, LocationTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, LocationTrigger.PlayerListeners>newHashMap();

	public LocationTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<LocationTrigger.TriggerInstance> listener) {
		LocationTrigger.PlayerListeners playerListeners = (LocationTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new LocationTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<LocationTrigger.TriggerInstance> listener) {
		LocationTrigger.PlayerListeners playerListeners = (LocationTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public LocationTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject);
		return new LocationTrigger.TriggerInstance(this.id, locationPredicate);
	}

	public void trigger(ServerPlayer serverPlayer) {
		LocationTrigger.PlayerListeners playerListeners = (LocationTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer.getLevel(), serverPlayer.x, serverPlayer.y, serverPlayer.z);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<LocationTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<LocationTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<LocationTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<LocationTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerLevel serverLevel, double d, double e, double f) {
			List<CriterionTrigger.Listener<LocationTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<LocationTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverLevel, d, e, f)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<LocationTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<LocationTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final LocationPredicate location;

		public TriggerInstance(ResourceLocation resourceLocation, LocationPredicate locationPredicate) {
			super(resourceLocation);
			this.location = locationPredicate;
		}

		public static LocationTrigger.TriggerInstance located(LocationPredicate locationPredicate) {
			return new LocationTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, locationPredicate);
		}

		public static LocationTrigger.TriggerInstance sleptInBed() {
			return new LocationTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, LocationPredicate.ANY);
		}

		public static LocationTrigger.TriggerInstance raidWon() {
			return new LocationTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, LocationPredicate.ANY);
		}

		public boolean matches(ServerLevel serverLevel, double d, double e, double f) {
			return this.location.matches(serverLevel, d, e, f);
		}

		@Override
		public JsonElement serializeToJson() {
			return this.location.serializeToJson();
		}
	}
}
