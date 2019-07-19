package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface FinishedRecipe {
	void serializeRecipeData(JsonObject jsonObject);

	default JsonObject serializeRecipe() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", Registry.RECIPE_SERIALIZER.getKey(this.getType()).toString());
		this.serializeRecipeData(jsonObject);
		return jsonObject;
	}

	ResourceLocation getId();

	RecipeSerializer<?> getType();

	@Nullable
	JsonObject serializeAdvancement();

	@Nullable
	ResourceLocation getAdvancementId();
}
