package net.minecraft.data.recipes.packs;

import java.util.function.Consumer;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyRecipeProvider extends RecipeProvider {
	public UpdateOneTwentyRecipeProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		generateForEnabledBlockFamilies(consumer, FeatureFlagSet.of(FeatureFlags.UPDATE_1_20));
		twoByTwoPacker(consumer, RecipeCategory.DECORATIONS, Blocks.BAMBOO_PLANKS, Items.BAMBOO);
		mosaicBuilder(consumer, RecipeCategory.DECORATIONS, Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_SLAB);
		woodenBoat(consumer, Items.BAMBOO_RAFT, Blocks.BAMBOO_PLANKS);
		chestBoat(consumer, Items.BAMBOO_CHEST_RAFT, Items.BAMBOO_RAFT);
		hangingSign(consumer, Items.ACACIA_HANGING_SIGN, Blocks.STRIPPED_OAK_LOG);
		hangingSign(consumer, Items.DARK_OAK_HANGING_SIGN, Blocks.STRIPPED_DARK_OAK_LOG);
		hangingSign(consumer, Items.JUNGLE_HANGING_SIGN, Blocks.STRIPPED_JUNGLE_LOG);
		hangingSign(consumer, Items.OAK_HANGING_SIGN, Blocks.STRIPPED_OAK_LOG);
		hangingSign(consumer, Items.SPRUCE_HANGING_SIGN, Blocks.STRIPPED_SPRUCE_LOG);
		hangingSign(consumer, Items.MANGROVE_HANGING_SIGN, Blocks.STRIPPED_MANGROVE_LOG);
		hangingSign(consumer, Items.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_PLANKS, 2);
		hangingSign(consumer, Items.CRIMSON_HANGING_SIGN, Blocks.STRIPPED_CRIMSON_STEM);
		hangingSign(consumer, Items.WARPED_HANGING_SIGN, Blocks.STRIPPED_WARPED_STEM);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_BOOKSHELF)
			.define('#', ItemTags.PLANKS)
			.define('X', ItemTags.WOODEN_SLABS)
			.pattern("###")
			.pattern("XXX")
			.pattern("###")
			.unlockedBy("has_book", has(Items.BOOK))
			.save(consumer);
	}

	@Override
	public String getName() {
		return "Update 1.20 Recipes";
	}
}
