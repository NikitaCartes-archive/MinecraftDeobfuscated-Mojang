package net.minecraft.data.recipes.packs;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;

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
