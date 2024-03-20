package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public interface Recipe<C extends Container> {
	Codec<Recipe<?>> CODEC = BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec().dispatch(Recipe::getSerializer, RecipeSerializer::codec);
	StreamCodec<RegistryFriendlyByteBuf, Recipe<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.RECIPE_SERIALIZER)
		.dispatch(Recipe::getSerializer, RecipeSerializer::streamCodec);

	boolean matches(C container, Level level);

	ItemStack assemble(C container, HolderLookup.Provider provider);

	boolean canCraftInDimensions(int i, int j);

	ItemStack getResultItem(HolderLookup.Provider provider);

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

	RecipeSerializer<?> getSerializer();

	RecipeType<?> getType();

	default boolean isIncomplete() {
		NonNullList<Ingredient> nonNullList = this.getIngredients();
		return nonNullList.isEmpty() || nonNullList.stream().anyMatch(ingredient -> ingredient.getItems().length == 0);
	}
}
