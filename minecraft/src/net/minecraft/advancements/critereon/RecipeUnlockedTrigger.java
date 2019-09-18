package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("recipe_unlocked");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public RecipeUnlockedTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "recipe"));
		return new RecipeUnlockedTrigger.TriggerInstance(resourceLocation);
	}

	public void trigger(ServerPlayer serverPlayer, Recipe<?> recipe) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(recipe));
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
