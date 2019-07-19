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
import net.minecraft.world.entity.Entity;

public class ChanneledLightningTrigger implements CriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("channeled_lightning");
	private final Map<PlayerAdvancements, ChanneledLightningTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, ChanneledLightningTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ChanneledLightningTrigger.TriggerInstance> listener) {
		ChanneledLightningTrigger.PlayerListeners playerListeners = (ChanneledLightningTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new ChanneledLightningTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ChanneledLightningTrigger.TriggerInstance> listener) {
		ChanneledLightningTrigger.PlayerListeners playerListeners = (ChanneledLightningTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public ChanneledLightningTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate[] entityPredicates = EntityPredicate.fromJsonArray(jsonObject.get("victims"));
		return new ChanneledLightningTrigger.TriggerInstance(entityPredicates);
	}

	public void trigger(ServerPlayer serverPlayer, Collection<? extends Entity> collection) {
		ChanneledLightningTrigger.PlayerListeners playerListeners = (ChanneledLightningTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, collection);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<ChanneledLightningTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<ChanneledLightningTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<ChanneledLightningTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<ChanneledLightningTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, Collection<? extends Entity> collection) {
			List<CriterionTrigger.Listener<ChanneledLightningTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<ChanneledLightningTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, collection)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<ChanneledLightningTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<ChanneledLightningTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate[] victims;

		public TriggerInstance(EntityPredicate[] entityPredicates) {
			super(ChanneledLightningTrigger.ID);
			this.victims = entityPredicates;
		}

		public static ChanneledLightningTrigger.TriggerInstance channeledLightning(EntityPredicate... entityPredicates) {
			return new ChanneledLightningTrigger.TriggerInstance(entityPredicates);
		}

		public boolean matches(ServerPlayer serverPlayer, Collection<? extends Entity> collection) {
			for (EntityPredicate entityPredicate : this.victims) {
				boolean bl = false;

				for (Entity entity : collection) {
					if (entityPredicate.matches(serverPlayer, entity)) {
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

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("victims", EntityPredicate.serializeArrayToJson(this.victims));
			return jsonObject;
		}
	}
}
