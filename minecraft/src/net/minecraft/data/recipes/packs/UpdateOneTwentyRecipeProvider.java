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
		threeByThreePacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.BAMBOO_BLOCK, Items.BAMBOO);
		planksFromLogs(consumer, Blocks.BAMBOO_PLANKS, ItemTags.BAMBOO_BLOCKS, 2);
		mosaicBuilder(consumer, RecipeCategory.DECORATIONS, Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_SLAB);
		woodenBoat(consumer, Items.BAMBOO_RAFT, Blocks.BAMBOO_PLANKS);
		chestBoat(consumer, Items.BAMBOO_CHEST_RAFT, Items.BAMBOO_RAFT);
		hangingSign(consumer, Items.OAK_HANGING_SIGN, Blocks.STRIPPED_OAK_LOG);
		hangingSign(consumer, Items.SPRUCE_HANGING_SIGN, Blocks.STRIPPED_SPRUCE_LOG);
		hangingSign(consumer, Items.BIRCH_HANGING_SIGN, Blocks.STRIPPED_BIRCH_LOG);
		hangingSign(consumer, Items.JUNGLE_HANGING_SIGN, Blocks.STRIPPED_JUNGLE_LOG);
		hangingSign(consumer, Items.ACACIA_HANGING_SIGN, Blocks.STRIPPED_ACACIA_LOG);
		hangingSign(consumer, Items.DARK_OAK_HANGING_SIGN, Blocks.STRIPPED_DARK_OAK_LOG);
		hangingSign(consumer, Items.MANGROVE_HANGING_SIGN, Blocks.STRIPPED_MANGROVE_LOG);
		hangingSign(consumer, Items.BAMBOO_HANGING_SIGN, Items.STRIPPED_BAMBOO_BLOCK);
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
		trimSmithing(consumer, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);
		trimSmithing(consumer, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE);
		trimSmithing(consumer, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE);
		trimSmithing(consumer, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE);
		trimSmithing(consumer, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE);
		trimSmithing(consumer, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE);
		trimSmithing(consumer, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);
		trimSmithing(consumer, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE);
		trimSmithing(consumer, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE);
		trimSmithing(consumer, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);
		trimSmithing(consumer, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE);
		netheriteSmithing(consumer, Items.DIAMOND_CHESTPLATE, RecipeCategory.COMBAT, Items.NETHERITE_CHESTPLATE);
		netheriteSmithing(consumer, Items.DIAMOND_LEGGINGS, RecipeCategory.COMBAT, Items.NETHERITE_LEGGINGS);
		netheriteSmithing(consumer, Items.DIAMOND_HELMET, RecipeCategory.COMBAT, Items.NETHERITE_HELMET);
		netheriteSmithing(consumer, Items.DIAMOND_BOOTS, RecipeCategory.COMBAT, Items.NETHERITE_BOOTS);
		netheriteSmithing(consumer, Items.DIAMOND_SWORD, RecipeCategory.COMBAT, Items.NETHERITE_SWORD);
		netheriteSmithing(consumer, Items.DIAMOND_AXE, RecipeCategory.TOOLS, Items.NETHERITE_AXE);
		netheriteSmithing(consumer, Items.DIAMOND_PICKAXE, RecipeCategory.TOOLS, Items.NETHERITE_PICKAXE);
		netheriteSmithing(consumer, Items.DIAMOND_HOE, RecipeCategory.TOOLS, Items.NETHERITE_HOE);
		netheriteSmithing(consumer, Items.DIAMOND_SHOVEL, RecipeCategory.TOOLS, Items.NETHERITE_SHOVEL);
		copySmithingTemplate(consumer, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.NETHERRACK);
		copySmithingTemplate(consumer, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
		copySmithingTemplate(consumer, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SANDSTONE);
		copySmithingTemplate(consumer, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
		copySmithingTemplate(consumer, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.MOSSY_COBBLESTONE);
		copySmithingTemplate(consumer, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLED_DEEPSLATE);
		copySmithingTemplate(consumer, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.END_STONE);
		copySmithingTemplate(consumer, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
		copySmithingTemplate(consumer, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.PRISMARINE);
		copySmithingTemplate(consumer, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Items.BLACKSTONE);
		copySmithingTemplate(consumer, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.NETHERRACK);
		copySmithingTemplate(consumer, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.PURPUR_BLOCK);
	}
}
