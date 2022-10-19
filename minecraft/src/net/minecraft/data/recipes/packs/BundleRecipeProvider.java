package net.minecraft.data.recipes.packs;

import java.util.function.Consumer;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

public class BundleRecipeProvider extends RecipeProvider {
	public BundleRecipeProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.BUNDLE)
			.define('#', Items.RABBIT_HIDE)
			.define('-', Items.STRING)
			.pattern("-#-")
			.pattern("# #")
			.pattern("###")
			.unlockedBy("has_string", has(Items.STRING))
			.save(consumer);
	}

	@Override
	public String getName() {
		return "Bundle Recipes";
	}
}
