package net.minecraft.data.recipes.packs;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.TransmuteRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class BundleRecipeProvider extends RecipeProvider {
	BundleRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
		super(provider, recipeOutput);
	}

	@Override
	protected void buildRecipes() {
		this.shaped(RecipeCategory.TOOLS, Items.BUNDLE)
			.define('-', Items.STRING)
			.define('#', Items.LEATHER)
			.pattern("-")
			.pattern("#")
			.unlockedBy("has_string", this.has(Items.STRING))
			.save(this.output);
		this.bundleRecipes();
	}

	private void bundleRecipes() {
		Ingredient ingredient = this.tag(ItemTags.BUNDLES);

		for (DyeColor dyeColor : DyeColor.values()) {
			TransmuteRecipeBuilder.transmute(RecipeCategory.TOOLS, ingredient, Ingredient.of(DyeItem.byColor(dyeColor)), BundleItem.getByColor(dyeColor))
				.group("bundle_dye")
				.unlockedBy("has_bundle", this.has(ItemTags.BUNDLES))
				.save(this.output);
		}
	}

	public static class Runner extends RecipeProvider.Runner {
		public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
			super(packOutput, completableFuture);
		}

		@Override
		protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
			return new BundleRecipeProvider(provider, recipeOutput);
		}

		@Override
		public String getName() {
			return "Bundle Recipes";
		}
	}
}
