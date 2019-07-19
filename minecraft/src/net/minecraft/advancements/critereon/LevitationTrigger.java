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
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger implements CriterionTrigger<LevitationTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("levitation");
	private final Map<PlayerAdvancements, LevitationTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, LevitationTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> listener) {
		LevitationTrigger.PlayerListeners playerListeners = (LevitationTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new LevitationTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> listener) {
		LevitationTrigger.PlayerListeners playerListeners = (LevitationTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public LevitationTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("duration"));
		return new LevitationTrigger.TriggerInstance(distancePredicate, ints);
	}

	public void trigger(ServerPlayer serverPlayer, Vec3 vec3, int i) {
		LevitationTrigger.PlayerListeners playerListeners = (LevitationTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, vec3, i);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<LevitationTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<LevitationTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, Vec3 vec3, int i) {
			List<CriterionTrigger.Listener<LevitationTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, vec3, i)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<LevitationTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final DistancePredicate distance;
		private final MinMaxBounds.Ints duration;

		public TriggerInstance(DistancePredicate distancePredicate, MinMaxBounds.Ints ints) {
			super(LevitationTrigger.ID);
			this.distance = distancePredicate;
			this.duration = ints;
		}

		public static LevitationTrigger.TriggerInstance levitated(DistancePredicate distancePredicate) {
			return new LevitationTrigger.TriggerInstance(distancePredicate, MinMaxBounds.Ints.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, Vec3 vec3, int i) {
			return !this.distance.matches(vec3.x, vec3.y, vec3.z, serverPlayer.x, serverPlayer.y, serverPlayer.z) ? false : this.duration.matches(i);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("distance", this.distance.serializeToJson());
			jsonObject.add("duration", this.duration.serializeToJson());
			return jsonObject;
		}
	}
}
