/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.recipes.packs;

import java.util.function.Consumer;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyRecipeProvider
extends RecipeProvider {
    public UpdateOneTwentyRecipeProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        UpdateOneTwentyRecipeProvider.generateForEnabledBlockFamilies(consumer, FeatureFlagSet.of(FeatureFlags.UPDATE_1_20));
        UpdateOneTwentyRecipeProvider.threeByThreePacker(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.BAMBOO_BLOCK, Items.BAMBOO);
        UpdateOneTwentyRecipeProvider.planksFromLogs(consumer, Blocks.BAMBOO_PLANKS, ItemTags.BAMBOO_BLOCKS, 2);
        UpdateOneTwentyRecipeProvider.mosaicBuilder(consumer, RecipeCategory.DECORATIONS, Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_SLAB);
        UpdateOneTwentyRecipeProvider.woodenBoat(consumer, Items.BAMBOO_RAFT, Blocks.BAMBOO_PLANKS);
        UpdateOneTwentyRecipeProvider.chestBoat(consumer, Items.BAMBOO_CHEST_RAFT, Items.BAMBOO_RAFT);
        UpdateOneTwentyRecipeProvider.hangingSign(consumer, Items.OAK_HANGING_SIGN, Blocks.STRIPPED_OAK_LOG);
        UpdateOneTwentyRecipeProvider.hangingSign(consumer, Items.SPRUCE_HANGING_SIGN, Blocks.STRIPPED_SPRUCE_LOG);
        UpdateOneTwentyRecipeProvider.hangingSign(consumer, Items.BIRCH_HANGING_SIGN, Blocks.STRIPPED_BIRCH_LOG);
        UpdateOneTwentyRecipeProvider.hangingSign(consumer, Items.JUNGLE_HANGING_SIGN, Blocks.STRIPPED_JUNGLE_LOG);
        UpdateOneTwentyRecipeProvider.hangingSign(consumer, Items.ACACIA_HANGING_SIGN, Blocks.STRIPPED_ACACIA_LOG);
        UpdateOneTwentyRecipeProvider.hangingSign(consumer, Items.CHERRY_HANGING_SIGN, Blocks.STRIPPED_CHERRY_LOG);
        UpdateOneTwentyRecipeProvider.hangingSign(consumer, Items.DARK_OAK_HANGING_SIGN, Blocks.STRIPPED_DARK_OAK_LOG);
        UpdateOneTwentyRecipeProvider.hangingSign(consumer, Items.MANGROVE_HANGING_SIGN, Blocks.STRIPPED_MANGROVE_LOG);
        UpdateOneTwentyRecipeProvider.hangingSign(consumer, Items.BAMBOO_HANGING_SIGN, Items.STRIPPED_BAMBOO_BLOCK);
        UpdateOneTwentyRecipeProvider.hangingSign(consumer, Items.CRIMSON_HANGING_SIGN, Blocks.STRIPPED_CRIMSON_STEM);
        UpdateOneTwentyRecipeProvider.hangingSign(consumer, Items.WARPED_HANGING_SIGN, Blocks.STRIPPED_WARPED_STEM);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_BOOKSHELF).define(Character.valueOf('#'), ItemTags.PLANKS).define(Character.valueOf('X'), ItemTags.WOODEN_SLABS).pattern("###").pattern("XXX").pattern("###").unlockedBy("has_book", UpdateOneTwentyRecipeProvider.has(Items.BOOK)).save(consumer);
        UpdateOneTwentyRecipeProvider.trimSmithing(consumer, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);
        UpdateOneTwentyRecipeProvider.trimSmithing(consumer, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE);
        UpdateOneTwentyRecipeProvider.trimSmithing(consumer, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE);
        UpdateOneTwentyRecipeProvider.trimSmithing(consumer, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE);
        UpdateOneTwentyRecipeProvider.trimSmithing(consumer, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE);
        UpdateOneTwentyRecipeProvider.trimSmithing(consumer, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE);
        UpdateOneTwentyRecipeProvider.trimSmithing(consumer, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);
        UpdateOneTwentyRecipeProvider.trimSmithing(consumer, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE);
        UpdateOneTwentyRecipeProvider.trimSmithing(consumer, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE);
        UpdateOneTwentyRecipeProvider.trimSmithing(consumer, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);
        UpdateOneTwentyRecipeProvider.trimSmithing(consumer, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE);
        UpdateOneTwentyRecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_CHESTPLATE, RecipeCategory.COMBAT, Items.NETHERITE_CHESTPLATE);
        UpdateOneTwentyRecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_LEGGINGS, RecipeCategory.COMBAT, Items.NETHERITE_LEGGINGS);
        UpdateOneTwentyRecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_HELMET, RecipeCategory.COMBAT, Items.NETHERITE_HELMET);
        UpdateOneTwentyRecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_BOOTS, RecipeCategory.COMBAT, Items.NETHERITE_BOOTS);
        UpdateOneTwentyRecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_SWORD, RecipeCategory.COMBAT, Items.NETHERITE_SWORD);
        UpdateOneTwentyRecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_AXE, RecipeCategory.TOOLS, Items.NETHERITE_AXE);
        UpdateOneTwentyRecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_PICKAXE, RecipeCategory.TOOLS, Items.NETHERITE_PICKAXE);
        UpdateOneTwentyRecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_HOE, RecipeCategory.TOOLS, Items.NETHERITE_HOE);
        UpdateOneTwentyRecipeProvider.netheriteSmithing(consumer, Items.DIAMOND_SHOVEL, RecipeCategory.TOOLS, Items.NETHERITE_SHOVEL);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.NETHERRACK);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SANDSTONE);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.MOSSY_COBBLESTONE);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLED_DEEPSLATE);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.END_STONE);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.PRISMARINE);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Items.BLACKSTONE);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.NETHERRACK);
        UpdateOneTwentyRecipeProvider.copySmithingTemplate(consumer, (ItemLike)Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.PURPUR_BLOCK);
        UpdateOneTwentyRecipeProvider.oneToOneConversionRecipe(consumer, Items.ORANGE_DYE, Blocks.TORCHFLOWER, "orange_dye");
        UpdateOneTwentyRecipeProvider.planksFromLog(consumer, Blocks.CHERRY_PLANKS, ItemTags.CHERRY_LOGS, 4);
        UpdateOneTwentyRecipeProvider.woodFromLogs(consumer, Blocks.CHERRY_WOOD, Blocks.CHERRY_LOG);
        UpdateOneTwentyRecipeProvider.woodFromLogs(consumer, Blocks.STRIPPED_CHERRY_WOOD, Blocks.STRIPPED_CHERRY_LOG);
        UpdateOneTwentyRecipeProvider.woodenBoat(consumer, Items.CHERRY_BOAT, Blocks.CHERRY_PLANKS);
        UpdateOneTwentyRecipeProvider.chestBoat(consumer, Items.CHERRY_CHEST_BOAT, Items.CHERRY_BOAT);
        UpdateOneTwentyRecipeProvider.oneToOneConversionRecipe(consumer, Items.PINK_DYE, Items.PINK_PETALS, "pink_dye", 1);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.BRUSH).define(Character.valueOf('X'), Items.FEATHER).define(Character.valueOf('#'), Items.COPPER_INGOT).define(Character.valueOf('I'), Items.STICK).pattern("X").pattern("#").pattern("I").unlockedBy("has_copper_ingot", UpdateOneTwentyRecipeProvider.has(Items.COPPER_INGOT)).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.DECORATED_POT).define(Character.valueOf('#'), Items.BRICK).pattern(" # ").pattern("# #").pattern(" # ").unlockedBy("has_brick", UpdateOneTwentyRecipeProvider.has(ItemTags.DECORATED_POT_SHARDS)).save(consumer, "decorated_pot_simple");
        SpecialRecipeBuilder.special(RecipeSerializer.DECORATED_POT_RECIPE).save(consumer, "decorated_pot");
    }
}

