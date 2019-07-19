package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

public class TickTrigger implements CriterionTrigger<TickTrigger.TriggerInstance> {
	public static final ResourceLocation ID = new ResourceLocation("tick");
	private final Map<PlayerAdvancements, TickTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, TickTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TickTrigger.TriggerInstance> listener) {
		TickTrigger.PlayerListeners playerListeners = (TickTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new TickTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TickTrigger.TriggerInstance> listener) {
		TickTrigger.PlayerListeners playerListeners = (TickTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public TickTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		return new TickTrigger.TriggerInstance();
	}

	public void trigger(ServerPlayer serverPlayer) {
		TickTrigger.PlayerListeners playerListeners = (TickTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger();
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<TickTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<TickTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<TickTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<TickTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger() {
			for (CriterionTrigger.Listener<TickTrigger.TriggerInstance> listener : Lists.newArrayList(this.listeners)) {
				listener.run(this.player);
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance() {
			super(TickTrigger.ID);
		}
	}
}
