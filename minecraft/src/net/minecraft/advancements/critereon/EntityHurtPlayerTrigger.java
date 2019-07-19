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

public class EntityHurtPlayerTrigger implements CriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("entity_hurt_player");
	private final Map<PlayerAdvancements, EntityHurtPlayerTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, EntityHurtPlayerTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> listener) {
		EntityHurtPlayerTrigger.PlayerListeners playerListeners = (EntityHurtPlayerTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new EntityHurtPlayerTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> listener) {
		EntityHurtPlayerTrigger.PlayerListeners playerListeners = (EntityHurtPlayerTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public EntityHurtPlayerTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		DamagePredicate damagePredicate = DamagePredicate.fromJson(jsonObject.get("damage"));
		return new EntityHurtPlayerTrigger.TriggerInstance(damagePredicate);
	}

	public void trigger(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
		EntityHurtPlayerTrigger.PlayerListeners playerListeners = (EntityHurtPlayerTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, damageSource, f, g, bl);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
			List<CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, damageSource, f, g, bl)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final DamagePredicate damage;

		public TriggerInstance(DamagePredicate damagePredicate) {
			super(EntityHurtPlayerTrigger.ID);
			this.damage = damagePredicate;
		}

		public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer(DamagePredicate.Builder builder) {
			return new EntityHurtPlayerTrigger.TriggerInstance(builder.build());
		}

		public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
			return this.damage.matches(serverPlayer, damageSource, f, g, bl);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("damage", this.damage.serializeToJson());
			return jsonObject;
		}
	}
}
