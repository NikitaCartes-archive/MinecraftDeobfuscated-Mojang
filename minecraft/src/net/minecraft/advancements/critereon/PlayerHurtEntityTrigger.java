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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class PlayerHurtEntityTrigger implements CriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");
	private final Map<PlayerAdvancements, PlayerHurtEntityTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, PlayerHurtEntityTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> listener) {
		PlayerHurtEntityTrigger.PlayerListeners playerListeners = (PlayerHurtEntityTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new PlayerHurtEntityTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> listener) {
		PlayerHurtEntityTrigger.PlayerListeners playerListeners = (PlayerHurtEntityTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public PlayerHurtEntityTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		DamagePredicate damagePredicate = DamagePredicate.fromJson(jsonObject.get("damage"));
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("entity"));
		return new PlayerHurtEntityTrigger.TriggerInstance(damagePredicate, entityPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource, float f, float g, boolean bl) {
		PlayerHurtEntityTrigger.PlayerListeners playerListeners = (PlayerHurtEntityTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, entity, damageSource, f, g, bl);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource, float f, float g, boolean bl) {
			List<CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, entity, damageSource, f, g, bl)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final DamagePredicate damage;
		private final EntityPredicate entity;

		public TriggerInstance(DamagePredicate damagePredicate, EntityPredicate entityPredicate) {
			super(PlayerHurtEntityTrigger.ID);
			this.damage = damagePredicate;
			this.entity = entityPredicate;
		}

		public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder builder) {
			return new PlayerHurtEntityTrigger.TriggerInstance(builder.build(), EntityPredicate.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource, float f, float g, boolean bl) {
			return !this.damage.matches(serverPlayer, damageSource, f, g, bl) ? false : this.entity.matches(serverPlayer, entity);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("damage", this.damage.serializeToJson());
			jsonObject.add("entity", this.entity.serializeToJson());
			return jsonObject;
		}
	}
}
