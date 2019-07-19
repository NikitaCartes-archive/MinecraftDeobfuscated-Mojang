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
import net.minecraft.world.entity.Entity;

public class SummonedEntityTrigger implements CriterionTrigger<SummonedEntityTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("summoned_entity");
	private final Map<PlayerAdvancements, SummonedEntityTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, SummonedEntityTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> listener) {
		SummonedEntityTrigger.PlayerListeners playerListeners = (SummonedEntityTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new SummonedEntityTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> listener) {
		SummonedEntityTrigger.PlayerListeners playerListeners = (SummonedEntityTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public SummonedEntityTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("entity"));
		return new SummonedEntityTrigger.TriggerInstance(entityPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity) {
		SummonedEntityTrigger.PlayerListeners playerListeners = (SummonedEntityTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, entity);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, Entity entity) {
			List<CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, entity)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate entity;

		public TriggerInstance(EntityPredicate entityPredicate) {
			super(SummonedEntityTrigger.ID);
			this.entity = entityPredicate;
		}

		public static SummonedEntityTrigger.TriggerInstance summonedEntity(EntityPredicate.Builder builder) {
			return new SummonedEntityTrigger.TriggerInstance(builder.build());
		}

		public boolean matches(ServerPlayer serverPlayer, Entity entity) {
			return this.entity.matches(serverPlayer, entity);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("entity", this.entity.serializeToJson());
			return jsonObject;
		}
	}
}
