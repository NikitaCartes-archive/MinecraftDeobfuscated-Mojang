/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Registry;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.recipes.UpgradeRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class RecipeProvider
implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ImmutableList<ItemLike> COAL_SMELTABLES = ImmutableList.of(Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE);
    private static final ImmutableList<ItemLike> IRON_SMELTABLES = ImmutableList.of(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE, Items.RAW_IRON);
    private static final ImmutableList<ItemLike> COPPER_SMELTABLES = ImmutableList.of(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE, Items.RAW_COPPER);
    private static final ImmutableList<ItemLike> GOLD_SMELTABLES = ImmutableList.of(Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE, Items.NETHER_GOLD_ORE, Items.RAW_GOLD);
    private static final ImmutableList<ItemLike> DIAMOND_SMELTABLES = ImmutableList.of(Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE);
    private static final ImmutableList<ItemLike> LAPIS_SMELTABLES = ImmutableList.of(Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE);
    private static final ImmutableList<ItemLike> REDSTONE_SMELTABLES = ImmutableList.of(Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE);
    private static final ImmutableList<ItemLike> EMERALD_SMELTABLES = ImmutableList.of(Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE);
    private final DataGenerator generator;
    private static final Map<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>> shapeBuilders = ImmutableMap.builder().put(BlockFamily.Variant.BUTTON, (itemLike, itemLike2) -> RecipeProvider.buttonBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.CHISELED, (itemLike, itemLike2) -> RecipeProvider.chiseledBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.CUT, (itemLike, itemLike2) -> RecipeProvider.cutBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.DOOR, (itemLike, itemLike2) -> RecipeProvider.doorBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.FENCE, (itemLike, itemLike2) -> RecipeProvider.fenceBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.FENCE_GATE, (itemLike, itemLike2) -> RecipeProvider.fenceGateBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.SIGN, (itemLike, itemLike2) -> RecipeProvider.signBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.SLAB, (itemLike, itemLike2) -> RecipeProvider.slabBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.STAIRS, (itemLike, itemLike2) -> RecipeProvider.stairBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.PRESSURE_PLATE, (itemLike, itemLike2) -> RecipeProvider.pressurePlateBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.POLISHED, (itemLike, itemLike2) -> RecipeProvider.polishedBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.TRAPDOOR, (itemLike, itemLike2) -> RecipeProvider.trapdoorBuilder(itemLike, Ingredient.of(itemLike2))).put(BlockFamily.Variant.WALL, (itemLike, itemLike2) -> RecipeProvider.wallBuilder(itemLike, Ingredient.of(itemLike2))).build();

    public RecipeProvider(DataGenerator dataGenerator) {
        this.generator = dataGenerator;
    }

    @Override
    public void run(HashCache hashCache) {
        Path path = this.generator.getOutputFolder();
        HashSet set = Sets.newHashSet();
        RecipeProvider.buildCraftingRecipes(finishedRecipe -> {
            if (!set.add(finishedRecipe.getId())) {
                throw new IllegalStateException("Duplicate recipe " + finishedRecipe.getId());
            }
            RecipeProvider.saveRecipe(hashCache, finishedRecipe.serializeRecipe(), path.resolve("data/" + finishedRecipe.getId().getNamespace() + "/recipes/" + finishedRecipe.getId().getPath() + ".json"));
            JsonObject jsonObject = finishedRecipe.serializeAdvancement();
            if (jsonObject != null) {
                RecipeProvider.saveAdvancement(hashCache, jsonObject, path.resolve("data/" + finishedRecipe.getId().getNamespace() + "/advancements/" + finishedRecipe.getAdvancementId().getPath() + ".json"));
            }
        });
        RecipeProvider.saveAdvancement(hashCache, Advancement.Builder.advancement().addCriterion("impossible", new ImpossibleTrigger.TriggerInstance()).serializeToJson(), path.resolve("data/minecraft/advancements/recipes/root.json"));
    }

    private static void saveRecipe(HashCache hashCache, JsonObject jsonObject, Path path) {
        try {
            String string = GSON.toJson(jsonObject);
            String string2 = SHA1.hashUnencodedChars(string).toString();
            if (!Objects.equals(hashCache.getHash(path), string2) || !Files.exists(path, new LinkOption[0])) {
                Files.createDirectories(path.getParent(), new FileAttribute[0]);
                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, new OpenOption[0]);){
                    bufferedWriter.write(string);
                }
            }
            hashCache.putNew(path, string2);
        } catch (IOException iOException) {
            LOGGER.error("Couldn't save recipe {}", (Object)path, (Object)iOException);
        }
    }

    private static void saveAdvancement(HashCache hashCache, JsonObject jsonObject, Path path) {
        try {
            String string = GSON.toJson(jsonObject);
            String string2 = SHA1.hashUnencodedChars(string).toString();
            if (!Objects.equals(hashCache.getHash(path), string2) || !Files.exists(path, new LinkOption[0])) {
                Files.createDirectories(path.getParent(), new FileAttribute[0]);
                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, new OpenOption[0]);){
                    bufferedWriter.write(string);
                }
            }
            hashCache.putNew(path, string2);
        } catch (IOException iOException) {
            LOGGER.error("Couldn't save recipe advancement {}", (Object)path, (Object)iOException);
        }
    }

    private static void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        BlockFamilies.getAllFamilies().filter(BlockFamily::shouldGenerateRecipe).forEach(blockFamily -> RecipeProvider.generateRecipes(consumer, blockFamily));
        RecipeProvider.planksFromLog(consumer, Blocks.ACACIA_PLANKS, ItemTags.ACACIA_LOGS);
        RecipeProvider.planksFromLogs(consumer, Blocks.BIRCH_PLANKS, ItemTags.BIRCH_LOGS);
        RecipeProvider.planksFromLogs(consumer, Blocks.CRIMSON_PLANKS, ItemTags.CRIMSON_STEMS);
        RecipeProvider.planksFromLog(consumer, Blocks.DARK_OAK_PLANKS, ItemTags.DARK_OAK_LOGS);
        RecipeProvider.planksFromLogs(consumer, Blocks.JUNGLE_PLANKS, ItemTags.JUNGLE_LOGS);
        RecipeProvider.planksFromLogs(consumer, Blocks.OAK_PLANKS, ItemTags.OAK_LOGS);
        RecipeProvider.planksFromLogs(consumer, Blocks.SPRUCE_PLANKS, ItemTags.SPRUCE_LOGS);
        RecipeProvider.planksFromLogs(consumer, Blocks.WARPED_PLANKS, ItemTags.WARPED_STEMS);
        RecipeProvider.woodFromLogs(consumer, Blocks.ACACIA_WOOD, Blocks.ACACIA_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.BIRCH_WOOD, Blocks.BIRCH_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.DARK_OAK_WOOD, Blocks.DARK_OAK_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.JUNGLE_WOOD, Blocks.JUNGLE_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.OAK_WOOD, Blocks.OAK_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.SPRUCE_WOOD, Blocks.SPRUCE_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.CRIMSON_HYPHAE, Blocks.CRIMSON_STEM);
        RecipeProvider.woodFromLogs(consumer, Blocks.WARPED_HYPHAE, Blocks.WARPED_STEM);
        RecipeProvider.woodFromLogs(consumer, Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_ACACIA_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_OAK_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_LOG);
        RecipeProvider.woodFromLogs(consumer, Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_STEM);
        RecipeProvider.woodFromLogs(consumer, Blocks.STRIPPED_WARPED_HYPHAE, Blocks.STRIPPED_WARPED_STEM);
        RecipeProvider.woodenBoat(consumer, Items.ACACIA_BOAT, Blocks.ACACIA_PLANKS);
        RecipeProvider.woodenBoat(consumer, Items.BIRCH_BOAT, Blocks.BIRCH_PLANKS);
        RecipeProvider.woodenBoat(consumer, Items.DARK_OAK_BOAT, Blocks.DARK_OAK_PLANKS);
        RecipeProvider.woodenBoat(consumer, Items.JUNGLE_BOAT, Blocks.JUNGLE_PLANKS);
        RecipeProvider.woodenBoat(consumer, Items.OAK_BOAT, Blocks.OAK_PLANKS);
        RecipeProvider.woodenBoat(consumer, Items.SPRUCE_BOAT, Blocks.SPRUCE_PLANKS);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.BLACK_WOOL, Items.BLACK_DYE);
        RecipeProvider.carpet(consumer, Blocks.BLACK_CARPET, Blocks.BLACK_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.BLACK_CARPET, Items.BLACK_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.BLACK_BED, Blocks.BLACK_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.BLACK_BED, Items.BLACK_DYE);
        RecipeProvider.banner(consumer, Items.BLACK_BANNER, Blocks.BLACK_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.BLUE_WOOL, Items.BLUE_DYE);
        RecipeProvider.carpet(consumer, Blocks.BLUE_CARPET, Blocks.BLUE_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.BLUE_CARPET, Items.BLUE_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.BLUE_BED, Blocks.BLUE_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.BLUE_BED, Items.BLUE_DYE);
        RecipeProvider.banner(consumer, Items.BLUE_BANNER, Blocks.BLUE_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.BROWN_WOOL, Items.BROWN_DYE);
        RecipeProvider.carpet(consumer, Blocks.BROWN_CARPET, Blocks.BROWN_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.BROWN_CARPET, Items.BROWN_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.BROWN_BED, Blocks.BROWN_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.BROWN_BED, Items.BROWN_DYE);
        RecipeProvider.banner(consumer, Items.BROWN_BANNER, Blocks.BROWN_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.CYAN_WOOL, Items.CYAN_DYE);
        RecipeProvider.carpet(consumer, Blocks.CYAN_CARPET, Blocks.CYAN_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.CYAN_CARPET, Items.CYAN_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.CYAN_BED, Blocks.CYAN_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.CYAN_BED, Items.CYAN_DYE);
        RecipeProvider.banner(consumer, Items.CYAN_BANNER, Blocks.CYAN_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.GRAY_WOOL, Items.GRAY_DYE);
        RecipeProvider.carpet(consumer, Blocks.GRAY_CARPET, Blocks.GRAY_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.GRAY_CARPET, Items.GRAY_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.GRAY_BED, Blocks.GRAY_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.GRAY_BED, Items.GRAY_DYE);
        RecipeProvider.banner(consumer, Items.GRAY_BANNER, Blocks.GRAY_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.GREEN_WOOL, Items.GREEN_DYE);
        RecipeProvider.carpet(consumer, Blocks.GREEN_CARPET, Blocks.GREEN_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.GREEN_CARPET, Items.GREEN_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.GREEN_BED, Blocks.GREEN_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.GREEN_BED, Items.GREEN_DYE);
        RecipeProvider.banner(consumer, Items.GREEN_BANNER, Blocks.GREEN_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.LIGHT_BLUE_WOOL, Items.LIGHT_BLUE_DYE);
        RecipeProvider.carpet(consumer, Blocks.LIGHT_BLUE_CARPET, Blocks.LIGHT_BLUE_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.LIGHT_BLUE_CARPET, Items.LIGHT_BLUE_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.LIGHT_BLUE_BED, Items.LIGHT_BLUE_DYE);
        RecipeProvider.banner(consumer, Items.LIGHT_BLUE_BANNER, Blocks.LIGHT_BLUE_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.LIGHT_GRAY_WOOL, Items.LIGHT_GRAY_DYE);
        RecipeProvider.carpet(consumer, Blocks.LIGHT_GRAY_CARPET, Blocks.LIGHT_GRAY_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.LIGHT_GRAY_CARPET, Items.LIGHT_GRAY_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.LIGHT_GRAY_BED, Items.LIGHT_GRAY_DYE);
        RecipeProvider.banner(consumer, Items.LIGHT_GRAY_BANNER, Blocks.LIGHT_GRAY_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.LIME_WOOL, Items.LIME_DYE);
        RecipeProvider.carpet(consumer, Blocks.LIME_CARPET, Blocks.LIME_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.LIME_CARPET, Items.LIME_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.LIME_BED, Blocks.LIME_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.LIME_BED, Items.LIME_DYE);
        RecipeProvider.banner(consumer, Items.LIME_BANNER, Blocks.LIME_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.MAGENTA_WOOL, Items.MAGENTA_DYE);
        RecipeProvider.carpet(consumer, Blocks.MAGENTA_CARPET, Blocks.MAGENTA_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.MAGENTA_CARPET, Items.MAGENTA_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.MAGENTA_BED, Blocks.MAGENTA_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.MAGENTA_BED, Items.MAGENTA_DYE);
        RecipeProvider.banner(consumer, Items.MAGENTA_BANNER, Blocks.MAGENTA_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.ORANGE_WOOL, Items.ORANGE_DYE);
        RecipeProvider.carpet(consumer, Blocks.ORANGE_CARPET, Blocks.ORANGE_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.ORANGE_CARPET, Items.ORANGE_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.ORANGE_BED, Blocks.ORANGE_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.ORANGE_BED, Items.ORANGE_DYE);
        RecipeProvider.banner(consumer, Items.ORANGE_BANNER, Blocks.ORANGE_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.PINK_WOOL, Items.PINK_DYE);
        RecipeProvider.carpet(consumer, Blocks.PINK_CARPET, Blocks.PINK_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.PINK_CARPET, Items.PINK_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.PINK_BED, Blocks.PINK_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.PINK_BED, Items.PINK_DYE);
        RecipeProvider.banner(consumer, Items.PINK_BANNER, Blocks.PINK_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.PURPLE_WOOL, Items.PURPLE_DYE);
        RecipeProvider.carpet(consumer, Blocks.PURPLE_CARPET, Blocks.PURPLE_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.PURPLE_CARPET, Items.PURPLE_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.PURPLE_BED, Blocks.PURPLE_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.PURPLE_BED, Items.PURPLE_DYE);
        RecipeProvider.banner(consumer, Items.PURPLE_BANNER, Blocks.PURPLE_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.RED_WOOL, Items.RED_DYE);
        RecipeProvider.carpet(consumer, Blocks.RED_CARPET, Blocks.RED_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.RED_CARPET, Items.RED_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.RED_BED, Blocks.RED_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.RED_BED, Items.RED_DYE);
        RecipeProvider.banner(consumer, Items.RED_BANNER, Blocks.RED_WOOL);
        RecipeProvider.carpet(consumer, Blocks.WHITE_CARPET, Blocks.WHITE_WOOL);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.WHITE_BED, Blocks.WHITE_WOOL);
        RecipeProvider.banner(consumer, Items.WHITE_BANNER, Blocks.WHITE_WOOL);
        RecipeProvider.coloredWoolFromWhiteWoolAndDye(consumer, Blocks.YELLOW_WOOL, Items.YELLOW_DYE);
        RecipeProvider.carpet(consumer, Blocks.YELLOW_CARPET, Blocks.YELLOW_WOOL);
        RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(consumer, Blocks.YELLOW_CARPET, Items.YELLOW_DYE);
        RecipeProvider.bedFromPlanksAndWool(consumer, Items.YELLOW_BED, Blocks.YELLOW_WOOL);
        RecipeProvider.bedFromWhiteBedAndDye(consumer, Items.YELLOW_BED, Items.YELLOW_DYE);
        RecipeProvider.banner(consumer, Items.YELLOW_BANNER, Blocks.YELLOW_WOOL);
        RecipeProvider.carpet(consumer, Blocks.MOSS_CARPET, Blocks.MOSS_BLOCK);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.BLACK_STAINED_GLASS, Items.BLACK_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.BLACK_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.BLACK_STAINED_GLASS_PANE, Items.BLACK_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.BLUE_STAINED_GLASS, Items.BLUE_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.BLUE_STAINED_GLASS_PANE, Items.BLUE_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.BROWN_STAINED_GLASS, Items.BROWN_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.BROWN_STAINED_GLASS_PANE, Items.BROWN_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.CYAN_STAINED_GLASS, Items.CYAN_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.CYAN_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.CYAN_STAINED_GLASS_PANE, Items.CYAN_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.GRAY_STAINED_GLASS, Items.GRAY_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.GRAY_STAINED_GLASS_PANE, Items.GRAY_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.GREEN_STAINED_GLASS, Items.GREEN_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.GREEN_STAINED_GLASS_PANE, Items.GREEN_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.LIGHT_BLUE_STAINED_GLASS, Items.LIGHT_BLUE_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Items.LIGHT_BLUE_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.LIGHT_GRAY_STAINED_GLASS, Items.LIGHT_GRAY_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Items.LIGHT_GRAY_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.LIME_STAINED_GLASS, Items.LIME_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.LIME_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.LIME_STAINED_GLASS_PANE, Items.LIME_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.MAGENTA_STAINED_GLASS, Items.MAGENTA_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.MAGENTA_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.MAGENTA_STAINED_GLASS_PANE, Items.MAGENTA_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.ORANGE_STAINED_GLASS, Items.ORANGE_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.ORANGE_STAINED_GLASS_PANE, Items.ORANGE_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.PINK_STAINED_GLASS, Items.PINK_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.PINK_STAINED_GLASS_PANE, Blocks.PINK_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.PINK_STAINED_GLASS_PANE, Items.PINK_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.PURPLE_STAINED_GLASS, Items.PURPLE_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.PURPLE_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.PURPLE_STAINED_GLASS_PANE, Items.PURPLE_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.RED_STAINED_GLASS, Items.RED_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.RED_STAINED_GLASS_PANE, Blocks.RED_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.RED_STAINED_GLASS_PANE, Items.RED_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.WHITE_STAINED_GLASS, Items.WHITE_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.WHITE_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.WHITE_STAINED_GLASS_PANE, Items.WHITE_DYE);
        RecipeProvider.stainedGlassFromGlassAndDye(consumer, Blocks.YELLOW_STAINED_GLASS, Items.YELLOW_DYE);
        RecipeProvider.stainedGlassPaneFromStainedGlass(consumer, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS);
        RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(consumer, Blocks.YELLOW_STAINED_GLASS_PANE, Items.YELLOW_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.BLACK_TERRACOTTA, Items.BLACK_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.BLUE_TERRACOTTA, Items.BLUE_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.BROWN_TERRACOTTA, Items.BROWN_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.CYAN_TERRACOTTA, Items.CYAN_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.GRAY_TERRACOTTA, Items.GRAY_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.GREEN_TERRACOTTA, Items.GREEN_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.LIGHT_GRAY_TERRACOTTA, Items.LIGHT_GRAY_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.LIME_TERRACOTTA, Items.LIME_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.MAGENTA_TERRACOTTA, Items.MAGENTA_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.ORANGE_TERRACOTTA, Items.ORANGE_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.PINK_TERRACOTTA, Items.PINK_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.PURPLE_TERRACOTTA, Items.PURPLE_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.RED_TERRACOTTA, Items.RED_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.WHITE_TERRACOTTA, Items.WHITE_DYE);
        RecipeProvider.coloredTerracottaFromTerracottaAndDye(consumer, Blocks.YELLOW_TERRACOTTA, Items.YELLOW_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.BLACK_CONCRETE_POWDER, Items.BLACK_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.BLUE_CONCRETE_POWDER, Items.BLUE_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.BROWN_CONCRETE_POWDER, Items.BROWN_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.CYAN_CONCRETE_POWDER, Items.CYAN_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.GRAY_CONCRETE_POWDER, Items.GRAY_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.GREEN_CONCRETE_POWDER, Items.GREEN_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Items.LIGHT_BLUE_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.LIME_CONCRETE_POWDER, Items.LIME_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.MAGENTA_CONCRETE_POWDER, Items.MAGENTA_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.ORANGE_CONCRETE_POWDER, Items.ORANGE_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.PINK_CONCRETE_POWDER, Items.PINK_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.PURPLE_CONCRETE_POWDER, Items.PURPLE_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.RED_CONCRETE_POWDER, Items.RED_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.WHITE_CONCRETE_POWDER, Items.WHITE_DYE);
        RecipeProvider.concretePowder(consumer, Blocks.YELLOW_CONCRETE_POWDER, Items.YELLOW_DYE);
        RecipeProvider.candle(consumer, Blocks.BLACK_CANDLE, Items.BLACK_DYE);
        RecipeProvider.candle(consumer, Blocks.BLUE_CANDLE, Items.BLUE_DYE);
        RecipeProvider.candle(consumer, Blocks.BROWN_CANDLE, Items.BROWN_DYE);
        RecipeProvider.candle(consumer, Blocks.CYAN_CANDLE, Items.CYAN_DYE);
        RecipeProvider.candle(consumer, Blocks.GRAY_CANDLE, Items.GRAY_DYE);
        RecipeProvider.candle(consumer, Blocks.GREEN_CANDLE, Items.GREEN_DYE);
        RecipeProvider.candle(consumer, Blocks.LIGHT_BLUE_CANDLE, Items.LIGHT_BLUE_DYE);
        RecipeProvider.candle(consumer, Blocks.LIGHT_GRAY_CANDLE, Items.LIGHT_GRAY_DYE);
        RecipeProvider.candle(consumer, Blocks.LIME_CANDLE, Items.LIME_DYE);
        RecipeProvider.candle(consumer, Blocks.MAGENTA_CANDLE, Items.MAGENTA_DYE);
        RecipeProvider.candle(consumer, Blocks.ORANGE_CANDLE, Items.ORANGE_DYE);
        RecipeProvider.candle(consumer, Blocks.PINK_CANDLE, Items.PINK_DYE);
        RecipeProvider.candle(consumer, Blocks.PURPLE_CANDLE, Items.PURPLE_DYE);
        RecipeProvider.candle(consumer, Blocks.RED_CANDLE, Items.RED_DYE);
        RecipeProvider.candle(consumer, Blocks.WHITE_CANDLE, Items.WHITE_DYE);
        RecipeProvider.candle(consumer, Blocks.YELLOW_CANDLE, Items.YELLOW_DYE);
        ShapedRecipeBuilder.shaped(Blocks.ACTIVATOR_RAIL, 6).define(Character.valueOf('#'), Blocks.REDSTONE_TORCH).define(Character.valueOf('S'), Items.STICK).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("XSX").pattern("X#X").pattern("XSX").unlockedBy("has_rail", RecipeProvider.has(Blocks.RAIL)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Blocks.ANDESITE, 2).requires(Blocks.DIORITE).requires(Blocks.COBBLESTONE).unlockedBy("has_stone", RecipeProvider.has(Blocks.DIORITE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.ANVIL).define(Character.valueOf('I'), Blocks.IRON_BLOCK).define(Character.valueOf('i'), Items.IRON_INGOT).pattern("III").pattern(" i ").pattern("iii").unlockedBy("has_iron_block", RecipeProvider.has(Blocks.IRON_BLOCK)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.ARMOR_STAND).define(Character.valueOf('/'), Items.STICK).define(Character.valueOf('_'), Blocks.SMOOTH_STONE_SLAB).pattern("///").pattern(" / ").pattern("/_/").unlockedBy("has_stone_slab", RecipeProvider.has(Blocks.SMOOTH_STONE_SLAB)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.ARROW, 4).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.FLINT).define(Character.valueOf('Y'), Items.FEATHER).pattern("X").pattern("#").pattern("Y").unlockedBy("has_feather", RecipeProvider.has(Items.FEATHER)).unlockedBy("has_flint", RecipeProvider.has(Items.FLINT)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.BARREL, 1).define(Character.valueOf('P'), ItemTags.PLANKS).define(Character.valueOf('S'), ItemTags.WOODEN_SLABS).pattern("PSP").pattern("P P").pattern("PSP").unlockedBy("has_planks", RecipeProvider.has(ItemTags.PLANKS)).unlockedBy("has_wood_slab", RecipeProvider.has(ItemTags.WOODEN_SLABS)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.BEACON).define(Character.valueOf('S'), Items.NETHER_STAR).define(Character.valueOf('G'), Blocks.GLASS).define(Character.valueOf('O'), Blocks.OBSIDIAN).pattern("GGG").pattern("GSG").pattern("OOO").unlockedBy("has_nether_star", RecipeProvider.has(Items.NETHER_STAR)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.BEEHIVE).define(Character.valueOf('P'), ItemTags.PLANKS).define(Character.valueOf('H'), Items.HONEYCOMB).pattern("PPP").pattern("HHH").pattern("PPP").unlockedBy("has_honeycomb", RecipeProvider.has(Items.HONEYCOMB)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.BEETROOT_SOUP).requires(Items.BOWL).requires(Items.BEETROOT, 6).unlockedBy("has_beetroot", RecipeProvider.has(Items.BEETROOT)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.BLACK_DYE).requires(Items.INK_SAC).group("black_dye").unlockedBy("has_ink_sac", RecipeProvider.has(Items.INK_SAC)).save(consumer);
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.BLACK_DYE, Blocks.WITHER_ROSE, "black_dye");
        ShapelessRecipeBuilder.shapeless(Items.BLAZE_POWDER, 2).requires(Items.BLAZE_ROD).unlockedBy("has_blaze_rod", RecipeProvider.has(Items.BLAZE_ROD)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.BLUE_DYE).requires(Items.LAPIS_LAZULI).group("blue_dye").unlockedBy("has_lapis_lazuli", RecipeProvider.has(Items.LAPIS_LAZULI)).save(consumer);
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.BLUE_DYE, Blocks.CORNFLOWER, "blue_dye");
        ShapedRecipeBuilder.shaped(Blocks.BLUE_ICE).define(Character.valueOf('#'), Blocks.PACKED_ICE).pattern("###").pattern("###").pattern("###").unlockedBy("has_packed_ice", RecipeProvider.has(Blocks.PACKED_ICE)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.BONE_MEAL, 3).requires(Items.BONE).group("bonemeal").unlockedBy("has_bone", RecipeProvider.has(Items.BONE)).save(consumer);
        RecipeProvider.nineBlockStorageRecipesRecipesWithCustomUnpacking(consumer, Items.BONE_MEAL, Items.BONE_BLOCK, "bone_meal_from_bone_block", "bonemeal");
        ShapelessRecipeBuilder.shapeless(Items.BOOK).requires(Items.PAPER, 3).requires(Items.LEATHER).unlockedBy("has_paper", RecipeProvider.has(Items.PAPER)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.BOOKSHELF).define(Character.valueOf('#'), ItemTags.PLANKS).define(Character.valueOf('X'), Items.BOOK).pattern("###").pattern("XXX").pattern("###").unlockedBy("has_book", RecipeProvider.has(Items.BOOK)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.BOW).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.STRING).pattern(" #X").pattern("# X").pattern(" #X").unlockedBy("has_string", RecipeProvider.has(Items.STRING)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.BOWL, 4).define(Character.valueOf('#'), ItemTags.PLANKS).pattern("# #").pattern(" # ").unlockedBy("has_brown_mushroom", RecipeProvider.has(Blocks.BROWN_MUSHROOM)).unlockedBy("has_red_mushroom", RecipeProvider.has(Blocks.RED_MUSHROOM)).unlockedBy("has_mushroom_stew", RecipeProvider.has(Items.MUSHROOM_STEW)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.BREAD).define(Character.valueOf('#'), Items.WHEAT).pattern("###").unlockedBy("has_wheat", RecipeProvider.has(Items.WHEAT)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.BREWING_STAND).define(Character.valueOf('B'), Items.BLAZE_ROD).define(Character.valueOf('#'), ItemTags.STONE_CRAFTING_MATERIALS).pattern(" B ").pattern("###").unlockedBy("has_blaze_rod", RecipeProvider.has(Items.BLAZE_ROD)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.BRICKS).define(Character.valueOf('#'), Items.BRICK).pattern("##").pattern("##").unlockedBy("has_brick", RecipeProvider.has(Items.BRICK)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.BROWN_DYE).requires(Items.COCOA_BEANS).group("brown_dye").unlockedBy("has_cocoa_beans", RecipeProvider.has(Items.COCOA_BEANS)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.BUCKET).define(Character.valueOf('#'), Items.IRON_INGOT).pattern("# #").pattern(" # ").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.CAKE).define(Character.valueOf('A'), Items.MILK_BUCKET).define(Character.valueOf('B'), Items.SUGAR).define(Character.valueOf('C'), Items.WHEAT).define(Character.valueOf('E'), Items.EGG).pattern("AAA").pattern("BEB").pattern("CCC").unlockedBy("has_egg", RecipeProvider.has(Items.EGG)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.CAMPFIRE).define(Character.valueOf('L'), ItemTags.LOGS).define(Character.valueOf('S'), Items.STICK).define(Character.valueOf('C'), ItemTags.COALS).pattern(" S ").pattern("SCS").pattern("LLL").unlockedBy("has_stick", RecipeProvider.has(Items.STICK)).unlockedBy("has_coal", RecipeProvider.has(ItemTags.COALS)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.CARROT_ON_A_STICK).define(Character.valueOf('#'), Items.FISHING_ROD).define(Character.valueOf('X'), Items.CARROT).pattern("# ").pattern(" X").unlockedBy("has_carrot", RecipeProvider.has(Items.CARROT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.WARPED_FUNGUS_ON_A_STICK).define(Character.valueOf('#'), Items.FISHING_ROD).define(Character.valueOf('X'), Items.WARPED_FUNGUS).pattern("# ").pattern(" X").unlockedBy("has_warped_fungus", RecipeProvider.has(Items.WARPED_FUNGUS)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.CAULDRON).define(Character.valueOf('#'), Items.IRON_INGOT).pattern("# #").pattern("# #").pattern("###").unlockedBy("has_water_bucket", RecipeProvider.has(Items.WATER_BUCKET)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.COMPOSTER).define(Character.valueOf('#'), ItemTags.WOODEN_SLABS).pattern("# #").pattern("# #").pattern("###").unlockedBy("has_wood_slab", RecipeProvider.has(ItemTags.WOODEN_SLABS)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.CHEST).define(Character.valueOf('#'), ItemTags.PLANKS).pattern("###").pattern("# #").pattern("###").unlockedBy("has_lots_of_items", new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.atLeast(10), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, new ItemPredicate[0])).save(consumer);
        ShapedRecipeBuilder.shaped(Items.CHEST_MINECART).define(Character.valueOf('A'), Blocks.CHEST).define(Character.valueOf('B'), Items.MINECART).pattern("A").pattern("B").unlockedBy("has_minecart", RecipeProvider.has(Items.MINECART)).save(consumer);
        RecipeProvider.chiseledBuilder(Blocks.CHISELED_QUARTZ_BLOCK, Ingredient.of(Blocks.QUARTZ_SLAB)).unlockedBy("has_chiseled_quartz_block", RecipeProvider.has(Blocks.CHISELED_QUARTZ_BLOCK)).unlockedBy("has_quartz_block", RecipeProvider.has(Blocks.QUARTZ_BLOCK)).unlockedBy("has_quartz_pillar", RecipeProvider.has(Blocks.QUARTZ_PILLAR)).save(consumer);
        RecipeProvider.chiseledBuilder(Blocks.CHISELED_STONE_BRICKS, Ingredient.of(Blocks.STONE_BRICK_SLAB)).unlockedBy("has_tag", RecipeProvider.has(ItemTags.STONE_BRICKS)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.CLAY).define(Character.valueOf('#'), Items.CLAY_BALL).pattern("##").pattern("##").unlockedBy("has_clay_ball", RecipeProvider.has(Items.CLAY_BALL)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.CLOCK).define(Character.valueOf('#'), Items.GOLD_INGOT).define(Character.valueOf('X'), Items.REDSTONE).pattern(" # ").pattern("#X#").pattern(" # ").unlockedBy("has_redstone", RecipeProvider.has(Items.REDSTONE)).save(consumer);
        RecipeProvider.nineBlockStorageRecipes(consumer, Items.COAL, Items.COAL_BLOCK);
        ShapedRecipeBuilder.shaped(Blocks.COARSE_DIRT, 4).define(Character.valueOf('D'), Blocks.DIRT).define(Character.valueOf('G'), Blocks.GRAVEL).pattern("DG").pattern("GD").unlockedBy("has_gravel", RecipeProvider.has(Blocks.GRAVEL)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.COMPARATOR).define(Character.valueOf('#'), Blocks.REDSTONE_TORCH).define(Character.valueOf('X'), Items.QUARTZ).define(Character.valueOf('I'), Blocks.STONE).pattern(" # ").pattern("#X#").pattern("III").unlockedBy("has_quartz", RecipeProvider.has(Items.QUARTZ)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.COMPASS).define(Character.valueOf('#'), Items.IRON_INGOT).define(Character.valueOf('X'), Items.REDSTONE).pattern(" # ").pattern("#X#").pattern(" # ").unlockedBy("has_redstone", RecipeProvider.has(Items.REDSTONE)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.COOKIE, 8).define(Character.valueOf('#'), Items.WHEAT).define(Character.valueOf('X'), Items.COCOA_BEANS).pattern("#X#").unlockedBy("has_cocoa", RecipeProvider.has(Items.COCOA_BEANS)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.CRAFTING_TABLE).define(Character.valueOf('#'), ItemTags.PLANKS).pattern("##").pattern("##").unlockedBy("has_planks", RecipeProvider.has(ItemTags.PLANKS)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.CROSSBOW).define(Character.valueOf('~'), Items.STRING).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('&'), Items.IRON_INGOT).define(Character.valueOf('$'), Blocks.TRIPWIRE_HOOK).pattern("#&#").pattern("~$~").pattern(" # ").unlockedBy("has_string", RecipeProvider.has(Items.STRING)).unlockedBy("has_stick", RecipeProvider.has(Items.STICK)).unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).unlockedBy("has_tripwire_hook", RecipeProvider.has(Blocks.TRIPWIRE_HOOK)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.LOOM).define(Character.valueOf('#'), ItemTags.PLANKS).define(Character.valueOf('@'), Items.STRING).pattern("@@").pattern("##").unlockedBy("has_string", RecipeProvider.has(Items.STRING)).save(consumer);
        RecipeProvider.chiseledBuilder(Blocks.CHISELED_RED_SANDSTONE, Ingredient.of(Blocks.RED_SANDSTONE_SLAB)).unlockedBy("has_red_sandstone", RecipeProvider.has(Blocks.RED_SANDSTONE)).unlockedBy("has_chiseled_red_sandstone", RecipeProvider.has(Blocks.CHISELED_RED_SANDSTONE)).unlockedBy("has_cut_red_sandstone", RecipeProvider.has(Blocks.CUT_RED_SANDSTONE)).save(consumer);
        RecipeProvider.chiseled(consumer, Blocks.CHISELED_SANDSTONE, Blocks.SANDSTONE_SLAB);
        RecipeProvider.nineBlockStorageRecipes(consumer, Items.COPPER_INGOT, Items.COPPER_BLOCK);
        ShapelessRecipeBuilder.shapeless(Items.COPPER_INGOT, 9).requires(Blocks.WAXED_COPPER_BLOCK).group(RecipeProvider.getItemName(Items.COPPER_INGOT)).unlockedBy(RecipeProvider.getHasName(Blocks.WAXED_COPPER_BLOCK), RecipeProvider.has(Blocks.WAXED_COPPER_BLOCK)).save(consumer, RecipeProvider.getConversionRecipeName(Items.COPPER_INGOT, Blocks.WAXED_COPPER_BLOCK));
        RecipeProvider.waxRecipes(consumer);
        ShapelessRecipeBuilder.shapeless(Items.CYAN_DYE, 2).requires(Items.BLUE_DYE).requires(Items.GREEN_DYE).unlockedBy("has_green_dye", RecipeProvider.has(Items.GREEN_DYE)).unlockedBy("has_blue_dye", RecipeProvider.has(Items.BLUE_DYE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.DARK_PRISMARINE).define(Character.valueOf('S'), Items.PRISMARINE_SHARD).define(Character.valueOf('I'), Items.BLACK_DYE).pattern("SSS").pattern("SIS").pattern("SSS").unlockedBy("has_prismarine_shard", RecipeProvider.has(Items.PRISMARINE_SHARD)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.DAYLIGHT_DETECTOR).define(Character.valueOf('Q'), Items.QUARTZ).define(Character.valueOf('G'), Blocks.GLASS).define(Character.valueOf('W'), Ingredient.of(ItemTags.WOODEN_SLABS)).pattern("GGG").pattern("QQQ").pattern("WWW").unlockedBy("has_quartz", RecipeProvider.has(Items.QUARTZ)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.DEEPSLATE_BRICKS, 4).define(Character.valueOf('S'), Blocks.POLISHED_DEEPSLATE).pattern("SS").pattern("SS").unlockedBy("has_polished_deepslate", RecipeProvider.has(Blocks.POLISHED_DEEPSLATE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.DEEPSLATE_TILES, 4).define(Character.valueOf('S'), Blocks.DEEPSLATE_BRICKS).pattern("SS").pattern("SS").unlockedBy("has_deepslate_bricks", RecipeProvider.has(Blocks.DEEPSLATE_BRICKS)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.DETECTOR_RAIL, 6).define(Character.valueOf('R'), Items.REDSTONE).define(Character.valueOf('#'), Blocks.STONE_PRESSURE_PLATE).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("X X").pattern("X#X").pattern("XRX").unlockedBy("has_rail", RecipeProvider.has(Blocks.RAIL)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_AXE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.DIAMOND).pattern("XX").pattern("X#").pattern(" #").unlockedBy("has_diamond", RecipeProvider.has(Items.DIAMOND)).save(consumer);
        RecipeProvider.nineBlockStorageRecipes(consumer, Items.DIAMOND, Items.DIAMOND_BLOCK);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_BOOTS).define(Character.valueOf('X'), Items.DIAMOND).pattern("X X").pattern("X X").unlockedBy("has_diamond", RecipeProvider.has(Items.DIAMOND)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_CHESTPLATE).define(Character.valueOf('X'), Items.DIAMOND).pattern("X X").pattern("XXX").pattern("XXX").unlockedBy("has_diamond", RecipeProvider.has(Items.DIAMOND)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_HELMET).define(Character.valueOf('X'), Items.DIAMOND).pattern("XXX").pattern("X X").unlockedBy("has_diamond", RecipeProvider.has(Items.DIAMOND)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_HOE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.DIAMOND).pattern("XX").pattern(" #").pattern(" #").unlockedBy("has_diamond", RecipeProvider.has(Items.DIAMOND)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_LEGGINGS).define(Character.valueOf('X'), Items.DIAMOND).pattern("XXX").pattern("X X").pattern("X X").unlockedBy("has_diamond", RecipeProvider.has(Items.DIAMOND)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_PICKAXE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.DIAMOND).pattern("XXX").pattern(" # ").pattern(" # ").unlockedBy("has_diamond", RecipeProvider.has(Items.DIAMOND)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_SHOVEL).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.DIAMOND).pattern("X").pattern("#").pattern("#").unlockedBy("has_diamond", RecipeProvider.has(Items.DIAMOND)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_SWORD).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.DIAMOND).pattern("X").pattern("X").pattern("#").unlockedBy("has_diamond", RecipeProvider.has(Items.DIAMOND)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.DIORITE, 2).define(Character.valueOf('Q'), Items.QUARTZ).define(Character.valueOf('C'), Blocks.COBBLESTONE).pattern("CQ").pattern("QC").unlockedBy("has_quartz", RecipeProvider.has(Items.QUARTZ)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.DISPENSER).define(Character.valueOf('R'), Items.REDSTONE).define(Character.valueOf('#'), Blocks.COBBLESTONE).define(Character.valueOf('X'), Items.BOW).pattern("###").pattern("#X#").pattern("#R#").unlockedBy("has_bow", RecipeProvider.has(Items.BOW)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.DRIPSTONE_BLOCK).define(Character.valueOf('#'), Items.POINTED_DRIPSTONE).pattern("##").pattern("##").group("pointed_dripstone").unlockedBy("has_pointed_dripstone", RecipeProvider.has(Items.POINTED_DRIPSTONE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.DROPPER).define(Character.valueOf('R'), Items.REDSTONE).define(Character.valueOf('#'), Blocks.COBBLESTONE).pattern("###").pattern("# #").pattern("#R#").unlockedBy("has_redstone", RecipeProvider.has(Items.REDSTONE)).save(consumer);
        RecipeProvider.nineBlockStorageRecipes(consumer, Items.EMERALD, Items.EMERALD_BLOCK);
        ShapedRecipeBuilder.shaped(Blocks.ENCHANTING_TABLE).define(Character.valueOf('B'), Items.BOOK).define(Character.valueOf('#'), Blocks.OBSIDIAN).define(Character.valueOf('D'), Items.DIAMOND).pattern(" B ").pattern("D#D").pattern("###").unlockedBy("has_obsidian", RecipeProvider.has(Blocks.OBSIDIAN)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.ENDER_CHEST).define(Character.valueOf('#'), Blocks.OBSIDIAN).define(Character.valueOf('E'), Items.ENDER_EYE).pattern("###").pattern("#E#").pattern("###").unlockedBy("has_ender_eye", RecipeProvider.has(Items.ENDER_EYE)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.ENDER_EYE).requires(Items.ENDER_PEARL).requires(Items.BLAZE_POWDER).unlockedBy("has_blaze_powder", RecipeProvider.has(Items.BLAZE_POWDER)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.END_STONE_BRICKS, 4).define(Character.valueOf('#'), Blocks.END_STONE).pattern("##").pattern("##").unlockedBy("has_end_stone", RecipeProvider.has(Blocks.END_STONE)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.END_CRYSTAL).define(Character.valueOf('T'), Items.GHAST_TEAR).define(Character.valueOf('E'), Items.ENDER_EYE).define(Character.valueOf('G'), Blocks.GLASS).pattern("GGG").pattern("GEG").pattern("GTG").unlockedBy("has_ender_eye", RecipeProvider.has(Items.ENDER_EYE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.END_ROD, 4).define(Character.valueOf('#'), Items.POPPED_CHORUS_FRUIT).define(Character.valueOf('/'), Items.BLAZE_ROD).pattern("/").pattern("#").unlockedBy("has_chorus_fruit_popped", RecipeProvider.has(Items.POPPED_CHORUS_FRUIT)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.FERMENTED_SPIDER_EYE).requires(Items.SPIDER_EYE).requires(Blocks.BROWN_MUSHROOM).requires(Items.SUGAR).unlockedBy("has_spider_eye", RecipeProvider.has(Items.SPIDER_EYE)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.FIRE_CHARGE, 3).requires(Items.GUNPOWDER).requires(Items.BLAZE_POWDER).requires(Ingredient.of(Items.COAL, Items.CHARCOAL)).unlockedBy("has_blaze_powder", RecipeProvider.has(Items.BLAZE_POWDER)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.FIREWORK_ROCKET, 3).requires(Items.GUNPOWDER).requires(Items.PAPER).unlockedBy("has_gunpowder", RecipeProvider.has(Items.GUNPOWDER)).save(consumer, "firework_rocket_simple");
        ShapedRecipeBuilder.shaped(Items.FISHING_ROD).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.STRING).pattern("  #").pattern(" #X").pattern("# X").unlockedBy("has_string", RecipeProvider.has(Items.STRING)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.FLINT_AND_STEEL).requires(Items.IRON_INGOT).requires(Items.FLINT).unlockedBy("has_flint", RecipeProvider.has(Items.FLINT)).unlockedBy("has_obsidian", RecipeProvider.has(Blocks.OBSIDIAN)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.FLOWER_POT).define(Character.valueOf('#'), Items.BRICK).pattern("# #").pattern(" # ").unlockedBy("has_brick", RecipeProvider.has(Items.BRICK)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.FURNACE).define(Character.valueOf('#'), ItemTags.STONE_CRAFTING_MATERIALS).pattern("###").pattern("# #").pattern("###").unlockedBy("has_cobblestone", RecipeProvider.has(ItemTags.STONE_CRAFTING_MATERIALS)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.FURNACE_MINECART).define(Character.valueOf('A'), Blocks.FURNACE).define(Character.valueOf('B'), Items.MINECART).pattern("A").pattern("B").unlockedBy("has_minecart", RecipeProvider.has(Items.MINECART)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GLASS_BOTTLE, 3).define(Character.valueOf('#'), Blocks.GLASS).pattern("# #").pattern(" # ").unlockedBy("has_glass", RecipeProvider.has(Blocks.GLASS)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.GLASS_PANE, 16).define(Character.valueOf('#'), Blocks.GLASS).pattern("###").pattern("###").unlockedBy("has_glass", RecipeProvider.has(Blocks.GLASS)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.GLOWSTONE).define(Character.valueOf('#'), Items.GLOWSTONE_DUST).pattern("##").pattern("##").unlockedBy("has_glowstone_dust", RecipeProvider.has(Items.GLOWSTONE_DUST)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.GLOW_ITEM_FRAME).requires(Items.ITEM_FRAME).requires(Items.GLOW_INK_SAC).unlockedBy("has_item_frame", RecipeProvider.has(Items.ITEM_FRAME)).unlockedBy("has_glow_ink_sac", RecipeProvider.has(Items.GLOW_INK_SAC)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_APPLE).define(Character.valueOf('#'), Items.GOLD_INGOT).define(Character.valueOf('X'), Items.APPLE).pattern("###").pattern("#X#").pattern("###").unlockedBy("has_gold_ingot", RecipeProvider.has(Items.GOLD_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_AXE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.GOLD_INGOT).pattern("XX").pattern("X#").pattern(" #").unlockedBy("has_gold_ingot", RecipeProvider.has(Items.GOLD_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_BOOTS).define(Character.valueOf('X'), Items.GOLD_INGOT).pattern("X X").pattern("X X").unlockedBy("has_gold_ingot", RecipeProvider.has(Items.GOLD_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_CARROT).define(Character.valueOf('#'), Items.GOLD_NUGGET).define(Character.valueOf('X'), Items.CARROT).pattern("###").pattern("#X#").pattern("###").unlockedBy("has_gold_nugget", RecipeProvider.has(Items.GOLD_NUGGET)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_CHESTPLATE).define(Character.valueOf('X'), Items.GOLD_INGOT).pattern("X X").pattern("XXX").pattern("XXX").unlockedBy("has_gold_ingot", RecipeProvider.has(Items.GOLD_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_HELMET).define(Character.valueOf('X'), Items.GOLD_INGOT).pattern("XXX").pattern("X X").unlockedBy("has_gold_ingot", RecipeProvider.has(Items.GOLD_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_HOE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.GOLD_INGOT).pattern("XX").pattern(" #").pattern(" #").unlockedBy("has_gold_ingot", RecipeProvider.has(Items.GOLD_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_LEGGINGS).define(Character.valueOf('X'), Items.GOLD_INGOT).pattern("XXX").pattern("X X").pattern("X X").unlockedBy("has_gold_ingot", RecipeProvider.has(Items.GOLD_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_PICKAXE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.GOLD_INGOT).pattern("XXX").pattern(" # ").pattern(" # ").unlockedBy("has_gold_ingot", RecipeProvider.has(Items.GOLD_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.POWERED_RAIL, 6).define(Character.valueOf('R'), Items.REDSTONE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.GOLD_INGOT).pattern("X X").pattern("X#X").pattern("XRX").unlockedBy("has_rail", RecipeProvider.has(Blocks.RAIL)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_SHOVEL).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.GOLD_INGOT).pattern("X").pattern("#").pattern("#").unlockedBy("has_gold_ingot", RecipeProvider.has(Items.GOLD_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_SWORD).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.GOLD_INGOT).pattern("X").pattern("X").pattern("#").unlockedBy("has_gold_ingot", RecipeProvider.has(Items.GOLD_INGOT)).save(consumer);
        RecipeProvider.nineBlockStorageRecipesRecipesWithCustomUnpacking(consumer, Items.GOLD_INGOT, Items.GOLD_BLOCK, "gold_ingot_from_gold_block", "gold_ingot");
        RecipeProvider.nineBlockStorageRecipesWithCustomPacking(consumer, Items.GOLD_NUGGET, Items.GOLD_INGOT, "gold_ingot_from_nuggets", "gold_ingot");
        ShapelessRecipeBuilder.shapeless(Blocks.GRANITE).requires(Blocks.DIORITE).requires(Items.QUARTZ).unlockedBy("has_quartz", RecipeProvider.has(Items.QUARTZ)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.GRAY_DYE, 2).requires(Items.BLACK_DYE).requires(Items.WHITE_DYE).unlockedBy("has_white_dye", RecipeProvider.has(Items.WHITE_DYE)).unlockedBy("has_black_dye", RecipeProvider.has(Items.BLACK_DYE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.HAY_BLOCK).define(Character.valueOf('#'), Items.WHEAT).pattern("###").pattern("###").pattern("###").unlockedBy("has_wheat", RecipeProvider.has(Items.WHEAT)).save(consumer);
        RecipeProvider.pressurePlate(consumer, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.IRON_INGOT);
        ShapelessRecipeBuilder.shapeless(Items.HONEY_BOTTLE, 4).requires(Items.HONEY_BLOCK).requires(Items.GLASS_BOTTLE, 4).unlockedBy("has_honey_block", RecipeProvider.has(Blocks.HONEY_BLOCK)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.HONEY_BLOCK, 1).define(Character.valueOf('S'), Items.HONEY_BOTTLE).pattern("SS").pattern("SS").unlockedBy("has_honey_bottle", RecipeProvider.has(Items.HONEY_BOTTLE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.HONEYCOMB_BLOCK).define(Character.valueOf('H'), Items.HONEYCOMB).pattern("HH").pattern("HH").unlockedBy("has_honeycomb", RecipeProvider.has(Items.HONEYCOMB)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.HOPPER).define(Character.valueOf('C'), Blocks.CHEST).define(Character.valueOf('I'), Items.IRON_INGOT).pattern("I I").pattern("ICI").pattern(" I ").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.HOPPER_MINECART).define(Character.valueOf('A'), Blocks.HOPPER).define(Character.valueOf('B'), Items.MINECART).pattern("A").pattern("B").unlockedBy("has_minecart", RecipeProvider.has(Items.MINECART)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.IRON_AXE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("XX").pattern("X#").pattern(" #").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.IRON_BARS, 16).define(Character.valueOf('#'), Items.IRON_INGOT).pattern("###").pattern("###").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.IRON_BOOTS).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("X X").pattern("X X").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.IRON_CHESTPLATE).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("X X").pattern("XXX").pattern("XXX").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        RecipeProvider.doorBuilder(Blocks.IRON_DOOR, Ingredient.of(Items.IRON_INGOT)).unlockedBy(RecipeProvider.getHasName(Items.IRON_INGOT), RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.IRON_HELMET).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("XXX").pattern("X X").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.IRON_HOE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("XX").pattern(" #").pattern(" #").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        RecipeProvider.nineBlockStorageRecipesRecipesWithCustomUnpacking(consumer, Items.IRON_INGOT, Items.IRON_BLOCK, "iron_ingot_from_iron_block", "iron_ingot");
        RecipeProvider.nineBlockStorageRecipesWithCustomPacking(consumer, Items.IRON_NUGGET, Items.IRON_INGOT, "iron_ingot_from_nuggets", "iron_ingot");
        ShapedRecipeBuilder.shaped(Items.IRON_LEGGINGS).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("XXX").pattern("X X").pattern("X X").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.IRON_PICKAXE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("XXX").pattern(" # ").pattern(" # ").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.IRON_SHOVEL).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("X").pattern("#").pattern("#").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.IRON_SWORD).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("X").pattern("X").pattern("#").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.IRON_TRAPDOOR).define(Character.valueOf('#'), Items.IRON_INGOT).pattern("##").pattern("##").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.ITEM_FRAME).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.LEATHER).pattern("###").pattern("#X#").pattern("###").unlockedBy("has_leather", RecipeProvider.has(Items.LEATHER)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.JUKEBOX).define(Character.valueOf('#'), ItemTags.PLANKS).define(Character.valueOf('X'), Items.DIAMOND).pattern("###").pattern("#X#").pattern("###").unlockedBy("has_diamond", RecipeProvider.has(Items.DIAMOND)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.LADDER, 3).define(Character.valueOf('#'), Items.STICK).pattern("# #").pattern("###").pattern("# #").unlockedBy("has_stick", RecipeProvider.has(Items.STICK)).save(consumer);
        RecipeProvider.nineBlockStorageRecipes(consumer, Items.LAPIS_LAZULI, Items.LAPIS_BLOCK);
        ShapedRecipeBuilder.shaped(Items.LEAD, 2).define(Character.valueOf('~'), Items.STRING).define(Character.valueOf('O'), Items.SLIME_BALL).pattern("~~ ").pattern("~O ").pattern("  ~").unlockedBy("has_slime_ball", RecipeProvider.has(Items.SLIME_BALL)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.LEATHER).define(Character.valueOf('#'), Items.RABBIT_HIDE).pattern("##").pattern("##").unlockedBy("has_rabbit_hide", RecipeProvider.has(Items.RABBIT_HIDE)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.LEATHER_BOOTS).define(Character.valueOf('X'), Items.LEATHER).pattern("X X").pattern("X X").unlockedBy("has_leather", RecipeProvider.has(Items.LEATHER)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.LEATHER_CHESTPLATE).define(Character.valueOf('X'), Items.LEATHER).pattern("X X").pattern("XXX").pattern("XXX").unlockedBy("has_leather", RecipeProvider.has(Items.LEATHER)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.LEATHER_HELMET).define(Character.valueOf('X'), Items.LEATHER).pattern("XXX").pattern("X X").unlockedBy("has_leather", RecipeProvider.has(Items.LEATHER)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.LEATHER_LEGGINGS).define(Character.valueOf('X'), Items.LEATHER).pattern("XXX").pattern("X X").pattern("X X").unlockedBy("has_leather", RecipeProvider.has(Items.LEATHER)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.LEATHER_HORSE_ARMOR).define(Character.valueOf('X'), Items.LEATHER).pattern("X X").pattern("XXX").pattern("X X").unlockedBy("has_leather", RecipeProvider.has(Items.LEATHER)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.LECTERN).define(Character.valueOf('S'), ItemTags.WOODEN_SLABS).define(Character.valueOf('B'), Blocks.BOOKSHELF).pattern("SSS").pattern(" B ").pattern(" S ").unlockedBy("has_book", RecipeProvider.has(Items.BOOK)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.LEVER).define(Character.valueOf('#'), Blocks.COBBLESTONE).define(Character.valueOf('X'), Items.STICK).pattern("X").pattern("#").unlockedBy("has_cobblestone", RecipeProvider.has(Blocks.COBBLESTONE)).save(consumer);
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.LIGHT_BLUE_DYE, Blocks.BLUE_ORCHID, "light_blue_dye");
        ShapelessRecipeBuilder.shapeless(Items.LIGHT_BLUE_DYE, 2).requires(Items.BLUE_DYE).requires(Items.WHITE_DYE).group("light_blue_dye").unlockedBy("has_blue_dye", RecipeProvider.has(Items.BLUE_DYE)).unlockedBy("has_white_dye", RecipeProvider.has(Items.WHITE_DYE)).save(consumer, "light_blue_dye_from_blue_white_dye");
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.LIGHT_GRAY_DYE, Blocks.AZURE_BLUET, "light_gray_dye");
        ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE, 2).requires(Items.GRAY_DYE).requires(Items.WHITE_DYE).group("light_gray_dye").unlockedBy("has_gray_dye", RecipeProvider.has(Items.GRAY_DYE)).unlockedBy("has_white_dye", RecipeProvider.has(Items.WHITE_DYE)).save(consumer, "light_gray_dye_from_gray_white_dye");
        ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE, 3).requires(Items.BLACK_DYE).requires(Items.WHITE_DYE, 2).group("light_gray_dye").unlockedBy("has_white_dye", RecipeProvider.has(Items.WHITE_DYE)).unlockedBy("has_black_dye", RecipeProvider.has(Items.BLACK_DYE)).save(consumer, "light_gray_dye_from_black_white_dye");
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.LIGHT_GRAY_DYE, Blocks.OXEYE_DAISY, "light_gray_dye");
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.LIGHT_GRAY_DYE, Blocks.WHITE_TULIP, "light_gray_dye");
        RecipeProvider.pressurePlate(consumer, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.GOLD_INGOT);
        ShapedRecipeBuilder.shaped(Blocks.LIGHTNING_ROD).define(Character.valueOf('#'), Items.COPPER_INGOT).pattern("#").pattern("#").pattern("#").unlockedBy("has_copper_ingot", RecipeProvider.has(Items.COPPER_INGOT)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.LIME_DYE, 2).requires(Items.GREEN_DYE).requires(Items.WHITE_DYE).unlockedBy("has_green_dye", RecipeProvider.has(Items.GREEN_DYE)).unlockedBy("has_white_dye", RecipeProvider.has(Items.WHITE_DYE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.JACK_O_LANTERN).define(Character.valueOf('A'), Blocks.CARVED_PUMPKIN).define(Character.valueOf('B'), Blocks.TORCH).pattern("A").pattern("B").unlockedBy("has_carved_pumpkin", RecipeProvider.has(Blocks.CARVED_PUMPKIN)).save(consumer);
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.MAGENTA_DYE, Blocks.ALLIUM, "magenta_dye");
        ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE, 4).requires(Items.BLUE_DYE).requires(Items.RED_DYE, 2).requires(Items.WHITE_DYE).group("magenta_dye").unlockedBy("has_blue_dye", RecipeProvider.has(Items.BLUE_DYE)).unlockedBy("has_rose_red", RecipeProvider.has(Items.RED_DYE)).unlockedBy("has_white_dye", RecipeProvider.has(Items.WHITE_DYE)).save(consumer, "magenta_dye_from_blue_red_white_dye");
        ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE, 3).requires(Items.BLUE_DYE).requires(Items.RED_DYE).requires(Items.PINK_DYE).group("magenta_dye").unlockedBy("has_pink_dye", RecipeProvider.has(Items.PINK_DYE)).unlockedBy("has_blue_dye", RecipeProvider.has(Items.BLUE_DYE)).unlockedBy("has_red_dye", RecipeProvider.has(Items.RED_DYE)).save(consumer, "magenta_dye_from_blue_red_pink");
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.MAGENTA_DYE, Blocks.LILAC, "magenta_dye", 2);
        ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE, 2).requires(Items.PURPLE_DYE).requires(Items.PINK_DYE).group("magenta_dye").unlockedBy("has_pink_dye", RecipeProvider.has(Items.PINK_DYE)).unlockedBy("has_purple_dye", RecipeProvider.has(Items.PURPLE_DYE)).save(consumer, "magenta_dye_from_purple_and_pink");
        ShapedRecipeBuilder.shaped(Blocks.MAGMA_BLOCK).define(Character.valueOf('#'), Items.MAGMA_CREAM).pattern("##").pattern("##").unlockedBy("has_magma_cream", RecipeProvider.has(Items.MAGMA_CREAM)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.MAGMA_CREAM).requires(Items.BLAZE_POWDER).requires(Items.SLIME_BALL).unlockedBy("has_blaze_powder", RecipeProvider.has(Items.BLAZE_POWDER)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.MAP).define(Character.valueOf('#'), Items.PAPER).define(Character.valueOf('X'), Items.COMPASS).pattern("###").pattern("#X#").pattern("###").unlockedBy("has_compass", RecipeProvider.has(Items.COMPASS)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.MELON).define(Character.valueOf('M'), Items.MELON_SLICE).pattern("MMM").pattern("MMM").pattern("MMM").unlockedBy("has_melon", RecipeProvider.has(Items.MELON_SLICE)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.MELON_SEEDS).requires(Items.MELON_SLICE).unlockedBy("has_melon", RecipeProvider.has(Items.MELON_SLICE)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.MINECART).define(Character.valueOf('#'), Items.IRON_INGOT).pattern("# #").pattern("###").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Blocks.MOSSY_COBBLESTONE).requires(Blocks.COBBLESTONE).requires(Blocks.VINE).group("mossy_cobblestone").unlockedBy("has_vine", RecipeProvider.has(Blocks.VINE)).save(consumer, RecipeProvider.getConversionRecipeName(Blocks.MOSSY_COBBLESTONE, Blocks.VINE));
        ShapelessRecipeBuilder.shapeless(Blocks.MOSSY_STONE_BRICKS).requires(Blocks.STONE_BRICKS).requires(Blocks.VINE).group("mossy_stone_bricks").unlockedBy("has_vine", RecipeProvider.has(Blocks.VINE)).save(consumer, RecipeProvider.getConversionRecipeName(Blocks.MOSSY_STONE_BRICKS, Blocks.VINE));
        ShapelessRecipeBuilder.shapeless(Blocks.MOSSY_COBBLESTONE).requires(Blocks.COBBLESTONE).requires(Blocks.MOSS_BLOCK).group("mossy_cobblestone").unlockedBy("has_moss_block", RecipeProvider.has(Blocks.MOSS_BLOCK)).save(consumer, RecipeProvider.getConversionRecipeName(Blocks.MOSSY_COBBLESTONE, Blocks.MOSS_BLOCK));
        ShapelessRecipeBuilder.shapeless(Blocks.MOSSY_STONE_BRICKS).requires(Blocks.STONE_BRICKS).requires(Blocks.MOSS_BLOCK).group("mossy_stone_bricks").unlockedBy("has_moss_block", RecipeProvider.has(Blocks.MOSS_BLOCK)).save(consumer, RecipeProvider.getConversionRecipeName(Blocks.MOSSY_STONE_BRICKS, Blocks.MOSS_BLOCK));
        ShapelessRecipeBuilder.shapeless(Items.MUSHROOM_STEW).requires(Blocks.BROWN_MUSHROOM).requires(Blocks.RED_MUSHROOM).requires(Items.BOWL).unlockedBy("has_mushroom_stew", RecipeProvider.has(Items.MUSHROOM_STEW)).unlockedBy("has_bowl", RecipeProvider.has(Items.BOWL)).unlockedBy("has_brown_mushroom", RecipeProvider.has(Blocks.BROWN_MUSHROOM)).unlockedBy("has_red_mushroom", RecipeProvider.has(Blocks.RED_MUSHROOM)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.NETHER_BRICKS).define(Character.valueOf('N'), Items.NETHER_BRICK).pattern("NN").pattern("NN").unlockedBy("has_netherbrick", RecipeProvider.has(Items.NETHER_BRICK)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.NETHER_WART_BLOCK).define(Character.valueOf('#'), Items.NETHER_WART).pattern("###").pattern("###").pattern("###").unlockedBy("has_nether_wart", RecipeProvider.has(Items.NETHER_WART)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.NOTE_BLOCK).define(Character.valueOf('#'), ItemTags.PLANKS).define(Character.valueOf('X'), Items.REDSTONE).pattern("###").pattern("#X#").pattern("###").unlockedBy("has_redstone", RecipeProvider.has(Items.REDSTONE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.OBSERVER).define(Character.valueOf('Q'), Items.QUARTZ).define(Character.valueOf('R'), Items.REDSTONE).define(Character.valueOf('#'), Blocks.COBBLESTONE).pattern("###").pattern("RRQ").pattern("###").unlockedBy("has_quartz", RecipeProvider.has(Items.QUARTZ)).save(consumer);
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.ORANGE_DYE, Blocks.ORANGE_TULIP, "orange_dye");
        ShapelessRecipeBuilder.shapeless(Items.ORANGE_DYE, 2).requires(Items.RED_DYE).requires(Items.YELLOW_DYE).group("orange_dye").unlockedBy("has_red_dye", RecipeProvider.has(Items.RED_DYE)).unlockedBy("has_yellow_dye", RecipeProvider.has(Items.YELLOW_DYE)).save(consumer, "orange_dye_from_red_yellow");
        ShapedRecipeBuilder.shaped(Items.PAINTING).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Ingredient.of(ItemTags.WOOL)).pattern("###").pattern("#X#").pattern("###").unlockedBy("has_wool", RecipeProvider.has(ItemTags.WOOL)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.PAPER, 3).define(Character.valueOf('#'), Blocks.SUGAR_CANE).pattern("###").unlockedBy("has_reeds", RecipeProvider.has(Blocks.SUGAR_CANE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.QUARTZ_PILLAR, 2).define(Character.valueOf('#'), Blocks.QUARTZ_BLOCK).pattern("#").pattern("#").unlockedBy("has_chiseled_quartz_block", RecipeProvider.has(Blocks.CHISELED_QUARTZ_BLOCK)).unlockedBy("has_quartz_block", RecipeProvider.has(Blocks.QUARTZ_BLOCK)).unlockedBy("has_quartz_pillar", RecipeProvider.has(Blocks.QUARTZ_PILLAR)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Blocks.PACKED_ICE).requires(Blocks.ICE, 9).unlockedBy("has_ice", RecipeProvider.has(Blocks.ICE)).save(consumer);
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.PINK_DYE, Blocks.PEONY, "pink_dye", 2);
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.PINK_DYE, Blocks.PINK_TULIP, "pink_dye");
        ShapelessRecipeBuilder.shapeless(Items.PINK_DYE, 2).requires(Items.RED_DYE).requires(Items.WHITE_DYE).group("pink_dye").unlockedBy("has_white_dye", RecipeProvider.has(Items.WHITE_DYE)).unlockedBy("has_red_dye", RecipeProvider.has(Items.RED_DYE)).save(consumer, "pink_dye_from_red_white_dye");
        ShapedRecipeBuilder.shaped(Blocks.PISTON).define(Character.valueOf('R'), Items.REDSTONE).define(Character.valueOf('#'), Blocks.COBBLESTONE).define(Character.valueOf('T'), ItemTags.PLANKS).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("TTT").pattern("#X#").pattern("#R#").unlockedBy("has_redstone", RecipeProvider.has(Items.REDSTONE)).save(consumer);
        RecipeProvider.polished(consumer, Blocks.POLISHED_BASALT, Blocks.BASALT);
        ShapedRecipeBuilder.shaped(Blocks.PRISMARINE).define(Character.valueOf('S'), Items.PRISMARINE_SHARD).pattern("SS").pattern("SS").unlockedBy("has_prismarine_shard", RecipeProvider.has(Items.PRISMARINE_SHARD)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.PRISMARINE_BRICKS).define(Character.valueOf('S'), Items.PRISMARINE_SHARD).pattern("SSS").pattern("SSS").pattern("SSS").unlockedBy("has_prismarine_shard", RecipeProvider.has(Items.PRISMARINE_SHARD)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.PUMPKIN_PIE).requires(Blocks.PUMPKIN).requires(Items.SUGAR).requires(Items.EGG).unlockedBy("has_carved_pumpkin", RecipeProvider.has(Blocks.CARVED_PUMPKIN)).unlockedBy("has_pumpkin", RecipeProvider.has(Blocks.PUMPKIN)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.PUMPKIN_SEEDS, 4).requires(Blocks.PUMPKIN).unlockedBy("has_pumpkin", RecipeProvider.has(Blocks.PUMPKIN)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.PURPLE_DYE, 2).requires(Items.BLUE_DYE).requires(Items.RED_DYE).unlockedBy("has_blue_dye", RecipeProvider.has(Items.BLUE_DYE)).unlockedBy("has_red_dye", RecipeProvider.has(Items.RED_DYE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.SHULKER_BOX).define(Character.valueOf('#'), Blocks.CHEST).define(Character.valueOf('-'), Items.SHULKER_SHELL).pattern("-").pattern("#").pattern("-").unlockedBy("has_shulker_shell", RecipeProvider.has(Items.SHULKER_SHELL)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.PURPUR_BLOCK, 4).define(Character.valueOf('F'), Items.POPPED_CHORUS_FRUIT).pattern("FF").pattern("FF").unlockedBy("has_chorus_fruit_popped", RecipeProvider.has(Items.POPPED_CHORUS_FRUIT)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.PURPUR_PILLAR).define(Character.valueOf('#'), Blocks.PURPUR_SLAB).pattern("#").pattern("#").unlockedBy("has_purpur_block", RecipeProvider.has(Blocks.PURPUR_BLOCK)).save(consumer);
        RecipeProvider.slabBuilder(Blocks.PURPUR_SLAB, Ingredient.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR)).unlockedBy("has_purpur_block", RecipeProvider.has(Blocks.PURPUR_BLOCK)).save(consumer);
        RecipeProvider.stairBuilder(Blocks.PURPUR_STAIRS, Ingredient.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR)).unlockedBy("has_purpur_block", RecipeProvider.has(Blocks.PURPUR_BLOCK)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.QUARTZ_BLOCK).define(Character.valueOf('#'), Items.QUARTZ).pattern("##").pattern("##").unlockedBy("has_quartz", RecipeProvider.has(Items.QUARTZ)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.QUARTZ_BRICKS, 4).define(Character.valueOf('#'), Blocks.QUARTZ_BLOCK).pattern("##").pattern("##").unlockedBy("has_quartz_block", RecipeProvider.has(Blocks.QUARTZ_BLOCK)).save(consumer);
        RecipeProvider.slabBuilder(Blocks.QUARTZ_SLAB, Ingredient.of(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR)).unlockedBy("has_chiseled_quartz_block", RecipeProvider.has(Blocks.CHISELED_QUARTZ_BLOCK)).unlockedBy("has_quartz_block", RecipeProvider.has(Blocks.QUARTZ_BLOCK)).unlockedBy("has_quartz_pillar", RecipeProvider.has(Blocks.QUARTZ_PILLAR)).save(consumer);
        RecipeProvider.stairBuilder(Blocks.QUARTZ_STAIRS, Ingredient.of(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR)).unlockedBy("has_chiseled_quartz_block", RecipeProvider.has(Blocks.CHISELED_QUARTZ_BLOCK)).unlockedBy("has_quartz_block", RecipeProvider.has(Blocks.QUARTZ_BLOCK)).unlockedBy("has_quartz_pillar", RecipeProvider.has(Blocks.QUARTZ_PILLAR)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.RABBIT_STEW).requires(Items.BAKED_POTATO).requires(Items.COOKED_RABBIT).requires(Items.BOWL).requires(Items.CARROT).requires(Blocks.BROWN_MUSHROOM).group("rabbit_stew").unlockedBy("has_cooked_rabbit", RecipeProvider.has(Items.COOKED_RABBIT)).save(consumer, RecipeProvider.getConversionRecipeName(Items.RABBIT_STEW, Items.BROWN_MUSHROOM));
        ShapelessRecipeBuilder.shapeless(Items.RABBIT_STEW).requires(Items.BAKED_POTATO).requires(Items.COOKED_RABBIT).requires(Items.BOWL).requires(Items.CARROT).requires(Blocks.RED_MUSHROOM).group("rabbit_stew").unlockedBy("has_cooked_rabbit", RecipeProvider.has(Items.COOKED_RABBIT)).save(consumer, RecipeProvider.getConversionRecipeName(Items.RABBIT_STEW, Items.RED_MUSHROOM));
        ShapedRecipeBuilder.shaped(Blocks.RAIL, 16).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.IRON_INGOT).pattern("X X").pattern("X#X").pattern("X X").unlockedBy("has_minecart", RecipeProvider.has(Items.MINECART)).save(consumer);
        RecipeProvider.nineBlockStorageRecipes(consumer, Items.REDSTONE, Items.REDSTONE_BLOCK);
        ShapedRecipeBuilder.shaped(Blocks.REDSTONE_LAMP).define(Character.valueOf('R'), Items.REDSTONE).define(Character.valueOf('G'), Blocks.GLOWSTONE).pattern(" R ").pattern("RGR").pattern(" R ").unlockedBy("has_glowstone", RecipeProvider.has(Blocks.GLOWSTONE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.REDSTONE_TORCH).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Items.REDSTONE).pattern("X").pattern("#").unlockedBy("has_redstone", RecipeProvider.has(Items.REDSTONE)).save(consumer);
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.RED_DYE, Items.BEETROOT, "red_dye");
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.RED_DYE, Blocks.POPPY, "red_dye");
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.RED_DYE, Blocks.ROSE_BUSH, "red_dye", 2);
        ShapelessRecipeBuilder.shapeless(Items.RED_DYE).requires(Blocks.RED_TULIP).group("red_dye").unlockedBy("has_red_flower", RecipeProvider.has(Blocks.RED_TULIP)).save(consumer, "red_dye_from_tulip");
        ShapedRecipeBuilder.shaped(Blocks.RED_NETHER_BRICKS).define(Character.valueOf('W'), Items.NETHER_WART).define(Character.valueOf('N'), Items.NETHER_BRICK).pattern("NW").pattern("WN").unlockedBy("has_nether_wart", RecipeProvider.has(Items.NETHER_WART)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.RED_SANDSTONE).define(Character.valueOf('#'), Blocks.RED_SAND).pattern("##").pattern("##").unlockedBy("has_sand", RecipeProvider.has(Blocks.RED_SAND)).save(consumer);
        RecipeProvider.slabBuilder(Blocks.RED_SANDSTONE_SLAB, Ingredient.of(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE)).unlockedBy("has_red_sandstone", RecipeProvider.has(Blocks.RED_SANDSTONE)).unlockedBy("has_chiseled_red_sandstone", RecipeProvider.has(Blocks.CHISELED_RED_SANDSTONE)).save(consumer);
        RecipeProvider.stairBuilder(Blocks.RED_SANDSTONE_STAIRS, Ingredient.of(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE)).unlockedBy("has_red_sandstone", RecipeProvider.has(Blocks.RED_SANDSTONE)).unlockedBy("has_chiseled_red_sandstone", RecipeProvider.has(Blocks.CHISELED_RED_SANDSTONE)).unlockedBy("has_cut_red_sandstone", RecipeProvider.has(Blocks.CUT_RED_SANDSTONE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.REPEATER).define(Character.valueOf('#'), Blocks.REDSTONE_TORCH).define(Character.valueOf('X'), Items.REDSTONE).define(Character.valueOf('I'), Blocks.STONE).pattern("#X#").pattern("III").unlockedBy("has_redstone_torch", RecipeProvider.has(Blocks.REDSTONE_TORCH)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.SANDSTONE).define(Character.valueOf('#'), Blocks.SAND).pattern("##").pattern("##").unlockedBy("has_sand", RecipeProvider.has(Blocks.SAND)).save(consumer);
        RecipeProvider.slabBuilder(Blocks.SANDSTONE_SLAB, Ingredient.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE)).unlockedBy("has_sandstone", RecipeProvider.has(Blocks.SANDSTONE)).unlockedBy("has_chiseled_sandstone", RecipeProvider.has(Blocks.CHISELED_SANDSTONE)).save(consumer);
        RecipeProvider.stairBuilder(Blocks.SANDSTONE_STAIRS, Ingredient.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE)).unlockedBy("has_sandstone", RecipeProvider.has(Blocks.SANDSTONE)).unlockedBy("has_chiseled_sandstone", RecipeProvider.has(Blocks.CHISELED_SANDSTONE)).unlockedBy("has_cut_sandstone", RecipeProvider.has(Blocks.CUT_SANDSTONE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.SEA_LANTERN).define(Character.valueOf('S'), Items.PRISMARINE_SHARD).define(Character.valueOf('C'), Items.PRISMARINE_CRYSTALS).pattern("SCS").pattern("CCC").pattern("SCS").unlockedBy("has_prismarine_crystals", RecipeProvider.has(Items.PRISMARINE_CRYSTALS)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.SHEARS).define(Character.valueOf('#'), Items.IRON_INGOT).pattern(" #").pattern("# ").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.SHIELD).define(Character.valueOf('W'), ItemTags.PLANKS).define(Character.valueOf('o'), Items.IRON_INGOT).pattern("WoW").pattern("WWW").pattern(" W ").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        RecipeProvider.nineBlockStorageRecipes(consumer, Items.SLIME_BALL, Items.SLIME_BLOCK);
        RecipeProvider.cut(consumer, Blocks.CUT_RED_SANDSTONE, Blocks.RED_SANDSTONE);
        RecipeProvider.cut(consumer, Blocks.CUT_SANDSTONE, Blocks.SANDSTONE);
        ShapedRecipeBuilder.shaped(Blocks.SNOW_BLOCK).define(Character.valueOf('#'), Items.SNOWBALL).pattern("##").pattern("##").unlockedBy("has_snowball", RecipeProvider.has(Items.SNOWBALL)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.SNOW, 6).define(Character.valueOf('#'), Blocks.SNOW_BLOCK).pattern("###").unlockedBy("has_snowball", RecipeProvider.has(Items.SNOWBALL)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.SOUL_CAMPFIRE).define(Character.valueOf('L'), ItemTags.LOGS).define(Character.valueOf('S'), Items.STICK).define(Character.valueOf('#'), ItemTags.SOUL_FIRE_BASE_BLOCKS).pattern(" S ").pattern("S#S").pattern("LLL").unlockedBy("has_stick", RecipeProvider.has(Items.STICK)).unlockedBy("has_soul_sand", RecipeProvider.has(ItemTags.SOUL_FIRE_BASE_BLOCKS)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.GLISTERING_MELON_SLICE).define(Character.valueOf('#'), Items.GOLD_NUGGET).define(Character.valueOf('X'), Items.MELON_SLICE).pattern("###").pattern("#X#").pattern("###").unlockedBy("has_melon", RecipeProvider.has(Items.MELON_SLICE)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.SPECTRAL_ARROW, 2).define(Character.valueOf('#'), Items.GLOWSTONE_DUST).define(Character.valueOf('X'), Items.ARROW).pattern(" # ").pattern("#X#").pattern(" # ").unlockedBy("has_glowstone_dust", RecipeProvider.has(Items.GLOWSTONE_DUST)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.SPYGLASS).define(Character.valueOf('#'), Items.AMETHYST_SHARD).define(Character.valueOf('X'), Items.COPPER_INGOT).pattern(" # ").pattern(" X ").pattern(" X ").unlockedBy("has_amethyst_shard", RecipeProvider.has(Items.AMETHYST_SHARD)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.STICK, 4).define(Character.valueOf('#'), ItemTags.PLANKS).pattern("#").pattern("#").group("sticks").unlockedBy("has_planks", RecipeProvider.has(ItemTags.PLANKS)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.STICK, 1).define(Character.valueOf('#'), Blocks.BAMBOO).pattern("#").pattern("#").group("sticks").unlockedBy("has_bamboo", RecipeProvider.has(Blocks.BAMBOO)).save(consumer, "stick_from_bamboo_item");
        ShapedRecipeBuilder.shaped(Blocks.STICKY_PISTON).define(Character.valueOf('P'), Blocks.PISTON).define(Character.valueOf('S'), Items.SLIME_BALL).pattern("S").pattern("P").unlockedBy("has_slime_ball", RecipeProvider.has(Items.SLIME_BALL)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.STONE_BRICKS, 4).define(Character.valueOf('#'), Blocks.STONE).pattern("##").pattern("##").unlockedBy("has_stone", RecipeProvider.has(Blocks.STONE)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.STONE_AXE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), ItemTags.STONE_TOOL_MATERIALS).pattern("XX").pattern("X#").pattern(" #").unlockedBy("has_cobblestone", RecipeProvider.has(ItemTags.STONE_TOOL_MATERIALS)).save(consumer);
        RecipeProvider.slabBuilder(Blocks.STONE_BRICK_SLAB, Ingredient.of(Blocks.STONE_BRICKS)).unlockedBy("has_stone_bricks", RecipeProvider.has(ItemTags.STONE_BRICKS)).save(consumer);
        RecipeProvider.stairBuilder(Blocks.STONE_BRICK_STAIRS, Ingredient.of(Blocks.STONE_BRICKS)).unlockedBy("has_stone_bricks", RecipeProvider.has(ItemTags.STONE_BRICKS)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.STONE_HOE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), ItemTags.STONE_TOOL_MATERIALS).pattern("XX").pattern(" #").pattern(" #").unlockedBy("has_cobblestone", RecipeProvider.has(ItemTags.STONE_TOOL_MATERIALS)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.STONE_PICKAXE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), ItemTags.STONE_TOOL_MATERIALS).pattern("XXX").pattern(" # ").pattern(" # ").unlockedBy("has_cobblestone", RecipeProvider.has(ItemTags.STONE_TOOL_MATERIALS)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.STONE_SHOVEL).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), ItemTags.STONE_TOOL_MATERIALS).pattern("X").pattern("#").pattern("#").unlockedBy("has_cobblestone", RecipeProvider.has(ItemTags.STONE_TOOL_MATERIALS)).save(consumer);
        RecipeProvider.slab(consumer, Blocks.SMOOTH_STONE_SLAB, Blocks.SMOOTH_STONE);
        ShapedRecipeBuilder.shaped(Items.STONE_SWORD).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), ItemTags.STONE_TOOL_MATERIALS).pattern("X").pattern("X").pattern("#").unlockedBy("has_cobblestone", RecipeProvider.has(ItemTags.STONE_TOOL_MATERIALS)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.WHITE_WOOL).define(Character.valueOf('#'), Items.STRING).pattern("##").pattern("##").unlockedBy("has_string", RecipeProvider.has(Items.STRING)).save(consumer, RecipeProvider.getConversionRecipeName(Blocks.WHITE_WOOL, Items.STRING));
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.SUGAR, Blocks.SUGAR_CANE, "sugar");
        ShapelessRecipeBuilder.shapeless(Items.SUGAR, 3).requires(Items.HONEY_BOTTLE).group("sugar").unlockedBy("has_honey_bottle", RecipeProvider.has(Items.HONEY_BOTTLE)).save(consumer, RecipeProvider.getConversionRecipeName(Items.SUGAR, Items.HONEY_BOTTLE));
        ShapedRecipeBuilder.shaped(Blocks.TARGET).define(Character.valueOf('H'), Items.HAY_BLOCK).define(Character.valueOf('R'), Items.REDSTONE).pattern(" R ").pattern("RHR").pattern(" R ").unlockedBy("has_redstone", RecipeProvider.has(Items.REDSTONE)).unlockedBy("has_hay_block", RecipeProvider.has(Blocks.HAY_BLOCK)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.TNT).define(Character.valueOf('#'), Ingredient.of(Blocks.SAND, Blocks.RED_SAND)).define(Character.valueOf('X'), Items.GUNPOWDER).pattern("X#X").pattern("#X#").pattern("X#X").unlockedBy("has_gunpowder", RecipeProvider.has(Items.GUNPOWDER)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.TNT_MINECART).define(Character.valueOf('A'), Blocks.TNT).define(Character.valueOf('B'), Items.MINECART).pattern("A").pattern("B").unlockedBy("has_minecart", RecipeProvider.has(Items.MINECART)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.TORCH, 4).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), Ingredient.of(Items.COAL, Items.CHARCOAL)).pattern("X").pattern("#").unlockedBy("has_stone_pickaxe", RecipeProvider.has(Items.STONE_PICKAXE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.SOUL_TORCH, 4).define(Character.valueOf('X'), Ingredient.of(Items.COAL, Items.CHARCOAL)).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('S'), ItemTags.SOUL_FIRE_BASE_BLOCKS).pattern("X").pattern("#").pattern("S").unlockedBy("has_soul_sand", RecipeProvider.has(ItemTags.SOUL_FIRE_BASE_BLOCKS)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.LANTERN).define(Character.valueOf('#'), Items.TORCH).define(Character.valueOf('X'), Items.IRON_NUGGET).pattern("XXX").pattern("X#X").pattern("XXX").unlockedBy("has_iron_nugget", RecipeProvider.has(Items.IRON_NUGGET)).unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.SOUL_LANTERN).define(Character.valueOf('#'), Items.SOUL_TORCH).define(Character.valueOf('X'), Items.IRON_NUGGET).pattern("XXX").pattern("X#X").pattern("XXX").unlockedBy("has_soul_torch", RecipeProvider.has(Items.SOUL_TORCH)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Blocks.TRAPPED_CHEST).requires(Blocks.CHEST).requires(Blocks.TRIPWIRE_HOOK).unlockedBy("has_tripwire_hook", RecipeProvider.has(Blocks.TRIPWIRE_HOOK)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.TRIPWIRE_HOOK, 2).define(Character.valueOf('#'), ItemTags.PLANKS).define(Character.valueOf('S'), Items.STICK).define(Character.valueOf('I'), Items.IRON_INGOT).pattern("I").pattern("S").pattern("#").unlockedBy("has_string", RecipeProvider.has(Items.STRING)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.TURTLE_HELMET).define(Character.valueOf('X'), Items.SCUTE).pattern("XXX").pattern("X X").unlockedBy("has_scute", RecipeProvider.has(Items.SCUTE)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.WHEAT, 9).requires(Blocks.HAY_BLOCK).unlockedBy("has_hay_block", RecipeProvider.has(Blocks.HAY_BLOCK)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.WHITE_DYE).requires(Items.BONE_MEAL).group("white_dye").unlockedBy("has_bone_meal", RecipeProvider.has(Items.BONE_MEAL)).save(consumer);
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.WHITE_DYE, Blocks.LILY_OF_THE_VALLEY, "white_dye");
        ShapedRecipeBuilder.shaped(Items.WOODEN_AXE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), ItemTags.PLANKS).pattern("XX").pattern("X#").pattern(" #").unlockedBy("has_stick", RecipeProvider.has(Items.STICK)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.WOODEN_HOE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), ItemTags.PLANKS).pattern("XX").pattern(" #").pattern(" #").unlockedBy("has_stick", RecipeProvider.has(Items.STICK)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.WOODEN_PICKAXE).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), ItemTags.PLANKS).pattern("XXX").pattern(" # ").pattern(" # ").unlockedBy("has_stick", RecipeProvider.has(Items.STICK)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.WOODEN_SHOVEL).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), ItemTags.PLANKS).pattern("X").pattern("#").pattern("#").unlockedBy("has_stick", RecipeProvider.has(Items.STICK)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.WOODEN_SWORD).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('X'), ItemTags.PLANKS).pattern("X").pattern("X").pattern("#").unlockedBy("has_stick", RecipeProvider.has(Items.STICK)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.WRITABLE_BOOK).requires(Items.BOOK).requires(Items.INK_SAC).requires(Items.FEATHER).unlockedBy("has_book", RecipeProvider.has(Items.BOOK)).save(consumer);
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.YELLOW_DYE, Blocks.DANDELION, "yellow_dye");
        RecipeProvider.oneToOneConversionRecipe(consumer, Items.YELLOW_DYE, Blocks.SUNFLOWER, "yellow_dye", 2);
        RecipeProvider.nineBlockStorageRecipes(consumer, Items.DRIED_KELP, Items.DRIED_KELP_BLOCK);
        ShapedRecipeBuilder.shaped(Blocks.CONDUIT).define(Character.valueOf('#'), Items.NAUTILUS_SHELL).define(Character.valueOf('X'), Items.HEART_OF_THE_SEA).pattern("###").pattern("#X#").pattern("###").unlockedBy("has_nautilus_core", RecipeProvider.has(Items.HEART_OF_THE_SEA)).unlockedBy("has_nautilus_shell", RecipeProvider.has(Items.NAUTILUS_SHELL)).save(consumer);
        RecipeProvider.wall(consumer, Blocks.RED_SANDSTONE_WALL, Blocks.RED_SANDSTONE);
        RecipeProvider.wall(consumer, Blocks.STONE_BRICK_WALL, Blocks.STONE_BRICKS);
        RecipeProvider.wall(consumer, Blocks.SANDSTONE_WALL, Blocks.SANDSTONE);
        ShapelessRecipeBuilder.shapeless(Items.CREEPER_BANNER_PATTERN).requires(Items.PAPER).requires(Items.CREEPER_HEAD).unlockedBy("has_creeper_head", RecipeProvider.has(Items.CREEPER_HEAD)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.SKULL_BANNER_PATTERN).requires(Items.PAPER).requires(Items.WITHER_SKELETON_SKULL).unlockedBy("has_wither_skeleton_skull", RecipeProvider.has(Items.WITHER_SKELETON_SKULL)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.FLOWER_BANNER_PATTERN).requires(Items.PAPER).requires(Blocks.OXEYE_DAISY).unlockedBy("has_oxeye_daisy", RecipeProvider.has(Blocks.OXEYE_DAISY)).save(consumer);
        ShapelessRecipeBuilder.shapeless(Items.MOJANG_BANNER_PATTERN).requires(Items.PAPER).requires(Items.ENCHANTED_GOLDEN_APPLE).unlockedBy("has_enchanted_golden_apple", RecipeProvider.has(Items.ENCHANTED_GOLDEN_APPLE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.SCAFFOLDING, 6).define(Character.valueOf('~'), Items.STRING).define(Character.valueOf('I'), Blocks.BAMBOO).pattern("I~I").pattern("I I").pattern("I I").unlockedBy("has_bamboo", RecipeProvider.has(Blocks.BAMBOO)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.GRINDSTONE).define(Character.valueOf('I'), Items.STICK).define(Character.valueOf('-'), Blocks.STONE_SLAB).define(Character.valueOf('#'), ItemTags.PLANKS).pattern("I-I").pattern("# #").unlockedBy("has_stone_slab", RecipeProvider.has(Blocks.STONE_SLAB)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.BLAST_FURNACE).define(Character.valueOf('#'), Blocks.SMOOTH_STONE).define(Character.valueOf('X'), Blocks.FURNACE).define(Character.valueOf('I'), Items.IRON_INGOT).pattern("III").pattern("IXI").pattern("###").unlockedBy("has_smooth_stone", RecipeProvider.has(Blocks.SMOOTH_STONE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.SMOKER).define(Character.valueOf('#'), ItemTags.LOGS).define(Character.valueOf('X'), Blocks.FURNACE).pattern(" # ").pattern("#X#").pattern(" # ").unlockedBy("has_furnace", RecipeProvider.has(Blocks.FURNACE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.CARTOGRAPHY_TABLE).define(Character.valueOf('#'), ItemTags.PLANKS).define(Character.valueOf('@'), Items.PAPER).pattern("@@").pattern("##").pattern("##").unlockedBy("has_paper", RecipeProvider.has(Items.PAPER)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.SMITHING_TABLE).define(Character.valueOf('#'), ItemTags.PLANKS).define(Character.valueOf('@'), Items.IRON_INGOT).pattern("@@").pattern("##").pattern("##").unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.FLETCHING_TABLE).define(Character.valueOf('#'), ItemTags.PLANKS).define(Character.valueOf('@'), Items.FLINT).pattern("@@").pattern("##").pattern("##").unlockedBy("has_flint", RecipeProvider.has(Items.FLINT)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.STONECUTTER).define(Character.valueOf('I'), Items.IRON_INGOT).define(Character.valueOf('#'), Blocks.STONE).pattern(" I ").pattern("###").unlockedBy("has_stone", RecipeProvider.has(Blocks.STONE)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.LODESTONE).define(Character.valueOf('S'), Items.CHISELED_STONE_BRICKS).define(Character.valueOf('#'), Items.NETHERITE_INGOT).pattern("SSS").pattern("S#S").pattern("SSS").unlockedBy("has_netherite_ingot", RecipeProvider.has(Items.NETHERITE_INGOT)).save(consumer);
        RecipeProvider.nineBlockStorageRecipesRecipesWithCustomUnpacking(consumer, Items.NETHERITE_INGOT, Items.NETHERITE_BLOCK, "netherite_ingot_from_netherite_block", "netherite_ingot");
        ShapelessRecipeBuilder.shapeless(Items.NETHERITE_INGOT).requires(Items.NETHERITE_SCRAP, 4).requires(Items.GOLD_INGOT, 4).group("netherite_ingot").unlockedBy("has_netherite_scrap", RecipeProvider.has(Items.NETHERITE_SCRAP)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.RESPAWN_ANCHOR).define(Character.valueOf('O'), Blocks.CRYING_OBSIDIAN).define(Character.valueOf('G'), Blocks.GLOWSTONE).pattern("OOO").pattern("GGG").pattern("OOO").unlockedBy("has_obsidian", RecipeProvider.has(Blocks.CRYING_OBSIDIAN)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.CHAIN).define(Character.valueOf('I'), Items.IRON_INGOT).define(Character.valueOf('N'), Items.IRON_NUGGET).pattern("N").pattern("I").pattern("N").unlockedBy("has_iron_nugget", RecipeProvider.has(Items.IRON_NUGGET)).unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(Items.CANDLE).define(Character.valueOf('S'), Items.STRING).define(Character.valueOf('H'), Items.HONEYCOMB).pattern("S").pattern("H").unlockedBy("has_string", RecipeProvider.has(Items.STRING)).unlockedBy("has_honeycomb", RecipeProvider.has(Items.HONEYCOMB)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.TINTED_GLASS, 2).define(Character.valueOf('G'), Blocks.GLASS).define(Character.valueOf('S'), Items.AMETHYST_SHARD).pattern(" S ").pattern("SGS").pattern(" S ").unlockedBy("has_amethyst_shard", RecipeProvider.has(Items.AMETHYST_SHARD)).save(consumer);
        ShapedRecipeBuilder.shaped(Blocks.AMETHYST_BLOCK).define(Character.valueOf('S'), Items.AMETHYST_SHARD).pattern("SS").pattern("SS").unlockedBy("has_amethyst_shard", RecipeProvider.has(Items.AMETHYST_SHARD)).save(consumer);
        SpecialRecipeBuilder.special(RecipeSerializer.ARMOR_DYE).save(consumer, "armor_dye");
        SpecialRecipeBuilder.special(RecipeSerializer.BANNER_DUPLICATE).save(consumer, "banner_duplicate");
        SpecialRecipeBuilder.special(RecipeSerializer.BOOK_CLONING).save(consumer, "book_cloning");
        ShapedRecipeBuilder.shaped(Items.BUNDLE).define(Character.valueOf('#'), Items.RABBIT_HIDE).define(Character.valueOf('-'), Items.STRING).pattern("-#-").pattern("# #").pattern("###").unlockedBy("has_string", RecipeProvider.has(Items.STRING)).save(consumer);
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
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.POTATO), Items.BAKED_POTATO, 0.35f, 200).unlockedBy("has_potato", RecipeProvider.has(Items.POTATO)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CLAY_BALL), Items.BRICK, 0.3f, 200).unlockedBy("has_clay_ball", RecipeProvider.has(Items.CLAY_BALL)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.LOGS_THAT_BURN), Items.CHARCOAL, 0.15f, 200).unlockedBy("has_log", RecipeProvider.has(ItemTags.LOGS_THAT_BURN)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CHORUS_FRUIT), Items.POPPED_CHORUS_FRUIT, 0.1f, 200).unlockedBy("has_chorus_fruit", RecipeProvider.has(Items.CHORUS_FRUIT)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.BEEF), Items.COOKED_BEEF, 0.35f, 200).unlockedBy("has_beef", RecipeProvider.has(Items.BEEF)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CHICKEN), Items.COOKED_CHICKEN, 0.35f, 200).unlockedBy("has_chicken", RecipeProvider.has(Items.CHICKEN)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.COD), Items.COOKED_COD, 0.35f, 200).unlockedBy("has_cod", RecipeProvider.has(Items.COD)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.KELP), Items.DRIED_KELP, 0.1f, 200).unlockedBy("has_kelp", RecipeProvider.has(Blocks.KELP)).save(consumer, RecipeProvider.getSmeltingRecipeName(Items.DRIED_KELP));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.SALMON), Items.COOKED_SALMON, 0.35f, 200).unlockedBy("has_salmon", RecipeProvider.has(Items.SALMON)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.MUTTON), Items.COOKED_MUTTON, 0.35f, 200).unlockedBy("has_mutton", RecipeProvider.has(Items.MUTTON)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.PORKCHOP), Items.COOKED_PORKCHOP, 0.35f, 200).unlockedBy("has_porkchop", RecipeProvider.has(Items.PORKCHOP)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.RABBIT), Items.COOKED_RABBIT, 0.35f, 200).unlockedBy("has_rabbit", RecipeProvider.has(Items.RABBIT)).save(consumer);
        RecipeProvider.oreSmelting(consumer, COAL_SMELTABLES, Items.COAL, 0.1f, 200, "coal");
        RecipeProvider.oreSmelting(consumer, IRON_SMELTABLES, Items.IRON_INGOT, 0.7f, 200, "iron_ingot");
        RecipeProvider.oreSmelting(consumer, COPPER_SMELTABLES, Items.COPPER_INGOT, 0.7f, 200, "copper_ingot");
        RecipeProvider.oreSmelting(consumer, GOLD_SMELTABLES, Items.GOLD_INGOT, 1.0f, 200, "gold_ingot");
        RecipeProvider.oreSmelting(consumer, DIAMOND_SMELTABLES, Items.DIAMOND, 1.0f, 200, "diamond");
        RecipeProvider.oreSmelting(consumer, LAPIS_SMELTABLES, Items.LAPIS_LAZULI, 0.2f, 200, "lapus_lazuli");
        RecipeProvider.oreSmelting(consumer, REDSTONE_SMELTABLES, Items.REDSTONE, 0.7f, 200, "redstone");
        RecipeProvider.oreSmelting(consumer, EMERALD_SMELTABLES, Items.EMERALD, 1.0f, 200, "emerald");
        RecipeProvider.nineBlockStorageRecipes(consumer, Items.RAW_IRON, Items.RAW_IRON_BLOCK);
        RecipeProvider.nineBlockStorageRecipes(consumer, Items.RAW_COPPER, Items.RAW_COPPER_BLOCK);
        RecipeProvider.nineBlockStorageRecipes(consumer, Items.RAW_GOLD, Items.RAW_GOLD_BLOCK);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.SAND), Blocks.GLASS.asItem(), 0.1f, 200).unlockedBy("has_sand", RecipeProvider.has(ItemTags.SAND)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.SEA_PICKLE), Items.LIME_DYE, 0.1f, 200).unlockedBy("has_sea_pickle", RecipeProvider.has(Blocks.SEA_PICKLE)).save(consumer, RecipeProvider.getSmeltingRecipeName(Items.LIME_DYE));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CACTUS.asItem()), Items.GREEN_DYE, 1.0f, 200).unlockedBy("has_cactus", RecipeProvider.has(Blocks.CACTUS)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE, Items.GOLDEN_HOE, Items.GOLDEN_SWORD, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, Items.GOLDEN_HORSE_ARMOR), Items.GOLD_NUGGET, 0.1f, 200).unlockedBy("has_golden_pickaxe", RecipeProvider.has(Items.GOLDEN_PICKAXE)).unlockedBy("has_golden_shovel", RecipeProvider.has(Items.GOLDEN_SHOVEL)).unlockedBy("has_golden_axe", RecipeProvider.has(Items.GOLDEN_AXE)).unlockedBy("has_golden_hoe", RecipeProvider.has(Items.GOLDEN_HOE)).unlockedBy("has_golden_sword", RecipeProvider.has(Items.GOLDEN_SWORD)).unlockedBy("has_golden_helmet", RecipeProvider.has(Items.GOLDEN_HELMET)).unlockedBy("has_golden_chestplate", RecipeProvider.has(Items.GOLDEN_CHESTPLATE)).unlockedBy("has_golden_leggings", RecipeProvider.has(Items.GOLDEN_LEGGINGS)).unlockedBy("has_golden_boots", RecipeProvider.has(Items.GOLDEN_BOOTS)).unlockedBy("has_golden_horse_armor", RecipeProvider.has(Items.GOLDEN_HORSE_ARMOR)).save(consumer, RecipeProvider.getSmeltingRecipeName(Items.GOLD_NUGGET));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_AXE, Items.IRON_HOE, Items.IRON_SWORD, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, Items.IRON_HORSE_ARMOR, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS), Items.IRON_NUGGET, 0.1f, 200).unlockedBy("has_iron_pickaxe", RecipeProvider.has(Items.IRON_PICKAXE)).unlockedBy("has_iron_shovel", RecipeProvider.has(Items.IRON_SHOVEL)).unlockedBy("has_iron_axe", RecipeProvider.has(Items.IRON_AXE)).unlockedBy("has_iron_hoe", RecipeProvider.has(Items.IRON_HOE)).unlockedBy("has_iron_sword", RecipeProvider.has(Items.IRON_SWORD)).unlockedBy("has_iron_helmet", RecipeProvider.has(Items.IRON_HELMET)).unlockedBy("has_iron_chestplate", RecipeProvider.has(Items.IRON_CHESTPLATE)).unlockedBy("has_iron_leggings", RecipeProvider.has(Items.IRON_LEGGINGS)).unlockedBy("has_iron_boots", RecipeProvider.has(Items.IRON_BOOTS)).unlockedBy("has_iron_horse_armor", RecipeProvider.has(Items.IRON_HORSE_ARMOR)).unlockedBy("has_chainmail_helmet", RecipeProvider.has(Items.CHAINMAIL_HELMET)).unlockedBy("has_chainmail_chestplate", RecipeProvider.has(Items.CHAINMAIL_CHESTPLATE)).unlockedBy("has_chainmail_leggings", RecipeProvider.has(Items.CHAINMAIL_LEGGINGS)).unlockedBy("has_chainmail_boots", RecipeProvider.has(Items.CHAINMAIL_BOOTS)).save(consumer, RecipeProvider.getSmeltingRecipeName(Items.IRON_NUGGET));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CLAY), Blocks.TERRACOTTA.asItem(), 0.35f, 200).unlockedBy("has_clay_block", RecipeProvider.has(Blocks.CLAY)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.NETHERRACK), Items.NETHER_BRICK, 0.1f, 200).unlockedBy("has_netherrack", RecipeProvider.has(Blocks.NETHERRACK)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.NETHER_QUARTZ_ORE), Items.QUARTZ, 0.2f, 200).unlockedBy("has_nether_quartz_ore", RecipeProvider.has(Blocks.NETHER_QUARTZ_ORE)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.WET_SPONGE), Blocks.SPONGE.asItem(), 0.15f, 200).unlockedBy("has_wet_sponge", RecipeProvider.has(Blocks.WET_SPONGE)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.COBBLESTONE), Blocks.STONE.asItem(), 0.1f, 200).unlockedBy("has_cobblestone", RecipeProvider.has(Blocks.COBBLESTONE)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.STONE), Blocks.SMOOTH_STONE.asItem(), 0.1f, 200).unlockedBy("has_stone", RecipeProvider.has(Blocks.STONE)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.SANDSTONE), Blocks.SMOOTH_SANDSTONE.asItem(), 0.1f, 200).unlockedBy("has_sandstone", RecipeProvider.has(Blocks.SANDSTONE)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.SMOOTH_RED_SANDSTONE.asItem(), 0.1f, 200).unlockedBy("has_red_sandstone", RecipeProvider.has(Blocks.RED_SANDSTONE)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.SMOOTH_QUARTZ.asItem(), 0.1f, 200).unlockedBy("has_quartz_block", RecipeProvider.has(Blocks.QUARTZ_BLOCK)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.CRACKED_STONE_BRICKS.asItem(), 0.1f, 200).unlockedBy("has_stone_bricks", RecipeProvider.has(Blocks.STONE_BRICKS)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BLACK_TERRACOTTA), Blocks.BLACK_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_black_terracotta", RecipeProvider.has(Blocks.BLACK_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BLUE_TERRACOTTA), Blocks.BLUE_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_blue_terracotta", RecipeProvider.has(Blocks.BLUE_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BROWN_TERRACOTTA), Blocks.BROWN_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_brown_terracotta", RecipeProvider.has(Blocks.BROWN_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CYAN_TERRACOTTA), Blocks.CYAN_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_cyan_terracotta", RecipeProvider.has(Blocks.CYAN_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.GRAY_TERRACOTTA), Blocks.GRAY_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_gray_terracotta", RecipeProvider.has(Blocks.GRAY_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.GREEN_TERRACOTTA), Blocks.GREEN_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_green_terracotta", RecipeProvider.has(Blocks.GREEN_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LIGHT_BLUE_TERRACOTTA), Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_light_blue_terracotta", RecipeProvider.has(Blocks.LIGHT_BLUE_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LIGHT_GRAY_TERRACOTTA), Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_light_gray_terracotta", RecipeProvider.has(Blocks.LIGHT_GRAY_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LIME_TERRACOTTA), Blocks.LIME_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_lime_terracotta", RecipeProvider.has(Blocks.LIME_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.MAGENTA_TERRACOTTA), Blocks.MAGENTA_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_magenta_terracotta", RecipeProvider.has(Blocks.MAGENTA_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.ORANGE_TERRACOTTA), Blocks.ORANGE_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_orange_terracotta", RecipeProvider.has(Blocks.ORANGE_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.PINK_TERRACOTTA), Blocks.PINK_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_pink_terracotta", RecipeProvider.has(Blocks.PINK_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.PURPLE_TERRACOTTA), Blocks.PURPLE_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_purple_terracotta", RecipeProvider.has(Blocks.PURPLE_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.RED_TERRACOTTA), Blocks.RED_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_red_terracotta", RecipeProvider.has(Blocks.RED_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.WHITE_TERRACOTTA), Blocks.WHITE_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_white_terracotta", RecipeProvider.has(Blocks.WHITE_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.YELLOW_TERRACOTTA), Blocks.YELLOW_GLAZED_TERRACOTTA.asItem(), 0.1f, 200).unlockedBy("has_yellow_terracotta", RecipeProvider.has(Blocks.YELLOW_TERRACOTTA)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.ANCIENT_DEBRIS), Items.NETHERITE_SCRAP, 2.0f, 200).unlockedBy("has_ancient_debris", RecipeProvider.has(Blocks.ANCIENT_DEBRIS)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BASALT), Blocks.SMOOTH_BASALT, 0.1f, 200).unlockedBy("has_basalt", RecipeProvider.has(Blocks.BASALT)).save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.COBBLED_DEEPSLATE), Blocks.DEEPSLATE, 0.1f, 200).unlockedBy("has_cobbled_deepslate", RecipeProvider.has(Blocks.COBBLED_DEEPSLATE)).save(consumer);
        RecipeProvider.oreBlasting(consumer, COAL_SMELTABLES, Items.COAL, 0.1f, 100, "coal");
        RecipeProvider.oreBlasting(consumer, IRON_SMELTABLES, Items.IRON_INGOT, 0.7f, 100, "iron_ingot");
        RecipeProvider.oreBlasting(consumer, COPPER_SMELTABLES, Items.COPPER_INGOT, 0.7f, 100, "copper_ingot");
        RecipeProvider.oreBlasting(consumer, GOLD_SMELTABLES, Items.GOLD_INGOT, 1.0f, 100, "gold_ingot");
        RecipeProvider.oreBlasting(consumer, DIAMOND_SMELTABLES, Items.DIAMOND, 1.0f, 100, "diamond");
        RecipeProvider.oreBlasting(consumer, LAPIS_SMELTABLES, Items.LAPIS_LAZULI, 0.2f, 100, "lapus_lazuli");
        RecipeProvider.oreBlasting(consumer, REDSTONE_SMELTABLES, Items.REDSTONE, 0.7f, 100, "redstone");
        RecipeProvider.oreBlasting(consumer, EMERALD_SMELTABLES, Items.EMERALD, 1.0f, 100, "emerald");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.NETHER_QUARTZ_ORE), Items.QUARTZ, 0.2f, 100).unlockedBy("has_nether_quartz_ore", RecipeProvider.has(Blocks.NETHER_QUARTZ_ORE)).save(consumer, RecipeProvider.getBlastingRecipeName(Items.QUARTZ));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE, Items.GOLDEN_HOE, Items.GOLDEN_SWORD, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, Items.GOLDEN_HORSE_ARMOR), Items.GOLD_NUGGET, 0.1f, 100).unlockedBy("has_golden_pickaxe", RecipeProvider.has(Items.GOLDEN_PICKAXE)).unlockedBy("has_golden_shovel", RecipeProvider.has(Items.GOLDEN_SHOVEL)).unlockedBy("has_golden_axe", RecipeProvider.has(Items.GOLDEN_AXE)).unlockedBy("has_golden_hoe", RecipeProvider.has(Items.GOLDEN_HOE)).unlockedBy("has_golden_sword", RecipeProvider.has(Items.GOLDEN_SWORD)).unlockedBy("has_golden_helmet", RecipeProvider.has(Items.GOLDEN_HELMET)).unlockedBy("has_golden_chestplate", RecipeProvider.has(Items.GOLDEN_CHESTPLATE)).unlockedBy("has_golden_leggings", RecipeProvider.has(Items.GOLDEN_LEGGINGS)).unlockedBy("has_golden_boots", RecipeProvider.has(Items.GOLDEN_BOOTS)).unlockedBy("has_golden_horse_armor", RecipeProvider.has(Items.GOLDEN_HORSE_ARMOR)).save(consumer, RecipeProvider.getBlastingRecipeName(Items.GOLD_NUGGET));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_AXE, Items.IRON_HOE, Items.IRON_SWORD, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, Items.IRON_HORSE_ARMOR, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS), Items.IRON_NUGGET, 0.1f, 100).unlockedBy("has_iron_pickaxe", RecipeProvider.has(Items.IRON_PICKAXE)).unlockedBy("has_iron_shovel", RecipeProvider.has(Items.IRON_SHOVEL)).unlockedBy("has_iron_axe", RecipeProvider.has(Items.IRON_AXE)).unlockedBy("has_iron_hoe", RecipeProvider.has(Items.IRON_HOE)).unlockedBy("has_iron_sword", RecipeProvider.has(Items.IRON_SWORD)).unlockedBy("has_iron_helmet", RecipeProvider.has(Items.IRON_HELMET)).unlockedBy("has_iron_chestplate", RecipeProvider.has(Items.IRON_CHESTPLATE)).unlockedBy("has_iron_leggings", RecipeProvider.has(Items.IRON_LEGGINGS)).unlockedBy("has_iron_boots", RecipeProvider.has(Items.IRON_BOOTS)).unlockedBy("has_iron_horse_armor", RecipeProvider.has(Items.IRON_HORSE_ARMOR)).unlockedBy("has_chainmail_helmet", RecipeProvider.has(Items.CHAINMAIL_HELMET)).unlockedBy("has_chainmail_chestplate", RecipeProvider.has(Items.CHAINMAIL_CHESTPLATE)).unlockedBy("has_chainmail_leggings", RecipeProvider.has(Items.CHAINMAIL_LEGGINGS)).unlockedBy("has_chainmail_boots", RecipeProvider.has(Items.CHAINMAIL_BOOTS)).save(consumer, RecipeProvider.getBlastingRecipeName(Items.IRON_NUGGET));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.ANCIENT_DEBRIS), Items.NETHERITE_SCRAP, 2.0f, 100).unlockedBy("has_ancient_debris", RecipeProvider.has(Blocks.ANCIENT_DEBRIS)).save(consumer, RecipeProvider.getBlastingRecipeName(Items.NETHERITE_SCRAP));
        RecipeProvider.cookRecipes(consumer, "smoking", RecipeSerializer.SMOKING_RECIPE, 100);
        RecipeProvider.cookRecipes(consumer, "campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING_RECIPE, 600);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.STONE_SLAB, Blocks.STONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.STONE_STAIRS, Blocks.STONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.STONE_BRICKS, Blocks.STONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.STONE_BRICK_SLAB, Blocks.STONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.STONE_BRICK_STAIRS, Blocks.STONE);
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.CHISELED_STONE_BRICKS).unlockedBy("has_stone", RecipeProvider.has(Blocks.STONE)).save(consumer, "chiseled_stone_bricks_stone_from_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_BRICK_WALL).unlockedBy("has_stone", RecipeProvider.has(Blocks.STONE)).save(consumer, "stone_brick_walls_from_stone_stonecutting");
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CUT_SANDSTONE, Blocks.SANDSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.SANDSTONE_SLAB, Blocks.SANDSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CUT_SANDSTONE_SLAB, Blocks.SANDSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CUT_SANDSTONE_SLAB, Blocks.CUT_SANDSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.SANDSTONE_STAIRS, Blocks.SANDSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.SANDSTONE_WALL, Blocks.SANDSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CHISELED_SANDSTONE, Blocks.SANDSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CUT_RED_SANDSTONE, Blocks.RED_SANDSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.CUT_RED_SANDSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.RED_SANDSTONE_STAIRS, Blocks.RED_SANDSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.RED_SANDSTONE_WALL, Blocks.RED_SANDSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CHISELED_RED_SANDSTONE, Blocks.RED_SANDSTONE);
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_SLAB, 2).unlockedBy("has_quartz_block", RecipeProvider.has(Blocks.QUARTZ_BLOCK)).save(consumer, "quartz_slab_from_stonecutting");
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.QUARTZ_STAIRS, Blocks.QUARTZ_BLOCK);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.QUARTZ_PILLAR, Blocks.QUARTZ_BLOCK);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_BLOCK);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.COBBLESTONE_STAIRS, Blocks.COBBLESTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.COBBLESTONE_SLAB, Blocks.COBBLESTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.COBBLESTONE_WALL, Blocks.COBBLESTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.STONE_BRICK_SLAB, Blocks.STONE_BRICKS, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICKS);
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.STONE_BRICK_WALL).unlockedBy("has_stone_bricks", RecipeProvider.has(Blocks.STONE_BRICKS)).save(consumer, "stone_brick_wall_from_stone_bricks_stonecutting");
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CHISELED_STONE_BRICKS, Blocks.STONE_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.BRICK_SLAB, Blocks.BRICKS, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.BRICK_STAIRS, Blocks.BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.BRICK_WALL, Blocks.BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.NETHER_BRICK_SLAB, Blocks.NETHER_BRICKS, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.NETHER_BRICK_WALL, Blocks.NETHER_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CHISELED_NETHER_BRICKS, Blocks.NETHER_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.RED_NETHER_BRICK_SLAB, Blocks.RED_NETHER_BRICKS, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.RED_NETHER_BRICK_STAIRS, Blocks.RED_NETHER_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.RED_NETHER_BRICK_WALL, Blocks.RED_NETHER_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.PURPUR_SLAB, Blocks.PURPUR_BLOCK, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.PURPUR_STAIRS, Blocks.PURPUR_BLOCK);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.PURPUR_PILLAR, Blocks.PURPUR_BLOCK);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.PRISMARINE_SLAB, Blocks.PRISMARINE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.PRISMARINE_STAIRS, Blocks.PRISMARINE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.PRISMARINE_WALL, Blocks.PRISMARINE);
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE_BRICKS), Blocks.PRISMARINE_BRICK_SLAB, 2).unlockedBy("has_prismarine_brick", RecipeProvider.has(Blocks.PRISMARINE_BRICKS)).save(consumer, "prismarine_brick_slab_from_prismarine_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE_BRICKS), Blocks.PRISMARINE_BRICK_STAIRS).unlockedBy("has_prismarine_brick", RecipeProvider.has(Blocks.PRISMARINE_BRICKS)).save(consumer, "prismarine_brick_stairs_from_prismarine_stonecutting");
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DARK_PRISMARINE_SLAB, Blocks.DARK_PRISMARINE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DARK_PRISMARINE_STAIRS, Blocks.DARK_PRISMARINE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.ANDESITE_SLAB, Blocks.ANDESITE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.ANDESITE_STAIRS, Blocks.ANDESITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.ANDESITE_WALL, Blocks.ANDESITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_ANDESITE, Blocks.ANDESITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_ANDESITE_SLAB, Blocks.ANDESITE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_ANDESITE_STAIRS, Blocks.ANDESITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_ANDESITE_SLAB, Blocks.POLISHED_ANDESITE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_ANDESITE_STAIRS, Blocks.POLISHED_ANDESITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BASALT, Blocks.BASALT);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.GRANITE_SLAB, Blocks.GRANITE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.GRANITE_STAIRS, Blocks.GRANITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.GRANITE_WALL, Blocks.GRANITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_GRANITE, Blocks.GRANITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_GRANITE_SLAB, Blocks.GRANITE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_GRANITE_STAIRS, Blocks.GRANITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_GRANITE_SLAB, Blocks.POLISHED_GRANITE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_GRANITE_STAIRS, Blocks.POLISHED_GRANITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DIORITE_SLAB, Blocks.DIORITE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DIORITE_STAIRS, Blocks.DIORITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DIORITE_WALL, Blocks.DIORITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DIORITE, Blocks.DIORITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DIORITE_SLAB, Blocks.DIORITE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DIORITE_STAIRS, Blocks.DIORITE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DIORITE_SLAB, Blocks.POLISHED_DIORITE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DIORITE_STAIRS, Blocks.POLISHED_DIORITE);
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_SLAB, 2).unlockedBy("has_mossy_stone_bricks", RecipeProvider.has(Blocks.MOSSY_STONE_BRICKS)).save(consumer, "mossy_stone_brick_slab_from_mossy_stone_brick_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_STAIRS).unlockedBy("has_mossy_stone_bricks", RecipeProvider.has(Blocks.MOSSY_STONE_BRICKS)).save(consumer, "mossy_stone_brick_stairs_from_mossy_stone_brick_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_WALL).unlockedBy("has_mossy_stone_bricks", RecipeProvider.has(Blocks.MOSSY_STONE_BRICKS)).save(consumer, "mossy_stone_brick_wall_from_mossy_stone_brick_stonecutting");
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.MOSSY_COBBLESTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.MOSSY_COBBLESTONE_STAIRS, Blocks.MOSSY_COBBLESTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.MOSSY_COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.SMOOTH_SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.SMOOTH_SANDSTONE_STAIRS, Blocks.SMOOTH_SANDSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.SMOOTH_RED_SANDSTONE_STAIRS, Blocks.SMOOTH_RED_SANDSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.SMOOTH_QUARTZ_SLAB, Blocks.SMOOTH_QUARTZ, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.SMOOTH_QUARTZ_STAIRS, Blocks.SMOOTH_QUARTZ);
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_SLAB, 2).unlockedBy("has_end_stone_brick", RecipeProvider.has(Blocks.END_STONE_BRICKS)).save(consumer, "end_stone_brick_slab_from_end_stone_brick_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_STAIRS).unlockedBy("has_end_stone_brick", RecipeProvider.has(Blocks.END_STONE_BRICKS)).save(consumer, "end_stone_brick_stairs_from_end_stone_brick_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_WALL).unlockedBy("has_end_stone_brick", RecipeProvider.has(Blocks.END_STONE_BRICKS)).save(consumer, "end_stone_brick_wall_from_end_stone_brick_stonecutting");
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.END_STONE_BRICKS, Blocks.END_STONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.END_STONE_BRICK_SLAB, Blocks.END_STONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.END_STONE_BRICK_STAIRS, Blocks.END_STONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.END_STONE_BRICK_WALL, Blocks.END_STONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.SMOOTH_STONE_SLAB, Blocks.SMOOTH_STONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.BLACKSTONE_SLAB, Blocks.BLACKSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.BLACKSTONE_STAIRS, Blocks.BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.BLACKSTONE_WALL, Blocks.BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE, Blocks.BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_WALL, Blocks.BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.BLACKSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_STAIRS, Blocks.BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CHISELED_POLISHED_BLACKSTONE, Blocks.BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.BLACKSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_STAIRS, Blocks.POLISHED_BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_WALL, Blocks.POLISHED_BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CHISELED_POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICKS, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CUT_COPPER_SLAB, Blocks.CUT_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CUT_COPPER_STAIRS, Blocks.CUT_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_CUT_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CUT_COPPER, Blocks.COPPER_BLOCK);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CUT_COPPER_STAIRS, Blocks.COPPER_BLOCK);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CUT_COPPER_SLAB, Blocks.COPPER_BLOCK, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.EXPOSED_CUT_COPPER, Blocks.EXPOSED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.EXPOSED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.EXPOSED_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WEATHERED_CUT_COPPER, Blocks.WEATHERED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WEATHERED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WEATHERED_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.OXIDIZED_CUT_COPPER, Blocks.OXIDIZED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.OXIDIZED_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_CUT_COPPER, Blocks.WAXED_COPPER_BLOCK);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_COPPER_BLOCK);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_COPPER_BLOCK, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_COPPER);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_COPPER, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.COBBLED_DEEPSLATE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.COBBLED_DEEPSLATE_STAIRS, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.COBBLED_DEEPSLATE_WALL, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.CHISELED_DEEPSLATE, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DEEPSLATE, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DEEPSLATE_STAIRS, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DEEPSLATE_WALL, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_BRICKS, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_BRICK_WALL, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILES, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_WALL, Blocks.COBBLED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DEEPSLATE_STAIRS, Blocks.POLISHED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.POLISHED_DEEPSLATE_WALL, Blocks.POLISHED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_BRICKS, Blocks.POLISHED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.POLISHED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_BRICK_WALL, Blocks.POLISHED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILES, Blocks.POLISHED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.POLISHED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_WALL, Blocks.POLISHED_DEEPSLATE);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.DEEPSLATE_BRICKS, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.DEEPSLATE_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_BRICK_WALL, Blocks.DEEPSLATE_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILES, Blocks.DEEPSLATE_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_BRICKS, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_WALL, Blocks.DEEPSLATE_BRICKS);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_TILES, 2);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILES);
        RecipeProvider.stonecutterResultFromBase(consumer, Blocks.DEEPSLATE_TILE_WALL, Blocks.DEEPSLATE_TILES);
        RecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
        RecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
        RecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET);
        RecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);
        RecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
        RecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
        RecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
        RecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_HOE, Items.NETHERITE_HOE);
        RecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
    }

    private static void oneToOneConversionRecipe(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, @Nullable String string) {
        RecipeProvider.oneToOneConversionRecipe(consumer, itemLike, itemLike2, string, 1);
    }

    private static void oneToOneConversionRecipe(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, @Nullable String string, int i) {
        ShapelessRecipeBuilder.shapeless(itemLike, i).requires(itemLike2).group(string).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, RecipeProvider.getConversionRecipeName(itemLike, itemLike2));
    }

    private static void oreSmelting(Consumer<FinishedRecipe> consumer, List<ItemLike> list, ItemLike itemLike, float f, int i, String string) {
        RecipeProvider.oreCooking(consumer, RecipeSerializer.SMELTING_RECIPE, list, itemLike, f, i, string, "_from_smelting");
    }

    private static void oreBlasting(Consumer<FinishedRecipe> consumer, List<ItemLike> list, ItemLike itemLike, float f, int i, String string) {
        RecipeProvider.oreCooking(consumer, RecipeSerializer.BLASTING_RECIPE, list, itemLike, f, i, string, "_from_blasting");
    }

    private static void oreCooking(Consumer<FinishedRecipe> consumer, SimpleCookingSerializer<?> simpleCookingSerializer, List<ItemLike> list, ItemLike itemLike, float f, int i, String string, String string2) {
        for (ItemLike itemLike2 : list) {
            SimpleCookingRecipeBuilder.cooking(Ingredient.of(itemLike2), itemLike, f, i, simpleCookingSerializer).group(string).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, RecipeProvider.getItemName(itemLike) + string2 + "_" + RecipeProvider.getItemName(itemLike2));
        }
    }

    private static void netheriteSmithing(Consumer<FinishedRecipe> consumer, Item item, Item item2) {
        UpgradeRecipeBuilder.smithing(Ingredient.of(item), Ingredient.of(Items.NETHERITE_INGOT), item2).unlocks("has_netherite_ingot", RecipeProvider.has(Items.NETHERITE_INGOT)).save(consumer, RecipeProvider.getItemName(item2) + "_smithing");
    }

    private static void planksFromLog(Consumer<FinishedRecipe> consumer, ItemLike itemLike, Tag<Item> tag) {
        ShapelessRecipeBuilder.shapeless(itemLike, 4).requires(tag).group("planks").unlockedBy("has_log", RecipeProvider.has(tag)).save(consumer);
    }

    private static void planksFromLogs(Consumer<FinishedRecipe> consumer, ItemLike itemLike, Tag<Item> tag) {
        ShapelessRecipeBuilder.shapeless(itemLike, 4).requires(tag).group("planks").unlockedBy("has_logs", RecipeProvider.has(tag)).save(consumer);
    }

    private static void woodFromLogs(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(itemLike, 3).define(Character.valueOf('#'), itemLike2).pattern("##").pattern("##").group("bark").unlockedBy("has_log", RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static void woodenBoat(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(itemLike).define(Character.valueOf('#'), itemLike2).pattern("# #").pattern("###").group("boat").unlockedBy("in_water", RecipeProvider.insideOf(Blocks.WATER)).save(consumer);
    }

    private static RecipeBuilder buttonBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapelessRecipeBuilder.shapeless(itemLike).requires(ingredient);
    }

    private static RecipeBuilder doorBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(itemLike, 3).define(Character.valueOf('#'), ingredient).pattern("##").pattern("##").pattern("##");
    }

    private static RecipeBuilder fenceBuilder(ItemLike itemLike, Ingredient ingredient) {
        int i = itemLike == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
        Item item = itemLike == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
        return ShapedRecipeBuilder.shaped(itemLike, i).define(Character.valueOf('W'), ingredient).define(Character.valueOf('#'), item).pattern("W#W").pattern("W#W");
    }

    private static RecipeBuilder fenceGateBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(itemLike).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('W'), ingredient).pattern("#W#").pattern("#W#");
    }

    private static void pressurePlate(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.pressurePlateBuilder(itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static RecipeBuilder pressurePlateBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(itemLike).define(Character.valueOf('#'), ingredient).pattern("##");
    }

    private static void slab(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.slabBuilder(itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static RecipeBuilder slabBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(itemLike, 6).define(Character.valueOf('#'), ingredient).pattern("###");
    }

    private static RecipeBuilder stairBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(itemLike, 4).define(Character.valueOf('#'), ingredient).pattern("#  ").pattern("## ").pattern("###");
    }

    private static RecipeBuilder trapdoorBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(itemLike, 2).define(Character.valueOf('#'), ingredient).pattern("###").pattern("###");
    }

    private static RecipeBuilder signBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(itemLike, 3).group("sign").define(Character.valueOf('#'), ingredient).define(Character.valueOf('X'), Items.STICK).pattern("###").pattern("###").pattern(" X ");
    }

    private static void coloredWoolFromWhiteWoolAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapelessRecipeBuilder.shapeless(itemLike).requires(itemLike2).requires(Blocks.WHITE_WOOL).group("wool").unlockedBy("has_white_wool", RecipeProvider.has(Blocks.WHITE_WOOL)).save(consumer);
    }

    private static void carpet(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(itemLike, 3).define(Character.valueOf('#'), itemLike2).pattern("##").group("carpet").unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static void coloredCarpetFromWhiteCarpetAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(itemLike, 8).define(Character.valueOf('#'), Blocks.WHITE_CARPET).define(Character.valueOf('$'), itemLike2).pattern("###").pattern("#$#").pattern("###").group("carpet").unlockedBy("has_white_carpet", RecipeProvider.has(Blocks.WHITE_CARPET)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, RecipeProvider.getConversionRecipeName(itemLike, Blocks.WHITE_CARPET));
    }

    private static void bedFromPlanksAndWool(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(itemLike).define(Character.valueOf('#'), itemLike2).define(Character.valueOf('X'), ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static void bedFromWhiteBedAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapelessRecipeBuilder.shapeless(itemLike).requires(Items.WHITE_BED).requires(itemLike2).group("dyed_bed").unlockedBy("has_bed", RecipeProvider.has(Items.WHITE_BED)).save(consumer, RecipeProvider.getConversionRecipeName(itemLike, Items.WHITE_BED));
    }

    private static void banner(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(itemLike).define(Character.valueOf('#'), itemLike2).define(Character.valueOf('|'), Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static void stainedGlassFromGlassAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(itemLike, 8).define(Character.valueOf('#'), Blocks.GLASS).define(Character.valueOf('X'), itemLike2).pattern("###").pattern("#X#").pattern("###").group("stained_glass").unlockedBy("has_glass", RecipeProvider.has(Blocks.GLASS)).save(consumer);
    }

    private static void stainedGlassPaneFromStainedGlass(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(itemLike, 16).define(Character.valueOf('#'), itemLike2).pattern("###").pattern("###").group("stained_glass_pane").unlockedBy("has_glass", RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static void stainedGlassPaneFromGlassPaneAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(itemLike, 8).define(Character.valueOf('#'), Blocks.GLASS_PANE).define(Character.valueOf('$'), itemLike2).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").unlockedBy("has_glass_pane", RecipeProvider.has(Blocks.GLASS_PANE)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, RecipeProvider.getConversionRecipeName(itemLike, Blocks.GLASS_PANE));
    }

    private static void coloredTerracottaFromTerracottaAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapedRecipeBuilder.shaped(itemLike, 8).define(Character.valueOf('#'), Blocks.TERRACOTTA).define(Character.valueOf('X'), itemLike2).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").unlockedBy("has_terracotta", RecipeProvider.has(Blocks.TERRACOTTA)).save(consumer);
    }

    private static void concretePowder(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapelessRecipeBuilder.shapeless(itemLike, 8).requires(itemLike2).requires(Blocks.SAND, 4).requires(Blocks.GRAVEL, 4).group("concrete_powder").unlockedBy("has_sand", RecipeProvider.has(Blocks.SAND)).unlockedBy("has_gravel", RecipeProvider.has(Blocks.GRAVEL)).save(consumer);
    }

    public static void candle(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        ShapelessRecipeBuilder.shapeless(itemLike).requires(Blocks.CANDLE).requires(itemLike2).group("dyed_candle").unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    public static void wall(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.wallBuilder(itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    public static RecipeBuilder wallBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(itemLike, 6).define(Character.valueOf('#'), ingredient).pattern("###").pattern("###");
    }

    public static void polished(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.polishedBuilder(itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    public static RecipeBuilder polishedBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(itemLike, 4).define(Character.valueOf('S'), ingredient).pattern("SS").pattern("SS");
    }

    public static void cut(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.cutBuilder(itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    public static ShapedRecipeBuilder cutBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(itemLike, 4).define(Character.valueOf('#'), ingredient).pattern("##").pattern("##");
    }

    public static void chiseled(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.chiseledBuilder(itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    public static ShapedRecipeBuilder chiseledBuilder(ItemLike itemLike, Ingredient ingredient) {
        return ShapedRecipeBuilder.shaped(itemLike).define(Character.valueOf('#'), ingredient).pattern("#").pattern("#");
    }

    private static void stonecutterResultFromBase(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.stonecutterResultFromBase(consumer, itemLike, itemLike2, 1);
    }

    private static void stonecutterResultFromBase(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, int i) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(itemLike2), itemLike, i).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, RecipeProvider.getConversionRecipeName(itemLike, itemLike2) + "_stonecutting");
    }

    private static void smeltingResultFromBase(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(itemLike2), itemLike, 0.1f, 200).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer);
    }

    private static void nineBlockStorageRecipes(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        RecipeProvider.nineBlockStorageRecipes(consumer, itemLike, itemLike2, RecipeProvider.getSimpleRecipeName(itemLike2), null, RecipeProvider.getSimpleRecipeName(itemLike), null);
    }

    private static void nineBlockStorageRecipesWithCustomPacking(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, String string, String string2) {
        RecipeProvider.nineBlockStorageRecipes(consumer, itemLike, itemLike2, string, string2, RecipeProvider.getSimpleRecipeName(itemLike), null);
    }

    private static void nineBlockStorageRecipesRecipesWithCustomUnpacking(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, String string, String string2) {
        RecipeProvider.nineBlockStorageRecipes(consumer, itemLike, itemLike2, RecipeProvider.getSimpleRecipeName(itemLike2), null, string, string2);
    }

    private static void nineBlockStorageRecipes(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, String string, @Nullable String string2, String string3, @Nullable String string4) {
        ShapelessRecipeBuilder.shapeless(itemLike, 9).requires(itemLike2).group(string4).unlockedBy(RecipeProvider.getHasName(itemLike2), RecipeProvider.has(itemLike2)).save(consumer, new ResourceLocation(string3));
        ShapedRecipeBuilder.shaped(itemLike2).define(Character.valueOf('#'), itemLike).pattern("###").pattern("###").pattern("###").group(string2).unlockedBy(RecipeProvider.getHasName(itemLike), RecipeProvider.has(itemLike)).save(consumer, new ResourceLocation(string));
    }

    private static void cookRecipes(Consumer<FinishedRecipe> consumer, String string, SimpleCookingSerializer<?> simpleCookingSerializer, int i) {
        RecipeProvider.simpleCookingRecipe(consumer, string, simpleCookingSerializer, i, Items.BEEF, Items.COOKED_BEEF, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, simpleCookingSerializer, i, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, simpleCookingSerializer, i, Items.COD, Items.COOKED_COD, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, simpleCookingSerializer, i, Items.KELP, Items.DRIED_KELP, 0.1f);
        RecipeProvider.simpleCookingRecipe(consumer, string, simpleCookingSerializer, i, Items.SALMON, Items.COOKED_SALMON, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, simpleCookingSerializer, i, Items.MUTTON, Items.COOKED_MUTTON, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, simpleCookingSerializer, i, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, simpleCookingSerializer, i, Items.POTATO, Items.BAKED_POTATO, 0.35f);
        RecipeProvider.simpleCookingRecipe(consumer, string, simpleCookingSerializer, i, Items.RABBIT, Items.COOKED_RABBIT, 0.35f);
    }

    private static void simpleCookingRecipe(Consumer<FinishedRecipe> consumer, String string, SimpleCookingSerializer<?> simpleCookingSerializer, int i, ItemLike itemLike, ItemLike itemLike2, float f) {
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(itemLike), itemLike2, f, i, simpleCookingSerializer).unlockedBy(RecipeProvider.getHasName(itemLike), RecipeProvider.has(itemLike)).save(consumer, RecipeProvider.getItemName(itemLike2) + "_from_" + string);
    }

    private static void waxRecipes(Consumer<FinishedRecipe> consumer) {
        HoneycombItem.WAXABLES.get().forEach((block, block2) -> ShapelessRecipeBuilder.shapeless(block2).requires((ItemLike)block).requires(Items.HONEYCOMB).group(RecipeProvider.getItemName(block2)).unlockedBy(RecipeProvider.getHasName(block), RecipeProvider.has(block)).save(consumer, RecipeProvider.getConversionRecipeName(block2, Items.HONEYCOMB)));
    }

    private static void generateRecipes(Consumer<FinishedRecipe> consumer, BlockFamily blockFamily) {
        blockFamily.getVariants().forEach((variant, block) -> {
            BiFunction<ItemLike, ItemLike, RecipeBuilder> biFunction = shapeBuilders.get(variant);
            Block itemLike = RecipeProvider.getBaseBlock(blockFamily, variant);
            if (biFunction != null) {
                RecipeBuilder recipeBuilder = biFunction.apply((ItemLike)block, itemLike);
                blockFamily.getRecipeGroupPrefix().ifPresent(string -> recipeBuilder.group(string + (variant == BlockFamily.Variant.CUT ? "" : "_" + variant.getName())));
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

    private static InventoryChangeTrigger.TriggerInstance has(ItemLike itemLike) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(itemLike).build());
    }

    private static InventoryChangeTrigger.TriggerInstance has(Tag<Item> tag) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(tag).build());
    }

    private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate ... itemPredicates) {
        return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, itemPredicates);
    }

    private static String getHasName(ItemLike itemLike) {
        return "has_" + RecipeProvider.getItemName(itemLike);
    }

    private static String getItemName(ItemLike itemLike) {
        return Registry.ITEM.getKey(itemLike.asItem()).getPath();
    }

    private static String getSimpleRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike);
    }

    private static String getConversionRecipeName(ItemLike itemLike, ItemLike itemLike2) {
        return RecipeProvider.getItemName(itemLike) + "_from_" + RecipeProvider.getItemName(itemLike2);
    }

    private static String getSmeltingRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike) + "_from_smelting";
    }

    private static String getBlastingRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike) + "_from_blasting";
    }

    @Override
    public String getName() {
        return "Recipes";
    }
}

