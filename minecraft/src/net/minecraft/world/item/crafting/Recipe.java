package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

public interface Recipe<T extends RecipeInput> {
	Codec<Recipe<?>> CODEC = BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec().dispatch(Recipe::getSerializer, RecipeSerializer::codec);
	StreamCodec<RegistryFriendlyByteBuf, Recipe<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.RECIPE_SERIALIZER)
		.dispatch(Recipe::getSerializer, RecipeSerializer::streamCodec);

	boolean matches(T recipeInput, Level level);

	ItemStack assemble(T recipeInput, HolderLookup.Provider provider);

	default boolean isSpecial() {
		return false;
	}

	default boolean showNotification() {
		return true;
	}

	default String group() {
		return "";
	}

	RecipeSerializer<? extends Recipe<T>> getSerializer();

	RecipeType<? extends Recipe<T>> getType();

	PlacementInfo placementInfo();

	default List<RecipeDisplay> display() {
		return List.of();
	}

	BasicRecipeBookCategory recipeBookCategory();
}
