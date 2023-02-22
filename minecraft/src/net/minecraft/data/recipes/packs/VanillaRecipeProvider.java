package net.minecraft.data.recipes.packs;

import com.google.common.collect.ImmutableList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class VanillaRecipeProvider extends RecipeProvider {
	private static final ImmutableList<ItemLike> COAL_SMELTABLES = ImmutableList.of(Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE);
	private static final ImmutableList<ItemLike> IRON_SMELTABLES = ImmutableList.of(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE, Items.RAW_IRON);
	private static final ImmutableList<ItemLike> COPPER_SMELTABLES = ImmutableList.of(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE, Items.RAW_COPPER);
	private static final ImmutableList<ItemLike> GOLD_SMELTABLES = ImmutableList.of(
		Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE, Items.NETHER_GOLD_ORE, Items.RAW_GOLD
	);
	private static final ImmutableList<ItemLike> DIAMOND_SMELTABLES = ImmutableList.of(Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE);
	private static final ImmutableList<ItemLike> LAPIS_SMELTABLES = ImmutableList.of(Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE);
	private static final ImmutableList<ItemLike> REDSTONE_SMELTABLES = ImmutableList.of(Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE);
	private static final ImmutableList<ItemLike> EMERALD_SMELTABLES = ImmutableList.of(Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE);

	public VanillaRecipeProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		return CompletableFuture.allOf(
			super.run(cachedOutput),
			this.buildAdvancement(
				cachedOutput, RecipeBuilder.ROOT_RECIPE_ADVANCEMENT, Advancement.Builder.advancement().addCriterion("impossible", new ImpossibleTrigger.TriggerInstance())
			)
		);
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		generateForEnabledBlockFamilies(consumer, FeatureFlagSet.of(FeatureFlags.VANILLA));
		planksFromLog(consumer, Blocks.ACACIA_PLANKS, ItemTags.ACACIA_LOGS, 4);
		planksFromLogs(consumer, Blocks.BIRCH_PLANKS, ItemTags.BIRCH_LOGS, 4);
		planksFromLogs(consumer, Blocks.CRIMSON_PLANKS, ItemTags.CRIMSON_STEMS, 4);
		planksFromLog(consumer, Blocks.DARK_OAK_PLANKS, ItemTags.DARK_OAK_LOGS, 4);
		planksFromLogs(consumer, Blocks.JUNGLE_PLANKS, ItemTags.JUNGLE_LOGS, 4);
		planksFromLogs(consumer, Blocks.OAK_PLANKS, ItemTags.OAK_LOGS, 4);
		planksFromLogs(consumer, Blocks.SPRUCE_PLANKS, ItemTags.SPRUCE_LOGS, 4);
		planksFromLogs(consumer, Blocks.WARPED_PLANKS, ItemTags.WARPED_STEMS, 4);
		planksFromLogs(consumer, Blocks.MANGROVE_PLANKS, ItemTags.MANGROVE_LOGS, 4);
		woodFromLogs(consumer, Blocks.ACACIA_WOOD, Blocks.ACACIA_LOG);
		woodFromLogs(consumer, Blocks.BIRCH_WOOD, Blocks.BIRCH_LOG);
		woodFromLogs(consumer, Blocks.DARK_OAK_WOOD, Blocks.DARK_OAK_LOG);
		woodFromLogs(consumer, Blocks.JUNGLE_WOOD, Blocks.JUNGLE_LOG);
		woodFromLogs(consumer, Blocks.OAK_WOOD, Blocks.OAK_LOG);
		woodFromLogs(consumer, Blocks.SPRUCE_WOOD, Blocks.SPRUCE_LOG);
		woodFromLogs(consumer, Blocks.CRIMSON_HYPHAE, Blocks.CRIMSON_STEM);
		woodFromLogs(consumer, Blocks.WARPED_HYPHAE, Blocks.WARPED_STEM);
		woodFromLogs(consumer, Blocks.MANGROVE_WOOD, Blocks.MANGROVE_LOG);
		woodFromLogs(consumer, Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_ACACIA_LOG);
		woodFromLogs(consumer, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG);
		woodFromLogs(consumer, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG);
		woodFromLogs(consumer, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_LOG);
		woodFromLogs(consumer, Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_OAK_LOG);
		woodFromLogs(consumer, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_LOG);
		woodFromLogs(consumer, Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_STEM);
		woodFromLogs(consumer, Blocks.STRIPPED_WARPED_HYPHAE, Blocks.STRIPPED_WARPED_STEM);
		woodFromLogs(consumer, Blocks.STRIPPED_MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_LOG);
		woodenBoat(consumer, Items.ACACIA_BOAT, Blocks.ACACIA_PLANKS);
		woodenBoat(consumer, Items.BIRCH_BOAT, Blocks.BIRCH_PLANKS);
		woodenBoat(consumer, Items.DARK_OAK_BOAT, Blocks.DARK_OAK_PLANKS);
		woodenBoat(consumer, Items.JUNGLE_BOAT, Blocks.JUNGLE_PLANKS);
		woodenBoat(consumer, Items.OAK_BOAT, Blocks.OAK_PLANKS);
		woodenBoat(consumer, Items.SPRUCE_BOAT, Blocks.SPRUCE_PLANKS);
		woodenBoat(consumer, Items.MANGROVE_BOAT, Blocks.MANGROVE_PLANKS);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.BLACK_WOOL, Items.BLACK_DYE);
		carpet(consumer, Blocks.BLACK_CARPET, Blocks.BLACK_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.BLACK_CARPET, Items.BLACK_DYE);
		bedFromPlanksAndWool(consumer, Items.BLACK_BED, Blocks.BLACK_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.BLACK_BED, Items.BLACK_DYE);
		banner(consumer, Items.BLACK_BANNER, Blocks.BLACK_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.BLUE_WOOL, Items.BLUE_DYE);
		carpet(consumer, Blocks.BLUE_CARPET, Blocks.BLUE_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.BLUE_CARPET, Items.BLUE_DYE);
		bedFromPlanksAndWool(consumer, Items.BLUE_BED, Blocks.BLUE_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.BLUE_BED, Items.BLUE_DYE);
		banner(consumer, Items.BLUE_BANNER, Blocks.BLUE_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.BROWN_WOOL, Items.BROWN_DYE);
		carpet(consumer, Blocks.BROWN_CARPET, Blocks.BROWN_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.BROWN_CARPET, Items.BROWN_DYE);
		bedFromPlanksAndWool(consumer, Items.BROWN_BED, Blocks.BROWN_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.BROWN_BED, Items.BROWN_DYE);
		banner(consumer, Items.BROWN_BANNER, Blocks.BROWN_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.CYAN_WOOL, Items.CYAN_DYE);
		carpet(consumer, Blocks.CYAN_CARPET, Blocks.CYAN_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.CYAN_CARPET, Items.CYAN_DYE);
		bedFromPlanksAndWool(consumer, Items.CYAN_BED, Blocks.CYAN_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.CYAN_BED, Items.CYAN_DYE);
		banner(consumer, Items.CYAN_BANNER, Blocks.CYAN_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.GRAY_WOOL, Items.GRAY_DYE);
		carpet(consumer, Blocks.GRAY_CARPET, Blocks.GRAY_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.GRAY_CARPET, Items.GRAY_DYE);
		bedFromPlanksAndWool(consumer, Items.GRAY_BED, Blocks.GRAY_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.GRAY_BED, Items.GRAY_DYE);
		banner(consumer, Items.GRAY_BANNER, Blocks.GRAY_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.GREEN_WOOL, Items.GREEN_DYE);
		carpet(consumer, Blocks.GREEN_CARPET, Blocks.GREEN_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.GREEN_CARPET, Items.GREEN_DYE);
		bedFromPlanksAndWool(consumer, Items.GREEN_BED, Blocks.GREEN_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.GREEN_BED, Items.GREEN_DYE);
		banner(consumer, Items.GREEN_BANNER, Blocks.GREEN_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.LIGHT_BLUE_WOOL, Items.LIGHT_BLUE_DYE);
		carpet(consumer, Blocks.LIGHT_BLUE_CARPET, Blocks.LIGHT_BLUE_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.LIGHT_BLUE_CARPET, Items.LIGHT_BLUE_DYE);
		bedFromPlanksAndWool(consumer, Items.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.LIGHT_BLUE_BED, Items.LIGHT_BLUE_DYE);
		banner(consumer, Items.LIGHT_BLUE_BANNER, Blocks.LIGHT_BLUE_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.LIGHT_GRAY_WOOL, Items.LIGHT_GRAY_DYE);
		carpet(consumer, Blocks.LIGHT_GRAY_CARPET, Blocks.LIGHT_GRAY_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.LIGHT_GRAY_CARPET, Items.LIGHT_GRAY_DYE);
		bedFromPlanksAndWool(consumer, Items.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.LIGHT_GRAY_BED, Items.LIGHT_GRAY_DYE);
		banner(consumer, Items.LIGHT_GRAY_BANNER, Blocks.LIGHT_GRAY_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.LIME_WOOL, Items.LIME_DYE);
		carpet(consumer, Blocks.LIME_CARPET, Blocks.LIME_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.LIME_CARPET, Items.LIME_DYE);
		bedFromPlanksAndWool(consumer, Items.LIME_BED, Blocks.LIME_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.LIME_BED, Items.LIME_DYE);
		banner(consumer, Items.LIME_BANNER, Blocks.LIME_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.MAGENTA_WOOL, Items.MAGENTA_DYE);
		carpet(consumer, Blocks.MAGENTA_CARPET, Blocks.MAGENTA_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.MAGENTA_CARPET, Items.MAGENTA_DYE);
		bedFromPlanksAndWool(consumer, Items.MAGENTA_BED, Blocks.MAGENTA_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.MAGENTA_BED, Items.MAGENTA_DYE);
		banner(consumer, Items.MAGENTA_BANNER, Blocks.MAGENTA_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.ORANGE_WOOL, Items.ORANGE_DYE);
		carpet(consumer, Blocks.ORANGE_CARPET, Blocks.ORANGE_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.ORANGE_CARPET, Items.ORANGE_DYE);
		bedFromPlanksAndWool(consumer, Items.ORANGE_BED, Blocks.ORANGE_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.ORANGE_BED, Items.ORANGE_DYE);
		banner(consumer, Items.ORANGE_BANNER, Blocks.ORANGE_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.PINK_WOOL, Items.PINK_DYE);
		carpet(consumer, Blocks.PINK_CARPET, Blocks.PINK_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.PINK_CARPET, Items.PINK_DYE);
		bedFromPlanksAndWool(consumer, Items.PINK_BED, Blocks.PINK_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.PINK_BED, Items.PINK_DYE);
		banner(consumer, Items.PINK_BANNER, Blocks.PINK_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.PURPLE_WOOL, Items.PURPLE_DYE);
		carpet(consumer, Blocks.PURPLE_CARPET, Blocks.PURPLE_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.PURPLE_CARPET, Items.PURPLE_DYE);
		bedFromPlanksAndWool(consumer, Items.PURPLE_BED, Blocks.PURPLE_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.PURPLE_BED, Items.PURPLE_DYE);
		banner(consumer, Items.PURPLE_BANNER, Blocks.PURPLE_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.RED_WOOL, Items.RED_DYE);
		carpet(consumer, Blocks.RED_CARPET, Blocks.RED_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.RED_CARPET, Items.RED_DYE);
		bedFromPlanksAndWool(consumer, Items.RED_BED, Blocks.RED_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.RED_BED, Items.RED_DYE);
		banner(consumer, Items.RED_BANNER, Blocks.RED_WOOL);
		carpet(consumer, Blocks.WHITE_CARPET, Blocks.WHITE_WOOL);
		bedFromPlanksAndWool(consumer, Items.WHITE_BED, Blocks.WHITE_WOOL);
		banner(consumer, Items.WHITE_BANNER, Blocks.WHITE_WOOL);
		coloredWoolFromWhiteWoolAndDye(consumer, Blocks.YELLOW_WOOL, Items.YELLOW_DYE);
		carpet(consumer, Blocks.YELLOW_CARPET, Blocks.YELLOW_WOOL);
		coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.YELLOW_CARPET, Items.YELLOW_DYE);
		bedFromPlanksAndWool(consumer, Items.YELLOW_BED, Blocks.YELLOW_WOOL);
		bedFromWhiteBedAndDye(consumer, Items.YELLOW_BED, Items.YELLOW_DYE);
		banner(consumer, Items.YELLOW_BANNER, Blocks.YELLOW_WOOL);
		carpet(consumer, Blocks.MOSS_CARPET, Blocks.MOSS_BLOCK);
		stainedGlassFromGlassAndDye(consumer, Blocks.BLACK_STAINED_GLASS, Items.BLACK_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.BLACK_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.BLACK_STAINED_GLASS_PANE, Items.BLACK_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.BLUE_STAINED_GLASS, Items.BLUE_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.BLUE_STAINED_GLASS_PANE, Items.BLUE_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.BROWN_STAINED_GLASS, Items.BROWN_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.BROWN_STAINED_GLASS_PANE, Items.BROWN_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.CYAN_STAINED_GLASS, Items.CYAN_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.CYAN_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.CYAN_STAINED_GLASS_PANE, Items.CYAN_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.GRAY_STAINED_GLASS, Items.GRAY_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.GRAY_STAINED_GLASS_PANE, Items.GRAY_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.GREEN_STAINED_GLASS, Items.GREEN_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.GREEN_STAINED_GLASS_PANE, Items.GREEN_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.LIGHT_BLUE_STAINED_GLASS, Items.LIGHT_BLUE_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Items.LIGHT_BLUE_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.LIGHT_GRAY_STAINED_GLASS, Items.LIGHT_GRAY_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Items.LIGHT_GRAY_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.LIME_STAINED_GLASS, Items.LIME_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.LIME_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.LIME_STAINED_GLASS_PANE, Items.LIME_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.MAGENTA_STAINED_GLASS, Items.MAGENTA_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.MAGENTA_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.MAGENTA_STAINED_GLASS_PANE, Items.MAGENTA_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.ORANGE_STAINED_GLASS, Items.ORANGE_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.ORANGE_STAINED_GLASS_PANE, Items.ORANGE_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.PINK_STAINED_GLASS, Items.PINK_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.PINK_STAINED_GLASS_PANE, Blocks.PINK_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.PINK_STAINED_GLASS_PANE, Items.PINK_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.PURPLE_STAINED_GLASS, Items.PURPLE_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.PURPLE_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.PURPLE_STAINED_GLASS_PANE, Items.PURPLE_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.RED_STAINED_GLASS, Items.RED_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.RED_STAINED_GLASS_PANE, Blocks.RED_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.RED_STAINED_GLASS_PANE, Items.RED_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.WHITE_STAINED_GLASS, Items.WHITE_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.WHITE_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.WHITE_STAINED_GLASS_PANE, Items.WHITE_DYE);
		stainedGlassFromGlassAndDye(consumer, Blocks.YELLOW_STAINED_GLASS, Items.YELLOW_DYE);
		stainedGlassPaneFromStainedGlass(consumer, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS);
		stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.YELLOW_STAINED_GLASS_PANE, Items.YELLOW_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.BLACK_TERRACOTTA, Items.BLACK_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.BLUE_TERRACOTTA, Items.BLUE_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.BROWN_TERRACOTTA, Items.BROWN_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.CYAN_TERRACOTTA, Items.CYAN_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.GRAY_TERRACOTTA, Items.GRAY_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.GREEN_TERRACOTTA, Items.GREEN_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.LIGHT_GRAY_TERRACOTTA, Items.LIGHT_GRAY_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.LIME_TERRACOTTA, Items.LIME_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.MAGENTA_TERRACOTTA, Items.MAGENTA_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.ORANGE_TERRACOTTA, Items.ORANGE_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.PINK_TERRACOTTA, Items.PINK_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.PURPLE_TERRACOTTA, Items.PURPLE_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.RED_TERRACOTTA, Items.RED_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.WHITE_TERRACOTTA, Items.WHITE_DYE);
		coloredTerracottaFromTerracottaAndDye(consumer, Blocks.YELLOW_TERRACOTTA, Items.YELLOW_DYE);
		concretePowder(consumer, Blocks.BLACK_CONCRETE_POWDER, Items.BLACK_DYE);
		concretePowder(consumer, Blocks.BLUE_CONCRETE_POWDER, Items.BLUE_DYE);
		concretePowder(consumer, Blocks.BROWN_CONCRETE_POWDER, Items.BROWN_DYE);
		concretePowder(consumer, Blocks.CYAN_CONCRETE_POWDER, Items.CYAN_DYE);
		concretePowder(consumer, Blocks.GRAY_CONCRETE_POWDER, Items.GRAY_DYE);
		concretePowder(consumer, Blocks.GREEN_CONCRETE_POWDER, Items.GREEN_DYE);
		concretePowder(consumer, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Items.LIGHT_BLUE_DYE);
		concretePowder(consumer, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_DYE);
		concretePowder(consumer, Blocks.LIME_CONCRETE_POWDER, Items.LIME_DYE);
		concretePowder(consumer, Blocks.MAGENTA_CONCRETE_POWDER, Items.MAGENTA_DYE);
		concretePowder(consumer, Blocks.ORANGE_CONCRETE_POWDER, Items.ORANGE_DYE);
		concretePowder(consumer, Blocks.PINK_CONCRETE_POWDER, Items.PINK_DYE);
		concretePowder(consumer, Blocks.PURPLE_CONCRETE_POWDER, Items.PURPLE_DYE);
		concretePowder(consumer, Blocks.RED_CONCRETE_POWDER, Items.RED_DYE);
		concretePowder(consumer, Blocks.WHITE_CONCRETE_POWDER, Items.WHITE_DYE);
		concretePowder(consumer, Blocks.YELLOW_CONCRETE_POWDER, Items.YELLOW_DYE);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.CANDLE)
			.define('S', Items.STRING)
			.define('H', Items.HONEYCOMB)
			.pattern("S")
			.pattern("H")
			.unlockedBy("has_string", has(Items.STRING))
			.unlockedBy("has_honeycomb", has(Items.HONEYCOMB))
			.save(consumer);
		candle(consumer, Blocks.BLACK_CANDLE, Items.BLACK_DYE);
		candle(consumer, Blocks.BLUE_CANDLE, Items.BLUE_DYE);
		candle(consumer, Blocks.BROWN_CANDLE, Items.BROWN_DYE);
		candle(consumer, Blocks.CYAN_CANDLE, Items.CYAN_DYE);
		candle(consumer, Blocks.GRAY_CANDLE, Items.GRAY_DYE);
		candle(consumer, Blocks.GREEN_CANDLE, Items.GREEN_DYE);
		candle(consumer, Blocks.LIGHT_BLUE_CANDLE, Items.LIGHT_BLUE_DYE);
		candle(consumer, Blocks.LIGHT_GRAY_CANDLE, Items.LIGHT_GRAY_DYE);
		candle(consumer, Blocks.LIME_CANDLE, Items.LIME_DYE);
		candle(consumer, Blocks.MAGENTA_CANDLE, Items.MAGENTA_DYE);
		candle(consumer, Blocks.ORANGE_CANDLE, Items.ORANGE_DYE);
		candle(consumer, Blocks.PINK_CANDLE, Items.PINK_DYE);
		candle(consumer, Blocks.PURPLE_CANDLE, Items.PURPLE_DYE);
		candle(consumer, Blocks.RED_CANDLE, Items.RED_DYE);
		candle(consumer, Blocks.WHITE_CANDLE, Items.WHITE_DYE);
		candle(consumer, Blocks.YELLOW_CANDLE, Items.YELLOW_DYE);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.PACKED_MUD, 1)
			.requires(Blocks.MUD)
			.requires(Items.WHEAT)
			.unlockedBy("has_mud", has(Blocks.MUD))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.MUD_BRICKS, 4)
			.define('#', Blocks.PACKED_MUD)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_packed_mud", has(Blocks.PACKED_MUD))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.MUDDY_MANGROVE_ROOTS, 1)
			.requires(Blocks.MUD)
			.requires(Items.MANGROVE_ROOTS)
			.unlockedBy("has_mangrove_roots", has(Blocks.MANGROVE_ROOTS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, Blocks.ACTIVATOR_RAIL, 6)
			.define('#', Blocks.REDSTONE_TORCH)
			.define('S', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("XSX")
			.pattern("X#X")
			.pattern("XSX")
			.unlockedBy("has_rail", has(Blocks.RAIL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.ANDESITE, 2)
			.requires(Blocks.DIORITE)
			.requires(Blocks.COBBLESTONE)
			.unlockedBy("has_stone", has(Blocks.DIORITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.ANVIL)
			.define('I', Blocks.IRON_BLOCK)
			.define('i', Items.IRON_INGOT)
			.pattern("III")
			.pattern(" i ")
			.pattern("iii")
			.unlockedBy("has_iron_block", has(Blocks.IRON_BLOCK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.ARMOR_STAND)
			.define('/', Items.STICK)
			.define('_', Blocks.SMOOTH_STONE_SLAB)
			.pattern("///")
			.pattern(" / ")
			.pattern("/_/")
			.unlockedBy("has_stone_slab", has(Blocks.SMOOTH_STONE_SLAB))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.ARROW, 4)
			.define('#', Items.STICK)
			.define('X', Items.FLINT)
			.define('Y', Items.FEATHER)
			.pattern("X")
			.pattern("#")
			.pattern("Y")
			.unlockedBy("has_feather", has(Items.FEATHER))
			.unlockedBy("has_flint", has(Items.FLINT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.BARREL, 1)
			.define('P', ItemTags.PLANKS)
			.define('S', ItemTags.WOODEN_SLABS)
			.pattern("PSP")
			.pattern("P P")
			.pattern("PSP")
			.unlockedBy("has_planks", has(ItemTags.PLANKS))
			.unlockedBy("has_wood_slab", has(ItemTags.WOODEN_SLABS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.BEACON)
			.define('S', Items.NETHER_STAR)
			.define('G', Blocks.GLASS)
			.define('O', Blocks.OBSIDIAN)
			.pattern("GGG")
			.pattern("GSG")
			.pattern("OOO")
			.unlockedBy("has_nether_star", has(Items.NETHER_STAR))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.BEEHIVE)
			.define('P', ItemTags.PLANKS)
			.define('H', Items.HONEYCOMB)
			.pattern("PPP")
			.pattern("HHH")
			.pattern("PPP")
			.unlockedBy("has_honeycomb", has(Items.HONEYCOMB))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.BEETROOT_SOUP)
			.requires(Items.BOWL)
			.requires(Items.BEETROOT, 6)
			.unlockedBy("has_beetroot", has(Items.BEETROOT))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BLACK_DYE)
			.requires(Items.INK_SAC)
			.group("black_dye")
			.unlockedBy("has_ink_sac", has(Items.INK_SAC))
			.save(consumer);
		oneToOneConversionRecipe(consumer, Items.BLACK_DYE, Blocks.WITHER_ROSE, "black_dye");
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BREWING, Items.BLAZE_POWDER, 2)
			.requires(Items.BLAZE_ROD)
			.unlockedBy("has_blaze_rod", has(Items.BLAZE_ROD))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BLUE_DYE)
			.requires(Items.LAPIS_LAZULI)
			.group("blue_dye")
			.unlockedBy("has_lapis_lazuli", has(Items.LAPIS_LAZULI))
			.save(consumer);
		oneToOneConversionRecipe(consumer, Items.BLUE_DYE, Blocks.CORNFLOWER, "blue_dye");
		threeByThreePacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.BLUE_ICE, Blocks.PACKED_ICE);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BONE_MEAL, 3)
			.requires(Items.BONE)
			.group("bonemeal")
			.unlockedBy("has_bone", has(Items.BONE))
			.save(consumer);
		nineBlockStorageRecipesRecipesWithCustomUnpacking(
			consumer, RecipeCategory.MISC, Items.BONE_MEAL, RecipeCategory.BUILDING_BLOCKS, Items.BONE_BLOCK, "bone_meal_from_bone_block", "bonemeal"
		);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BOOK)
			.requires(Items.PAPER, 3)
			.requires(Items.LEATHER)
			.unlockedBy("has_paper", has(Items.PAPER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.BOOKSHELF)
			.define('#', ItemTags.PLANKS)
			.define('X', Items.BOOK)
			.pattern("###")
			.pattern("XXX")
			.pattern("###")
			.unlockedBy("has_book", has(Items.BOOK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.BOW)
			.define('#', Items.STICK)
			.define('X', Items.STRING)
			.pattern(" #X")
			.pattern("# X")
			.pattern(" #X")
			.unlockedBy("has_string", has(Items.STRING))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.BOWL, 4)
			.define('#', ItemTags.PLANKS)
			.pattern("# #")
			.pattern(" # ")
			.unlockedBy("has_brown_mushroom", has(Blocks.BROWN_MUSHROOM))
			.unlockedBy("has_red_mushroom", has(Blocks.RED_MUSHROOM))
			.unlockedBy("has_mushroom_stew", has(Items.MUSHROOM_STEW))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.BREAD).define('#', Items.WHEAT).pattern("###").unlockedBy("has_wheat", has(Items.WHEAT)).save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, Blocks.BREWING_STAND)
			.define('B', Items.BLAZE_ROD)
			.define('#', ItemTags.STONE_CRAFTING_MATERIALS)
			.pattern(" B ")
			.pattern("###")
			.unlockedBy("has_blaze_rod", has(Items.BLAZE_ROD))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.BRICKS)
			.define('#', Items.BRICK)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_brick", has(Items.BRICK))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BROWN_DYE)
			.requires(Items.COCOA_BEANS)
			.group("brown_dye")
			.unlockedBy("has_cocoa_beans", has(Items.COCOA_BEANS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.BUCKET)
			.define('#', Items.IRON_INGOT)
			.pattern("# #")
			.pattern(" # ")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Blocks.CAKE)
			.define('A', Items.MILK_BUCKET)
			.define('B', Items.SUGAR)
			.define('C', Items.WHEAT)
			.define('E', Items.EGG)
			.pattern("AAA")
			.pattern("BEB")
			.pattern("CCC")
			.unlockedBy("has_egg", has(Items.EGG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.CAMPFIRE)
			.define('L', ItemTags.LOGS)
			.define('S', Items.STICK)
			.define('C', ItemTags.COALS)
			.pattern(" S ")
			.pattern("SCS")
			.pattern("LLL")
			.unlockedBy("has_stick", has(Items.STICK))
			.unlockedBy("has_coal", has(ItemTags.COALS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, Items.CARROT_ON_A_STICK)
			.define('#', Items.FISHING_ROD)
			.define('X', Items.CARROT)
			.pattern("# ")
			.pattern(" X")
			.unlockedBy("has_carrot", has(Items.CARROT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, Items.WARPED_FUNGUS_ON_A_STICK)
			.define('#', Items.FISHING_ROD)
			.define('X', Items.WARPED_FUNGUS)
			.pattern("# ")
			.pattern(" X")
			.unlockedBy("has_warped_fungus", has(Items.WARPED_FUNGUS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, Blocks.CAULDRON)
			.define('#', Items.IRON_INGOT)
			.pattern("# #")
			.pattern("# #")
			.pattern("###")
			.unlockedBy("has_water_bucket", has(Items.WATER_BUCKET))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.COMPOSTER)
			.define('#', ItemTags.WOODEN_SLABS)
			.pattern("# #")
			.pattern("# #")
			.pattern("###")
			.unlockedBy("has_wood_slab", has(ItemTags.WOODEN_SLABS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.CHEST)
			.define('#', ItemTags.PLANKS)
			.pattern("###")
			.pattern("# #")
			.pattern("###")
			.unlockedBy(
				"has_lots_of_items",
				new InventoryChangeTrigger.TriggerInstance(
					EntityPredicate.Composite.ANY, MinMaxBounds.Ints.atLeast(10), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, new ItemPredicate[0]
				)
			)
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, Items.CHEST_MINECART)
			.requires(Blocks.CHEST)
			.requires(Items.MINECART)
			.unlockedBy("has_minecart", has(Items.MINECART))
			.save(consumer);
		chestBoat(consumer, Items.ACACIA_CHEST_BOAT, Items.ACACIA_BOAT);
		chestBoat(consumer, Items.BIRCH_CHEST_BOAT, Items.BIRCH_BOAT);
		chestBoat(consumer, Items.DARK_OAK_CHEST_BOAT, Items.DARK_OAK_BOAT);
		chestBoat(consumer, Items.JUNGLE_CHEST_BOAT, Items.JUNGLE_BOAT);
		chestBoat(consumer, Items.OAK_CHEST_BOAT, Items.OAK_BOAT);
		chestBoat(consumer, Items.SPRUCE_CHEST_BOAT, Items.SPRUCE_BOAT);
		chestBoat(consumer, Items.MANGROVE_CHEST_BOAT, Items.MANGROVE_BOAT);
		chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_QUARTZ_BLOCK, Ingredient.of(Blocks.QUARTZ_SLAB))
			.unlockedBy("has_chiseled_quartz_block", has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
			.unlockedBy("has_quartz_pillar", has(Blocks.QUARTZ_PILLAR))
			.save(consumer);
		chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_STONE_BRICKS, Ingredient.of(Blocks.STONE_BRICK_SLAB))
			.unlockedBy("has_tag", has(ItemTags.STONE_BRICKS))
			.save(consumer);
		twoByTwoPacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CLAY, Items.CLAY_BALL);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.CLOCK)
			.define('#', Items.GOLD_INGOT)
			.define('X', Items.REDSTONE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlockedBy("has_redstone", has(Items.REDSTONE))
			.save(consumer);
		nineBlockStorageRecipes(consumer, RecipeCategory.MISC, Items.COAL, RecipeCategory.BUILDING_BLOCKS, Items.COAL_BLOCK);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.COARSE_DIRT, 4)
			.define('D', Blocks.DIRT)
			.define('G', Blocks.GRAVEL)
			.pattern("DG")
			.pattern("GD")
			.unlockedBy("has_gravel", has(Blocks.GRAVEL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.COMPARATOR)
			.define('#', Blocks.REDSTONE_TORCH)
			.define('X', Items.QUARTZ)
			.define('I', Blocks.STONE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern("III")
			.unlockedBy("has_quartz", has(Items.QUARTZ))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.COMPASS)
			.define('#', Items.IRON_INGOT)
			.define('X', Items.REDSTONE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlockedBy("has_redstone", has(Items.REDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.COOKIE, 8)
			.define('#', Items.WHEAT)
			.define('X', Items.COCOA_BEANS)
			.pattern("#X#")
			.unlockedBy("has_cocoa", has(Items.COCOA_BEANS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.CRAFTING_TABLE)
			.define('#', ItemTags.PLANKS)
			.pattern("##")
			.pattern("##")
			.unlockedBy("unlock_right_away", PlayerTrigger.TriggerInstance.tick())
			.showNotification(false)
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.CROSSBOW)
			.define('~', Items.STRING)
			.define('#', Items.STICK)
			.define('&', Items.IRON_INGOT)
			.define('$', Blocks.TRIPWIRE_HOOK)
			.pattern("#&#")
			.pattern("~$~")
			.pattern(" # ")
			.unlockedBy("has_string", has(Items.STRING))
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.unlockedBy("has_tripwire_hook", has(Blocks.TRIPWIRE_HOOK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.LOOM)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.STRING)
			.pattern("@@")
			.pattern("##")
			.unlockedBy("has_string", has(Items.STRING))
			.save(consumer);
		chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_RED_SANDSTONE, Ingredient.of(Blocks.RED_SANDSTONE_SLAB))
			.unlockedBy("has_red_sandstone", has(Blocks.RED_SANDSTONE))
			.unlockedBy("has_chiseled_red_sandstone", has(Blocks.CHISELED_RED_SANDSTONE))
			.unlockedBy("has_cut_red_sandstone", has(Blocks.CUT_RED_SANDSTONE))
			.save(consumer);
		chiseled(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_SANDSTONE, Blocks.SANDSTONE_SLAB);
		nineBlockStorageRecipesRecipesWithCustomUnpacking(
			consumer,
			RecipeCategory.MISC,
			Items.COPPER_INGOT,
			RecipeCategory.BUILDING_BLOCKS,
			Items.COPPER_BLOCK,
			getSimpleRecipeName(Items.COPPER_INGOT),
			getItemName(Items.COPPER_INGOT)
		);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.COPPER_INGOT, 9)
			.requires(Blocks.WAXED_COPPER_BLOCK)
			.group(getItemName(Items.COPPER_INGOT))
			.unlockedBy(getHasName(Blocks.WAXED_COPPER_BLOCK), has(Blocks.WAXED_COPPER_BLOCK))
			.save(consumer, getConversionRecipeName(Items.COPPER_INGOT, Blocks.WAXED_COPPER_BLOCK));
		waxRecipes(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.CYAN_DYE, 2)
			.requires(Items.BLUE_DYE)
			.requires(Items.GREEN_DYE)
			.unlockedBy("has_green_dye", has(Items.GREEN_DYE))
			.unlockedBy("has_blue_dye", has(Items.BLUE_DYE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.DARK_PRISMARINE)
			.define('S', Items.PRISMARINE_SHARD)
			.define('I', Items.BLACK_DYE)
			.pattern("SSS")
			.pattern("SIS")
			.pattern("SSS")
			.unlockedBy("has_prismarine_shard", has(Items.PRISMARINE_SHARD))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.DAYLIGHT_DETECTOR)
			.define('Q', Items.QUARTZ)
			.define('G', Blocks.GLASS)
			.define('W', Ingredient.of(ItemTags.WOODEN_SLABS))
			.pattern("GGG")
			.pattern("QQQ")
			.pattern("WWW")
			.unlockedBy("has_quartz", has(Items.QUARTZ))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICKS, 4)
			.define('S', Blocks.POLISHED_DEEPSLATE)
			.pattern("SS")
			.pattern("SS")
			.unlockedBy("has_polished_deepslate", has(Blocks.POLISHED_DEEPSLATE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILES, 4)
			.define('S', Blocks.DEEPSLATE_BRICKS)
			.pattern("SS")
			.pattern("SS")
			.unlockedBy("has_deepslate_bricks", has(Blocks.DEEPSLATE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, Blocks.DETECTOR_RAIL, 6)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.STONE_PRESSURE_PLATE)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("X#X")
			.pattern("XRX")
			.unlockedBy("has_rail", has(Blocks.RAIL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.DIAMOND_AXE)
			.define('#', Items.STICK)
			.define('X', Items.DIAMOND)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(consumer);
		nineBlockStorageRecipes(consumer, RecipeCategory.MISC, Items.DIAMOND, RecipeCategory.BUILDING_BLOCKS, Items.DIAMOND_BLOCK);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.DIAMOND_BOOTS)
			.define('X', Items.DIAMOND)
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.DIAMOND_CHESTPLATE)
			.define('X', Items.DIAMOND)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.DIAMOND_HELMET)
			.define('X', Items.DIAMOND)
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.DIAMOND_HOE)
			.define('#', Items.STICK)
			.define('X', Items.DIAMOND)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.DIAMOND_LEGGINGS)
			.define('X', Items.DIAMOND)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.DIAMOND_PICKAXE)
			.define('#', Items.STICK)
			.define('X', Items.DIAMOND)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.DIAMOND_SHOVEL)
			.define('#', Items.STICK)
			.define('X', Items.DIAMOND)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.DIAMOND_SWORD)
			.define('#', Items.STICK)
			.define('X', Items.DIAMOND)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.DIORITE, 2)
			.define('Q', Items.QUARTZ)
			.define('C', Blocks.COBBLESTONE)
			.pattern("CQ")
			.pattern("QC")
			.unlockedBy("has_quartz", has(Items.QUARTZ))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.DISPENSER)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.define('X', Items.BOW)
			.pattern("###")
			.pattern("#X#")
			.pattern("#R#")
			.unlockedBy("has_bow", has(Items.BOW))
			.save(consumer);
		twoByTwoPacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DRIPSTONE_BLOCK, Items.POINTED_DRIPSTONE);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.DROPPER)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.pattern("###")
			.pattern("# #")
			.pattern("#R#")
			.unlockedBy("has_redstone", has(Items.REDSTONE))
			.save(consumer);
		nineBlockStorageRecipes(consumer, RecipeCategory.MISC, Items.EMERALD, RecipeCategory.BUILDING_BLOCKS, Items.EMERALD_BLOCK);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.ENCHANTING_TABLE)
			.define('B', Items.BOOK)
			.define('#', Blocks.OBSIDIAN)
			.define('D', Items.DIAMOND)
			.pattern(" B ")
			.pattern("D#D")
			.pattern("###")
			.unlockedBy("has_obsidian", has(Blocks.OBSIDIAN))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.ENDER_CHEST)
			.define('#', Blocks.OBSIDIAN)
			.define('E', Items.ENDER_EYE)
			.pattern("###")
			.pattern("#E#")
			.pattern("###")
			.unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ENDER_EYE)
			.requires(Items.ENDER_PEARL)
			.requires(Items.BLAZE_POWDER)
			.unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICKS, 4)
			.define('#', Blocks.END_STONE)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_end_stone", has(Blocks.END_STONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.END_CRYSTAL)
			.define('T', Items.GHAST_TEAR)
			.define('E', Items.ENDER_EYE)
			.define('G', Blocks.GLASS)
			.pattern("GGG")
			.pattern("GEG")
			.pattern("GTG")
			.unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.END_ROD, 4)
			.define('#', Items.POPPED_CHORUS_FRUIT)
			.define('/', Items.BLAZE_ROD)
			.pattern("/")
			.pattern("#")
			.unlockedBy("has_chorus_fruit_popped", has(Items.POPPED_CHORUS_FRUIT))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BREWING, Items.FERMENTED_SPIDER_EYE)
			.requires(Items.SPIDER_EYE)
			.requires(Blocks.BROWN_MUSHROOM)
			.requires(Items.SUGAR)
			.unlockedBy("has_spider_eye", has(Items.SPIDER_EYE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.FIRE_CHARGE, 3)
			.requires(Items.GUNPOWDER)
			.requires(Items.BLAZE_POWDER)
			.requires(Ingredient.of(Items.COAL, Items.CHARCOAL))
			.unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.FIREWORK_ROCKET, 3)
			.requires(Items.GUNPOWDER)
			.requires(Items.PAPER)
			.unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
			.save(consumer, "firework_rocket_simple");
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.FISHING_ROD)
			.define('#', Items.STICK)
			.define('X', Items.STRING)
			.pattern("  #")
			.pattern(" #X")
			.pattern("# X")
			.unlockedBy("has_string", has(Items.STRING))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.FLINT_AND_STEEL)
			.requires(Items.IRON_INGOT)
			.requires(Items.FLINT)
			.unlockedBy("has_flint", has(Items.FLINT))
			.unlockedBy("has_obsidian", has(Blocks.OBSIDIAN))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.FLOWER_POT)
			.define('#', Items.BRICK)
			.pattern("# #")
			.pattern(" # ")
			.unlockedBy("has_brick", has(Items.BRICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.FURNACE)
			.define('#', ItemTags.STONE_CRAFTING_MATERIALS)
			.pattern("###")
			.pattern("# #")
			.pattern("###")
			.unlockedBy("has_cobblestone", has(ItemTags.STONE_CRAFTING_MATERIALS))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, Items.FURNACE_MINECART)
			.requires(Blocks.FURNACE)
			.requires(Items.MINECART)
			.unlockedBy("has_minecart", has(Items.MINECART))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, Items.GLASS_BOTTLE, 3)
			.define('#', Blocks.GLASS)
			.pattern("# #")
			.pattern(" # ")
			.unlockedBy("has_glass", has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.GLASS_PANE, 16)
			.define('#', Blocks.GLASS)
			.pattern("###")
			.pattern("###")
			.unlockedBy("has_glass", has(Blocks.GLASS))
			.save(consumer);
		twoByTwoPacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.GLOWSTONE, Items.GLOWSTONE_DUST);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, Items.GLOW_ITEM_FRAME)
			.requires(Items.ITEM_FRAME)
			.requires(Items.GLOW_INK_SAC)
			.unlockedBy("has_item_frame", has(Items.ITEM_FRAME))
			.unlockedBy("has_glow_ink_sac", has(Items.GLOW_INK_SAC))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.GOLDEN_APPLE)
			.define('#', Items.GOLD_INGOT)
			.define('X', Items.APPLE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.GOLDEN_AXE)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.GOLDEN_BOOTS)
			.define('X', Items.GOLD_INGOT)
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, Items.GOLDEN_CARROT)
			.define('#', Items.GOLD_NUGGET)
			.define('X', Items.CARROT)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_gold_nugget", has(Items.GOLD_NUGGET))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.GOLDEN_CHESTPLATE)
			.define('X', Items.GOLD_INGOT)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.GOLDEN_HELMET)
			.define('X', Items.GOLD_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.GOLDEN_HOE)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.GOLDEN_LEGGINGS)
			.define('X', Items.GOLD_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.GOLDEN_PICKAXE)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, Blocks.POWERED_RAIL, 6)
			.define('R', Items.REDSTONE)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("X X")
			.pattern("X#X")
			.pattern("XRX")
			.unlockedBy("has_rail", has(Blocks.RAIL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.GOLDEN_SHOVEL)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.GOLDEN_SWORD)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(consumer);
		nineBlockStorageRecipesRecipesWithCustomUnpacking(
			consumer, RecipeCategory.MISC, Items.GOLD_INGOT, RecipeCategory.BUILDING_BLOCKS, Items.GOLD_BLOCK, "gold_ingot_from_gold_block", "gold_ingot"
		);
		nineBlockStorageRecipesWithCustomPacking(
			consumer, RecipeCategory.MISC, Items.GOLD_NUGGET, RecipeCategory.MISC, Items.GOLD_INGOT, "gold_ingot_from_nuggets", "gold_ingot"
		);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.GRANITE)
			.requires(Blocks.DIORITE)
			.requires(Items.QUARTZ)
			.unlockedBy("has_quartz", has(Items.QUARTZ))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.GRAY_DYE, 2)
			.requires(Items.BLACK_DYE)
			.requires(Items.WHITE_DYE)
			.unlockedBy("has_white_dye", has(Items.WHITE_DYE))
			.unlockedBy("has_black_dye", has(Items.BLACK_DYE))
			.save(consumer);
		threeByThreePacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.HAY_BLOCK, Items.WHEAT);
		pressurePlate(consumer, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.IRON_INGOT);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.HONEY_BOTTLE, 4)
			.requires(Items.HONEY_BLOCK)
			.requires(Items.GLASS_BOTTLE, 4)
			.unlockedBy("has_honey_block", has(Blocks.HONEY_BLOCK))
			.save(consumer);
		twoByTwoPacker(consumer, RecipeCategory.REDSTONE, Blocks.HONEY_BLOCK, Items.HONEY_BOTTLE);
		twoByTwoPacker(consumer, RecipeCategory.DECORATIONS, Blocks.HONEYCOMB_BLOCK, Items.HONEYCOMB);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.HOPPER)
			.define('C', Blocks.CHEST)
			.define('I', Items.IRON_INGOT)
			.pattern("I I")
			.pattern("ICI")
			.pattern(" I ")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, Items.HOPPER_MINECART)
			.requires(Blocks.HOPPER)
			.requires(Items.MINECART)
			.unlockedBy("has_minecart", has(Items.MINECART))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.IRON_AXE)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.IRON_BARS, 16)
			.define('#', Items.IRON_INGOT)
			.pattern("###")
			.pattern("###")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.IRON_BOOTS)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.IRON_CHESTPLATE)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		doorBuilder(Blocks.IRON_DOOR, Ingredient.of(Items.IRON_INGOT)).unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT)).save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.IRON_HELMET)
			.define('X', Items.IRON_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.IRON_HOE)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		nineBlockStorageRecipesRecipesWithCustomUnpacking(
			consumer, RecipeCategory.MISC, Items.IRON_INGOT, RecipeCategory.BUILDING_BLOCKS, Items.IRON_BLOCK, "iron_ingot_from_iron_block", "iron_ingot"
		);
		nineBlockStorageRecipesWithCustomPacking(
			consumer, RecipeCategory.MISC, Items.IRON_NUGGET, RecipeCategory.MISC, Items.IRON_INGOT, "iron_ingot_from_nuggets", "iron_ingot"
		);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.IRON_LEGGINGS)
			.define('X', Items.IRON_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.IRON_PICKAXE)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.IRON_SHOVEL)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.IRON_SWORD)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		twoByTwoPacker(consumer, RecipeCategory.REDSTONE, Blocks.IRON_TRAPDOOR, Items.IRON_INGOT);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.ITEM_FRAME)
			.define('#', Items.STICK)
			.define('X', Items.LEATHER)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_leather", has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.JUKEBOX)
			.define('#', ItemTags.PLANKS)
			.define('X', Items.DIAMOND)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.LADDER, 3)
			.define('#', Items.STICK)
			.pattern("# #")
			.pattern("###")
			.pattern("# #")
			.unlockedBy("has_stick", has(Items.STICK))
			.save(consumer);
		nineBlockStorageRecipes(consumer, RecipeCategory.MISC, Items.LAPIS_LAZULI, RecipeCategory.BUILDING_BLOCKS, Items.LAPIS_BLOCK);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.LEAD, 2)
			.define('~', Items.STRING)
			.define('O', Items.SLIME_BALL)
			.pattern("~~ ")
			.pattern("~O ")
			.pattern("  ~")
			.unlockedBy("has_slime_ball", has(Items.SLIME_BALL))
			.save(consumer);
		twoByTwoPacker(consumer, RecipeCategory.MISC, Items.LEATHER, Items.RABBIT_HIDE);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.LEATHER_BOOTS)
			.define('X', Items.LEATHER)
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_leather", has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.LEATHER_CHESTPLATE)
			.define('X', Items.LEATHER)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlockedBy("has_leather", has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.LEATHER_HELMET)
			.define('X', Items.LEATHER)
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_leather", has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.LEATHER_LEGGINGS)
			.define('X', Items.LEATHER)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_leather", has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.LEATHER_HORSE_ARMOR)
			.define('X', Items.LEATHER)
			.pattern("X X")
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_leather", has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.LECTERN)
			.define('S', ItemTags.WOODEN_SLABS)
			.define('B', Blocks.BOOKSHELF)
			.pattern("SSS")
			.pattern(" B ")
			.pattern(" S ")
			.unlockedBy("has_book", has(Items.BOOK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.LEVER)
			.define('#', Blocks.COBBLESTONE)
			.define('X', Items.STICK)
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_cobblestone", has(Blocks.COBBLESTONE))
			.save(consumer);
		oneToOneConversionRecipe(consumer, Items.LIGHT_BLUE_DYE, Blocks.BLUE_ORCHID, "light_blue_dye");
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.LIGHT_BLUE_DYE, 2)
			.requires(Items.BLUE_DYE)
			.requires(Items.WHITE_DYE)
			.group("light_blue_dye")
			.unlockedBy("has_blue_dye", has(Items.BLUE_DYE))
			.unlockedBy("has_white_dye", has(Items.WHITE_DYE))
			.save(consumer, "light_blue_dye_from_blue_white_dye");
		oneToOneConversionRecipe(consumer, Items.LIGHT_GRAY_DYE, Blocks.AZURE_BLUET, "light_gray_dye");
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.LIGHT_GRAY_DYE, 2)
			.requires(Items.GRAY_DYE)
			.requires(Items.WHITE_DYE)
			.group("light_gray_dye")
			.unlockedBy("has_gray_dye", has(Items.GRAY_DYE))
			.unlockedBy("has_white_dye", has(Items.WHITE_DYE))
			.save(consumer, "light_gray_dye_from_gray_white_dye");
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.LIGHT_GRAY_DYE, 3)
			.requires(Items.BLACK_DYE)
			.requires(Items.WHITE_DYE, 2)
			.group("light_gray_dye")
			.unlockedBy("has_white_dye", has(Items.WHITE_DYE))
			.unlockedBy("has_black_dye", has(Items.BLACK_DYE))
			.save(consumer, "light_gray_dye_from_black_white_dye");
		oneToOneConversionRecipe(consumer, Items.LIGHT_GRAY_DYE, Blocks.OXEYE_DAISY, "light_gray_dye");
		oneToOneConversionRecipe(consumer, Items.LIGHT_GRAY_DYE, Blocks.WHITE_TULIP, "light_gray_dye");
		pressurePlate(consumer, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.GOLD_INGOT);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.LIGHTNING_ROD)
			.define('#', Items.COPPER_INGOT)
			.pattern("#")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.LIME_DYE, 2)
			.requires(Items.GREEN_DYE)
			.requires(Items.WHITE_DYE)
			.unlockedBy("has_green_dye", has(Items.GREEN_DYE))
			.unlockedBy("has_white_dye", has(Items.WHITE_DYE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.JACK_O_LANTERN)
			.define('A', Blocks.CARVED_PUMPKIN)
			.define('B', Blocks.TORCH)
			.pattern("A")
			.pattern("B")
			.unlockedBy("has_carved_pumpkin", has(Blocks.CARVED_PUMPKIN))
			.save(consumer);
		oneToOneConversionRecipe(consumer, Items.MAGENTA_DYE, Blocks.ALLIUM, "magenta_dye");
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.MAGENTA_DYE, 4)
			.requires(Items.BLUE_DYE)
			.requires(Items.RED_DYE, 2)
			.requires(Items.WHITE_DYE)
			.group("magenta_dye")
			.unlockedBy("has_blue_dye", has(Items.BLUE_DYE))
			.unlockedBy("has_rose_red", has(Items.RED_DYE))
			.unlockedBy("has_white_dye", has(Items.WHITE_DYE))
			.save(consumer, "magenta_dye_from_blue_red_white_dye");
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.MAGENTA_DYE, 3)
			.requires(Items.BLUE_DYE)
			.requires(Items.RED_DYE)
			.requires(Items.PINK_DYE)
			.group("magenta_dye")
			.unlockedBy("has_pink_dye", has(Items.PINK_DYE))
			.unlockedBy("has_blue_dye", has(Items.BLUE_DYE))
			.unlockedBy("has_red_dye", has(Items.RED_DYE))
			.save(consumer, "magenta_dye_from_blue_red_pink");
		oneToOneConversionRecipe(consumer, Items.MAGENTA_DYE, Blocks.LILAC, "magenta_dye", 2);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.MAGENTA_DYE, 2)
			.requires(Items.PURPLE_DYE)
			.requires(Items.PINK_DYE)
			.group("magenta_dye")
			.unlockedBy("has_pink_dye", has(Items.PINK_DYE))
			.unlockedBy("has_purple_dye", has(Items.PURPLE_DYE))
			.save(consumer, "magenta_dye_from_purple_and_pink");
		twoByTwoPacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.MAGMA_BLOCK, Items.MAGMA_CREAM);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BREWING, Items.MAGMA_CREAM)
			.requires(Items.BLAZE_POWDER)
			.requires(Items.SLIME_BALL)
			.unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.MAP)
			.define('#', Items.PAPER)
			.define('X', Items.COMPASS)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_compass", has(Items.COMPASS))
			.save(consumer);
		threeByThreePacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.MELON, Items.MELON_SLICE, "has_melon");
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.MELON_SEEDS)
			.requires(Items.MELON_SLICE)
			.unlockedBy("has_melon", has(Items.MELON_SLICE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, Items.MINECART)
			.define('#', Items.IRON_INGOT)
			.pattern("# #")
			.pattern("###")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_COBBLESTONE)
			.requires(Blocks.COBBLESTONE)
			.requires(Blocks.VINE)
			.group("mossy_cobblestone")
			.unlockedBy("has_vine", has(Blocks.VINE))
			.save(consumer, getConversionRecipeName(Blocks.MOSSY_COBBLESTONE, Blocks.VINE));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_STONE_BRICKS)
			.requires(Blocks.STONE_BRICKS)
			.requires(Blocks.VINE)
			.group("mossy_stone_bricks")
			.unlockedBy("has_vine", has(Blocks.VINE))
			.save(consumer, getConversionRecipeName(Blocks.MOSSY_STONE_BRICKS, Blocks.VINE));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_COBBLESTONE)
			.requires(Blocks.COBBLESTONE)
			.requires(Blocks.MOSS_BLOCK)
			.group("mossy_cobblestone")
			.unlockedBy("has_moss_block", has(Blocks.MOSS_BLOCK))
			.save(consumer, getConversionRecipeName(Blocks.MOSSY_COBBLESTONE, Blocks.MOSS_BLOCK));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_STONE_BRICKS)
			.requires(Blocks.STONE_BRICKS)
			.requires(Blocks.MOSS_BLOCK)
			.group("mossy_stone_bricks")
			.unlockedBy("has_moss_block", has(Blocks.MOSS_BLOCK))
			.save(consumer, getConversionRecipeName(Blocks.MOSSY_STONE_BRICKS, Blocks.MOSS_BLOCK));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.MUSHROOM_STEW)
			.requires(Blocks.BROWN_MUSHROOM)
			.requires(Blocks.RED_MUSHROOM)
			.requires(Items.BOWL)
			.unlockedBy("has_mushroom_stew", has(Items.MUSHROOM_STEW))
			.unlockedBy("has_bowl", has(Items.BOWL))
			.unlockedBy("has_brown_mushroom", has(Blocks.BROWN_MUSHROOM))
			.unlockedBy("has_red_mushroom", has(Blocks.RED_MUSHROOM))
			.save(consumer);
		twoByTwoPacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.NETHER_BRICKS, Items.NETHER_BRICK);
		threeByThreePacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.NETHER_WART_BLOCK, Items.NETHER_WART);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.NOTE_BLOCK)
			.define('#', ItemTags.PLANKS)
			.define('X', Items.REDSTONE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_redstone", has(Items.REDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.OBSERVER)
			.define('Q', Items.QUARTZ)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.pattern("###")
			.pattern("RRQ")
			.pattern("###")
			.unlockedBy("has_quartz", has(Items.QUARTZ))
			.save(consumer);
		oneToOneConversionRecipe(consumer, Items.ORANGE_DYE, Blocks.ORANGE_TULIP, "orange_dye");
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ORANGE_DYE, 2)
			.requires(Items.RED_DYE)
			.requires(Items.YELLOW_DYE)
			.group("orange_dye")
			.unlockedBy("has_red_dye", has(Items.RED_DYE))
			.unlockedBy("has_yellow_dye", has(Items.YELLOW_DYE))
			.save(consumer, "orange_dye_from_red_yellow");
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.PAINTING)
			.define('#', Items.STICK)
			.define('X', Ingredient.of(ItemTags.WOOL))
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_wool", has(ItemTags.WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.PAPER, 3)
			.define('#', Blocks.SUGAR_CANE)
			.pattern("###")
			.unlockedBy("has_reeds", has(Blocks.SUGAR_CANE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_PILLAR, 2)
			.define('#', Blocks.QUARTZ_BLOCK)
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_chiseled_quartz_block", has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
			.unlockedBy("has_quartz_pillar", has(Blocks.QUARTZ_PILLAR))
			.save(consumer);
		threeByThreePacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.PACKED_ICE, Blocks.ICE);
		oneToOneConversionRecipe(consumer, Items.PINK_DYE, Blocks.PEONY, "pink_dye", 2);
		oneToOneConversionRecipe(consumer, Items.PINK_DYE, Blocks.PINK_TULIP, "pink_dye");
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.PINK_DYE, 2)
			.requires(Items.RED_DYE)
			.requires(Items.WHITE_DYE)
			.group("pink_dye")
			.unlockedBy("has_white_dye", has(Items.WHITE_DYE))
			.unlockedBy("has_red_dye", has(Items.RED_DYE))
			.save(consumer, "pink_dye_from_red_white_dye");
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.PISTON)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.define('T', ItemTags.PLANKS)
			.define('X', Items.IRON_INGOT)
			.pattern("TTT")
			.pattern("#X#")
			.pattern("#R#")
			.unlockedBy("has_redstone", has(Items.REDSTONE))
			.save(consumer);
		polished(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BASALT, Blocks.BASALT);
		twoByTwoPacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE, Items.PRISMARINE_SHARD);
		threeByThreePacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE_BRICKS, Items.PRISMARINE_SHARD);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.PUMPKIN_PIE)
			.requires(Blocks.PUMPKIN)
			.requires(Items.SUGAR)
			.requires(Items.EGG)
			.unlockedBy("has_carved_pumpkin", has(Blocks.CARVED_PUMPKIN))
			.unlockedBy("has_pumpkin", has(Blocks.PUMPKIN))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.PUMPKIN_SEEDS, 4)
			.requires(Blocks.PUMPKIN)
			.unlockedBy("has_pumpkin", has(Blocks.PUMPKIN))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.PURPLE_DYE, 2)
			.requires(Items.BLUE_DYE)
			.requires(Items.RED_DYE)
			.unlockedBy("has_blue_dye", has(Items.BLUE_DYE))
			.unlockedBy("has_red_dye", has(Items.RED_DYE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.SHULKER_BOX)
			.define('#', Blocks.CHEST)
			.define('-', Items.SHULKER_SHELL)
			.pattern("-")
			.pattern("#")
			.pattern("-")
			.unlockedBy("has_shulker_shell", has(Items.SHULKER_SHELL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_BLOCK, 4)
			.define('F', Items.POPPED_CHORUS_FRUIT)
			.pattern("FF")
			.pattern("FF")
			.unlockedBy("has_chorus_fruit_popped", has(Items.POPPED_CHORUS_FRUIT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_PILLAR)
			.define('#', Blocks.PURPUR_SLAB)
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_purpur_block", has(Blocks.PURPUR_BLOCK))
			.save(consumer);
		slabBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_SLAB, Ingredient.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR))
			.unlockedBy("has_purpur_block", has(Blocks.PURPUR_BLOCK))
			.save(consumer);
		stairBuilder(Blocks.PURPUR_STAIRS, Ingredient.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR))
			.unlockedBy("has_purpur_block", has(Blocks.PURPUR_BLOCK))
			.save(consumer);
		twoByTwoPacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_BLOCK, Items.QUARTZ);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_BRICKS, 4)
			.define('#', Blocks.QUARTZ_BLOCK)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
			.save(consumer);
		slabBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_SLAB, Ingredient.of(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR))
			.unlockedBy("has_chiseled_quartz_block", has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
			.unlockedBy("has_quartz_pillar", has(Blocks.QUARTZ_PILLAR))
			.save(consumer);
		stairBuilder(Blocks.QUARTZ_STAIRS, Ingredient.of(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR))
			.unlockedBy("has_chiseled_quartz_block", has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
			.unlockedBy("has_quartz_pillar", has(Blocks.QUARTZ_PILLAR))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.RABBIT_STEW)
			.requires(Items.BAKED_POTATO)
			.requires(Items.COOKED_RABBIT)
			.requires(Items.BOWL)
			.requires(Items.CARROT)
			.requires(Blocks.BROWN_MUSHROOM)
			.group("rabbit_stew")
			.unlockedBy("has_cooked_rabbit", has(Items.COOKED_RABBIT))
			.save(consumer, getConversionRecipeName(Items.RABBIT_STEW, Items.BROWN_MUSHROOM));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.RABBIT_STEW)
			.requires(Items.BAKED_POTATO)
			.requires(Items.COOKED_RABBIT)
			.requires(Items.BOWL)
			.requires(Items.CARROT)
			.requires(Blocks.RED_MUSHROOM)
			.group("rabbit_stew")
			.unlockedBy("has_cooked_rabbit", has(Items.COOKED_RABBIT))
			.save(consumer, getConversionRecipeName(Items.RABBIT_STEW, Items.RED_MUSHROOM));
		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, Blocks.RAIL, 16)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("X#X")
			.pattern("X X")
			.unlockedBy("has_minecart", has(Items.MINECART))
			.save(consumer);
		nineBlockStorageRecipes(consumer, RecipeCategory.REDSTONE, Items.REDSTONE, RecipeCategory.REDSTONE, Items.REDSTONE_BLOCK);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.REDSTONE_LAMP)
			.define('R', Items.REDSTONE)
			.define('G', Blocks.GLOWSTONE)
			.pattern(" R ")
			.pattern("RGR")
			.pattern(" R ")
			.unlockedBy("has_glowstone", has(Blocks.GLOWSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.REDSTONE_TORCH)
			.define('#', Items.STICK)
			.define('X', Items.REDSTONE)
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_redstone", has(Items.REDSTONE))
			.save(consumer);
		oneToOneConversionRecipe(consumer, Items.RED_DYE, Items.BEETROOT, "red_dye");
		oneToOneConversionRecipe(consumer, Items.RED_DYE, Blocks.POPPY, "red_dye");
		oneToOneConversionRecipe(consumer, Items.RED_DYE, Blocks.ROSE_BUSH, "red_dye", 2);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.RED_DYE)
			.requires(Blocks.RED_TULIP)
			.group("red_dye")
			.unlockedBy("has_red_flower", has(Blocks.RED_TULIP))
			.save(consumer, "red_dye_from_tulip");
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.RED_NETHER_BRICKS)
			.define('W', Items.NETHER_WART)
			.define('N', Items.NETHER_BRICK)
			.pattern("NW")
			.pattern("WN")
			.unlockedBy("has_nether_wart", has(Items.NETHER_WART))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.RED_SANDSTONE)
			.define('#', Blocks.RED_SAND)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_sand", has(Blocks.RED_SAND))
			.save(consumer);
		slabBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.RED_SANDSTONE_SLAB, Ingredient.of(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE))
			.unlockedBy("has_red_sandstone", has(Blocks.RED_SANDSTONE))
			.unlockedBy("has_chiseled_red_sandstone", has(Blocks.CHISELED_RED_SANDSTONE))
			.save(consumer);
		stairBuilder(Blocks.RED_SANDSTONE_STAIRS, Ingredient.of(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE))
			.unlockedBy("has_red_sandstone", has(Blocks.RED_SANDSTONE))
			.unlockedBy("has_chiseled_red_sandstone", has(Blocks.CHISELED_RED_SANDSTONE))
			.unlockedBy("has_cut_red_sandstone", has(Blocks.CUT_RED_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.REPEATER)
			.define('#', Blocks.REDSTONE_TORCH)
			.define('X', Items.REDSTONE)
			.define('I', Blocks.STONE)
			.pattern("#X#")
			.pattern("III")
			.unlockedBy("has_redstone_torch", has(Blocks.REDSTONE_TORCH))
			.save(consumer);
		twoByTwoPacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SANDSTONE, Blocks.SAND);
		slabBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.SANDSTONE_SLAB, Ingredient.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE))
			.unlockedBy("has_sandstone", has(Blocks.SANDSTONE))
			.unlockedBy("has_chiseled_sandstone", has(Blocks.CHISELED_SANDSTONE))
			.save(consumer);
		stairBuilder(Blocks.SANDSTONE_STAIRS, Ingredient.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE))
			.unlockedBy("has_sandstone", has(Blocks.SANDSTONE))
			.unlockedBy("has_chiseled_sandstone", has(Blocks.CHISELED_SANDSTONE))
			.unlockedBy("has_cut_sandstone", has(Blocks.CUT_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.SEA_LANTERN)
			.define('S', Items.PRISMARINE_SHARD)
			.define('C', Items.PRISMARINE_CRYSTALS)
			.pattern("SCS")
			.pattern("CCC")
			.pattern("SCS")
			.unlockedBy("has_prismarine_crystals", has(Items.PRISMARINE_CRYSTALS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.SHEARS)
			.define('#', Items.IRON_INGOT)
			.pattern(" #")
			.pattern("# ")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.SHIELD)
			.define('W', ItemTags.PLANKS)
			.define('o', Items.IRON_INGOT)
			.pattern("WoW")
			.pattern("WWW")
			.pattern(" W ")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		nineBlockStorageRecipes(consumer, RecipeCategory.MISC, Items.SLIME_BALL, RecipeCategory.REDSTONE, Items.SLIME_BLOCK);
		cut(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_RED_SANDSTONE, Blocks.RED_SANDSTONE);
		cut(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_SANDSTONE, Blocks.SANDSTONE);
		twoByTwoPacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SNOW_BLOCK, Items.SNOWBALL);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.SNOW, 6)
			.define('#', Blocks.SNOW_BLOCK)
			.pattern("###")
			.unlockedBy("has_snowball", has(Items.SNOWBALL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.SOUL_CAMPFIRE)
			.define('L', ItemTags.LOGS)
			.define('S', Items.STICK)
			.define('#', ItemTags.SOUL_FIRE_BASE_BLOCKS)
			.pattern(" S ")
			.pattern("S#S")
			.pattern("LLL")
			.unlockedBy("has_soul_sand", has(ItemTags.SOUL_FIRE_BASE_BLOCKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, Items.GLISTERING_MELON_SLICE)
			.define('#', Items.GOLD_NUGGET)
			.define('X', Items.MELON_SLICE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_melon", has(Items.MELON_SLICE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.SPECTRAL_ARROW, 2)
			.define('#', Items.GLOWSTONE_DUST)
			.define('X', Items.ARROW)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlockedBy("has_glowstone_dust", has(Items.GLOWSTONE_DUST))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.SPYGLASS)
			.define('#', Items.AMETHYST_SHARD)
			.define('X', Items.COPPER_INGOT)
			.pattern(" # ")
			.pattern(" X ")
			.pattern(" X ")
			.unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK, 4)
			.define('#', ItemTags.PLANKS)
			.pattern("#")
			.pattern("#")
			.group("sticks")
			.unlockedBy("has_planks", has(ItemTags.PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK, 1)
			.define('#', Blocks.BAMBOO)
			.pattern("#")
			.pattern("#")
			.group("sticks")
			.unlockedBy("has_bamboo", has(Blocks.BAMBOO))
			.save(consumer, "stick_from_bamboo_item");
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.STICKY_PISTON)
			.define('P', Blocks.PISTON)
			.define('S', Items.SLIME_BALL)
			.pattern("S")
			.pattern("P")
			.unlockedBy("has_slime_ball", has(Items.SLIME_BALL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICKS, 4)
			.define('#', Blocks.STONE)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_stone", has(Blocks.STONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.STONE_AXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
			.save(consumer);
		slabBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICK_SLAB, Ingredient.of(Blocks.STONE_BRICKS))
			.unlockedBy("has_stone_bricks", has(ItemTags.STONE_BRICKS))
			.save(consumer);
		stairBuilder(Blocks.STONE_BRICK_STAIRS, Ingredient.of(Blocks.STONE_BRICKS)).unlockedBy("has_stone_bricks", has(ItemTags.STONE_BRICKS)).save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.STONE_HOE)
			.define('#', Items.STICK)
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.STONE_PICKAXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.STONE_SHOVEL)
			.define('#', Items.STICK)
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
			.save(consumer);
		slab(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_STONE_SLAB, Blocks.SMOOTH_STONE);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.STONE_SWORD)
			.define('#', Items.STICK)
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.WHITE_WOOL)
			.define('#', Items.STRING)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_string", has(Items.STRING))
			.save(consumer, getConversionRecipeName(Blocks.WHITE_WOOL, Items.STRING));
		oneToOneConversionRecipe(consumer, Items.SUGAR, Blocks.SUGAR_CANE, "sugar");
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.SUGAR, 3)
			.requires(Items.HONEY_BOTTLE)
			.group("sugar")
			.unlockedBy("has_honey_bottle", has(Items.HONEY_BOTTLE))
			.save(consumer, getConversionRecipeName(Items.SUGAR, Items.HONEY_BOTTLE));
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.TARGET)
			.define('H', Items.HAY_BLOCK)
			.define('R', Items.REDSTONE)
			.pattern(" R ")
			.pattern("RHR")
			.pattern(" R ")
			.unlockedBy("has_redstone", has(Items.REDSTONE))
			.unlockedBy("has_hay_block", has(Blocks.HAY_BLOCK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.TNT)
			.define('#', Ingredient.of(Blocks.SAND, Blocks.RED_SAND))
			.define('X', Items.GUNPOWDER)
			.pattern("X#X")
			.pattern("#X#")
			.pattern("X#X")
			.unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, Items.TNT_MINECART)
			.requires(Blocks.TNT)
			.requires(Items.MINECART)
			.unlockedBy("has_minecart", has(Items.MINECART))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.TORCH, 4)
			.define('#', Items.STICK)
			.define('X', Ingredient.of(Items.COAL, Items.CHARCOAL))
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_stone_pickaxe", has(Items.STONE_PICKAXE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.SOUL_TORCH, 4)
			.define('X', Ingredient.of(Items.COAL, Items.CHARCOAL))
			.define('#', Items.STICK)
			.define('S', ItemTags.SOUL_FIRE_BASE_BLOCKS)
			.pattern("X")
			.pattern("#")
			.pattern("S")
			.unlockedBy("has_soul_sand", has(ItemTags.SOUL_FIRE_BASE_BLOCKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.LANTERN)
			.define('#', Items.TORCH)
			.define('X', Items.IRON_NUGGET)
			.pattern("XXX")
			.pattern("X#X")
			.pattern("XXX")
			.unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.SOUL_LANTERN)
			.define('#', Items.SOUL_TORCH)
			.define('X', Items.IRON_NUGGET)
			.pattern("XXX")
			.pattern("X#X")
			.pattern("XXX")
			.unlockedBy("has_soul_torch", has(Items.SOUL_TORCH))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, Blocks.TRAPPED_CHEST)
			.requires(Blocks.CHEST)
			.requires(Blocks.TRIPWIRE_HOOK)
			.unlockedBy("has_tripwire_hook", has(Blocks.TRIPWIRE_HOOK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.TRIPWIRE_HOOK, 2)
			.define('#', ItemTags.PLANKS)
			.define('S', Items.STICK)
			.define('I', Items.IRON_INGOT)
			.pattern("I")
			.pattern("S")
			.pattern("#")
			.unlockedBy("has_string", has(Items.STRING))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.TURTLE_HELMET)
			.define('X', Items.SCUTE)
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_scute", has(Items.SCUTE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WHEAT, 9)
			.requires(Blocks.HAY_BLOCK)
			.unlockedBy("has_hay_block", has(Blocks.HAY_BLOCK))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WHITE_DYE)
			.requires(Items.BONE_MEAL)
			.group("white_dye")
			.unlockedBy("has_bone_meal", has(Items.BONE_MEAL))
			.save(consumer);
		oneToOneConversionRecipe(consumer, Items.WHITE_DYE, Blocks.LILY_OF_THE_VALLEY, "white_dye");
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.WOODEN_AXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.PLANKS)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlockedBy("has_stick", has(Items.STICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.WOODEN_HOE)
			.define('#', Items.STICK)
			.define('X', ItemTags.PLANKS)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlockedBy("has_stick", has(Items.STICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.WOODEN_PICKAXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.PLANKS)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlockedBy("has_stick", has(Items.STICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.WOODEN_SHOVEL)
			.define('#', Items.STICK)
			.define('X', ItemTags.PLANKS)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_stick", has(Items.STICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.WOODEN_SWORD)
			.define('#', Items.STICK)
			.define('X', ItemTags.PLANKS)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_stick", has(Items.STICK))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WRITABLE_BOOK)
			.requires(Items.BOOK)
			.requires(Items.INK_SAC)
			.requires(Items.FEATHER)
			.unlockedBy("has_book", has(Items.BOOK))
			.save(consumer);
		oneToOneConversionRecipe(consumer, Items.YELLOW_DYE, Blocks.DANDELION, "yellow_dye");
		oneToOneConversionRecipe(consumer, Items.YELLOW_DYE, Blocks.SUNFLOWER, "yellow_dye", 2);
		nineBlockStorageRecipes(consumer, RecipeCategory.FOOD, Items.DRIED_KELP, RecipeCategory.BUILDING_BLOCKS, Items.DRIED_KELP_BLOCK);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.CONDUIT)
			.define('#', Items.NAUTILUS_SHELL)
			.define('X', Items.HEART_OF_THE_SEA)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_nautilus_core", has(Items.HEART_OF_THE_SEA))
			.unlockedBy("has_nautilus_shell", has(Items.NAUTILUS_SHELL))
			.save(consumer);
		wall(consumer, RecipeCategory.DECORATIONS, Blocks.RED_SANDSTONE_WALL, Blocks.RED_SANDSTONE);
		wall(consumer, RecipeCategory.DECORATIONS, Blocks.STONE_BRICK_WALL, Blocks.STONE_BRICKS);
		wall(consumer, RecipeCategory.DECORATIONS, Blocks.SANDSTONE_WALL, Blocks.SANDSTONE);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.CREEPER_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Items.CREEPER_HEAD)
			.unlockedBy("has_creeper_head", has(Items.CREEPER_HEAD))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.SKULL_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Items.WITHER_SKELETON_SKULL)
			.unlockedBy("has_wither_skeleton_skull", has(Items.WITHER_SKELETON_SKULL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.FLOWER_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Blocks.OXEYE_DAISY)
			.unlockedBy("has_oxeye_daisy", has(Blocks.OXEYE_DAISY))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.MOJANG_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Items.ENCHANTED_GOLDEN_APPLE)
			.unlockedBy("has_enchanted_golden_apple", has(Items.ENCHANTED_GOLDEN_APPLE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.SCAFFOLDING, 6)
			.define('~', Items.STRING)
			.define('I', Blocks.BAMBOO)
			.pattern("I~I")
			.pattern("I I")
			.pattern("I I")
			.unlockedBy("has_bamboo", has(Blocks.BAMBOO))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.GRINDSTONE)
			.define('I', Items.STICK)
			.define('-', Blocks.STONE_SLAB)
			.define('#', ItemTags.PLANKS)
			.pattern("I-I")
			.pattern("# #")
			.unlockedBy("has_stone_slab", has(Blocks.STONE_SLAB))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.BLAST_FURNACE)
			.define('#', Blocks.SMOOTH_STONE)
			.define('X', Blocks.FURNACE)
			.define('I', Items.IRON_INGOT)
			.pattern("III")
			.pattern("IXI")
			.pattern("###")
			.unlockedBy("has_smooth_stone", has(Blocks.SMOOTH_STONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.SMOKER)
			.define('#', ItemTags.LOGS)
			.define('X', Blocks.FURNACE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlockedBy("has_furnace", has(Blocks.FURNACE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.CARTOGRAPHY_TABLE)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.PAPER)
			.pattern("@@")
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_paper", has(Items.PAPER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.SMITHING_TABLE)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.IRON_INGOT)
			.pattern("@@")
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.FLETCHING_TABLE)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.FLINT)
			.pattern("@@")
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_flint", has(Items.FLINT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.STONECUTTER)
			.define('I', Items.IRON_INGOT)
			.define('#', Blocks.STONE)
			.pattern(" I ")
			.pattern("###")
			.unlockedBy("has_stone", has(Blocks.STONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.LODESTONE)
			.define('S', Items.CHISELED_STONE_BRICKS)
			.define('#', Items.NETHERITE_INGOT)
			.pattern("SSS")
			.pattern("S#S")
			.pattern("SSS")
			.unlockedBy("has_netherite_ingot", has(Items.NETHERITE_INGOT))
			.save(consumer);
		nineBlockStorageRecipesRecipesWithCustomUnpacking(
			consumer,
			RecipeCategory.MISC,
			Items.NETHERITE_INGOT,
			RecipeCategory.BUILDING_BLOCKS,
			Items.NETHERITE_BLOCK,
			"netherite_ingot_from_netherite_block",
			"netherite_ingot"
		);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.NETHERITE_INGOT)
			.requires(Items.NETHERITE_SCRAP, 4)
			.requires(Items.GOLD_INGOT, 4)
			.group("netherite_ingot")
			.unlockedBy("has_netherite_scrap", has(Items.NETHERITE_SCRAP))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.RESPAWN_ANCHOR)
			.define('O', Blocks.CRYING_OBSIDIAN)
			.define('G', Blocks.GLOWSTONE)
			.pattern("OOO")
			.pattern("GGG")
			.pattern("OOO")
			.unlockedBy("has_obsidian", has(Blocks.CRYING_OBSIDIAN))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Blocks.CHAIN)
			.define('I', Items.IRON_INGOT)
			.define('N', Items.IRON_NUGGET)
			.pattern("N")
			.pattern("I")
			.pattern("N")
			.unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.TINTED_GLASS, 2)
			.define('G', Blocks.GLASS)
			.define('S', Items.AMETHYST_SHARD)
			.pattern(" S ")
			.pattern("SGS")
			.pattern(" S ")
			.unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
			.save(consumer);
		twoByTwoPacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.AMETHYST_BLOCK, Items.AMETHYST_SHARD);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.RECOVERY_COMPASS)
			.define('C', Items.COMPASS)
			.define('S', Items.ECHO_SHARD)
			.pattern("SSS")
			.pattern("SCS")
			.pattern("SSS")
			.unlockedBy("has_echo_shard", has(Items.ECHO_SHARD))
			.save(consumer);
		threeByThreePacker(consumer, RecipeCategory.MISC, Items.MUSIC_DISC_5, Items.DISC_FRAGMENT_5);
		SpecialRecipeBuilder.special(RecipeSerializer.ARMOR_DYE).save(consumer, "armor_dye");
		SpecialRecipeBuilder.special(RecipeSerializer.BANNER_DUPLICATE).save(consumer, "banner_duplicate");
		SpecialRecipeBuilder.special(RecipeSerializer.BOOK_CLONING).save(consumer, "book_cloning");
		SpecialRecipeBuilder.special(RecipeSerializer.FIREWORK_ROCKET).save(consumer, "firework_rocket");
		SpecialRecipeBuilder.special(RecipeSerializer.FIREWORK_STAR).save(consumer, "firework_star");
		SpecialRecipeBuilder.special(RecipeSerializer.FIREWORK_STAR_FADE).save(consumer, "firework_star_fade");
		SpecialRecipeBuilder.special(RecipeSerializer.MAP_CLONING).save(consumer, "map_cloning");
		SpecialRecipeBuilder.special(RecipeSerializer.MAP_EXTENDING).save(consumer, "map_extending");
		SpecialRecipeBuilder.special(RecipeSerializer.REPAIR_ITEM).save(consumer, "repair_item");
		SpecialRecipeBuilder.special(RecipeSerializer.SHIELD_DECORATION).save(consumer, "shield_decoration");
		SpecialRecipeBuilder.special(RecipeSerializer.SHULKER_BOX_COLORING).save(consumer, "shulker_box_coloring");
		SpecialRecipeBuilder.special(RecipeSerializer.TIPPED_ARROW).save(consumer, "tipped_arrow");
		SpecialRecipeBuilder.special(RecipeSerializer.SUSPICIOUS_STEW).save(consumer, "suspicious_stew");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.POTATO), RecipeCategory.FOOD, Items.BAKED_POTATO, 0.35F, 200)
			.unlockedBy("has_potato", has(Items.POTATO))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CLAY_BALL), RecipeCategory.MISC, Items.BRICK, 0.3F, 200)
			.unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.LOGS_THAT_BURN), RecipeCategory.MISC, Items.CHARCOAL, 0.15F, 200)
			.unlockedBy("has_log", has(ItemTags.LOGS_THAT_BURN))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CHORUS_FRUIT), RecipeCategory.MISC, Items.POPPED_CHORUS_FRUIT, 0.1F, 200)
			.unlockedBy("has_chorus_fruit", has(Items.CHORUS_FRUIT))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.BEEF), RecipeCategory.FOOD, Items.COOKED_BEEF, 0.35F, 200)
			.unlockedBy("has_beef", has(Items.BEEF))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CHICKEN), RecipeCategory.FOOD, Items.COOKED_CHICKEN, 0.35F, 200)
			.unlockedBy("has_chicken", has(Items.CHICKEN))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.COD), RecipeCategory.FOOD, Items.COOKED_COD, 0.35F, 200)
			.unlockedBy("has_cod", has(Items.COD))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.KELP), RecipeCategory.FOOD, Items.DRIED_KELP, 0.1F, 200)
			.unlockedBy("has_kelp", has(Blocks.KELP))
			.save(consumer, getSmeltingRecipeName(Items.DRIED_KELP));
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.SALMON), RecipeCategory.FOOD, Items.COOKED_SALMON, 0.35F, 200)
			.unlockedBy("has_salmon", has(Items.SALMON))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.MUTTON), RecipeCategory.FOOD, Items.COOKED_MUTTON, 0.35F, 200)
			.unlockedBy("has_mutton", has(Items.MUTTON))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.PORKCHOP), RecipeCategory.FOOD, Items.COOKED_PORKCHOP, 0.35F, 200)
			.unlockedBy("has_porkchop", has(Items.PORKCHOP))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.RABBIT), RecipeCategory.FOOD, Items.COOKED_RABBIT, 0.35F, 200)
			.unlockedBy("has_rabbit", has(Items.RABBIT))
			.save(consumer);
		oreSmelting(consumer, COAL_SMELTABLES, RecipeCategory.MISC, Items.COAL, 0.1F, 200, "coal");
		oreSmelting(consumer, IRON_SMELTABLES, RecipeCategory.MISC, Items.IRON_INGOT, 0.7F, 200, "iron_ingot");
		oreSmelting(consumer, COPPER_SMELTABLES, RecipeCategory.MISC, Items.COPPER_INGOT, 0.7F, 200, "copper_ingot");
		oreSmelting(consumer, GOLD_SMELTABLES, RecipeCategory.MISC, Items.GOLD_INGOT, 1.0F, 200, "gold_ingot");
		oreSmelting(consumer, DIAMOND_SMELTABLES, RecipeCategory.MISC, Items.DIAMOND, 1.0F, 200, "diamond");
		oreSmelting(consumer, LAPIS_SMELTABLES, RecipeCategory.MISC, Items.LAPIS_LAZULI, 0.2F, 200, "lapis_lazuli");
		oreSmelting(consumer, REDSTONE_SMELTABLES, RecipeCategory.REDSTONE, Items.REDSTONE, 0.7F, 200, "redstone");
		oreSmelting(consumer, EMERALD_SMELTABLES, RecipeCategory.MISC, Items.EMERALD, 1.0F, 200, "emerald");
		nineBlockStorageRecipes(consumer, RecipeCategory.MISC, Items.RAW_IRON, RecipeCategory.BUILDING_BLOCKS, Items.RAW_IRON_BLOCK);
		nineBlockStorageRecipes(consumer, RecipeCategory.MISC, Items.RAW_COPPER, RecipeCategory.BUILDING_BLOCKS, Items.RAW_COPPER_BLOCK);
		nineBlockStorageRecipes(consumer, RecipeCategory.MISC, Items.RAW_GOLD, RecipeCategory.BUILDING_BLOCKS, Items.RAW_GOLD_BLOCK);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.SMELTS_TO_GLASS), RecipeCategory.BUILDING_BLOCKS, Blocks.GLASS.asItem(), 0.1F, 200)
			.unlockedBy("has_smelts_to_glass", has(ItemTags.SMELTS_TO_GLASS))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.SEA_PICKLE), RecipeCategory.MISC, Items.LIME_DYE, 0.1F, 200)
			.unlockedBy("has_sea_pickle", has(Blocks.SEA_PICKLE))
			.save(consumer, getSmeltingRecipeName(Items.LIME_DYE));
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CACTUS.asItem()), RecipeCategory.MISC, Items.GREEN_DYE, 1.0F, 200)
			.unlockedBy("has_cactus", has(Blocks.CACTUS))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(
				Ingredient.of(
					Items.GOLDEN_PICKAXE,
					Items.GOLDEN_SHOVEL,
					Items.GOLDEN_AXE,
					Items.GOLDEN_HOE,
					Items.GOLDEN_SWORD,
					Items.GOLDEN_HELMET,
					Items.GOLDEN_CHESTPLATE,
					Items.GOLDEN_LEGGINGS,
					Items.GOLDEN_BOOTS,
					Items.GOLDEN_HORSE_ARMOR
				),
				RecipeCategory.MISC,
				Items.GOLD_NUGGET,
				0.1F,
				200
			)
			.unlockedBy("has_golden_pickaxe", has(Items.GOLDEN_PICKAXE))
			.unlockedBy("has_golden_shovel", has(Items.GOLDEN_SHOVEL))
			.unlockedBy("has_golden_axe", has(Items.GOLDEN_AXE))
			.unlockedBy("has_golden_hoe", has(Items.GOLDEN_HOE))
			.unlockedBy("has_golden_sword", has(Items.GOLDEN_SWORD))
			.unlockedBy("has_golden_helmet", has(Items.GOLDEN_HELMET))
			.unlockedBy("has_golden_chestplate", has(Items.GOLDEN_CHESTPLATE))
			.unlockedBy("has_golden_leggings", has(Items.GOLDEN_LEGGINGS))
			.unlockedBy("has_golden_boots", has(Items.GOLDEN_BOOTS))
			.unlockedBy("has_golden_horse_armor", has(Items.GOLDEN_HORSE_ARMOR))
			.save(consumer, getSmeltingRecipeName(Items.GOLD_NUGGET));
		SimpleCookingRecipeBuilder.smelting(
				Ingredient.of(
					Items.IRON_PICKAXE,
					Items.IRON_SHOVEL,
					Items.IRON_AXE,
					Items.IRON_HOE,
					Items.IRON_SWORD,
					Items.IRON_HELMET,
					Items.IRON_CHESTPLATE,
					Items.IRON_LEGGINGS,
					Items.IRON_BOOTS,
					Items.IRON_HORSE_ARMOR,
					Items.CHAINMAIL_HELMET,
					Items.CHAINMAIL_CHESTPLATE,
					Items.CHAINMAIL_LEGGINGS,
					Items.CHAINMAIL_BOOTS
				),
				RecipeCategory.MISC,
				Items.IRON_NUGGET,
				0.1F,
				200
			)
			.unlockedBy("has_iron_pickaxe", has(Items.IRON_PICKAXE))
			.unlockedBy("has_iron_shovel", has(Items.IRON_SHOVEL))
			.unlockedBy("has_iron_axe", has(Items.IRON_AXE))
			.unlockedBy("has_iron_hoe", has(Items.IRON_HOE))
			.unlockedBy("has_iron_sword", has(Items.IRON_SWORD))
			.unlockedBy("has_iron_helmet", has(Items.IRON_HELMET))
			.unlockedBy("has_iron_chestplate", has(Items.IRON_CHESTPLATE))
			.unlockedBy("has_iron_leggings", has(Items.IRON_LEGGINGS))
			.unlockedBy("has_iron_boots", has(Items.IRON_BOOTS))
			.unlockedBy("has_iron_horse_armor", has(Items.IRON_HORSE_ARMOR))
			.unlockedBy("has_chainmail_helmet", has(Items.CHAINMAIL_HELMET))
			.unlockedBy("has_chainmail_chestplate", has(Items.CHAINMAIL_CHESTPLATE))
			.unlockedBy("has_chainmail_leggings", has(Items.CHAINMAIL_LEGGINGS))
			.unlockedBy("has_chainmail_boots", has(Items.CHAINMAIL_BOOTS))
			.save(consumer, getSmeltingRecipeName(Items.IRON_NUGGET));
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CLAY), RecipeCategory.BUILDING_BLOCKS, Blocks.TERRACOTTA.asItem(), 0.35F, 200)
			.unlockedBy("has_clay_block", has(Blocks.CLAY))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.NETHERRACK), RecipeCategory.MISC, Items.NETHER_BRICK, 0.1F, 200)
			.unlockedBy("has_netherrack", has(Blocks.NETHERRACK))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.NETHER_QUARTZ_ORE), RecipeCategory.MISC, Items.QUARTZ, 0.2F, 200)
			.unlockedBy("has_nether_quartz_ore", has(Blocks.NETHER_QUARTZ_ORE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.WET_SPONGE), RecipeCategory.BUILDING_BLOCKS, Blocks.SPONGE.asItem(), 0.15F, 200)
			.unlockedBy("has_wet_sponge", has(Blocks.WET_SPONGE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.COBBLESTONE), RecipeCategory.BUILDING_BLOCKS, Blocks.STONE.asItem(), 0.1F, 200)
			.unlockedBy("has_cobblestone", has(Blocks.COBBLESTONE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.STONE), RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_STONE.asItem(), 0.1F, 200)
			.unlockedBy("has_stone", has(Blocks.STONE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.SANDSTONE), RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_SANDSTONE.asItem(), 0.1F, 200)
			.unlockedBy("has_sandstone", has(Blocks.SANDSTONE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.RED_SANDSTONE), RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_RED_SANDSTONE.asItem(), 0.1F, 200)
			.unlockedBy("has_red_sandstone", has(Blocks.RED_SANDSTONE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.QUARTZ_BLOCK), RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_QUARTZ.asItem(), 0.1F, 200)
			.unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.STONE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_STONE_BRICKS.asItem(), 0.1F, 200)
			.unlockedBy("has_stone_bricks", has(Blocks.STONE_BRICKS))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BLACK_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.BLACK_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_black_terracotta", has(Blocks.BLACK_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BLUE_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.BLUE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_blue_terracotta", has(Blocks.BLUE_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BROWN_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.BROWN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_brown_terracotta", has(Blocks.BROWN_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CYAN_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.CYAN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_cyan_terracotta", has(Blocks.CYAN_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.GRAY_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.GRAY_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_gray_terracotta", has(Blocks.GRAY_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.GREEN_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.GREEN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_green_terracotta", has(Blocks.GREEN_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(
				Ingredient.of(Blocks.LIGHT_BLUE_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200
			)
			.unlockedBy("has_light_blue_terracotta", has(Blocks.LIGHT_BLUE_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(
				Ingredient.of(Blocks.LIGHT_GRAY_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.asItem(), 0.1F, 200
			)
			.unlockedBy("has_light_gray_terracotta", has(Blocks.LIGHT_GRAY_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LIME_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.LIME_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_lime_terracotta", has(Blocks.LIME_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(
				Ingredient.of(Blocks.MAGENTA_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.MAGENTA_GLAZED_TERRACOTTA.asItem(), 0.1F, 200
			)
			.unlockedBy("has_magenta_terracotta", has(Blocks.MAGENTA_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.ORANGE_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.ORANGE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_orange_terracotta", has(Blocks.ORANGE_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.PINK_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.PINK_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_pink_terracotta", has(Blocks.PINK_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.PURPLE_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.PURPLE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_purple_terracotta", has(Blocks.PURPLE_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.RED_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.RED_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_red_terracotta", has(Blocks.RED_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.WHITE_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.WHITE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_white_terracotta", has(Blocks.WHITE_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.YELLOW_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.YELLOW_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_yellow_terracotta", has(Blocks.YELLOW_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.ANCIENT_DEBRIS), RecipeCategory.MISC, Items.NETHERITE_SCRAP, 2.0F, 200)
			.unlockedBy("has_ancient_debris", has(Blocks.ANCIENT_DEBRIS))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BASALT), RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_BASALT, 0.1F, 200)
			.unlockedBy("has_basalt", has(Blocks.BASALT))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.COBBLED_DEEPSLATE), RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE, 0.1F, 200)
			.unlockedBy("has_cobbled_deepslate", has(Blocks.COBBLED_DEEPSLATE))
			.save(consumer);
		oreBlasting(consumer, COAL_SMELTABLES, RecipeCategory.MISC, Items.COAL, 0.1F, 100, "coal");
		oreBlasting(consumer, IRON_SMELTABLES, RecipeCategory.MISC, Items.IRON_INGOT, 0.7F, 100, "iron_ingot");
		oreBlasting(consumer, COPPER_SMELTABLES, RecipeCategory.MISC, Items.COPPER_INGOT, 0.7F, 100, "copper_ingot");
		oreBlasting(consumer, GOLD_SMELTABLES, RecipeCategory.MISC, Items.GOLD_INGOT, 1.0F, 100, "gold_ingot");
		oreBlasting(consumer, DIAMOND_SMELTABLES, RecipeCategory.MISC, Items.DIAMOND, 1.0F, 100, "diamond");
		oreBlasting(consumer, LAPIS_SMELTABLES, RecipeCategory.MISC, Items.LAPIS_LAZULI, 0.2F, 100, "lapis_lazuli");
		oreBlasting(consumer, REDSTONE_SMELTABLES, RecipeCategory.REDSTONE, Items.REDSTONE, 0.7F, 100, "redstone");
		oreBlasting(consumer, EMERALD_SMELTABLES, RecipeCategory.MISC, Items.EMERALD, 1.0F, 100, "emerald");
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.NETHER_QUARTZ_ORE), RecipeCategory.MISC, Items.QUARTZ, 0.2F, 100)
			.unlockedBy("has_nether_quartz_ore", has(Blocks.NETHER_QUARTZ_ORE))
			.save(consumer, getBlastingRecipeName(Items.QUARTZ));
		SimpleCookingRecipeBuilder.blasting(
				Ingredient.of(
					Items.GOLDEN_PICKAXE,
					Items.GOLDEN_SHOVEL,
					Items.GOLDEN_AXE,
					Items.GOLDEN_HOE,
					Items.GOLDEN_SWORD,
					Items.GOLDEN_HELMET,
					Items.GOLDEN_CHESTPLATE,
					Items.GOLDEN_LEGGINGS,
					Items.GOLDEN_BOOTS,
					Items.GOLDEN_HORSE_ARMOR
				),
				RecipeCategory.MISC,
				Items.GOLD_NUGGET,
				0.1F,
				100
			)
			.unlockedBy("has_golden_pickaxe", has(Items.GOLDEN_PICKAXE))
			.unlockedBy("has_golden_shovel", has(Items.GOLDEN_SHOVEL))
			.unlockedBy("has_golden_axe", has(Items.GOLDEN_AXE))
			.unlockedBy("has_golden_hoe", has(Items.GOLDEN_HOE))
			.unlockedBy("has_golden_sword", has(Items.GOLDEN_SWORD))
			.unlockedBy("has_golden_helmet", has(Items.GOLDEN_HELMET))
			.unlockedBy("has_golden_chestplate", has(Items.GOLDEN_CHESTPLATE))
			.unlockedBy("has_golden_leggings", has(Items.GOLDEN_LEGGINGS))
			.unlockedBy("has_golden_boots", has(Items.GOLDEN_BOOTS))
			.unlockedBy("has_golden_horse_armor", has(Items.GOLDEN_HORSE_ARMOR))
			.save(consumer, getBlastingRecipeName(Items.GOLD_NUGGET));
		SimpleCookingRecipeBuilder.blasting(
				Ingredient.of(
					Items.IRON_PICKAXE,
					Items.IRON_SHOVEL,
					Items.IRON_AXE,
					Items.IRON_HOE,
					Items.IRON_SWORD,
					Items.IRON_HELMET,
					Items.IRON_CHESTPLATE,
					Items.IRON_LEGGINGS,
					Items.IRON_BOOTS,
					Items.IRON_HORSE_ARMOR,
					Items.CHAINMAIL_HELMET,
					Items.CHAINMAIL_CHESTPLATE,
					Items.CHAINMAIL_LEGGINGS,
					Items.CHAINMAIL_BOOTS
				),
				RecipeCategory.MISC,
				Items.IRON_NUGGET,
				0.1F,
				100
			)
			.unlockedBy("has_iron_pickaxe", has(Items.IRON_PICKAXE))
			.unlockedBy("has_iron_shovel", has(Items.IRON_SHOVEL))
			.unlockedBy("has_iron_axe", has(Items.IRON_AXE))
			.unlockedBy("has_iron_hoe", has(Items.IRON_HOE))
			.unlockedBy("has_iron_sword", has(Items.IRON_SWORD))
			.unlockedBy("has_iron_helmet", has(Items.IRON_HELMET))
			.unlockedBy("has_iron_chestplate", has(Items.IRON_CHESTPLATE))
			.unlockedBy("has_iron_leggings", has(Items.IRON_LEGGINGS))
			.unlockedBy("has_iron_boots", has(Items.IRON_BOOTS))
			.unlockedBy("has_iron_horse_armor", has(Items.IRON_HORSE_ARMOR))
			.unlockedBy("has_chainmail_helmet", has(Items.CHAINMAIL_HELMET))
			.unlockedBy("has_chainmail_chestplate", has(Items.CHAINMAIL_CHESTPLATE))
			.unlockedBy("has_chainmail_leggings", has(Items.CHAINMAIL_LEGGINGS))
			.unlockedBy("has_chainmail_boots", has(Items.CHAINMAIL_BOOTS))
			.save(consumer, getBlastingRecipeName(Items.IRON_NUGGET));
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.ANCIENT_DEBRIS), RecipeCategory.MISC, Items.NETHERITE_SCRAP, 2.0F, 100)
			.unlockedBy("has_ancient_debris", has(Blocks.ANCIENT_DEBRIS))
			.save(consumer, getBlastingRecipeName(Items.NETHERITE_SCRAP));
		cookRecipes(consumer, "smoking", RecipeSerializer.SMOKING_RECIPE, 100);
		cookRecipes(consumer, "campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING_RECIPE, 600);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_SLAB, Blocks.STONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_STAIRS, Blocks.STONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICKS, Blocks.STONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICK_SLAB, Blocks.STONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICK_STAIRS, Blocks.STONE);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_STONE_BRICKS)
			.unlockedBy("has_stone", has(Blocks.STONE))
			.save(consumer, "chiseled_stone_bricks_stone_from_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), RecipeCategory.DECORATIONS, Blocks.STONE_BRICK_WALL)
			.unlockedBy("has_stone", has(Blocks.STONE))
			.save(consumer, "stone_brick_walls_from_stone_stonecutting");
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_SANDSTONE, Blocks.SANDSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SANDSTONE_SLAB, Blocks.SANDSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_SANDSTONE_SLAB, Blocks.SANDSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_SANDSTONE_SLAB, Blocks.CUT_SANDSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SANDSTONE_STAIRS, Blocks.SANDSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.SANDSTONE_WALL, Blocks.SANDSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_SANDSTONE, Blocks.SANDSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_RED_SANDSTONE, Blocks.RED_SANDSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.CUT_RED_SANDSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.RED_SANDSTONE_STAIRS, Blocks.RED_SANDSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.RED_SANDSTONE_WALL, Blocks.RED_SANDSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_RED_SANDSTONE, Blocks.RED_SANDSTONE);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_SLAB, 2)
			.unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
			.save(consumer, "quartz_slab_from_stonecutting");
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_STAIRS, Blocks.QUARTZ_BLOCK);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_PILLAR, Blocks.QUARTZ_BLOCK);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_BLOCK);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.COBBLESTONE_STAIRS, Blocks.COBBLESTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.COBBLESTONE_SLAB, Blocks.COBBLESTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.COBBLESTONE_WALL, Blocks.COBBLESTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICK_SLAB, Blocks.STONE_BRICKS, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICKS);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE_BRICKS), RecipeCategory.DECORATIONS, Blocks.STONE_BRICK_WALL)
			.unlockedBy("has_stone_bricks", has(Blocks.STONE_BRICKS))
			.save(consumer, "stone_brick_wall_from_stone_bricks_stonecutting");
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_STONE_BRICKS, Blocks.STONE_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.BRICK_SLAB, Blocks.BRICKS, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.BRICK_STAIRS, Blocks.BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.BRICK_WALL, Blocks.BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.MUD_BRICK_SLAB, Blocks.MUD_BRICKS, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.MUD_BRICK_STAIRS, Blocks.MUD_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.MUD_BRICK_WALL, Blocks.MUD_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.NETHER_BRICK_SLAB, Blocks.NETHER_BRICKS, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.NETHER_BRICK_WALL, Blocks.NETHER_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_NETHER_BRICKS, Blocks.NETHER_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.RED_NETHER_BRICK_SLAB, Blocks.RED_NETHER_BRICKS, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.RED_NETHER_BRICK_STAIRS, Blocks.RED_NETHER_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.RED_NETHER_BRICK_WALL, Blocks.RED_NETHER_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_SLAB, Blocks.PURPUR_BLOCK, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_STAIRS, Blocks.PURPUR_BLOCK);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_PILLAR, Blocks.PURPUR_BLOCK);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE_SLAB, Blocks.PRISMARINE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE_STAIRS, Blocks.PRISMARINE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.PRISMARINE_WALL, Blocks.PRISMARINE);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE_BRICK_SLAB, 2)
			.unlockedBy("has_prismarine_brick", has(Blocks.PRISMARINE_BRICKS))
			.save(consumer, "prismarine_brick_slab_from_prismarine_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE_BRICK_STAIRS)
			.unlockedBy("has_prismarine_brick", has(Blocks.PRISMARINE_BRICKS))
			.save(consumer, "prismarine_brick_stairs_from_prismarine_stonecutting");
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DARK_PRISMARINE_SLAB, Blocks.DARK_PRISMARINE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DARK_PRISMARINE_STAIRS, Blocks.DARK_PRISMARINE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.ANDESITE_SLAB, Blocks.ANDESITE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.ANDESITE_STAIRS, Blocks.ANDESITE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.ANDESITE_WALL, Blocks.ANDESITE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_ANDESITE, Blocks.ANDESITE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_ANDESITE_SLAB, Blocks.ANDESITE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_ANDESITE_STAIRS, Blocks.ANDESITE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_ANDESITE_SLAB, Blocks.POLISHED_ANDESITE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_ANDESITE_STAIRS, Blocks.POLISHED_ANDESITE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BASALT, Blocks.BASALT);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.GRANITE_SLAB, Blocks.GRANITE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.GRANITE_STAIRS, Blocks.GRANITE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.GRANITE_WALL, Blocks.GRANITE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_GRANITE, Blocks.GRANITE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_GRANITE_SLAB, Blocks.GRANITE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_GRANITE_STAIRS, Blocks.GRANITE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_GRANITE_SLAB, Blocks.POLISHED_GRANITE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_GRANITE_STAIRS, Blocks.POLISHED_GRANITE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DIORITE_SLAB, Blocks.DIORITE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DIORITE_STAIRS, Blocks.DIORITE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.DIORITE_WALL, Blocks.DIORITE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DIORITE, Blocks.DIORITE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DIORITE_SLAB, Blocks.DIORITE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DIORITE_STAIRS, Blocks.DIORITE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DIORITE_SLAB, Blocks.POLISHED_DIORITE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DIORITE_STAIRS, Blocks.POLISHED_DIORITE);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_STONE_BRICK_SLAB, 2)
			.unlockedBy("has_mossy_stone_bricks", has(Blocks.MOSSY_STONE_BRICKS))
			.save(consumer, "mossy_stone_brick_slab_from_mossy_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_STONE_BRICK_STAIRS)
			.unlockedBy("has_mossy_stone_bricks", has(Blocks.MOSSY_STONE_BRICKS))
			.save(consumer, "mossy_stone_brick_stairs_from_mossy_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), RecipeCategory.DECORATIONS, Blocks.MOSSY_STONE_BRICK_WALL)
			.unlockedBy("has_mossy_stone_bricks", has(Blocks.MOSSY_STONE_BRICKS))
			.save(consumer, "mossy_stone_brick_wall_from_mossy_stone_brick_stonecutting");
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.MOSSY_COBBLESTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_COBBLESTONE_STAIRS, Blocks.MOSSY_COBBLESTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.MOSSY_COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_SANDSTONE_STAIRS, Blocks.SMOOTH_SANDSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_RED_SANDSTONE_STAIRS, Blocks.SMOOTH_RED_SANDSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_QUARTZ_SLAB, Blocks.SMOOTH_QUARTZ, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_QUARTZ_STAIRS, Blocks.SMOOTH_QUARTZ);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICK_SLAB, 2)
			.unlockedBy("has_end_stone_brick", has(Blocks.END_STONE_BRICKS))
			.save(consumer, "end_stone_brick_slab_from_end_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICK_STAIRS)
			.unlockedBy("has_end_stone_brick", has(Blocks.END_STONE_BRICKS))
			.save(consumer, "end_stone_brick_stairs_from_end_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), RecipeCategory.DECORATIONS, Blocks.END_STONE_BRICK_WALL)
			.unlockedBy("has_end_stone_brick", has(Blocks.END_STONE_BRICKS))
			.save(consumer, "end_stone_brick_wall_from_end_stone_brick_stonecutting");
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICKS, Blocks.END_STONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICK_SLAB, Blocks.END_STONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICK_STAIRS, Blocks.END_STONE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.END_STONE_BRICK_WALL, Blocks.END_STONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_STONE_SLAB, Blocks.SMOOTH_STONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.BLACKSTONE_SLAB, Blocks.BLACKSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.BLACKSTONE_STAIRS, Blocks.BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.BLACKSTONE_WALL, Blocks.BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE, Blocks.BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.POLISHED_BLACKSTONE_WALL, Blocks.BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.BLACKSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_STAIRS, Blocks.BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_POLISHED_BLACKSTONE, Blocks.BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.BLACKSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_STAIRS, Blocks.POLISHED_BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.POLISHED_BLACKSTONE_WALL, Blocks.POLISHED_BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICKS, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_COPPER_SLAB, Blocks.CUT_COPPER, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_COPPER_STAIRS, Blocks.CUT_COPPER);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_CUT_COPPER);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_COPPER, Blocks.COPPER_BLOCK, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_COPPER_STAIRS, Blocks.COPPER_BLOCK, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_COPPER_SLAB, Blocks.COPPER_BLOCK, 8);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CUT_COPPER, Blocks.EXPOSED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.EXPOSED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.EXPOSED_COPPER, 8);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CUT_COPPER, Blocks.WEATHERED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WEATHERED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WEATHERED_COPPER, 8);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CUT_COPPER, Blocks.OXIDIZED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.OXIDIZED_COPPER, 8);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CUT_COPPER, Blocks.WAXED_COPPER_BLOCK, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_COPPER_BLOCK, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_COPPER_BLOCK, 8);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_COPPER, 8);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_COPPER, 8);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_COPPER, 4);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_COPPER, 8);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.COBBLED_DEEPSLATE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.COBBLED_DEEPSLATE_STAIRS, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.COBBLED_DEEPSLATE_WALL, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_DEEPSLATE, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DEEPSLATE, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DEEPSLATE_STAIRS, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.POLISHED_DEEPSLATE_WALL, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICKS, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_BRICK_WALL, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILES, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_TILE_WALL, Blocks.COBBLED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DEEPSLATE_STAIRS, Blocks.POLISHED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.POLISHED_DEEPSLATE_WALL, Blocks.POLISHED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICKS, Blocks.POLISHED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.POLISHED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_BRICK_WALL, Blocks.POLISHED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILES, Blocks.POLISHED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.POLISHED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_TILE_WALL, Blocks.POLISHED_DEEPSLATE);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.DEEPSLATE_BRICKS, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.DEEPSLATE_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_BRICK_WALL, Blocks.DEEPSLATE_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILES, Blocks.DEEPSLATE_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_BRICKS, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_TILE_WALL, Blocks.DEEPSLATE_BRICKS);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_TILES, 2);
		stonecutterResultFromBase(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILES);
		stonecutterResultFromBase(consumer, RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_TILE_WALL, Blocks.DEEPSLATE_TILES);
		legacyNetheriteSmithing(consumer, Items.DIAMOND_CHESTPLATE, RecipeCategory.COMBAT, Items.NETHERITE_CHESTPLATE);
		legacyNetheriteSmithing(consumer, Items.DIAMOND_LEGGINGS, RecipeCategory.COMBAT, Items.NETHERITE_LEGGINGS);
		legacyNetheriteSmithing(consumer, Items.DIAMOND_HELMET, RecipeCategory.COMBAT, Items.NETHERITE_HELMET);
		legacyNetheriteSmithing(consumer, Items.DIAMOND_BOOTS, RecipeCategory.COMBAT, Items.NETHERITE_BOOTS);
		legacyNetheriteSmithing(consumer, Items.DIAMOND_SWORD, RecipeCategory.COMBAT, Items.NETHERITE_SWORD);
		legacyNetheriteSmithing(consumer, Items.DIAMOND_AXE, RecipeCategory.TOOLS, Items.NETHERITE_AXE);
		legacyNetheriteSmithing(consumer, Items.DIAMOND_PICKAXE, RecipeCategory.TOOLS, Items.NETHERITE_PICKAXE);
		legacyNetheriteSmithing(consumer, Items.DIAMOND_HOE, RecipeCategory.TOOLS, Items.NETHERITE_HOE);
		legacyNetheriteSmithing(consumer, Items.DIAMOND_SHOVEL, RecipeCategory.TOOLS, Items.NETHERITE_SHOVEL);
	}
}
