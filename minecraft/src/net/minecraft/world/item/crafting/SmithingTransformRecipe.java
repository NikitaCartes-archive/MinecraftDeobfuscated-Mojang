package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.stream.Stream;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SmithingTransformRecipe implements SmithingRecipe {
	private final ResourceLocation id;
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;
	final ItemStack result;

	public SmithingTransformRecipe(ResourceLocation resourceLocation, Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3, ItemStack itemStack) {
		this.id = resourceLocation;
		this.template = ingredient;
		this.base = ingredient2;
		this.addition = ingredient3;
		this.result = itemStack;
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.template.test(container.getItem(0)) && this.base.test(container.getItem(1)) && this.addition.test(container.getItem(2));
	}

	@Override
	public ItemStack assemble(Container container, RegistryAccess registryAccess) {
		ItemStack itemStack = this.result.copy();
		CompoundTag compoundTag = container.getItem(1).getTag();
		if (compoundTag != null) {
			itemStack.setTag(compoundTag.copy());
		}

		return itemStack;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return this.result;
	}

	@Override
	public boolean isTemplateIngredient(ItemStack itemStack) {
		return this.template.test(itemStack);
	}

	@Override
	public boolean isBaseIngredient(ItemStack itemStack) {
		return this.base.test(itemStack);
	}

	@Override
	public boolean isAdditionIngredient(ItemStack itemStack) {
		return this.addition.test(itemStack);
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SMITHING_TRANSFORM;
	}

	@Override
	public boolean isIncomplete() {
		return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
	}

	public static class Serializer implements RecipeSerializer<SmithingTransformRecipe> {
		public SmithingTransformRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
			Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(jsonObject, "template"));
			Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getNonNull(jsonObject, "base"));
			Ingredient ingredient3 = Ingredient.fromJson(GsonHelper.getNonNull(jsonObject, "addition"));
			ItemStack itemStack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "result"));
			return new SmithingTransformRecipe(resourceLocation, ingredient, ingredient2, ingredient3, itemStack);
		}

		public SmithingTransformRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
			Ingredient ingredient = Ingredient.fromNetwork(friendlyByteBuf);
			Ingredient ingredient2 = Ingredient.fromNetwork(friendlyByteBuf);
			Ingredient ingredient3 = Ingredient.fromNetwork(friendlyByteBuf);
			ItemStack itemStack = friendlyByteBuf.readItem();
			return new SmithingTransformRecipe(resourceLocation, ingredient, ingredient2, ingredient3, itemStack);
		}

		public void toNetwork(FriendlyByteBuf friendlyByteBuf, SmithingTransformRecipe smithingTransformRecipe) {
			smithingTransformRecipe.template.toNetwork(friendlyByteBuf);
			smithingTransformRecipe.base.toNetwork(friendlyByteBuf);
			smithingTransformRecipe.addition.toNetwork(friendlyByteBuf);
			friendlyByteBuf.writeItem(smithingTransformRecipe.result);
		}
	}
}
