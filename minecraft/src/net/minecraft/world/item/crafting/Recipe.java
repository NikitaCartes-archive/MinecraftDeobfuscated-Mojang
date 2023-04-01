package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public interface Recipe<C extends Container> {
	boolean matches(C container, Level level);

	ItemStack assembleRaw(C container, RegistryAccess registryAccess);

	default ItemStack assemble(C container, RegistryAccess registryAccess) {
		return Rules.DOUBLE_OR_HALF_RECIPE_OUTPUT
			.adjustItemStack(Rules.REPLACE_RECIPE_OUTPUT.adjustItemStack(registryAccess, this.assembleRaw(container, registryAccess)));
	}

	boolean canCraftInDimensions(int i, int j);

	ItemStack getResultItemRaw(RegistryAccess registryAccess);

	default ItemStack getResultItem(RegistryAccess registryAccess) {
		return Rules.DOUBLE_OR_HALF_RECIPE_OUTPUT.adjustItemStack(Rules.REPLACE_RECIPE_OUTPUT.adjustItemStack(registryAccess, this.getResultItemRaw(registryAccess)));
	}

	default NonNullList<ItemStack> getRemainingItems(C container) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

		for (int i = 0; i < nonNullList.size(); i++) {
			Item item = container.getItem(i).getItem();
			if (item.hasCraftingRemainingItem()) {
				nonNullList.set(i, new ItemStack(item.getCraftingRemainingItem()));
			}
		}

		return nonNullList;
	}

	default NonNullList<Ingredient> getIngredients() {
		return NonNullList.create();
	}

	default boolean isSpecial() {
		return false;
	}

	default boolean showNotification() {
		return true;
	}

	default String getGroup() {
		return "";
	}

	default ItemStack getToastSymbol() {
		return new ItemStack(Blocks.CRAFTING_TABLE);
	}

	ResourceLocation getId();

	RecipeSerializer<?> getSerializer();

	RecipeType<?> getType();

	default boolean isIncomplete() {
		NonNullList<Ingredient> nonNullList = this.getIngredients();
		return nonNullList.isEmpty() || nonNullList.stream().anyMatch(ingredient -> ingredient.getItems().length == 0);
	}
}
