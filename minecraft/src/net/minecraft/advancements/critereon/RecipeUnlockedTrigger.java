package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
	public RecipeUnlockedTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "recipe"));
		return new RecipeUnlockedTrigger.TriggerInstance(optional, resourceLocation);
	}

	public void trigger(ServerPlayer serverPlayer, RecipeHolder<?> recipeHolder) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(recipeHolder));
	}

	public static Criterion<RecipeUnlockedTrigger.TriggerInstance> unlocked(ResourceLocation resourceLocation) {
		return CriteriaTriggers.RECIPE_UNLOCKED.createCriterion(new RecipeUnlockedTrigger.TriggerInstance(Optional.empty(), resourceLocation));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ResourceLocation recipe;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, ResourceLocation resourceLocation) {
			super(optional);
			this.recipe = resourceLocation;
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			jsonObject.addProperty("recipe", this.recipe.toString());
			return jsonObject;
		}

		public boolean matches(RecipeHolder<?> recipeHolder) {
			return this.recipe.equals(recipeHolder.id());
		}
	}
}
