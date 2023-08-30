package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface FinishedRecipe {
	void serializeRecipeData(JsonObject jsonObject);

	default JsonObject serializeRecipe() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", BuiltInRegistries.RECIPE_SERIALIZER.getKey(this.type()).toString());
		this.serializeRecipeData(jsonObject);
		return jsonObject;
	}

	ResourceLocation id();

	RecipeSerializer<?> type();

	@Nullable
	AdvancementHolder advancement();
}
