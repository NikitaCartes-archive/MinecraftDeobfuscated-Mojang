package net.minecraft.advancements.critereon;

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

	public RecipeUnlockedTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "recipe"));
		return new RecipeUnlockedTrigger.TriggerInstance(composite, resourceLocation);
	}

	public void trigger(ServerPlayer serverPlayer, Recipe<?> recipe) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(recipe));
	}

	public static RecipeUnlockedTrigger.TriggerInstance unlocked(ResourceLocation resourceLocation) {
		return new RecipeUnlockedTrigger.TriggerInstance(EntityPredicate.Composite.ANY, resourceLocation);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ResourceLocation recipe;

		public TriggerInstance(EntityPredicate.Composite composite, ResourceLocation resourceLocation) {
			super(RecipeUnlockedTrigger.ID, composite);
			this.recipe = resourceLocation;
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.addProperty("recipe", this.recipe.toString());
			return jsonObject;
		}

		public boolean matches(Recipe<?> recipe) {
			return this.recipe.equals(recipe.getId());
		}
	}
}
