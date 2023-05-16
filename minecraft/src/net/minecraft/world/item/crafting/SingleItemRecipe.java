package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public abstract class SingleItemRecipe implements Recipe<Container> {
	protected final Ingredient ingredient;
	protected final ItemStack result;
	private final RecipeType<?> type;
	private final RecipeSerializer<?> serializer;
	protected final ResourceLocation id;
	protected final String group;

	public SingleItemRecipe(
		RecipeType<?> recipeType, RecipeSerializer<?> recipeSerializer, ResourceLocation resourceLocation, String string, Ingredient ingredient, ItemStack itemStack
	) {
		this.type = recipeType;
		this.serializer = recipeSerializer;
		this.id = resourceLocation;
		this.group = string;
		this.ingredient = ingredient;
		this.result = itemStack;
	}

	@Override
	public RecipeType<?> getType() {
		return this.type;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return this.serializer;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return this.result;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nonNullList = NonNullList.create();
		nonNullList.add(this.ingredient);
		return nonNullList;
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return true;
	}

	@Override
	public ItemStack assemble(Container container, RegistryAccess registryAccess) {
		return this.result.copy();
	}

	public static class Serializer<T extends SingleItemRecipe> implements RecipeSerializer<T> {
		final SingleItemRecipe.Serializer.SingleItemMaker<T> factory;

		protected Serializer(SingleItemRecipe.Serializer.SingleItemMaker<T> singleItemMaker) {
			this.factory = singleItemMaker;
		}

		public T fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
			String string = GsonHelper.getAsString(jsonObject, "group", "");
			Ingredient ingredient;
			if (GsonHelper.isArrayNode(jsonObject, "ingredient")) {
				ingredient = Ingredient.fromJson(GsonHelper.getAsJsonArray(jsonObject, "ingredient"), false);
			} else {
				ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(jsonObject, "ingredient"), false);
			}

			String string2 = GsonHelper.getAsString(jsonObject, "result");
			int i = GsonHelper.getAsInt(jsonObject, "count");
			ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(string2)), i);
			return this.factory.create(resourceLocation, string, ingredient, itemStack);
		}

		public T fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
			String string = friendlyByteBuf.readUtf();
			Ingredient ingredient = Ingredient.fromNetwork(friendlyByteBuf);
			ItemStack itemStack = friendlyByteBuf.readItem();
			return this.factory.create(resourceLocation, string, ingredient, itemStack);
		}

		public void toNetwork(FriendlyByteBuf friendlyByteBuf, T singleItemRecipe) {
			friendlyByteBuf.writeUtf(singleItemRecipe.group);
			singleItemRecipe.ingredient.toNetwork(friendlyByteBuf);
			friendlyByteBuf.writeItem(singleItemRecipe.result);
		}

		interface SingleItemMaker<T extends SingleItemRecipe> {
			T create(ResourceLocation resourceLocation, String string, Ingredient ingredient, ItemStack itemStack);
		}
	}
}
