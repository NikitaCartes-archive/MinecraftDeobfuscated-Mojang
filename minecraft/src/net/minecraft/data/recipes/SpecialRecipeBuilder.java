package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

public class SpecialRecipeBuilder {
	private final SimpleRecipeSerializer<?> serializer;

	public SpecialRecipeBuilder(SimpleRecipeSerializer<?> simpleRecipeSerializer) {
		this.serializer = simpleRecipeSerializer;
	}

	public static SpecialRecipeBuilder special(SimpleRecipeSerializer<?> simpleRecipeSerializer) {
		return new SpecialRecipeBuilder(simpleRecipeSerializer);
	}

	public void save(Consumer<FinishedRecipe> consumer, String string) {
		consumer.accept(new FinishedRecipe() {
			@Override
			public void serializeRecipeData(JsonObject jsonObject) {
			}

			@Override
			public RecipeSerializer<?> getType() {
				return SpecialRecipeBuilder.this.serializer;
			}

			@Override
			public ResourceLocation getId() {
				return new ResourceLocation(string);
			}

			@Nullable
			@Override
			public JsonObject serializeAdvancement() {
				return null;
			}

			@Override
			public ResourceLocation getAdvancementId() {
				return new ResourceLocation("");
			}
		});
	}
}
