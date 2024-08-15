package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public interface Recipe<T extends RecipeInput> {
	Codec<Recipe<?>> CODEC = BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec().dispatch(Recipe::getSerializer, RecipeSerializer::codec);
	StreamCodec<RegistryFriendlyByteBuf, Recipe<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.RECIPE_SERIALIZER)
		.dispatch(Recipe::getSerializer, RecipeSerializer::streamCodec);

	boolean matches(T recipeInput, Level level);

	ItemStack assemble(T recipeInput, HolderLookup.Provider provider);

	boolean canCraftInDimensions(int i, int j);

	ItemStack getResultItem(HolderLookup.Provider provider);

	default NonNullList<ItemStack> getRemainingItems(T recipeInput) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(recipeInput.size(), ItemStack.EMPTY);

		for (int i = 0; i < nonNullList.size(); i++) {
			Item item = recipeInput.getItem(i).getItem();
			if (item.hasCraftingRemainingItem()) {
				nonNullList.set(i, new ItemStack(item.getCraftingRemainingItem()));
			}
		}

		return nonNullList;
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

	default ItemStack getCategoryIconItem() {
		return new ItemStack(Blocks.CRAFTING_TABLE);
	}

	RecipeSerializer<?> getSerializer();

	RecipeType<?> getType();

	PlacementInfo placementInfo();
}
