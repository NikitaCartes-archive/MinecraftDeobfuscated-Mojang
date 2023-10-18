package net.minecraft.data.recipes.packs;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyOneRecipeProvider extends RecipeProvider {
	public UpdateOneTwentyOneRecipeProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	protected void buildRecipes(RecipeOutput recipeOutput) {
		generateForEnabledBlockFamilies(recipeOutput, FeatureFlagSet.of(FeatureFlags.UPDATE_1_21));
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.CRAFTER)
			.define('#', Items.IRON_INGOT)
			.define('C', Items.CRAFTING_TABLE)
			.define('R', Items.REDSTONE)
			.define('D', Items.DROPPER)
			.pattern("###")
			.pattern("#C#")
			.pattern("RDR")
			.unlockedBy("has_dropper", has(Items.DROPPER))
			.save(recipeOutput);
	}
}
