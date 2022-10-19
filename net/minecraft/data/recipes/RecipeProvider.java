/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Registry;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.UpgradeRecipeBuilder;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class RecipeProvider
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput.PathProvider recipePathProvider;
    private final PackOutput.PathProvider advancementPathProvider;
    private static final Map<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>> SHAPE_BUILDERS = ImmutableMap.builder().put(BlockFamily.Variant.BUTTON, (itemLike, itemLike2) -> RecipeProvider.buttonBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.CHISELED, (itemLike, itemLike2) -> RecipeProvider.chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.CUT, (itemLike, itemLike2) -> RecipeProvider.cutBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.DOOR, (itemLike, itemLike2) -> RecipeProvider.doorBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.CUSTOM_FENCE, (itemLike, itemLike2) -> RecipeProvider.fenceBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.FENCE, (itemLike, itemLike2) -> RecipeProvider.fenceBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.CUSTOM_FENCE_GATE, (itemLike, itemLike2) -> RecipeProvider.fenceGateBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.FENCE_GATE, (itemLike, itemLike2) -> RecipeProvider.fenceGateBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.SIGN, (itemLike, itemLike2) -> RecipeProvider.signBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.SLAB, (itemLike, itemLike2) -> RecipeProvider.slabBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.STAIRS, (itemLike, itemLike2) -> RecipeProvider.stairBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.PRESSURE_PLATE, (itemLike, itemLike2) -> RecipeProvider.pressurePlateBuilder(RecipeCategory.REDSTONE, itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.POLISHED, (itemLike, itemLike2) -> RecipeProvider.polishedBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.TRAPDOOR, (itemLike, itemLike2) -> RecipeProvider.trapdoorBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.WALL, (itemLike, itemLike2) -> RecipeProvider.wallBuilder(RecipeCategory.DECORATIONS, itemLike, Ingredient.of(itemLike2))).build();

    public RecipeProvider(PackOutput packOutput) {
        this.recipePathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.advancementPathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
    }

    @Override
    public void run(CachedOutput cachedOutput) {
        HashSet set = Sets.newHashSet();
        this.buildRecipes(finishedRecipe -> {
            if (!set.add(finishedRecipe.getId())) {
                throw new IllegalStateException("Duplicate recipe " + finishedRecipe.getId());
            }
            RecipeProvider.saveRecipe(cachedOutput, finishedRecipe.serializeRecipe(), this.recipePathProvider.json(finishedRecipe.getId()));
            JsonObject jsonObject = finishedRecipe.serializeAdvancement();
            if (jsonObject != null) {
                RecipeProvider.saveAdvancement(cachedOutput, jsonObject, this.advancementPathProvider.json(finishedRecipe.getAdvancementId()));
            }
        });
    }

    protected void buildAdvancement(CachedOutput cachedOutput, ResourceLocation resourceLocation, Advancement.Builder builder) {
        RecipeProvider.saveAdvancement(cachedOutput, builder.serializeToJson(), this.advancementPathProvider.json(resourceLocation));
    }

    private static void saveRecipe(CachedOutput cachedOutput, JsonObject jsonObject, Path path) {
        try {
            DataProvider.saveStable(cachedOutput, jsonObject, path);
        } catch (IOException iOException) {
            LOGGER.error("Couldn't save recipe {}", (Object)path, (Object)iOException);
        }
    }

    private static void saveAdvancement(CachedOutput cachedOutput, JsonObject jsonObject, Path path) {
        try {
            DataProvider.saveStable(cachedOutput, jsonObject, path);
        } catch (IOException iOException) {
            LOGGER.error("Couldn't save recipe advancement {}", (Object)path, (Object)iOException);
        }
    }

    protected abstract void buildRecipes(Consumer<FinishedRecipe> var1);

    protected static void generateForEnabledBlockFamilies(Consumer<FinishedRecipe> consumer, FeatureFlagSet featureFlagSet) {
        BlockFamilies.getAllFamilies().filter(blockFamily -> blockFamily.shouldGenerateRecipe(featureFlagSet)).forEach(blockFamily -> RecipeProvider.generateRecipes(consumer, blockFamily));
    }

    protected static void oneToOneConversionRecipe(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, @Nullable String string) {
        RecipeProvider.oneToOneConversionRecipe(consumer, itemLike, itemLike2, string, 1);
    }

    protected static void oneToOneConversionRecipe(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, @Nullable String string, int i) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, itemLike, i).requires(itemLike2).group(string).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, RecipeProvider.getConversionRecipeName(itemLike, itemLike2));
    }

    protected static void oreSmelting(Consumer<FinishedRecipe> consumer, List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, String string) {
        RecipeProvider.oreCooking(consumer, RecipeSerializer.SMELTING_RECIPE, list, recipeCategory, itemLike, f, i, string, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> consumer, List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, String string) {
        RecipeProvider.oreCooking(consumer, RecipeSerializer.BLASTING_RECIPE, list, recipeCategory, itemLike, f, i, string, "_from_blasting");
    }

    private static void oreCooking(Consumer<FinishedRecipe> consumer, RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer, List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, String string, String string2) {
        for (ItemLike itemLike2 : list) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemLike2), recipeCategory, itemLike, f, i, recipeSerializer).group(string).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, RecipeProvider.getItemName(itemLike) + string2 + "_" + RecipeProvider.getItemName(itemLike2));
        }
    }

    protected static void netheriteSmithing(Consumer<FinishedRecipe> consumer, Item item, RecipeCategory recipeCategory, Item item2) {
        UpgradeRecipeBuilder.smithing(Ingredient.of(item), Ingredient.of(Items.NETHERITE_INGOT), recipeCategory, item2).unlocks("has_netherite_ingot", RecipeProvider.has(Items.NETHERITE_INGOT)).save(consumer, RecipeProvider.getItemName(item2) + "_smithing");
    }

    protected static void twoByTwoPacker(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 1).define(Character.valueOf('#'), itemLike2).pattern("##").pattern("##").unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    protected static void planksFromLog(Consumer<FinishedRecipe> consumer, ItemLike itemLike, TagKey<Item> tagKey) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, 4).requires(tagKey).group("planks").unlockedBy("has_log", RecipeProvider.has(tagKey)).save(consumer);
    }

    protected static void planksFromLogs(Consumer<FinishedRecipe> consumer, ItemLike itemLike, TagKey<Item> tagKey) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, 4).requires(tagKey).group("planks").unlockedBy("has_logs", RecipeProvider.has(tagKey)).save(consumer);
    }

    protected static void woodFromLogs(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 3).define(Character.valueOf('#'), itemLike2).pattern("##").pattern("##").group("bark").unlockedBy("has_log", RecipeProvider.has(itemLike2)).save(consumer);
    }

    protected static void woodenBoat(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, itemLike).define(Character.valueOf('#'), itemLike2).pattern("# #").pattern("###").group("boat").unlockedBy("in_water", RecipeProvider.insideOf(Blocks.WATER)).save(consumer);
    }

    protected static void chestBoat(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, itemLike).requires(Blocks.CHEST).requires(itemLike2).group("chest_boat").unlockedBy("has_boat", RecipeProvider.has(ItemTags.BOATS)).save(consumer);
    }

    private static RecipeBuilder buttonBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, itemLike).requires(ingredient);
    }

    protected static RecipeBuilder doorBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemLike, 3).define(Character.valueOf('#'), ingredient).pattern("##").pattern("##").pattern("##");
    }

    private static RecipeBuilder fenceBuilder(ItemLike itemLike, Ingredient ingredient) {
        int i = itemLike == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
        Item item = itemLike == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
        return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, i).define(Character.valueOf('W'), ingredient).define(Character.valueOf('#'), item).pattern("W#W").pattern("W#W");
    }

    private static RecipeBuilder fenceGateBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemLike).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('W'), ingredient).pattern("#W#").pattern("#W#");
    }

    protected static void pressurePlate(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.pressurePlateBuilder(RecipeCategory.REDSTONE, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static RecipeBuilder pressurePlateBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike).define(Character.valueOf('#'), ingredient).pattern("##");
    }

    protected static void slab(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.slabBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    protected static RecipeBuilder slabBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 6).define(Character.valueOf('#'), ingredient).pattern("###");
    }

    protected static RecipeBuilder stairBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 4).define(Character.valueOf('#'), ingredient).pattern("#  ").pattern("## ").pattern("###");
    }

    private static RecipeBuilder trapdoorBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemLike, 2).define(Character.valueOf('#'), ingredient).pattern("###").pattern("###");
    }

    private static RecipeBuilder signBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 3).group("sign").define(Character.valueOf('#'), ingredient).define(Character.valueOf('X'), Items.STICK).pattern("###").pattern("###").pattern(" X ");
    }

    protected static void hangingSign(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.hangingSign(consumer, itemLike, itemLike2, 6);
    }

    protected static void hangingSign(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, int i) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, i).group("hanging_sign").define(Character.valueOf('#'), itemLike2).define(Character.valueOf('X'), Items.CHAIN).pattern("X X").pattern("###").pattern("###").unlockedBy("has_stripped_logs", RecipeProvider.has(ItemTags.STRIPPED_LOGS)).save(consumer);
    }

    protected static void coloredWoolFromWhiteWoolAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike).requires(itemLike2).requires(Blocks.WHITE_WOOL).group("wool").unlockedBy("has_white_wool", RecipeProvider.has(Blocks.WHITE_WOOL)).save(consumer);
    }

    protected static void carpet(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 3).define(Character.valueOf('#'), itemLike2).pattern("##").group("carpet").unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    protected static void coloredCarpetFromWhiteCarpetAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 8).define(Character.valueOf('#'), Blocks.WHITE_CARPET).define(Character.valueOf('$'), itemLike2).pattern("###").pattern("#$#").pattern("###").group("carpet").unlockedBy("has_white_carpet", RecipeProvider.has(Blocks.WHITE_CARPET)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, RecipeProvider.getConversionRecipeName(itemLike, Blocks.WHITE_CARPET));
    }

    protected static void bedFromPlanksAndWool(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike).define(Character.valueOf('#'), itemLike2).define(Character.valueOf('X'), ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    protected static void bedFromWhiteBedAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, itemLike).requires(Items.WHITE_BED).requires(itemLike2).group("dyed_bed").unlockedBy("has_bed", RecipeProvider.has(Items.WHITE_BED)).save(consumer, RecipeProvider.getConversionRecipeName(itemLike, Items.WHITE_BED));
    }

    protected static void banner(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike).define(Character.valueOf('#'), itemLike2).define(Character.valueOf('|'), Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    protected static void stainedGlassFromGlassAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 8).define(Character.valueOf('#'), Blocks.GLASS).define(Character.valueOf('X'), itemLike2).pattern("###").pattern("#X#").pattern("###").group("stained_glass").unlockedBy("has_glass", RecipeProvider.has(Blocks.GLASS)).save(consumer);
    }

    protected static void stainedGlassPaneFromStainedGlass(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 16).define(Character.valueOf('#'), itemLike2).pattern("###").pattern("###").group("stained_glass_pane").unlockedBy("has_glass", RecipeProvider.has(itemLike2)).save(consumer);
    }

    protected static void stainedGlassPaneFromGlassPaneAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemLike, 8).define(Character.valueOf('#'), Blocks.GLASS_PANE).define(Character.valueOf('$'), itemLike2).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").unlockedBy("has_glass_pane", RecipeProvider.has(Blocks.GLASS_PANE)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, RecipeProvider.getConversionRecipeName(itemLike, Blocks.GLASS_PANE));
    }

    protected static void coloredTerracottaFromTerracottaAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 8).define(Character.valueOf('#'), Blocks.TERRACOTTA).define(Character.valueOf('X'), itemLike2).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").unlockedBy("has_terracotta", RecipeProvider.has(Blocks.TERRACOTTA)).save(consumer);
    }

    protected static void concretePowder(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, 8).requires(itemLike2).requires(Blocks.SAND, 4).requires(Blocks.GRAVEL, 4).group("concrete_powder").unlockedBy("has_sand", RecipeProvider.has(Blocks.SAND)).unlockedBy("has_gravel", RecipeProvider.has(Blocks.GRAVEL)).save(consumer);
    }

    protected static void candle(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, itemLike).requires(Blocks.CANDLE).requires(itemLike2).group("dyed_candle").unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    protected static void wall(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.wallBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static RecipeBuilder wallBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 6).define(Character.valueOf('#'), ingredient).pattern("###").pattern("###");
    }

    protected static void polished(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.polishedBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static RecipeBuilder polishedBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 4).define(Character.valueOf('S'), ingredient).pattern("SS").pattern("SS");
    }

    protected static void cut(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.cutBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static ShapedRecipeBuilder cutBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 4).define(Character.valueOf('#'), ingredient).pattern("##").pattern("##");
    }

    protected static void chiseled(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.chiseledBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    protected static void mosaicBuilder(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(recipeCategory, itemLike).define(Character.valueOf('#'), itemLike2).pattern("#").pattern("#").unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    protected static ShapedRecipeBuilder chiseledBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike).define(Character.valueOf('#'), ingredient).pattern("#").pattern("#");
    }

    protected static void stonecutterResultFromBase(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.stonecutterResultFromBase(consumer, recipeCategory, itemLike, itemLike2, 1);
    }

    protected static void stonecutterResultFromBase(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2, int i) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(itemLike2), recipeCategory, itemLike, i).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, RecipeProvider.getConversionRecipeName(itemLike, itemLike2) + "_stonecutting");
    }

    private static void smeltingResultFromBase(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(itemLike2), RecipeCategory.BUILDING_BLOCKS, itemLike, 0.1f, 200).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    protected static void nineBlockStorageRecipes(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2) {
        RecipeProvider.nineBlockStorageRecipes(consumer, recipeCategory, itemLike, recipeCategory2, itemLike2, RecipeProvider.getSimpleRecipeName(itemLike2), null, RecipeProvider.getSimpleRecipeName(itemLike), null);
    }

    protected static void nineBlockStorageRecipesWithCustomPacking(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2, String string, String string2) {
        RecipeProvider.nineBlockStorageRecipes(consumer, recipeCategory, itemLike, recipeCategory2, itemLike2, string, string2, RecipeProvider.getSimpleRecipeName(itemLike), null);
    }

    protected static void nineBlockStorageRecipesRecipesWithCustomUnpacking(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2, String string, String string2) {
        RecipeProvider.nineBlockStorageRecipes(consumer, recipeCategory, itemLike, recipeCategory2, itemLike2, RecipeProvider.getSimpleRecipeName(itemLike2), null, string, string2);
    }

    private static void nineBlockStorageRecipes(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2, String string, @Nullable String string2, String string3, @Nullable String string4) {
        ShapelessRecipeBuilder.shapeless(recipeCategory, itemLike, 9).requires(itemLike2).group(string4).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, new ResourceLocation(string3));
        ShapedRecipeBuilder.shaped(recipeCategory2, itemLike2).define(Character.valueOf('#'), itemLike).pattern("###").pattern("###").pattern("###").group(string2).unlockedBy(RecipeProvider.getHasName(itemLike), RecipeProvider.has(itemLike)).save(consumer, new ResourceLocation(string));
    }

    protected static void cookRecipes(Consumer<FinishedRecipe> consumer, String string, RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer, int i) {
        RecipeProvider.simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.BEEF, Items.COOKED_BEEF, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.COD, Items.COOKED_COD, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.KELP, Items.DRIED_KELP, 0.1f);
        RecipeProvider.simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.SALMON, Items.COOKED_SALMON, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.MUTTON, Items.COOKED_MUTTON, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.POTATO, Items.BAKED_POTATO, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, recipeSerializer, i, Items.RABBIT, Items.COOKED_RABBIT, 0.35f);
    }

    private static void simpleCookingRecipe(Consumer<FinishedRecipe> consumer, String string, RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer, int i, ItemLike itemLike, ItemLike itemLike2, float f) {
        SimpleCookingRecipeBuilder.generic(Ingredient.of(itemLike), RecipeCategory.FOOD, itemLike2, f, i, recipeSerializer).unlockedBy(RecipeProvider.getHasName(itemLike), RecipeProvider.has(itemLike)).save(consumer, RecipeProvider.getItemName(itemLike2) + "_from_" + string);
    }

    protected static void waxRecipes(Consumer<FinishedRecipe> consumer) {
        HoneycombItem.WAXABLES.get().forEach((block, block2) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block2).requires((ItemLike)block).requires(Items.HONEYCOMB).group(RecipeProvider.getItemName(block2)).unlockedBy(RecipeProvider.getHasName(block), RecipeProvider.has(block)).save(consumer, RecipeProvider.getConversionRecipeName(block2, Items.HONEYCOMB)));
    }

    protected static void generateRecipes(Consumer<FinishedRecipe> consumer, BlockFamily blockFamily) {
        blockFamily.getVariants().forEach((variant, block) -> {
            BiFunction<ItemLike, ItemLike, RecipeBuilder> biFunction = SHAPE_BUILDERS.get(variant);
            Block itemLike = RecipeProvider.getBaseBlock(blockFamily, variant);
            if (biFunction != null) {
                RecipeBuilder recipeBuilder = biFunction.apply((ItemLike)block, itemLike);
                blockFamily.getRecipeGroupPrefix().ifPresent(string -> recipeBuilder.group(string + (String)(variant == BlockFamily.Variant.CUT ? "" : "_" + variant.getName())));
                recipeBuilder.unlockedBy(blockFamily.getRecipeUnlockedBy().orElseGet(() -> RecipeProvider.getHasName(itemLike)), RecipeProvider.has(itemLike));
                recipeBuilder.save(consumer);
            }
            if (variant == BlockFamily.Variant.CRACKED) {
                RecipeProvider.smeltingResultFromBase(consumer, block, itemLike);
            }
        });
    }

    private static Block getBaseBlock(BlockFamily blockFamily, BlockFamily.Variant variant) {
        if (variant == BlockFamily.Variant.CHISELED) {
            if (!blockFamily.getVariants().containsKey((Object)BlockFamily.Variant.SLAB)) {
                throw new IllegalStateException("Slab is not defined for the family.");
            }
            return blockFamily.get(BlockFamily.Variant.SLAB);
        }
        return blockFamily.getBaseBlock();
    }

    private static EnterBlockTrigger.TriggerInstance insideOf(Block block) {
        return new EnterBlockTrigger.TriggerInstance(EntityPredicate.Composite.ANY, block, StatePropertiesPredicate.ANY);
    }

    private static InventoryChangeTrigger.TriggerInstance has(MinMaxBounds.Ints ints, ItemLike itemLike) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(itemLike).withCount(ints).build());
    }

    protected static InventoryChangeTrigger.TriggerInstance has(ItemLike itemLike) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(itemLike).build());
    }

    protected static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tagKey) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(tagKey).build());
    }

    private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate ... itemPredicates) {
        return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, itemPredicates);
    }

    protected static String getHasName(ItemLike itemLike) {
        return "has_" + RecipeProvider.getItemName(itemLike);
    }

    protected static String getItemName(ItemLike itemLike) {
        return Registry.ITEM.getKey(itemLike.asItem()).getPath();
    }

    protected static String getSimpleRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike);
    }

    protected static String getConversionRecipeName(ItemLike itemLike, ItemLike itemLike2) {
        return RecipeProvider.getItemName(itemLike) + "_from_" + RecipeProvider.getItemName(itemLike2);
    }

    protected static String getSmeltingRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike) + "_from_smelting";
    }

    protected static String getBlastingRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike) + "_from_blasting";
    }
}

