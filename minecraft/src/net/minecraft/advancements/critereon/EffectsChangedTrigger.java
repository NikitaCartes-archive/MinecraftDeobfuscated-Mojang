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
import net.minecraft.world.entity.LivingEntity;

public class EffectsChangedTrigger implements CriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("effects_changed");
	private final Map<PlayerAdvancements, EffectsChangedTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, EffectsChangedTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> listener) {
		EffectsChangedTrigger.PlayerListeners playerListeners = (EffectsChangedTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new EffectsChangedTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> listener) {
		EffectsChangedTrigger.PlayerListeners playerListeners = (EffectsChangedTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public EffectsChangedTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		MobEffectsPredicate mobEffectsPredicate = MobEffectsPredicate.fromJson(jsonObject.get("effects"));
		return new EffectsChangedTrigger.TriggerInstance(mobEffectsPredicate);
	}

	public void trigger(ServerPlayer serverPlayer) {
		EffectsChangedTrigger.PlayerListeners playerListeners = (EffectsChangedTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer) {
			List<CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MobEffectsPredicate effects;

		public TriggerInstance(MobEffectsPredicate mobEffectsPredicate) {
			super(EffectsChangedTrigger.ID);
			this.effects = mobEffectsPredicate;
		}

		public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate mobEffectsPredicate) {
			return new EffectsChangedTrigger.TriggerInstance(mobEffectsPredicate);
		}

		public boolean matches(ServerPlayer serverPlayer) {
			return this.effects.matches((LivingEntity)serverPlayer);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("effects", this.effects.serializeToJson());
			return jsonObject;
		}
	}
}
