package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class SimpleCraftingRecipeSerializer<T extends CraftingRecipe> implements RecipeSerializer<T> {
	private final SimpleCraftingRecipeSerializer.Factory<T> constructor;

	public SimpleCraftingRecipeSerializer(SimpleCraftingRecipeSerializer.Factory<T> factory) {
		this.constructor = factory;
	}

	public T fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
		CraftingBookCategory craftingBookCategory = (CraftingBookCategory)Objects.requireNonNullElse(
			(CraftingBookCategory)CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(jsonObject, "category", null)), CraftingBookCategory.MISC
		);
		return this.constructor.create(resourceLocation, craftingBookCategory);
	}

	public T fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
		CraftingBookCategory craftingBookCategory = friendlyByteBuf.readEnum(CraftingBookCategory.class);
		return this.constructor.create(resourceLocation, craftingBookCategory);
	}

	public void toNetwork(FriendlyByteBuf friendlyByteBuf, T craftingRecipe) {
		friendlyByteBuf.writeEnum(craftingRecipe.category());
	}

	@FunctionalInterface
	public interface Factory<T extends CraftingRecipe> {
		T create(ResourceLocation resourceLocation, CraftingBookCategory craftingBookCategory);
	}
}
