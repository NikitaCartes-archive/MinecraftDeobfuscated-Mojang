package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface RecipeSerializer<T extends Recipe<?>> {
	RecipeSerializer<ShapedRecipe> SHAPED_RECIPE = register("crafting_shaped", new ShapedRecipe.Serializer());
	RecipeSerializer<ShapelessRecipe> SHAPELESS_RECIPE = register("crafting_shapeless", new ShapelessRecipe.Serializer());
	RecipeSerializer<ArmorDyeRecipe> ARMOR_DYE = register("crafting_special_armordye", new CustomRecipe.Serializer<>(ArmorDyeRecipe::new));
	RecipeSerializer<BookCloningRecipe> BOOK_CLONING = register("crafting_special_bookcloning", new CustomRecipe.Serializer<>(BookCloningRecipe::new));
	RecipeSerializer<MapCloningRecipe> MAP_CLONING = register("crafting_special_mapcloning", new CustomRecipe.Serializer<>(MapCloningRecipe::new));
	RecipeSerializer<MapExtendingRecipe> MAP_EXTENDING = register("crafting_special_mapextending", new CustomRecipe.Serializer<>(MapExtendingRecipe::new));
	RecipeSerializer<FireworkRocketRecipe> FIREWORK_ROCKET = register("crafting_special_firework_rocket", new CustomRecipe.Serializer<>(FireworkRocketRecipe::new));
	RecipeSerializer<FireworkStarRecipe> FIREWORK_STAR = register("crafting_special_firework_star", new CustomRecipe.Serializer<>(FireworkStarRecipe::new));
	RecipeSerializer<FireworkStarFadeRecipe> FIREWORK_STAR_FADE = register(
		"crafting_special_firework_star_fade", new CustomRecipe.Serializer<>(FireworkStarFadeRecipe::new)
	);
	RecipeSerializer<TippedArrowRecipe> TIPPED_ARROW = register("crafting_special_tippedarrow", new CustomRecipe.Serializer<>(TippedArrowRecipe::new));
	RecipeSerializer<BannerDuplicateRecipe> BANNER_DUPLICATE = register(
		"crafting_special_bannerduplicate", new CustomRecipe.Serializer<>(BannerDuplicateRecipe::new)
	);
	RecipeSerializer<ShieldDecorationRecipe> SHIELD_DECORATION = register(
		"crafting_special_shielddecoration", new CustomRecipe.Serializer<>(ShieldDecorationRecipe::new)
	);
	RecipeSerializer<TransmuteRecipe> TRANSMUTE = register("crafting_transmute", new TransmuteRecipe.Serializer());
	RecipeSerializer<RepairItemRecipe> REPAIR_ITEM = register("crafting_special_repairitem", new CustomRecipe.Serializer<>(RepairItemRecipe::new));
	RecipeSerializer<SmeltingRecipe> SMELTING_RECIPE = register("smelting", new AbstractCookingRecipe.Serializer<>(SmeltingRecipe::new, 200));
	RecipeSerializer<BlastingRecipe> BLASTING_RECIPE = register("blasting", new AbstractCookingRecipe.Serializer<>(BlastingRecipe::new, 100));
	RecipeSerializer<SmokingRecipe> SMOKING_RECIPE = register("smoking", new AbstractCookingRecipe.Serializer<>(SmokingRecipe::new, 100));
	RecipeSerializer<CampfireCookingRecipe> CAMPFIRE_COOKING_RECIPE = register(
		"campfire_cooking", new AbstractCookingRecipe.Serializer<>(CampfireCookingRecipe::new, 100)
	);
	RecipeSerializer<StonecutterRecipe> STONECUTTER = register("stonecutting", new SingleItemRecipe.Serializer<>(StonecutterRecipe::new));
	RecipeSerializer<SmithingTransformRecipe> SMITHING_TRANSFORM = register("smithing_transform", new SmithingTransformRecipe.Serializer());
	RecipeSerializer<SmithingTrimRecipe> SMITHING_TRIM = register("smithing_trim", new SmithingTrimRecipe.Serializer());
	RecipeSerializer<DecoratedPotRecipe> DECORATED_POT_RECIPE = register("crafting_decorated_pot", new CustomRecipe.Serializer<>(DecoratedPotRecipe::new));

	MapCodec<T> codec();

	@Deprecated
	StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();

	static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String string, S recipeSerializer) {
		return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, string, recipeSerializer);
	}
}
