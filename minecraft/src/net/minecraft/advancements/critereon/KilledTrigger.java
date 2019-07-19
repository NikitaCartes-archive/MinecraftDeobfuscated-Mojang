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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class KilledTrigger implements CriterionTrigger<KilledTrigger.TriggerInstance> {
	private final Map<PlayerAdvancements, KilledTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, KilledTrigger.PlayerListeners>newHashMap();
	private final ResourceLocation id;

	public KilledTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<KilledTrigger.TriggerInstance> listener) {
		KilledTrigger.PlayerListeners playerListeners = (KilledTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new KilledTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<KilledTrigger.TriggerInstance> listener) {
		KilledTrigger.PlayerListeners playerListeners = (KilledTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public KilledTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		return new KilledTrigger.TriggerInstance(
			this.id, EntityPredicate.fromJson(jsonObject.get("entity")), DamageSourcePredicate.fromJson(jsonObject.get("killing_blow"))
		);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
		KilledTrigger.PlayerListeners playerListeners = (KilledTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, entity, damageSource);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<KilledTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<KilledTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<KilledTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<KilledTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
			List<CriterionTrigger.Listener<KilledTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<KilledTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, entity, damageSource)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<KilledTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<KilledTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate entityPredicate;
		private final DamageSourcePredicate killingBlow;

		public TriggerInstance(ResourceLocation resourceLocation, EntityPredicate entityPredicate, DamageSourcePredicate damageSourcePredicate) {
			super(resourceLocation);
			this.entityPredicate = entityPredicate;
			this.killingBlow = damageSourcePredicate;
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder builder) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, builder.build(), DamageSourcePredicate.ANY);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity() {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.ANY, DamageSourcePredicate.ANY);
		}

		public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, builder.build(), builder2.build());
		}

		public static KilledTrigger.TriggerInstance entityKilledPlayer() {
			return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.ANY, DamageSourcePredicate.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
			return !this.killingBlow.matches(serverPlayer, damageSource) ? false : this.entityPredicate.matches(serverPlayer, entity);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("entity", this.entityPredicate.serializeToJson());
			jsonObject.add("killing_blow", this.killingBlow.serializeToJson());
			return jsonObject;
		}
	}
}
