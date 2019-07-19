package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger implements CriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("used_ender_eye");
	private final Map<PlayerAdvancements, UsedEnderEyeTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, UsedEnderEyeTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> listener) {
		UsedEnderEyeTrigger.PlayerListeners playerListeners = (UsedEnderEyeTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new UsedEnderEyeTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> listener) {
		UsedEnderEyeTrigger.PlayerListeners playerListeners = (UsedEnderEyeTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public UsedEnderEyeTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		MinMaxBounds.Floats floats = MinMaxBounds.Floats.fromJson(jsonObject.get("distance"));
		return new UsedEnderEyeTrigger.TriggerInstance(floats);
	}

	public void trigger(ServerPlayer serverPlayer, BlockPos blockPos) {
		UsedEnderEyeTrigger.PlayerListeners playerListeners = (UsedEnderEyeTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			double d = serverPlayer.x - (double)blockPos.getX();
			double e = serverPlayer.z - (double)blockPos.getZ();
			playerListeners.trigger(d * d + e * e);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(double d) {
			List<CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(d)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Floats level;

		public TriggerInstance(MinMaxBounds.Floats floats) {
			super(UsedEnderEyeTrigger.ID);
			this.level = floats;
		}

		public boolean matches(double d) {
			return this.level.matchesSqr(d);
		}
	}
}
