package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger implements CriterionTrigger<BrewedPotionTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("brewed_potion");
	private final Map<PlayerAdvancements, BrewedPotionTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, BrewedPotionTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> listener) {
		BrewedPotionTrigger.PlayerListeners playerListeners = (BrewedPotionTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new BrewedPotionTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> listener) {
		BrewedPotionTrigger.PlayerListeners playerListeners = (BrewedPotionTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public BrewedPotionTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		Potion potion = null;
		if (jsonObject.has("potion")) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "potion"));
			potion = (Potion)Registry.POTION.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + resourceLocation + "'"));
		}

		return new BrewedPotionTrigger.TriggerInstance(potion);
	}

	public void trigger(ServerPlayer serverPlayer, Potion potion) {
		BrewedPotionTrigger.PlayerListeners playerListeners = (BrewedPotionTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(potion);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(Potion potion) {
			List<CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(potion)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<BrewedPotionTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Potion potion;

		public TriggerInstance(@Nullable Potion potion) {
			super(BrewedPotionTrigger.ID);
			this.potion = potion;
		}

		public static BrewedPotionTrigger.TriggerInstance brewedPotion() {
			return new BrewedPotionTrigger.TriggerInstance(null);
		}

		public boolean matches(Potion potion) {
			return this.potion == null || this.potion == potion;
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			if (this.potion != null) {
				jsonObject.addProperty("potion", Registry.POTION.getKey(this.potion).toString());
			}

			return jsonObject;
		}
	}
}
