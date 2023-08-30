package net.minecraft.data.recipes;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public abstract class RecipeProvider implements DataProvider {
	final PackOutput.PathProvider recipePathProvider;
	final PackOutput.PathProvider advancementPathProvider;
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
		final Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();
		final List<CompletableFuture<?>> list = new ArrayList();
		this.buildRecipes(new RecipeOutput() {
			@Override
			public void accept(FinishedRecipe finishedRecipe) {
				if (!set.add(finishedRecipe.id())) {
					throw new IllegalStateException("Duplicate recipe " + finishedRecipe.id());
				} else {
					list.add(DataProvider.saveStable(cachedOutput, finishedRecipe.serializeRecipe(), RecipeProvider.this.recipePathProvider.json(finishedRecipe.id())));
					AdvancementHolder advancementHolder = finishedRecipe.advancement();
					if (advancementHolder != null) {
						JsonObject jsonObject = advancementHolder.value().serializeToJson();
						list.add(DataProvider.saveStable(cachedOutput, jsonObject, RecipeProvider.this.advancementPathProvider.json(advancementHolder.id())));
					}
				}
			}

			@Override
			public Advancement.Builder advancement() {
				return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
			}
		});
		return CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new));
	}

	protected CompletableFuture<?> buildAdvancement(CachedOutput cachedOutput, AdvancementHolder advancementHolder) {
		return DataProvider.saveStable(cachedOutput, advancementHolder.value().serializeToJson(), this.advancementPathProvider.json(advancementHolder.id()));
	}

	protected abstract void buildRecipes(RecipeOutput recipeOutput);

	protected static void generateForEnabledBlockFamilies(RecipeOutput recipeOutput, FeatureFlagSet featureFlagSet) {
		BlockFamilies.getAllFamilies()
			.filter(blockFamily -> blockFamily.shouldGenerateRecipe(featureFlagSet))
			.forEach(blockFamily -> generateRecipes(recipeOutput, blockFamily));
	}

	protected static void oneToOneConversionRecipe(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2, @Nullable String string) {
		oneToOneConversionRecipe(recipeOutput, itemLike, itemLike2, string, 1);
	}

	protected static void oneToOneConversionRecipe(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2, @Nullable String string, int i) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, itemLike, i)
			.requires(itemLike2)
			.group(string)
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(recipeOutput, getConversionRecipeName(itemLike, itemLike2));
	}

	protected static void oreSmelting(
		RecipeOutput recipeOutput, List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, String string
	) {
		oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, list, recipeCategory, itemLike, f, i, string, "_from_smelting");
	}

	protected static void oreBlasting(
		RecipeOutput recipeOutput, List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, String string
	) {
		oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, list, recipeCategory, itemLike, f, i, string, "_from_blasting");
	}

	private static void oreCooking(
		RecipeOutput recipeOutput,
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
				.save(recipeOutput, getItemName(itemLike) + string2 + "_" + getItemName(itemLike2));
		}
	}

	protected static void netheriteSmithing(RecipeOutput recipeOutput, Item item, RecipeCategory recipeCategory, Item item2) {
		SmithingTransformRecipeBuilder.smithing(
				Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(item), Ingredient.of(Items.NETHERITE_INGOT), recipeCategory, item2
			)
			.unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
			.save(recipeOutput, getItemName(item2) + "_smithing");
	}

	protected static void trimSmithing(RecipeOutput recipeOutput, Item item, ResourceLocation resourceLocation) {
		SmithingTrimRecipeBuilder.smithingTrim(
				Ingredient.of(item), Ingredient.of(ItemTags.TRIMMABLE_ARMOR), Ingredient.of(ItemTags.TRIM_MATERIALS), RecipeCategory.MISC
			)
			.unlocks("has_smithing_trim_template", has(item))
			.save(recipeOutput, resourceLocation);
	}

	protected static void twoByTwoPacker(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 1)
			.define('#', itemLike2)
			.pattern("##")
			.pattern("##")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(recipeOutput);
	}

	protected static void threeByThreePacker(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2, String string) {
		ShapelessRecipeBuilder.shapeless(recipeCategory, itemLike).requires(itemLike2, 9).unlockedBy(string, has(itemLike2)).save(recipeOutput);
	}

	protected static void threeByThreePacker(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		threeByThreePacker(recipeOutput, recipeCategory, itemLike, itemLike2, getHasName(itemLike2));
	}

	protected static void planksFromLog(RecipeOutput recipeOutput, ItemLike itemLike, TagKey<Item> tagKey, int i) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, i)
			.requires(tagKey)
			.group("planks")
			.unlockedBy("has_log", has(tagKey))
			.save(recipeOutput);
	}

	protected static void planksFromLogs(RecipeOutput recipeOutput, ItemLike itemLike, TagKey<Item> tagKey, int i) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, i)
			.requires(tagKey)
			.group("planks")
			.unlockedBy("has_logs", has(tagKey))
			.save(recipeOutput);
	}

	protected static void woodFromLogs(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 3)
			.define('#', itemLike2)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlockedBy("has_log", has(itemLike2))
			.save(recipeOutput);
	}

	protected static void woodenBoat(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, itemLike)
			.define('#', itemLike2)
			.pattern("# #")
			.pattern("###")
			.group("boat")
			.unlockedBy("in_water", insideOf(Blocks.WATER))
			.save(recipeOutput);
	}

	protected static void chestBoat(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, itemLike)
			.requires(Blocks.CHEST)
			.requires(itemLike2)
			.group("chest_boat")
			.unlockedBy("has_boat", has(ItemTags.BOATS))
			.save(recipeOutput);
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

	protected static void pressurePlate(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		pressurePlateBuilder(RecipeCategory.REDSTONE, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(recipeOutput);
	}

	private static RecipeBuilder pressurePlateBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(recipeCategory, itemLike).define('#', ingredient).pattern("##");
	}

	protected static void slab(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		slabBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(recipeOutput);
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

	protected static void hangingSign(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 6)
			.group("hanging_sign")
			.define('#', itemLike2)
			.define('X', Items.CHAIN)
			.pattern("X X")
			.pattern("###")
			.pattern("###")
			.unlockedBy("has_stripped_logs", has(itemLike2))
			.save(recipeOutput);
	}

	protected static void colorBlockWithDye(RecipeOutput recipeOutput, List<Item> list, List<Item> list2, String string) {
		for (int i = 0; i < list.size(); i++) {
			Item item = (Item)list.get(i);
			Item item2 = (Item)list2.get(i);
			ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, item2)
				.requires(item)
				.requires(Ingredient.of(list2.stream().filter(item2x -> !item2x.equals(item2)).map(ItemStack::new)))
				.group(string)
				.unlockedBy("has_needed_dye", has(item))
				.save(recipeOutput, "dye_" + getItemName(item2));
		}
	}

	protected static void carpet(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 3)
			.define('#', itemLike2)
			.pattern("##")
			.group("carpet")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(recipeOutput);
	}

	protected static void bedFromPlanksAndWool(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike)
			.define('#', itemLike2)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(recipeOutput);
	}

	protected static void banner(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike)
			.define('#', itemLike2)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(recipeOutput);
	}

	protected static void stainedGlassFromGlassAndDye(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 8)
			.define('#', Blocks.GLASS)
			.define('X', itemLike2)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlockedBy("has_glass", has(Blocks.GLASS))
			.save(recipeOutput);
	}

	protected static void stainedGlassPaneFromStainedGlass(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 16)
			.define('#', itemLike2)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlockedBy("has_glass", has(itemLike2))
			.save(recipeOutput);
	}

	protected static void stainedGlassPaneFromGlassPaneAndDye(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', itemLike2)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlockedBy("has_glass_pane", has(Blocks.GLASS_PANE))
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(recipeOutput, getConversionRecipeName(itemLike, Blocks.GLASS_PANE));
	}

	protected static void coloredTerracottaFromTerracottaAndDye(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', itemLike2)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlockedBy("has_terracotta", has(Blocks.TERRACOTTA))
			.save(recipeOutput);
	}

	protected static void concretePowder(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, 8)
			.requires(itemLike2)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlockedBy("has_sand", has(Blocks.SAND))
			.unlockedBy("has_gravel", has(Blocks.GRAVEL))
			.save(recipeOutput);
	}

	protected static void candle(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, itemLike)
			.requires(Blocks.CANDLE)
			.requires(itemLike2)
			.group("dyed_candle")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(recipeOutput);
	}

	protected static void wall(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		wallBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(recipeOutput);
	}

	private static RecipeBuilder wallBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 6).define('#', ingredient).pattern("###").pattern("###");
	}

	protected static void polished(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		polishedBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(recipeOutput);
	}

	private static RecipeBuilder polishedBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 4).define('S', ingredient).pattern("SS").pattern("SS");
	}

	protected static void cut(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		cutBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(recipeOutput);
	}

	private static ShapedRecipeBuilder cutBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 4).define('#', ingredient).pattern("##").pattern("##");
	}

	protected static void chiseled(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		chiseledBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(getHasName(itemLike2), has(itemLike2)).save(recipeOutput);
	}

	protected static void mosaicBuilder(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(recipeCategory, itemLike)
			.define('#', itemLike2)
			.pattern("#")
			.pattern("#")
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(recipeOutput);
	}

	protected static ShapedRecipeBuilder chiseledBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
		return ShapedRecipeBuilder.shaped(recipeCategory, itemLike).define('#', ingredient).pattern("#").pattern("#");
	}

	protected static void stonecutterResultFromBase(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
		stonecutterResultFromBase(recipeOutput, recipeCategory, itemLike, itemLike2, 1);
	}

	protected static void stonecutterResultFromBase(RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2, int i) {
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(itemLike2), recipeCategory, itemLike, i)
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(recipeOutput, getConversionRecipeName(itemLike, itemLike2) + "_stonecutting");
	}

	private static void smeltingResultFromBase(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(itemLike2), RecipeCategory.BUILDING_BLOCKS, itemLike, 0.1F, 200)
			.unlockedBy(getHasName(itemLike2), has(itemLike2))
			.save(recipeOutput);
	}

	protected static void nineBlockStorageRecipes(
		RecipeOutput recipeOutput, RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2
	) {
		nineBlockStorageRecipes(
			recipeOutput, recipeCategory, itemLike, recipeCategory2, itemLike2, getSimpleRecipeName(itemLike2), null, getSimpleRecipeName(itemLike), null
		);
	}

	protected static void nineBlockStorageRecipesWithCustomPacking(
		RecipeOutput recipeOutput,
		RecipeCategory recipeCategory,
		ItemLike itemLike,
		RecipeCategory recipeCategory2,
		ItemLike itemLike2,
		String string,
		String string2
	) {
		nineBlockStorageRecipes(recipeOutput, recipeCategory, itemLike, recipeCategory2, itemLike2, string, string2, getSimpleRecipeName(itemLike), null);
	}

	protected static void nineBlockStorageRecipesRecipesWithCustomUnpacking(
		RecipeOutput recipeOutput,
		RecipeCategory recipeCategory,
		ItemLike itemLike,
		RecipeCategory recipeCategory2,
		ItemLike itemLike2,
		String string,
		String string2
	) {
		nineBlockStorageRecipes(recipeOutput, recipeCategory, itemLike, recipeCategory2, itemLike2, getSimpleRecipeName(itemLike2), null, string, string2);
	}

	private static void nineBlockStorageRecipes(
		RecipeOutput recipeOutput,
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
			.save(recipeOutput, new ResourceLocation(string3));
		ShapedRecipeBuilder.shaped(recipeCategory2, itemLike2)
			.define('#', itemLike)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.group(string2)
			.unlockedBy(getHasName(itemLike), has(itemLike))
			.save(recipeOutput, new ResourceLocation(string));
	}

	protected static void copySmithingTemplate(RecipeOutput recipeOutput, ItemLike itemLike, TagKey<Item> tagKey) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, itemLike, 2)
			.define('#', Items.DIAMOND)
			.define('C', tagKey)
			.define('S', itemLike)
			.pattern("#S#")
			.pattern("#C#")
			.pattern("###")
			.unlockedBy(getHasName(itemLike), has(itemLike))
			.save(recipeOutput);
	}

	protected static void copySmithingTemplate(RecipeOutput recipeOutput, ItemLike itemLike, ItemLike itemLike2) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, itemLike, 2)
			.define('#', Items.DIAMOND)
			.define('C', itemLike2)
			.define('S', itemLike)
			.pattern("#S#")
			.pattern("#C#")
			.pattern("###")
			.unlockedBy(getHasName(itemLike), has(itemLike))
			.save(recipeOutput);
	}

	protected static void cookRecipes(RecipeOutput recipeOutput, String string, RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer, int i) {
		simpleCookingRecipe(recipeOutput, string, recipeSerializer, i, Items.BEEF, Items.COOKED_BEEF, 0.35F);
		simpleCookingRecipe(recipeOutput, string, recipeSerializer, i, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35F);
		simpleCookingRecipe(recipeOutput, string, recipeSerializer, i, Items.COD, Items.COOKED_COD, 0.35F);
		simpleCookingRecipe(recipeOutput, string, recipeSerializer, i, Items.KELP, Items.DRIED_KELP, 0.1F);
		simpleCookingRecipe(recipeOutput, string, recipeSerializer, i, Items.SALMON, Items.COOKED_SALMON, 0.35F);
		simpleCookingRecipe(recipeOutput, string, recipeSerializer, i, Items.MUTTON, Items.COOKED_MUTTON, 0.35F);
		simpleCookingRecipe(recipeOutput, string, recipeSerializer, i, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35F);
		simpleCookingRecipe(recipeOutput, string, recipeSerializer, i, Items.POTATO, Items.BAKED_POTATO, 0.35F);
		simpleCookingRecipe(recipeOutput, string, recipeSerializer, i, Items.RABBIT, Items.COOKED_RABBIT, 0.35F);
	}

	private static void simpleCookingRecipe(
		RecipeOutput recipeOutput,
		String string,
		RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer,
		int i,
		ItemLike itemLike,
		ItemLike itemLike2,
		float f
	) {
		SimpleCookingRecipeBuilder.generic(Ingredient.of(itemLike), RecipeCategory.FOOD, itemLike2, f, i, recipeSerializer)
			.unlockedBy(getHasName(itemLike), has(itemLike))
			.save(recipeOutput, getItemName(itemLike2) + "_from_" + string);
	}

	protected static void waxRecipes(RecipeOutput recipeOutput) {
		((BiMap)HoneycombItem.WAXABLES.get())
			.forEach(
				(block, block2) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block2)
						.requires(block)
						.requires(Items.HONEYCOMB)
						.group(getItemName(block2))
						.unlockedBy(getHasName(block), has(block))
						.save(recipeOutput, getConversionRecipeName(block2, Items.HONEYCOMB))
			);
	}

	protected static void generateRecipes(RecipeOutput recipeOutput, BlockFamily blockFamily) {
		blockFamily.getVariants()
			.forEach(
				(variant, block) -> {
					BiFunction<ItemLike, ItemLike, RecipeBuilder> biFunction = (BiFunction<ItemLike, ItemLike, RecipeBuilder>)SHAPE_BUILDERS.get(variant);
					ItemLike itemLike = getBaseBlock(blockFamily, variant);
					if (biFunction != null) {
						RecipeBuilder recipeBuilder = (RecipeBuilder)biFunction.apply(block, itemLike);
						blockFamily.getRecipeGroupPrefix()
							.ifPresent(string -> recipeBuilder.group(string + (variant == BlockFamily.Variant.CUT ? "" : "_" + variant.getRecipeGroup())));
						recipeBuilder.unlockedBy((String)blockFamily.getRecipeUnlockedBy().orElseGet(() -> getHasName(itemLike)), has(itemLike));
						recipeBuilder.save(recipeOutput);
					}

					if (variant == BlockFamily.Variant.CRACKED) {
						smeltingResultFromBase(recipeOutput, block, itemLike);
					}
				}
			);
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

	private static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block block) {
		return CriteriaTriggers.ENTER_BLOCK.createCriterion(new EnterBlockTrigger.TriggerInstance(Optional.empty(), block, Optional.empty()));
	}

	private static Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints ints, ItemLike itemLike) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(itemLike).withCount(ints));
	}

	protected static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(itemLike));
	}

	protected static Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tagKey) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(tagKey));
	}

	private static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... builders) {
		return inventoryTrigger((ItemPredicate[])Arrays.stream(builders).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
	}

	private static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... itemPredicates) {
		return CriteriaTriggers.INVENTORY_CHANGED
			.createCriterion(
				new InventoryChangeTrigger.TriggerInstance(Optional.empty(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, List.of(itemPredicates))
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
