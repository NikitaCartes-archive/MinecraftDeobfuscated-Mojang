package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class UpgradeRecipe implements Recipe<Container> {
	private final Ingredient base;
	private final Ingredient addition;
	private final ItemStack result;
	private final ResourceLocation id;

	public UpgradeRecipe(ResourceLocation resourceLocation, Ingredient ingredient, Ingredient ingredient2, ItemStack itemStack) {
		this.id = resourceLocation;
		this.base = ingredient;
		this.addition = ingredient2;
		this.result = itemStack;
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.base.test(container.getItem(0)) && this.addition.test(container.getItem(1));
	}

	@Override
	public ItemStack assemble(Container container) {
		ItemStack itemStack = this.result.copy();
		CompoundTag compoundTag = container.getItem(0).getTag();
		if (compoundTag != null) {
			itemStack.setTag(compoundTag.copy());
		}

		return itemStack;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= 2;
	}

	@Override
	public ItemStack getResultItem() {
		return this.result;
	}

	public boolean isAdditionIngredient(ItemStack itemStack) {
		return this.addition.test(itemStack);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.SMITHING_TABLE);
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SMITHING;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeType.SMITHING;
	}

	public static class Serializer implements RecipeSerializer<UpgradeRecipe> {
		public UpgradeRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
			Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(jsonObject, "base"));
			Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getAsJsonObject(jsonObject, "addition"));
			ItemStack itemStack = ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(jsonObject, "result"));
			return new UpgradeRecipe(resourceLocation, ingredient, ingredient2, itemStack);
		}

		public UpgradeRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
			Ingredient ingredient = Ingredient.fromNetwork(friendlyByteBuf);
			Ingredient ingredient2 = Ingredient.fromNetwork(friendlyByteBuf);
			ItemStack itemStack = friendlyByteBuf.readItem();
			return new UpgradeRecipe(resourceLocation, ingredient, ingredient2, itemStack);
		}

		public void toNetwork(FriendlyByteBuf friendlyByteBuf, UpgradeRecipe upgradeRecipe) {
			upgradeRecipe.base.toNetwork(friendlyByteBuf);
			upgradeRecipe.addition.toNetwork(friendlyByteBuf);
			friendlyByteBuf.writeItem(upgradeRecipe.result);
		}
	}
}
