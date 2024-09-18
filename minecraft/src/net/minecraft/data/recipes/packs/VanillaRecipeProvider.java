package net.minecraft.data.recipes.packs;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.recipes.TransmuteRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ArmorDyeRecipe;
import net.minecraft.world.item.crafting.BannerDuplicateRecipe;
import net.minecraft.world.item.crafting.BookCloningRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.DecoratedPotRecipe;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.MapCloningRecipe;
import net.minecraft.world.item.crafting.MapExtendingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.TippedArrowRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

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

	VanillaRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
		super(provider, recipeOutput);
	}

	@Override
	protected void buildRecipes() {
		this.output.includeRootAdvancement();
		this.generateForEnabledBlockFamilies(FeatureFlagSet.of(FeatureFlags.VANILLA));
		this.planksFromLog(Blocks.ACACIA_PLANKS, ItemTags.ACACIA_LOGS, 4);
		this.planksFromLogs(Blocks.BIRCH_PLANKS, ItemTags.BIRCH_LOGS, 4);
		this.planksFromLogs(Blocks.CRIMSON_PLANKS, ItemTags.CRIMSON_STEMS, 4);
		this.planksFromLog(Blocks.DARK_OAK_PLANKS, ItemTags.DARK_OAK_LOGS, 4);
		this.planksFromLogs(Blocks.JUNGLE_PLANKS, ItemTags.JUNGLE_LOGS, 4);
		this.planksFromLogs(Blocks.OAK_PLANKS, ItemTags.OAK_LOGS, 4);
		this.planksFromLogs(Blocks.SPRUCE_PLANKS, ItemTags.SPRUCE_LOGS, 4);
		this.planksFromLogs(Blocks.WARPED_PLANKS, ItemTags.WARPED_STEMS, 4);
		this.planksFromLogs(Blocks.MANGROVE_PLANKS, ItemTags.MANGROVE_LOGS, 4);
		this.woodFromLogs(Blocks.ACACIA_WOOD, Blocks.ACACIA_LOG);
		this.woodFromLogs(Blocks.BIRCH_WOOD, Blocks.BIRCH_LOG);
		this.woodFromLogs(Blocks.DARK_OAK_WOOD, Blocks.DARK_OAK_LOG);
		this.woodFromLogs(Blocks.JUNGLE_WOOD, Blocks.JUNGLE_LOG);
		this.woodFromLogs(Blocks.OAK_WOOD, Blocks.OAK_LOG);
		this.woodFromLogs(Blocks.SPRUCE_WOOD, Blocks.SPRUCE_LOG);
		this.woodFromLogs(Blocks.CRIMSON_HYPHAE, Blocks.CRIMSON_STEM);
		this.woodFromLogs(Blocks.WARPED_HYPHAE, Blocks.WARPED_STEM);
		this.woodFromLogs(Blocks.MANGROVE_WOOD, Blocks.MANGROVE_LOG);
		this.woodFromLogs(Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_ACACIA_LOG);
		this.woodFromLogs(Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG);
		this.woodFromLogs(Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG);
		this.woodFromLogs(Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_LOG);
		this.woodFromLogs(Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_OAK_LOG);
		this.woodFromLogs(Blocks.STRIPPED_SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_LOG);
		this.woodFromLogs(Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_STEM);
		this.woodFromLogs(Blocks.STRIPPED_WARPED_HYPHAE, Blocks.STRIPPED_WARPED_STEM);
		this.woodFromLogs(Blocks.STRIPPED_MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_LOG);
		this.woodenBoat(Items.ACACIA_BOAT, Blocks.ACACIA_PLANKS);
		this.woodenBoat(Items.BIRCH_BOAT, Blocks.BIRCH_PLANKS);
		this.woodenBoat(Items.DARK_OAK_BOAT, Blocks.DARK_OAK_PLANKS);
		this.woodenBoat(Items.JUNGLE_BOAT, Blocks.JUNGLE_PLANKS);
		this.woodenBoat(Items.OAK_BOAT, Blocks.OAK_PLANKS);
		this.woodenBoat(Items.SPRUCE_BOAT, Blocks.SPRUCE_PLANKS);
		this.woodenBoat(Items.MANGROVE_BOAT, Blocks.MANGROVE_PLANKS);
		List<Item> list = List.of(
			Items.BLACK_DYE,
			Items.BLUE_DYE,
			Items.BROWN_DYE,
			Items.CYAN_DYE,
			Items.GRAY_DYE,
			Items.GREEN_DYE,
			Items.LIGHT_BLUE_DYE,
			Items.LIGHT_GRAY_DYE,
			Items.LIME_DYE,
			Items.MAGENTA_DYE,
			Items.ORANGE_DYE,
			Items.PINK_DYE,
			Items.PURPLE_DYE,
			Items.RED_DYE,
			Items.YELLOW_DYE,
			Items.WHITE_DYE
		);
		List<Item> list2 = List.of(
			Items.BLACK_WOOL,
			Items.BLUE_WOOL,
			Items.BROWN_WOOL,
			Items.CYAN_WOOL,
			Items.GRAY_WOOL,
			Items.GREEN_WOOL,
			Items.LIGHT_BLUE_WOOL,
			Items.LIGHT_GRAY_WOOL,
			Items.LIME_WOOL,
			Items.MAGENTA_WOOL,
			Items.ORANGE_WOOL,
			Items.PINK_WOOL,
			Items.PURPLE_WOOL,
			Items.RED_WOOL,
			Items.YELLOW_WOOL,
			Items.WHITE_WOOL
		);
		List<Item> list3 = List.of(
			Items.BLACK_BED,
			Items.BLUE_BED,
			Items.BROWN_BED,
			Items.CYAN_BED,
			Items.GRAY_BED,
			Items.GREEN_BED,
			Items.LIGHT_BLUE_BED,
			Items.LIGHT_GRAY_BED,
			Items.LIME_BED,
			Items.MAGENTA_BED,
			Items.ORANGE_BED,
			Items.PINK_BED,
			Items.PURPLE_BED,
			Items.RED_BED,
			Items.YELLOW_BED,
			Items.WHITE_BED
		);
		List<Item> list4 = List.of(
			Items.BLACK_CARPET,
			Items.BLUE_CARPET,
			Items.BROWN_CARPET,
			Items.CYAN_CARPET,
			Items.GRAY_CARPET,
			Items.GREEN_CARPET,
			Items.LIGHT_BLUE_CARPET,
			Items.LIGHT_GRAY_CARPET,
			Items.LIME_CARPET,
			Items.MAGENTA_CARPET,
			Items.ORANGE_CARPET,
			Items.PINK_CARPET,
			Items.PURPLE_CARPET,
			Items.RED_CARPET,
			Items.YELLOW_CARPET,
			Items.WHITE_CARPET
		);
		this.colorBlockWithDye(list, list2, "wool");
		this.colorBlockWithDye(list, list3, "bed");
		this.colorBlockWithDye(list, list4, "carpet");
		this.carpet(Blocks.BLACK_CARPET, Blocks.BLACK_WOOL);
		this.bedFromPlanksAndWool(Items.BLACK_BED, Blocks.BLACK_WOOL);
		this.banner(Items.BLACK_BANNER, Blocks.BLACK_WOOL);
		this.carpet(Blocks.BLUE_CARPET, Blocks.BLUE_WOOL);
		this.bedFromPlanksAndWool(Items.BLUE_BED, Blocks.BLUE_WOOL);
		this.banner(Items.BLUE_BANNER, Blocks.BLUE_WOOL);
		this.carpet(Blocks.BROWN_CARPET, Blocks.BROWN_WOOL);
		this.bedFromPlanksAndWool(Items.BROWN_BED, Blocks.BROWN_WOOL);
		this.banner(Items.BROWN_BANNER, Blocks.BROWN_WOOL);
		this.carpet(Blocks.CYAN_CARPET, Blocks.CYAN_WOOL);
		this.bedFromPlanksAndWool(Items.CYAN_BED, Blocks.CYAN_WOOL);
		this.banner(Items.CYAN_BANNER, Blocks.CYAN_WOOL);
		this.carpet(Blocks.GRAY_CARPET, Blocks.GRAY_WOOL);
		this.bedFromPlanksAndWool(Items.GRAY_BED, Blocks.GRAY_WOOL);
		this.banner(Items.GRAY_BANNER, Blocks.GRAY_WOOL);
		this.carpet(Blocks.GREEN_CARPET, Blocks.GREEN_WOOL);
		this.bedFromPlanksAndWool(Items.GREEN_BED, Blocks.GREEN_WOOL);
		this.banner(Items.GREEN_BANNER, Blocks.GREEN_WOOL);
		this.carpet(Blocks.LIGHT_BLUE_CARPET, Blocks.LIGHT_BLUE_WOOL);
		this.bedFromPlanksAndWool(Items.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL);
		this.banner(Items.LIGHT_BLUE_BANNER, Blocks.LIGHT_BLUE_WOOL);
		this.carpet(Blocks.LIGHT_GRAY_CARPET, Blocks.LIGHT_GRAY_WOOL);
		this.bedFromPlanksAndWool(Items.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL);
		this.banner(Items.LIGHT_GRAY_BANNER, Blocks.LIGHT_GRAY_WOOL);
		this.carpet(Blocks.LIME_CARPET, Blocks.LIME_WOOL);
		this.bedFromPlanksAndWool(Items.LIME_BED, Blocks.LIME_WOOL);
		this.banner(Items.LIME_BANNER, Blocks.LIME_WOOL);
		this.carpet(Blocks.MAGENTA_CARPET, Blocks.MAGENTA_WOOL);
		this.bedFromPlanksAndWool(Items.MAGENTA_BED, Blocks.MAGENTA_WOOL);
		this.banner(Items.MAGENTA_BANNER, Blocks.MAGENTA_WOOL);
		this.carpet(Blocks.ORANGE_CARPET, Blocks.ORANGE_WOOL);
		this.bedFromPlanksAndWool(Items.ORANGE_BED, Blocks.ORANGE_WOOL);
		this.banner(Items.ORANGE_BANNER, Blocks.ORANGE_WOOL);
		this.carpet(Blocks.PINK_CARPET, Blocks.PINK_WOOL);
		this.bedFromPlanksAndWool(Items.PINK_BED, Blocks.PINK_WOOL);
		this.banner(Items.PINK_BANNER, Blocks.PINK_WOOL);
		this.carpet(Blocks.PURPLE_CARPET, Blocks.PURPLE_WOOL);
		this.bedFromPlanksAndWool(Items.PURPLE_BED, Blocks.PURPLE_WOOL);
		this.banner(Items.PURPLE_BANNER, Blocks.PURPLE_WOOL);
		this.carpet(Blocks.RED_CARPET, Blocks.RED_WOOL);
		this.bedFromPlanksAndWool(Items.RED_BED, Blocks.RED_WOOL);
		this.banner(Items.RED_BANNER, Blocks.RED_WOOL);
		this.carpet(Blocks.WHITE_CARPET, Blocks.WHITE_WOOL);
		this.bedFromPlanksAndWool(Items.WHITE_BED, Blocks.WHITE_WOOL);
		this.banner(Items.WHITE_BANNER, Blocks.WHITE_WOOL);
		this.carpet(Blocks.YELLOW_CARPET, Blocks.YELLOW_WOOL);
		this.bedFromPlanksAndWool(Items.YELLOW_BED, Blocks.YELLOW_WOOL);
		this.banner(Items.YELLOW_BANNER, Blocks.YELLOW_WOOL);
		this.carpet(Blocks.MOSS_CARPET, Blocks.MOSS_BLOCK);
		this.stainedGlassFromGlassAndDye(Blocks.BLACK_STAINED_GLASS, Items.BLACK_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.BLACK_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.BLACK_STAINED_GLASS_PANE, Items.BLACK_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.BLUE_STAINED_GLASS, Items.BLUE_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.BLUE_STAINED_GLASS_PANE, Items.BLUE_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.BROWN_STAINED_GLASS, Items.BROWN_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.BROWN_STAINED_GLASS_PANE, Items.BROWN_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.CYAN_STAINED_GLASS, Items.CYAN_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.CYAN_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.CYAN_STAINED_GLASS_PANE, Items.CYAN_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.GRAY_STAINED_GLASS, Items.GRAY_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.GRAY_STAINED_GLASS_PANE, Items.GRAY_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.GREEN_STAINED_GLASS, Items.GREEN_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.GREEN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.GREEN_STAINED_GLASS_PANE, Items.GREEN_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.LIGHT_BLUE_STAINED_GLASS, Items.LIGHT_BLUE_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Items.LIGHT_BLUE_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.LIGHT_GRAY_STAINED_GLASS, Items.LIGHT_GRAY_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Items.LIGHT_GRAY_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.LIME_STAINED_GLASS, Items.LIME_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.LIME_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.LIME_STAINED_GLASS_PANE, Items.LIME_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.MAGENTA_STAINED_GLASS, Items.MAGENTA_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.MAGENTA_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.MAGENTA_STAINED_GLASS_PANE, Items.MAGENTA_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.ORANGE_STAINED_GLASS, Items.ORANGE_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.ORANGE_STAINED_GLASS_PANE, Items.ORANGE_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.PINK_STAINED_GLASS, Items.PINK_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.PINK_STAINED_GLASS_PANE, Blocks.PINK_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.PINK_STAINED_GLASS_PANE, Items.PINK_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.PURPLE_STAINED_GLASS, Items.PURPLE_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.PURPLE_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.PURPLE_STAINED_GLASS_PANE, Items.PURPLE_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.RED_STAINED_GLASS, Items.RED_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.RED_STAINED_GLASS_PANE, Blocks.RED_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.RED_STAINED_GLASS_PANE, Items.RED_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.WHITE_STAINED_GLASS, Items.WHITE_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.WHITE_STAINED_GLASS_PANE, Blocks.WHITE_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.WHITE_STAINED_GLASS_PANE, Items.WHITE_DYE);
		this.stainedGlassFromGlassAndDye(Blocks.YELLOW_STAINED_GLASS, Items.YELLOW_DYE);
		this.stainedGlassPaneFromStainedGlass(Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS);
		this.stainedGlassPaneFromGlassPaneAndDye(Blocks.YELLOW_STAINED_GLASS_PANE, Items.YELLOW_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.BLACK_TERRACOTTA, Items.BLACK_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.BLUE_TERRACOTTA, Items.BLUE_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.BROWN_TERRACOTTA, Items.BROWN_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.CYAN_TERRACOTTA, Items.CYAN_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.GRAY_TERRACOTTA, Items.GRAY_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.GREEN_TERRACOTTA, Items.GREEN_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.LIGHT_GRAY_TERRACOTTA, Items.LIGHT_GRAY_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.LIME_TERRACOTTA, Items.LIME_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.MAGENTA_TERRACOTTA, Items.MAGENTA_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.ORANGE_TERRACOTTA, Items.ORANGE_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.PINK_TERRACOTTA, Items.PINK_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.PURPLE_TERRACOTTA, Items.PURPLE_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.RED_TERRACOTTA, Items.RED_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.WHITE_TERRACOTTA, Items.WHITE_DYE);
		this.coloredTerracottaFromTerracottaAndDye(Blocks.YELLOW_TERRACOTTA, Items.YELLOW_DYE);
		this.concretePowder(Blocks.BLACK_CONCRETE_POWDER, Items.BLACK_DYE);
		this.concretePowder(Blocks.BLUE_CONCRETE_POWDER, Items.BLUE_DYE);
		this.concretePowder(Blocks.BROWN_CONCRETE_POWDER, Items.BROWN_DYE);
		this.concretePowder(Blocks.CYAN_CONCRETE_POWDER, Items.CYAN_DYE);
		this.concretePowder(Blocks.GRAY_CONCRETE_POWDER, Items.GRAY_DYE);
		this.concretePowder(Blocks.GREEN_CONCRETE_POWDER, Items.GREEN_DYE);
		this.concretePowder(Blocks.LIGHT_BLUE_CONCRETE_POWDER, Items.LIGHT_BLUE_DYE);
		this.concretePowder(Blocks.LIGHT_GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_DYE);
		this.concretePowder(Blocks.LIME_CONCRETE_POWDER, Items.LIME_DYE);
		this.concretePowder(Blocks.MAGENTA_CONCRETE_POWDER, Items.MAGENTA_DYE);
		this.concretePowder(Blocks.ORANGE_CONCRETE_POWDER, Items.ORANGE_DYE);
		this.concretePowder(Blocks.PINK_CONCRETE_POWDER, Items.PINK_DYE);
		this.concretePowder(Blocks.PURPLE_CONCRETE_POWDER, Items.PURPLE_DYE);
		this.concretePowder(Blocks.RED_CONCRETE_POWDER, Items.RED_DYE);
		this.concretePowder(Blocks.WHITE_CONCRETE_POWDER, Items.WHITE_DYE);
		this.concretePowder(Blocks.YELLOW_CONCRETE_POWDER, Items.YELLOW_DYE);
		this.shaped(RecipeCategory.DECORATIONS, Items.CANDLE)
			.define('S', Items.STRING)
			.define('H', Items.HONEYCOMB)
			.pattern("S")
			.pattern("H")
			.unlockedBy("has_string", this.has(Items.STRING))
			.unlockedBy("has_honeycomb", this.has(Items.HONEYCOMB))
			.save(this.output);
		this.candle(Blocks.BLACK_CANDLE, Items.BLACK_DYE);
		this.candle(Blocks.BLUE_CANDLE, Items.BLUE_DYE);
		this.candle(Blocks.BROWN_CANDLE, Items.BROWN_DYE);
		this.candle(Blocks.CYAN_CANDLE, Items.CYAN_DYE);
		this.candle(Blocks.GRAY_CANDLE, Items.GRAY_DYE);
		this.candle(Blocks.GREEN_CANDLE, Items.GREEN_DYE);
		this.candle(Blocks.LIGHT_BLUE_CANDLE, Items.LIGHT_BLUE_DYE);
		this.candle(Blocks.LIGHT_GRAY_CANDLE, Items.LIGHT_GRAY_DYE);
		this.candle(Blocks.LIME_CANDLE, Items.LIME_DYE);
		this.candle(Blocks.MAGENTA_CANDLE, Items.MAGENTA_DYE);
		this.candle(Blocks.ORANGE_CANDLE, Items.ORANGE_DYE);
		this.candle(Blocks.PINK_CANDLE, Items.PINK_DYE);
		this.candle(Blocks.PURPLE_CANDLE, Items.PURPLE_DYE);
		this.candle(Blocks.RED_CANDLE, Items.RED_DYE);
		this.candle(Blocks.WHITE_CANDLE, Items.WHITE_DYE);
		this.candle(Blocks.YELLOW_CANDLE, Items.YELLOW_DYE);
		this.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.PACKED_MUD, 1)
			.requires(Blocks.MUD)
			.requires(Items.WHEAT)
			.unlockedBy("has_mud", this.has(Blocks.MUD))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.MUD_BRICKS, 4)
			.define('#', Blocks.PACKED_MUD)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_packed_mud", this.has(Blocks.PACKED_MUD))
			.save(this.output);
		this.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.MUDDY_MANGROVE_ROOTS, 1)
			.requires(Blocks.MUD)
			.requires(Items.MANGROVE_ROOTS)
			.unlockedBy("has_mangrove_roots", this.has(Blocks.MANGROVE_ROOTS))
			.save(this.output);
		this.shaped(RecipeCategory.TRANSPORTATION, Blocks.ACTIVATOR_RAIL, 6)
			.define('#', Blocks.REDSTONE_TORCH)
			.define('S', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("XSX")
			.pattern("X#X")
			.pattern("XSX")
			.unlockedBy("has_rail", this.has(Blocks.RAIL))
			.save(this.output);
		this.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.ANDESITE, 2)
			.requires(Blocks.DIORITE)
			.requires(Blocks.COBBLESTONE)
			.unlockedBy("has_stone", this.has(Blocks.DIORITE))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.ANVIL)
			.define('I', Blocks.IRON_BLOCK)
			.define('i', Items.IRON_INGOT)
			.pattern("III")
			.pattern(" i ")
			.pattern("iii")
			.unlockedBy("has_iron_block", this.has(Blocks.IRON_BLOCK))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Items.ARMOR_STAND)
			.define('/', Items.STICK)
			.define('_', Blocks.SMOOTH_STONE_SLAB)
			.pattern("///")
			.pattern(" / ")
			.pattern("/_/")
			.unlockedBy("has_stone_slab", this.has(Blocks.SMOOTH_STONE_SLAB))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.ARROW, 4)
			.define('#', Items.STICK)
			.define('X', Items.FLINT)
			.define('Y', Items.FEATHER)
			.pattern("X")
			.pattern("#")
			.pattern("Y")
			.unlockedBy("has_feather", this.has(Items.FEATHER))
			.unlockedBy("has_flint", this.has(Items.FLINT))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.BARREL, 1)
			.define('P', ItemTags.PLANKS)
			.define('S', ItemTags.WOODEN_SLABS)
			.pattern("PSP")
			.pattern("P P")
			.pattern("PSP")
			.unlockedBy("has_planks", this.has(ItemTags.PLANKS))
			.unlockedBy("has_wood_slab", this.has(ItemTags.WOODEN_SLABS))
			.save(this.output);
		this.shaped(RecipeCategory.MISC, Blocks.BEACON)
			.define('S', Items.NETHER_STAR)
			.define('G', Blocks.GLASS)
			.define('O', Blocks.OBSIDIAN)
			.pattern("GGG")
			.pattern("GSG")
			.pattern("OOO")
			.unlockedBy("has_nether_star", this.has(Items.NETHER_STAR))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.BEEHIVE)
			.define('P', ItemTags.PLANKS)
			.define('H', Items.HONEYCOMB)
			.pattern("PPP")
			.pattern("HHH")
			.pattern("PPP")
			.unlockedBy("has_honeycomb", this.has(Items.HONEYCOMB))
			.save(this.output);
		this.shapeless(RecipeCategory.FOOD, Items.BEETROOT_SOUP)
			.requires(Items.BOWL)
			.requires(Items.BEETROOT, 6)
			.unlockedBy("has_beetroot", this.has(Items.BEETROOT))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.BLACK_DYE)
			.requires(Items.INK_SAC)
			.group("black_dye")
			.unlockedBy("has_ink_sac", this.has(Items.INK_SAC))
			.save(this.output);
		this.oneToOneConversionRecipe(Items.BLACK_DYE, Blocks.WITHER_ROSE, "black_dye");
		this.shapeless(RecipeCategory.BREWING, Items.BLAZE_POWDER, 2)
			.requires(Items.BLAZE_ROD)
			.unlockedBy("has_blaze_rod", this.has(Items.BLAZE_ROD))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.BLUE_DYE)
			.requires(Items.LAPIS_LAZULI)
			.group("blue_dye")
			.unlockedBy("has_lapis_lazuli", this.has(Items.LAPIS_LAZULI))
			.save(this.output);
		this.oneToOneConversionRecipe(Items.BLUE_DYE, Blocks.CORNFLOWER, "blue_dye");
		this.threeByThreePacker(RecipeCategory.BUILDING_BLOCKS, Blocks.BLUE_ICE, Blocks.PACKED_ICE);
		this.shapeless(RecipeCategory.MISC, Items.BONE_MEAL, 3).requires(Items.BONE).group("bonemeal").unlockedBy("has_bone", this.has(Items.BONE)).save(this.output);
		this.nineBlockStorageRecipesRecipesWithCustomUnpacking(
			RecipeCategory.MISC, Items.BONE_MEAL, RecipeCategory.BUILDING_BLOCKS, Items.BONE_BLOCK, "bone_meal_from_bone_block", "bonemeal"
		);
		this.shapeless(RecipeCategory.MISC, Items.BOOK)
			.requires(Items.PAPER, 3)
			.requires(Items.LEATHER)
			.unlockedBy("has_paper", this.has(Items.PAPER))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.BOOKSHELF)
			.define('#', ItemTags.PLANKS)
			.define('X', Items.BOOK)
			.pattern("###")
			.pattern("XXX")
			.pattern("###")
			.unlockedBy("has_book", this.has(Items.BOOK))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.BOW)
			.define('#', Items.STICK)
			.define('X', Items.STRING)
			.pattern(" #X")
			.pattern("# X")
			.pattern(" #X")
			.unlockedBy("has_string", this.has(Items.STRING))
			.save(this.output);
		this.shaped(RecipeCategory.MISC, Items.BOWL, 4)
			.define('#', ItemTags.PLANKS)
			.pattern("# #")
			.pattern(" # ")
			.unlockedBy("has_brown_mushroom", this.has(Blocks.BROWN_MUSHROOM))
			.unlockedBy("has_red_mushroom", this.has(Blocks.RED_MUSHROOM))
			.unlockedBy("has_mushroom_stew", this.has(Items.MUSHROOM_STEW))
			.save(this.output);
		this.shaped(RecipeCategory.FOOD, Items.BREAD).define('#', Items.WHEAT).pattern("###").unlockedBy("has_wheat", this.has(Items.WHEAT)).save(this.output);
		this.shaped(RecipeCategory.BREWING, Blocks.BREWING_STAND)
			.define('B', Items.BLAZE_ROD)
			.define('#', ItemTags.STONE_CRAFTING_MATERIALS)
			.pattern(" B ")
			.pattern("###")
			.unlockedBy("has_blaze_rod", this.has(Items.BLAZE_ROD))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.BRICKS)
			.define('#', Items.BRICK)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_brick", this.has(Items.BRICK))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.BROWN_DYE)
			.requires(Items.COCOA_BEANS)
			.group("brown_dye")
			.unlockedBy("has_cocoa_beans", this.has(Items.COCOA_BEANS))
			.save(this.output);
		this.shaped(RecipeCategory.MISC, Items.BUCKET)
			.define('#', Items.IRON_INGOT)
			.pattern("# #")
			.pattern(" # ")
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.FOOD, Blocks.CAKE)
			.define('A', Items.MILK_BUCKET)
			.define('B', Items.SUGAR)
			.define('C', Items.WHEAT)
			.define('E', Items.EGG)
			.pattern("AAA")
			.pattern("BEB")
			.pattern("CCC")
			.unlockedBy("has_egg", this.has(Items.EGG))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.CAMPFIRE)
			.define('L', ItemTags.LOGS)
			.define('S', Items.STICK)
			.define('C', ItemTags.COALS)
			.pattern(" S ")
			.pattern("SCS")
			.pattern("LLL")
			.unlockedBy("has_stick", this.has(Items.STICK))
			.unlockedBy("has_coal", this.has(ItemTags.COALS))
			.save(this.output);
		this.shaped(RecipeCategory.TRANSPORTATION, Items.CARROT_ON_A_STICK)
			.define('#', Items.FISHING_ROD)
			.define('X', Items.CARROT)
			.pattern("# ")
			.pattern(" X")
			.unlockedBy("has_carrot", this.has(Items.CARROT))
			.save(this.output);
		this.shaped(RecipeCategory.TRANSPORTATION, Items.WARPED_FUNGUS_ON_A_STICK)
			.define('#', Items.FISHING_ROD)
			.define('X', Items.WARPED_FUNGUS)
			.pattern("# ")
			.pattern(" X")
			.unlockedBy("has_warped_fungus", this.has(Items.WARPED_FUNGUS))
			.save(this.output);
		this.shaped(RecipeCategory.BREWING, Blocks.CAULDRON)
			.define('#', Items.IRON_INGOT)
			.pattern("# #")
			.pattern("# #")
			.pattern("###")
			.unlockedBy("has_water_bucket", this.has(Items.WATER_BUCKET))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.COMPOSTER)
			.define('#', ItemTags.WOODEN_SLABS)
			.pattern("# #")
			.pattern("# #")
			.pattern("###")
			.unlockedBy("has_wood_slab", this.has(ItemTags.WOODEN_SLABS))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.CHEST)
			.define('#', ItemTags.PLANKS)
			.pattern("###")
			.pattern("# #")
			.pattern("###")
			.unlockedBy(
				"has_lots_of_items",
				CriteriaTriggers.INVENTORY_CHANGED
					.createCriterion(
						new InventoryChangeTrigger.TriggerInstance(
							Optional.empty(),
							new InventoryChangeTrigger.TriggerInstance.Slots(MinMaxBounds.Ints.atLeast(10), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY),
							List.of()
						)
					)
			)
			.save(this.output);
		this.shapeless(RecipeCategory.TRANSPORTATION, Items.CHEST_MINECART)
			.requires(Blocks.CHEST)
			.requires(Items.MINECART)
			.unlockedBy("has_minecart", this.has(Items.MINECART))
			.save(this.output);
		this.chestBoat(Items.ACACIA_CHEST_BOAT, Items.ACACIA_BOAT);
		this.chestBoat(Items.BIRCH_CHEST_BOAT, Items.BIRCH_BOAT);
		this.chestBoat(Items.DARK_OAK_CHEST_BOAT, Items.DARK_OAK_BOAT);
		this.chestBoat(Items.JUNGLE_CHEST_BOAT, Items.JUNGLE_BOAT);
		this.chestBoat(Items.OAK_CHEST_BOAT, Items.OAK_BOAT);
		this.chestBoat(Items.SPRUCE_CHEST_BOAT, Items.SPRUCE_BOAT);
		this.chestBoat(Items.MANGROVE_CHEST_BOAT, Items.MANGROVE_BOAT);
		this.chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_QUARTZ_BLOCK, Ingredient.of(Blocks.QUARTZ_SLAB))
			.unlockedBy("has_chiseled_quartz_block", this.has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlockedBy("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.unlockedBy("has_quartz_pillar", this.has(Blocks.QUARTZ_PILLAR))
			.save(this.output);
		this.chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_STONE_BRICKS, Ingredient.of(Blocks.STONE_BRICK_SLAB))
			.unlockedBy("has_tag", this.has(ItemTags.STONE_BRICKS))
			.save(this.output);
		this.twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, Blocks.CLAY, Items.CLAY_BALL);
		this.shaped(RecipeCategory.TOOLS, Items.CLOCK)
			.define('#', Items.GOLD_INGOT)
			.define('X', Items.REDSTONE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlockedBy("has_redstone", this.has(Items.REDSTONE))
			.save(this.output);
		this.nineBlockStorageRecipes(RecipeCategory.MISC, Items.COAL, RecipeCategory.BUILDING_BLOCKS, Items.COAL_BLOCK);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.COARSE_DIRT, 4)
			.define('D', Blocks.DIRT)
			.define('G', Blocks.GRAVEL)
			.pattern("DG")
			.pattern("GD")
			.unlockedBy("has_gravel", this.has(Blocks.GRAVEL))
			.save(this.output);
		this.shaped(RecipeCategory.REDSTONE, Blocks.COMPARATOR)
			.define('#', Blocks.REDSTONE_TORCH)
			.define('X', Items.QUARTZ)
			.define('I', Blocks.STONE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern("III")
			.unlockedBy("has_quartz", this.has(Items.QUARTZ))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.COMPASS)
			.define('#', Items.IRON_INGOT)
			.define('X', Items.REDSTONE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlockedBy("has_redstone", this.has(Items.REDSTONE))
			.save(this.output);
		this.shaped(RecipeCategory.FOOD, Items.COOKIE, 8)
			.define('#', Items.WHEAT)
			.define('X', Items.COCOA_BEANS)
			.pattern("#X#")
			.unlockedBy("has_cocoa", this.has(Items.COCOA_BEANS))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.CRAFTING_TABLE)
			.define('#', ItemTags.PLANKS)
			.pattern("##")
			.pattern("##")
			.unlockedBy("unlock_right_away", PlayerTrigger.TriggerInstance.tick())
			.showNotification(false)
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.CROSSBOW)
			.define('~', Items.STRING)
			.define('#', Items.STICK)
			.define('&', Items.IRON_INGOT)
			.define('$', Blocks.TRIPWIRE_HOOK)
			.pattern("#&#")
			.pattern("~$~")
			.pattern(" # ")
			.unlockedBy("has_string", this.has(Items.STRING))
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.unlockedBy("has_tripwire_hook", this.has(Blocks.TRIPWIRE_HOOK))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.LOOM)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.STRING)
			.pattern("@@")
			.pattern("##")
			.unlockedBy("has_string", this.has(Items.STRING))
			.save(this.output);
		this.chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_RED_SANDSTONE, Ingredient.of(Blocks.RED_SANDSTONE_SLAB))
			.unlockedBy("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.unlockedBy("has_chiseled_red_sandstone", this.has(Blocks.CHISELED_RED_SANDSTONE))
			.unlockedBy("has_cut_red_sandstone", this.has(Blocks.CUT_RED_SANDSTONE))
			.save(this.output);
		this.chiseled(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_SANDSTONE, Blocks.SANDSTONE_SLAB);
		this.nineBlockStorageRecipesRecipesWithCustomUnpacking(
			RecipeCategory.MISC,
			Items.COPPER_INGOT,
			RecipeCategory.BUILDING_BLOCKS,
			Items.COPPER_BLOCK,
			getSimpleRecipeName(Items.COPPER_INGOT),
			getItemName(Items.COPPER_INGOT)
		);
		this.shapeless(RecipeCategory.MISC, Items.COPPER_INGOT, 9)
			.requires(Blocks.WAXED_COPPER_BLOCK)
			.group(getItemName(Items.COPPER_INGOT))
			.unlockedBy(getHasName(Blocks.WAXED_COPPER_BLOCK), this.has(Blocks.WAXED_COPPER_BLOCK))
			.save(this.output, getConversionRecipeName(Items.COPPER_INGOT, Blocks.WAXED_COPPER_BLOCK));
		this.waxRecipes(FeatureFlagSet.of(FeatureFlags.VANILLA));
		this.shapeless(RecipeCategory.MISC, Items.CYAN_DYE, 2)
			.requires(Items.BLUE_DYE)
			.requires(Items.GREEN_DYE)
			.group("cyan_dye")
			.unlockedBy("has_green_dye", this.has(Items.GREEN_DYE))
			.unlockedBy("has_blue_dye", this.has(Items.BLUE_DYE))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.DARK_PRISMARINE)
			.define('S', Items.PRISMARINE_SHARD)
			.define('I', Items.BLACK_DYE)
			.pattern("SSS")
			.pattern("SIS")
			.pattern("SSS")
			.unlockedBy("has_prismarine_shard", this.has(Items.PRISMARINE_SHARD))
			.save(this.output);
		this.shaped(RecipeCategory.REDSTONE, Blocks.DAYLIGHT_DETECTOR)
			.define('Q', Items.QUARTZ)
			.define('G', Blocks.GLASS)
			.define('W', ItemTags.WOODEN_SLABS)
			.pattern("GGG")
			.pattern("QQQ")
			.pattern("WWW")
			.unlockedBy("has_quartz", this.has(Items.QUARTZ))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICKS, 4)
			.define('S', Blocks.POLISHED_DEEPSLATE)
			.pattern("SS")
			.pattern("SS")
			.unlockedBy("has_polished_deepslate", this.has(Blocks.POLISHED_DEEPSLATE))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILES, 4)
			.define('S', Blocks.DEEPSLATE_BRICKS)
			.pattern("SS")
			.pattern("SS")
			.unlockedBy("has_deepslate_bricks", this.has(Blocks.DEEPSLATE_BRICKS))
			.save(this.output);
		this.shaped(RecipeCategory.TRANSPORTATION, Blocks.DETECTOR_RAIL, 6)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.STONE_PRESSURE_PLATE)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("X#X")
			.pattern("XRX")
			.unlockedBy("has_rail", this.has(Blocks.RAIL))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.DIAMOND_AXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.DIAMOND_TOOL_MATERIALS)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlockedBy("has_diamond", this.has(ItemTags.DIAMOND_TOOL_MATERIALS))
			.save(this.output);
		this.nineBlockStorageRecipes(RecipeCategory.MISC, Items.DIAMOND, RecipeCategory.BUILDING_BLOCKS, Items.DIAMOND_BLOCK);
		this.shaped(RecipeCategory.COMBAT, Items.DIAMOND_BOOTS)
			.define('X', Items.DIAMOND)
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_diamond", this.has(Items.DIAMOND))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.DIAMOND_CHESTPLATE)
			.define('X', Items.DIAMOND)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlockedBy("has_diamond", this.has(Items.DIAMOND))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.DIAMOND_HELMET)
			.define('X', Items.DIAMOND)
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_diamond", this.has(Items.DIAMOND))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.DIAMOND_HOE)
			.define('#', Items.STICK)
			.define('X', ItemTags.DIAMOND_TOOL_MATERIALS)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlockedBy("has_diamond", this.has(ItemTags.DIAMOND_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.DIAMOND_LEGGINGS)
			.define('X', Items.DIAMOND)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_diamond", this.has(Items.DIAMOND))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.DIAMOND_PICKAXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.DIAMOND_TOOL_MATERIALS)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlockedBy("has_diamond", this.has(ItemTags.DIAMOND_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.DIAMOND_SHOVEL)
			.define('#', Items.STICK)
			.define('X', ItemTags.DIAMOND_TOOL_MATERIALS)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_diamond", this.has(ItemTags.DIAMOND_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.DIAMOND_SWORD)
			.define('#', Items.STICK)
			.define('X', ItemTags.DIAMOND_TOOL_MATERIALS)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_diamond", this.has(ItemTags.DIAMOND_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.DIORITE, 2)
			.define('Q', Items.QUARTZ)
			.define('C', Blocks.COBBLESTONE)
			.pattern("CQ")
			.pattern("QC")
			.unlockedBy("has_quartz", this.has(Items.QUARTZ))
			.save(this.output);
		this.shaped(RecipeCategory.REDSTONE, Blocks.DISPENSER)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.define('X', Items.BOW)
			.pattern("###")
			.pattern("#X#")
			.pattern("#R#")
			.unlockedBy("has_bow", this.has(Items.BOW))
			.save(this.output);
		this.twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, Blocks.DRIPSTONE_BLOCK, Items.POINTED_DRIPSTONE);
		this.shaped(RecipeCategory.REDSTONE, Blocks.DROPPER)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.pattern("###")
			.pattern("# #")
			.pattern("#R#")
			.unlockedBy("has_redstone", this.has(Items.REDSTONE))
			.save(this.output);
		this.nineBlockStorageRecipes(RecipeCategory.MISC, Items.EMERALD, RecipeCategory.BUILDING_BLOCKS, Items.EMERALD_BLOCK);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.ENCHANTING_TABLE)
			.define('B', Items.BOOK)
			.define('#', Blocks.OBSIDIAN)
			.define('D', Items.DIAMOND)
			.pattern(" B ")
			.pattern("D#D")
			.pattern("###")
			.unlockedBy("has_obsidian", this.has(Blocks.OBSIDIAN))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.ENDER_CHEST)
			.define('#', Blocks.OBSIDIAN)
			.define('E', Items.ENDER_EYE)
			.pattern("###")
			.pattern("#E#")
			.pattern("###")
			.unlockedBy("has_ender_eye", this.has(Items.ENDER_EYE))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.ENDER_EYE)
			.requires(Items.ENDER_PEARL)
			.requires(Items.BLAZE_POWDER)
			.unlockedBy("has_blaze_powder", this.has(Items.BLAZE_POWDER))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICKS, 4)
			.define('#', Blocks.END_STONE)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_end_stone", this.has(Blocks.END_STONE))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Items.END_CRYSTAL)
			.define('T', Items.GHAST_TEAR)
			.define('E', Items.ENDER_EYE)
			.define('G', Blocks.GLASS)
			.pattern("GGG")
			.pattern("GEG")
			.pattern("GTG")
			.unlockedBy("has_ender_eye", this.has(Items.ENDER_EYE))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.END_ROD, 4)
			.define('#', Items.POPPED_CHORUS_FRUIT)
			.define('/', Items.BLAZE_ROD)
			.pattern("/")
			.pattern("#")
			.unlockedBy("has_chorus_fruit_popped", this.has(Items.POPPED_CHORUS_FRUIT))
			.save(this.output);
		this.shapeless(RecipeCategory.BREWING, Items.FERMENTED_SPIDER_EYE)
			.requires(Items.SPIDER_EYE)
			.requires(Blocks.BROWN_MUSHROOM)
			.requires(Items.SUGAR)
			.unlockedBy("has_spider_eye", this.has(Items.SPIDER_EYE))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.FIRE_CHARGE, 3)
			.requires(Items.GUNPOWDER)
			.requires(Items.BLAZE_POWDER)
			.requires(Ingredient.of(Items.COAL, Items.CHARCOAL))
			.unlockedBy("has_blaze_powder", this.has(Items.BLAZE_POWDER))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.FIREWORK_ROCKET, 3)
			.requires(Items.GUNPOWDER)
			.requires(Items.PAPER)
			.unlockedBy("has_gunpowder", this.has(Items.GUNPOWDER))
			.save(this.output, "firework_rocket_simple");
		this.shaped(RecipeCategory.TOOLS, Items.FISHING_ROD)
			.define('#', Items.STICK)
			.define('X', Items.STRING)
			.pattern("  #")
			.pattern(" #X")
			.pattern("# X")
			.unlockedBy("has_string", this.has(Items.STRING))
			.save(this.output);
		this.shapeless(RecipeCategory.TOOLS, Items.FLINT_AND_STEEL)
			.requires(Items.IRON_INGOT)
			.requires(Items.FLINT)
			.unlockedBy("has_flint", this.has(Items.FLINT))
			.unlockedBy("has_obsidian", this.has(Blocks.OBSIDIAN))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.FLOWER_POT)
			.define('#', Items.BRICK)
			.pattern("# #")
			.pattern(" # ")
			.unlockedBy("has_brick", this.has(Items.BRICK))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.FURNACE)
			.define('#', ItemTags.STONE_CRAFTING_MATERIALS)
			.pattern("###")
			.pattern("# #")
			.pattern("###")
			.unlockedBy("has_cobblestone", this.has(ItemTags.STONE_CRAFTING_MATERIALS))
			.save(this.output);
		this.shapeless(RecipeCategory.TRANSPORTATION, Items.FURNACE_MINECART)
			.requires(Blocks.FURNACE)
			.requires(Items.MINECART)
			.unlockedBy("has_minecart", this.has(Items.MINECART))
			.save(this.output);
		this.shaped(RecipeCategory.BREWING, Items.GLASS_BOTTLE, 3)
			.define('#', Blocks.GLASS)
			.pattern("# #")
			.pattern(" # ")
			.unlockedBy("has_glass", this.has(Blocks.GLASS))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.GLASS_PANE, 16)
			.define('#', Blocks.GLASS)
			.pattern("###")
			.pattern("###")
			.unlockedBy("has_glass", this.has(Blocks.GLASS))
			.save(this.output);
		this.twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, Blocks.GLOWSTONE, Items.GLOWSTONE_DUST);
		this.shapeless(RecipeCategory.DECORATIONS, Items.GLOW_ITEM_FRAME)
			.requires(Items.ITEM_FRAME)
			.requires(Items.GLOW_INK_SAC)
			.unlockedBy("has_item_frame", this.has(Items.ITEM_FRAME))
			.unlockedBy("has_glow_ink_sac", this.has(Items.GLOW_INK_SAC))
			.save(this.output);
		this.shaped(RecipeCategory.FOOD, Items.GOLDEN_APPLE)
			.define('#', Items.GOLD_INGOT)
			.define('X', Items.APPLE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.GOLDEN_AXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.GOLD_TOOL_MATERIALS)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlockedBy("has_gold_ingot", this.has(ItemTags.GOLD_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.GOLDEN_BOOTS)
			.define('X', Items.GOLD_INGOT)
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.BREWING, Items.GOLDEN_CARROT)
			.define('#', Items.GOLD_NUGGET)
			.define('X', Items.CARROT)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_gold_nugget", this.has(Items.GOLD_NUGGET))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.GOLDEN_CHESTPLATE)
			.define('X', Items.GOLD_INGOT)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlockedBy("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.GOLDEN_HELMET)
			.define('X', Items.GOLD_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.GOLDEN_HOE)
			.define('#', Items.STICK)
			.define('X', ItemTags.GOLD_TOOL_MATERIALS)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlockedBy("has_gold_ingot", this.has(ItemTags.GOLD_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.GOLDEN_LEGGINGS)
			.define('X', Items.GOLD_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.GOLDEN_PICKAXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.GOLD_TOOL_MATERIALS)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlockedBy("has_gold_ingot", this.has(ItemTags.GOLD_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.TRANSPORTATION, Blocks.POWERED_RAIL, 6)
			.define('R', Items.REDSTONE)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("X X")
			.pattern("X#X")
			.pattern("XRX")
			.unlockedBy("has_rail", this.has(Blocks.RAIL))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.GOLDEN_SHOVEL)
			.define('#', Items.STICK)
			.define('X', ItemTags.GOLD_TOOL_MATERIALS)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_gold_ingot", this.has(ItemTags.GOLD_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.GOLDEN_SWORD)
			.define('#', Items.STICK)
			.define('X', ItemTags.GOLD_TOOL_MATERIALS)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_gold_ingot", this.has(ItemTags.GOLD_TOOL_MATERIALS))
			.save(this.output);
		this.nineBlockStorageRecipesRecipesWithCustomUnpacking(
			RecipeCategory.MISC, Items.GOLD_INGOT, RecipeCategory.BUILDING_BLOCKS, Items.GOLD_BLOCK, "gold_ingot_from_gold_block", "gold_ingot"
		);
		this.nineBlockStorageRecipesWithCustomPacking(
			RecipeCategory.MISC, Items.GOLD_NUGGET, RecipeCategory.MISC, Items.GOLD_INGOT, "gold_ingot_from_nuggets", "gold_ingot"
		);
		this.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.GRANITE)
			.requires(Blocks.DIORITE)
			.requires(Items.QUARTZ)
			.unlockedBy("has_quartz", this.has(Items.QUARTZ))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.GRAY_DYE, 2)
			.requires(Items.BLACK_DYE)
			.requires(Items.WHITE_DYE)
			.unlockedBy("has_white_dye", this.has(Items.WHITE_DYE))
			.unlockedBy("has_black_dye", this.has(Items.BLACK_DYE))
			.save(this.output);
		this.threeByThreePacker(RecipeCategory.BUILDING_BLOCKS, Blocks.HAY_BLOCK, Items.WHEAT);
		this.pressurePlate(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.IRON_INGOT);
		this.shapeless(RecipeCategory.FOOD, Items.HONEY_BOTTLE, 4)
			.requires(Items.HONEY_BLOCK)
			.requires(Items.GLASS_BOTTLE, 4)
			.unlockedBy("has_honey_block", this.has(Blocks.HONEY_BLOCK))
			.save(this.output);
		this.twoByTwoPacker(RecipeCategory.REDSTONE, Blocks.HONEY_BLOCK, Items.HONEY_BOTTLE);
		this.twoByTwoPacker(RecipeCategory.DECORATIONS, Blocks.HONEYCOMB_BLOCK, Items.HONEYCOMB);
		this.shaped(RecipeCategory.REDSTONE, Blocks.HOPPER)
			.define('C', Blocks.CHEST)
			.define('I', Items.IRON_INGOT)
			.pattern("I I")
			.pattern("ICI")
			.pattern(" I ")
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.shapeless(RecipeCategory.TRANSPORTATION, Items.HOPPER_MINECART)
			.requires(Blocks.HOPPER)
			.requires(Items.MINECART)
			.unlockedBy("has_minecart", this.has(Items.MINECART))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.IRON_AXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.IRON_TOOL_MATERIALS)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlockedBy("has_iron_ingot", this.has(ItemTags.IRON_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.IRON_BARS, 16)
			.define('#', Items.IRON_INGOT)
			.pattern("###")
			.pattern("###")
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.IRON_BOOTS)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.IRON_CHESTPLATE)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.doorBuilder(Blocks.IRON_DOOR, Ingredient.of(Items.IRON_INGOT)).unlockedBy(getHasName(Items.IRON_INGOT), this.has(Items.IRON_INGOT)).save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.IRON_HELMET)
			.define('X', Items.IRON_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.IRON_HOE)
			.define('#', Items.STICK)
			.define('X', ItemTags.IRON_TOOL_MATERIALS)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlockedBy("has_iron_ingot", this.has(ItemTags.IRON_TOOL_MATERIALS))
			.save(this.output);
		this.nineBlockStorageRecipesRecipesWithCustomUnpacking(
			RecipeCategory.MISC, Items.IRON_INGOT, RecipeCategory.BUILDING_BLOCKS, Items.IRON_BLOCK, "iron_ingot_from_iron_block", "iron_ingot"
		);
		this.nineBlockStorageRecipesWithCustomPacking(
			RecipeCategory.MISC, Items.IRON_NUGGET, RecipeCategory.MISC, Items.IRON_INGOT, "iron_ingot_from_nuggets", "iron_ingot"
		);
		this.shaped(RecipeCategory.COMBAT, Items.IRON_LEGGINGS)
			.define('X', Items.IRON_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.IRON_PICKAXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.IRON_TOOL_MATERIALS)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlockedBy("has_iron_ingot", this.has(ItemTags.IRON_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.IRON_SHOVEL)
			.define('#', Items.STICK)
			.define('X', ItemTags.IRON_TOOL_MATERIALS)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_iron_ingot", this.has(ItemTags.IRON_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.IRON_SWORD)
			.define('#', Items.STICK)
			.define('X', ItemTags.IRON_TOOL_MATERIALS)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_iron_ingot", this.has(ItemTags.IRON_TOOL_MATERIALS))
			.save(this.output);
		this.twoByTwoPacker(RecipeCategory.REDSTONE, Blocks.IRON_TRAPDOOR, Items.IRON_INGOT);
		this.shaped(RecipeCategory.DECORATIONS, Items.ITEM_FRAME)
			.define('#', Items.STICK)
			.define('X', Items.LEATHER)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_leather", this.has(Items.LEATHER))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.JUKEBOX)
			.define('#', ItemTags.PLANKS)
			.define('X', Items.DIAMOND)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_diamond", this.has(Items.DIAMOND))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.LADDER, 3)
			.define('#', Items.STICK)
			.pattern("# #")
			.pattern("###")
			.pattern("# #")
			.unlockedBy("has_stick", this.has(Items.STICK))
			.save(this.output);
		this.nineBlockStorageRecipes(RecipeCategory.MISC, Items.LAPIS_LAZULI, RecipeCategory.BUILDING_BLOCKS, Items.LAPIS_BLOCK);
		this.shaped(RecipeCategory.TOOLS, Items.LEAD, 2)
			.define('~', Items.STRING)
			.define('O', Items.SLIME_BALL)
			.pattern("~~ ")
			.pattern("~O ")
			.pattern("  ~")
			.unlockedBy("has_slime_ball", this.has(Items.SLIME_BALL))
			.save(this.output);
		this.twoByTwoPacker(RecipeCategory.MISC, Items.LEATHER, Items.RABBIT_HIDE);
		this.shaped(RecipeCategory.COMBAT, Items.LEATHER_BOOTS)
			.define('X', Items.LEATHER)
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_leather", this.has(Items.LEATHER))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.LEATHER_CHESTPLATE)
			.define('X', Items.LEATHER)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlockedBy("has_leather", this.has(Items.LEATHER))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.LEATHER_HELMET)
			.define('X', Items.LEATHER)
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_leather", this.has(Items.LEATHER))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.LEATHER_LEGGINGS)
			.define('X', Items.LEATHER)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlockedBy("has_leather", this.has(Items.LEATHER))
			.save(this.output);
		this.shaped(RecipeCategory.MISC, Items.LEATHER_HORSE_ARMOR)
			.define('X', Items.LEATHER)
			.pattern("X X")
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_leather", this.has(Items.LEATHER))
			.save(this.output);
		this.shaped(RecipeCategory.REDSTONE, Blocks.LECTERN)
			.define('S', ItemTags.WOODEN_SLABS)
			.define('B', Blocks.BOOKSHELF)
			.pattern("SSS")
			.pattern(" B ")
			.pattern(" S ")
			.unlockedBy("has_book", this.has(Items.BOOK))
			.save(this.output);
		this.shaped(RecipeCategory.REDSTONE, Blocks.LEVER)
			.define('#', Blocks.COBBLESTONE)
			.define('X', Items.STICK)
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(this.output);
		this.oneToOneConversionRecipe(Items.LIGHT_BLUE_DYE, Blocks.BLUE_ORCHID, "light_blue_dye");
		this.shapeless(RecipeCategory.MISC, Items.LIGHT_BLUE_DYE, 2)
			.requires(Items.BLUE_DYE)
			.requires(Items.WHITE_DYE)
			.group("light_blue_dye")
			.unlockedBy("has_blue_dye", this.has(Items.BLUE_DYE))
			.unlockedBy("has_white_dye", this.has(Items.WHITE_DYE))
			.save(this.output, "light_blue_dye_from_blue_white_dye");
		this.oneToOneConversionRecipe(Items.LIGHT_GRAY_DYE, Blocks.AZURE_BLUET, "light_gray_dye");
		this.shapeless(RecipeCategory.MISC, Items.LIGHT_GRAY_DYE, 2)
			.requires(Items.GRAY_DYE)
			.requires(Items.WHITE_DYE)
			.group("light_gray_dye")
			.unlockedBy("has_gray_dye", this.has(Items.GRAY_DYE))
			.unlockedBy("has_white_dye", this.has(Items.WHITE_DYE))
			.save(this.output, "light_gray_dye_from_gray_white_dye");
		this.shapeless(RecipeCategory.MISC, Items.LIGHT_GRAY_DYE, 3)
			.requires(Items.BLACK_DYE)
			.requires(Items.WHITE_DYE, 2)
			.group("light_gray_dye")
			.unlockedBy("has_white_dye", this.has(Items.WHITE_DYE))
			.unlockedBy("has_black_dye", this.has(Items.BLACK_DYE))
			.save(this.output, "light_gray_dye_from_black_white_dye");
		this.oneToOneConversionRecipe(Items.LIGHT_GRAY_DYE, Blocks.OXEYE_DAISY, "light_gray_dye");
		this.oneToOneConversionRecipe(Items.LIGHT_GRAY_DYE, Blocks.WHITE_TULIP, "light_gray_dye");
		this.pressurePlate(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.GOLD_INGOT);
		this.shaped(RecipeCategory.REDSTONE, Blocks.LIGHTNING_ROD)
			.define('#', Items.COPPER_INGOT)
			.pattern("#")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_copper_ingot", this.has(Items.COPPER_INGOT))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.LIME_DYE, 2)
			.requires(Items.GREEN_DYE)
			.requires(Items.WHITE_DYE)
			.unlockedBy("has_green_dye", this.has(Items.GREEN_DYE))
			.unlockedBy("has_white_dye", this.has(Items.WHITE_DYE))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.JACK_O_LANTERN)
			.define('A', Blocks.CARVED_PUMPKIN)
			.define('B', Blocks.TORCH)
			.pattern("A")
			.pattern("B")
			.unlockedBy("has_carved_pumpkin", this.has(Blocks.CARVED_PUMPKIN))
			.save(this.output);
		this.oneToOneConversionRecipe(Items.MAGENTA_DYE, Blocks.ALLIUM, "magenta_dye");
		this.shapeless(RecipeCategory.MISC, Items.MAGENTA_DYE, 4)
			.requires(Items.BLUE_DYE)
			.requires(Items.RED_DYE, 2)
			.requires(Items.WHITE_DYE)
			.group("magenta_dye")
			.unlockedBy("has_blue_dye", this.has(Items.BLUE_DYE))
			.unlockedBy("has_rose_red", this.has(Items.RED_DYE))
			.unlockedBy("has_white_dye", this.has(Items.WHITE_DYE))
			.save(this.output, "magenta_dye_from_blue_red_white_dye");
		this.shapeless(RecipeCategory.MISC, Items.MAGENTA_DYE, 3)
			.requires(Items.BLUE_DYE)
			.requires(Items.RED_DYE)
			.requires(Items.PINK_DYE)
			.group("magenta_dye")
			.unlockedBy("has_pink_dye", this.has(Items.PINK_DYE))
			.unlockedBy("has_blue_dye", this.has(Items.BLUE_DYE))
			.unlockedBy("has_red_dye", this.has(Items.RED_DYE))
			.save(this.output, "magenta_dye_from_blue_red_pink");
		this.oneToOneConversionRecipe(Items.MAGENTA_DYE, Blocks.LILAC, "magenta_dye", 2);
		this.shapeless(RecipeCategory.MISC, Items.MAGENTA_DYE, 2)
			.requires(Items.PURPLE_DYE)
			.requires(Items.PINK_DYE)
			.group("magenta_dye")
			.unlockedBy("has_pink_dye", this.has(Items.PINK_DYE))
			.unlockedBy("has_purple_dye", this.has(Items.PURPLE_DYE))
			.save(this.output, "magenta_dye_from_purple_and_pink");
		this.twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, Blocks.MAGMA_BLOCK, Items.MAGMA_CREAM);
		this.shapeless(RecipeCategory.BREWING, Items.MAGMA_CREAM)
			.requires(Items.BLAZE_POWDER)
			.requires(Items.SLIME_BALL)
			.unlockedBy("has_blaze_powder", this.has(Items.BLAZE_POWDER))
			.save(this.output);
		this.shaped(RecipeCategory.MISC, Items.MAP)
			.define('#', Items.PAPER)
			.define('X', Items.COMPASS)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_compass", this.has(Items.COMPASS))
			.save(this.output);
		this.threeByThreePacker(RecipeCategory.BUILDING_BLOCKS, Blocks.MELON, Items.MELON_SLICE, "has_melon");
		this.shapeless(RecipeCategory.MISC, Items.MELON_SEEDS).requires(Items.MELON_SLICE).unlockedBy("has_melon", this.has(Items.MELON_SLICE)).save(this.output);
		this.shaped(RecipeCategory.TRANSPORTATION, Items.MINECART)
			.define('#', Items.IRON_INGOT)
			.pattern("# #")
			.pattern("###")
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_COBBLESTONE)
			.requires(Blocks.COBBLESTONE)
			.requires(Blocks.VINE)
			.group("mossy_cobblestone")
			.unlockedBy("has_vine", this.has(Blocks.VINE))
			.save(this.output, getConversionRecipeName(Blocks.MOSSY_COBBLESTONE, Blocks.VINE));
		this.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_STONE_BRICKS)
			.requires(Blocks.STONE_BRICKS)
			.requires(Blocks.VINE)
			.group("mossy_stone_bricks")
			.unlockedBy("has_vine", this.has(Blocks.VINE))
			.save(this.output, getConversionRecipeName(Blocks.MOSSY_STONE_BRICKS, Blocks.VINE));
		this.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_COBBLESTONE)
			.requires(Blocks.COBBLESTONE)
			.requires(Blocks.MOSS_BLOCK)
			.group("mossy_cobblestone")
			.unlockedBy("has_moss_block", this.has(Blocks.MOSS_BLOCK))
			.save(this.output, getConversionRecipeName(Blocks.MOSSY_COBBLESTONE, Blocks.MOSS_BLOCK));
		this.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_STONE_BRICKS)
			.requires(Blocks.STONE_BRICKS)
			.requires(Blocks.MOSS_BLOCK)
			.group("mossy_stone_bricks")
			.unlockedBy("has_moss_block", this.has(Blocks.MOSS_BLOCK))
			.save(this.output, getConversionRecipeName(Blocks.MOSSY_STONE_BRICKS, Blocks.MOSS_BLOCK));
		this.shapeless(RecipeCategory.FOOD, Items.MUSHROOM_STEW)
			.requires(Blocks.BROWN_MUSHROOM)
			.requires(Blocks.RED_MUSHROOM)
			.requires(Items.BOWL)
			.unlockedBy("has_mushroom_stew", this.has(Items.MUSHROOM_STEW))
			.unlockedBy("has_bowl", this.has(Items.BOWL))
			.unlockedBy("has_brown_mushroom", this.has(Blocks.BROWN_MUSHROOM))
			.unlockedBy("has_red_mushroom", this.has(Blocks.RED_MUSHROOM))
			.save(this.output);
		BuiltInRegistries.ITEM.stream().forEach(item -> {
			SuspiciousEffectHolder suspiciousEffectHolder = SuspiciousEffectHolder.tryGet(item);
			if (suspiciousEffectHolder != null) {
				this.suspiciousStew(item, suspiciousEffectHolder);
			}
		});
		this.twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, Blocks.NETHER_BRICKS, Items.NETHER_BRICK);
		this.threeByThreePacker(RecipeCategory.BUILDING_BLOCKS, Blocks.NETHER_WART_BLOCK, Items.NETHER_WART);
		this.shaped(RecipeCategory.REDSTONE, Blocks.NOTE_BLOCK)
			.define('#', ItemTags.PLANKS)
			.define('X', Items.REDSTONE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_redstone", this.has(Items.REDSTONE))
			.save(this.output);
		this.shaped(RecipeCategory.REDSTONE, Blocks.OBSERVER)
			.define('Q', Items.QUARTZ)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.pattern("###")
			.pattern("RRQ")
			.pattern("###")
			.unlockedBy("has_quartz", this.has(Items.QUARTZ))
			.save(this.output);
		this.oneToOneConversionRecipe(Items.ORANGE_DYE, Blocks.ORANGE_TULIP, "orange_dye");
		this.shapeless(RecipeCategory.MISC, Items.ORANGE_DYE, 2)
			.requires(Items.RED_DYE)
			.requires(Items.YELLOW_DYE)
			.group("orange_dye")
			.unlockedBy("has_red_dye", this.has(Items.RED_DYE))
			.unlockedBy("has_yellow_dye", this.has(Items.YELLOW_DYE))
			.save(this.output, "orange_dye_from_red_yellow");
		this.shaped(RecipeCategory.DECORATIONS, Items.PAINTING)
			.define('#', Items.STICK)
			.define('X', ItemTags.WOOL)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_wool", this.has(ItemTags.WOOL))
			.save(this.output);
		this.shaped(RecipeCategory.MISC, Items.PAPER, 3)
			.define('#', Blocks.SUGAR_CANE)
			.pattern("###")
			.unlockedBy("has_reeds", this.has(Blocks.SUGAR_CANE))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_PILLAR, 2)
			.define('#', Blocks.QUARTZ_BLOCK)
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_chiseled_quartz_block", this.has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlockedBy("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.unlockedBy("has_quartz_pillar", this.has(Blocks.QUARTZ_PILLAR))
			.save(this.output);
		this.threeByThreePacker(RecipeCategory.BUILDING_BLOCKS, Blocks.PACKED_ICE, Blocks.ICE);
		this.oneToOneConversionRecipe(Items.PINK_DYE, Blocks.PEONY, "pink_dye", 2);
		this.oneToOneConversionRecipe(Items.PINK_DYE, Blocks.PINK_TULIP, "pink_dye");
		this.shapeless(RecipeCategory.MISC, Items.PINK_DYE, 2)
			.requires(Items.RED_DYE)
			.requires(Items.WHITE_DYE)
			.group("pink_dye")
			.unlockedBy("has_white_dye", this.has(Items.WHITE_DYE))
			.unlockedBy("has_red_dye", this.has(Items.RED_DYE))
			.save(this.output, "pink_dye_from_red_white_dye");
		this.shaped(RecipeCategory.REDSTONE, Blocks.PISTON)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.define('T', ItemTags.PLANKS)
			.define('X', Items.IRON_INGOT)
			.pattern("TTT")
			.pattern("#X#")
			.pattern("#R#")
			.unlockedBy("has_redstone", this.has(Items.REDSTONE))
			.save(this.output);
		this.polished(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BASALT, Blocks.BASALT);
		this.twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE, Items.PRISMARINE_SHARD);
		this.threeByThreePacker(RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE_BRICKS, Items.PRISMARINE_SHARD);
		this.shapeless(RecipeCategory.FOOD, Items.PUMPKIN_PIE)
			.requires(Blocks.PUMPKIN)
			.requires(Items.SUGAR)
			.requires(Items.EGG)
			.unlockedBy("has_carved_pumpkin", this.has(Blocks.CARVED_PUMPKIN))
			.unlockedBy("has_pumpkin", this.has(Blocks.PUMPKIN))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.PUMPKIN_SEEDS, 4).requires(Blocks.PUMPKIN).unlockedBy("has_pumpkin", this.has(Blocks.PUMPKIN)).save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.PURPLE_DYE, 2)
			.requires(Items.BLUE_DYE)
			.requires(Items.RED_DYE)
			.unlockedBy("has_blue_dye", this.has(Items.BLUE_DYE))
			.unlockedBy("has_red_dye", this.has(Items.RED_DYE))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.SHULKER_BOX)
			.define('#', Blocks.CHEST)
			.define('-', Items.SHULKER_SHELL)
			.pattern("-")
			.pattern("#")
			.pattern("-")
			.unlockedBy("has_shulker_shell", this.has(Items.SHULKER_SHELL))
			.save(this.output);
		this.shulkerBoxRecipes();
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_BLOCK, 4)
			.define('F', Items.POPPED_CHORUS_FRUIT)
			.pattern("FF")
			.pattern("FF")
			.unlockedBy("has_chorus_fruit_popped", this.has(Items.POPPED_CHORUS_FRUIT))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_PILLAR)
			.define('#', Blocks.PURPUR_SLAB)
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_purpur_block", this.has(Blocks.PURPUR_BLOCK))
			.save(this.output);
		this.slabBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_SLAB, Ingredient.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR))
			.unlockedBy("has_purpur_block", this.has(Blocks.PURPUR_BLOCK))
			.save(this.output);
		this.stairBuilder(Blocks.PURPUR_STAIRS, Ingredient.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR))
			.unlockedBy("has_purpur_block", this.has(Blocks.PURPUR_BLOCK))
			.save(this.output);
		this.twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_BLOCK, Items.QUARTZ);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_BRICKS, 4)
			.define('#', Blocks.QUARTZ_BLOCK)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.save(this.output);
		this.slabBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_SLAB, Ingredient.of(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR))
			.unlockedBy("has_chiseled_quartz_block", this.has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlockedBy("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.unlockedBy("has_quartz_pillar", this.has(Blocks.QUARTZ_PILLAR))
			.save(this.output);
		this.stairBuilder(Blocks.QUARTZ_STAIRS, Ingredient.of(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR))
			.unlockedBy("has_chiseled_quartz_block", this.has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlockedBy("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.unlockedBy("has_quartz_pillar", this.has(Blocks.QUARTZ_PILLAR))
			.save(this.output);
		this.shapeless(RecipeCategory.FOOD, Items.RABBIT_STEW)
			.requires(Items.BAKED_POTATO)
			.requires(Items.COOKED_RABBIT)
			.requires(Items.BOWL)
			.requires(Items.CARROT)
			.requires(Blocks.BROWN_MUSHROOM)
			.group("rabbit_stew")
			.unlockedBy("has_cooked_rabbit", this.has(Items.COOKED_RABBIT))
			.save(this.output, getConversionRecipeName(Items.RABBIT_STEW, Items.BROWN_MUSHROOM));
		this.shapeless(RecipeCategory.FOOD, Items.RABBIT_STEW)
			.requires(Items.BAKED_POTATO)
			.requires(Items.COOKED_RABBIT)
			.requires(Items.BOWL)
			.requires(Items.CARROT)
			.requires(Blocks.RED_MUSHROOM)
			.group("rabbit_stew")
			.unlockedBy("has_cooked_rabbit", this.has(Items.COOKED_RABBIT))
			.save(this.output, getConversionRecipeName(Items.RABBIT_STEW, Items.RED_MUSHROOM));
		this.shaped(RecipeCategory.TRANSPORTATION, Blocks.RAIL, 16)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("X#X")
			.pattern("X X")
			.unlockedBy("has_minecart", this.has(Items.MINECART))
			.save(this.output);
		this.nineBlockStorageRecipes(RecipeCategory.REDSTONE, Items.REDSTONE, RecipeCategory.REDSTONE, Items.REDSTONE_BLOCK);
		this.shaped(RecipeCategory.REDSTONE, Blocks.REDSTONE_LAMP)
			.define('R', Items.REDSTONE)
			.define('G', Blocks.GLOWSTONE)
			.pattern(" R ")
			.pattern("RGR")
			.pattern(" R ")
			.unlockedBy("has_glowstone", this.has(Blocks.GLOWSTONE))
			.save(this.output);
		this.shaped(RecipeCategory.REDSTONE, Blocks.REDSTONE_TORCH)
			.define('#', Items.STICK)
			.define('X', Items.REDSTONE)
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_redstone", this.has(Items.REDSTONE))
			.save(this.output);
		this.oneToOneConversionRecipe(Items.RED_DYE, Items.BEETROOT, "red_dye");
		this.oneToOneConversionRecipe(Items.RED_DYE, Blocks.POPPY, "red_dye");
		this.oneToOneConversionRecipe(Items.RED_DYE, Blocks.ROSE_BUSH, "red_dye", 2);
		this.shapeless(RecipeCategory.MISC, Items.RED_DYE)
			.requires(Blocks.RED_TULIP)
			.group("red_dye")
			.unlockedBy("has_red_flower", this.has(Blocks.RED_TULIP))
			.save(this.output, "red_dye_from_tulip");
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.RED_NETHER_BRICKS)
			.define('W', Items.NETHER_WART)
			.define('N', Items.NETHER_BRICK)
			.pattern("NW")
			.pattern("WN")
			.unlockedBy("has_nether_wart", this.has(Items.NETHER_WART))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.RED_SANDSTONE)
			.define('#', Blocks.RED_SAND)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_sand", this.has(Blocks.RED_SAND))
			.save(this.output);
		this.slabBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.RED_SANDSTONE_SLAB, Ingredient.of(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE))
			.unlockedBy("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.unlockedBy("has_chiseled_red_sandstone", this.has(Blocks.CHISELED_RED_SANDSTONE))
			.save(this.output);
		this.stairBuilder(Blocks.RED_SANDSTONE_STAIRS, Ingredient.of(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE))
			.unlockedBy("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.unlockedBy("has_chiseled_red_sandstone", this.has(Blocks.CHISELED_RED_SANDSTONE))
			.unlockedBy("has_cut_red_sandstone", this.has(Blocks.CUT_RED_SANDSTONE))
			.save(this.output);
		this.shaped(RecipeCategory.REDSTONE, Blocks.REPEATER)
			.define('#', Blocks.REDSTONE_TORCH)
			.define('X', Items.REDSTONE)
			.define('I', Blocks.STONE)
			.pattern("#X#")
			.pattern("III")
			.unlockedBy("has_redstone_torch", this.has(Blocks.REDSTONE_TORCH))
			.save(this.output);
		this.twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, Blocks.SANDSTONE, Blocks.SAND);
		this.slabBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.SANDSTONE_SLAB, Ingredient.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE))
			.unlockedBy("has_sandstone", this.has(Blocks.SANDSTONE))
			.unlockedBy("has_chiseled_sandstone", this.has(Blocks.CHISELED_SANDSTONE))
			.save(this.output);
		this.stairBuilder(Blocks.SANDSTONE_STAIRS, Ingredient.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE))
			.unlockedBy("has_sandstone", this.has(Blocks.SANDSTONE))
			.unlockedBy("has_chiseled_sandstone", this.has(Blocks.CHISELED_SANDSTONE))
			.unlockedBy("has_cut_sandstone", this.has(Blocks.CUT_SANDSTONE))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.SEA_LANTERN)
			.define('S', Items.PRISMARINE_SHARD)
			.define('C', Items.PRISMARINE_CRYSTALS)
			.pattern("SCS")
			.pattern("CCC")
			.pattern("SCS")
			.unlockedBy("has_prismarine_crystals", this.has(Items.PRISMARINE_CRYSTALS))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.SHEARS)
			.define('#', Items.IRON_INGOT)
			.pattern(" #")
			.pattern("# ")
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.SHIELD)
			.define('W', ItemTags.WOODEN_TOOL_MATERIALS)
			.define('o', Items.IRON_INGOT)
			.pattern("WoW")
			.pattern("WWW")
			.pattern(" W ")
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.nineBlockStorageRecipes(RecipeCategory.MISC, Items.SLIME_BALL, RecipeCategory.REDSTONE, Items.SLIME_BLOCK);
		this.cut(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_RED_SANDSTONE, Blocks.RED_SANDSTONE);
		this.cut(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_SANDSTONE, Blocks.SANDSTONE);
		this.twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, Blocks.SNOW_BLOCK, Items.SNOWBALL);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.SNOW, 6)
			.define('#', Blocks.SNOW_BLOCK)
			.pattern("###")
			.unlockedBy("has_snowball", this.has(Items.SNOWBALL))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.SOUL_CAMPFIRE)
			.define('L', ItemTags.LOGS)
			.define('S', Items.STICK)
			.define('#', ItemTags.SOUL_FIRE_BASE_BLOCKS)
			.pattern(" S ")
			.pattern("S#S")
			.pattern("LLL")
			.unlockedBy("has_soul_sand", this.has(ItemTags.SOUL_FIRE_BASE_BLOCKS))
			.save(this.output);
		this.shaped(RecipeCategory.BREWING, Items.GLISTERING_MELON_SLICE)
			.define('#', Items.GOLD_NUGGET)
			.define('X', Items.MELON_SLICE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_melon", this.has(Items.MELON_SLICE))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.SPECTRAL_ARROW, 2)
			.define('#', Items.GLOWSTONE_DUST)
			.define('X', Items.ARROW)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlockedBy("has_glowstone_dust", this.has(Items.GLOWSTONE_DUST))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.SPYGLASS)
			.define('#', Items.AMETHYST_SHARD)
			.define('X', Items.COPPER_INGOT)
			.pattern(" # ")
			.pattern(" X ")
			.pattern(" X ")
			.unlockedBy("has_amethyst_shard", this.has(Items.AMETHYST_SHARD))
			.save(this.output);
		this.shaped(RecipeCategory.MISC, Items.STICK, 4)
			.define('#', ItemTags.PLANKS)
			.pattern("#")
			.pattern("#")
			.group("sticks")
			.unlockedBy("has_planks", this.has(ItemTags.PLANKS))
			.save(this.output);
		this.shaped(RecipeCategory.MISC, Items.STICK, 1)
			.define('#', Blocks.BAMBOO)
			.pattern("#")
			.pattern("#")
			.group("sticks")
			.unlockedBy("has_bamboo", this.has(Blocks.BAMBOO))
			.save(this.output, "stick_from_bamboo_item");
		this.shaped(RecipeCategory.REDSTONE, Blocks.STICKY_PISTON)
			.define('P', Blocks.PISTON)
			.define('S', Items.SLIME_BALL)
			.pattern("S")
			.pattern("P")
			.unlockedBy("has_slime_ball", this.has(Items.SLIME_BALL))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICKS, 4)
			.define('#', Blocks.STONE)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_stone", this.has(Blocks.STONE))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.STONE_AXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlockedBy("has_cobblestone", this.has(ItemTags.STONE_TOOL_MATERIALS))
			.save(this.output);
		this.slabBuilder(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICK_SLAB, Ingredient.of(Blocks.STONE_BRICKS))
			.unlockedBy("has_stone_bricks", this.has(ItemTags.STONE_BRICKS))
			.save(this.output);
		this.stairBuilder(Blocks.STONE_BRICK_STAIRS, Ingredient.of(Blocks.STONE_BRICKS))
			.unlockedBy("has_stone_bricks", this.has(ItemTags.STONE_BRICKS))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.STONE_HOE)
			.define('#', Items.STICK)
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlockedBy("has_cobblestone", this.has(ItemTags.STONE_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.STONE_PICKAXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlockedBy("has_cobblestone", this.has(ItemTags.STONE_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.STONE_SHOVEL)
			.define('#', Items.STICK)
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_cobblestone", this.has(ItemTags.STONE_TOOL_MATERIALS))
			.save(this.output);
		this.slab(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_STONE_SLAB, Blocks.SMOOTH_STONE);
		this.shaped(RecipeCategory.COMBAT, Items.STONE_SWORD)
			.define('#', Items.STICK)
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_cobblestone", this.has(ItemTags.STONE_TOOL_MATERIALS))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.WHITE_WOOL)
			.define('#', Items.STRING)
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_string", this.has(Items.STRING))
			.save(this.output, getConversionRecipeName(Blocks.WHITE_WOOL, Items.STRING));
		this.oneToOneConversionRecipe(Items.SUGAR, Blocks.SUGAR_CANE, "sugar");
		this.shapeless(RecipeCategory.MISC, Items.SUGAR, 3)
			.requires(Items.HONEY_BOTTLE)
			.group("sugar")
			.unlockedBy("has_honey_bottle", this.has(Items.HONEY_BOTTLE))
			.save(this.output, getConversionRecipeName(Items.SUGAR, Items.HONEY_BOTTLE));
		this.shaped(RecipeCategory.REDSTONE, Blocks.TARGET)
			.define('H', Items.HAY_BLOCK)
			.define('R', Items.REDSTONE)
			.pattern(" R ")
			.pattern("RHR")
			.pattern(" R ")
			.unlockedBy("has_redstone", this.has(Items.REDSTONE))
			.unlockedBy("has_hay_block", this.has(Blocks.HAY_BLOCK))
			.save(this.output);
		this.shaped(RecipeCategory.REDSTONE, Blocks.TNT)
			.define('#', Ingredient.of(Blocks.SAND, Blocks.RED_SAND))
			.define('X', Items.GUNPOWDER)
			.pattern("X#X")
			.pattern("#X#")
			.pattern("X#X")
			.unlockedBy("has_gunpowder", this.has(Items.GUNPOWDER))
			.save(this.output);
		this.shapeless(RecipeCategory.TRANSPORTATION, Items.TNT_MINECART)
			.requires(Blocks.TNT)
			.requires(Items.MINECART)
			.unlockedBy("has_minecart", this.has(Items.MINECART))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.TORCH, 4)
			.define('#', Items.STICK)
			.define('X', Ingredient.of(Items.COAL, Items.CHARCOAL))
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_stone_pickaxe", this.has(Items.STONE_PICKAXE))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.SOUL_TORCH, 4)
			.define('X', Ingredient.of(Items.COAL, Items.CHARCOAL))
			.define('#', Items.STICK)
			.define('S', ItemTags.SOUL_FIRE_BASE_BLOCKS)
			.pattern("X")
			.pattern("#")
			.pattern("S")
			.unlockedBy("has_soul_sand", this.has(ItemTags.SOUL_FIRE_BASE_BLOCKS))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.LANTERN)
			.define('#', Items.TORCH)
			.define('X', Items.IRON_NUGGET)
			.pattern("XXX")
			.pattern("X#X")
			.pattern("XXX")
			.unlockedBy("has_iron_nugget", this.has(Items.IRON_NUGGET))
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.SOUL_LANTERN)
			.define('#', Items.SOUL_TORCH)
			.define('X', Items.IRON_NUGGET)
			.pattern("XXX")
			.pattern("X#X")
			.pattern("XXX")
			.unlockedBy("has_soul_torch", this.has(Items.SOUL_TORCH))
			.save(this.output);
		this.shapeless(RecipeCategory.REDSTONE, Blocks.TRAPPED_CHEST)
			.requires(Blocks.CHEST)
			.requires(Blocks.TRIPWIRE_HOOK)
			.unlockedBy("has_tripwire_hook", this.has(Blocks.TRIPWIRE_HOOK))
			.save(this.output);
		this.shaped(RecipeCategory.REDSTONE, Blocks.TRIPWIRE_HOOK, 2)
			.define('#', ItemTags.PLANKS)
			.define('S', Items.STICK)
			.define('I', Items.IRON_INGOT)
			.pattern("I")
			.pattern("S")
			.pattern("#")
			.unlockedBy("has_string", this.has(Items.STRING))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.TURTLE_HELMET)
			.define('X', Items.TURTLE_SCUTE)
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_turtle_scute", this.has(Items.TURTLE_SCUTE))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.WOLF_ARMOR)
			.define('X', Items.ARMADILLO_SCUTE)
			.pattern("X  ")
			.pattern("XXX")
			.pattern("X X")
			.unlockedBy("has_armadillo_scute", this.has(Items.ARMADILLO_SCUTE))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.WHEAT, 9).requires(Blocks.HAY_BLOCK).unlockedBy("has_hay_block", this.has(Blocks.HAY_BLOCK)).save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.WHITE_DYE)
			.requires(Items.BONE_MEAL)
			.group("white_dye")
			.unlockedBy("has_bone_meal", this.has(Items.BONE_MEAL))
			.save(this.output);
		this.oneToOneConversionRecipe(Items.WHITE_DYE, Blocks.LILY_OF_THE_VALLEY, "white_dye");
		this.shaped(RecipeCategory.TOOLS, Items.WOODEN_AXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.WOODEN_TOOL_MATERIALS)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlockedBy("has_stick", this.has(Items.STICK))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.WOODEN_HOE)
			.define('#', Items.STICK)
			.define('X', ItemTags.WOODEN_TOOL_MATERIALS)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlockedBy("has_stick", this.has(Items.STICK))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.WOODEN_PICKAXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.WOODEN_TOOL_MATERIALS)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlockedBy("has_stick", this.has(Items.STICK))
			.save(this.output);
		this.shaped(RecipeCategory.TOOLS, Items.WOODEN_SHOVEL)
			.define('#', Items.STICK)
			.define('X', ItemTags.WOODEN_TOOL_MATERIALS)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlockedBy("has_stick", this.has(Items.STICK))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.WOODEN_SWORD)
			.define('#', Items.STICK)
			.define('X', ItemTags.WOODEN_TOOL_MATERIALS)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlockedBy("has_stick", this.has(Items.STICK))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.WRITABLE_BOOK)
			.requires(Items.BOOK)
			.requires(Items.INK_SAC)
			.requires(Items.FEATHER)
			.unlockedBy("has_book", this.has(Items.BOOK))
			.save(this.output);
		this.oneToOneConversionRecipe(Items.YELLOW_DYE, Blocks.DANDELION, "yellow_dye");
		this.oneToOneConversionRecipe(Items.YELLOW_DYE, Blocks.SUNFLOWER, "yellow_dye", 2);
		this.nineBlockStorageRecipes(RecipeCategory.FOOD, Items.DRIED_KELP, RecipeCategory.BUILDING_BLOCKS, Items.DRIED_KELP_BLOCK);
		this.shaped(RecipeCategory.MISC, Blocks.CONDUIT)
			.define('#', Items.NAUTILUS_SHELL)
			.define('X', Items.HEART_OF_THE_SEA)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlockedBy("has_nautilus_core", this.has(Items.HEART_OF_THE_SEA))
			.unlockedBy("has_nautilus_shell", this.has(Items.NAUTILUS_SHELL))
			.save(this.output);
		this.wall(RecipeCategory.DECORATIONS, Blocks.RED_SANDSTONE_WALL, Blocks.RED_SANDSTONE);
		this.wall(RecipeCategory.DECORATIONS, Blocks.STONE_BRICK_WALL, Blocks.STONE_BRICKS);
		this.wall(RecipeCategory.DECORATIONS, Blocks.SANDSTONE_WALL, Blocks.SANDSTONE);
		this.shapeless(RecipeCategory.MISC, Items.FIELD_MASONED_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Blocks.BRICKS)
			.unlockedBy("has_bricks", this.has(Blocks.BRICKS))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.BORDURE_INDENTED_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Blocks.VINE)
			.unlockedBy("has_vines", this.has(Blocks.VINE))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.CREEPER_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Items.CREEPER_HEAD)
			.unlockedBy("has_creeper_head", this.has(Items.CREEPER_HEAD))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.SKULL_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Items.WITHER_SKELETON_SKULL)
			.unlockedBy("has_wither_skeleton_skull", this.has(Items.WITHER_SKELETON_SKULL))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.FLOWER_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Blocks.OXEYE_DAISY)
			.unlockedBy("has_oxeye_daisy", this.has(Blocks.OXEYE_DAISY))
			.save(this.output);
		this.shapeless(RecipeCategory.MISC, Items.MOJANG_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Items.ENCHANTED_GOLDEN_APPLE)
			.unlockedBy("has_enchanted_golden_apple", this.has(Items.ENCHANTED_GOLDEN_APPLE))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.SCAFFOLDING, 6)
			.define('~', Items.STRING)
			.define('I', Blocks.BAMBOO)
			.pattern("I~I")
			.pattern("I I")
			.pattern("I I")
			.unlockedBy("has_bamboo", this.has(Blocks.BAMBOO))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.GRINDSTONE)
			.define('I', Items.STICK)
			.define('-', Blocks.STONE_SLAB)
			.define('#', ItemTags.PLANKS)
			.pattern("I-I")
			.pattern("# #")
			.unlockedBy("has_stone_slab", this.has(Blocks.STONE_SLAB))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.BLAST_FURNACE)
			.define('#', Blocks.SMOOTH_STONE)
			.define('X', Blocks.FURNACE)
			.define('I', Items.IRON_INGOT)
			.pattern("III")
			.pattern("IXI")
			.pattern("###")
			.unlockedBy("has_smooth_stone", this.has(Blocks.SMOOTH_STONE))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.SMOKER)
			.define('#', ItemTags.LOGS)
			.define('X', Blocks.FURNACE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlockedBy("has_furnace", this.has(Blocks.FURNACE))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.CARTOGRAPHY_TABLE)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.PAPER)
			.pattern("@@")
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_paper", this.has(Items.PAPER))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.SMITHING_TABLE)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.IRON_INGOT)
			.pattern("@@")
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.FLETCHING_TABLE)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.FLINT)
			.pattern("@@")
			.pattern("##")
			.pattern("##")
			.unlockedBy("has_flint", this.has(Items.FLINT))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.STONECUTTER)
			.define('I', Items.IRON_INGOT)
			.define('#', Blocks.STONE)
			.pattern(" I ")
			.pattern("###")
			.unlockedBy("has_stone", this.has(Blocks.STONE))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.LODESTONE)
			.define('S', Items.CHISELED_STONE_BRICKS)
			.define('#', Items.NETHERITE_INGOT)
			.pattern("SSS")
			.pattern("S#S")
			.pattern("SSS")
			.unlockedBy("has_netherite_ingot", this.has(Items.NETHERITE_INGOT))
			.save(this.output);
		this.nineBlockStorageRecipesRecipesWithCustomUnpacking(
			RecipeCategory.MISC, Items.NETHERITE_INGOT, RecipeCategory.BUILDING_BLOCKS, Items.NETHERITE_BLOCK, "netherite_ingot_from_netherite_block", "netherite_ingot"
		);
		this.shapeless(RecipeCategory.MISC, Items.NETHERITE_INGOT)
			.requires(Items.NETHERITE_SCRAP, 4)
			.requires(Items.GOLD_INGOT, 4)
			.group("netherite_ingot")
			.unlockedBy("has_netherite_scrap", this.has(Items.NETHERITE_SCRAP))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.RESPAWN_ANCHOR)
			.define('O', Blocks.CRYING_OBSIDIAN)
			.define('G', Blocks.GLOWSTONE)
			.pattern("OOO")
			.pattern("GGG")
			.pattern("OOO")
			.unlockedBy("has_obsidian", this.has(Blocks.CRYING_OBSIDIAN))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Blocks.CHAIN)
			.define('I', Items.IRON_INGOT)
			.define('N', Items.IRON_NUGGET)
			.pattern("N")
			.pattern("I")
			.pattern("N")
			.unlockedBy("has_iron_nugget", this.has(Items.IRON_NUGGET))
			.unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.TINTED_GLASS, 2)
			.define('G', Blocks.GLASS)
			.define('S', Items.AMETHYST_SHARD)
			.pattern(" S ")
			.pattern("SGS")
			.pattern(" S ")
			.unlockedBy("has_amethyst_shard", this.has(Items.AMETHYST_SHARD))
			.save(this.output);
		this.twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, Blocks.AMETHYST_BLOCK, Items.AMETHYST_SHARD);
		this.shaped(RecipeCategory.TOOLS, Items.RECOVERY_COMPASS)
			.define('C', Items.COMPASS)
			.define('S', Items.ECHO_SHARD)
			.pattern("SSS")
			.pattern("SCS")
			.pattern("SSS")
			.unlockedBy("has_echo_shard", this.has(Items.ECHO_SHARD))
			.save(this.output);
		this.shaped(RecipeCategory.REDSTONE, Items.CALIBRATED_SCULK_SENSOR)
			.define('#', Items.AMETHYST_SHARD)
			.define('X', Items.SCULK_SENSOR)
			.pattern(" # ")
			.pattern("#X#")
			.unlockedBy("has_amethyst_shard", this.has(Items.AMETHYST_SHARD))
			.save(this.output);
		this.threeByThreePacker(RecipeCategory.MISC, Items.MUSIC_DISC_5, Items.DISC_FRAGMENT_5);
		SpecialRecipeBuilder.special(ArmorDyeRecipe::new).save(this.output, "armor_dye");
		SpecialRecipeBuilder.special(BannerDuplicateRecipe::new).save(this.output, "banner_duplicate");
		SpecialRecipeBuilder.special(BookCloningRecipe::new).save(this.output, "book_cloning");
		SpecialRecipeBuilder.special(FireworkRocketRecipe::new).save(this.output, "firework_rocket");
		SpecialRecipeBuilder.special(FireworkStarRecipe::new).save(this.output, "firework_star");
		SpecialRecipeBuilder.special(FireworkStarFadeRecipe::new).save(this.output, "firework_star_fade");
		SpecialRecipeBuilder.special(MapCloningRecipe::new).save(this.output, "map_cloning");
		SpecialRecipeBuilder.special(MapExtendingRecipe::new).save(this.output, "map_extending");
		SpecialRecipeBuilder.special(RepairItemRecipe::new).save(this.output, "repair_item");
		SpecialRecipeBuilder.special(ShieldDecorationRecipe::new).save(this.output, "shield_decoration");
		SpecialRecipeBuilder.special(TippedArrowRecipe::new).save(this.output, "tipped_arrow");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.POTATO), RecipeCategory.FOOD, Items.BAKED_POTATO, 0.35F, 200)
			.unlockedBy("has_potato", this.has(Items.POTATO))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CLAY_BALL), RecipeCategory.MISC, Items.BRICK, 0.3F, 200)
			.unlockedBy("has_clay_ball", this.has(Items.CLAY_BALL))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(this.tag(ItemTags.LOGS_THAT_BURN), RecipeCategory.MISC, Items.CHARCOAL, 0.15F, 200)
			.unlockedBy("has_log", this.has(ItemTags.LOGS_THAT_BURN))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CHORUS_FRUIT), RecipeCategory.MISC, Items.POPPED_CHORUS_FRUIT, 0.1F, 200)
			.unlockedBy("has_chorus_fruit", this.has(Items.CHORUS_FRUIT))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.BEEF), RecipeCategory.FOOD, Items.COOKED_BEEF, 0.35F, 200)
			.unlockedBy("has_beef", this.has(Items.BEEF))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CHICKEN), RecipeCategory.FOOD, Items.COOKED_CHICKEN, 0.35F, 200)
			.unlockedBy("has_chicken", this.has(Items.CHICKEN))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.COD), RecipeCategory.FOOD, Items.COOKED_COD, 0.35F, 200)
			.unlockedBy("has_cod", this.has(Items.COD))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.KELP), RecipeCategory.FOOD, Items.DRIED_KELP, 0.1F, 200)
			.unlockedBy("has_kelp", this.has(Blocks.KELP))
			.save(this.output, getSmeltingRecipeName(Items.DRIED_KELP));
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.SALMON), RecipeCategory.FOOD, Items.COOKED_SALMON, 0.35F, 200)
			.unlockedBy("has_salmon", this.has(Items.SALMON))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.MUTTON), RecipeCategory.FOOD, Items.COOKED_MUTTON, 0.35F, 200)
			.unlockedBy("has_mutton", this.has(Items.MUTTON))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.PORKCHOP), RecipeCategory.FOOD, Items.COOKED_PORKCHOP, 0.35F, 200)
			.unlockedBy("has_porkchop", this.has(Items.PORKCHOP))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.RABBIT), RecipeCategory.FOOD, Items.COOKED_RABBIT, 0.35F, 200)
			.unlockedBy("has_rabbit", this.has(Items.RABBIT))
			.save(this.output);
		this.oreSmelting(COAL_SMELTABLES, RecipeCategory.MISC, Items.COAL, 0.1F, 200, "coal");
		this.oreSmelting(IRON_SMELTABLES, RecipeCategory.MISC, Items.IRON_INGOT, 0.7F, 200, "iron_ingot");
		this.oreSmelting(COPPER_SMELTABLES, RecipeCategory.MISC, Items.COPPER_INGOT, 0.7F, 200, "copper_ingot");
		this.oreSmelting(GOLD_SMELTABLES, RecipeCategory.MISC, Items.GOLD_INGOT, 1.0F, 200, "gold_ingot");
		this.oreSmelting(DIAMOND_SMELTABLES, RecipeCategory.MISC, Items.DIAMOND, 1.0F, 200, "diamond");
		this.oreSmelting(LAPIS_SMELTABLES, RecipeCategory.MISC, Items.LAPIS_LAZULI, 0.2F, 200, "lapis_lazuli");
		this.oreSmelting(REDSTONE_SMELTABLES, RecipeCategory.REDSTONE, Items.REDSTONE, 0.7F, 200, "redstone");
		this.oreSmelting(EMERALD_SMELTABLES, RecipeCategory.MISC, Items.EMERALD, 1.0F, 200, "emerald");
		this.nineBlockStorageRecipes(RecipeCategory.MISC, Items.RAW_IRON, RecipeCategory.BUILDING_BLOCKS, Items.RAW_IRON_BLOCK);
		this.nineBlockStorageRecipes(RecipeCategory.MISC, Items.RAW_COPPER, RecipeCategory.BUILDING_BLOCKS, Items.RAW_COPPER_BLOCK);
		this.nineBlockStorageRecipes(RecipeCategory.MISC, Items.RAW_GOLD, RecipeCategory.BUILDING_BLOCKS, Items.RAW_GOLD_BLOCK);
		SimpleCookingRecipeBuilder.smelting(this.tag(ItemTags.SMELTS_TO_GLASS), RecipeCategory.BUILDING_BLOCKS, Blocks.GLASS.asItem(), 0.1F, 200)
			.unlockedBy("has_smelts_to_glass", this.has(ItemTags.SMELTS_TO_GLASS))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.SEA_PICKLE), RecipeCategory.MISC, Items.LIME_DYE, 0.1F, 200)
			.unlockedBy("has_sea_pickle", this.has(Blocks.SEA_PICKLE))
			.save(this.output, getSmeltingRecipeName(Items.LIME_DYE));
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CACTUS.asItem()), RecipeCategory.MISC, Items.GREEN_DYE, 1.0F, 200)
			.unlockedBy("has_cactus", this.has(Blocks.CACTUS))
			.save(this.output);
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
			.unlockedBy("has_golden_pickaxe", this.has(Items.GOLDEN_PICKAXE))
			.unlockedBy("has_golden_shovel", this.has(Items.GOLDEN_SHOVEL))
			.unlockedBy("has_golden_axe", this.has(Items.GOLDEN_AXE))
			.unlockedBy("has_golden_hoe", this.has(Items.GOLDEN_HOE))
			.unlockedBy("has_golden_sword", this.has(Items.GOLDEN_SWORD))
			.unlockedBy("has_golden_helmet", this.has(Items.GOLDEN_HELMET))
			.unlockedBy("has_golden_chestplate", this.has(Items.GOLDEN_CHESTPLATE))
			.unlockedBy("has_golden_leggings", this.has(Items.GOLDEN_LEGGINGS))
			.unlockedBy("has_golden_boots", this.has(Items.GOLDEN_BOOTS))
			.unlockedBy("has_golden_horse_armor", this.has(Items.GOLDEN_HORSE_ARMOR))
			.save(this.output, getSmeltingRecipeName(Items.GOLD_NUGGET));
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
			.unlockedBy("has_iron_pickaxe", this.has(Items.IRON_PICKAXE))
			.unlockedBy("has_iron_shovel", this.has(Items.IRON_SHOVEL))
			.unlockedBy("has_iron_axe", this.has(Items.IRON_AXE))
			.unlockedBy("has_iron_hoe", this.has(Items.IRON_HOE))
			.unlockedBy("has_iron_sword", this.has(Items.IRON_SWORD))
			.unlockedBy("has_iron_helmet", this.has(Items.IRON_HELMET))
			.unlockedBy("has_iron_chestplate", this.has(Items.IRON_CHESTPLATE))
			.unlockedBy("has_iron_leggings", this.has(Items.IRON_LEGGINGS))
			.unlockedBy("has_iron_boots", this.has(Items.IRON_BOOTS))
			.unlockedBy("has_iron_horse_armor", this.has(Items.IRON_HORSE_ARMOR))
			.unlockedBy("has_chainmail_helmet", this.has(Items.CHAINMAIL_HELMET))
			.unlockedBy("has_chainmail_chestplate", this.has(Items.CHAINMAIL_CHESTPLATE))
			.unlockedBy("has_chainmail_leggings", this.has(Items.CHAINMAIL_LEGGINGS))
			.unlockedBy("has_chainmail_boots", this.has(Items.CHAINMAIL_BOOTS))
			.save(this.output, getSmeltingRecipeName(Items.IRON_NUGGET));
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CLAY), RecipeCategory.BUILDING_BLOCKS, Blocks.TERRACOTTA.asItem(), 0.35F, 200)
			.unlockedBy("has_clay_block", this.has(Blocks.CLAY))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.NETHERRACK), RecipeCategory.MISC, Items.NETHER_BRICK, 0.1F, 200)
			.unlockedBy("has_netherrack", this.has(Blocks.NETHERRACK))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.NETHER_QUARTZ_ORE), RecipeCategory.MISC, Items.QUARTZ, 0.2F, 200)
			.unlockedBy("has_nether_quartz_ore", this.has(Blocks.NETHER_QUARTZ_ORE))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.WET_SPONGE), RecipeCategory.BUILDING_BLOCKS, Blocks.SPONGE.asItem(), 0.15F, 200)
			.unlockedBy("has_wet_sponge", this.has(Blocks.WET_SPONGE))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.COBBLESTONE), RecipeCategory.BUILDING_BLOCKS, Blocks.STONE.asItem(), 0.1F, 200)
			.unlockedBy("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.STONE), RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_STONE.asItem(), 0.1F, 200)
			.unlockedBy("has_stone", this.has(Blocks.STONE))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.SANDSTONE), RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_SANDSTONE.asItem(), 0.1F, 200)
			.unlockedBy("has_sandstone", this.has(Blocks.SANDSTONE))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.RED_SANDSTONE), RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_RED_SANDSTONE.asItem(), 0.1F, 200)
			.unlockedBy("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.QUARTZ_BLOCK), RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_QUARTZ.asItem(), 0.1F, 200)
			.unlockedBy("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.STONE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_STONE_BRICKS.asItem(), 0.1F, 200)
			.unlockedBy("has_stone_bricks", this.has(Blocks.STONE_BRICKS))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BLACK_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.BLACK_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_black_terracotta", this.has(Blocks.BLACK_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BLUE_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.BLUE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_blue_terracotta", this.has(Blocks.BLUE_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BROWN_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.BROWN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_brown_terracotta", this.has(Blocks.BROWN_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CYAN_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.CYAN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_cyan_terracotta", this.has(Blocks.CYAN_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.GRAY_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.GRAY_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_gray_terracotta", this.has(Blocks.GRAY_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.GREEN_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.GREEN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_green_terracotta", this.has(Blocks.GREEN_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(
				Ingredient.of(Blocks.LIGHT_BLUE_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200
			)
			.unlockedBy("has_light_blue_terracotta", this.has(Blocks.LIGHT_BLUE_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(
				Ingredient.of(Blocks.LIGHT_GRAY_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.asItem(), 0.1F, 200
			)
			.unlockedBy("has_light_gray_terracotta", this.has(Blocks.LIGHT_GRAY_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LIME_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.LIME_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_lime_terracotta", this.has(Blocks.LIME_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(
				Ingredient.of(Blocks.MAGENTA_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.MAGENTA_GLAZED_TERRACOTTA.asItem(), 0.1F, 200
			)
			.unlockedBy("has_magenta_terracotta", this.has(Blocks.MAGENTA_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.ORANGE_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.ORANGE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_orange_terracotta", this.has(Blocks.ORANGE_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.PINK_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.PINK_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_pink_terracotta", this.has(Blocks.PINK_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.PURPLE_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.PURPLE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_purple_terracotta", this.has(Blocks.PURPLE_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.RED_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.RED_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_red_terracotta", this.has(Blocks.RED_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.WHITE_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.WHITE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_white_terracotta", this.has(Blocks.WHITE_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.YELLOW_TERRACOTTA), RecipeCategory.DECORATIONS, Blocks.YELLOW_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlockedBy("has_yellow_terracotta", this.has(Blocks.YELLOW_TERRACOTTA))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.ANCIENT_DEBRIS), RecipeCategory.MISC, Items.NETHERITE_SCRAP, 2.0F, 200)
			.unlockedBy("has_ancient_debris", this.has(Blocks.ANCIENT_DEBRIS))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BASALT), RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_BASALT, 0.1F, 200)
			.unlockedBy("has_basalt", this.has(Blocks.BASALT))
			.save(this.output);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.COBBLED_DEEPSLATE), RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE, 0.1F, 200)
			.unlockedBy("has_cobbled_deepslate", this.has(Blocks.COBBLED_DEEPSLATE))
			.save(this.output);
		this.oreBlasting(COAL_SMELTABLES, RecipeCategory.MISC, Items.COAL, 0.1F, 100, "coal");
		this.oreBlasting(IRON_SMELTABLES, RecipeCategory.MISC, Items.IRON_INGOT, 0.7F, 100, "iron_ingot");
		this.oreBlasting(COPPER_SMELTABLES, RecipeCategory.MISC, Items.COPPER_INGOT, 0.7F, 100, "copper_ingot");
		this.oreBlasting(GOLD_SMELTABLES, RecipeCategory.MISC, Items.GOLD_INGOT, 1.0F, 100, "gold_ingot");
		this.oreBlasting(DIAMOND_SMELTABLES, RecipeCategory.MISC, Items.DIAMOND, 1.0F, 100, "diamond");
		this.oreBlasting(LAPIS_SMELTABLES, RecipeCategory.MISC, Items.LAPIS_LAZULI, 0.2F, 100, "lapis_lazuli");
		this.oreBlasting(REDSTONE_SMELTABLES, RecipeCategory.REDSTONE, Items.REDSTONE, 0.7F, 100, "redstone");
		this.oreBlasting(EMERALD_SMELTABLES, RecipeCategory.MISC, Items.EMERALD, 1.0F, 100, "emerald");
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.NETHER_QUARTZ_ORE), RecipeCategory.MISC, Items.QUARTZ, 0.2F, 100)
			.unlockedBy("has_nether_quartz_ore", this.has(Blocks.NETHER_QUARTZ_ORE))
			.save(this.output, getBlastingRecipeName(Items.QUARTZ));
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
			.unlockedBy("has_golden_pickaxe", this.has(Items.GOLDEN_PICKAXE))
			.unlockedBy("has_golden_shovel", this.has(Items.GOLDEN_SHOVEL))
			.unlockedBy("has_golden_axe", this.has(Items.GOLDEN_AXE))
			.unlockedBy("has_golden_hoe", this.has(Items.GOLDEN_HOE))
			.unlockedBy("has_golden_sword", this.has(Items.GOLDEN_SWORD))
			.unlockedBy("has_golden_helmet", this.has(Items.GOLDEN_HELMET))
			.unlockedBy("has_golden_chestplate", this.has(Items.GOLDEN_CHESTPLATE))
			.unlockedBy("has_golden_leggings", this.has(Items.GOLDEN_LEGGINGS))
			.unlockedBy("has_golden_boots", this.has(Items.GOLDEN_BOOTS))
			.unlockedBy("has_golden_horse_armor", this.has(Items.GOLDEN_HORSE_ARMOR))
			.save(this.output, getBlastingRecipeName(Items.GOLD_NUGGET));
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
			.unlockedBy("has_iron_pickaxe", this.has(Items.IRON_PICKAXE))
			.unlockedBy("has_iron_shovel", this.has(Items.IRON_SHOVEL))
			.unlockedBy("has_iron_axe", this.has(Items.IRON_AXE))
			.unlockedBy("has_iron_hoe", this.has(Items.IRON_HOE))
			.unlockedBy("has_iron_sword", this.has(Items.IRON_SWORD))
			.unlockedBy("has_iron_helmet", this.has(Items.IRON_HELMET))
			.unlockedBy("has_iron_chestplate", this.has(Items.IRON_CHESTPLATE))
			.unlockedBy("has_iron_leggings", this.has(Items.IRON_LEGGINGS))
			.unlockedBy("has_iron_boots", this.has(Items.IRON_BOOTS))
			.unlockedBy("has_iron_horse_armor", this.has(Items.IRON_HORSE_ARMOR))
			.unlockedBy("has_chainmail_helmet", this.has(Items.CHAINMAIL_HELMET))
			.unlockedBy("has_chainmail_chestplate", this.has(Items.CHAINMAIL_CHESTPLATE))
			.unlockedBy("has_chainmail_leggings", this.has(Items.CHAINMAIL_LEGGINGS))
			.unlockedBy("has_chainmail_boots", this.has(Items.CHAINMAIL_BOOTS))
			.save(this.output, getBlastingRecipeName(Items.IRON_NUGGET));
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.ANCIENT_DEBRIS), RecipeCategory.MISC, Items.NETHERITE_SCRAP, 2.0F, 100)
			.unlockedBy("has_ancient_debris", this.has(Blocks.ANCIENT_DEBRIS))
			.save(this.output, getBlastingRecipeName(Items.NETHERITE_SCRAP));
		this.cookRecipes("smoking", RecipeSerializer.SMOKING_RECIPE, SmokingRecipe::new, 100);
		this.cookRecipes("campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING_RECIPE, CampfireCookingRecipe::new, 600);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_SLAB, Blocks.STONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_STAIRS, Blocks.STONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICKS, Blocks.STONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICK_SLAB, Blocks.STONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICK_STAIRS, Blocks.STONE);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_STONE_BRICKS)
			.unlockedBy("has_stone", this.has(Blocks.STONE))
			.save(this.output, "chiseled_stone_bricks_stone_from_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), RecipeCategory.DECORATIONS, Blocks.STONE_BRICK_WALL)
			.unlockedBy("has_stone", this.has(Blocks.STONE))
			.save(this.output, "stone_brick_walls_from_stone_stonecutting");
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_SANDSTONE, Blocks.SANDSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.SANDSTONE_SLAB, Blocks.SANDSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_SANDSTONE_SLAB, Blocks.SANDSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_SANDSTONE_SLAB, Blocks.CUT_SANDSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.SANDSTONE_STAIRS, Blocks.SANDSTONE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.SANDSTONE_WALL, Blocks.SANDSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_SANDSTONE, Blocks.SANDSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_RED_SANDSTONE, Blocks.RED_SANDSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.CUT_RED_SANDSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.RED_SANDSTONE_STAIRS, Blocks.RED_SANDSTONE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.RED_SANDSTONE_WALL, Blocks.RED_SANDSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_RED_SANDSTONE, Blocks.RED_SANDSTONE);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_SLAB, 2)
			.unlockedBy("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.save(this.output, "quartz_slab_from_stonecutting");
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_STAIRS, Blocks.QUARTZ_BLOCK);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_PILLAR, Blocks.QUARTZ_BLOCK);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_BLOCK);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.COBBLESTONE_STAIRS, Blocks.COBBLESTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.COBBLESTONE_SLAB, Blocks.COBBLESTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.COBBLESTONE_WALL, Blocks.COBBLESTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICK_SLAB, Blocks.STONE_BRICKS, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICKS);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE_BRICKS), RecipeCategory.DECORATIONS, Blocks.STONE_BRICK_WALL)
			.unlockedBy("has_stone_bricks", this.has(Blocks.STONE_BRICKS))
			.save(this.output, "stone_brick_wall_from_stone_bricks_stonecutting");
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_STONE_BRICKS, Blocks.STONE_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.BRICK_SLAB, Blocks.BRICKS, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.BRICK_STAIRS, Blocks.BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.BRICK_WALL, Blocks.BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.MUD_BRICK_SLAB, Blocks.MUD_BRICKS, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.MUD_BRICK_STAIRS, Blocks.MUD_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.MUD_BRICK_WALL, Blocks.MUD_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.NETHER_BRICK_SLAB, Blocks.NETHER_BRICKS, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.NETHER_BRICK_WALL, Blocks.NETHER_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_NETHER_BRICKS, Blocks.NETHER_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.RED_NETHER_BRICK_SLAB, Blocks.RED_NETHER_BRICKS, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.RED_NETHER_BRICK_STAIRS, Blocks.RED_NETHER_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.RED_NETHER_BRICK_WALL, Blocks.RED_NETHER_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_SLAB, Blocks.PURPUR_BLOCK, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_STAIRS, Blocks.PURPUR_BLOCK);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.PURPUR_PILLAR, Blocks.PURPUR_BLOCK);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE_SLAB, Blocks.PRISMARINE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE_STAIRS, Blocks.PRISMARINE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.PRISMARINE_WALL, Blocks.PRISMARINE);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE_BRICK_SLAB, 2)
			.unlockedBy("has_prismarine_brick", this.has(Blocks.PRISMARINE_BRICKS))
			.save(this.output, "prismarine_brick_slab_from_prismarine_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.PRISMARINE_BRICK_STAIRS)
			.unlockedBy("has_prismarine_brick", this.has(Blocks.PRISMARINE_BRICKS))
			.save(this.output, "prismarine_brick_stairs_from_prismarine_stonecutting");
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DARK_PRISMARINE_SLAB, Blocks.DARK_PRISMARINE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DARK_PRISMARINE_STAIRS, Blocks.DARK_PRISMARINE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.ANDESITE_SLAB, Blocks.ANDESITE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.ANDESITE_STAIRS, Blocks.ANDESITE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.ANDESITE_WALL, Blocks.ANDESITE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_ANDESITE, Blocks.ANDESITE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_ANDESITE_SLAB, Blocks.ANDESITE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_ANDESITE_STAIRS, Blocks.ANDESITE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_ANDESITE_SLAB, Blocks.POLISHED_ANDESITE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_ANDESITE_STAIRS, Blocks.POLISHED_ANDESITE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BASALT, Blocks.BASALT);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.GRANITE_SLAB, Blocks.GRANITE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.GRANITE_STAIRS, Blocks.GRANITE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.GRANITE_WALL, Blocks.GRANITE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_GRANITE, Blocks.GRANITE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_GRANITE_SLAB, Blocks.GRANITE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_GRANITE_STAIRS, Blocks.GRANITE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_GRANITE_SLAB, Blocks.POLISHED_GRANITE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_GRANITE_STAIRS, Blocks.POLISHED_GRANITE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DIORITE_SLAB, Blocks.DIORITE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DIORITE_STAIRS, Blocks.DIORITE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.DIORITE_WALL, Blocks.DIORITE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DIORITE, Blocks.DIORITE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DIORITE_SLAB, Blocks.DIORITE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DIORITE_STAIRS, Blocks.DIORITE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DIORITE_SLAB, Blocks.POLISHED_DIORITE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DIORITE_STAIRS, Blocks.POLISHED_DIORITE);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_STONE_BRICK_SLAB, 2)
			.unlockedBy("has_mossy_stone_bricks", this.has(Blocks.MOSSY_STONE_BRICKS))
			.save(this.output, "mossy_stone_brick_slab_from_mossy_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_STONE_BRICK_STAIRS)
			.unlockedBy("has_mossy_stone_bricks", this.has(Blocks.MOSSY_STONE_BRICKS))
			.save(this.output, "mossy_stone_brick_stairs_from_mossy_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), RecipeCategory.DECORATIONS, Blocks.MOSSY_STONE_BRICK_WALL)
			.unlockedBy("has_mossy_stone_bricks", this.has(Blocks.MOSSY_STONE_BRICKS))
			.save(this.output, "mossy_stone_brick_wall_from_mossy_stone_brick_stonecutting");
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.MOSSY_COBBLESTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_COBBLESTONE_STAIRS, Blocks.MOSSY_COBBLESTONE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.MOSSY_COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_SANDSTONE_STAIRS, Blocks.SMOOTH_SANDSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_RED_SANDSTONE_STAIRS, Blocks.SMOOTH_RED_SANDSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_QUARTZ_SLAB, Blocks.SMOOTH_QUARTZ, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_QUARTZ_STAIRS, Blocks.SMOOTH_QUARTZ);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICK_SLAB, 2)
			.unlockedBy("has_end_stone_brick", this.has(Blocks.END_STONE_BRICKS))
			.save(this.output, "end_stone_brick_slab_from_end_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICK_STAIRS)
			.unlockedBy("has_end_stone_brick", this.has(Blocks.END_STONE_BRICKS))
			.save(this.output, "end_stone_brick_stairs_from_end_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), RecipeCategory.DECORATIONS, Blocks.END_STONE_BRICK_WALL)
			.unlockedBy("has_end_stone_brick", this.has(Blocks.END_STONE_BRICKS))
			.save(this.output, "end_stone_brick_wall_from_end_stone_brick_stonecutting");
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICKS, Blocks.END_STONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICK_SLAB, Blocks.END_STONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.END_STONE_BRICK_STAIRS, Blocks.END_STONE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.END_STONE_BRICK_WALL, Blocks.END_STONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_STONE_SLAB, Blocks.SMOOTH_STONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.BLACKSTONE_SLAB, Blocks.BLACKSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.BLACKSTONE_STAIRS, Blocks.BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.BLACKSTONE_WALL, Blocks.BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE, Blocks.BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.POLISHED_BLACKSTONE_WALL, Blocks.BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.BLACKSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_STAIRS, Blocks.BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_POLISHED_BLACKSTONE, Blocks.BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.BLACKSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_STAIRS, Blocks.POLISHED_BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.POLISHED_BLACKSTONE_WALL, Blocks.POLISHED_BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICKS, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_COPPER_SLAB, Blocks.CUT_COPPER, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_COPPER_STAIRS, Blocks.CUT_COPPER);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_CUT_COPPER);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_COPPER, Blocks.COPPER_BLOCK, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_COPPER_STAIRS, Blocks.COPPER_BLOCK, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CUT_COPPER_SLAB, Blocks.COPPER_BLOCK, 8);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CUT_COPPER, Blocks.EXPOSED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.EXPOSED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.EXPOSED_COPPER, 8);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CUT_COPPER, Blocks.WEATHERED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WEATHERED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WEATHERED_COPPER, 8);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CUT_COPPER, Blocks.OXIDIZED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.OXIDIZED_COPPER, 8);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CUT_COPPER, Blocks.WAXED_COPPER_BLOCK, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_COPPER_BLOCK, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_COPPER_BLOCK, 8);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_COPPER, 8);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_COPPER, 8);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_COPPER, 8);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.COBBLED_DEEPSLATE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.COBBLED_DEEPSLATE_STAIRS, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.COBBLED_DEEPSLATE_WALL, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_DEEPSLATE, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DEEPSLATE, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DEEPSLATE_STAIRS, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.POLISHED_DEEPSLATE_WALL, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICKS, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_BRICK_WALL, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILES, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_TILE_WALL, Blocks.COBBLED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_DEEPSLATE_STAIRS, Blocks.POLISHED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.POLISHED_DEEPSLATE_WALL, Blocks.POLISHED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICKS, Blocks.POLISHED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.POLISHED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_BRICK_WALL, Blocks.POLISHED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILES, Blocks.POLISHED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.POLISHED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_TILE_WALL, Blocks.POLISHED_DEEPSLATE);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.DEEPSLATE_BRICKS, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.DEEPSLATE_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_BRICK_WALL, Blocks.DEEPSLATE_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILES, Blocks.DEEPSLATE_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_BRICKS, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_TILE_WALL, Blocks.DEEPSLATE_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_TILES, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILES);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.DEEPSLATE_TILE_WALL, Blocks.DEEPSLATE_TILES);
		smithingTrims().forEach(trimTemplate -> this.trimSmithing(trimTemplate.template(), trimTemplate.id()));
		this.netheriteSmithing(Items.DIAMOND_CHESTPLATE, RecipeCategory.COMBAT, Items.NETHERITE_CHESTPLATE);
		this.netheriteSmithing(Items.DIAMOND_LEGGINGS, RecipeCategory.COMBAT, Items.NETHERITE_LEGGINGS);
		this.netheriteSmithing(Items.DIAMOND_HELMET, RecipeCategory.COMBAT, Items.NETHERITE_HELMET);
		this.netheriteSmithing(Items.DIAMOND_BOOTS, RecipeCategory.COMBAT, Items.NETHERITE_BOOTS);
		this.netheriteSmithing(Items.DIAMOND_SWORD, RecipeCategory.COMBAT, Items.NETHERITE_SWORD);
		this.netheriteSmithing(Items.DIAMOND_AXE, RecipeCategory.TOOLS, Items.NETHERITE_AXE);
		this.netheriteSmithing(Items.DIAMOND_PICKAXE, RecipeCategory.TOOLS, Items.NETHERITE_PICKAXE);
		this.netheriteSmithing(Items.DIAMOND_HOE, RecipeCategory.TOOLS, Items.NETHERITE_HOE);
		this.netheriteSmithing(Items.DIAMOND_SHOVEL, RecipeCategory.TOOLS, Items.NETHERITE_SHOVEL);
		this.copySmithingTemplate(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.NETHERRACK);
		this.copySmithingTemplate(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
		this.copySmithingTemplate(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SANDSTONE);
		this.copySmithingTemplate(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
		this.copySmithingTemplate(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.MOSSY_COBBLESTONE);
		this.copySmithingTemplate(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLED_DEEPSLATE);
		this.copySmithingTemplate(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.END_STONE);
		this.copySmithingTemplate(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
		this.copySmithingTemplate(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.PRISMARINE);
		this.copySmithingTemplate(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Items.BLACKSTONE);
		this.copySmithingTemplate(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.NETHERRACK);
		this.copySmithingTemplate(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.PURPUR_BLOCK);
		this.copySmithingTemplate(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLED_DEEPSLATE);
		this.copySmithingTemplate(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, Items.TERRACOTTA);
		this.copySmithingTemplate(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, Items.TERRACOTTA);
		this.copySmithingTemplate(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, Items.TERRACOTTA);
		this.copySmithingTemplate(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.TERRACOTTA);
		this.copySmithingTemplate(Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, Items.BREEZE_ROD);
		this.copySmithingTemplate(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, Ingredient.of(Items.COPPER_BLOCK, Items.WAXED_COPPER_BLOCK));
		this.threeByThreePacker(RecipeCategory.BUILDING_BLOCKS, Blocks.BAMBOO_BLOCK, Items.BAMBOO);
		this.planksFromLogs(Blocks.BAMBOO_PLANKS, ItemTags.BAMBOO_BLOCKS, 2);
		this.mosaicBuilder(RecipeCategory.DECORATIONS, Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_SLAB);
		this.woodenBoat(Items.BAMBOO_RAFT, Blocks.BAMBOO_PLANKS);
		this.chestBoat(Items.BAMBOO_CHEST_RAFT, Items.BAMBOO_RAFT);
		this.hangingSign(Items.OAK_HANGING_SIGN, Blocks.STRIPPED_OAK_LOG);
		this.hangingSign(Items.SPRUCE_HANGING_SIGN, Blocks.STRIPPED_SPRUCE_LOG);
		this.hangingSign(Items.BIRCH_HANGING_SIGN, Blocks.STRIPPED_BIRCH_LOG);
		this.hangingSign(Items.JUNGLE_HANGING_SIGN, Blocks.STRIPPED_JUNGLE_LOG);
		this.hangingSign(Items.ACACIA_HANGING_SIGN, Blocks.STRIPPED_ACACIA_LOG);
		this.hangingSign(Items.CHERRY_HANGING_SIGN, Blocks.STRIPPED_CHERRY_LOG);
		this.hangingSign(Items.DARK_OAK_HANGING_SIGN, Blocks.STRIPPED_DARK_OAK_LOG);
		this.hangingSign(Items.MANGROVE_HANGING_SIGN, Blocks.STRIPPED_MANGROVE_LOG);
		this.hangingSign(Items.BAMBOO_HANGING_SIGN, Items.STRIPPED_BAMBOO_BLOCK);
		this.hangingSign(Items.CRIMSON_HANGING_SIGN, Blocks.STRIPPED_CRIMSON_STEM);
		this.hangingSign(Items.WARPED_HANGING_SIGN, Blocks.STRIPPED_WARPED_STEM);
		this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_BOOKSHELF)
			.define('#', ItemTags.PLANKS)
			.define('X', ItemTags.WOODEN_SLABS)
			.pattern("###")
			.pattern("XXX")
			.pattern("###")
			.unlockedBy("has_book", this.has(Items.BOOK))
			.save(this.output);
		this.oneToOneConversionRecipe(Items.ORANGE_DYE, Blocks.TORCHFLOWER, "orange_dye");
		this.oneToOneConversionRecipe(Items.CYAN_DYE, Blocks.PITCHER_PLANT, "cyan_dye", 2);
		this.planksFromLog(Blocks.CHERRY_PLANKS, ItemTags.CHERRY_LOGS, 4);
		this.woodFromLogs(Blocks.CHERRY_WOOD, Blocks.CHERRY_LOG);
		this.woodFromLogs(Blocks.STRIPPED_CHERRY_WOOD, Blocks.STRIPPED_CHERRY_LOG);
		this.woodenBoat(Items.CHERRY_BOAT, Blocks.CHERRY_PLANKS);
		this.chestBoat(Items.CHERRY_CHEST_BOAT, Items.CHERRY_BOAT);
		this.oneToOneConversionRecipe(Items.PINK_DYE, Items.PINK_PETALS, "pink_dye", 1);
		this.shaped(RecipeCategory.TOOLS, Items.BRUSH)
			.define('X', Items.FEATHER)
			.define('#', Items.COPPER_INGOT)
			.define('I', Items.STICK)
			.pattern("X")
			.pattern("#")
			.pattern("I")
			.unlockedBy("has_copper_ingot", this.has(Items.COPPER_INGOT))
			.save(this.output);
		this.shaped(RecipeCategory.DECORATIONS, Items.DECORATED_POT)
			.define('#', Items.BRICK)
			.pattern(" # ")
			.pattern("# #")
			.pattern(" # ")
			.unlockedBy("has_brick", this.has(ItemTags.DECORATED_POT_INGREDIENTS))
			.save(this.output, "decorated_pot_simple");
		SpecialRecipeBuilder.special(DecoratedPotRecipe::new).save(this.output, "decorated_pot");
		this.shaped(RecipeCategory.REDSTONE, Blocks.CRAFTER)
			.define('#', Items.IRON_INGOT)
			.define('C', Items.CRAFTING_TABLE)
			.define('R', Items.REDSTONE)
			.define('D', Items.DROPPER)
			.pattern("###")
			.pattern("#C#")
			.pattern("RDR")
			.unlockedBy("has_dropper", this.has(Items.DROPPER))
			.save(this.output);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.TUFF_SLAB, Blocks.TUFF, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.TUFF_STAIRS, Blocks.TUFF);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.TUFF_WALL, Blocks.TUFF);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_TUFF, Blocks.TUFF);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_TUFF, Blocks.TUFF);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_TUFF_SLAB, Blocks.TUFF, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_TUFF_STAIRS, Blocks.TUFF);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.POLISHED_TUFF_WALL, Blocks.TUFF);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.TUFF_BRICKS, Blocks.TUFF);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.TUFF_BRICK_SLAB, Blocks.TUFF, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.TUFF_BRICK_STAIRS, Blocks.TUFF);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.TUFF_BRICK_WALL, Blocks.TUFF);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_TUFF_BRICKS, Blocks.TUFF);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_TUFF_SLAB, Blocks.POLISHED_TUFF, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.POLISHED_TUFF_STAIRS, Blocks.POLISHED_TUFF);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.POLISHED_TUFF_WALL, Blocks.POLISHED_TUFF);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.TUFF_BRICKS, Blocks.POLISHED_TUFF);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.TUFF_BRICK_SLAB, Blocks.POLISHED_TUFF, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.TUFF_BRICK_STAIRS, Blocks.POLISHED_TUFF);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.TUFF_BRICK_WALL, Blocks.POLISHED_TUFF);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_TUFF_BRICKS, Blocks.POLISHED_TUFF);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.TUFF_BRICK_SLAB, Blocks.TUFF_BRICKS, 2);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.TUFF_BRICK_STAIRS, Blocks.TUFF_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.DECORATIONS, Blocks.TUFF_BRICK_WALL, Blocks.TUFF_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_TUFF_BRICKS, Blocks.TUFF_BRICKS);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_COPPER, Blocks.COPPER_BLOCK, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CHISELED_COPPER, Blocks.EXPOSED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CHISELED_COPPER, Blocks.WEATHERED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CHISELED_COPPER, Blocks.OXIDIZED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CHISELED_COPPER, Blocks.WAXED_COPPER_BLOCK, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_COPPER, Blocks.CUT_COPPER, 1);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_CHISELED_COPPER, Blocks.EXPOSED_CUT_COPPER, 1);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_CHISELED_COPPER, Blocks.WEATHERED_CUT_COPPER, 1);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_CHISELED_COPPER, Blocks.OXIDIZED_CUT_COPPER, 1);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_CHISELED_COPPER, Blocks.WAXED_CUT_COPPER, 1);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER, 1);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER, 1);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER, 1);
		this.grate(Blocks.COPPER_GRATE, Blocks.COPPER_BLOCK);
		this.grate(Blocks.EXPOSED_COPPER_GRATE, Blocks.EXPOSED_COPPER);
		this.grate(Blocks.WEATHERED_COPPER_GRATE, Blocks.WEATHERED_COPPER);
		this.grate(Blocks.OXIDIZED_COPPER_GRATE, Blocks.OXIDIZED_COPPER);
		this.grate(Blocks.WAXED_COPPER_GRATE, Blocks.WAXED_COPPER_BLOCK);
		this.grate(Blocks.WAXED_EXPOSED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER);
		this.grate(Blocks.WAXED_WEATHERED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER);
		this.grate(Blocks.WAXED_OXIDIZED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER);
		this.copperBulb(Blocks.COPPER_BULB, Blocks.COPPER_BLOCK);
		this.copperBulb(Blocks.EXPOSED_COPPER_BULB, Blocks.EXPOSED_COPPER);
		this.copperBulb(Blocks.WEATHERED_COPPER_BULB, Blocks.WEATHERED_COPPER);
		this.copperBulb(Blocks.OXIDIZED_COPPER_BULB, Blocks.OXIDIZED_COPPER);
		this.copperBulb(Blocks.WAXED_COPPER_BULB, Blocks.WAXED_COPPER_BLOCK);
		this.copperBulb(Blocks.WAXED_EXPOSED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER);
		this.copperBulb(Blocks.WAXED_WEATHERED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER);
		this.copperBulb(Blocks.WAXED_OXIDIZED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.COPPER_GRATE, Blocks.COPPER_BLOCK, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.EXPOSED_COPPER_GRATE, Blocks.EXPOSED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WEATHERED_COPPER_GRATE, Blocks.WEATHERED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.OXIDIZED_COPPER_GRATE, Blocks.OXIDIZED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_COPPER_GRATE, Blocks.WAXED_COPPER_BLOCK, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_EXPOSED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_WEATHERED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER, 4);
		this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, Blocks.WAXED_OXIDIZED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER, 4);
		this.shapeless(RecipeCategory.MISC, Items.WIND_CHARGE, 4)
			.requires(Items.BREEZE_ROD)
			.unlockedBy("has_breeze_rod", this.has(Items.BREEZE_ROD))
			.save(this.output);
		this.shaped(RecipeCategory.COMBAT, Items.MACE, 1)
			.define('I', Items.BREEZE_ROD)
			.define('#', Blocks.HEAVY_CORE)
			.pattern(" # ")
			.pattern(" I ")
			.unlockedBy("has_breeze_rod", this.has(Items.BREEZE_ROD))
			.unlockedBy("has_heavy_core", this.has(Blocks.HEAVY_CORE))
			.save(this.output);
		this.doorBuilder(Blocks.COPPER_DOOR, Ingredient.of(Items.COPPER_INGOT))
			.unlockedBy(getHasName(Items.COPPER_INGOT), this.has(Items.COPPER_INGOT))
			.save(this.output);
		this.trapdoorBuilder(Blocks.COPPER_TRAPDOOR, Ingredient.of(Items.COPPER_INGOT))
			.unlockedBy(getHasName(Items.COPPER_INGOT), this.has(Items.COPPER_INGOT))
			.save(this.output);
	}

	public static Stream<VanillaRecipeProvider.TrimTemplate> smithingTrims() {
		return Stream.of(
				Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE,
				Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE
			)
			.map(item -> new VanillaRecipeProvider.TrimTemplate(item, ResourceLocation.withDefaultNamespace(getItemName(item) + "_smithing_trim")));
	}

	private void shulkerBoxRecipes() {
		Ingredient ingredient = this.tag(ItemTags.SHULKER_BOXES);

		for (DyeColor dyeColor : DyeColor.values()) {
			TransmuteRecipeBuilder.transmute(
					RecipeCategory.DECORATIONS, ingredient, Ingredient.of(DyeItem.byColor(dyeColor)), ShulkerBoxBlock.getBlockByColor(dyeColor).asItem()
				)
				.group("shulker_box_dye")
				.unlockedBy("has_shulker_box", this.has(ItemTags.SHULKER_BOXES))
				.save(this.output);
		}
	}

	public static class Runner extends RecipeProvider.Runner {
		public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
			super(packOutput, completableFuture);
		}

		@Override
		protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
			return new VanillaRecipeProvider(provider, recipeOutput);
		}

		@Override
		public String getName() {
			return "Vanilla Recipes";
		}
	}

	public static record TrimTemplate(Item template, ResourceLocation id) {
	}
}
