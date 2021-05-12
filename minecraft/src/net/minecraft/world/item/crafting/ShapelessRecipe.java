package net.minecraft.world.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapelessRecipe implements CraftingRecipe {
	private final ResourceLocation id;
	final String group;
	final ItemStack result;
	final NonNullList<Ingredient> ingredients;

	public ShapelessRecipe(ResourceLocation resourceLocation, String string, ItemStack itemStack, NonNullList<Ingredient> nonNullList) {
		this.id = resourceLocation;
		this.group = string;
		this.result = itemStack;
		this.ingredients = nonNullList;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SHAPELESS_RECIPE;
	}

	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
	public ItemStack getResultItem() {
		return this.result;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return this.ingredients;
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		StackedContents stackedContents = new StackedContents();
		int i = 0;

		for (int j = 0; j < craftingContainer.getContainerSize(); j++) {
			ItemStack itemStack = craftingContainer.getItem(j);
			if (!itemStack.isEmpty()) {
				i++;
				stackedContents.accountStack(itemStack, 1);
			}
		}

		return i == this.ingredients.size() && stackedContents.canCraft(this, null);
	}

	public ItemStack assemble(CraftingContainer craftingContainer) {
		return this.result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= this.ingredients.size();
	}

	public static class Serializer implements RecipeSerializer<ShapelessRecipe> {
		public ShapelessRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
			String string = GsonHelper.getAsString(jsonObject, "group", "");
			NonNullList<Ingredient> nonNullList = itemsFromJson(GsonHelper.getAsJsonArray(jsonObject, "ingredients"));
			if (nonNullList.isEmpty()) {
				throw new JsonParseException("No ingredients for shapeless recipe");
			} else if (nonNullList.size() > 9) {
				throw new JsonParseException("Too many ingredients for shapeless recipe");
			} else {
				ItemStack itemStack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "result"));
				return new ShapelessRecipe(resourceLocation, string, itemStack, nonNullList);
			}
		}

		private static NonNullList<Ingredient> itemsFromJson(JsonArray jsonArray) {
			NonNullList<Ingredient> nonNullList = NonNullList.create();

			for (int i = 0; i < jsonArray.size(); i++) {
				Ingredient ingredient = Ingredient.fromJson(jsonArray.get(i));
				if (!ingredient.isEmpty()) {
					nonNullList.add(ingredient);
				}
			}

			return nonNullList;
		}

		public ShapelessRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
			String string = friendlyByteBuf.readUtf();
			int i = friendlyByteBuf.readVarInt();
			NonNullList<Ingredient> nonNullList = NonNullList.withSize(i, Ingredient.EMPTY);

			for (int j = 0; j < nonNullList.size(); j++) {
				nonNullList.set(j, Ingredient.fromNetwork(friendlyByteBuf));
			}

			ItemStack itemStack = friendlyByteBuf.readItem();
			return new ShapelessRecipe(resourceLocation, string, itemStack, nonNullList);
		}

		public void toNetwork(FriendlyByteBuf friendlyByteBuf, ShapelessRecipe shapelessRecipe) {
			friendlyByteBuf.writeUtf(shapelessRecipe.group);
			friendlyByteBuf.writeVarInt(shapelessRecipe.ingredients.size());

			for (Ingredient ingredient : shapelessRecipe.ingredients) {
				ingredient.toNetwork(friendlyByteBuf);
			}

			friendlyByteBuf.writeItem(shapelessRecipe.result);
		}
	}
}
