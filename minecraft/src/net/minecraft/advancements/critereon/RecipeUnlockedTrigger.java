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
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeUnlockedTrigger implements CriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("recipe_unlocked");
	private final Map<PlayerAdvancements, RecipeUnlockedTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, RecipeUnlockedTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> listener) {
		RecipeUnlockedTrigger.PlayerListeners playerListeners = (RecipeUnlockedTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new RecipeUnlockedTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> listener) {
		RecipeUnlockedTrigger.PlayerListeners playerListeners = (RecipeUnlockedTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public RecipeUnlockedTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "recipe"));
		return new RecipeUnlockedTrigger.TriggerInstance(resourceLocation);
	}

	public void trigger(ServerPlayer serverPlayer, Recipe<?> recipe) {
		RecipeUnlockedTrigger.PlayerListeners playerListeners = (RecipeUnlockedTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(recipe);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(Recipe<?> recipe) {
			List<CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(recipe)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ResourceLocation recipe;

		public TriggerInstance(ResourceLocation resourceLocation) {
			super(RecipeUnlockedTrigger.ID);
			this.recipe = resourceLocation;
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("recipe", this.recipe.toString());
			return jsonObject;
		}

		public boolean matches(Recipe<?> recipe) {
			return this.recipe.equals(recipe.getId());
		}
	}
}
