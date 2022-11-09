package net.minecraft.world.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
	private final int defaultCookingTime;
	private final SimpleCookingSerializer.CookieBaker<T> factory;

	public SimpleCookingSerializer(SimpleCookingSerializer.CookieBaker<T> cookieBaker, int i) {
		this.defaultCookingTime = i;
		this.factory = cookieBaker;
	}

	public T fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
		String string = GsonHelper.getAsString(jsonObject, "group", "");
		CookingBookCategory cookingBookCategory = (CookingBookCategory)Objects.requireNonNullElse(
			(CookingBookCategory)CookingBookCategory.CODEC.byName(GsonHelper.getAsString(jsonObject, "category", null)), CookingBookCategory.MISC
		);
		JsonElement jsonElement = (JsonElement)(GsonHelper.isArrayNode(jsonObject, "ingredient")
			? GsonHelper.getAsJsonArray(jsonObject, "ingredient")
			: GsonHelper.getAsJsonObject(jsonObject, "ingredient"));
		Ingredient ingredient = Ingredient.fromJson(jsonElement);
		String string2 = GsonHelper.getAsString(jsonObject, "result");
		ResourceLocation resourceLocation2 = new ResourceLocation(string2);
		ItemStack itemStack = new ItemStack(
			(ItemLike)BuiltInRegistries.ITEM.getOptional(resourceLocation2).orElseThrow(() -> new IllegalStateException("Item: " + string2 + " does not exist"))
		);
		float f = GsonHelper.getAsFloat(jsonObject, "experience", 0.0F);
		int i = GsonHelper.getAsInt(jsonObject, "cookingtime", this.defaultCookingTime);
		return this.factory.create(resourceLocation, string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	public T fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
		String string = friendlyByteBuf.readUtf();
		CookingBookCategory cookingBookCategory = friendlyByteBuf.readEnum(CookingBookCategory.class);
		Ingredient ingredient = Ingredient.fromNetwork(friendlyByteBuf);
		ItemStack itemStack = friendlyByteBuf.readItem();
		float f = friendlyByteBuf.readFloat();
		int i = friendlyByteBuf.readVarInt();
		return this.factory.create(resourceLocation, string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	public void toNetwork(FriendlyByteBuf friendlyByteBuf, T abstractCookingRecipe) {
		friendlyByteBuf.writeUtf(abstractCookingRecipe.group);
		friendlyByteBuf.writeEnum(abstractCookingRecipe.category());
		abstractCookingRecipe.ingredient.toNetwork(friendlyByteBuf);
		friendlyByteBuf.writeItem(abstractCookingRecipe.result);
		friendlyByteBuf.writeFloat(abstractCookingRecipe.experience);
		friendlyByteBuf.writeVarInt(abstractCookingRecipe.cookingTime);
	}

	interface CookieBaker<T extends AbstractCookingRecipe> {
		T create(
			ResourceLocation resourceLocation, String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i
		);
	}
}
