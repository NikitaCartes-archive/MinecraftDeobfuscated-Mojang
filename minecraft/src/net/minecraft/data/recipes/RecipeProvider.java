package net.minecraft.data.recipes;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
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

public class RecipeProvider implements DataProvider {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final DataGenerator generator;

	public RecipeProvider(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(HashCache hashCache) throws IOException {
		Path path = this.generator.getOutputFolder();
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();
		this.buildShapelessRecipes(
			finishedRecipe -> {
				if (!set.add(finishedRecipe.getId())) {
					throw new IllegalStateException("Duplicate recipe " + finishedRecipe.getId());
				} else {
					this.saveRecipe(
						hashCache,
						finishedRecipe.serializeRecipe(),
						path.resolve("data/" + finishedRecipe.getId().getNamespace() + "/recipes/" + finishedRecipe.getId().getPath() + ".json")
					);
					JsonObject jsonObject = finishedRecipe.serializeAdvancement();
					if (jsonObject != null) {
						this.saveAdvancement(
							hashCache,
							jsonObject,
							path.resolve("data/" + finishedRecipe.getId().getNamespace() + "/advancements/" + finishedRecipe.getAdvancementId().getPath() + ".json")
						);
					}
				}
			}
		);
		this.saveAdvancement(
			hashCache,
			Advancement.Builder.advancement().addCriterion("impossible", new ImpossibleTrigger.TriggerInstance()).serializeToJson(),
			path.resolve("data/minecraft/advancements/recipes/root.json")
		);
	}

	private void saveRecipe(HashCache hashCache, JsonObject jsonObject, Path path) {
		try {
			String string = GSON.toJson((JsonElement)jsonObject);
			String string2 = SHA1.hashUnencodedChars(string).toString();
			if (!Objects.equals(hashCache.getHash(path), string2) || !Files.exists(path, new LinkOption[0])) {
				Files.createDirectories(path.getParent());
				BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
				Throwable var7 = null;

				try {
					bufferedWriter.write(string);
				} catch (Throwable var17) {
					var7 = var17;
					throw var17;
				} finally {
					if (bufferedWriter != null) {
						if (var7 != null) {
							try {
								bufferedWriter.close();
							} catch (Throwable var16) {
								var7.addSuppressed(var16);
							}
						} else {
							bufferedWriter.close();
						}
					}
				}
			}

			hashCache.putNew(path, string2);
		} catch (IOException var19) {
			LOGGER.error("Couldn't save recipe {}", path, var19);
		}
	}

	private void saveAdvancement(HashCache hashCache, JsonObject jsonObject, Path path) {
		try {
			String string = GSON.toJson((JsonElement)jsonObject);
			String string2 = SHA1.hashUnencodedChars(string).toString();
			if (!Objects.equals(hashCache.getHash(path), string2) || !Files.exists(path, new LinkOption[0])) {
				Files.createDirectories(path.getParent());
				BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
				Throwable var7 = null;

				try {
					bufferedWriter.write(string);
				} catch (Throwable var17) {
					var7 = var17;
					throw var17;
				} finally {
					if (bufferedWriter != null) {
						if (var7 != null) {
							try {
								bufferedWriter.close();
							} catch (Throwable var16) {
								var7.addSuppressed(var16);
							}
						} else {
							bufferedWriter.close();
						}
					}
				}
			}

			hashCache.putNew(path, string2);
		} catch (IOException var19) {
			LOGGER.error("Couldn't save recipe advancement {}", path, var19);
		}
	}

	private void buildShapelessRecipes(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(Blocks.ACACIA_WOOD, 3)
			.define('#', Blocks.ACACIA_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.ACACIA_LOG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STRIPPED_ACACIA_WOOD, 3)
			.define('#', Blocks.STRIPPED_ACACIA_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.STRIPPED_ACACIA_LOG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.ACACIA_BOAT)
			.define('#', Blocks.ACACIA_PLANKS)
			.pattern("# #")
			.pattern("###")
			.group("boat")
			.unlocks("in_water", this.insideOf(Blocks.WATER))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.ACACIA_BUTTON)
			.requires(Blocks.ACACIA_PLANKS)
			.group("wooden_button")
			.unlocks("has_planks", this.has(Blocks.ACACIA_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ACACIA_DOOR, 3)
			.define('#', Blocks.ACACIA_PLANKS)
			.pattern("##")
			.pattern("##")
			.pattern("##")
			.group("wooden_door")
			.unlocks("has_planks", this.has(Blocks.ACACIA_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ACACIA_FENCE, 3)
			.define('#', Items.STICK)
			.define('W', Blocks.ACACIA_PLANKS)
			.pattern("W#W")
			.pattern("W#W")
			.group("wooden_fence")
			.unlocks("has_planks", this.has(Blocks.ACACIA_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ACACIA_FENCE_GATE)
			.define('#', Items.STICK)
			.define('W', Blocks.ACACIA_PLANKS)
			.pattern("#W#")
			.pattern("#W#")
			.group("wooden_fence_gate")
			.unlocks("has_planks", this.has(Blocks.ACACIA_PLANKS))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.ACACIA_PLANKS, 4)
			.requires(ItemTags.ACACIA_LOGS)
			.group("planks")
			.unlocks("has_logs", this.has(ItemTags.ACACIA_LOGS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ACACIA_PRESSURE_PLATE)
			.define('#', Blocks.ACACIA_PLANKS)
			.pattern("##")
			.group("wooden_pressure_plate")
			.unlocks("has_planks", this.has(Blocks.ACACIA_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ACACIA_SLAB, 6)
			.define('#', Blocks.ACACIA_PLANKS)
			.pattern("###")
			.group("wooden_slab")
			.unlocks("has_planks", this.has(Blocks.ACACIA_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ACACIA_STAIRS, 4)
			.define('#', Blocks.ACACIA_PLANKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.group("wooden_stairs")
			.unlocks("has_planks", this.has(Blocks.ACACIA_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ACACIA_TRAPDOOR, 2)
			.define('#', Blocks.ACACIA_PLANKS)
			.pattern("###")
			.pattern("###")
			.group("wooden_trapdoor")
			.unlocks("has_planks", this.has(Blocks.ACACIA_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ACTIVATOR_RAIL, 6)
			.define('#', Blocks.REDSTONE_TORCH)
			.define('S', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("XSX")
			.pattern("X#X")
			.pattern("XSX")
			.unlocks("has_rail", this.has(Blocks.RAIL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.ANDESITE, 2)
			.requires(Blocks.DIORITE)
			.requires(Blocks.COBBLESTONE)
			.unlocks("has_stone", this.has(Blocks.DIORITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ANVIL)
			.define('I', Blocks.IRON_BLOCK)
			.define('i', Items.IRON_INGOT)
			.pattern("III")
			.pattern(" i ")
			.pattern("iii")
			.unlocks("has_iron_block", this.has(Blocks.IRON_BLOCK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.ARMOR_STAND)
			.define('/', Items.STICK)
			.define('_', Blocks.SMOOTH_STONE_SLAB)
			.pattern("///")
			.pattern(" / ")
			.pattern("/_/")
			.unlocks("has_stone_slab", this.has(Blocks.SMOOTH_STONE_SLAB))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.ARROW, 4)
			.define('#', Items.STICK)
			.define('X', Items.FLINT)
			.define('Y', Items.FEATHER)
			.pattern("X")
			.pattern("#")
			.pattern("Y")
			.unlocks("has_feather", this.has(Items.FEATHER))
			.unlocks("has_flint", this.has(Items.FLINT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BARREL, 1)
			.define('P', ItemTags.PLANKS)
			.define('S', ItemTags.WOODEN_SLABS)
			.pattern("PSP")
			.pattern("P P")
			.pattern("PSP")
			.unlocks("has_planks", this.has(ItemTags.PLANKS))
			.unlocks("has_wood_slab", this.has(ItemTags.WOODEN_SLABS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BEACON)
			.define('S', Items.NETHER_STAR)
			.define('G', Blocks.GLASS)
			.define('O', Blocks.OBSIDIAN)
			.pattern("GGG")
			.pattern("GSG")
			.pattern("OOO")
			.unlocks("has_nether_star", this.has(Items.NETHER_STAR))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BEEHIVE)
			.define('P', ItemTags.PLANKS)
			.define('H', Items.HONEYCOMB)
			.pattern("PPP")
			.pattern("HHH")
			.pattern("PPP")
			.unlocks("has_honeycomb", this.has(Items.HONEYCOMB))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BEETROOT_SOUP)
			.requires(Items.BOWL)
			.requires(Items.BEETROOT, 6)
			.unlocks("has_beetroot", this.has(Items.BEETROOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BIRCH_WOOD, 3)
			.define('#', Blocks.BIRCH_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.BIRCH_LOG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STRIPPED_BIRCH_WOOD, 3)
			.define('#', Blocks.STRIPPED_BIRCH_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.STRIPPED_BIRCH_LOG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.BIRCH_BOAT)
			.define('#', Blocks.BIRCH_PLANKS)
			.pattern("# #")
			.pattern("###")
			.group("boat")
			.unlocks("in_water", this.insideOf(Blocks.WATER))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.BIRCH_BUTTON)
			.requires(Blocks.BIRCH_PLANKS)
			.group("wooden_button")
			.unlocks("has_planks", this.has(Blocks.BIRCH_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BIRCH_DOOR, 3)
			.define('#', Blocks.BIRCH_PLANKS)
			.pattern("##")
			.pattern("##")
			.pattern("##")
			.group("wooden_door")
			.unlocks("has_planks", this.has(Blocks.BIRCH_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BIRCH_FENCE, 3)
			.define('#', Items.STICK)
			.define('W', Blocks.BIRCH_PLANKS)
			.pattern("W#W")
			.pattern("W#W")
			.group("wooden_fence")
			.unlocks("has_planks", this.has(Blocks.BIRCH_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BIRCH_FENCE_GATE)
			.define('#', Items.STICK)
			.define('W', Blocks.BIRCH_PLANKS)
			.pattern("#W#")
			.pattern("#W#")
			.group("wooden_fence_gate")
			.unlocks("has_planks", this.has(Blocks.BIRCH_PLANKS))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.BIRCH_PLANKS, 4)
			.requires(ItemTags.BIRCH_LOGS)
			.group("planks")
			.unlocks("has_log", this.has(ItemTags.BIRCH_LOGS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BIRCH_PRESSURE_PLATE)
			.define('#', Blocks.BIRCH_PLANKS)
			.pattern("##")
			.group("wooden_pressure_plate")
			.unlocks("has_planks", this.has(Blocks.BIRCH_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BIRCH_SLAB, 6)
			.define('#', Blocks.BIRCH_PLANKS)
			.pattern("###")
			.group("wooden_slab")
			.unlocks("has_planks", this.has(Blocks.BIRCH_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BIRCH_STAIRS, 4)
			.define('#', Blocks.BIRCH_PLANKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.group("wooden_stairs")
			.unlocks("has_planks", this.has(Blocks.BIRCH_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BIRCH_TRAPDOOR, 2)
			.define('#', Blocks.BIRCH_PLANKS)
			.pattern("###")
			.pattern("###")
			.group("wooden_trapdoor")
			.unlocks("has_planks", this.has(Blocks.BIRCH_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.BLACK_BANNER)
			.define('#', Blocks.BLACK_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_black_wool", this.has(Blocks.BLACK_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.BLACK_BED)
			.define('#', Blocks.BLACK_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_black_wool", this.has(Blocks.BLACK_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BLACK_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.BLACK_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "black_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.BLACK_CARPET, 3)
			.define('#', Blocks.BLACK_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_black_wool", this.has(Blocks.BLACK_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BLACK_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.BLACK_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_black_dye", this.has(Items.BLACK_DYE))
			.save(consumer, "black_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.BLACK_CONCRETE_POWDER, 8)
			.requires(Items.BLACK_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BLACK_DYE).requires(Items.INK_SAC).group("black_dye").unlocks("has_ink_sac", this.has(Items.INK_SAC)).save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BLACK_DYE)
			.requires(Blocks.WITHER_ROSE)
			.group("black_dye")
			.unlocks("has_black_flower", this.has(Blocks.WITHER_ROSE))
			.save(consumer, "black_dye_from_wither_rose");
		ShapedRecipeBuilder.shaped(Blocks.BLACK_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.BLACK_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BLACK_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.BLACK_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BLACK_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.BLACK_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_black_dye", this.has(Items.BLACK_DYE))
			.save(consumer, "black_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.BLACK_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.BLACK_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.BLACK_WOOL)
			.requires(Items.BLACK_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BLAZE_POWDER, 2).requires(Items.BLAZE_ROD).unlocks("has_blaze_rod", this.has(Items.BLAZE_ROD)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.BLUE_BANNER)
			.define('#', Blocks.BLUE_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_blue_wool", this.has(Blocks.BLUE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.BLUE_BED)
			.define('#', Blocks.BLUE_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_blue_wool", this.has(Blocks.BLUE_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BLUE_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.BLUE_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "blue_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.BLUE_CARPET, 3)
			.define('#', Blocks.BLUE_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_blue_wool", this.has(Blocks.BLUE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BLUE_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.BLUE_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_blue_dye", this.has(Items.BLUE_DYE))
			.save(consumer, "blue_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.BLUE_CONCRETE_POWDER, 8)
			.requires(Items.BLUE_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BLUE_DYE)
			.requires(Items.LAPIS_LAZULI)
			.group("blue_dye")
			.unlocks("has_lapis_lazuli", this.has(Items.LAPIS_LAZULI))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BLUE_DYE)
			.requires(Blocks.CORNFLOWER)
			.group("blue_dye")
			.unlocks("has_blue_flower", this.has(Blocks.CORNFLOWER))
			.save(consumer, "blue_dye_from_cornflower");
		ShapedRecipeBuilder.shaped(Blocks.BLUE_ICE)
			.define('#', Blocks.PACKED_ICE)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.unlocks("has_packed_ice", this.has(Blocks.PACKED_ICE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BLUE_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.BLUE_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BLUE_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.BLUE_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BLUE_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.BLUE_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_blue_dye", this.has(Items.BLUE_DYE))
			.save(consumer, "blue_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.BLUE_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.BLUE_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.BLUE_WOOL)
			.requires(Items.BLUE_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.OAK_BOAT)
			.define('#', Blocks.OAK_PLANKS)
			.pattern("# #")
			.pattern("###")
			.group("boat")
			.unlocks("in_water", this.insideOf(Blocks.WATER))
			.save(consumer);
		Item item = Items.BONE_MEAL;
		ShapedRecipeBuilder.shaped(Blocks.BONE_BLOCK)
			.define('X', Items.BONE_MEAL)
			.pattern("XXX")
			.pattern("XXX")
			.pattern("XXX")
			.unlocks("has_bonemeal", this.has(item))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BONE_MEAL, 3).requires(Items.BONE).group("bonemeal").unlocks("has_bone", this.has(Items.BONE)).save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BONE_MEAL, 9)
			.requires(Blocks.BONE_BLOCK)
			.group("bonemeal")
			.unlocks("has_bone_block", this.has(Blocks.BONE_BLOCK))
			.save(consumer, "bone_meal_from_bone_block");
		ShapelessRecipeBuilder.shapeless(Items.BOOK).requires(Items.PAPER, 3).requires(Items.LEATHER).unlocks("has_paper", this.has(Items.PAPER)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BOOKSHELF)
			.define('#', ItemTags.PLANKS)
			.define('X', Items.BOOK)
			.pattern("###")
			.pattern("XXX")
			.pattern("###")
			.unlocks("has_book", this.has(Items.BOOK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.BOW)
			.define('#', Items.STICK)
			.define('X', Items.STRING)
			.pattern(" #X")
			.pattern("# X")
			.pattern(" #X")
			.unlocks("has_string", this.has(Items.STRING))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.BOWL, 4)
			.define('#', ItemTags.PLANKS)
			.pattern("# #")
			.pattern(" # ")
			.unlocks("has_brown_mushroom", this.has(Blocks.BROWN_MUSHROOM))
			.unlocks("has_red_mushroom", this.has(Blocks.RED_MUSHROOM))
			.unlocks("has_mushroom_stew", this.has(Items.MUSHROOM_STEW))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.BREAD).define('#', Items.WHEAT).pattern("###").unlocks("has_wheat", this.has(Items.WHEAT)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BREWING_STAND)
			.define('B', Items.BLAZE_ROD)
			.define('#', Blocks.COBBLESTONE)
			.pattern(" B ")
			.pattern("###")
			.unlocks("has_blaze_rod", this.has(Items.BLAZE_ROD))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BRICKS).define('#', Items.BRICK).pattern("##").pattern("##").unlocks("has_brick", this.has(Items.BRICK)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BRICK_SLAB, 6).define('#', Blocks.BRICKS).pattern("###").unlocks("has_brick_block", this.has(Blocks.BRICKS)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BRICK_STAIRS, 4)
			.define('#', Blocks.BRICKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_brick_block", this.has(Blocks.BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.BROWN_BANNER)
			.define('#', Blocks.BROWN_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_brown_wool", this.has(Blocks.BROWN_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.BROWN_BED)
			.define('#', Blocks.BROWN_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_brown_wool", this.has(Blocks.BROWN_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BROWN_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.BROWN_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "brown_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.BROWN_CARPET, 3)
			.define('#', Blocks.BROWN_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_brown_wool", this.has(Blocks.BROWN_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BROWN_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.BROWN_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_brown_dye", this.has(Items.BROWN_DYE))
			.save(consumer, "brown_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.BROWN_CONCRETE_POWDER, 8)
			.requires(Items.BROWN_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.BROWN_DYE)
			.requires(Items.COCOA_BEANS)
			.group("brown_dye")
			.unlocks("has_cocoa_beans", this.has(Items.COCOA_BEANS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BROWN_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.BROWN_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BROWN_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.BROWN_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BROWN_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.BROWN_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_brown_dye", this.has(Items.BROWN_DYE))
			.save(consumer, "brown_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.BROWN_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.BROWN_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.BROWN_WOOL)
			.requires(Items.BROWN_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.BUCKET)
			.define('#', Items.IRON_INGOT)
			.pattern("# #")
			.pattern(" # ")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CAKE)
			.define('A', Items.MILK_BUCKET)
			.define('B', Items.SUGAR)
			.define('C', Items.WHEAT)
			.define('E', Items.EGG)
			.pattern("AAA")
			.pattern("BEB")
			.pattern("CCC")
			.unlocks("has_egg", this.has(Items.EGG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CAMPFIRE)
			.define('L', ItemTags.LOGS)
			.define('S', Items.STICK)
			.define('C', ItemTags.COALS)
			.pattern(" S ")
			.pattern("SCS")
			.pattern("LLL")
			.unlocks("has_stick", this.has(Items.STICK))
			.unlocks("has_coal", this.has(ItemTags.COALS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.CARROT_ON_A_STICK)
			.define('#', Items.FISHING_ROD)
			.define('X', Items.CARROT)
			.pattern("# ")
			.pattern(" X")
			.unlocks("has_carrot", this.has(Items.CARROT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CAULDRON)
			.define('#', Items.IRON_INGOT)
			.pattern("# #")
			.pattern("# #")
			.pattern("###")
			.unlocks("has_water_bucket", this.has(Items.WATER_BUCKET))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.COMPOSTER)
			.define('#', ItemTags.WOODEN_SLABS)
			.pattern("# #")
			.pattern("# #")
			.pattern("###")
			.unlocks("has_wood_slab", this.has(ItemTags.WOODEN_SLABS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CHEST)
			.define('#', ItemTags.PLANKS)
			.pattern("###")
			.pattern("# #")
			.pattern("###")
			.unlocks(
				"has_lots_of_items",
				new InventoryChangeTrigger.TriggerInstance(MinMaxBounds.Ints.atLeast(10), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, new ItemPredicate[0])
			)
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.CHEST_MINECART)
			.define('A', Blocks.CHEST)
			.define('B', Items.MINECART)
			.pattern("A")
			.pattern("B")
			.unlocks("has_minecart", this.has(Items.MINECART))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CHISELED_QUARTZ_BLOCK)
			.define('#', Blocks.QUARTZ_SLAB)
			.pattern("#")
			.pattern("#")
			.unlocks("has_chiseled_quartz_block", this.has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlocks("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.unlocks("has_quartz_pillar", this.has(Blocks.QUARTZ_PILLAR))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CHISELED_STONE_BRICKS)
			.define('#', Blocks.STONE_BRICK_SLAB)
			.pattern("#")
			.pattern("#")
			.unlocks("has_stone_bricks", this.has(ItemTags.STONE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CLAY)
			.define('#', Items.CLAY_BALL)
			.pattern("##")
			.pattern("##")
			.unlocks("has_clay_ball", this.has(Items.CLAY_BALL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.CLOCK)
			.define('#', Items.GOLD_INGOT)
			.define('X', Items.REDSTONE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlocks("has_redstone", this.has(Items.REDSTONE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.COAL, 9).requires(Blocks.COAL_BLOCK).unlocks("has_coal_block", this.has(Blocks.COAL_BLOCK)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.COAL_BLOCK)
			.define('#', Items.COAL)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.unlocks("has_coal", this.has(Items.COAL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.COARSE_DIRT, 4)
			.define('D', Blocks.DIRT)
			.define('G', Blocks.GRAVEL)
			.pattern("DG")
			.pattern("GD")
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.COBBLESTONE_SLAB, 6)
			.define('#', Blocks.COBBLESTONE)
			.pattern("###")
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.COBBLESTONE_WALL, 6)
			.define('#', Blocks.COBBLESTONE)
			.pattern("###")
			.pattern("###")
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.COMPARATOR)
			.define('#', Blocks.REDSTONE_TORCH)
			.define('X', Items.QUARTZ)
			.define('I', Blocks.STONE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern("III")
			.unlocks("has_quartz", this.has(Items.QUARTZ))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.COMPASS)
			.define('#', Items.IRON_INGOT)
			.define('X', Items.REDSTONE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlocks("has_redstone", this.has(Items.REDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.COOKIE, 8)
			.define('#', Items.WHEAT)
			.define('X', Items.COCOA_BEANS)
			.pattern("#X#")
			.unlocks("has_cocoa", this.has(Items.COCOA_BEANS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CRAFTING_TABLE)
			.define('#', ItemTags.PLANKS)
			.pattern("##")
			.pattern("##")
			.unlocks("has_planks", this.has(ItemTags.PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.CROSSBOW)
			.define('~', Items.STRING)
			.define('#', Items.STICK)
			.define('&', Items.IRON_INGOT)
			.define('$', Blocks.TRIPWIRE_HOOK)
			.pattern("#&#")
			.pattern("~$~")
			.pattern(" # ")
			.unlocks("has_string", this.has(Items.STRING))
			.unlocks("has_stick", this.has(Items.STICK))
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.unlocks("has_tripwire_hook", this.has(Blocks.TRIPWIRE_HOOK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LOOM)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.STRING)
			.pattern("@@")
			.pattern("##")
			.unlocks("has_string", this.has(Items.STRING))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CHISELED_RED_SANDSTONE)
			.define('#', Blocks.RED_SANDSTONE_SLAB)
			.pattern("#")
			.pattern("#")
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.unlocks("has_chiseled_red_sandstone", this.has(Blocks.CHISELED_RED_SANDSTONE))
			.unlocks("has_cut_red_sandstone", this.has(Blocks.CUT_RED_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CHISELED_SANDSTONE)
			.define('#', Blocks.SANDSTONE_SLAB)
			.pattern("#")
			.pattern("#")
			.unlocks("has_stone_slab", this.has(Blocks.SANDSTONE_SLAB))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.CYAN_BANNER)
			.define('#', Blocks.CYAN_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_cyan_wool", this.has(Blocks.CYAN_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.CYAN_BED)
			.define('#', Blocks.CYAN_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_cyan_wool", this.has(Blocks.CYAN_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.CYAN_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.CYAN_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "cyan_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.CYAN_CARPET, 3)
			.define('#', Blocks.CYAN_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_cyan_wool", this.has(Blocks.CYAN_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CYAN_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.CYAN_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_cyan_dye", this.has(Items.CYAN_DYE))
			.save(consumer, "cyan_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.CYAN_CONCRETE_POWDER, 8)
			.requires(Items.CYAN_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.CYAN_DYE, 2)
			.requires(Items.BLUE_DYE)
			.requires(Items.GREEN_DYE)
			.unlocks("has_green_dye", this.has(Items.GREEN_DYE))
			.unlocks("has_blue_dye", this.has(Items.BLUE_DYE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CYAN_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.CYAN_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CYAN_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.CYAN_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CYAN_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.CYAN_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_cyan_dye", this.has(Items.CYAN_DYE))
			.save(consumer, "cyan_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.CYAN_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.CYAN_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.CYAN_WOOL)
			.requires(Items.CYAN_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DARK_OAK_WOOD, 3)
			.define('#', Blocks.DARK_OAK_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.DARK_OAK_LOG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STRIPPED_DARK_OAK_WOOD, 3)
			.define('#', Blocks.STRIPPED_DARK_OAK_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.STRIPPED_DARK_OAK_LOG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.DARK_OAK_BOAT)
			.define('#', Blocks.DARK_OAK_PLANKS)
			.pattern("# #")
			.pattern("###")
			.group("boat")
			.unlocks("in_water", this.insideOf(Blocks.WATER))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.DARK_OAK_BUTTON)
			.requires(Blocks.DARK_OAK_PLANKS)
			.group("wooden_button")
			.unlocks("has_planks", this.has(Blocks.DARK_OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DARK_OAK_DOOR, 3)
			.define('#', Blocks.DARK_OAK_PLANKS)
			.pattern("##")
			.pattern("##")
			.pattern("##")
			.group("wooden_door")
			.unlocks("has_planks", this.has(Blocks.DARK_OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DARK_OAK_FENCE, 3)
			.define('#', Items.STICK)
			.define('W', Blocks.DARK_OAK_PLANKS)
			.pattern("W#W")
			.pattern("W#W")
			.group("wooden_fence")
			.unlocks("has_planks", this.has(Blocks.DARK_OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DARK_OAK_FENCE_GATE)
			.define('#', Items.STICK)
			.define('W', Blocks.DARK_OAK_PLANKS)
			.pattern("#W#")
			.pattern("#W#")
			.group("wooden_fence_gate")
			.unlocks("has_planks", this.has(Blocks.DARK_OAK_PLANKS))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.DARK_OAK_PLANKS, 4)
			.requires(ItemTags.DARK_OAK_LOGS)
			.group("planks")
			.unlocks("has_logs", this.has(ItemTags.DARK_OAK_LOGS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DARK_OAK_PRESSURE_PLATE)
			.define('#', Blocks.DARK_OAK_PLANKS)
			.pattern("##")
			.group("wooden_pressure_plate")
			.unlocks("has_planks", this.has(Blocks.DARK_OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DARK_OAK_SLAB, 6)
			.define('#', Blocks.DARK_OAK_PLANKS)
			.pattern("###")
			.group("wooden_slab")
			.unlocks("has_planks", this.has(Blocks.DARK_OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DARK_OAK_STAIRS, 4)
			.define('#', Blocks.DARK_OAK_PLANKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.group("wooden_stairs")
			.unlocks("has_planks", this.has(Blocks.DARK_OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DARK_OAK_TRAPDOOR, 2)
			.define('#', Blocks.DARK_OAK_PLANKS)
			.pattern("###")
			.pattern("###")
			.group("wooden_trapdoor")
			.unlocks("has_planks", this.has(Blocks.DARK_OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DARK_PRISMARINE)
			.define('S', Items.PRISMARINE_SHARD)
			.define('I', Items.BLACK_DYE)
			.pattern("SSS")
			.pattern("SIS")
			.pattern("SSS")
			.unlocks("has_prismarine_shard", this.has(Items.PRISMARINE_SHARD))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PRISMARINE_STAIRS, 4)
			.define('#', Blocks.PRISMARINE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_prismarine", this.has(Blocks.PRISMARINE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PRISMARINE_BRICK_STAIRS, 4)
			.define('#', Blocks.PRISMARINE_BRICKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_prismarine_bricks", this.has(Blocks.PRISMARINE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DARK_PRISMARINE_STAIRS, 4)
			.define('#', Blocks.DARK_PRISMARINE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_dark_prismarine", this.has(Blocks.DARK_PRISMARINE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DAYLIGHT_DETECTOR)
			.define('Q', Items.QUARTZ)
			.define('G', Blocks.GLASS)
			.define('W', Ingredient.of(ItemTags.WOODEN_SLABS))
			.pattern("GGG")
			.pattern("QQQ")
			.pattern("WWW")
			.unlocks("has_quartz", this.has(Items.QUARTZ))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DETECTOR_RAIL, 6)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.STONE_PRESSURE_PLATE)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("X#X")
			.pattern("XRX")
			.unlocks("has_rail", this.has(Blocks.RAIL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.DIAMOND, 9).requires(Blocks.DIAMOND_BLOCK).unlocks("has_diamond_block", this.has(Blocks.DIAMOND_BLOCK)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.DIAMOND_AXE)
			.define('#', Items.STICK)
			.define('X', Items.DIAMOND)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlocks("has_diamond", this.has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DIAMOND_BLOCK)
			.define('#', Items.DIAMOND)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.unlocks("has_diamond", this.has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.DIAMOND_BOOTS)
			.define('X', Items.DIAMOND)
			.pattern("X X")
			.pattern("X X")
			.unlocks("has_diamond", this.has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.DIAMOND_CHESTPLATE)
			.define('X', Items.DIAMOND)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlocks("has_diamond", this.has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.DIAMOND_HELMET)
			.define('X', Items.DIAMOND)
			.pattern("XXX")
			.pattern("X X")
			.unlocks("has_diamond", this.has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.DIAMOND_HOE)
			.define('#', Items.STICK)
			.define('X', Items.DIAMOND)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlocks("has_diamond", this.has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.DIAMOND_LEGGINGS)
			.define('X', Items.DIAMOND)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlocks("has_diamond", this.has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.DIAMOND_PICKAXE)
			.define('#', Items.STICK)
			.define('X', Items.DIAMOND)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlocks("has_diamond", this.has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.DIAMOND_SHOVEL)
			.define('#', Items.STICK)
			.define('X', Items.DIAMOND)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlocks("has_diamond", this.has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.DIAMOND_SWORD)
			.define('#', Items.STICK)
			.define('X', Items.DIAMOND)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlocks("has_diamond", this.has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DIORITE, 2)
			.define('Q', Items.QUARTZ)
			.define('C', Blocks.COBBLESTONE)
			.pattern("CQ")
			.pattern("QC")
			.unlocks("has_quartz", this.has(Items.QUARTZ))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DISPENSER)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.define('X', Items.BOW)
			.pattern("###")
			.pattern("#X#")
			.pattern("#R#")
			.unlocks("has_bow", this.has(Items.BOW))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DROPPER)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.pattern("###")
			.pattern("# #")
			.pattern("#R#")
			.unlocks("has_redstone", this.has(Items.REDSTONE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.EMERALD, 9).requires(Blocks.EMERALD_BLOCK).unlocks("has_emerald_block", this.has(Blocks.EMERALD_BLOCK)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.EMERALD_BLOCK)
			.define('#', Items.EMERALD)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.unlocks("has_emerald", this.has(Items.EMERALD))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ENCHANTING_TABLE)
			.define('B', Items.BOOK)
			.define('#', Blocks.OBSIDIAN)
			.define('D', Items.DIAMOND)
			.pattern(" B ")
			.pattern("D#D")
			.pattern("###")
			.unlocks("has_obsidian", this.has(Blocks.OBSIDIAN))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ENDER_CHEST)
			.define('#', Blocks.OBSIDIAN)
			.define('E', Items.ENDER_EYE)
			.pattern("###")
			.pattern("#E#")
			.pattern("###")
			.unlocks("has_ender_eye", this.has(Items.ENDER_EYE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.ENDER_EYE)
			.requires(Items.ENDER_PEARL)
			.requires(Items.BLAZE_POWDER)
			.unlocks("has_blaze_powder", this.has(Items.BLAZE_POWDER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.END_STONE_BRICKS, 4)
			.define('#', Blocks.END_STONE)
			.pattern("##")
			.pattern("##")
			.unlocks("has_end_stone", this.has(Blocks.END_STONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.END_CRYSTAL)
			.define('T', Items.GHAST_TEAR)
			.define('E', Items.ENDER_EYE)
			.define('G', Blocks.GLASS)
			.pattern("GGG")
			.pattern("GEG")
			.pattern("GTG")
			.unlocks("has_ender_eye", this.has(Items.ENDER_EYE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.END_ROD, 4)
			.define('#', Items.POPPED_CHORUS_FRUIT)
			.define('/', Items.BLAZE_ROD)
			.pattern("/")
			.pattern("#")
			.unlocks("has_chorus_fruit_popped", this.has(Items.POPPED_CHORUS_FRUIT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.OAK_FENCE, 3)
			.define('#', Items.STICK)
			.define('W', Blocks.OAK_PLANKS)
			.pattern("W#W")
			.pattern("W#W")
			.group("wooden_fence")
			.unlocks("has_planks", this.has(Blocks.OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.OAK_FENCE_GATE)
			.define('#', Items.STICK)
			.define('W', Blocks.OAK_PLANKS)
			.pattern("#W#")
			.pattern("#W#")
			.group("wooden_fence_gate")
			.unlocks("has_planks", this.has(Blocks.OAK_PLANKS))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.FERMENTED_SPIDER_EYE)
			.requires(Items.SPIDER_EYE)
			.requires(Blocks.BROWN_MUSHROOM)
			.requires(Items.SUGAR)
			.unlocks("has_spider_eye", this.has(Items.SPIDER_EYE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.FIRE_CHARGE, 3)
			.requires(Items.GUNPOWDER)
			.requires(Items.BLAZE_POWDER)
			.requires(Ingredient.of(Items.COAL, Items.CHARCOAL))
			.unlocks("has_blaze_powder", this.has(Items.BLAZE_POWDER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.FISHING_ROD)
			.define('#', Items.STICK)
			.define('X', Items.STRING)
			.pattern("  #")
			.pattern(" #X")
			.pattern("# X")
			.unlocks("has_string", this.has(Items.STRING))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.FLINT_AND_STEEL)
			.requires(Items.IRON_INGOT)
			.requires(Items.FLINT)
			.unlocks("has_flint", this.has(Items.FLINT))
			.unlocks("has_obsidian", this.has(Blocks.OBSIDIAN))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.FLOWER_POT)
			.define('#', Items.BRICK)
			.pattern("# #")
			.pattern(" # ")
			.unlocks("has_brick", this.has(Items.BRICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.FURNACE)
			.define('#', Blocks.COBBLESTONE)
			.pattern("###")
			.pattern("# #")
			.pattern("###")
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.FURNACE_MINECART)
			.define('A', Blocks.FURNACE)
			.define('B', Items.MINECART)
			.pattern("A")
			.pattern("B")
			.unlocks("has_minecart", this.has(Items.MINECART))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GLASS_BOTTLE, 3)
			.define('#', Blocks.GLASS)
			.pattern("# #")
			.pattern(" # ")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GLASS_PANE, 16)
			.define('#', Blocks.GLASS)
			.pattern("###")
			.pattern("###")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GLOWSTONE)
			.define('#', Items.GLOWSTONE_DUST)
			.pattern("##")
			.pattern("##")
			.unlocks("has_glowstone_dust", this.has(Items.GLOWSTONE_DUST))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GOLDEN_APPLE)
			.define('#', Items.GOLD_INGOT)
			.define('X', Items.APPLE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GOLDEN_AXE)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GOLDEN_BOOTS)
			.define('X', Items.GOLD_INGOT)
			.pattern("X X")
			.pattern("X X")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GOLDEN_CARROT)
			.define('#', Items.GOLD_NUGGET)
			.define('X', Items.CARROT)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlocks("has_gold_nugget", this.has(Items.GOLD_NUGGET))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GOLDEN_CHESTPLATE)
			.define('X', Items.GOLD_INGOT)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GOLDEN_HELMET)
			.define('X', Items.GOLD_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GOLDEN_HOE)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GOLDEN_LEGGINGS)
			.define('X', Items.GOLD_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GOLDEN_PICKAXE)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.POWERED_RAIL, 6)
			.define('R', Items.REDSTONE)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("X X")
			.pattern("X#X")
			.pattern("XRX")
			.unlocks("has_rail", this.has(Blocks.RAIL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GOLDEN_SHOVEL)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GOLDEN_SWORD)
			.define('#', Items.STICK)
			.define('X', Items.GOLD_INGOT)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GOLD_BLOCK)
			.define('#', Items.GOLD_INGOT)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.GOLD_INGOT, 9)
			.requires(Blocks.GOLD_BLOCK)
			.group("gold_ingot")
			.unlocks("has_gold_block", this.has(Blocks.GOLD_BLOCK))
			.save(consumer, "gold_ingot_from_gold_block");
		ShapedRecipeBuilder.shaped(Items.GOLD_INGOT)
			.define('#', Items.GOLD_NUGGET)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.group("gold_ingot")
			.unlocks("has_gold_nugget", this.has(Items.GOLD_NUGGET))
			.save(consumer, "gold_ingot_from_nuggets");
		ShapelessRecipeBuilder.shapeless(Items.GOLD_NUGGET, 9).requires(Items.GOLD_INGOT).unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT)).save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.GRANITE).requires(Blocks.DIORITE).requires(Items.QUARTZ).unlocks("has_quartz", this.has(Items.QUARTZ)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.GRAY_BANNER)
			.define('#', Blocks.GRAY_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_gray_wool", this.has(Blocks.GRAY_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GRAY_BED)
			.define('#', Blocks.GRAY_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_gray_wool", this.has(Blocks.GRAY_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.GRAY_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.GRAY_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "gray_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.GRAY_CARPET, 3)
			.define('#', Blocks.GRAY_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_gray_wool", this.has(Blocks.GRAY_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GRAY_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.GRAY_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_gray_dye", this.has(Items.GRAY_DYE))
			.save(consumer, "gray_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.GRAY_CONCRETE_POWDER, 8)
			.requires(Items.GRAY_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.GRAY_DYE, 2)
			.requires(Items.BLACK_DYE)
			.requires(Items.WHITE_DYE)
			.unlocks("has_white_dye", this.has(Items.WHITE_DYE))
			.unlocks("has_black_dye", this.has(Items.BLACK_DYE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GRAY_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.GRAY_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GRAY_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.GRAY_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GRAY_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.GRAY_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_gray_dye", this.has(Items.GRAY_DYE))
			.save(consumer, "gray_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.GRAY_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.GRAY_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.GRAY_WOOL)
			.requires(Items.GRAY_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GREEN_BANNER)
			.define('#', Blocks.GREEN_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_green_wool", this.has(Blocks.GREEN_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.GREEN_BED)
			.define('#', Blocks.GREEN_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_green_wool", this.has(Blocks.GREEN_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.GREEN_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.GREEN_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "green_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.GREEN_CARPET, 3)
			.define('#', Blocks.GREEN_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_green_wool", this.has(Blocks.GREEN_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GREEN_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.GREEN_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_green_dye", this.has(Items.GREEN_DYE))
			.save(consumer, "green_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.GREEN_CONCRETE_POWDER, 8)
			.requires(Items.GREEN_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GREEN_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.GREEN_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GREEN_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.GREEN_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GREEN_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.GREEN_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_green_dye", this.has(Items.GREEN_DYE))
			.save(consumer, "green_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.GREEN_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.GREEN_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.GREEN_WOOL)
			.requires(Items.GREEN_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.HAY_BLOCK)
			.define('#', Items.WHEAT)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.unlocks("has_wheat", this.has(Items.WHEAT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE)
			.define('#', Items.IRON_INGOT)
			.pattern("##")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.HONEY_BOTTLE, 4)
			.requires(Items.HONEY_BLOCK)
			.requires(Items.GLASS_BOTTLE, 4)
			.unlocks("has_honey_block", this.has(Blocks.HONEY_BLOCK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.HONEY_BLOCK, 1)
			.define('S', Items.HONEY_BOTTLE)
			.pattern("SS")
			.pattern("SS")
			.unlocks("has_honey_bottle", this.has(Items.HONEY_BOTTLE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.HONEYCOMB_BLOCK)
			.define('H', Items.HONEYCOMB)
			.pattern("HH")
			.pattern("HH")
			.unlocks("has_honeycomb", this.has(Items.HONEYCOMB))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.HOPPER)
			.define('C', Blocks.CHEST)
			.define('I', Items.IRON_INGOT)
			.pattern("I I")
			.pattern("ICI")
			.pattern(" I ")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.HOPPER_MINECART)
			.define('A', Blocks.HOPPER)
			.define('B', Items.MINECART)
			.pattern("A")
			.pattern("B")
			.unlocks("has_minecart", this.has(Items.MINECART))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.IRON_AXE)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.IRON_BARS, 16)
			.define('#', Items.IRON_INGOT)
			.pattern("###")
			.pattern("###")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.IRON_BLOCK)
			.define('#', Items.IRON_INGOT)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.IRON_BOOTS)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("X X")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.IRON_CHESTPLATE)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.IRON_DOOR, 3)
			.define('#', Items.IRON_INGOT)
			.pattern("##")
			.pattern("##")
			.pattern("##")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.IRON_HELMET)
			.define('X', Items.IRON_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.IRON_HOE)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.IRON_INGOT, 9)
			.requires(Blocks.IRON_BLOCK)
			.group("iron_ingot")
			.unlocks("has_iron_block", this.has(Blocks.IRON_BLOCK))
			.save(consumer, "iron_ingot_from_iron_block");
		ShapedRecipeBuilder.shaped(Items.IRON_INGOT)
			.define('#', Items.IRON_NUGGET)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.group("iron_ingot")
			.unlocks("has_iron_nugget", this.has(Items.IRON_NUGGET))
			.save(consumer, "iron_ingot_from_nuggets");
		ShapedRecipeBuilder.shaped(Items.IRON_LEGGINGS)
			.define('X', Items.IRON_INGOT)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.IRON_NUGGET, 9).requires(Items.IRON_INGOT).unlocks("has_iron_ingot", this.has(Items.IRON_INGOT)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.IRON_PICKAXE)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.IRON_SHOVEL)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.IRON_SWORD)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.IRON_TRAPDOOR)
			.define('#', Items.IRON_INGOT)
			.pattern("##")
			.pattern("##")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.ITEM_FRAME)
			.define('#', Items.STICK)
			.define('X', Items.LEATHER)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlocks("has_leather", this.has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.JUKEBOX)
			.define('#', ItemTags.PLANKS)
			.define('X', Items.DIAMOND)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlocks("has_diamond", this.has(Items.DIAMOND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.JUNGLE_WOOD, 3)
			.define('#', Blocks.JUNGLE_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.JUNGLE_LOG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STRIPPED_JUNGLE_WOOD, 3)
			.define('#', Blocks.STRIPPED_JUNGLE_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.STRIPPED_JUNGLE_LOG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.JUNGLE_BOAT)
			.define('#', Blocks.JUNGLE_PLANKS)
			.pattern("# #")
			.pattern("###")
			.group("boat")
			.unlocks("in_water", this.insideOf(Blocks.WATER))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.JUNGLE_BUTTON)
			.requires(Blocks.JUNGLE_PLANKS)
			.group("wooden_button")
			.unlocks("has_planks", this.has(Blocks.JUNGLE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.JUNGLE_DOOR, 3)
			.define('#', Blocks.JUNGLE_PLANKS)
			.pattern("##")
			.pattern("##")
			.pattern("##")
			.group("wooden_door")
			.unlocks("has_planks", this.has(Blocks.JUNGLE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.JUNGLE_FENCE, 3)
			.define('#', Items.STICK)
			.define('W', Blocks.JUNGLE_PLANKS)
			.pattern("W#W")
			.pattern("W#W")
			.group("wooden_fence")
			.unlocks("has_planks", this.has(Blocks.JUNGLE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.JUNGLE_FENCE_GATE)
			.define('#', Items.STICK)
			.define('W', Blocks.JUNGLE_PLANKS)
			.pattern("#W#")
			.pattern("#W#")
			.group("wooden_fence_gate")
			.unlocks("has_planks", this.has(Blocks.JUNGLE_PLANKS))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.JUNGLE_PLANKS, 4)
			.requires(ItemTags.JUNGLE_LOGS)
			.group("planks")
			.unlocks("has_log", this.has(ItemTags.JUNGLE_LOGS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.JUNGLE_PRESSURE_PLATE)
			.define('#', Blocks.JUNGLE_PLANKS)
			.pattern("##")
			.group("wooden_pressure_plate")
			.unlocks("has_planks", this.has(Blocks.JUNGLE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.JUNGLE_SLAB, 6)
			.define('#', Blocks.JUNGLE_PLANKS)
			.pattern("###")
			.group("wooden_slab")
			.unlocks("has_planks", this.has(Blocks.JUNGLE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.JUNGLE_STAIRS, 4)
			.define('#', Blocks.JUNGLE_PLANKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.group("wooden_stairs")
			.unlocks("has_planks", this.has(Blocks.JUNGLE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.JUNGLE_TRAPDOOR, 2)
			.define('#', Blocks.JUNGLE_PLANKS)
			.pattern("###")
			.pattern("###")
			.group("wooden_trapdoor")
			.unlocks("has_planks", this.has(Blocks.JUNGLE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LADDER, 3)
			.define('#', Items.STICK)
			.pattern("# #")
			.pattern("###")
			.pattern("# #")
			.unlocks("has_stick", this.has(Items.STICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LAPIS_BLOCK)
			.define('#', Items.LAPIS_LAZULI)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.unlocks("has_lapis", this.has(Items.LAPIS_LAZULI))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.LAPIS_LAZULI, 9).requires(Blocks.LAPIS_BLOCK).unlocks("has_lapis_block", this.has(Blocks.LAPIS_BLOCK)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.LEAD, 2)
			.define('~', Items.STRING)
			.define('O', Items.SLIME_BALL)
			.pattern("~~ ")
			.pattern("~O ")
			.pattern("  ~")
			.unlocks("has_slime_ball", this.has(Items.SLIME_BALL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LEATHER)
			.define('#', Items.RABBIT_HIDE)
			.pattern("##")
			.pattern("##")
			.unlocks("has_rabbit_hide", this.has(Items.RABBIT_HIDE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LEATHER_BOOTS)
			.define('X', Items.LEATHER)
			.pattern("X X")
			.pattern("X X")
			.unlocks("has_leather", this.has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LEATHER_CHESTPLATE)
			.define('X', Items.LEATHER)
			.pattern("X X")
			.pattern("XXX")
			.pattern("XXX")
			.unlocks("has_leather", this.has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LEATHER_HELMET)
			.define('X', Items.LEATHER)
			.pattern("XXX")
			.pattern("X X")
			.unlocks("has_leather", this.has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LEATHER_LEGGINGS)
			.define('X', Items.LEATHER)
			.pattern("XXX")
			.pattern("X X")
			.pattern("X X")
			.unlocks("has_leather", this.has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LEATHER_HORSE_ARMOR)
			.define('X', Items.LEATHER)
			.pattern("X X")
			.pattern("XXX")
			.pattern("X X")
			.unlocks("has_leather", this.has(Items.LEATHER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LECTERN)
			.define('S', ItemTags.WOODEN_SLABS)
			.define('B', Blocks.BOOKSHELF)
			.pattern("SSS")
			.pattern(" B ")
			.pattern(" S ")
			.unlocks("has_book", this.has(Items.BOOK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LEVER)
			.define('#', Blocks.COBBLESTONE)
			.define('X', Items.STICK)
			.pattern("X")
			.pattern("#")
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LIGHT_BLUE_BANNER)
			.define('#', Blocks.LIGHT_BLUE_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_light_blue_wool", this.has(Blocks.LIGHT_BLUE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LIGHT_BLUE_BED)
			.define('#', Blocks.LIGHT_BLUE_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_light_blue_wool", this.has(Blocks.LIGHT_BLUE_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.LIGHT_BLUE_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.LIGHT_BLUE_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "light_blue_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_BLUE_CARPET, 3)
			.define('#', Blocks.LIGHT_BLUE_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_light_blue_wool", this.has(Blocks.LIGHT_BLUE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_BLUE_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.LIGHT_BLUE_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_light_blue_dye", this.has(Items.LIGHT_BLUE_DYE))
			.save(consumer, "light_blue_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.LIGHT_BLUE_CONCRETE_POWDER, 8)
			.requires(Items.LIGHT_BLUE_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.LIGHT_BLUE_DYE)
			.requires(Blocks.BLUE_ORCHID)
			.group("light_blue_dye")
			.unlocks("has_red_flower", this.has(Blocks.BLUE_ORCHID))
			.save(consumer, "light_blue_dye_from_blue_orchid");
		ShapelessRecipeBuilder.shapeless(Items.LIGHT_BLUE_DYE, 2)
			.requires(Items.BLUE_DYE)
			.requires(Items.WHITE_DYE)
			.group("light_blue_dye")
			.unlocks("has_blue_dye", this.has(Items.BLUE_DYE))
			.unlocks("has_white_dye", this.has(Items.WHITE_DYE))
			.save(consumer, "light_blue_dye_from_blue_white_dye");
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_BLUE_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.LIGHT_BLUE_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.LIGHT_BLUE_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.LIGHT_BLUE_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_light_blue_dye", this.has(Items.LIGHT_BLUE_DYE))
			.save(consumer, "light_blue_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_BLUE_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.LIGHT_BLUE_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.LIGHT_BLUE_WOOL)
			.requires(Items.LIGHT_BLUE_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LIGHT_GRAY_BANNER)
			.define('#', Blocks.LIGHT_GRAY_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_light_gray_wool", this.has(Blocks.LIGHT_GRAY_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LIGHT_GRAY_BED)
			.define('#', Blocks.LIGHT_GRAY_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_light_gray_wool", this.has(Blocks.LIGHT_GRAY_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.LIGHT_GRAY_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "light_gray_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_GRAY_CARPET, 3)
			.define('#', Blocks.LIGHT_GRAY_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_light_gray_wool", this.has(Blocks.LIGHT_GRAY_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_GRAY_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.LIGHT_GRAY_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_light_gray_dye", this.has(Items.LIGHT_GRAY_DYE))
			.save(consumer, "light_gray_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.LIGHT_GRAY_CONCRETE_POWDER, 8)
			.requires(Items.LIGHT_GRAY_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE)
			.requires(Blocks.AZURE_BLUET)
			.group("light_gray_dye")
			.unlocks("has_red_flower", this.has(Blocks.AZURE_BLUET))
			.save(consumer, "light_gray_dye_from_azure_bluet");
		ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE, 2)
			.requires(Items.GRAY_DYE)
			.requires(Items.WHITE_DYE)
			.group("light_gray_dye")
			.unlocks("has_gray_dye", this.has(Items.GRAY_DYE))
			.unlocks("has_white_dye", this.has(Items.WHITE_DYE))
			.save(consumer, "light_gray_dye_from_gray_white_dye");
		ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE, 3)
			.requires(Items.BLACK_DYE)
			.requires(Items.WHITE_DYE, 2)
			.group("light_gray_dye")
			.unlocks("has_white_dye", this.has(Items.WHITE_DYE))
			.unlocks("has_black_dye", this.has(Items.BLACK_DYE))
			.save(consumer, "light_gray_dye_from_black_white_dye");
		ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE)
			.requires(Blocks.OXEYE_DAISY)
			.group("light_gray_dye")
			.unlocks("has_red_flower", this.has(Blocks.OXEYE_DAISY))
			.save(consumer, "light_gray_dye_from_oxeye_daisy");
		ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE)
			.requires(Blocks.WHITE_TULIP)
			.group("light_gray_dye")
			.unlocks("has_red_flower", this.has(Blocks.WHITE_TULIP))
			.save(consumer, "light_gray_dye_from_white_tulip");
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_GRAY_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.LIGHT_GRAY_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.LIGHT_GRAY_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.LIGHT_GRAY_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_light_gray_dye", this.has(Items.LIGHT_GRAY_DYE))
			.save(consumer, "light_gray_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_GRAY_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.LIGHT_GRAY_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.LIGHT_GRAY_WOOL)
			.requires(Items.LIGHT_GRAY_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE)
			.define('#', Items.GOLD_INGOT)
			.pattern("##")
			.unlocks("has_gold_ingot", this.has(Items.GOLD_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LIME_BANNER)
			.define('#', Blocks.LIME_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_lime_wool", this.has(Blocks.LIME_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.LIME_BED)
			.define('#', Blocks.LIME_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_lime_wool", this.has(Blocks.LIME_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.LIME_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.LIME_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "lime_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.LIME_CARPET, 3)
			.define('#', Blocks.LIME_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_lime_wool", this.has(Blocks.LIME_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LIME_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.LIME_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_lime_dye", this.has(Items.LIME_DYE))
			.save(consumer, "lime_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.LIME_CONCRETE_POWDER, 8)
			.requires(Items.LIME_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.LIME_DYE, 2)
			.requires(Items.GREEN_DYE)
			.requires(Items.WHITE_DYE)
			.unlocks("has_green_dye", this.has(Items.GREEN_DYE))
			.unlocks("has_white_dye", this.has(Items.WHITE_DYE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LIME_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.LIME_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LIME_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.LIME_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LIME_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.LIME_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_lime_dye", this.has(Items.LIME_DYE))
			.save(consumer, "lime_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.LIME_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.LIME_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.LIME_WOOL)
			.requires(Items.LIME_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.JACK_O_LANTERN)
			.define('A', Blocks.CARVED_PUMPKIN)
			.define('B', Blocks.TORCH)
			.pattern("A")
			.pattern("B")
			.unlocks("has_carved_pumpkin", this.has(Blocks.CARVED_PUMPKIN))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.MAGENTA_BANNER)
			.define('#', Blocks.MAGENTA_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_magenta_wool", this.has(Blocks.MAGENTA_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.MAGENTA_BED)
			.define('#', Blocks.MAGENTA_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_magenta_wool", this.has(Blocks.MAGENTA_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.MAGENTA_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.MAGENTA_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "magenta_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.MAGENTA_CARPET, 3)
			.define('#', Blocks.MAGENTA_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_magenta_wool", this.has(Blocks.MAGENTA_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.MAGENTA_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.MAGENTA_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_magenta_dye", this.has(Items.MAGENTA_DYE))
			.save(consumer, "magenta_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.MAGENTA_CONCRETE_POWDER, 8)
			.requires(Items.MAGENTA_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE)
			.requires(Blocks.ALLIUM)
			.group("magenta_dye")
			.unlocks("has_red_flower", this.has(Blocks.ALLIUM))
			.save(consumer, "magenta_dye_from_allium");
		ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE, 4)
			.requires(Items.BLUE_DYE)
			.requires(Items.RED_DYE, 2)
			.requires(Items.WHITE_DYE)
			.group("magenta_dye")
			.unlocks("has_blue_dye", this.has(Items.BLUE_DYE))
			.unlocks("has_rose_red", this.has(Items.RED_DYE))
			.unlocks("has_white_dye", this.has(Items.WHITE_DYE))
			.save(consumer, "magenta_dye_from_blue_red_white_dye");
		ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE, 3)
			.requires(Items.BLUE_DYE)
			.requires(Items.RED_DYE)
			.requires(Items.PINK_DYE)
			.group("magenta_dye")
			.unlocks("has_pink_dye", this.has(Items.PINK_DYE))
			.unlocks("has_blue_dye", this.has(Items.BLUE_DYE))
			.unlocks("has_red_dye", this.has(Items.RED_DYE))
			.save(consumer, "magenta_dye_from_blue_red_pink");
		ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE, 2)
			.requires(Blocks.LILAC)
			.group("magenta_dye")
			.unlocks("has_double_plant", this.has(Blocks.LILAC))
			.save(consumer, "magenta_dye_from_lilac");
		ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE, 2)
			.requires(Items.PURPLE_DYE)
			.requires(Items.PINK_DYE)
			.group("magenta_dye")
			.unlocks("has_pink_dye", this.has(Items.PINK_DYE))
			.unlocks("has_purple_dye", this.has(Items.PURPLE_DYE))
			.save(consumer, "magenta_dye_from_purple_and_pink");
		ShapedRecipeBuilder.shaped(Blocks.MAGENTA_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.MAGENTA_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.MAGENTA_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.MAGENTA_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.MAGENTA_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.MAGENTA_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_magenta_dye", this.has(Items.MAGENTA_DYE))
			.save(consumer, "magenta_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.MAGENTA_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.MAGENTA_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.MAGENTA_WOOL)
			.requires(Items.MAGENTA_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.MAGMA_BLOCK)
			.define('#', Items.MAGMA_CREAM)
			.pattern("##")
			.pattern("##")
			.unlocks("has_magma_cream", this.has(Items.MAGMA_CREAM))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.MAGMA_CREAM)
			.requires(Items.BLAZE_POWDER)
			.requires(Items.SLIME_BALL)
			.unlocks("has_blaze_powder", this.has(Items.BLAZE_POWDER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.MAP)
			.define('#', Items.PAPER)
			.define('X', Items.COMPASS)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlocks("has_compass", this.has(Items.COMPASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.MELON)
			.define('M', Items.MELON_SLICE)
			.pattern("MMM")
			.pattern("MMM")
			.pattern("MMM")
			.unlocks("has_melon", this.has(Items.MELON_SLICE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.MELON_SEEDS).requires(Items.MELON_SLICE).unlocks("has_melon", this.has(Items.MELON_SLICE)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.MINECART)
			.define('#', Items.IRON_INGOT)
			.pattern("# #")
			.pattern("###")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.MOSSY_COBBLESTONE)
			.requires(Blocks.COBBLESTONE)
			.requires(Blocks.VINE)
			.unlocks("has_vine", this.has(Blocks.VINE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.MOSSY_COBBLESTONE_WALL, 6)
			.define('#', Blocks.MOSSY_COBBLESTONE)
			.pattern("###")
			.pattern("###")
			.unlocks("has_mossy_cobblestone", this.has(Blocks.MOSSY_COBBLESTONE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.MOSSY_STONE_BRICKS)
			.requires(Blocks.STONE_BRICKS)
			.requires(Blocks.VINE)
			.unlocks("has_mossy_cobblestone", this.has(Blocks.MOSSY_COBBLESTONE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.MUSHROOM_STEW)
			.requires(Blocks.BROWN_MUSHROOM)
			.requires(Blocks.RED_MUSHROOM)
			.requires(Items.BOWL)
			.unlocks("has_mushroom_stew", this.has(Items.MUSHROOM_STEW))
			.unlocks("has_bowl", this.has(Items.BOWL))
			.unlocks("has_brown_mushroom", this.has(Blocks.BROWN_MUSHROOM))
			.unlocks("has_red_mushroom", this.has(Blocks.RED_MUSHROOM))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.NETHER_BRICKS)
			.define('N', Items.NETHER_BRICK)
			.pattern("NN")
			.pattern("NN")
			.unlocks("has_netherbrick", this.has(Items.NETHER_BRICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.NETHER_BRICK_FENCE, 6)
			.define('#', Blocks.NETHER_BRICKS)
			.define('-', Items.NETHER_BRICK)
			.pattern("#-#")
			.pattern("#-#")
			.unlocks("has_nether_brick", this.has(Blocks.NETHER_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.NETHER_BRICK_SLAB, 6)
			.define('#', Blocks.NETHER_BRICKS)
			.pattern("###")
			.unlocks("has_nether_brick", this.has(Blocks.NETHER_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.NETHER_BRICK_STAIRS, 4)
			.define('#', Blocks.NETHER_BRICKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_nether_brick", this.has(Blocks.NETHER_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.NETHER_WART_BLOCK)
			.define('#', Items.NETHER_WART)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.unlocks("has_nether_wart", this.has(Items.NETHER_WART))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.NOTE_BLOCK)
			.define('#', ItemTags.PLANKS)
			.define('X', Items.REDSTONE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlocks("has_redstone", this.has(Items.REDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.OAK_WOOD, 3)
			.define('#', Blocks.OAK_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.OAK_LOG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STRIPPED_OAK_WOOD, 3)
			.define('#', Blocks.STRIPPED_OAK_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.STRIPPED_OAK_LOG))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.OAK_BUTTON)
			.requires(Blocks.OAK_PLANKS)
			.group("wooden_button")
			.unlocks("has_planks", this.has(Blocks.OAK_PLANKS))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.OAK_PLANKS, 4)
			.requires(ItemTags.OAK_LOGS)
			.group("planks")
			.unlocks("has_log", this.has(ItemTags.OAK_LOGS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.OAK_PRESSURE_PLATE)
			.define('#', Blocks.OAK_PLANKS)
			.pattern("##")
			.group("wooden_pressure_plate")
			.unlocks("has_planks", this.has(Blocks.OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.OAK_SLAB, 6)
			.define('#', Blocks.OAK_PLANKS)
			.pattern("###")
			.group("wooden_slab")
			.unlocks("has_planks", this.has(Blocks.OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.OAK_STAIRS, 4)
			.define('#', Blocks.OAK_PLANKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.group("wooden_stairs")
			.unlocks("has_planks", this.has(Blocks.OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.OAK_TRAPDOOR, 2)
			.define('#', Blocks.OAK_PLANKS)
			.pattern("###")
			.pattern("###")
			.group("wooden_trapdoor")
			.unlocks("has_planks", this.has(Blocks.OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.OBSERVER)
			.define('Q', Items.QUARTZ)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.pattern("###")
			.pattern("RRQ")
			.pattern("###")
			.unlocks("has_quartz", this.has(Items.QUARTZ))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.ORANGE_BANNER)
			.define('#', Blocks.ORANGE_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_orange_wool", this.has(Blocks.ORANGE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.ORANGE_BED)
			.define('#', Blocks.ORANGE_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_orange_wool", this.has(Blocks.ORANGE_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.ORANGE_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.ORANGE_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "orange_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.ORANGE_CARPET, 3)
			.define('#', Blocks.ORANGE_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_orange_wool", this.has(Blocks.ORANGE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ORANGE_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.ORANGE_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_oramge_dye", this.has(Items.ORANGE_DYE))
			.save(consumer, "orange_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.ORANGE_CONCRETE_POWDER, 8)
			.requires(Items.ORANGE_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.ORANGE_DYE)
			.requires(Blocks.ORANGE_TULIP)
			.group("orange_dye")
			.unlocks("has_red_flower", this.has(Blocks.ORANGE_TULIP))
			.save(consumer, "orange_dye_from_orange_tulip");
		ShapelessRecipeBuilder.shapeless(Items.ORANGE_DYE, 2)
			.requires(Items.RED_DYE)
			.requires(Items.YELLOW_DYE)
			.group("orange_dye")
			.unlocks("has_red_dye", this.has(Items.RED_DYE))
			.unlocks("has_yellow_dye", this.has(Items.YELLOW_DYE))
			.save(consumer, "orange_dye_from_red_yellow");
		ShapedRecipeBuilder.shaped(Blocks.ORANGE_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.ORANGE_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ORANGE_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.ORANGE_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ORANGE_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.ORANGE_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_orange_dye", this.has(Items.ORANGE_DYE))
			.save(consumer, "orange_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.ORANGE_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.ORANGE_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.ORANGE_WOOL)
			.requires(Items.ORANGE_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.PAINTING)
			.define('#', Items.STICK)
			.define('X', Ingredient.of(ItemTags.WOOL))
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlocks("has_wool", this.has(ItemTags.WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.PAPER, 3).define('#', Blocks.SUGAR_CANE).pattern("###").unlocks("has_reeds", this.has(Blocks.SUGAR_CANE)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.QUARTZ_PILLAR, 2)
			.define('#', Blocks.QUARTZ_BLOCK)
			.pattern("#")
			.pattern("#")
			.unlocks("has_chiseled_quartz_block", this.has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlocks("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.unlocks("has_quartz_pillar", this.has(Blocks.QUARTZ_PILLAR))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.PACKED_ICE).requires(Blocks.ICE, 9).unlocks("has_ice", this.has(Blocks.ICE)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.PINK_BANNER)
			.define('#', Blocks.PINK_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_pink_wool", this.has(Blocks.PINK_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.PINK_BED)
			.define('#', Blocks.PINK_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_pink_wool", this.has(Blocks.PINK_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.PINK_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.PINK_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "pink_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.PINK_CARPET, 3)
			.define('#', Blocks.PINK_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_pink_wool", this.has(Blocks.PINK_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PINK_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.PINK_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_pink_dye", this.has(Items.PINK_DYE))
			.save(consumer, "pink_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.PINK_CONCRETE_POWDER, 8)
			.requires(Items.PINK_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.PINK_DYE, 2)
			.requires(Blocks.PEONY)
			.group("pink_dye")
			.unlocks("has_double_plant", this.has(Blocks.PEONY))
			.save(consumer, "pink_dye_from_peony");
		ShapelessRecipeBuilder.shapeless(Items.PINK_DYE)
			.requires(Blocks.PINK_TULIP)
			.group("pink_dye")
			.unlocks("has_red_flower", this.has(Blocks.PINK_TULIP))
			.save(consumer, "pink_dye_from_pink_tulip");
		ShapelessRecipeBuilder.shapeless(Items.PINK_DYE, 2)
			.requires(Items.RED_DYE)
			.requires(Items.WHITE_DYE)
			.group("pink_dye")
			.unlocks("has_white_dye", this.has(Items.WHITE_DYE))
			.unlocks("has_red_dye", this.has(Items.RED_DYE))
			.save(consumer, "pink_dye_from_red_white_dye");
		ShapedRecipeBuilder.shaped(Blocks.PINK_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.PINK_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PINK_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.PINK_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PINK_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.PINK_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_pink_dye", this.has(Items.PINK_DYE))
			.save(consumer, "pink_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.PINK_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.PINK_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.PINK_WOOL)
			.requires(Items.PINK_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PISTON)
			.define('R', Items.REDSTONE)
			.define('#', Blocks.COBBLESTONE)
			.define('T', ItemTags.PLANKS)
			.define('X', Items.IRON_INGOT)
			.pattern("TTT")
			.pattern("#X#")
			.pattern("#R#")
			.unlocks("has_redstone", this.has(Items.REDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.POLISHED_GRANITE, 4)
			.define('S', Blocks.GRANITE)
			.pattern("SS")
			.pattern("SS")
			.unlocks("has_stone", this.has(Blocks.GRANITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.POLISHED_DIORITE, 4)
			.define('S', Blocks.DIORITE)
			.pattern("SS")
			.pattern("SS")
			.unlocks("has_stone", this.has(Blocks.DIORITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.POLISHED_ANDESITE, 4)
			.define('S', Blocks.ANDESITE)
			.pattern("SS")
			.pattern("SS")
			.unlocks("has_stone", this.has(Blocks.ANDESITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PRISMARINE)
			.define('S', Items.PRISMARINE_SHARD)
			.pattern("SS")
			.pattern("SS")
			.unlocks("has_prismarine_shard", this.has(Items.PRISMARINE_SHARD))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PRISMARINE_BRICKS)
			.define('S', Items.PRISMARINE_SHARD)
			.pattern("SSS")
			.pattern("SSS")
			.pattern("SSS")
			.unlocks("has_prismarine_shard", this.has(Items.PRISMARINE_SHARD))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PRISMARINE_SLAB, 6)
			.define('#', Blocks.PRISMARINE)
			.pattern("###")
			.unlocks("has_prismarine", this.has(Blocks.PRISMARINE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PRISMARINE_BRICK_SLAB, 6)
			.define('#', Blocks.PRISMARINE_BRICKS)
			.pattern("###")
			.unlocks("has_prismarine_bricks", this.has(Blocks.PRISMARINE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DARK_PRISMARINE_SLAB, 6)
			.define('#', Blocks.DARK_PRISMARINE)
			.pattern("###")
			.unlocks("has_dark_prismarine", this.has(Blocks.DARK_PRISMARINE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.PUMPKIN_PIE)
			.requires(Blocks.PUMPKIN)
			.requires(Items.SUGAR)
			.requires(Items.EGG)
			.unlocks("has_carved_pumpkin", this.has(Blocks.CARVED_PUMPKIN))
			.unlocks("has_pumpkin", this.has(Blocks.PUMPKIN))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.PUMPKIN_SEEDS, 4).requires(Blocks.PUMPKIN).unlocks("has_pumpkin", this.has(Blocks.PUMPKIN)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.PURPLE_BANNER)
			.define('#', Blocks.PURPLE_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_purple_wool", this.has(Blocks.PURPLE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.PURPLE_BED)
			.define('#', Blocks.PURPLE_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_purple_wool", this.has(Blocks.PURPLE_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.PURPLE_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.PURPLE_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "purple_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.PURPLE_CARPET, 3)
			.define('#', Blocks.PURPLE_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_purple_wool", this.has(Blocks.PURPLE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PURPLE_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.PURPLE_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_purple_dye", this.has(Items.PURPLE_DYE))
			.save(consumer, "purple_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.PURPLE_CONCRETE_POWDER, 8)
			.requires(Items.PURPLE_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.PURPLE_DYE, 2)
			.requires(Items.BLUE_DYE)
			.requires(Items.RED_DYE)
			.unlocks("has_blue_dye", this.has(Items.BLUE_DYE))
			.unlocks("has_red_dye", this.has(Items.RED_DYE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SHULKER_BOX)
			.define('#', Blocks.CHEST)
			.define('-', Items.SHULKER_SHELL)
			.pattern("-")
			.pattern("#")
			.pattern("-")
			.unlocks("has_shulker_shell", this.has(Items.SHULKER_SHELL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PURPLE_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.PURPLE_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PURPLE_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.PURPLE_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PURPLE_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.PURPLE_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_purple_dye", this.has(Items.PURPLE_DYE))
			.save(consumer, "purple_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.PURPLE_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.PURPLE_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.PURPLE_WOOL)
			.requires(Items.PURPLE_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PURPUR_BLOCK, 4)
			.define('F', Items.POPPED_CHORUS_FRUIT)
			.pattern("FF")
			.pattern("FF")
			.unlocks("has_chorus_fruit_popped", this.has(Items.POPPED_CHORUS_FRUIT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PURPUR_PILLAR)
			.define('#', Blocks.PURPUR_SLAB)
			.pattern("#")
			.pattern("#")
			.unlocks("has_purpur_block", this.has(Blocks.PURPUR_BLOCK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PURPUR_SLAB, 6)
			.define('#', Ingredient.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR))
			.pattern("###")
			.unlocks("has_purpur_block", this.has(Blocks.PURPUR_BLOCK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PURPUR_STAIRS, 4)
			.define('#', Ingredient.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR))
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_purpur_block", this.has(Blocks.PURPUR_BLOCK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.QUARTZ_BLOCK)
			.define('#', Items.QUARTZ)
			.pattern("##")
			.pattern("##")
			.unlocks("has_quartz", this.has(Items.QUARTZ))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.QUARTZ_SLAB, 6)
			.define('#', Ingredient.of(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR))
			.pattern("###")
			.unlocks("has_chiseled_quartz_block", this.has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlocks("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.unlocks("has_quartz_pillar", this.has(Blocks.QUARTZ_PILLAR))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.QUARTZ_STAIRS, 4)
			.define('#', Ingredient.of(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR))
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_chiseled_quartz_block", this.has(Blocks.CHISELED_QUARTZ_BLOCK))
			.unlocks("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.unlocks("has_quartz_pillar", this.has(Blocks.QUARTZ_PILLAR))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.RABBIT_STEW)
			.requires(Items.BAKED_POTATO)
			.requires(Items.COOKED_RABBIT)
			.requires(Items.BOWL)
			.requires(Items.CARROT)
			.requires(Blocks.BROWN_MUSHROOM)
			.group("rabbit_stew")
			.unlocks("has_cooked_rabbit", this.has(Items.COOKED_RABBIT))
			.save(consumer, "rabbit_stew_from_brown_mushroom");
		ShapelessRecipeBuilder.shapeless(Items.RABBIT_STEW)
			.requires(Items.BAKED_POTATO)
			.requires(Items.COOKED_RABBIT)
			.requires(Items.BOWL)
			.requires(Items.CARROT)
			.requires(Blocks.RED_MUSHROOM)
			.group("rabbit_stew")
			.unlocks("has_cooked_rabbit", this.has(Items.COOKED_RABBIT))
			.save(consumer, "rabbit_stew_from_red_mushroom");
		ShapedRecipeBuilder.shaped(Blocks.RAIL, 16)
			.define('#', Items.STICK)
			.define('X', Items.IRON_INGOT)
			.pattern("X X")
			.pattern("X#X")
			.pattern("X X")
			.unlocks("has_minecart", this.has(Items.MINECART))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.REDSTONE, 9)
			.requires(Blocks.REDSTONE_BLOCK)
			.unlocks("has_redstone_block", this.has(Blocks.REDSTONE_BLOCK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.REDSTONE_BLOCK)
			.define('#', Items.REDSTONE)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.unlocks("has_redstone", this.has(Items.REDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.REDSTONE_LAMP)
			.define('R', Items.REDSTONE)
			.define('G', Blocks.GLOWSTONE)
			.pattern(" R ")
			.pattern("RGR")
			.pattern(" R ")
			.unlocks("has_glowstone", this.has(Blocks.GLOWSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.REDSTONE_TORCH)
			.define('#', Items.STICK)
			.define('X', Items.REDSTONE)
			.pattern("X")
			.pattern("#")
			.unlocks("has_redstone", this.has(Items.REDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.RED_BANNER)
			.define('#', Blocks.RED_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_red_wool", this.has(Blocks.RED_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.RED_BED)
			.define('#', Blocks.RED_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_red_wool", this.has(Blocks.RED_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.RED_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.RED_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "red_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.RED_CARPET, 3)
			.define('#', Blocks.RED_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_red_wool", this.has(Blocks.RED_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.RED_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.RED_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_red_dye", this.has(Items.RED_DYE))
			.save(consumer, "red_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.RED_CONCRETE_POWDER, 8)
			.requires(Items.RED_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.RED_DYE)
			.requires(Items.BEETROOT)
			.group("red_dye")
			.unlocks("has_beetroot", this.has(Items.BEETROOT))
			.save(consumer, "red_dye_from_beetroot");
		ShapelessRecipeBuilder.shapeless(Items.RED_DYE)
			.requires(Blocks.POPPY)
			.group("red_dye")
			.unlocks("has_red_flower", this.has(Blocks.POPPY))
			.save(consumer, "red_dye_from_poppy");
		ShapelessRecipeBuilder.shapeless(Items.RED_DYE, 2)
			.requires(Blocks.ROSE_BUSH)
			.group("red_dye")
			.unlocks("has_double_plant", this.has(Blocks.ROSE_BUSH))
			.save(consumer, "red_dye_from_rose_bush");
		ShapelessRecipeBuilder.shapeless(Items.RED_DYE)
			.requires(Blocks.RED_TULIP)
			.group("red_dye")
			.unlocks("has_red_flower", this.has(Blocks.RED_TULIP))
			.save(consumer, "red_dye_from_tulip");
		ShapedRecipeBuilder.shaped(Blocks.RED_NETHER_BRICKS)
			.define('W', Items.NETHER_WART)
			.define('N', Items.NETHER_BRICK)
			.pattern("NW")
			.pattern("WN")
			.unlocks("has_nether_wart", this.has(Items.NETHER_WART))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.RED_SANDSTONE)
			.define('#', Blocks.RED_SAND)
			.pattern("##")
			.pattern("##")
			.unlocks("has_sand", this.has(Blocks.RED_SAND))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.RED_SANDSTONE_SLAB, 6)
			.define('#', Ingredient.of(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE))
			.pattern("###")
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.unlocks("has_chiseled_red_sandstone", this.has(Blocks.CHISELED_RED_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CUT_RED_SANDSTONE_SLAB, 6)
			.define('#', Blocks.CUT_RED_SANDSTONE)
			.pattern("###")
			.unlocks("has_cut_red_sandstone", this.has(Blocks.CUT_RED_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.RED_SANDSTONE_STAIRS, 4)
			.define('#', Ingredient.of(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE))
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.unlocks("has_chiseled_red_sandstone", this.has(Blocks.CHISELED_RED_SANDSTONE))
			.unlocks("has_cut_red_sandstone", this.has(Blocks.CUT_RED_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.RED_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.RED_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.RED_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.RED_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.RED_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.RED_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_red_dye", this.has(Items.RED_DYE))
			.save(consumer, "red_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.RED_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.RED_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.RED_WOOL)
			.requires(Items.RED_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.REPEATER)
			.define('#', Blocks.REDSTONE_TORCH)
			.define('X', Items.REDSTONE)
			.define('I', Blocks.STONE)
			.pattern("#X#")
			.pattern("III")
			.unlocks("has_redstone_torch", this.has(Blocks.REDSTONE_TORCH))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SANDSTONE).define('#', Blocks.SAND).pattern("##").pattern("##").unlocks("has_sand", this.has(Blocks.SAND)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SANDSTONE_SLAB, 6)
			.define('#', Ingredient.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE))
			.pattern("###")
			.unlocks("has_sandstone", this.has(Blocks.SANDSTONE))
			.unlocks("has_chiseled_sandstone", this.has(Blocks.CHISELED_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CUT_SANDSTONE_SLAB, 6)
			.define('#', Blocks.CUT_SANDSTONE)
			.pattern("###")
			.unlocks("has_cut_sandstone", this.has(Blocks.CUT_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SANDSTONE_STAIRS, 4)
			.define('#', Ingredient.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE))
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_sandstone", this.has(Blocks.SANDSTONE))
			.unlocks("has_chiseled_sandstone", this.has(Blocks.CHISELED_SANDSTONE))
			.unlocks("has_cut_sandstone", this.has(Blocks.CUT_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SEA_LANTERN)
			.define('S', Items.PRISMARINE_SHARD)
			.define('C', Items.PRISMARINE_CRYSTALS)
			.pattern("SCS")
			.pattern("CCC")
			.pattern("SCS")
			.unlocks("has_prismarine_crystals", this.has(Items.PRISMARINE_CRYSTALS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.SHEARS)
			.define('#', Items.IRON_INGOT)
			.pattern(" #")
			.pattern("# ")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.SHIELD)
			.define('W', ItemTags.PLANKS)
			.define('o', Items.IRON_INGOT)
			.pattern("WoW")
			.pattern("WWW")
			.pattern(" W ")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.OAK_SIGN, 3)
			.define('#', Items.OAK_PLANKS)
			.define('X', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" X ")
			.unlocks("has_oak_planks", this.has(Items.OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.SPRUCE_SIGN, 3)
			.define('#', Items.SPRUCE_PLANKS)
			.define('X', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" X ")
			.unlocks("has_spruce_planks", this.has(Items.SPRUCE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.BIRCH_SIGN, 3)
			.define('#', Items.BIRCH_PLANKS)
			.define('X', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" X ")
			.unlocks("has_birch_planks", this.has(Items.BIRCH_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.ACACIA_SIGN, 3)
			.define('#', Items.ACACIA_PLANKS)
			.define('X', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" X ")
			.unlocks("has_acacia_planks", this.has(Items.ACACIA_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.JUNGLE_SIGN, 3)
			.define('#', Items.JUNGLE_PLANKS)
			.define('X', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" X ")
			.unlocks("has_jungle_planks", this.has(Items.JUNGLE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.DARK_OAK_SIGN, 3)
			.define('#', Items.DARK_OAK_PLANKS)
			.define('X', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" X ")
			.unlocks("has_dark_oak_planks", this.has(Items.DARK_OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SLIME_BLOCK)
			.define('#', Items.SLIME_BALL)
			.pattern("###")
			.pattern("###")
			.pattern("###")
			.unlocks("has_slime_ball", this.has(Items.SLIME_BALL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.SLIME_BALL, 9).requires(Blocks.SLIME_BLOCK).unlocks("has_slime", this.has(Blocks.SLIME_BLOCK)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CUT_RED_SANDSTONE, 4)
			.define('#', Blocks.RED_SANDSTONE)
			.pattern("##")
			.pattern("##")
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CUT_SANDSTONE, 4)
			.define('#', Blocks.SANDSTONE)
			.pattern("##")
			.pattern("##")
			.unlocks("has_sandstone", this.has(Blocks.SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SNOW_BLOCK)
			.define('#', Items.SNOWBALL)
			.pattern("##")
			.pattern("##")
			.unlocks("has_snowball", this.has(Items.SNOWBALL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SNOW, 6).define('#', Blocks.SNOW_BLOCK).pattern("###").unlocks("has_snowball", this.has(Items.SNOWBALL)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.GLISTERING_MELON_SLICE)
			.define('#', Items.GOLD_NUGGET)
			.define('X', Items.MELON_SLICE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlocks("has_melon", this.has(Items.MELON_SLICE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.SPECTRAL_ARROW, 2)
			.define('#', Items.GLOWSTONE_DUST)
			.define('X', Items.ARROW)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlocks("has_glowstone_dust", this.has(Items.GLOWSTONE_DUST))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SPRUCE_WOOD, 3)
			.define('#', Blocks.SPRUCE_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.SPRUCE_LOG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STRIPPED_SPRUCE_WOOD, 3)
			.define('#', Blocks.STRIPPED_SPRUCE_LOG)
			.pattern("##")
			.pattern("##")
			.group("bark")
			.unlocks("has_log", this.has(Blocks.STRIPPED_SPRUCE_LOG))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.SPRUCE_BOAT)
			.define('#', Blocks.SPRUCE_PLANKS)
			.pattern("# #")
			.pattern("###")
			.group("boat")
			.unlocks("in_water", this.insideOf(Blocks.WATER))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.SPRUCE_BUTTON)
			.requires(Blocks.SPRUCE_PLANKS)
			.group("wooden_button")
			.unlocks("has_planks", this.has(Blocks.SPRUCE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SPRUCE_DOOR, 3)
			.define('#', Blocks.SPRUCE_PLANKS)
			.pattern("##")
			.pattern("##")
			.pattern("##")
			.group("wooden_door")
			.unlocks("has_planks", this.has(Blocks.SPRUCE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SPRUCE_FENCE, 3)
			.define('#', Items.STICK)
			.define('W', Blocks.SPRUCE_PLANKS)
			.pattern("W#W")
			.pattern("W#W")
			.group("wooden_fence")
			.unlocks("has_planks", this.has(Blocks.SPRUCE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SPRUCE_FENCE_GATE)
			.define('#', Items.STICK)
			.define('W', Blocks.SPRUCE_PLANKS)
			.pattern("#W#")
			.pattern("#W#")
			.group("wooden_fence_gate")
			.unlocks("has_planks", this.has(Blocks.SPRUCE_PLANKS))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.SPRUCE_PLANKS, 4)
			.requires(ItemTags.SPRUCE_LOGS)
			.group("planks")
			.unlocks("has_log", this.has(ItemTags.SPRUCE_LOGS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SPRUCE_PRESSURE_PLATE)
			.define('#', Blocks.SPRUCE_PLANKS)
			.pattern("##")
			.group("wooden_pressure_plate")
			.unlocks("has_planks", this.has(Blocks.SPRUCE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SPRUCE_SLAB, 6)
			.define('#', Blocks.SPRUCE_PLANKS)
			.pattern("###")
			.group("wooden_slab")
			.unlocks("has_planks", this.has(Blocks.SPRUCE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SPRUCE_STAIRS, 4)
			.define('#', Blocks.SPRUCE_PLANKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.group("wooden_stairs")
			.unlocks("has_planks", this.has(Blocks.SPRUCE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SPRUCE_TRAPDOOR, 2)
			.define('#', Blocks.SPRUCE_PLANKS)
			.pattern("###")
			.pattern("###")
			.group("wooden_trapdoor")
			.unlocks("has_planks", this.has(Blocks.SPRUCE_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.STICK, 4)
			.define('#', ItemTags.PLANKS)
			.pattern("#")
			.pattern("#")
			.group("sticks")
			.unlocks("has_planks", this.has(ItemTags.PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.STICK, 1)
			.define('#', Blocks.BAMBOO)
			.pattern("#")
			.pattern("#")
			.group("sticks")
			.unlocks("has_bamboo", this.has(Blocks.BAMBOO))
			.save(consumer, "stick_from_bamboo_item");
		ShapedRecipeBuilder.shaped(Blocks.STICKY_PISTON)
			.define('P', Blocks.PISTON)
			.define('S', Items.SLIME_BALL)
			.pattern("S")
			.pattern("P")
			.unlocks("has_slime_ball", this.has(Items.SLIME_BALL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STONE_BRICKS, 4)
			.define('#', Blocks.STONE)
			.pattern("##")
			.pattern("##")
			.unlocks("has_stone", this.has(Blocks.STONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.STONE_AXE)
			.define('#', Items.STICK)
			.define('X', Blocks.COBBLESTONE)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STONE_BRICK_SLAB, 6)
			.define('#', Blocks.STONE_BRICKS)
			.pattern("###")
			.unlocks("has_stone_bricks", this.has(ItemTags.STONE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STONE_BRICK_STAIRS, 4)
			.define('#', Blocks.STONE_BRICKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_stone_bricks", this.has(ItemTags.STONE_BRICKS))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.STONE_BUTTON).requires(Blocks.STONE).unlocks("has_stone", this.has(Blocks.STONE)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.STONE_HOE)
			.define('#', Items.STICK)
			.define('X', Blocks.COBBLESTONE)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.STONE_PICKAXE)
			.define('#', Items.STICK)
			.define('X', Blocks.COBBLESTONE)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STONE_PRESSURE_PLATE).define('#', Blocks.STONE).pattern("##").unlocks("has_stone", this.has(Blocks.STONE)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.STONE_SHOVEL)
			.define('#', Items.STICK)
			.define('X', Blocks.COBBLESTONE)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STONE_SLAB, 6).define('#', Blocks.STONE).pattern("###").unlocks("has_stone", this.has(Blocks.STONE)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SMOOTH_STONE_SLAB, 6)
			.define('#', Blocks.SMOOTH_STONE)
			.pattern("###")
			.unlocks("has_smooth_stone", this.has(Blocks.SMOOTH_STONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.COBBLESTONE_STAIRS, 4)
			.define('#', Blocks.COBBLESTONE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.STONE_SWORD)
			.define('#', Items.STICK)
			.define('X', Blocks.COBBLESTONE)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.WHITE_WOOL)
			.define('#', Items.STRING)
			.pattern("##")
			.pattern("##")
			.unlocks("has_string", this.has(Items.STRING))
			.save(consumer, "white_wool_from_string");
		ShapelessRecipeBuilder.shapeless(Items.SUGAR)
			.requires(Blocks.SUGAR_CANE)
			.group("sugar")
			.unlocks("has_reeds", this.has(Blocks.SUGAR_CANE))
			.save(consumer, "sugar_from_sugar_cane");
		ShapelessRecipeBuilder.shapeless(Items.SUGAR, 3)
			.requires(Items.HONEY_BOTTLE)
			.group("sugar")
			.unlocks("has_honey_bottle", this.has(Items.HONEY_BOTTLE))
			.save(consumer, "sugar_from_honey_bottle");
		ShapedRecipeBuilder.shaped(Blocks.TNT)
			.define('#', Ingredient.of(Blocks.SAND, Blocks.RED_SAND))
			.define('X', Items.GUNPOWDER)
			.pattern("X#X")
			.pattern("#X#")
			.pattern("X#X")
			.unlocks("has_gunpowder", this.has(Items.GUNPOWDER))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.TNT_MINECART)
			.define('A', Blocks.TNT)
			.define('B', Items.MINECART)
			.pattern("A")
			.pattern("B")
			.unlocks("has_minecart", this.has(Items.MINECART))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.TORCH, 4)
			.define('#', Items.STICK)
			.define('X', Ingredient.of(Items.COAL, Items.CHARCOAL))
			.pattern("X")
			.pattern("#")
			.unlocks("has_stone_pickaxe", this.has(Items.STONE_PICKAXE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.LANTERN)
			.define('#', Items.TORCH)
			.define('X', Items.IRON_NUGGET)
			.pattern("XXX")
			.pattern("X#X")
			.pattern("XXX")
			.unlocks("has_iron_nugget", this.has(Items.IRON_NUGGET))
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.TRAPPED_CHEST)
			.requires(Blocks.CHEST)
			.requires(Blocks.TRIPWIRE_HOOK)
			.unlocks("has_tripwire_hook", this.has(Blocks.TRIPWIRE_HOOK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.TRIPWIRE_HOOK, 2)
			.define('#', ItemTags.PLANKS)
			.define('S', Items.STICK)
			.define('I', Items.IRON_INGOT)
			.pattern("I")
			.pattern("S")
			.pattern("#")
			.unlocks("has_string", this.has(Items.STRING))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.TURTLE_HELMET)
			.define('X', Items.SCUTE)
			.pattern("XXX")
			.pattern("X X")
			.unlocks("has_scute", this.has(Items.SCUTE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.WHEAT, 9).requires(Blocks.HAY_BLOCK).unlocks("has_hay_block", this.has(Blocks.HAY_BLOCK)).save(consumer);
		ShapedRecipeBuilder.shaped(Items.WHITE_BANNER)
			.define('#', Blocks.WHITE_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.WHITE_BED)
			.define('#', Blocks.WHITE_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.WHITE_CARPET, 3)
			.define('#', Blocks.WHITE_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.WHITE_CONCRETE_POWDER, 8)
			.requires(Items.WHITE_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.WHITE_DYE)
			.requires(Items.BONE_MEAL)
			.group("white_dye")
			.unlocks("has_bone_meal", this.has(Items.BONE_MEAL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.WHITE_DYE)
			.requires(Blocks.LILY_OF_THE_VALLEY)
			.group("white_dye")
			.unlocks("has_white_flower", this.has(Blocks.LILY_OF_THE_VALLEY))
			.save(consumer, "white_dye_from_lily_of_the_valley");
		ShapedRecipeBuilder.shaped(Blocks.WHITE_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.WHITE_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.WHITE_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.WHITE_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.WHITE_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.WHITE_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_white_dye", this.has(Items.WHITE_DYE))
			.save(consumer, "white_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.WHITE_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.WHITE_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.WOODEN_AXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.PLANKS)
			.pattern("XX")
			.pattern("X#")
			.pattern(" #")
			.unlocks("has_stick", this.has(Items.STICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.OAK_DOOR, 3)
			.define('#', Blocks.OAK_PLANKS)
			.pattern("##")
			.pattern("##")
			.pattern("##")
			.group("wooden_door")
			.unlocks("has_planks", this.has(Blocks.OAK_PLANKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.WOODEN_HOE)
			.define('#', Items.STICK)
			.define('X', ItemTags.PLANKS)
			.pattern("XX")
			.pattern(" #")
			.pattern(" #")
			.unlocks("has_stick", this.has(Items.STICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.WOODEN_PICKAXE)
			.define('#', Items.STICK)
			.define('X', ItemTags.PLANKS)
			.pattern("XXX")
			.pattern(" # ")
			.pattern(" # ")
			.unlocks("has_stick", this.has(Items.STICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.WOODEN_SHOVEL)
			.define('#', Items.STICK)
			.define('X', ItemTags.PLANKS)
			.pattern("X")
			.pattern("#")
			.pattern("#")
			.unlocks("has_stick", this.has(Items.STICK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.WOODEN_SWORD)
			.define('#', Items.STICK)
			.define('X', ItemTags.PLANKS)
			.pattern("X")
			.pattern("X")
			.pattern("#")
			.unlocks("has_stick", this.has(Items.STICK))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.WRITABLE_BOOK)
			.requires(Items.BOOK)
			.requires(Items.INK_SAC)
			.requires(Items.FEATHER)
			.unlocks("has_book", this.has(Items.BOOK))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.YELLOW_BANNER)
			.define('#', Blocks.YELLOW_WOOL)
			.define('|', Items.STICK)
			.pattern("###")
			.pattern("###")
			.pattern(" | ")
			.group("banner")
			.unlocks("has_yellow_wool", this.has(Blocks.YELLOW_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Items.YELLOW_BED)
			.define('#', Blocks.YELLOW_WOOL)
			.define('X', ItemTags.PLANKS)
			.pattern("###")
			.pattern("XXX")
			.group("bed")
			.unlocks("has_yellow_wool", this.has(Blocks.YELLOW_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.YELLOW_BED)
			.requires(Items.WHITE_BED)
			.requires(Items.YELLOW_DYE)
			.group("dyed_bed")
			.unlocks("has_bed", this.has(Items.WHITE_BED))
			.save(consumer, "yellow_bed_from_white_bed");
		ShapedRecipeBuilder.shaped(Blocks.YELLOW_CARPET, 3)
			.define('#', Blocks.YELLOW_WOOL)
			.pattern("##")
			.group("carpet")
			.unlocks("has_yellow_wool", this.has(Blocks.YELLOW_WOOL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.YELLOW_CARPET, 8)
			.define('#', Blocks.WHITE_CARPET)
			.define('$', Items.YELLOW_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("carpet")
			.unlocks("has_white_carpet", this.has(Blocks.WHITE_CARPET))
			.unlocks("has_yellow_dye", this.has(Items.YELLOW_DYE))
			.save(consumer, "yellow_carpet_from_white_carpet");
		ShapelessRecipeBuilder.shapeless(Blocks.YELLOW_CONCRETE_POWDER, 8)
			.requires(Items.YELLOW_DYE)
			.requires(Blocks.SAND, 4)
			.requires(Blocks.GRAVEL, 4)
			.group("concrete_powder")
			.unlocks("has_sand", this.has(Blocks.SAND))
			.unlocks("has_gravel", this.has(Blocks.GRAVEL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.YELLOW_DYE)
			.requires(Blocks.DANDELION)
			.group("yellow_dye")
			.unlocks("has_yellow_flower", this.has(Blocks.DANDELION))
			.save(consumer, "yellow_dye_from_dandelion");
		ShapelessRecipeBuilder.shapeless(Items.YELLOW_DYE, 2)
			.requires(Blocks.SUNFLOWER)
			.group("yellow_dye")
			.unlocks("has_double_plant", this.has(Blocks.SUNFLOWER))
			.save(consumer, "yellow_dye_from_sunflower");
		ShapedRecipeBuilder.shaped(Blocks.YELLOW_STAINED_GLASS, 8)
			.define('#', Blocks.GLASS)
			.define('X', Items.YELLOW_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_glass")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.YELLOW_STAINED_GLASS_PANE, 16)
			.define('#', Blocks.YELLOW_STAINED_GLASS)
			.pattern("###")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass", this.has(Blocks.GLASS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.YELLOW_STAINED_GLASS_PANE, 8)
			.define('#', Blocks.GLASS_PANE)
			.define('$', Items.YELLOW_DYE)
			.pattern("###")
			.pattern("#$#")
			.pattern("###")
			.group("stained_glass_pane")
			.unlocks("has_glass_pane", this.has(Blocks.GLASS_PANE))
			.unlocks("has_yellow_dye", this.has(Items.YELLOW_DYE))
			.save(consumer, "yellow_stained_glass_pane_from_glass_pane");
		ShapedRecipeBuilder.shaped(Blocks.YELLOW_TERRACOTTA, 8)
			.define('#', Blocks.TERRACOTTA)
			.define('X', Items.YELLOW_DYE)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.group("stained_terracotta")
			.unlocks("has_terracotta", this.has(Blocks.TERRACOTTA))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.YELLOW_WOOL)
			.requires(Items.YELLOW_DYE)
			.requires(Blocks.WHITE_WOOL)
			.group("wool")
			.unlocks("has_white_wool", this.has(Blocks.WHITE_WOOL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.DRIED_KELP, 9)
			.requires(Blocks.DRIED_KELP_BLOCK)
			.unlocks("has_dried_kelp_block", this.has(Blocks.DRIED_KELP_BLOCK))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Blocks.DRIED_KELP_BLOCK).requires(Items.DRIED_KELP, 9).unlocks("has_dried_kelp", this.has(Items.DRIED_KELP)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CONDUIT)
			.define('#', Items.NAUTILUS_SHELL)
			.define('X', Items.HEART_OF_THE_SEA)
			.pattern("###")
			.pattern("#X#")
			.pattern("###")
			.unlocks("has_nautilus_core", this.has(Items.HEART_OF_THE_SEA))
			.unlocks("has_nautilus_shell", this.has(Items.NAUTILUS_SHELL))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.POLISHED_GRANITE_STAIRS, 4)
			.define('#', Blocks.POLISHED_GRANITE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_polished_granite", this.has(Blocks.POLISHED_GRANITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SMOOTH_RED_SANDSTONE_STAIRS, 4)
			.define('#', Blocks.SMOOTH_RED_SANDSTONE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_smooth_red_sandstone", this.has(Blocks.SMOOTH_RED_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.MOSSY_STONE_BRICK_STAIRS, 4)
			.define('#', Blocks.MOSSY_STONE_BRICKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_mossy_stone_bricks", this.has(Blocks.MOSSY_STONE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.POLISHED_DIORITE_STAIRS, 4)
			.define('#', Blocks.POLISHED_DIORITE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_polished_diorite", this.has(Blocks.POLISHED_DIORITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.MOSSY_COBBLESTONE_STAIRS, 4)
			.define('#', Blocks.MOSSY_COBBLESTONE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_mossy_cobblestone", this.has(Blocks.MOSSY_COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.END_STONE_BRICK_STAIRS, 4)
			.define('#', Blocks.END_STONE_BRICKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_end_stone_bricks", this.has(Blocks.END_STONE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STONE_STAIRS, 4)
			.define('#', Blocks.STONE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_stone", this.has(Blocks.STONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SMOOTH_SANDSTONE_STAIRS, 4)
			.define('#', Blocks.SMOOTH_SANDSTONE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_smooth_sandstone", this.has(Blocks.SMOOTH_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SMOOTH_QUARTZ_STAIRS, 4)
			.define('#', Blocks.SMOOTH_QUARTZ)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_smooth_quartz", this.has(Blocks.SMOOTH_QUARTZ))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GRANITE_STAIRS, 4)
			.define('#', Blocks.GRANITE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_granite", this.has(Blocks.GRANITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ANDESITE_STAIRS, 4)
			.define('#', Blocks.ANDESITE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_andesite", this.has(Blocks.ANDESITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.RED_NETHER_BRICK_STAIRS, 4)
			.define('#', Blocks.RED_NETHER_BRICKS)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_red_nether_bricks", this.has(Blocks.RED_NETHER_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.POLISHED_ANDESITE_STAIRS, 4)
			.define('#', Blocks.POLISHED_ANDESITE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_polished_andesite", this.has(Blocks.POLISHED_ANDESITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DIORITE_STAIRS, 4)
			.define('#', Blocks.DIORITE)
			.pattern("#  ")
			.pattern("## ")
			.pattern("###")
			.unlocks("has_diorite", this.has(Blocks.DIORITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.POLISHED_GRANITE_SLAB, 6)
			.define('#', Blocks.POLISHED_GRANITE)
			.pattern("###")
			.unlocks("has_polished_granite", this.has(Blocks.POLISHED_GRANITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SMOOTH_RED_SANDSTONE_SLAB, 6)
			.define('#', Blocks.SMOOTH_RED_SANDSTONE)
			.pattern("###")
			.unlocks("has_smooth_red_sandstone", this.has(Blocks.SMOOTH_RED_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.MOSSY_STONE_BRICK_SLAB, 6)
			.define('#', Blocks.MOSSY_STONE_BRICKS)
			.pattern("###")
			.unlocks("has_mossy_stone_bricks", this.has(Blocks.MOSSY_STONE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.POLISHED_DIORITE_SLAB, 6)
			.define('#', Blocks.POLISHED_DIORITE)
			.pattern("###")
			.unlocks("has_polished_diorite", this.has(Blocks.POLISHED_DIORITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.MOSSY_COBBLESTONE_SLAB, 6)
			.define('#', Blocks.MOSSY_COBBLESTONE)
			.pattern("###")
			.unlocks("has_mossy_cobblestone", this.has(Blocks.MOSSY_COBBLESTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.END_STONE_BRICK_SLAB, 6)
			.define('#', Blocks.END_STONE_BRICKS)
			.pattern("###")
			.unlocks("has_end_stone_bricks", this.has(Blocks.END_STONE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SMOOTH_SANDSTONE_SLAB, 6)
			.define('#', Blocks.SMOOTH_SANDSTONE)
			.pattern("###")
			.unlocks("has_smooth_sandstone", this.has(Blocks.SMOOTH_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SMOOTH_QUARTZ_SLAB, 6)
			.define('#', Blocks.SMOOTH_QUARTZ)
			.pattern("###")
			.unlocks("has_smooth_quartz", this.has(Blocks.SMOOTH_QUARTZ))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GRANITE_SLAB, 6).define('#', Blocks.GRANITE).pattern("###").unlocks("has_granite", this.has(Blocks.GRANITE)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ANDESITE_SLAB, 6)
			.define('#', Blocks.ANDESITE)
			.pattern("###")
			.unlocks("has_andesite", this.has(Blocks.ANDESITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.RED_NETHER_BRICK_SLAB, 6)
			.define('#', Blocks.RED_NETHER_BRICKS)
			.pattern("###")
			.unlocks("has_red_nether_bricks", this.has(Blocks.RED_NETHER_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.POLISHED_ANDESITE_SLAB, 6)
			.define('#', Blocks.POLISHED_ANDESITE)
			.pattern("###")
			.unlocks("has_polished_andesite", this.has(Blocks.POLISHED_ANDESITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DIORITE_SLAB, 6).define('#', Blocks.DIORITE).pattern("###").unlocks("has_diorite", this.has(Blocks.DIORITE)).save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BRICK_WALL, 6)
			.define('#', Blocks.BRICKS)
			.pattern("###")
			.pattern("###")
			.unlocks("has_bricks", this.has(Blocks.BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.PRISMARINE_WALL, 6)
			.define('#', Blocks.PRISMARINE)
			.pattern("###")
			.pattern("###")
			.unlocks("has_prismarine", this.has(Blocks.PRISMARINE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.RED_SANDSTONE_WALL, 6)
			.define('#', Blocks.RED_SANDSTONE)
			.pattern("###")
			.pattern("###")
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.MOSSY_STONE_BRICK_WALL, 6)
			.define('#', Blocks.MOSSY_STONE_BRICKS)
			.pattern("###")
			.pattern("###")
			.unlocks("has_mossy_stone_bricks", this.has(Blocks.MOSSY_STONE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GRANITE_WALL, 6)
			.define('#', Blocks.GRANITE)
			.pattern("###")
			.pattern("###")
			.unlocks("has_granite", this.has(Blocks.GRANITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STONE_BRICK_WALL, 6)
			.define('#', Blocks.STONE_BRICKS)
			.pattern("###")
			.pattern("###")
			.unlocks("has_stone_bricks", this.has(Blocks.STONE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.NETHER_BRICK_WALL, 6)
			.define('#', Blocks.NETHER_BRICKS)
			.pattern("###")
			.pattern("###")
			.unlocks("has_nether_bricks", this.has(Blocks.NETHER_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.ANDESITE_WALL, 6)
			.define('#', Blocks.ANDESITE)
			.pattern("###")
			.pattern("###")
			.unlocks("has_andesite", this.has(Blocks.ANDESITE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.RED_NETHER_BRICK_WALL, 6)
			.define('#', Blocks.RED_NETHER_BRICKS)
			.pattern("###")
			.pattern("###")
			.unlocks("has_red_nether_bricks", this.has(Blocks.RED_NETHER_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SANDSTONE_WALL, 6)
			.define('#', Blocks.SANDSTONE)
			.pattern("###")
			.pattern("###")
			.unlocks("has_sandstone", this.has(Blocks.SANDSTONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.END_STONE_BRICK_WALL, 6)
			.define('#', Blocks.END_STONE_BRICKS)
			.pattern("###")
			.pattern("###")
			.unlocks("has_end_stone_bricks", this.has(Blocks.END_STONE_BRICKS))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.DIORITE_WALL, 6)
			.define('#', Blocks.DIORITE)
			.pattern("###")
			.pattern("###")
			.unlocks("has_diorite", this.has(Blocks.DIORITE))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.CREEPER_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Items.CREEPER_HEAD)
			.unlocks("has_creeper_head", this.has(Items.CREEPER_HEAD))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.SKULL_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Items.WITHER_SKELETON_SKULL)
			.unlocks("has_wither_skeleton_skull", this.has(Items.WITHER_SKELETON_SKULL))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.FLOWER_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Blocks.OXEYE_DAISY)
			.unlocks("has_oxeye_daisy", this.has(Blocks.OXEYE_DAISY))
			.save(consumer);
		ShapelessRecipeBuilder.shapeless(Items.MOJANG_BANNER_PATTERN)
			.requires(Items.PAPER)
			.requires(Items.ENCHANTED_GOLDEN_APPLE)
			.unlocks("has_enchanted_golden_apple", this.has(Items.ENCHANTED_GOLDEN_APPLE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SCAFFOLDING, 6)
			.define('~', Items.STRING)
			.define('I', Blocks.BAMBOO)
			.pattern("I~I")
			.pattern("I I")
			.pattern("I I")
			.unlocks("has_bamboo", this.has(Blocks.BAMBOO))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.GRINDSTONE)
			.define('I', Items.STICK)
			.define('-', Blocks.STONE_SLAB)
			.define('#', ItemTags.PLANKS)
			.pattern("I-I")
			.pattern("# #")
			.unlocks("has_stone_slab", this.has(Blocks.STONE_SLAB))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.BLAST_FURNACE)
			.define('#', Blocks.SMOOTH_STONE)
			.define('X', Blocks.FURNACE)
			.define('I', Items.IRON_INGOT)
			.pattern("III")
			.pattern("IXI")
			.pattern("###")
			.unlocks("has_smooth_stone", this.has(Blocks.SMOOTH_STONE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SMOKER)
			.define('#', ItemTags.LOGS)
			.define('X', Blocks.FURNACE)
			.pattern(" # ")
			.pattern("#X#")
			.pattern(" # ")
			.unlocks("has_furnace", this.has(Blocks.FURNACE))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.CARTOGRAPHY_TABLE)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.PAPER)
			.pattern("@@")
			.pattern("##")
			.pattern("##")
			.unlocks("has_string", this.has(Items.STRING))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.SMITHING_TABLE)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.IRON_INGOT)
			.pattern("@@")
			.pattern("##")
			.pattern("##")
			.unlocks("has_iron_ingot", this.has(Items.IRON_INGOT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.FLETCHING_TABLE)
			.define('#', ItemTags.PLANKS)
			.define('@', Items.FLINT)
			.pattern("@@")
			.pattern("##")
			.pattern("##")
			.unlocks("has_flint", this.has(Items.FLINT))
			.save(consumer);
		ShapedRecipeBuilder.shaped(Blocks.STONECUTTER)
			.define('I', Items.IRON_INGOT)
			.define('#', Blocks.STONE)
			.pattern(" I ")
			.pattern("###")
			.unlocks("has_stone", this.has(Blocks.STONE))
			.save(consumer);
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
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.POTATO), Items.BAKED_POTATO, 0.35F, 200).unlocks("has_potato", this.has(Items.POTATO)).save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CLAY_BALL), Items.BRICK, 0.3F, 200)
			.unlocks("has_clay_ball", this.has(Items.CLAY_BALL))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.LOGS), Items.CHARCOAL, 0.15F, 200).unlocks("has_log", this.has(ItemTags.LOGS)).save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CHORUS_FRUIT), Items.POPPED_CHORUS_FRUIT, 0.1F, 200)
			.unlocks("has_chorus_fruit", this.has(Items.CHORUS_FRUIT))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.COAL_ORE.asItem()), Items.COAL, 0.1F, 200)
			.unlocks("has_coal_ore", this.has(Blocks.COAL_ORE))
			.save(consumer, "coal_from_smelting");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.BEEF), Items.COOKED_BEEF, 0.35F, 200).unlocks("has_beef", this.has(Items.BEEF)).save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CHICKEN), Items.COOKED_CHICKEN, 0.35F, 200)
			.unlocks("has_chicken", this.has(Items.CHICKEN))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.COD), Items.COOKED_COD, 0.35F, 200).unlocks("has_cod", this.has(Items.COD)).save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.KELP), Items.DRIED_KELP, 0.1F, 200)
			.unlocks("has_kelp", this.has(Blocks.KELP))
			.save(consumer, "dried_kelp_from_smelting");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.SALMON), Items.COOKED_SALMON, 0.35F, 200)
			.unlocks("has_salmon", this.has(Items.SALMON))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.MUTTON), Items.COOKED_MUTTON, 0.35F, 200)
			.unlocks("has_mutton", this.has(Items.MUTTON))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.PORKCHOP), Items.COOKED_PORKCHOP, 0.35F, 200)
			.unlocks("has_porkchop", this.has(Items.PORKCHOP))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.RABBIT), Items.COOKED_RABBIT, 0.35F, 200)
			.unlocks("has_rabbit", this.has(Items.RABBIT))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.DIAMOND_ORE.asItem()), Items.DIAMOND, 1.0F, 200)
			.unlocks("has_diamond_ore", this.has(Blocks.DIAMOND_ORE))
			.save(consumer, "diamond_from_smelting");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LAPIS_ORE.asItem()), Items.LAPIS_LAZULI, 0.2F, 200)
			.unlocks("has_lapis_ore", this.has(Blocks.LAPIS_ORE))
			.save(consumer, "lapis_from_smelting");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.EMERALD_ORE.asItem()), Items.EMERALD, 1.0F, 200)
			.unlocks("has_emerald_ore", this.has(Blocks.EMERALD_ORE))
			.save(consumer, "emerald_from_smelting");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.SAND), Blocks.GLASS.asItem(), 0.1F, 200)
			.unlocks("has_sand", this.has(ItemTags.SAND))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.GOLD_ORE.asItem()), Items.GOLD_INGOT, 1.0F, 200)
			.unlocks("has_gold_ore", this.has(Blocks.GOLD_ORE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.SEA_PICKLE.asItem()), Items.LIME_DYE, 0.1F, 200)
			.unlocks("has_sea_pickle", this.has(Blocks.SEA_PICKLE))
			.save(consumer, "lime_dye_from_smelting");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CACTUS.asItem()), Items.GREEN_DYE, 1.0F, 200)
			.unlocks("has_cactus", this.has(Blocks.CACTUS))
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
				Items.GOLD_NUGGET,
				0.1F,
				200
			)
			.unlocks("has_golden_pickaxe", this.has(Items.GOLDEN_PICKAXE))
			.unlocks("has_golden_shovel", this.has(Items.GOLDEN_SHOVEL))
			.unlocks("has_golden_axe", this.has(Items.GOLDEN_AXE))
			.unlocks("has_golden_hoe", this.has(Items.GOLDEN_HOE))
			.unlocks("has_golden_sword", this.has(Items.GOLDEN_SWORD))
			.unlocks("has_golden_helmet", this.has(Items.GOLDEN_HELMET))
			.unlocks("has_golden_chestplate", this.has(Items.GOLDEN_CHESTPLATE))
			.unlocks("has_golden_leggings", this.has(Items.GOLDEN_LEGGINGS))
			.unlocks("has_golden_boots", this.has(Items.GOLDEN_BOOTS))
			.unlocks("has_golden_horse_armor", this.has(Items.GOLDEN_HORSE_ARMOR))
			.save(consumer, "gold_nugget_from_smelting");
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
				Items.IRON_NUGGET,
				0.1F,
				200
			)
			.unlocks("has_iron_pickaxe", this.has(Items.IRON_PICKAXE))
			.unlocks("has_iron_shovel", this.has(Items.IRON_SHOVEL))
			.unlocks("has_iron_axe", this.has(Items.IRON_AXE))
			.unlocks("has_iron_hoe", this.has(Items.IRON_HOE))
			.unlocks("has_iron_sword", this.has(Items.IRON_SWORD))
			.unlocks("has_iron_helmet", this.has(Items.IRON_HELMET))
			.unlocks("has_iron_chestplate", this.has(Items.IRON_CHESTPLATE))
			.unlocks("has_iron_leggings", this.has(Items.IRON_LEGGINGS))
			.unlocks("has_iron_boots", this.has(Items.IRON_BOOTS))
			.unlocks("has_iron_horse_armor", this.has(Items.IRON_HORSE_ARMOR))
			.unlocks("has_chainmail_helmet", this.has(Items.CHAINMAIL_HELMET))
			.unlocks("has_chainmail_chestplate", this.has(Items.CHAINMAIL_CHESTPLATE))
			.unlocks("has_chainmail_leggings", this.has(Items.CHAINMAIL_LEGGINGS))
			.unlocks("has_chainmail_boots", this.has(Items.CHAINMAIL_BOOTS))
			.save(consumer, "iron_nugget_from_smelting");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.IRON_ORE.asItem()), Items.IRON_INGOT, 0.7F, 200)
			.unlocks("has_iron_ore", this.has(Blocks.IRON_ORE.asItem()))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CLAY), Blocks.TERRACOTTA.asItem(), 0.35F, 200)
			.unlocks("has_clay_block", this.has(Blocks.CLAY))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.NETHERRACK), Items.NETHER_BRICK, 0.1F, 200)
			.unlocks("has_netherrack", this.has(Blocks.NETHERRACK))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.NETHER_QUARTZ_ORE), Items.QUARTZ, 0.2F, 200)
			.unlocks("has_nether_quartz_ore", this.has(Blocks.NETHER_QUARTZ_ORE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.REDSTONE_ORE), Items.REDSTONE, 0.7F, 200)
			.unlocks("has_redstone_ore", this.has(Blocks.REDSTONE_ORE))
			.save(consumer, "redstone_from_smelting");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.WET_SPONGE), Blocks.SPONGE.asItem(), 0.15F, 200)
			.unlocks("has_wet_sponge", this.has(Blocks.WET_SPONGE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.COBBLESTONE), Blocks.STONE.asItem(), 0.1F, 200)
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.STONE), Blocks.SMOOTH_STONE.asItem(), 0.1F, 200)
			.unlocks("has_stone", this.has(Blocks.STONE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.SANDSTONE), Blocks.SMOOTH_SANDSTONE.asItem(), 0.1F, 200)
			.unlocks("has_sandstone", this.has(Blocks.SANDSTONE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.SMOOTH_RED_SANDSTONE.asItem(), 0.1F, 200)
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.SMOOTH_QUARTZ.asItem(), 0.1F, 200)
			.unlocks("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.CRACKED_STONE_BRICKS.asItem(), 0.1F, 200)
			.unlocks("has_stone_bricks", this.has(Blocks.STONE_BRICKS))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BLACK_TERRACOTTA), Blocks.BLACK_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_black_terracotta", this.has(Blocks.BLACK_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BLUE_TERRACOTTA), Blocks.BLUE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_blue_terracotta", this.has(Blocks.BLUE_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BROWN_TERRACOTTA), Blocks.BROWN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_brown_terracotta", this.has(Blocks.BROWN_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CYAN_TERRACOTTA), Blocks.CYAN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_cyan_terracotta", this.has(Blocks.CYAN_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.GRAY_TERRACOTTA), Blocks.GRAY_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_gray_terracotta", this.has(Blocks.GRAY_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.GREEN_TERRACOTTA), Blocks.GREEN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_green_terracotta", this.has(Blocks.GREEN_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LIGHT_BLUE_TERRACOTTA), Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_light_blue_terracotta", this.has(Blocks.LIGHT_BLUE_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LIGHT_GRAY_TERRACOTTA), Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_light_gray_terracotta", this.has(Blocks.LIGHT_GRAY_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LIME_TERRACOTTA), Blocks.LIME_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_lime_terracotta", this.has(Blocks.LIME_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.MAGENTA_TERRACOTTA), Blocks.MAGENTA_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_magenta_terracotta", this.has(Blocks.MAGENTA_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.ORANGE_TERRACOTTA), Blocks.ORANGE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_orange_terracotta", this.has(Blocks.ORANGE_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.PINK_TERRACOTTA), Blocks.PINK_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_pink_terracotta", this.has(Blocks.PINK_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.PURPLE_TERRACOTTA), Blocks.PURPLE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_purple_terracotta", this.has(Blocks.PURPLE_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.RED_TERRACOTTA), Blocks.RED_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_red_terracotta", this.has(Blocks.RED_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.WHITE_TERRACOTTA), Blocks.WHITE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_white_terracotta", this.has(Blocks.WHITE_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.YELLOW_TERRACOTTA), Blocks.YELLOW_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
			.unlocks("has_yellow_terracotta", this.has(Blocks.YELLOW_TERRACOTTA))
			.save(consumer);
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.IRON_ORE.asItem()), Items.IRON_INGOT, 0.7F, 100)
			.unlocks("has_iron_ore", this.has(Blocks.IRON_ORE.asItem()))
			.save(consumer, "iron_ingot_from_blasting");
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.GOLD_ORE.asItem()), Items.GOLD_INGOT, 1.0F, 100)
			.unlocks("has_gold_ore", this.has(Blocks.GOLD_ORE))
			.save(consumer, "gold_ingot_from_blasting");
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.DIAMOND_ORE.asItem()), Items.DIAMOND, 1.0F, 100)
			.unlocks("has_diamond_ore", this.has(Blocks.DIAMOND_ORE))
			.save(consumer, "diamond_from_blasting");
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.LAPIS_ORE.asItem()), Items.LAPIS_LAZULI, 0.2F, 100)
			.unlocks("has_lapis_ore", this.has(Blocks.LAPIS_ORE))
			.save(consumer, "lapis_from_blasting");
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.REDSTONE_ORE), Items.REDSTONE, 0.7F, 100)
			.unlocks("has_redstone_ore", this.has(Blocks.REDSTONE_ORE))
			.save(consumer, "redstone_from_blasting");
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.COAL_ORE.asItem()), Items.COAL, 0.1F, 100)
			.unlocks("has_coal_ore", this.has(Blocks.COAL_ORE))
			.save(consumer, "coal_from_blasting");
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.EMERALD_ORE.asItem()), Items.EMERALD, 1.0F, 100)
			.unlocks("has_emerald_ore", this.has(Blocks.EMERALD_ORE))
			.save(consumer, "emerald_from_blasting");
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.NETHER_QUARTZ_ORE), Items.QUARTZ, 0.2F, 100)
			.unlocks("has_nether_quartz_ore", this.has(Blocks.NETHER_QUARTZ_ORE))
			.save(consumer, "quartz_from_blasting");
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
				Items.GOLD_NUGGET,
				0.1F,
				100
			)
			.unlocks("has_golden_pickaxe", this.has(Items.GOLDEN_PICKAXE))
			.unlocks("has_golden_shovel", this.has(Items.GOLDEN_SHOVEL))
			.unlocks("has_golden_axe", this.has(Items.GOLDEN_AXE))
			.unlocks("has_golden_hoe", this.has(Items.GOLDEN_HOE))
			.unlocks("has_golden_sword", this.has(Items.GOLDEN_SWORD))
			.unlocks("has_golden_helmet", this.has(Items.GOLDEN_HELMET))
			.unlocks("has_golden_chestplate", this.has(Items.GOLDEN_CHESTPLATE))
			.unlocks("has_golden_leggings", this.has(Items.GOLDEN_LEGGINGS))
			.unlocks("has_golden_boots", this.has(Items.GOLDEN_BOOTS))
			.unlocks("has_golden_horse_armor", this.has(Items.GOLDEN_HORSE_ARMOR))
			.save(consumer, "gold_nugget_from_blasting");
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
				Items.IRON_NUGGET,
				0.1F,
				100
			)
			.unlocks("has_iron_pickaxe", this.has(Items.IRON_PICKAXE))
			.unlocks("has_iron_shovel", this.has(Items.IRON_SHOVEL))
			.unlocks("has_iron_axe", this.has(Items.IRON_AXE))
			.unlocks("has_iron_hoe", this.has(Items.IRON_HOE))
			.unlocks("has_iron_sword", this.has(Items.IRON_SWORD))
			.unlocks("has_iron_helmet", this.has(Items.IRON_HELMET))
			.unlocks("has_iron_chestplate", this.has(Items.IRON_CHESTPLATE))
			.unlocks("has_iron_leggings", this.has(Items.IRON_LEGGINGS))
			.unlocks("has_iron_boots", this.has(Items.IRON_BOOTS))
			.unlocks("has_iron_horse_armor", this.has(Items.IRON_HORSE_ARMOR))
			.unlocks("has_chainmail_helmet", this.has(Items.CHAINMAIL_HELMET))
			.unlocks("has_chainmail_chestplate", this.has(Items.CHAINMAIL_CHESTPLATE))
			.unlocks("has_chainmail_leggings", this.has(Items.CHAINMAIL_LEGGINGS))
			.unlocks("has_chainmail_boots", this.has(Items.CHAINMAIL_BOOTS))
			.save(consumer, "iron_nugget_from_blasting");
		this.cookRecipes(consumer, "smoking", RecipeSerializer.SMOKING_RECIPE, 100);
		this.cookRecipes(consumer, "campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING_RECIPE, 600);
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_SLAB, 2)
			.unlocks("has_stone", this.has(Blocks.STONE))
			.save(consumer, "stone_slab_from_stone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_STAIRS)
			.unlocks("has_stone", this.has(Blocks.STONE))
			.save(consumer, "stone_stairs_from_stone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_BRICKS)
			.unlocks("has_stone", this.has(Blocks.STONE))
			.save(consumer, "stone_bricks_from_stone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_BRICK_SLAB, 2)
			.unlocks("has_stone", this.has(Blocks.STONE))
			.save(consumer, "stone_brick_slab_from_stone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_BRICK_STAIRS)
			.unlocks("has_stone", this.has(Blocks.STONE))
			.save(consumer, "stone_brick_stairs_from_stone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.CHISELED_STONE_BRICKS)
			.unlocks("has_stone", this.has(Blocks.STONE))
			.save(consumer, "chiseled_stone_bricks_stone_from_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_BRICK_WALL)
			.unlocks("has_stone", this.has(Blocks.STONE))
			.save(consumer, "stone_brick_walls_from_stone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.CUT_SANDSTONE)
			.unlocks("has_sandstone", this.has(Blocks.SANDSTONE))
			.save(consumer, "cut_sandstone_from_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.SANDSTONE_SLAB, 2)
			.unlocks("has_sandstone", this.has(Blocks.SANDSTONE))
			.save(consumer, "sandstone_slab_from_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.CUT_SANDSTONE_SLAB, 2)
			.unlocks("has_sandstone", this.has(Blocks.SANDSTONE))
			.save(consumer, "cut_sandstone_slab_from_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.CUT_SANDSTONE), Blocks.CUT_SANDSTONE_SLAB, 2)
			.unlocks("has_cut_sandstone", this.has(Blocks.SANDSTONE))
			.save(consumer, "cut_sandstone_slab_from_cut_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.SANDSTONE_STAIRS)
			.unlocks("has_sandstone", this.has(Blocks.SANDSTONE))
			.save(consumer, "sandstone_stairs_from_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.SANDSTONE_WALL)
			.unlocks("has_sandstone", this.has(Blocks.SANDSTONE))
			.save(consumer, "sandstone_wall_from_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.CHISELED_SANDSTONE)
			.unlocks("has_sandstone", this.has(Blocks.SANDSTONE))
			.save(consumer, "chiseled_sandstone_from_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.CUT_RED_SANDSTONE)
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.save(consumer, "cut_red_sandstone_from_red_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.RED_SANDSTONE_SLAB, 2)
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.save(consumer, "red_sandstone_slab_from_red_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.CUT_RED_SANDSTONE_SLAB, 2)
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.save(consumer, "cut_red_sandstone_slab_from_red_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.CUT_RED_SANDSTONE), Blocks.CUT_RED_SANDSTONE_SLAB, 2)
			.unlocks("has_cut_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.save(consumer, "cut_red_sandstone_slab_from_cut_red_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.RED_SANDSTONE_STAIRS)
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.save(consumer, "red_sandstone_stairs_from_red_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.RED_SANDSTONE_WALL)
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.save(consumer, "red_sandstone_wall_from_red_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.CHISELED_RED_SANDSTONE)
			.unlocks("has_red_sandstone", this.has(Blocks.RED_SANDSTONE))
			.save(consumer, "chiseled_red_sandstone_from_red_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_SLAB, 2)
			.unlocks("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.save(consumer, "quartz_slab_from_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_STAIRS)
			.unlocks("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.save(consumer, "quartz_stairs_from_quartz_block_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_PILLAR)
			.unlocks("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.save(consumer, "quartz_pillar_from_quartz_block_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.CHISELED_QUARTZ_BLOCK)
			.unlocks("has_quartz_block", this.has(Blocks.QUARTZ_BLOCK))
			.save(consumer, "chiseled_quartz_block_from_quartz_block_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.COBBLESTONE), Blocks.COBBLESTONE_STAIRS)
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer, "cobblestone_stairs_from_cobblestone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.COBBLESTONE), Blocks.COBBLESTONE_SLAB, 2)
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer, "cobblestone_slab_from_cobblestone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.COBBLESTONE), Blocks.COBBLESTONE_WALL)
			.unlocks("has_cobblestone", this.has(Blocks.COBBLESTONE))
			.save(consumer, "cobblestone_wall_from_cobblestone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.STONE_BRICK_SLAB, 2)
			.unlocks("has_stone_bricks", this.has(Blocks.STONE_BRICKS))
			.save(consumer, "stone_brick_slab_from_stone_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.STONE_BRICK_STAIRS)
			.unlocks("has_stone_bricks", this.has(Blocks.STONE_BRICKS))
			.save(consumer, "stone_brick_stairs_from_stone_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.STONE_BRICK_WALL)
			.unlocks("has_stone_bricks", this.has(Blocks.STONE_BRICKS))
			.save(consumer, "stone_brick_wall_from_stone_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.CHISELED_STONE_BRICKS)
			.unlocks("has_stone_bricks", this.has(Blocks.STONE_BRICKS))
			.save(consumer, "chiseled_stone_bricks_from_stone_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BRICKS), Blocks.BRICK_SLAB, 2)
			.unlocks("has_bricks", this.has(Blocks.BRICKS))
			.save(consumer, "brick_slab_from_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BRICKS), Blocks.BRICK_STAIRS)
			.unlocks("has_bricks", this.has(Blocks.BRICKS))
			.save(consumer, "brick_stairs_from_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BRICKS), Blocks.BRICK_WALL)
			.unlocks("has_bricks", this.has(Blocks.BRICKS))
			.save(consumer, "brick_wall_from_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.NETHER_BRICKS), Blocks.NETHER_BRICK_SLAB, 2)
			.unlocks("has_nether_bricks", this.has(Blocks.NETHER_BRICKS))
			.save(consumer, "nether_brick_slab_from_nether_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.NETHER_BRICKS), Blocks.NETHER_BRICK_STAIRS)
			.unlocks("has_nether_bricks", this.has(Blocks.NETHER_BRICKS))
			.save(consumer, "nether_brick_stairs_from_nether_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.NETHER_BRICKS), Blocks.NETHER_BRICK_WALL)
			.unlocks("has_nether_bricks", this.has(Blocks.NETHER_BRICKS))
			.save(consumer, "nether_brick_wall_from_nether_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_NETHER_BRICKS), Blocks.RED_NETHER_BRICK_SLAB, 2)
			.unlocks("has_nether_bricks", this.has(Blocks.RED_NETHER_BRICKS))
			.save(consumer, "red_nether_brick_slab_from_red_nether_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_NETHER_BRICKS), Blocks.RED_NETHER_BRICK_STAIRS)
			.unlocks("has_nether_bricks", this.has(Blocks.RED_NETHER_BRICKS))
			.save(consumer, "red_nether_brick_stairs_from_red_nether_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_NETHER_BRICKS), Blocks.RED_NETHER_BRICK_WALL)
			.unlocks("has_nether_bricks", this.has(Blocks.RED_NETHER_BRICKS))
			.save(consumer, "red_nether_brick_wall_from_red_nether_bricks_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PURPUR_BLOCK), Blocks.PURPUR_SLAB, 2)
			.unlocks("has_purpur_block", this.has(Blocks.PURPUR_BLOCK))
			.save(consumer, "purpur_slab_from_purpur_block_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PURPUR_BLOCK), Blocks.PURPUR_STAIRS)
			.unlocks("has_purpur_block", this.has(Blocks.PURPUR_BLOCK))
			.save(consumer, "purpur_stairs_from_purpur_block_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PURPUR_BLOCK), Blocks.PURPUR_PILLAR)
			.unlocks("has_purpur_block", this.has(Blocks.PURPUR_BLOCK))
			.save(consumer, "purpur_pillar_from_purpur_block_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE), Blocks.PRISMARINE_SLAB, 2)
			.unlocks("has_prismarine", this.has(Blocks.PRISMARINE))
			.save(consumer, "prismarine_slab_from_prismarine_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE), Blocks.PRISMARINE_STAIRS)
			.unlocks("has_prismarine", this.has(Blocks.PRISMARINE))
			.save(consumer, "prismarine_stairs_from_prismarine_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE), Blocks.PRISMARINE_WALL)
			.unlocks("has_prismarine", this.has(Blocks.PRISMARINE))
			.save(consumer, "prismarine_wall_from_prismarine_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE_BRICKS), Blocks.PRISMARINE_BRICK_SLAB, 2)
			.unlocks("has_prismarine_brick", this.has(Blocks.PRISMARINE_BRICKS))
			.save(consumer, "prismarine_brick_slab_from_prismarine_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE_BRICKS), Blocks.PRISMARINE_BRICK_STAIRS)
			.unlocks("has_prismarine_brick", this.has(Blocks.PRISMARINE_BRICKS))
			.save(consumer, "prismarine_brick_stairs_from_prismarine_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DARK_PRISMARINE), Blocks.DARK_PRISMARINE_SLAB, 2)
			.unlocks("has_dark_prismarine", this.has(Blocks.DARK_PRISMARINE))
			.save(consumer, "dark_prismarine_slab_from_dark_prismarine_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DARK_PRISMARINE), Blocks.DARK_PRISMARINE_STAIRS)
			.unlocks("has_dark_prismarine", this.has(Blocks.DARK_PRISMARINE))
			.save(consumer, "dark_prismarine_stairs_from_dark_prismarine_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.ANDESITE_SLAB, 2)
			.unlocks("has_andesite", this.has(Blocks.ANDESITE))
			.save(consumer, "andesite_slab_from_andesite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.ANDESITE_STAIRS)
			.unlocks("has_andesite", this.has(Blocks.ANDESITE))
			.save(consumer, "andesite_stairs_from_andesite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.ANDESITE_WALL)
			.unlocks("has_andesite", this.has(Blocks.ANDESITE))
			.save(consumer, "andesite_wall_from_andesite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.POLISHED_ANDESITE)
			.unlocks("has_andesite", this.has(Blocks.ANDESITE))
			.save(consumer, "polished_andesite_from_andesite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.POLISHED_ANDESITE_SLAB, 2)
			.unlocks("has_andesite", this.has(Blocks.ANDESITE))
			.save(consumer, "polished_andesite_slab_from_andesite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.POLISHED_ANDESITE_STAIRS)
			.unlocks("has_andesite", this.has(Blocks.ANDESITE))
			.save(consumer, "polished_andesite_stairs_from_andesite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_ANDESITE), Blocks.POLISHED_ANDESITE_SLAB, 2)
			.unlocks("has_polished_andesite", this.has(Blocks.POLISHED_ANDESITE))
			.save(consumer, "polished_andesite_slab_from_polished_andesite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_ANDESITE), Blocks.POLISHED_ANDESITE_STAIRS)
			.unlocks("has_polished_andesite", this.has(Blocks.POLISHED_ANDESITE))
			.save(consumer, "polished_andesite_stairs_from_polished_andesite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.GRANITE_SLAB, 2)
			.unlocks("has_granite", this.has(Blocks.GRANITE))
			.save(consumer, "granite_slab_from_granite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.GRANITE_STAIRS)
			.unlocks("has_granite", this.has(Blocks.GRANITE))
			.save(consumer, "granite_stairs_from_granite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.GRANITE_WALL)
			.unlocks("has_granite", this.has(Blocks.GRANITE))
			.save(consumer, "granite_wall_from_granite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.POLISHED_GRANITE)
			.unlocks("has_granite", this.has(Blocks.GRANITE))
			.save(consumer, "polished_granite_from_granite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.POLISHED_GRANITE_SLAB, 2)
			.unlocks("has_granite", this.has(Blocks.GRANITE))
			.save(consumer, "polished_granite_slab_from_granite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.POLISHED_GRANITE_STAIRS)
			.unlocks("has_granite", this.has(Blocks.GRANITE))
			.save(consumer, "polished_granite_stairs_from_granite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_GRANITE), Blocks.POLISHED_GRANITE_SLAB, 2)
			.unlocks("has_polished_granite", this.has(Blocks.POLISHED_GRANITE))
			.save(consumer, "polished_granite_slab_from_polished_granite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_GRANITE), Blocks.POLISHED_GRANITE_STAIRS)
			.unlocks("has_polished_granite", this.has(Blocks.POLISHED_GRANITE))
			.save(consumer, "polished_granite_stairs_from_polished_granite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.DIORITE_SLAB, 2)
			.unlocks("has_diorite", this.has(Blocks.DIORITE))
			.save(consumer, "diorite_slab_from_diorite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.DIORITE_STAIRS)
			.unlocks("has_diorite", this.has(Blocks.DIORITE))
			.save(consumer, "diorite_stairs_from_diorite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.DIORITE_WALL)
			.unlocks("has_diorite", this.has(Blocks.DIORITE))
			.save(consumer, "diorite_wall_from_diorite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.POLISHED_DIORITE)
			.unlocks("has_diorite", this.has(Blocks.DIORITE))
			.save(consumer, "polished_diorite_from_diorite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.POLISHED_DIORITE_SLAB, 2)
			.unlocks("has_diorite", this.has(Blocks.POLISHED_DIORITE))
			.save(consumer, "polished_diorite_slab_from_diorite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.POLISHED_DIORITE_STAIRS)
			.unlocks("has_diorite", this.has(Blocks.POLISHED_DIORITE))
			.save(consumer, "polished_diorite_stairs_from_diorite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_DIORITE), Blocks.POLISHED_DIORITE_SLAB, 2)
			.unlocks("has_polished_diorite", this.has(Blocks.POLISHED_DIORITE))
			.save(consumer, "polished_diorite_slab_from_polished_diorite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_DIORITE), Blocks.POLISHED_DIORITE_STAIRS)
			.unlocks("has_polished_diorite", this.has(Blocks.POLISHED_DIORITE))
			.save(consumer, "polished_diorite_stairs_from_polished_diorite_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_SLAB, 2)
			.unlocks("has_mossy_stone_bricks", this.has(Blocks.MOSSY_STONE_BRICKS))
			.save(consumer, "mossy_stone_brick_slab_from_mossy_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_STAIRS)
			.unlocks("has_mossy_stone_bricks", this.has(Blocks.MOSSY_STONE_BRICKS))
			.save(consumer, "mossy_stone_brick_stairs_from_mossy_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_WALL)
			.unlocks("has_mossy_stone_bricks", this.has(Blocks.MOSSY_STONE_BRICKS))
			.save(consumer, "mossy_stone_brick_wall_from_mossy_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_COBBLESTONE), Blocks.MOSSY_COBBLESTONE_SLAB, 2)
			.unlocks("has_mossy_cobblestone", this.has(Blocks.MOSSY_COBBLESTONE))
			.save(consumer, "mossy_cobblestone_slab_from_mossy_cobblestone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_COBBLESTONE), Blocks.MOSSY_COBBLESTONE_STAIRS)
			.unlocks("has_mossy_cobblestone", this.has(Blocks.MOSSY_COBBLESTONE))
			.save(consumer, "mossy_cobblestone_stairs_from_mossy_cobblestone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_COBBLESTONE), Blocks.MOSSY_COBBLESTONE_WALL)
			.unlocks("has_mossy_cobblestone", this.has(Blocks.MOSSY_COBBLESTONE))
			.save(consumer, "mossy_cobblestone_wall_from_mossy_cobblestone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_SANDSTONE), Blocks.SMOOTH_SANDSTONE_SLAB, 2)
			.unlocks("has_smooth_sandstone", this.has(Blocks.SMOOTH_SANDSTONE))
			.save(consumer, "smooth_sandstone_slab_from_smooth_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_SANDSTONE), Blocks.SMOOTH_SANDSTONE_STAIRS)
			.unlocks("has_mossy_cobblestone", this.has(Blocks.SMOOTH_SANDSTONE))
			.save(consumer, "smooth_sandstone_stairs_from_smooth_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_RED_SANDSTONE), Blocks.SMOOTH_RED_SANDSTONE_SLAB, 2)
			.unlocks("has_smooth_red_sandstone", this.has(Blocks.SMOOTH_RED_SANDSTONE))
			.save(consumer, "smooth_red_sandstone_slab_from_smooth_red_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_RED_SANDSTONE), Blocks.SMOOTH_RED_SANDSTONE_STAIRS)
			.unlocks("has_smooth_red_sandstone", this.has(Blocks.SMOOTH_RED_SANDSTONE))
			.save(consumer, "smooth_red_sandstone_stairs_from_smooth_red_sandstone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_QUARTZ), Blocks.SMOOTH_QUARTZ_SLAB, 2)
			.unlocks("has_smooth_quartz", this.has(Blocks.SMOOTH_QUARTZ))
			.save(consumer, "smooth_quartz_slab_from_smooth_quartz_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_QUARTZ), Blocks.SMOOTH_QUARTZ_STAIRS)
			.unlocks("has_smooth_quartz", this.has(Blocks.SMOOTH_QUARTZ))
			.save(consumer, "smooth_quartz_stairs_from_smooth_quartz_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_SLAB, 2)
			.unlocks("has_end_stone_brick", this.has(Blocks.END_STONE_BRICKS))
			.save(consumer, "end_stone_brick_slab_from_end_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_STAIRS)
			.unlocks("has_end_stone_brick", this.has(Blocks.END_STONE_BRICKS))
			.save(consumer, "end_stone_brick_stairs_from_end_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_WALL)
			.unlocks("has_end_stone_brick", this.has(Blocks.END_STONE_BRICKS))
			.save(consumer, "end_stone_brick_wall_from_end_stone_brick_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE), Blocks.END_STONE_BRICKS)
			.unlocks("has_end_stone", this.has(Blocks.END_STONE))
			.save(consumer, "end_stone_bricks_from_end_stone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE), Blocks.END_STONE_BRICK_SLAB, 2)
			.unlocks("has_end_stone", this.has(Blocks.END_STONE))
			.save(consumer, "end_stone_brick_slab_from_end_stone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE), Blocks.END_STONE_BRICK_STAIRS)
			.unlocks("has_end_stone", this.has(Blocks.END_STONE))
			.save(consumer, "end_stone_brick_stairs_from_end_stone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE), Blocks.END_STONE_BRICK_WALL)
			.unlocks("has_end_stone", this.has(Blocks.END_STONE))
			.save(consumer, "end_stone_brick_wall_from_end_stone_stonecutting");
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_STONE), Blocks.SMOOTH_STONE_SLAB, 2)
			.unlocks("has_smooth_stone", this.has(Blocks.SMOOTH_STONE))
			.save(consumer, "smooth_stone_slab_from_smooth_stone_stonecutting");
	}

	private void cookRecipes(Consumer<FinishedRecipe> consumer, String string, SimpleCookingSerializer<?> simpleCookingSerializer, int i) {
		SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.BEEF), Items.COOKED_BEEF, 0.35F, i, simpleCookingSerializer)
			.unlocks("has_beef", this.has(Items.BEEF))
			.save(consumer, "cooked_beef_from_" + string);
		SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.CHICKEN), Items.COOKED_CHICKEN, 0.35F, i, simpleCookingSerializer)
			.unlocks("has_chicken", this.has(Items.CHICKEN))
			.save(consumer, "cooked_chicken_from_" + string);
		SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.COD), Items.COOKED_COD, 0.35F, i, simpleCookingSerializer)
			.unlocks("has_cod", this.has(Items.COD))
			.save(consumer, "cooked_cod_from_" + string);
		SimpleCookingRecipeBuilder.cooking(Ingredient.of(Blocks.KELP), Items.DRIED_KELP, 0.1F, i, simpleCookingSerializer)
			.unlocks("has_kelp", this.has(Blocks.KELP))
			.save(consumer, "dried_kelp_from_" + string);
		SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.SALMON), Items.COOKED_SALMON, 0.35F, i, simpleCookingSerializer)
			.unlocks("has_salmon", this.has(Items.SALMON))
			.save(consumer, "cooked_salmon_from_" + string);
		SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.MUTTON), Items.COOKED_MUTTON, 0.35F, i, simpleCookingSerializer)
			.unlocks("has_mutton", this.has(Items.MUTTON))
			.save(consumer, "cooked_mutton_from_" + string);
		SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.PORKCHOP), Items.COOKED_PORKCHOP, 0.35F, i, simpleCookingSerializer)
			.unlocks("has_porkchop", this.has(Items.PORKCHOP))
			.save(consumer, "cooked_porkchop_from_" + string);
		SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.POTATO), Items.BAKED_POTATO, 0.35F, i, simpleCookingSerializer)
			.unlocks("has_potato", this.has(Items.POTATO))
			.save(consumer, "baked_potato_from_" + string);
		SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.RABBIT), Items.COOKED_RABBIT, 0.35F, i, simpleCookingSerializer)
			.unlocks("has_rabbit", this.has(Items.RABBIT))
			.save(consumer, "cooked_rabbit_from_" + string);
	}

	private EnterBlockTrigger.TriggerInstance insideOf(Block block) {
		return new EnterBlockTrigger.TriggerInstance(block, StatePropertiesPredicate.ANY);
	}

	private InventoryChangeTrigger.TriggerInstance has(ItemLike itemLike) {
		return this.inventoryTrigger(ItemPredicate.Builder.item().of(itemLike).build());
	}

	private InventoryChangeTrigger.TriggerInstance has(Tag<Item> tag) {
		return this.inventoryTrigger(ItemPredicate.Builder.item().of(tag).build());
	}

	private InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... itemPredicates) {
		return new InventoryChangeTrigger.TriggerInstance(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, itemPredicates);
	}

	@Override
	public String getName() {
		return "Recipes";
	}
}
