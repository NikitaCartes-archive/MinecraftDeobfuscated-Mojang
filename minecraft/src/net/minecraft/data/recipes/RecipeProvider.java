package net.minecraft.data.recipes;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public abstract class RecipeProvider implements DataProvider {
	private final PackOutput.PathProvider recipePathProvider;
	private final PackOutput.PathProvider advancementPathProvider;
	private static final Map<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>> SHAPE_BUILDERS = ImmutableMap.<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>>builder()
		.put(BlockFamily.Variant.BUTTON, (itemLike, itemLike2) -> buttonBuilder(itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.CHISELED, (itemLike, itemLike2) -> chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.CUT, (itemLike, itemLike2) -> cutBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.DOOR, (itemLike, itemLike2) -> doorBuilder(itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.CUSTOM_FENCE, (itemLike, itemLike2) -> fenceBuilder(itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.FENCE, (itemLike, itemLike2) -> fenceBuilder(itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.CUSTOM_FENCE_GATE, (itemLike, itemLike2) -> fenceGateBuilder(itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.FENCE_GATE, (itemLike, itemLike2) -> fenceGateBuilder(itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.SIGN, (itemLike, itemLike2) -> signBuilder(itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.SLAB, (itemLike, itemLike2) -> slabBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.STAIRS, (itemLike, itemLike2) -> stairBuilder(itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.PRESSURE_PLATE, (itemLike, itemLike2) -> pressurePlateBuilder(RecipeCategory.REDSTONE, itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.POLISHED, (itemLike, itemLike2) -> polishedBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.TRAPDOOR, (itemLike, itemLike2) -> trapdoorBuilder(itemLike, Ingredient.of(itemLike2)))
		.put(BlockFamily.Variant.WALL, (itemLike, itemLike2) -> wallBuilder(RecipeCategory.DECORATIONS, itemLike, Ingredient.of(itemLike2)))
		.build();

	public RecipeProvider(PackOutput packOutput) {
		this.recipePathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
		this.advancementPathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();
		List<CompletableFuture<?>> list = new ArrayList();
		this.buildRecipes(finishedRecipe -> {
			if (!set.add(finishedRecipe.getId())) {
				throw new IllegalStateException("Duplicate recipe " + finishedRecipe.getId());
			} else {
				list.add(DataProvider.saveStable(cachedOutput, finishedRecipe.serializeRecipe(), this.recipePathProvider.json(finishedRecipe.getId())));
				JsonObject jsonObject = finishedRecipe.serializeAdvancement();
				if (jsonObject != null) {
					list.add(DataProvider.saveStable(cachedOutput, jsonObject, this.advancementPathProvider.json(finishedRecipe.getAdvancementId())));
				}
			}
		});
		return CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new));
	}

	protected CompletableFuture<?> buildAdvancement(CachedOutput cachedOutput, ResourceLocation resourceLocation, Advancement.Builder builder) {
		return DataProvider.saveStable(cachedOutput, builder.serializeToJson(), this.advancementPathProvider.json(resourceLocation));
	}

	protected abstract void buildRecipes(Consumer<FinishedRecipe> consumer);

	protected static void generateForEnabledBlockFamilies(Consumer<FinishedRecipe> consumer, FeatureFlagSet featureFlagSet) {
		BlockFamilies.getAllFamilies()
			.filter(blockFamily -> blockFamily.shouldGenerateRecipe(featureFlagSet))
			.forEach(blockFamily -> generateRecipes(consumer, blockFamily));
	}

	protected static void oneToOneConversionRecipe(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, @Nullable String string) {
		oneToOneConversionRecipe(consumer, itemLike, itemLike2, string, 1);
	}

	protected static void oneToOneConversionRecipe(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, @Nullable String string, int i) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, itemLike, i)
			.requires(itemLike2)
			.group(string)
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer, getConversionRecipeName(itemLike, itemLike2));
	}

	protected static void oreSmelting(
		Consumer<FinishedRecipe> consumer, List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, String string
	) {
		oreCooking(consumer, RecipeSerializer.SMELTING_RECIPE, list, recipeCategory, itemLike, f, i, string, "_from_smelting");
	}

	protected static void oreBlasting(
		Consumer<FinishedRecipe> consumer, List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, String string
	) {
		oreCooking(consumer, RecipeSerializer.BLASTING_RECIPE, list, recipeCategory, itemLike, f, i, string, "_from_blasting");
	}

	private static void oreCooking(
		Consumer<FinishedRecipe> consumer,
		RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer,
		List<ItemLike> list,
		RecipeCategory recipeCategory,
		ItemLike itemLike,
		float f,
		int i,
		String string,
		String string2
	) {
		for (ItemLike itemLike2 : list) {
			SimpleCookingRecipeBuilder.generic(Ingredient.of(itemLike2), recipeCategory, itemLike, f, i, recipeSerializer)
				.group(string)
				.unlockedBy(getHasName(itemLike2), has(itemLike2))
				.save(consumer, getItemName(itemLike) + string2 + "_" + getItemName(itemLike2));
		}
	}

	protected static void netheriteSmithing(Consumer<FinishedRecipe> consumer, Item item, RecipeCategory recipeCategory, Item item2) {
		SmithingTransformRecipeBuilder.smithing(
				Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(item), Ingredient.of(Items.NETHERITE_INGOT), recipeCategory, item2
			)
			.unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
			.save(consumer, getItemName(item2) + "_smithing");
	}

	protected static void trimSmithing(Consumer<FinishedRecipe> consumer, Item item, ResourceLocation resourceLocation) {
		SmithingTrimRecipeBuilder.smithingTrim(
				Ingredient.of(item), Ingredient.of(ItemTags.TRIMMABLE_ARMOR), Ingredient.of(ItemTags.TRIM_MATERIALS), RecipeCategory.MISC
			)
			.unlocks("has_smithing_trim_template", has(item))
			.save(consumer, resourceLocation);
	}

	protected static void twoByTwoPacker(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 1)
			.define('#', itemLike2)
			.pattern("##")
			.pattern("##")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer);
	}

	protected static void threeByThreePacker(
		Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2, String string
	) {
		ShapelessRecipeBuilder.shapeless(recipeCategory, itemLike).requires(itemLike2, 9).unlockedBy(string, has(itemLike2)).save(consumer);
	}

	protected static void threeByThreePacker(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		threeByThreePacker(consumer, recipeCategory, itemLike, itemLike2, getHasName(itemLike2));
	}

	protected static void planksFromLog(Consumer<FinishedRecipe> consumer, ItemLike itemLike, TagKey<Item> tagKey, int i) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, i)
			.requires(tagKey)
			.group("planks")
			.unlockedBy("has_log", has(tagKey))
			.save(consumer);
	}

	protected static void planksFromLogs(Consumer<FinishedRecipe> consumer, ItemLike itemLike, TagKey<Item> tagKey, int i) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, i)
			.requires(tagKey)
			.group("planks")
			.unlockedBy("has_logs", has(tagKey))
			.save(consumer);
	}

	protected static void woodFromLogs(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 3)
			.define('#', itemLike2)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlockedBy("has_log", has(itemLike2))
			.save(consumer);
	}

	protected static void woodenBoat(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, itemLike)
			.define('#', itemLike2)
			.pattern("# #")
			.pattern("###")
			.group("boat")
			.unlockedBy("in_water", insideOf(Blocks.WATER))
			.save(consumer);
	}

	protected static void chestBoat(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, itemLike)
			.requires(Blocks.CHEST)
			.requires(itemLike2)
			.group("chest_boat")
			.unlockedBy("has_boat", has(ItemTags.BOATS))
			.save(consumer);
	}

	private static RecipeBuilder buttonBuilder(ItemLike itemLike, Ingredient ingredient) {
		return ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, itemLike).requires(ingredient);
	}

	protected static RecipeBuilder doorBuilder(ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemLike, 3).define('#', ingredient).pattern("##").pattern("##").pattern("##");
	}

	private static RecipeBuilder fenceBuilder(ItemLike itemLike, Ingredient ingredient) {
		int i = itemLike == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
		Item item = itemLike == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
		return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, i).define('W', ingredient).define('#', item).pattern("W#W").pattern("W#W");
	}

	private static RecipeBuilder fenceGateBuilder(ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemLike).define('#', Items.STICK).define('W', ingredient).pattern("#W#").pattern("#W#");
	}

	protected static void pressurePlate(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		pressurePlateBuilder(RecipeCategory.REDSTONE, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(consumer);
	}

	private static RecipeBuilder pressurePlateBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(recipeCategory, itemLike).define('#', ingredient).pattern("##");
	}

	protected static void slab(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		slabBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(consumer);
	}

	protected static RecipeBuilder slabBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 6).define('#', ingredient).pattern("###");
	}

	protected static RecipeBuilder stairBuilder(ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 4).define('#', ingredient).pattern("#  ").pattern("## ").pattern("###");
	}

	private static RecipeBuilder trapdoorBuilder(ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemLike, 2).define('#', ingredient).pattern("###").pattern("###");
	}

	private static RecipeBuilder signBuilder(ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 3)
			.group("sign")
			.define('#', ingredient)
			.define('X', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" X ");
	}

	protected static void hangingSign(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 6)
			.group("hanging_sign")
			.define('#', itemLike2)
			.define('X', Items.CHAIN)
			.pattern("X X")
			.pattern("###")
			.pattern("###")
			.unlockedBy("has_stripped_logs", has(itemLike2))
			.save(consumer);
	}

	protected static void coloredWoolFromWhiteWoolAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike)
			.requires(itemLike2)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlockedBy("has_white_wool", has(Blocks.WHITE_WOOL))
			.save(consumer);
	}

	protected static void carpet(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 3)
			.define('#', itemLike2)
			.pattern("##")
			.group("carpet")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer);
	}

	protected static void coloredCarpetFromWhiteCarpetAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', itemLike2)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlockedBy("has_white_carpet", has(Blocks.WHITE_CARPET))
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer, getConversionRecipeName(itemLike, Blocks.WHITE_CARPET));
	}

	protected static void bedFromPlanksAndWool(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike)
			.define('#', itemLike2)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer);
	}

	protected static void bedFromWhiteBedAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, itemLike)
			.requires(Items.WHITE_BED)
			.requires(itemLike2)
			.group("dyed_bed")
			.unlockedBy("has_bed", has(Items.WHITE_BED))
			.save(consumer, getConversionRecipeName(itemLike, Items.WHITE_BED));
	}

	protected static void banner(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike)
			.define('#', itemLike2)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer);
	}

	protected static void stainedGlassFromGlassAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 8)
			.define('#', Blocks.GLASS)
			.define('X', itemLike2)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlockedBy("has_glass", has(Blocks.GLASS))
			.save(consumer);
	}

	protected static void stainedGlassPaneFromStainedGlass(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 16)
			.define('#', itemLike2)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlockedBy("has_glass", has(itemLike2))
			.save(consumer);
	}

	protected static void stainedGlassPaneFromGlassPaneAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', itemLike2)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlockedBy("has_glass_pane", has(Blocks.GLASS_PANE))
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer, getConversionRecipeName(itemLike, Blocks.GLASS_PANE));
	}

	protected static void coloredTerracottaFromTerracottaAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', itemLike2)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlockedBy("has_terracotta", has(Blocks.TERRACOTTA))
			.save(consumer);
	}

	protected static void concretePowder(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, 8)
			.requires(itemLike2)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlockedBy("has_sand", has(Blocks.SAND))
			.unlockedBy("has_gravel", has(Blocks.GRAVEL))
			.save(consumer);
	}

	protected static void candle(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, itemLike)
			.requires(Blocks.CANDLE)
			.requires(itemLike2)
			.group("dyed_candle")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer);
	}

	protected static void wall(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		wallBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(consumer);
	}

	private static RecipeBuilder wallBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 6).define('#', ingredient).pattern("###").pattern("###");
	}

	protected static void polished(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		polishedBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(consumer);
	}

	private static RecipeBuilder polishedBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 4).define('S', ingredient).pattern("SS").pattern("SS");
	}

	protected static void cut(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		cutBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(consumer);
	}

	private static ShapedRecipeBuilder cutBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 4).define('#', ingredient).pattern("##").pattern("##");
	}

	protected static void chiseled(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		chiseledBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(consumer);
	}

	protected static void mosaicBuilder(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(recipeCategory, itemLike)
			.define('#', itemLike2)
			.pattern("#")
			.pattern("#")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer);
	}

	protected static ShapedRecipeBuilder chiseledBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(recipeCategory, itemLike).define('#', ingredient).pattern("#").pattern("#");
	}

	protected static void stonecutterResultFromBase(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		stonecutterResultFromBase(consumer, recipeCategory, itemLike, itemLike2, 1);
	}

	protected static void stonecutterResultFromBase(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2, int i) {
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(itemLike2), recipeCategory, itemLike, i)
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer, getConversionRecipeName(itemLike, itemLike2) + "_stonecutting");
	}

	private static void smeltingResultFromBase(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(itemLike2), RecipeCategory.BUILDING_BLOCKS, itemLike, 0.1F, 200)
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer);
	}

	protected static void nineBlockStorageRecipes(
		Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2
	) {
		nineBlockStorageRecipes(
			consumer, recipeCategory, itemLike, recipeCategory2, itemLike2, getSimpleRecipeName(itemLike2), null, getSimpleRecipeName(itemLike), null
		);
	}

	protected static void nineBlockStorageRecipesWithCustomPacking(
		Consumer<FinishedRecipe> consumer,
		RecipeCategory recipeCategory,
		ItemLike itemLike,
		RecipeCategory recipeCategory2,
		ItemLike itemLike2,
		String string,
		String string2
	) {
		nineBlockStorageRecipes(consumer, recipeCategory, itemLike, recipeCategory2, itemLike2, string, string2, getSimpleRecipeName(itemLike), null);
	}

	protected static void nineBlockStorageRecipesRecipesWithCustomUnpacking(
		Consumer<FinishedRecipe> consumer,
		RecipeCategory recipeCategory,
		ItemLike itemLike,
		RecipeCategory recipeCategory2,
		ItemLike itemLike2,
		String string,
		String string2
	) {
		nineBlockStorageRecipes(consumer, recipeCategory, itemLike, recipeCategory2, itemLike2, getSimpleRecipeName(itemLike2), null, string, string2);
	}

	private static void nineBlockStorageRecipes(
		Consumer<FinishedRecipe> consumer,
		RecipeCategory recipeCategory,
		ItemLike itemLike,
		RecipeCategory recipeCategory2,
		ItemLike itemLike2,
		String string,
		@Nullable String string2,
		String string3,
		@Nullable String string4
	) {
		ShapelessRecipeBuilder.shapeless(recipeCategory, itemLike, 9)
			.requires(itemLike2)
			.group(string4)
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(consumer, new ResourceLocation(string3));
		ShapedRecipeBuilder.shaped(recipeCategory2, itemLike2)
			.define('#', itemLike)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.group(string2)
			.unlockedBy(getHasName(itemLike), has(itemLike))
			.save(consumer, new ResourceLocation(string));
	}

	protected static void copySmithingTemplate(Consumer<FinishedRecipe> consumer, ItemLike itemLike, TagKey<Item> tagKey) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, itemLike, 2)
			.define('#', Items.DIAMOND)
			.define('C', tagKey)
			.define('S', itemLike)
			.pattern("#S#")
			.pattern("#C#")
			.pattern("###")
			.unlockedBy(getHasName(itemLike), has(itemLike))
			.save(consumer);
	}

	protected static void copySmithingTemplate(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, itemLike, 2)
			.define('#', Items.DIAMOND)
			.define('C', itemLike2)
			.define('S', itemLike)
			.pattern("#S#")
			.pattern("#C#")
			.pattern("###")
			.unlockedBy(getHasName(itemLike), has(itemLike))
			.save(consumer);
	}

	protected static void cookRecipes(Consumer<FinishedRecipe> consumer, String string, RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer, int i) {
		simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.BEEF, Items.COOKED_BEEF, 0.35F);
		simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35F);
		simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.COD, Items.COOKED_COD, 0.35F);
		simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.KELP, Items.DRIED_KELP, 0.1F);
		simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.SALMON, Items.COOKED_SALMON, 0.35F);
		simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.MUTTON, Items.COOKED_MUTTON, 0.35F);
		simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35F);
		simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.POTATO, Items.BAKED_POTATO, 0.35F);
		simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.RABBIT, Items.COOKED_RABBIT, 0.35F);
	}

	private static void simpleCookingRecipe(
		Consumer<FinishedRecipe> consumer,
		String string,
		RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer,
		int i,
		ItemLike itemLike,
		ItemLike itemLike2,
		float f
	) {
		SimpleCookingRecipeBuilder.generic(Ingredient.of(itemLike), RecipeCategory.FOOD, itemLike2, f, i, recipeSerializer)
			.unlockedBy(getHasName(itemLike), has(itemLike))
			.save(consumer, getItemName(itemLike2) + "_from_" + string);
	}

	protected static void waxRecipes(Consumer<FinishedRecipe> consumer) {
		((BiMap)HoneycombItem.WAXABLES.get())
			.forEach(
				(block, block2) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block2)
						.requires(block)
						.requires(Items.HONEYCOMB)
						.group(getItemName(block2))
						.unlockedBy(getHasName(block), has(block))
						.save(consumer, getConversionRecipeName(block2, Items.HONEYCOMB))
			);
	}

	protected static void generateRecipes(Consumer<FinishedRecipe> consumer, BlockFamily blockFamily) {
		blockFamily.getVariants().forEach((variant, block) -> {
			BiFunction<ItemLike, ItemLike, RecipeBuilder> biFunction = (BiFunction<ItemLike, ItemLike, RecipeBuilder>)SHAPE_BUILDERS.get(variant);
			ItemLike itemLike = getBaseBlock(blockFamily, variant);
			if (biFunction != null) {
				RecipeBuilder recipeBuilder = (RecipeBuilder)biFunction.apply(block, itemLike);
				blockFamily.getRecipeGroupPrefix().ifPresent(string -> recipeBuilder.group(string + (variant == BlockFamily.Variant.CUT ? "" : "_" + variant.getName())));
				recipeBuilder.unlockedBy((String)blockFamily.getRecipeUnlockedBy().orElseGet(() -> getHasName(itemLike)), has(itemLike));
				recipeBuilder.save(consumer);
			}

			if (variant == BlockFamily.Variant.CRACKED) {
				smeltingResultFromBase(consumer, block, itemLike);
			}
		});
	}

	private static Block getBaseBlock(BlockFamily blockFamily, BlockFamily.Variant variant) {
		if (variant == BlockFamily.Variant.CHISELED) {
			if (!blockFamily.getVariants().containsKey(BlockFamily.Variant.SLAB)) {
				throw new IllegalStateException("Slab is not defined for the family.");
			} else {
				return blockFamily.get(BlockFamily.Variant.SLAB);
			}
		} else {
			return blockFamily.getBaseBlock();
		}
	}

	private static EnterBlockTrigger.TriggerInstance insideOf(Block block) {
		return new EnterBlockTrigger.TriggerInstance(EntityPredicate.Composite.ANY, block, StatePropertiesPredicate.ANY);
	}

	private static InventoryChangeTrigger.TriggerInstance has(MinMaxBounds.Ints ints, ItemLike itemLike) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(itemLike).withCount(ints).build());
	}

	protected static InventoryChangeTrigger.TriggerInstance has(ItemLike itemLike) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(itemLike).build());
	}

	protected static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tagKey) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(tagKey).build());
	}

	private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... itemPredicates) {
		return new InventoryChangeTrigger.TriggerInstance(
			EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, itemPredicates
		);
	}

	protected static String getHasName(ItemLike itemLike) {
		return "has_" + getItemName(itemLike);
	}

	protected static String getItemName(ItemLike itemLike) {
		return BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath();
	}

	protected static String getSimpleRecipeName(ItemLike itemLike) {
		return getItemName(itemLike);
	}

	protected static String getConversionRecipeName(ItemLike itemLike, ItemLike itemLike2) {
		return getItemName(itemLike) + "_from_" + getItemName(itemLike2);
	}

	protected static String getSmeltingRecipeName(ItemLike itemLike) {
		return getItemName(itemLike) + "_from_smelting";
	}

	protected static String getBlastingRecipeName(ItemLike itemLike) {
		return getItemName(itemLike) + "_from_blasting";
	}

	@Override
	public final String getName() {
		return "Recipes";
	}
}
