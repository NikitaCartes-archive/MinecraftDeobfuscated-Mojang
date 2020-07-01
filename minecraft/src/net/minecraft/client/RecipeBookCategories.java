package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public enum RecipeBookCategories {
	CRAFTING_SEARCH(new ItemStack(Items.COMPASS)),
	CRAFTING_BUILDING_BLOCKS(new ItemStack(Blocks.BRICKS)),
	CRAFTING_REDSTONE(new ItemStack(Items.REDSTONE)),
	CRAFTING_EQUIPMENT(new ItemStack(Items.IRON_AXE), new ItemStack(Items.GOLDEN_SWORD)),
	CRAFTING_MISC(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.APPLE)),
	FURNACE_SEARCH(new ItemStack(Items.COMPASS)),
	FURNACE_FOOD(new ItemStack(Items.PORKCHOP)),
	FURNACE_BLOCKS(new ItemStack(Blocks.STONE)),
	FURNACE_MISC(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.EMERALD)),
	BLAST_FURNACE_SEARCH(new ItemStack(Items.COMPASS)),
	BLAST_FURNACE_BLOCKS(new ItemStack(Blocks.REDSTONE_ORE)),
	BLAST_FURNACE_MISC(new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.GOLDEN_LEGGINGS)),
	SMOKER_SEARCH(new ItemStack(Items.COMPASS)),
	SMOKER_FOOD(new ItemStack(Items.PORKCHOP)),
	STONECUTTER(new ItemStack(Items.CHISELED_STONE_BRICKS)),
	SMITHING(new ItemStack(Items.NETHERITE_CHESTPLATE)),
	CAMPFIRE(new ItemStack(Items.PORKCHOP)),
	UNKNOWN(new ItemStack(Items.BARRIER));

	public static final List<RecipeBookCategories> SMOKER_CATEGORIES = ImmutableList.of(SMOKER_SEARCH, SMOKER_FOOD);
	public static final List<RecipeBookCategories> BLAST_FURNACE_CATEGORIES = ImmutableList.of(BLAST_FURNACE_SEARCH, BLAST_FURNACE_BLOCKS, BLAST_FURNACE_MISC);
	public static final List<RecipeBookCategories> FURNACE_CATEGORIES = ImmutableList.of(FURNACE_SEARCH, FURNACE_FOOD, FURNACE_BLOCKS, FURNACE_MISC);
	public static final List<RecipeBookCategories> CRAFTING_CATEGORIES = ImmutableList.of(
		CRAFTING_SEARCH, CRAFTING_EQUIPMENT, CRAFTING_BUILDING_BLOCKS, CRAFTING_MISC, CRAFTING_REDSTONE
	);
	public static final Map<RecipeBookCategories, List<RecipeBookCategories>> AGGREGATE_CATEGORIES = ImmutableMap.of(
		CRAFTING_SEARCH,
		ImmutableList.of(CRAFTING_EQUIPMENT, CRAFTING_BUILDING_BLOCKS, CRAFTING_MISC, CRAFTING_REDSTONE),
		FURNACE_SEARCH,
		ImmutableList.of(FURNACE_FOOD, FURNACE_BLOCKS, FURNACE_MISC),
		BLAST_FURNACE_SEARCH,
		ImmutableList.of(BLAST_FURNACE_BLOCKS, BLAST_FURNACE_MISC),
		SMOKER_SEARCH,
		ImmutableList.of(SMOKER_FOOD)
	);
	private final List<ItemStack> itemIcons;

	private RecipeBookCategories(ItemStack... itemStacks) {
		this.itemIcons = ImmutableList.copyOf(itemStacks);
	}

	public static List<RecipeBookCategories> getCategories(RecipeBookType recipeBookType) {
		switch (recipeBookType) {
			case CRAFTING:
				return CRAFTING_CATEGORIES;
			case FURNACE:
				return FURNACE_CATEGORIES;
			case BLAST_FURNACE:
				return BLAST_FURNACE_CATEGORIES;
			case SMOKER:
				return SMOKER_CATEGORIES;
			default:
				return ImmutableList.of();
		}
	}

	public List<ItemStack> getIconItems() {
		return this.itemIcons;
	}
}
