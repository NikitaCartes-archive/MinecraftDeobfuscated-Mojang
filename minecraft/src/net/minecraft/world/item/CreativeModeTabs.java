package net.minecraft.world.item;

import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public class CreativeModeTabs {
	private static final ResourceKey<CreativeModeTab> BUILDING_BLOCKS = createKey("building_blocks");
	private static final ResourceKey<CreativeModeTab> COLORED_BLOCKS = createKey("colored_blocks");
	private static final ResourceKey<CreativeModeTab> NATURAL_BLOCKS = createKey("natural_blocks");
	private static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS = createKey("functional_blocks");
	private static final ResourceKey<CreativeModeTab> REDSTONE_BLOCKS = createKey("redstone_blocks");
	private static final ResourceKey<CreativeModeTab> HOTBAR = createKey("hotbar");
	private static final ResourceKey<CreativeModeTab> SEARCH = createKey("search");
	private static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = createKey("tools_and_utilities");
	private static final ResourceKey<CreativeModeTab> COMBAT = createKey("combat");
	private static final ResourceKey<CreativeModeTab> FOOD_AND_DRINKS = createKey("food_and_drinks");
	private static final ResourceKey<CreativeModeTab> INGREDIENTS = createKey("ingredients");
	private static final ResourceKey<CreativeModeTab> SPAWN_EGGS = createKey("spawn_eggs");
	private static final ResourceKey<CreativeModeTab> OP_BLOCKS = createKey("op_blocks");
	private static final ResourceKey<CreativeModeTab> INVENTORY = createKey("inventory");
	private static final Comparator<Holder<PaintingVariant>> PAINTING_COMPARATOR = Comparator.comparing(
		Holder::value, Comparator.comparingInt(paintingVariant -> paintingVariant.getHeight() * paintingVariant.getWidth()).thenComparing(PaintingVariant::getWidth)
	);
	@Nullable
	private static CreativeModeTab.ItemDisplayParameters CACHED_PARAMETERS;

	private static ResourceKey<CreativeModeTab> createKey(String string) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(string));
	}

	public static CreativeModeTab bootstrap(Registry<CreativeModeTab> registry) {
		Registry.register(
			registry,
			BUILDING_BLOCKS,
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
				.title(Component.translatable("itemGroup.buildingBlocks"))
				.icon(() -> new ItemStack(Blocks.BRICKS))
				.displayItems((itemDisplayParameters, output) -> {
					output.accept(Items.OAK_LOG);
					output.accept(Items.OAK_WOOD);
					output.accept(Items.STRIPPED_OAK_LOG);
					output.accept(Items.STRIPPED_OAK_WOOD);
					output.accept(Items.OAK_PLANKS);
					output.accept(Items.OAK_STAIRS);
					output.accept(Items.OAK_SLAB);
					output.accept(Items.OAK_FENCE);
					output.accept(Items.OAK_FENCE_GATE);
					output.accept(Items.OAK_DOOR);
					output.accept(Items.OAK_TRAPDOOR);
					output.accept(Items.OAK_PRESSURE_PLATE);
					output.accept(Items.OAK_BUTTON);
					output.accept(Items.SPRUCE_LOG);
					output.accept(Items.SPRUCE_WOOD);
					output.accept(Items.STRIPPED_SPRUCE_LOG);
					output.accept(Items.STRIPPED_SPRUCE_WOOD);
					output.accept(Items.SPRUCE_PLANKS);
					output.accept(Items.SPRUCE_STAIRS);
					output.accept(Items.SPRUCE_SLAB);
					output.accept(Items.SPRUCE_FENCE);
					output.accept(Items.SPRUCE_FENCE_GATE);
					output.accept(Items.SPRUCE_DOOR);
					output.accept(Items.SPRUCE_TRAPDOOR);
					output.accept(Items.SPRUCE_PRESSURE_PLATE);
					output.accept(Items.SPRUCE_BUTTON);
					output.accept(Items.BIRCH_LOG);
					output.accept(Items.BIRCH_WOOD);
					output.accept(Items.STRIPPED_BIRCH_LOG);
					output.accept(Items.STRIPPED_BIRCH_WOOD);
					output.accept(Items.BIRCH_PLANKS);
					output.accept(Items.BIRCH_STAIRS);
					output.accept(Items.BIRCH_SLAB);
					output.accept(Items.BIRCH_FENCE);
					output.accept(Items.BIRCH_FENCE_GATE);
					output.accept(Items.BIRCH_DOOR);
					output.accept(Items.BIRCH_TRAPDOOR);
					output.accept(Items.BIRCH_PRESSURE_PLATE);
					output.accept(Items.BIRCH_BUTTON);
					output.accept(Items.JUNGLE_LOG);
					output.accept(Items.JUNGLE_WOOD);
					output.accept(Items.STRIPPED_JUNGLE_LOG);
					output.accept(Items.STRIPPED_JUNGLE_WOOD);
					output.accept(Items.JUNGLE_PLANKS);
					output.accept(Items.JUNGLE_STAIRS);
					output.accept(Items.JUNGLE_SLAB);
					output.accept(Items.JUNGLE_FENCE);
					output.accept(Items.JUNGLE_FENCE_GATE);
					output.accept(Items.JUNGLE_DOOR);
					output.accept(Items.JUNGLE_TRAPDOOR);
					output.accept(Items.JUNGLE_PRESSURE_PLATE);
					output.accept(Items.JUNGLE_BUTTON);
					output.accept(Items.ACACIA_LOG);
					output.accept(Items.ACACIA_WOOD);
					output.accept(Items.STRIPPED_ACACIA_LOG);
					output.accept(Items.STRIPPED_ACACIA_WOOD);
					output.accept(Items.ACACIA_PLANKS);
					output.accept(Items.ACACIA_STAIRS);
					output.accept(Items.ACACIA_SLAB);
					output.accept(Items.ACACIA_FENCE);
					output.accept(Items.ACACIA_FENCE_GATE);
					output.accept(Items.ACACIA_DOOR);
					output.accept(Items.ACACIA_TRAPDOOR);
					output.accept(Items.ACACIA_PRESSURE_PLATE);
					output.accept(Items.ACACIA_BUTTON);
					output.accept(Items.DARK_OAK_LOG);
					output.accept(Items.DARK_OAK_WOOD);
					output.accept(Items.STRIPPED_DARK_OAK_LOG);
					output.accept(Items.STRIPPED_DARK_OAK_WOOD);
					output.accept(Items.DARK_OAK_PLANKS);
					output.accept(Items.DARK_OAK_STAIRS);
					output.accept(Items.DARK_OAK_SLAB);
					output.accept(Items.DARK_OAK_FENCE);
					output.accept(Items.DARK_OAK_FENCE_GATE);
					output.accept(Items.DARK_OAK_DOOR);
					output.accept(Items.DARK_OAK_TRAPDOOR);
					output.accept(Items.DARK_OAK_PRESSURE_PLATE);
					output.accept(Items.DARK_OAK_BUTTON);
					output.accept(Items.MANGROVE_LOG);
					output.accept(Items.MANGROVE_WOOD);
					output.accept(Items.STRIPPED_MANGROVE_LOG);
					output.accept(Items.STRIPPED_MANGROVE_WOOD);
					output.accept(Items.MANGROVE_PLANKS);
					output.accept(Items.MANGROVE_STAIRS);
					output.accept(Items.MANGROVE_SLAB);
					output.accept(Items.MANGROVE_FENCE);
					output.accept(Items.MANGROVE_FENCE_GATE);
					output.accept(Items.MANGROVE_DOOR);
					output.accept(Items.MANGROVE_TRAPDOOR);
					output.accept(Items.MANGROVE_PRESSURE_PLATE);
					output.accept(Items.MANGROVE_BUTTON);
					output.accept(Items.CHERRY_LOG);
					output.accept(Items.CHERRY_WOOD);
					output.accept(Items.STRIPPED_CHERRY_LOG);
					output.accept(Items.STRIPPED_CHERRY_WOOD);
					output.accept(Items.CHERRY_PLANKS);
					output.accept(Items.CHERRY_STAIRS);
					output.accept(Items.CHERRY_SLAB);
					output.accept(Items.CHERRY_FENCE);
					output.accept(Items.CHERRY_FENCE_GATE);
					output.accept(Items.CHERRY_DOOR);
					output.accept(Items.CHERRY_TRAPDOOR);
					output.accept(Items.CHERRY_PRESSURE_PLATE);
					output.accept(Items.CHERRY_BUTTON);
					output.accept(Items.BAMBOO_BLOCK);
					output.accept(Items.STRIPPED_BAMBOO_BLOCK);
					output.accept(Items.BAMBOO_PLANKS);
					output.accept(Items.BAMBOO_MOSAIC);
					output.accept(Items.BAMBOO_STAIRS);
					output.accept(Items.BAMBOO_MOSAIC_STAIRS);
					output.accept(Items.BAMBOO_SLAB);
					output.accept(Items.BAMBOO_MOSAIC_SLAB);
					output.accept(Items.BAMBOO_FENCE);
					output.accept(Items.BAMBOO_FENCE_GATE);
					output.accept(Items.BAMBOO_DOOR);
					output.accept(Items.BAMBOO_TRAPDOOR);
					output.accept(Items.BAMBOO_PRESSURE_PLATE);
					output.accept(Items.BAMBOO_BUTTON);
					output.accept(Items.CRIMSON_STEM);
					output.accept(Items.CRIMSON_HYPHAE);
					output.accept(Items.STRIPPED_CRIMSON_STEM);
					output.accept(Items.STRIPPED_CRIMSON_HYPHAE);
					output.accept(Items.CRIMSON_PLANKS);
					output.accept(Items.CRIMSON_STAIRS);
					output.accept(Items.CRIMSON_SLAB);
					output.accept(Items.CRIMSON_FENCE);
					output.accept(Items.CRIMSON_FENCE_GATE);
					output.accept(Items.CRIMSON_DOOR);
					output.accept(Items.CRIMSON_TRAPDOOR);
					output.accept(Items.CRIMSON_PRESSURE_PLATE);
					output.accept(Items.CRIMSON_BUTTON);
					output.accept(Items.WARPED_STEM);
					output.accept(Items.WARPED_HYPHAE);
					output.accept(Items.STRIPPED_WARPED_STEM);
					output.accept(Items.STRIPPED_WARPED_HYPHAE);
					output.accept(Items.WARPED_PLANKS);
					output.accept(Items.WARPED_STAIRS);
					output.accept(Items.WARPED_SLAB);
					output.accept(Items.WARPED_FENCE);
					output.accept(Items.WARPED_FENCE_GATE);
					output.accept(Items.WARPED_DOOR);
					output.accept(Items.WARPED_TRAPDOOR);
					output.accept(Items.WARPED_PRESSURE_PLATE);
					output.accept(Items.WARPED_BUTTON);
					output.accept(Items.STONE);
					output.accept(Items.STONE_STAIRS);
					output.accept(Items.STONE_SLAB);
					output.accept(Items.STONE_PRESSURE_PLATE);
					output.accept(Items.STONE_BUTTON);
					output.accept(Items.COBBLESTONE);
					output.accept(Items.COBBLESTONE_STAIRS);
					output.accept(Items.COBBLESTONE_SLAB);
					output.accept(Items.COBBLESTONE_WALL);
					output.accept(Items.MOSSY_COBBLESTONE);
					output.accept(Items.MOSSY_COBBLESTONE_STAIRS);
					output.accept(Items.MOSSY_COBBLESTONE_SLAB);
					output.accept(Items.MOSSY_COBBLESTONE_WALL);
					output.accept(Items.SMOOTH_STONE);
					output.accept(Items.SMOOTH_STONE_SLAB);
					output.accept(Items.STONE_BRICKS);
					output.accept(Items.CRACKED_STONE_BRICKS);
					output.accept(Items.STONE_BRICK_STAIRS);
					output.accept(Items.STONE_BRICK_SLAB);
					output.accept(Items.STONE_BRICK_WALL);
					output.accept(Items.CHISELED_STONE_BRICKS);
					output.accept(Items.MOSSY_STONE_BRICKS);
					output.accept(Items.MOSSY_STONE_BRICK_STAIRS);
					output.accept(Items.MOSSY_STONE_BRICK_SLAB);
					output.accept(Items.MOSSY_STONE_BRICK_WALL);
					output.accept(Items.GRANITE);
					output.accept(Items.GRANITE_STAIRS);
					output.accept(Items.GRANITE_SLAB);
					output.accept(Items.GRANITE_WALL);
					output.accept(Items.POLISHED_GRANITE);
					output.accept(Items.POLISHED_GRANITE_STAIRS);
					output.accept(Items.POLISHED_GRANITE_SLAB);
					output.accept(Items.DIORITE);
					output.accept(Items.DIORITE_STAIRS);
					output.accept(Items.DIORITE_SLAB);
					output.accept(Items.DIORITE_WALL);
					output.accept(Items.POLISHED_DIORITE);
					output.accept(Items.POLISHED_DIORITE_STAIRS);
					output.accept(Items.POLISHED_DIORITE_SLAB);
					output.accept(Items.ANDESITE);
					output.accept(Items.ANDESITE_STAIRS);
					output.accept(Items.ANDESITE_SLAB);
					output.accept(Items.ANDESITE_WALL);
					output.accept(Items.POLISHED_ANDESITE);
					output.accept(Items.POLISHED_ANDESITE_STAIRS);
					output.accept(Items.POLISHED_ANDESITE_SLAB);
					output.accept(Items.DEEPSLATE);
					output.accept(Items.COBBLED_DEEPSLATE);
					output.accept(Items.COBBLED_DEEPSLATE_STAIRS);
					output.accept(Items.COBBLED_DEEPSLATE_SLAB);
					output.accept(Items.COBBLED_DEEPSLATE_WALL);
					output.accept(Items.CHISELED_DEEPSLATE);
					output.accept(Items.POLISHED_DEEPSLATE);
					output.accept(Items.POLISHED_DEEPSLATE_STAIRS);
					output.accept(Items.POLISHED_DEEPSLATE_SLAB);
					output.accept(Items.POLISHED_DEEPSLATE_WALL);
					output.accept(Items.DEEPSLATE_BRICKS);
					output.accept(Items.CRACKED_DEEPSLATE_BRICKS);
					output.accept(Items.DEEPSLATE_BRICK_STAIRS);
					output.accept(Items.DEEPSLATE_BRICK_SLAB);
					output.accept(Items.DEEPSLATE_BRICK_WALL);
					output.accept(Items.DEEPSLATE_TILES);
					output.accept(Items.CRACKED_DEEPSLATE_TILES);
					output.accept(Items.DEEPSLATE_TILE_STAIRS);
					output.accept(Items.DEEPSLATE_TILE_SLAB);
					output.accept(Items.DEEPSLATE_TILE_WALL);
					output.accept(Items.REINFORCED_DEEPSLATE);
					output.accept(Items.TUFF);
					output.accept(Items.TUFF_STAIRS);
					output.accept(Items.TUFF_SLAB);
					output.accept(Items.TUFF_WALL);
					output.accept(Items.CHISELED_TUFF);
					output.accept(Items.POLISHED_TUFF);
					output.accept(Items.POLISHED_TUFF_STAIRS);
					output.accept(Items.POLISHED_TUFF_SLAB);
					output.accept(Items.POLISHED_TUFF_WALL);
					output.accept(Items.TUFF_BRICKS);
					output.accept(Items.TUFF_BRICK_STAIRS);
					output.accept(Items.TUFF_BRICK_SLAB);
					output.accept(Items.TUFF_BRICK_WALL);
					output.accept(Items.CHISELED_TUFF_BRICKS);
					output.accept(Items.BRICKS);
					output.accept(Items.BRICK_STAIRS);
					output.accept(Items.BRICK_SLAB);
					output.accept(Items.BRICK_WALL);
					output.accept(Items.PACKED_MUD);
					output.accept(Items.MUD_BRICKS);
					output.accept(Items.MUD_BRICK_STAIRS);
					output.accept(Items.MUD_BRICK_SLAB);
					output.accept(Items.MUD_BRICK_WALL);
					output.accept(Items.SANDSTONE);
					output.accept(Items.SANDSTONE_STAIRS);
					output.accept(Items.SANDSTONE_SLAB);
					output.accept(Items.SANDSTONE_WALL);
					output.accept(Items.CHISELED_SANDSTONE);
					output.accept(Items.SMOOTH_SANDSTONE);
					output.accept(Items.SMOOTH_SANDSTONE_STAIRS);
					output.accept(Items.SMOOTH_SANDSTONE_SLAB);
					output.accept(Items.CUT_SANDSTONE);
					output.accept(Items.CUT_STANDSTONE_SLAB);
					output.accept(Items.RED_SANDSTONE);
					output.accept(Items.RED_SANDSTONE_STAIRS);
					output.accept(Items.RED_SANDSTONE_SLAB);
					output.accept(Items.RED_SANDSTONE_WALL);
					output.accept(Items.CHISELED_RED_SANDSTONE);
					output.accept(Items.SMOOTH_RED_SANDSTONE);
					output.accept(Items.SMOOTH_RED_SANDSTONE_STAIRS);
					output.accept(Items.SMOOTH_RED_SANDSTONE_SLAB);
					output.accept(Items.CUT_RED_SANDSTONE);
					output.accept(Items.CUT_RED_SANDSTONE_SLAB);
					output.accept(Items.SEA_LANTERN);
					output.accept(Items.PRISMARINE);
					output.accept(Items.PRISMARINE_STAIRS);
					output.accept(Items.PRISMARINE_SLAB);
					output.accept(Items.PRISMARINE_WALL);
					output.accept(Items.PRISMARINE_BRICKS);
					output.accept(Items.PRISMARINE_BRICK_STAIRS);
					output.accept(Items.PRISMARINE_BRICK_SLAB);
					output.accept(Items.DARK_PRISMARINE);
					output.accept(Items.DARK_PRISMARINE_STAIRS);
					output.accept(Items.DARK_PRISMARINE_SLAB);
					output.accept(Items.NETHERRACK);
					output.accept(Items.NETHER_BRICKS);
					output.accept(Items.CRACKED_NETHER_BRICKS);
					output.accept(Items.NETHER_BRICK_STAIRS);
					output.accept(Items.NETHER_BRICK_SLAB);
					output.accept(Items.NETHER_BRICK_WALL);
					output.accept(Items.NETHER_BRICK_FENCE);
					output.accept(Items.CHISELED_NETHER_BRICKS);
					output.accept(Items.RED_NETHER_BRICKS);
					output.accept(Items.RED_NETHER_BRICK_STAIRS);
					output.accept(Items.RED_NETHER_BRICK_SLAB);
					output.accept(Items.RED_NETHER_BRICK_WALL);
					output.accept(Items.BASALT);
					output.accept(Items.SMOOTH_BASALT);
					output.accept(Items.POLISHED_BASALT);
					output.accept(Items.BLACKSTONE);
					output.accept(Items.GILDED_BLACKSTONE);
					output.accept(Items.BLACKSTONE_STAIRS);
					output.accept(Items.BLACKSTONE_SLAB);
					output.accept(Items.BLACKSTONE_WALL);
					output.accept(Items.CHISELED_POLISHED_BLACKSTONE);
					output.accept(Items.POLISHED_BLACKSTONE);
					output.accept(Items.POLISHED_BLACKSTONE_STAIRS);
					output.accept(Items.POLISHED_BLACKSTONE_SLAB);
					output.accept(Items.POLISHED_BLACKSTONE_WALL);
					output.accept(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE);
					output.accept(Items.POLISHED_BLACKSTONE_BUTTON);
					output.accept(Items.POLISHED_BLACKSTONE_BRICKS);
					output.accept(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS);
					output.accept(Items.POLISHED_BLACKSTONE_BRICK_STAIRS);
					output.accept(Items.POLISHED_BLACKSTONE_BRICK_SLAB);
					output.accept(Items.POLISHED_BLACKSTONE_BRICK_WALL);
					output.accept(Items.END_STONE);
					output.accept(Items.END_STONE_BRICKS);
					output.accept(Items.END_STONE_BRICK_STAIRS);
					output.accept(Items.END_STONE_BRICK_SLAB);
					output.accept(Items.END_STONE_BRICK_WALL);
					output.accept(Items.PURPUR_BLOCK);
					output.accept(Items.PURPUR_PILLAR);
					output.accept(Items.PURPUR_STAIRS);
					output.accept(Items.PURPUR_SLAB);
					output.accept(Items.COAL_BLOCK);
					output.accept(Items.IRON_BLOCK);
					output.accept(Items.IRON_BARS);
					output.accept(Items.IRON_DOOR);
					output.accept(Items.IRON_TRAPDOOR);
					output.accept(Items.HEAVY_WEIGHTED_PRESSURE_PLATE);
					output.accept(Items.CHAIN);
					output.accept(Items.GOLD_BLOCK);
					output.accept(Items.LIGHT_WEIGHTED_PRESSURE_PLATE);
					output.accept(Items.REDSTONE_BLOCK);
					output.accept(Items.EMERALD_BLOCK);
					output.accept(Items.LAPIS_BLOCK);
					output.accept(Items.DIAMOND_BLOCK);
					output.accept(Items.NETHERITE_BLOCK);
					output.accept(Items.QUARTZ_BLOCK);
					output.accept(Items.QUARTZ_STAIRS);
					output.accept(Items.QUARTZ_SLAB);
					output.accept(Items.CHISELED_QUARTZ_BLOCK);
					output.accept(Items.QUARTZ_BRICKS);
					output.accept(Items.QUARTZ_PILLAR);
					output.accept(Items.SMOOTH_QUARTZ);
					output.accept(Items.SMOOTH_QUARTZ_STAIRS);
					output.accept(Items.SMOOTH_QUARTZ_SLAB);
					output.accept(Items.AMETHYST_BLOCK);
					output.accept(Items.COPPER_BLOCK);
					output.accept(Items.CHISELED_COPPER);
					output.accept(Items.COPPER_GRATE);
					output.accept(Items.CUT_COPPER);
					output.accept(Items.CUT_COPPER_STAIRS);
					output.accept(Items.CUT_COPPER_SLAB);
					output.accept(Items.COPPER_DOOR);
					output.accept(Items.COPPER_TRAPDOOR);
					output.accept(Items.COPPER_BULB);
					output.accept(Items.EXPOSED_COPPER);
					output.accept(Items.EXPOSED_CHISELED_COPPER);
					output.accept(Items.EXPOSED_COPPER_GRATE);
					output.accept(Items.EXPOSED_CUT_COPPER);
					output.accept(Items.EXPOSED_CUT_COPPER_STAIRS);
					output.accept(Items.EXPOSED_CUT_COPPER_SLAB);
					output.accept(Items.EXPOSED_COPPER_DOOR);
					output.accept(Items.EXPOSED_COPPER_TRAPDOOR);
					output.accept(Items.EXPOSED_COPPER_BULB);
					output.accept(Items.WEATHERED_COPPER);
					output.accept(Items.WEATHERED_CHISELED_COPPER);
					output.accept(Items.WEATHERED_COPPER_GRATE);
					output.accept(Items.WEATHERED_CUT_COPPER);
					output.accept(Items.WEATHERED_CUT_COPPER_STAIRS);
					output.accept(Items.WEATHERED_CUT_COPPER_SLAB);
					output.accept(Items.WEATHERED_COPPER_DOOR);
					output.accept(Items.WEATHERED_COPPER_TRAPDOOR);
					output.accept(Items.WEATHERED_COPPER_BULB);
					output.accept(Items.OXIDIZED_COPPER);
					output.accept(Items.OXIDIZED_CHISELED_COPPER);
					output.accept(Items.OXIDIZED_COPPER_GRATE);
					output.accept(Items.OXIDIZED_CUT_COPPER);
					output.accept(Items.OXIDIZED_CUT_COPPER_STAIRS);
					output.accept(Items.OXIDIZED_CUT_COPPER_SLAB);
					output.accept(Items.OXIDIZED_COPPER_DOOR);
					output.accept(Items.OXIDIZED_COPPER_TRAPDOOR);
					output.accept(Items.OXIDIZED_COPPER_BULB);
					output.accept(Items.WAXED_COPPER_BLOCK);
					output.accept(Items.WAXED_CHISELED_COPPER);
					output.accept(Items.WAXED_COPPER_GRATE);
					output.accept(Items.WAXED_CUT_COPPER);
					output.accept(Items.WAXED_CUT_COPPER_STAIRS);
					output.accept(Items.WAXED_CUT_COPPER_SLAB);
					output.accept(Items.WAXED_COPPER_DOOR);
					output.accept(Items.WAXED_COPPER_TRAPDOOR);
					output.accept(Items.WAXED_COPPER_BULB);
					output.accept(Items.WAXED_EXPOSED_COPPER);
					output.accept(Items.WAXED_EXPOSED_CHISELED_COPPER);
					output.accept(Items.WAXED_EXPOSED_COPPER_GRATE);
					output.accept(Items.WAXED_EXPOSED_CUT_COPPER);
					output.accept(Items.WAXED_EXPOSED_CUT_COPPER_STAIRS);
					output.accept(Items.WAXED_EXPOSED_CUT_COPPER_SLAB);
					output.accept(Items.WAXED_EXPOSED_COPPER_DOOR);
					output.accept(Items.WAXED_EXPOSED_COPPER_TRAPDOOR);
					output.accept(Items.WAXED_EXPOSED_COPPER_BULB);
					output.accept(Items.WAXED_WEATHERED_COPPER);
					output.accept(Items.WAXED_WEATHERED_CHISELED_COPPER);
					output.accept(Items.WAXED_WEATHERED_COPPER_GRATE);
					output.accept(Items.WAXED_WEATHERED_CUT_COPPER);
					output.accept(Items.WAXED_WEATHERED_CUT_COPPER_STAIRS);
					output.accept(Items.WAXED_WEATHERED_CUT_COPPER_SLAB);
					output.accept(Items.WAXED_WEATHERED_COPPER_DOOR);
					output.accept(Items.WAXED_WEATHERED_COPPER_TRAPDOOR);
					output.accept(Items.WAXED_WEATHERED_COPPER_BULB);
					output.accept(Items.WAXED_OXIDIZED_COPPER);
					output.accept(Items.WAXED_OXIDIZED_CHISELED_COPPER);
					output.accept(Items.WAXED_OXIDIZED_COPPER_GRATE);
					output.accept(Items.WAXED_OXIDIZED_CUT_COPPER);
					output.accept(Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS);
					output.accept(Items.WAXED_OXIDIZED_CUT_COPPER_SLAB);
					output.accept(Items.WAXED_OXIDIZED_COPPER_DOOR);
					output.accept(Items.WAXED_OXIDIZED_COPPER_TRAPDOOR);
					output.accept(Items.WAXED_OXIDIZED_COPPER_BULB);
				})
				.build()
		);
		Registry.register(
			registry,
			COLORED_BLOCKS,
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
				.title(Component.translatable("itemGroup.coloredBlocks"))
				.icon(() -> new ItemStack(Blocks.CYAN_WOOL))
				.displayItems((itemDisplayParameters, output) -> {
					output.accept(Items.WHITE_WOOL);
					output.accept(Items.LIGHT_GRAY_WOOL);
					output.accept(Items.GRAY_WOOL);
					output.accept(Items.BLACK_WOOL);
					output.accept(Items.BROWN_WOOL);
					output.accept(Items.RED_WOOL);
					output.accept(Items.ORANGE_WOOL);
					output.accept(Items.YELLOW_WOOL);
					output.accept(Items.LIME_WOOL);
					output.accept(Items.GREEN_WOOL);
					output.accept(Items.CYAN_WOOL);
					output.accept(Items.LIGHT_BLUE_WOOL);
					output.accept(Items.BLUE_WOOL);
					output.accept(Items.PURPLE_WOOL);
					output.accept(Items.MAGENTA_WOOL);
					output.accept(Items.PINK_WOOL);
					output.accept(Items.WHITE_CARPET);
					output.accept(Items.LIGHT_GRAY_CARPET);
					output.accept(Items.GRAY_CARPET);
					output.accept(Items.BLACK_CARPET);
					output.accept(Items.BROWN_CARPET);
					output.accept(Items.RED_CARPET);
					output.accept(Items.ORANGE_CARPET);
					output.accept(Items.YELLOW_CARPET);
					output.accept(Items.LIME_CARPET);
					output.accept(Items.GREEN_CARPET);
					output.accept(Items.CYAN_CARPET);
					output.accept(Items.LIGHT_BLUE_CARPET);
					output.accept(Items.BLUE_CARPET);
					output.accept(Items.PURPLE_CARPET);
					output.accept(Items.MAGENTA_CARPET);
					output.accept(Items.PINK_CARPET);
					output.accept(Items.TERRACOTTA);
					output.accept(Items.WHITE_TERRACOTTA);
					output.accept(Items.LIGHT_GRAY_TERRACOTTA);
					output.accept(Items.GRAY_TERRACOTTA);
					output.accept(Items.BLACK_TERRACOTTA);
					output.accept(Items.BROWN_TERRACOTTA);
					output.accept(Items.RED_TERRACOTTA);
					output.accept(Items.ORANGE_TERRACOTTA);
					output.accept(Items.YELLOW_TERRACOTTA);
					output.accept(Items.LIME_TERRACOTTA);
					output.accept(Items.GREEN_TERRACOTTA);
					output.accept(Items.CYAN_TERRACOTTA);
					output.accept(Items.LIGHT_BLUE_TERRACOTTA);
					output.accept(Items.BLUE_TERRACOTTA);
					output.accept(Items.PURPLE_TERRACOTTA);
					output.accept(Items.MAGENTA_TERRACOTTA);
					output.accept(Items.PINK_TERRACOTTA);
					output.accept(Items.WHITE_CONCRETE);
					output.accept(Items.LIGHT_GRAY_CONCRETE);
					output.accept(Items.GRAY_CONCRETE);
					output.accept(Items.BLACK_CONCRETE);
					output.accept(Items.BROWN_CONCRETE);
					output.accept(Items.RED_CONCRETE);
					output.accept(Items.ORANGE_CONCRETE);
					output.accept(Items.YELLOW_CONCRETE);
					output.accept(Items.LIME_CONCRETE);
					output.accept(Items.GREEN_CONCRETE);
					output.accept(Items.CYAN_CONCRETE);
					output.accept(Items.LIGHT_BLUE_CONCRETE);
					output.accept(Items.BLUE_CONCRETE);
					output.accept(Items.PURPLE_CONCRETE);
					output.accept(Items.MAGENTA_CONCRETE);
					output.accept(Items.PINK_CONCRETE);
					output.accept(Items.WHITE_CONCRETE_POWDER);
					output.accept(Items.LIGHT_GRAY_CONCRETE_POWDER);
					output.accept(Items.GRAY_CONCRETE_POWDER);
					output.accept(Items.BLACK_CONCRETE_POWDER);
					output.accept(Items.BROWN_CONCRETE_POWDER);
					output.accept(Items.RED_CONCRETE_POWDER);
					output.accept(Items.ORANGE_CONCRETE_POWDER);
					output.accept(Items.YELLOW_CONCRETE_POWDER);
					output.accept(Items.LIME_CONCRETE_POWDER);
					output.accept(Items.GREEN_CONCRETE_POWDER);
					output.accept(Items.CYAN_CONCRETE_POWDER);
					output.accept(Items.LIGHT_BLUE_CONCRETE_POWDER);
					output.accept(Items.BLUE_CONCRETE_POWDER);
					output.accept(Items.PURPLE_CONCRETE_POWDER);
					output.accept(Items.MAGENTA_CONCRETE_POWDER);
					output.accept(Items.PINK_CONCRETE_POWDER);
					output.accept(Items.WHITE_GLAZED_TERRACOTTA);
					output.accept(Items.LIGHT_GRAY_GLAZED_TERRACOTTA);
					output.accept(Items.GRAY_GLAZED_TERRACOTTA);
					output.accept(Items.BLACK_GLAZED_TERRACOTTA);
					output.accept(Items.BROWN_GLAZED_TERRACOTTA);
					output.accept(Items.RED_GLAZED_TERRACOTTA);
					output.accept(Items.ORANGE_GLAZED_TERRACOTTA);
					output.accept(Items.YELLOW_GLAZED_TERRACOTTA);
					output.accept(Items.LIME_GLAZED_TERRACOTTA);
					output.accept(Items.GREEN_GLAZED_TERRACOTTA);
					output.accept(Items.CYAN_GLAZED_TERRACOTTA);
					output.accept(Items.LIGHT_BLUE_GLAZED_TERRACOTTA);
					output.accept(Items.BLUE_GLAZED_TERRACOTTA);
					output.accept(Items.PURPLE_GLAZED_TERRACOTTA);
					output.accept(Items.MAGENTA_GLAZED_TERRACOTTA);
					output.accept(Items.PINK_GLAZED_TERRACOTTA);
					output.accept(Items.GLASS);
					output.accept(Items.TINTED_GLASS);
					output.accept(Items.WHITE_STAINED_GLASS);
					output.accept(Items.LIGHT_GRAY_STAINED_GLASS);
					output.accept(Items.GRAY_STAINED_GLASS);
					output.accept(Items.BLACK_STAINED_GLASS);
					output.accept(Items.BROWN_STAINED_GLASS);
					output.accept(Items.RED_STAINED_GLASS);
					output.accept(Items.ORANGE_STAINED_GLASS);
					output.accept(Items.YELLOW_STAINED_GLASS);
					output.accept(Items.LIME_STAINED_GLASS);
					output.accept(Items.GREEN_STAINED_GLASS);
					output.accept(Items.CYAN_STAINED_GLASS);
					output.accept(Items.LIGHT_BLUE_STAINED_GLASS);
					output.accept(Items.BLUE_STAINED_GLASS);
					output.accept(Items.PURPLE_STAINED_GLASS);
					output.accept(Items.MAGENTA_STAINED_GLASS);
					output.accept(Items.PINK_STAINED_GLASS);
					output.accept(Items.GLASS_PANE);
					output.accept(Items.WHITE_STAINED_GLASS_PANE);
					output.accept(Items.LIGHT_GRAY_STAINED_GLASS_PANE);
					output.accept(Items.GRAY_STAINED_GLASS_PANE);
					output.accept(Items.BLACK_STAINED_GLASS_PANE);
					output.accept(Items.BROWN_STAINED_GLASS_PANE);
					output.accept(Items.RED_STAINED_GLASS_PANE);
					output.accept(Items.ORANGE_STAINED_GLASS_PANE);
					output.accept(Items.YELLOW_STAINED_GLASS_PANE);
					output.accept(Items.LIME_STAINED_GLASS_PANE);
					output.accept(Items.GREEN_STAINED_GLASS_PANE);
					output.accept(Items.CYAN_STAINED_GLASS_PANE);
					output.accept(Items.LIGHT_BLUE_STAINED_GLASS_PANE);
					output.accept(Items.BLUE_STAINED_GLASS_PANE);
					output.accept(Items.PURPLE_STAINED_GLASS_PANE);
					output.accept(Items.MAGENTA_STAINED_GLASS_PANE);
					output.accept(Items.PINK_STAINED_GLASS_PANE);
					output.accept(Items.SHULKER_BOX);
					output.accept(Items.WHITE_SHULKER_BOX);
					output.accept(Items.LIGHT_GRAY_SHULKER_BOX);
					output.accept(Items.GRAY_SHULKER_BOX);
					output.accept(Items.BLACK_SHULKER_BOX);
					output.accept(Items.BROWN_SHULKER_BOX);
					output.accept(Items.RED_SHULKER_BOX);
					output.accept(Items.ORANGE_SHULKER_BOX);
					output.accept(Items.YELLOW_SHULKER_BOX);
					output.accept(Items.LIME_SHULKER_BOX);
					output.accept(Items.GREEN_SHULKER_BOX);
					output.accept(Items.CYAN_SHULKER_BOX);
					output.accept(Items.LIGHT_BLUE_SHULKER_BOX);
					output.accept(Items.BLUE_SHULKER_BOX);
					output.accept(Items.PURPLE_SHULKER_BOX);
					output.accept(Items.MAGENTA_SHULKER_BOX);
					output.accept(Items.PINK_SHULKER_BOX);
					output.accept(Items.WHITE_BED);
					output.accept(Items.LIGHT_GRAY_BED);
					output.accept(Items.GRAY_BED);
					output.accept(Items.BLACK_BED);
					output.accept(Items.BROWN_BED);
					output.accept(Items.RED_BED);
					output.accept(Items.ORANGE_BED);
					output.accept(Items.YELLOW_BED);
					output.accept(Items.LIME_BED);
					output.accept(Items.GREEN_BED);
					output.accept(Items.CYAN_BED);
					output.accept(Items.LIGHT_BLUE_BED);
					output.accept(Items.BLUE_BED);
					output.accept(Items.PURPLE_BED);
					output.accept(Items.MAGENTA_BED);
					output.accept(Items.PINK_BED);
					output.accept(Items.CANDLE);
					output.accept(Items.WHITE_CANDLE);
					output.accept(Items.LIGHT_GRAY_CANDLE);
					output.accept(Items.GRAY_CANDLE);
					output.accept(Items.BLACK_CANDLE);
					output.accept(Items.BROWN_CANDLE);
					output.accept(Items.RED_CANDLE);
					output.accept(Items.ORANGE_CANDLE);
					output.accept(Items.YELLOW_CANDLE);
					output.accept(Items.LIME_CANDLE);
					output.accept(Items.GREEN_CANDLE);
					output.accept(Items.CYAN_CANDLE);
					output.accept(Items.LIGHT_BLUE_CANDLE);
					output.accept(Items.BLUE_CANDLE);
					output.accept(Items.PURPLE_CANDLE);
					output.accept(Items.MAGENTA_CANDLE);
					output.accept(Items.PINK_CANDLE);
					output.accept(Items.WHITE_BANNER);
					output.accept(Items.LIGHT_GRAY_BANNER);
					output.accept(Items.GRAY_BANNER);
					output.accept(Items.BLACK_BANNER);
					output.accept(Items.BROWN_BANNER);
					output.accept(Items.RED_BANNER);
					output.accept(Items.ORANGE_BANNER);
					output.accept(Items.YELLOW_BANNER);
					output.accept(Items.LIME_BANNER);
					output.accept(Items.GREEN_BANNER);
					output.accept(Items.CYAN_BANNER);
					output.accept(Items.LIGHT_BLUE_BANNER);
					output.accept(Items.BLUE_BANNER);
					output.accept(Items.PURPLE_BANNER);
					output.accept(Items.MAGENTA_BANNER);
					output.accept(Items.PINK_BANNER);
				})
				.build()
		);
		Registry.register(
			registry,
			NATURAL_BLOCKS,
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 2)
				.title(Component.translatable("itemGroup.natural"))
				.icon(() -> new ItemStack(Blocks.GRASS_BLOCK))
				.displayItems((itemDisplayParameters, output) -> {
					output.accept(Items.GRASS_BLOCK);
					output.accept(Items.PODZOL);
					output.accept(Items.MYCELIUM);
					output.accept(Items.DIRT_PATH);
					output.accept(Items.DIRT);
					output.accept(Items.COARSE_DIRT);
					output.accept(Items.ROOTED_DIRT);
					output.accept(Items.FARMLAND);
					output.accept(Items.MUD);
					output.accept(Items.CLAY);
					output.accept(Items.GRAVEL);
					output.accept(Items.SAND);
					output.accept(Items.SANDSTONE);
					output.accept(Items.RED_SAND);
					output.accept(Items.RED_SANDSTONE);
					output.accept(Items.ICE);
					output.accept(Items.PACKED_ICE);
					output.accept(Items.BLUE_ICE);
					output.accept(Items.SNOW_BLOCK);
					output.accept(Items.SNOW);
					output.accept(Items.MOSS_BLOCK);
					output.accept(Items.MOSS_CARPET);
					output.accept(Items.STONE);
					output.accept(Items.DEEPSLATE);
					output.accept(Items.GRANITE);
					output.accept(Items.DIORITE);
					output.accept(Items.ANDESITE);
					output.accept(Items.CALCITE);
					output.accept(Items.TUFF);
					output.accept(Items.DRIPSTONE_BLOCK);
					output.accept(Items.POINTED_DRIPSTONE);
					output.accept(Items.PRISMARINE);
					output.accept(Items.MAGMA_BLOCK);
					output.accept(Items.OBSIDIAN);
					output.accept(Items.CRYING_OBSIDIAN);
					output.accept(Items.NETHERRACK);
					output.accept(Items.CRIMSON_NYLIUM);
					output.accept(Items.WARPED_NYLIUM);
					output.accept(Items.SOUL_SAND);
					output.accept(Items.SOUL_SOIL);
					output.accept(Items.BONE_BLOCK);
					output.accept(Items.BLACKSTONE);
					output.accept(Items.BASALT);
					output.accept(Items.SMOOTH_BASALT);
					output.accept(Items.END_STONE);
					output.accept(Items.COAL_ORE);
					output.accept(Items.DEEPSLATE_COAL_ORE);
					output.accept(Items.IRON_ORE);
					output.accept(Items.DEEPSLATE_IRON_ORE);
					output.accept(Items.COPPER_ORE);
					output.accept(Items.DEEPSLATE_COPPER_ORE);
					output.accept(Items.GOLD_ORE);
					output.accept(Items.DEEPSLATE_GOLD_ORE);
					output.accept(Items.REDSTONE_ORE);
					output.accept(Items.DEEPSLATE_REDSTONE_ORE);
					output.accept(Items.EMERALD_ORE);
					output.accept(Items.DEEPSLATE_EMERALD_ORE);
					output.accept(Items.LAPIS_ORE);
					output.accept(Items.DEEPSLATE_LAPIS_ORE);
					output.accept(Items.DIAMOND_ORE);
					output.accept(Items.DEEPSLATE_DIAMOND_ORE);
					output.accept(Items.NETHER_GOLD_ORE);
					output.accept(Items.NETHER_QUARTZ_ORE);
					output.accept(Items.ANCIENT_DEBRIS);
					output.accept(Items.RAW_IRON_BLOCK);
					output.accept(Items.RAW_COPPER_BLOCK);
					output.accept(Items.RAW_GOLD_BLOCK);
					output.accept(Items.GLOWSTONE);
					output.accept(Items.AMETHYST_BLOCK);
					output.accept(Items.BUDDING_AMETHYST);
					output.accept(Items.SMALL_AMETHYST_BUD);
					output.accept(Items.MEDIUM_AMETHYST_BUD);
					output.accept(Items.LARGE_AMETHYST_BUD);
					output.accept(Items.AMETHYST_CLUSTER);
					output.accept(Items.OAK_LOG);
					output.accept(Items.SPRUCE_LOG);
					output.accept(Items.BIRCH_LOG);
					output.accept(Items.JUNGLE_LOG);
					output.accept(Items.ACACIA_LOG);
					output.accept(Items.DARK_OAK_LOG);
					output.accept(Items.MANGROVE_LOG);
					output.accept(Items.MANGROVE_ROOTS);
					output.accept(Items.MUDDY_MANGROVE_ROOTS);
					output.accept(Items.CHERRY_LOG);
					output.accept(Items.MUSHROOM_STEM);
					output.accept(Items.CRIMSON_STEM);
					output.accept(Items.WARPED_STEM);
					output.accept(Items.OAK_LEAVES);
					output.accept(Items.SPRUCE_LEAVES);
					output.accept(Items.BIRCH_LEAVES);
					output.accept(Items.JUNGLE_LEAVES);
					output.accept(Items.ACACIA_LEAVES);
					output.accept(Items.DARK_OAK_LEAVES);
					output.accept(Items.MANGROVE_LEAVES);
					output.accept(Items.CHERRY_LEAVES);
					output.accept(Items.AZALEA_LEAVES);
					output.accept(Items.FLOWERING_AZALEA_LEAVES);
					output.accept(Items.BROWN_MUSHROOM_BLOCK);
					output.accept(Items.RED_MUSHROOM_BLOCK);
					output.accept(Items.NETHER_WART_BLOCK);
					output.accept(Items.WARPED_WART_BLOCK);
					output.accept(Items.SHROOMLIGHT);
					output.accept(Items.OAK_SAPLING);
					output.accept(Items.SPRUCE_SAPLING);
					output.accept(Items.BIRCH_SAPLING);
					output.accept(Items.JUNGLE_SAPLING);
					output.accept(Items.ACACIA_SAPLING);
					output.accept(Items.DARK_OAK_SAPLING);
					output.accept(Items.MANGROVE_PROPAGULE);
					output.accept(Items.CHERRY_SAPLING);
					output.accept(Items.AZALEA);
					output.accept(Items.FLOWERING_AZALEA);
					output.accept(Items.BROWN_MUSHROOM);
					output.accept(Items.RED_MUSHROOM);
					output.accept(Items.CRIMSON_FUNGUS);
					output.accept(Items.WARPED_FUNGUS);
					output.accept(Items.SHORT_GRASS);
					output.accept(Items.FERN);
					output.accept(Items.DEAD_BUSH);
					output.accept(Items.DANDELION);
					output.accept(Items.POPPY);
					output.accept(Items.BLUE_ORCHID);
					output.accept(Items.ALLIUM);
					output.accept(Items.AZURE_BLUET);
					output.accept(Items.RED_TULIP);
					output.accept(Items.ORANGE_TULIP);
					output.accept(Items.WHITE_TULIP);
					output.accept(Items.PINK_TULIP);
					output.accept(Items.OXEYE_DAISY);
					output.accept(Items.CORNFLOWER);
					output.accept(Items.LILY_OF_THE_VALLEY);
					output.accept(Items.TORCHFLOWER);
					output.accept(Items.WITHER_ROSE);
					output.accept(Items.PINK_PETALS);
					output.accept(Items.SPORE_BLOSSOM);
					output.accept(Items.BAMBOO);
					output.accept(Items.SUGAR_CANE);
					output.accept(Items.CACTUS);
					output.accept(Items.CRIMSON_ROOTS);
					output.accept(Items.WARPED_ROOTS);
					output.accept(Items.NETHER_SPROUTS);
					output.accept(Items.WEEPING_VINES);
					output.accept(Items.TWISTING_VINES);
					output.accept(Items.VINE);
					output.accept(Items.TALL_GRASS);
					output.accept(Items.LARGE_FERN);
					output.accept(Items.SUNFLOWER);
					output.accept(Items.LILAC);
					output.accept(Items.ROSE_BUSH);
					output.accept(Items.PEONY);
					output.accept(Items.PITCHER_PLANT);
					output.accept(Items.BIG_DRIPLEAF);
					output.accept(Items.SMALL_DRIPLEAF);
					output.accept(Items.CHORUS_PLANT);
					output.accept(Items.CHORUS_FLOWER);
					output.accept(Items.GLOW_LICHEN);
					output.accept(Items.HANGING_ROOTS);
					output.accept(Items.FROGSPAWN);
					output.accept(Items.TURTLE_EGG);
					output.accept(Items.SNIFFER_EGG);
					output.accept(Items.WHEAT_SEEDS);
					output.accept(Items.COCOA_BEANS);
					output.accept(Items.PUMPKIN_SEEDS);
					output.accept(Items.MELON_SEEDS);
					output.accept(Items.BEETROOT_SEEDS);
					output.accept(Items.TORCHFLOWER_SEEDS);
					output.accept(Items.PITCHER_POD);
					output.accept(Items.GLOW_BERRIES);
					output.accept(Items.SWEET_BERRIES);
					output.accept(Items.NETHER_WART);
					output.accept(Items.LILY_PAD);
					output.accept(Items.SEAGRASS);
					output.accept(Items.SEA_PICKLE);
					output.accept(Items.KELP);
					output.accept(Items.DRIED_KELP_BLOCK);
					output.accept(Items.TUBE_CORAL_BLOCK);
					output.accept(Items.BRAIN_CORAL_BLOCK);
					output.accept(Items.BUBBLE_CORAL_BLOCK);
					output.accept(Items.FIRE_CORAL_BLOCK);
					output.accept(Items.HORN_CORAL_BLOCK);
					output.accept(Items.DEAD_TUBE_CORAL_BLOCK);
					output.accept(Items.DEAD_BRAIN_CORAL_BLOCK);
					output.accept(Items.DEAD_BUBBLE_CORAL_BLOCK);
					output.accept(Items.DEAD_FIRE_CORAL_BLOCK);
					output.accept(Items.DEAD_HORN_CORAL_BLOCK);
					output.accept(Items.TUBE_CORAL);
					output.accept(Items.BRAIN_CORAL);
					output.accept(Items.BUBBLE_CORAL);
					output.accept(Items.FIRE_CORAL);
					output.accept(Items.HORN_CORAL);
					output.accept(Items.DEAD_TUBE_CORAL);
					output.accept(Items.DEAD_BRAIN_CORAL);
					output.accept(Items.DEAD_BUBBLE_CORAL);
					output.accept(Items.DEAD_FIRE_CORAL);
					output.accept(Items.DEAD_HORN_CORAL);
					output.accept(Items.TUBE_CORAL_FAN);
					output.accept(Items.BRAIN_CORAL_FAN);
					output.accept(Items.BUBBLE_CORAL_FAN);
					output.accept(Items.FIRE_CORAL_FAN);
					output.accept(Items.HORN_CORAL_FAN);
					output.accept(Items.DEAD_TUBE_CORAL_FAN);
					output.accept(Items.DEAD_BRAIN_CORAL_FAN);
					output.accept(Items.DEAD_BUBBLE_CORAL_FAN);
					output.accept(Items.DEAD_FIRE_CORAL_FAN);
					output.accept(Items.DEAD_HORN_CORAL_FAN);
					output.accept(Items.SPONGE);
					output.accept(Items.WET_SPONGE);
					output.accept(Items.MELON);
					output.accept(Items.PUMPKIN);
					output.accept(Items.CARVED_PUMPKIN);
					output.accept(Items.JACK_O_LANTERN);
					output.accept(Items.HAY_BLOCK);
					output.accept(Items.BEE_NEST);
					output.accept(Items.HONEYCOMB_BLOCK);
					output.accept(Items.SLIME_BLOCK);
					output.accept(Items.HONEY_BLOCK);
					output.accept(Items.OCHRE_FROGLIGHT);
					output.accept(Items.VERDANT_FROGLIGHT);
					output.accept(Items.PEARLESCENT_FROGLIGHT);
					output.accept(Items.SCULK);
					output.accept(Items.SCULK_VEIN);
					output.accept(Items.SCULK_CATALYST);
					output.accept(Items.SCULK_SHRIEKER);
					output.accept(Items.SCULK_SENSOR);
					output.accept(Items.COBWEB);
					output.accept(Items.BEDROCK);
				})
				.build()
		);
		Registry.register(
			registry,
			FUNCTIONAL_BLOCKS,
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 3)
				.title(Component.translatable("itemGroup.functional"))
				.icon(() -> new ItemStack(Items.OAK_SIGN))
				.displayItems(
					(itemDisplayParameters, output) -> {
						output.accept(Items.TORCH);
						output.accept(Items.SOUL_TORCH);
						output.accept(Items.REDSTONE_TORCH);
						output.accept(Items.LANTERN);
						output.accept(Items.SOUL_LANTERN);
						output.accept(Items.CHAIN);
						output.accept(Items.END_ROD);
						output.accept(Items.SEA_LANTERN);
						output.accept(Items.REDSTONE_LAMP);
						output.accept(Items.WAXED_COPPER_BULB);
						output.accept(Items.WAXED_EXPOSED_COPPER_BULB);
						output.accept(Items.WAXED_WEATHERED_COPPER_BULB);
						output.accept(Items.WAXED_OXIDIZED_COPPER_BULB);
						output.accept(Items.GLOWSTONE);
						output.accept(Items.SHROOMLIGHT);
						output.accept(Items.OCHRE_FROGLIGHT);
						output.accept(Items.VERDANT_FROGLIGHT);
						output.accept(Items.PEARLESCENT_FROGLIGHT);
						output.accept(Items.CRYING_OBSIDIAN);
						output.accept(Items.GLOW_LICHEN);
						output.accept(Items.MAGMA_BLOCK);
						output.accept(Items.CRAFTING_TABLE);
						output.accept(Items.STONECUTTER);
						output.accept(Items.CARTOGRAPHY_TABLE);
						output.accept(Items.FLETCHING_TABLE);
						output.accept(Items.SMITHING_TABLE);
						output.accept(Items.GRINDSTONE);
						output.accept(Items.LOOM);
						output.accept(Items.FURNACE);
						output.accept(Items.SMOKER);
						output.accept(Items.BLAST_FURNACE);
						output.accept(Items.CAMPFIRE);
						output.accept(Items.SOUL_CAMPFIRE);
						output.accept(Items.ANVIL);
						output.accept(Items.CHIPPED_ANVIL);
						output.accept(Items.DAMAGED_ANVIL);
						output.accept(Items.COMPOSTER);
						output.accept(Items.NOTE_BLOCK);
						output.accept(Items.JUKEBOX);
						output.accept(Items.ENCHANTING_TABLE);
						output.accept(Items.END_CRYSTAL);
						output.accept(Items.BREWING_STAND);
						output.accept(Items.CAULDRON);
						output.accept(Items.BELL);
						output.accept(Items.BEACON);
						output.accept(Items.CONDUIT);
						output.accept(Items.LODESTONE);
						output.accept(Items.LADDER);
						output.accept(Items.SCAFFOLDING);
						output.accept(Items.BEE_NEST);
						output.accept(Items.BEEHIVE);
						output.accept(Items.SUSPICIOUS_SAND);
						output.accept(Items.SUSPICIOUS_GRAVEL);
						output.accept(Items.LIGHTNING_ROD);
						output.accept(Items.FLOWER_POT);
						output.accept(Items.DECORATED_POT);
						output.accept(Items.ARMOR_STAND);
						output.accept(Items.ITEM_FRAME);
						output.accept(Items.GLOW_ITEM_FRAME);
						output.accept(Items.PAINTING);
						itemDisplayParameters.holders()
							.lookup(Registries.PAINTING_VARIANT)
							.ifPresent(
								registryLookup -> generatePresetPaintings(
										output, registryLookup, holder -> holder.is(PaintingVariantTags.PLACEABLE), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
									)
							);
						output.accept(Items.BOOKSHELF);
						output.accept(Items.CHISELED_BOOKSHELF);
						output.accept(Items.LECTERN);
						output.accept(Items.TINTED_GLASS);
						output.accept(Items.OAK_SIGN);
						output.accept(Items.OAK_HANGING_SIGN);
						output.accept(Items.SPRUCE_SIGN);
						output.accept(Items.SPRUCE_HANGING_SIGN);
						output.accept(Items.BIRCH_SIGN);
						output.accept(Items.BIRCH_HANGING_SIGN);
						output.accept(Items.JUNGLE_SIGN);
						output.accept(Items.JUNGLE_HANGING_SIGN);
						output.accept(Items.ACACIA_SIGN);
						output.accept(Items.ACACIA_HANGING_SIGN);
						output.accept(Items.DARK_OAK_SIGN);
						output.accept(Items.DARK_OAK_HANGING_SIGN);
						output.accept(Items.MANGROVE_SIGN);
						output.accept(Items.MANGROVE_HANGING_SIGN);
						output.accept(Items.CHERRY_SIGN);
						output.accept(Items.CHERRY_HANGING_SIGN);
						output.accept(Items.BAMBOO_SIGN);
						output.accept(Items.BAMBOO_HANGING_SIGN);
						output.accept(Items.CRIMSON_SIGN);
						output.accept(Items.CRIMSON_HANGING_SIGN);
						output.accept(Items.WARPED_SIGN);
						output.accept(Items.WARPED_HANGING_SIGN);
						output.accept(Items.CHEST);
						output.accept(Items.BARREL);
						output.accept(Items.ENDER_CHEST);
						output.accept(Items.SHULKER_BOX);
						output.accept(Items.WHITE_SHULKER_BOX);
						output.accept(Items.LIGHT_GRAY_SHULKER_BOX);
						output.accept(Items.GRAY_SHULKER_BOX);
						output.accept(Items.BLACK_SHULKER_BOX);
						output.accept(Items.BROWN_SHULKER_BOX);
						output.accept(Items.RED_SHULKER_BOX);
						output.accept(Items.ORANGE_SHULKER_BOX);
						output.accept(Items.YELLOW_SHULKER_BOX);
						output.accept(Items.LIME_SHULKER_BOX);
						output.accept(Items.GREEN_SHULKER_BOX);
						output.accept(Items.CYAN_SHULKER_BOX);
						output.accept(Items.LIGHT_BLUE_SHULKER_BOX);
						output.accept(Items.BLUE_SHULKER_BOX);
						output.accept(Items.PURPLE_SHULKER_BOX);
						output.accept(Items.MAGENTA_SHULKER_BOX);
						output.accept(Items.PINK_SHULKER_BOX);
						output.accept(Items.RESPAWN_ANCHOR);
						output.accept(Items.WHITE_BED);
						output.accept(Items.LIGHT_GRAY_BED);
						output.accept(Items.GRAY_BED);
						output.accept(Items.BLACK_BED);
						output.accept(Items.BROWN_BED);
						output.accept(Items.RED_BED);
						output.accept(Items.ORANGE_BED);
						output.accept(Items.YELLOW_BED);
						output.accept(Items.LIME_BED);
						output.accept(Items.GREEN_BED);
						output.accept(Items.CYAN_BED);
						output.accept(Items.LIGHT_BLUE_BED);
						output.accept(Items.BLUE_BED);
						output.accept(Items.PURPLE_BED);
						output.accept(Items.MAGENTA_BED);
						output.accept(Items.PINK_BED);
						output.accept(Items.CANDLE);
						output.accept(Items.WHITE_CANDLE);
						output.accept(Items.LIGHT_GRAY_CANDLE);
						output.accept(Items.GRAY_CANDLE);
						output.accept(Items.BLACK_CANDLE);
						output.accept(Items.BROWN_CANDLE);
						output.accept(Items.RED_CANDLE);
						output.accept(Items.ORANGE_CANDLE);
						output.accept(Items.YELLOW_CANDLE);
						output.accept(Items.LIME_CANDLE);
						output.accept(Items.GREEN_CANDLE);
						output.accept(Items.CYAN_CANDLE);
						output.accept(Items.LIGHT_BLUE_CANDLE);
						output.accept(Items.BLUE_CANDLE);
						output.accept(Items.PURPLE_CANDLE);
						output.accept(Items.MAGENTA_CANDLE);
						output.accept(Items.PINK_CANDLE);
						output.accept(Items.WHITE_BANNER);
						output.accept(Items.LIGHT_GRAY_BANNER);
						output.accept(Items.GRAY_BANNER);
						output.accept(Items.BLACK_BANNER);
						output.accept(Items.BROWN_BANNER);
						output.accept(Items.RED_BANNER);
						output.accept(Items.ORANGE_BANNER);
						output.accept(Items.YELLOW_BANNER);
						output.accept(Items.LIME_BANNER);
						output.accept(Items.GREEN_BANNER);
						output.accept(Items.CYAN_BANNER);
						output.accept(Items.LIGHT_BLUE_BANNER);
						output.accept(Items.BLUE_BANNER);
						output.accept(Items.PURPLE_BANNER);
						output.accept(Items.MAGENTA_BANNER);
						output.accept(Items.PINK_BANNER);
						output.accept(Raid.getLeaderBannerInstance(itemDisplayParameters.holders().lookupOrThrow(Registries.BANNER_PATTERN)));
						output.accept(Items.SKELETON_SKULL);
						output.accept(Items.WITHER_SKELETON_SKULL);
						output.accept(Items.PLAYER_HEAD);
						output.accept(Items.ZOMBIE_HEAD);
						output.accept(Items.CREEPER_HEAD);
						output.accept(Items.PIGLIN_HEAD);
						output.accept(Items.DRAGON_HEAD);
						output.accept(Items.DRAGON_EGG);
						output.accept(Items.END_PORTAL_FRAME);
						output.accept(Items.ENDER_EYE);
						output.accept(Items.VAULT);
						output.accept(Items.INFESTED_STONE);
						output.accept(Items.INFESTED_COBBLESTONE);
						output.accept(Items.INFESTED_STONE_BRICKS);
						output.accept(Items.INFESTED_MOSSY_STONE_BRICKS);
						output.accept(Items.INFESTED_CRACKED_STONE_BRICKS);
						output.accept(Items.INFESTED_CHISELED_STONE_BRICKS);
						output.accept(Items.INFESTED_DEEPSLATE);
					}
				)
				.build()
		);
		Registry.register(
			registry,
			REDSTONE_BLOCKS,
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 4)
				.title(Component.translatable("itemGroup.redstone"))
				.icon(() -> new ItemStack(Items.REDSTONE))
				.displayItems((itemDisplayParameters, output) -> {
					output.accept(Items.REDSTONE);
					output.accept(Items.REDSTONE_TORCH);
					output.accept(Items.REDSTONE_BLOCK);
					output.accept(Items.REPEATER);
					output.accept(Items.COMPARATOR);
					output.accept(Items.TARGET);
					output.accept(Items.WAXED_COPPER_BULB);
					output.accept(Items.WAXED_EXPOSED_COPPER_BULB);
					output.accept(Items.WAXED_WEATHERED_COPPER_BULB);
					output.accept(Items.WAXED_OXIDIZED_COPPER_BULB);
					output.accept(Items.LEVER);
					output.accept(Items.OAK_BUTTON);
					output.accept(Items.STONE_BUTTON);
					output.accept(Items.OAK_PRESSURE_PLATE);
					output.accept(Items.STONE_PRESSURE_PLATE);
					output.accept(Items.LIGHT_WEIGHTED_PRESSURE_PLATE);
					output.accept(Items.HEAVY_WEIGHTED_PRESSURE_PLATE);
					output.accept(Items.SCULK_SENSOR);
					output.accept(Items.CALIBRATED_SCULK_SENSOR);
					output.accept(Items.SCULK_SHRIEKER);
					output.accept(Items.AMETHYST_BLOCK);
					output.accept(Items.WHITE_WOOL);
					output.accept(Items.TRIPWIRE_HOOK);
					output.accept(Items.STRING);
					output.accept(Items.LECTERN);
					output.accept(Items.DAYLIGHT_DETECTOR);
					output.accept(Items.LIGHTNING_ROD);
					output.accept(Items.PISTON);
					output.accept(Items.STICKY_PISTON);
					output.accept(Items.SLIME_BLOCK);
					output.accept(Items.HONEY_BLOCK);
					output.accept(Items.DISPENSER);
					output.accept(Items.DROPPER);
					output.accept(Items.CRAFTER);
					output.accept(Items.HOPPER);
					output.accept(Items.CHEST);
					output.accept(Items.BARREL);
					output.accept(Items.CHISELED_BOOKSHELF);
					output.accept(Items.FURNACE);
					output.accept(Items.TRAPPED_CHEST);
					output.accept(Items.JUKEBOX);
					output.accept(Items.DECORATED_POT);
					output.accept(Items.OBSERVER);
					output.accept(Items.NOTE_BLOCK);
					output.accept(Items.COMPOSTER);
					output.accept(Items.CAULDRON);
					output.accept(Items.RAIL);
					output.accept(Items.POWERED_RAIL);
					output.accept(Items.DETECTOR_RAIL);
					output.accept(Items.ACTIVATOR_RAIL);
					output.accept(Items.MINECART);
					output.accept(Items.HOPPER_MINECART);
					output.accept(Items.CHEST_MINECART);
					output.accept(Items.FURNACE_MINECART);
					output.accept(Items.TNT_MINECART);
					output.accept(Items.OAK_CHEST_BOAT);
					output.accept(Items.BAMBOO_CHEST_RAFT);
					output.accept(Items.OAK_DOOR);
					output.accept(Items.IRON_DOOR);
					output.accept(Items.OAK_FENCE_GATE);
					output.accept(Items.OAK_TRAPDOOR);
					output.accept(Items.IRON_TRAPDOOR);
					output.accept(Items.TNT);
					output.accept(Items.REDSTONE_LAMP);
					output.accept(Items.BELL);
					output.accept(Items.BIG_DRIPLEAF);
					output.accept(Items.ARMOR_STAND);
					output.accept(Items.REDSTONE_ORE);
				})
				.build()
		);
		Registry.register(
			registry,
			HOTBAR,
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 5)
				.title(Component.translatable("itemGroup.hotbar"))
				.icon(() -> new ItemStack(Blocks.BOOKSHELF))
				.alignedRight()
				.type(CreativeModeTab.Type.HOTBAR)
				.build()
		);
		Registry.register(
			registry,
			SEARCH,
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 6)
				.title(Component.translatable("itemGroup.search"))
				.icon(() -> new ItemStack(Items.COMPASS))
				.displayItems((itemDisplayParameters, output) -> {
					Set<ItemStack> set = ItemStackLinkedSet.createTypeAndComponentsSet();

					for (CreativeModeTab creativeModeTab : registry) {
						if (creativeModeTab.getType() != CreativeModeTab.Type.SEARCH) {
							set.addAll(creativeModeTab.getSearchTabDisplayItems());
						}
					}

					output.acceptAll(set);
				})
				.backgroundSuffix("item_search.png")
				.alignedRight()
				.type(CreativeModeTab.Type.SEARCH)
				.build()
		);
		Registry.register(
			registry,
			TOOLS_AND_UTILITIES,
			CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 0)
				.title(Component.translatable("itemGroup.tools"))
				.icon(() -> new ItemStack(Items.DIAMOND_PICKAXE))
				.displayItems(
					(itemDisplayParameters, output) -> {
						output.accept(Items.WOODEN_SHOVEL);
						output.accept(Items.WOODEN_PICKAXE);
						output.accept(Items.WOODEN_AXE);
						output.accept(Items.WOODEN_HOE);
						output.accept(Items.STONE_SHOVEL);
						output.accept(Items.STONE_PICKAXE);
						output.accept(Items.STONE_AXE);
						output.accept(Items.STONE_HOE);
						output.accept(Items.IRON_SHOVEL);
						output.accept(Items.IRON_PICKAXE);
						output.accept(Items.IRON_AXE);
						output.accept(Items.IRON_HOE);
						output.accept(Items.GOLDEN_SHOVEL);
						output.accept(Items.GOLDEN_PICKAXE);
						output.accept(Items.GOLDEN_AXE);
						output.accept(Items.GOLDEN_HOE);
						output.accept(Items.DIAMOND_SHOVEL);
						output.accept(Items.DIAMOND_PICKAXE);
						output.accept(Items.DIAMOND_AXE);
						output.accept(Items.DIAMOND_HOE);
						output.accept(Items.NETHERITE_SHOVEL);
						output.accept(Items.NETHERITE_PICKAXE);
						output.accept(Items.NETHERITE_AXE);
						output.accept(Items.NETHERITE_HOE);
						output.accept(Items.BUCKET);
						output.accept(Items.WATER_BUCKET);
						output.accept(Items.COD_BUCKET);
						output.accept(Items.SALMON_BUCKET);
						output.accept(Items.TROPICAL_FISH_BUCKET);
						output.accept(Items.PUFFERFISH_BUCKET);
						output.accept(Items.AXOLOTL_BUCKET);
						output.accept(Items.TADPOLE_BUCKET);
						output.accept(Items.LAVA_BUCKET);
						output.accept(Items.POWDER_SNOW_BUCKET);
						output.accept(Items.MILK_BUCKET);
						output.accept(Items.FISHING_ROD);
						output.accept(Items.FLINT_AND_STEEL);
						output.accept(Items.FIRE_CHARGE);
						output.accept(Items.BONE_MEAL);
						output.accept(Items.SHEARS);
						output.accept(Items.BRUSH);
						output.accept(Items.NAME_TAG);
						output.accept(Items.LEAD);
						if (itemDisplayParameters.enabledFeatures().contains(FeatureFlags.BUNDLE)) {
							output.accept(Items.BUNDLE);
						}

						output.accept(Items.COMPASS);
						output.accept(Items.RECOVERY_COMPASS);
						output.accept(Items.CLOCK);
						output.accept(Items.SPYGLASS);
						output.accept(Items.MAP);
						output.accept(Items.WRITABLE_BOOK);
						output.accept(Items.WIND_CHARGE);
						output.accept(Items.ENDER_PEARL);
						output.accept(Items.ENDER_EYE);
						output.accept(Items.ELYTRA);
						generateFireworksAllDurations(output, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
						output.accept(Items.SADDLE);
						output.accept(Items.CARROT_ON_A_STICK);
						output.accept(Items.WARPED_FUNGUS_ON_A_STICK);
						output.accept(Items.OAK_BOAT);
						output.accept(Items.OAK_CHEST_BOAT);
						output.accept(Items.SPRUCE_BOAT);
						output.accept(Items.SPRUCE_CHEST_BOAT);
						output.accept(Items.BIRCH_BOAT);
						output.accept(Items.BIRCH_CHEST_BOAT);
						output.accept(Items.JUNGLE_BOAT);
						output.accept(Items.JUNGLE_CHEST_BOAT);
						output.accept(Items.ACACIA_BOAT);
						output.accept(Items.ACACIA_CHEST_BOAT);
						output.accept(Items.DARK_OAK_BOAT);
						output.accept(Items.DARK_OAK_CHEST_BOAT);
						output.accept(Items.MANGROVE_BOAT);
						output.accept(Items.MANGROVE_CHEST_BOAT);
						output.accept(Items.CHERRY_BOAT);
						output.accept(Items.CHERRY_CHEST_BOAT);
						output.accept(Items.BAMBOO_RAFT);
						output.accept(Items.BAMBOO_CHEST_RAFT);
						output.accept(Items.RAIL);
						output.accept(Items.POWERED_RAIL);
						output.accept(Items.DETECTOR_RAIL);
						output.accept(Items.ACTIVATOR_RAIL);
						output.accept(Items.MINECART);
						output.accept(Items.HOPPER_MINECART);
						output.accept(Items.CHEST_MINECART);
						output.accept(Items.FURNACE_MINECART);
						output.accept(Items.TNT_MINECART);
						itemDisplayParameters.holders()
							.lookup(Registries.INSTRUMENT)
							.ifPresent(
								registryLookup -> generateInstrumentTypes(
										output, registryLookup, Items.GOAT_HORN, InstrumentTags.GOAT_HORNS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
									)
							);
						output.accept(Items.MUSIC_DISC_13);
						output.accept(Items.MUSIC_DISC_CAT);
						output.accept(Items.MUSIC_DISC_BLOCKS);
						output.accept(Items.MUSIC_DISC_CHIRP);
						output.accept(Items.MUSIC_DISC_FAR);
						output.accept(Items.MUSIC_DISC_MALL);
						output.accept(Items.MUSIC_DISC_MELLOHI);
						output.accept(Items.MUSIC_DISC_STAL);
						output.accept(Items.MUSIC_DISC_STRAD);
						output.accept(Items.MUSIC_DISC_WARD);
						output.accept(Items.MUSIC_DISC_11);
						output.accept(Items.MUSIC_DISC_WAIT);
						output.accept(Items.MUSIC_DISC_OTHERSIDE);
						output.accept(Items.MUSIC_DISC_RELIC);
						output.accept(Items.MUSIC_DISC_5);
						output.accept(Items.MUSIC_DISC_PIGSTEP);
					}
				)
				.build()
		);
		Registry.register(
			registry,
			COMBAT,
			CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 1)
				.title(Component.translatable("itemGroup.combat"))
				.icon(() -> new ItemStack(Items.NETHERITE_SWORD))
				.displayItems(
					(itemDisplayParameters, output) -> {
						output.accept(Items.WOODEN_SWORD);
						output.accept(Items.STONE_SWORD);
						output.accept(Items.IRON_SWORD);
						output.accept(Items.GOLDEN_SWORD);
						output.accept(Items.DIAMOND_SWORD);
						output.accept(Items.NETHERITE_SWORD);
						output.accept(Items.WOODEN_AXE);
						output.accept(Items.STONE_AXE);
						output.accept(Items.IRON_AXE);
						output.accept(Items.GOLDEN_AXE);
						output.accept(Items.DIAMOND_AXE);
						output.accept(Items.NETHERITE_AXE);
						output.accept(Items.TRIDENT);
						output.accept(Items.SHIELD);
						output.accept(Items.LEATHER_HELMET);
						output.accept(Items.LEATHER_CHESTPLATE);
						output.accept(Items.LEATHER_LEGGINGS);
						output.accept(Items.LEATHER_BOOTS);
						output.accept(Items.CHAINMAIL_HELMET);
						output.accept(Items.CHAINMAIL_CHESTPLATE);
						output.accept(Items.CHAINMAIL_LEGGINGS);
						output.accept(Items.CHAINMAIL_BOOTS);
						output.accept(Items.IRON_HELMET);
						output.accept(Items.IRON_CHESTPLATE);
						output.accept(Items.IRON_LEGGINGS);
						output.accept(Items.IRON_BOOTS);
						output.accept(Items.GOLDEN_HELMET);
						output.accept(Items.GOLDEN_CHESTPLATE);
						output.accept(Items.GOLDEN_LEGGINGS);
						output.accept(Items.GOLDEN_BOOTS);
						output.accept(Items.DIAMOND_HELMET);
						output.accept(Items.DIAMOND_CHESTPLATE);
						output.accept(Items.DIAMOND_LEGGINGS);
						output.accept(Items.DIAMOND_BOOTS);
						output.accept(Items.NETHERITE_HELMET);
						output.accept(Items.NETHERITE_CHESTPLATE);
						output.accept(Items.NETHERITE_LEGGINGS);
						output.accept(Items.NETHERITE_BOOTS);
						output.accept(Items.TURTLE_HELMET);
						output.accept(Items.LEATHER_HORSE_ARMOR);
						output.accept(Items.IRON_HORSE_ARMOR);
						output.accept(Items.GOLDEN_HORSE_ARMOR);
						output.accept(Items.DIAMOND_HORSE_ARMOR);
						output.accept(Items.WOLF_ARMOR);
						output.accept(Items.TOTEM_OF_UNDYING);
						output.accept(Items.TNT);
						output.accept(Items.END_CRYSTAL);
						output.accept(Items.SNOWBALL);
						output.accept(Items.EGG);
						output.accept(Items.WIND_CHARGE);
						output.accept(Items.BOW);
						output.accept(Items.CROSSBOW);
						generateFireworksAllDurations(output, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
						output.accept(Items.ARROW);
						output.accept(Items.SPECTRAL_ARROW);
						itemDisplayParameters.holders()
							.lookup(Registries.POTION)
							.ifPresent(registryLookup -> generatePotionEffectTypes(output, registryLookup, Items.TIPPED_ARROW, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
					}
				)
				.build()
		);
		Registry.register(
			registry,
			FOOD_AND_DRINKS,
			CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 2)
				.title(Component.translatable("itemGroup.foodAndDrink"))
				.icon(() -> new ItemStack(Items.GOLDEN_APPLE))
				.displayItems((itemDisplayParameters, output) -> {
					output.accept(Items.APPLE);
					output.accept(Items.GOLDEN_APPLE);
					output.accept(Items.ENCHANTED_GOLDEN_APPLE);
					output.accept(Items.MELON_SLICE);
					output.accept(Items.SWEET_BERRIES);
					output.accept(Items.GLOW_BERRIES);
					output.accept(Items.CHORUS_FRUIT);
					output.accept(Items.CARROT);
					output.accept(Items.GOLDEN_CARROT);
					output.accept(Items.POTATO);
					output.accept(Items.BAKED_POTATO);
					output.accept(Items.POISONOUS_POTATO);
					output.accept(Items.BEETROOT);
					output.accept(Items.DRIED_KELP);
					output.accept(Items.BEEF);
					output.accept(Items.COOKED_BEEF);
					output.accept(Items.PORKCHOP);
					output.accept(Items.COOKED_PORKCHOP);
					output.accept(Items.MUTTON);
					output.accept(Items.COOKED_MUTTON);
					output.accept(Items.CHICKEN);
					output.accept(Items.COOKED_CHICKEN);
					output.accept(Items.RABBIT);
					output.accept(Items.COOKED_RABBIT);
					output.accept(Items.COD);
					output.accept(Items.COOKED_COD);
					output.accept(Items.SALMON);
					output.accept(Items.COOKED_SALMON);
					output.accept(Items.TROPICAL_FISH);
					output.accept(Items.PUFFERFISH);
					output.accept(Items.BREAD);
					output.accept(Items.COOKIE);
					output.accept(Items.CAKE);
					output.accept(Items.PUMPKIN_PIE);
					output.accept(Items.ROTTEN_FLESH);
					output.accept(Items.SPIDER_EYE);
					output.accept(Items.MUSHROOM_STEW);
					output.accept(Items.BEETROOT_SOUP);
					output.accept(Items.RABBIT_STEW);
					generateSuspiciousStews(output, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
					output.accept(Items.MILK_BUCKET);
					output.accept(Items.HONEY_BOTTLE);
					itemDisplayParameters.holders().lookup(Registries.POTION).ifPresent(registryLookup -> {
						generatePotionEffectTypes(output, registryLookup, Items.POTION, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
						generatePotionEffectTypes(output, registryLookup, Items.SPLASH_POTION, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
						generatePotionEffectTypes(output, registryLookup, Items.LINGERING_POTION, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
					});
				})
				.build()
		);
		Registry.register(
			registry,
			INGREDIENTS,
			CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 3)
				.title(Component.translatable("itemGroup.ingredients"))
				.icon(() -> new ItemStack(Items.IRON_INGOT))
				.displayItems(
					(itemDisplayParameters, output) -> {
						output.accept(Items.COAL);
						output.accept(Items.CHARCOAL);
						output.accept(Items.RAW_IRON);
						output.accept(Items.RAW_COPPER);
						output.accept(Items.RAW_GOLD);
						output.accept(Items.EMERALD);
						output.accept(Items.LAPIS_LAZULI);
						output.accept(Items.DIAMOND);
						output.accept(Items.ANCIENT_DEBRIS);
						output.accept(Items.QUARTZ);
						output.accept(Items.AMETHYST_SHARD);
						output.accept(Items.IRON_NUGGET);
						output.accept(Items.GOLD_NUGGET);
						output.accept(Items.IRON_INGOT);
						output.accept(Items.COPPER_INGOT);
						output.accept(Items.GOLD_INGOT);
						output.accept(Items.NETHERITE_SCRAP);
						output.accept(Items.NETHERITE_INGOT);
						output.accept(Items.STICK);
						output.accept(Items.FLINT);
						output.accept(Items.WHEAT);
						output.accept(Items.BONE);
						output.accept(Items.BONE_MEAL);
						output.accept(Items.STRING);
						output.accept(Items.FEATHER);
						output.accept(Items.SNOWBALL);
						output.accept(Items.EGG);
						output.accept(Items.LEATHER);
						output.accept(Items.RABBIT_HIDE);
						output.accept(Items.HONEYCOMB);
						output.accept(Items.INK_SAC);
						output.accept(Items.GLOW_INK_SAC);
						output.accept(Items.TURTLE_SCUTE);
						output.accept(Items.ARMADILLO_SCUTE);
						output.accept(Items.SLIME_BALL);
						output.accept(Items.CLAY_BALL);
						output.accept(Items.PRISMARINE_SHARD);
						output.accept(Items.PRISMARINE_CRYSTALS);
						output.accept(Items.NAUTILUS_SHELL);
						output.accept(Items.HEART_OF_THE_SEA);
						output.accept(Items.FIRE_CHARGE);
						output.accept(Items.BLAZE_ROD);
						output.accept(Items.NETHER_STAR);
						output.accept(Items.ENDER_PEARL);
						output.accept(Items.ENDER_EYE);
						output.accept(Items.SHULKER_SHELL);
						output.accept(Items.POPPED_CHORUS_FRUIT);
						output.accept(Items.ECHO_SHARD);
						output.accept(Items.DISC_FRAGMENT_5);
						output.accept(Items.WHITE_DYE);
						output.accept(Items.LIGHT_GRAY_DYE);
						output.accept(Items.GRAY_DYE);
						output.accept(Items.BLACK_DYE);
						output.accept(Items.BROWN_DYE);
						output.accept(Items.RED_DYE);
						output.accept(Items.ORANGE_DYE);
						output.accept(Items.YELLOW_DYE);
						output.accept(Items.LIME_DYE);
						output.accept(Items.GREEN_DYE);
						output.accept(Items.CYAN_DYE);
						output.accept(Items.LIGHT_BLUE_DYE);
						output.accept(Items.BLUE_DYE);
						output.accept(Items.PURPLE_DYE);
						output.accept(Items.MAGENTA_DYE);
						output.accept(Items.PINK_DYE);
						output.accept(Items.BOWL);
						output.accept(Items.BRICK);
						output.accept(Items.NETHER_BRICK);
						output.accept(Items.PAPER);
						output.accept(Items.BOOK);
						output.accept(Items.FIREWORK_STAR);
						output.accept(Items.GLASS_BOTTLE);
						output.accept(Items.NETHER_WART);
						output.accept(Items.REDSTONE);
						output.accept(Items.GLOWSTONE_DUST);
						output.accept(Items.GUNPOWDER);
						output.accept(Items.DRAGON_BREATH);
						output.accept(Items.FERMENTED_SPIDER_EYE);
						output.accept(Items.BLAZE_POWDER);
						output.accept(Items.SUGAR);
						output.accept(Items.RABBIT_FOOT);
						output.accept(Items.GLISTERING_MELON_SLICE);
						output.accept(Items.SPIDER_EYE);
						output.accept(Items.PUFFERFISH);
						output.accept(Items.MAGMA_CREAM);
						output.accept(Items.GOLDEN_CARROT);
						output.accept(Items.GHAST_TEAR);
						output.accept(Items.TURTLE_HELMET);
						output.accept(Items.PHANTOM_MEMBRANE);
						output.accept(Items.FLOWER_BANNER_PATTERN);
						output.accept(Items.CREEPER_BANNER_PATTERN);
						output.accept(Items.SKULL_BANNER_PATTERN);
						output.accept(Items.MOJANG_BANNER_PATTERN);
						output.accept(Items.GLOBE_BANNER_PATTERN);
						output.accept(Items.PIGLIN_BANNER_PATTERN);
						output.accept(Items.ANGLER_POTTERY_SHERD);
						output.accept(Items.ARCHER_POTTERY_SHERD);
						output.accept(Items.ARMS_UP_POTTERY_SHERD);
						output.accept(Items.BLADE_POTTERY_SHERD);
						output.accept(Items.BREWER_POTTERY_SHERD);
						output.accept(Items.BURN_POTTERY_SHERD);
						output.accept(Items.DANGER_POTTERY_SHERD);
						output.accept(Items.EXPLORER_POTTERY_SHERD);
						output.accept(Items.FRIEND_POTTERY_SHERD);
						output.accept(Items.HEART_POTTERY_SHERD);
						output.accept(Items.HEARTBREAK_POTTERY_SHERD);
						output.accept(Items.HOWL_POTTERY_SHERD);
						output.accept(Items.MINER_POTTERY_SHERD);
						output.accept(Items.MOURNER_POTTERY_SHERD);
						output.accept(Items.PLENTY_POTTERY_SHERD);
						output.accept(Items.PRIZE_POTTERY_SHERD);
						output.accept(Items.SHEAF_POTTERY_SHERD);
						output.accept(Items.SHELTER_POTTERY_SHERD);
						output.accept(Items.SKULL_POTTERY_SHERD);
						output.accept(Items.SNORT_POTTERY_SHERD);
						output.accept(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
						output.accept(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE);
						output.accept(Items.EXPERIENCE_BOTTLE);
						output.accept(Items.TRIAL_KEY);
						Set<TagKey<Item>> set = Set.of(
							ItemTags.FOOT_ARMOR_ENCHANTABLE,
							ItemTags.LEG_ARMOR_ENCHANTABLE,
							ItemTags.CHEST_ARMOR_ENCHANTABLE,
							ItemTags.HEAD_ARMOR_ENCHANTABLE,
							ItemTags.ARMOR_ENCHANTABLE,
							ItemTags.SWORD_ENCHANTABLE,
							ItemTags.WEAPON_ENCHANTABLE,
							ItemTags.MINING_ENCHANTABLE,
							ItemTags.FISHING_ENCHANTABLE,
							ItemTags.TRIDENT_ENCHANTABLE,
							ItemTags.DURABILITY_ENCHANTABLE,
							ItemTags.BOW_ENCHANTABLE,
							ItemTags.EQUIPPABLE_ENCHANTABLE,
							ItemTags.CROSSBOW_ENCHANTABLE,
							ItemTags.VANISHING_ENCHANTABLE
						);
						itemDisplayParameters.holders().lookup(Registries.ENCHANTMENT).ifPresent(registryLookup -> {
							generateEnchantmentBookTypesOnlyMaxLevel(output, registryLookup, set, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
							generateEnchantmentBookTypesAllLevels(output, registryLookup, set, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
						});
					}
				)
				.build()
		);
		Registry.register(
			registry,
			SPAWN_EGGS,
			CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 4)
				.title(Component.translatable("itemGroup.spawnEggs"))
				.icon(() -> new ItemStack(Items.PIG_SPAWN_EGG))
				.displayItems((itemDisplayParameters, output) -> {
					output.accept(Items.SPAWNER);
					output.accept(Items.TRIAL_SPAWNER);
					output.accept(Items.ALLAY_SPAWN_EGG);
					output.accept(Items.ARMADILLO_SPAWN_EGG);
					output.accept(Items.AXOLOTL_SPAWN_EGG);
					output.accept(Items.BAT_SPAWN_EGG);
					output.accept(Items.BEE_SPAWN_EGG);
					output.accept(Items.BLAZE_SPAWN_EGG);
					output.accept(Items.BOGGED_SPAWN_EGG);
					output.accept(Items.BREEZE_SPAWN_EGG);
					output.accept(Items.CAMEL_SPAWN_EGG);
					output.accept(Items.CAT_SPAWN_EGG);
					output.accept(Items.CAVE_SPIDER_SPAWN_EGG);
					output.accept(Items.CHICKEN_SPAWN_EGG);
					output.accept(Items.COD_SPAWN_EGG);
					output.accept(Items.COW_SPAWN_EGG);
					output.accept(Items.CREEPER_SPAWN_EGG);
					output.accept(Items.DOLPHIN_SPAWN_EGG);
					output.accept(Items.DONKEY_SPAWN_EGG);
					output.accept(Items.DROWNED_SPAWN_EGG);
					output.accept(Items.ELDER_GUARDIAN_SPAWN_EGG);
					output.accept(Items.ENDERMAN_SPAWN_EGG);
					output.accept(Items.ENDERMITE_SPAWN_EGG);
					output.accept(Items.EVOKER_SPAWN_EGG);
					output.accept(Items.FOX_SPAWN_EGG);
					output.accept(Items.FROG_SPAWN_EGG);
					output.accept(Items.GHAST_SPAWN_EGG);
					output.accept(Items.GLOW_SQUID_SPAWN_EGG);
					output.accept(Items.GOAT_SPAWN_EGG);
					output.accept(Items.GUARDIAN_SPAWN_EGG);
					output.accept(Items.HOGLIN_SPAWN_EGG);
					output.accept(Items.HORSE_SPAWN_EGG);
					output.accept(Items.HUSK_SPAWN_EGG);
					output.accept(Items.IRON_GOLEM_SPAWN_EGG);
					output.accept(Items.LLAMA_SPAWN_EGG);
					output.accept(Items.MAGMA_CUBE_SPAWN_EGG);
					output.accept(Items.MOOSHROOM_SPAWN_EGG);
					output.accept(Items.MULE_SPAWN_EGG);
					output.accept(Items.OCELOT_SPAWN_EGG);
					output.accept(Items.PANDA_SPAWN_EGG);
					output.accept(Items.PARROT_SPAWN_EGG);
					output.accept(Items.PHANTOM_SPAWN_EGG);
					output.accept(Items.PIG_SPAWN_EGG);
					output.accept(Items.PIGLIN_SPAWN_EGG);
					output.accept(Items.PIGLIN_BRUTE_SPAWN_EGG);
					output.accept(Items.PILLAGER_SPAWN_EGG);
					output.accept(Items.POLAR_BEAR_SPAWN_EGG);
					output.accept(Items.PUFFERFISH_SPAWN_EGG);
					output.accept(Items.RABBIT_SPAWN_EGG);
					output.accept(Items.RAVAGER_SPAWN_EGG);
					output.accept(Items.SALMON_SPAWN_EGG);
					output.accept(Items.SHEEP_SPAWN_EGG);
					output.accept(Items.SHULKER_SPAWN_EGG);
					output.accept(Items.SILVERFISH_SPAWN_EGG);
					output.accept(Items.SKELETON_SPAWN_EGG);
					output.accept(Items.SKELETON_HORSE_SPAWN_EGG);
					output.accept(Items.SLIME_SPAWN_EGG);
					output.accept(Items.SNIFFER_SPAWN_EGG);
					output.accept(Items.SNOW_GOLEM_SPAWN_EGG);
					output.accept(Items.SPIDER_SPAWN_EGG);
					output.accept(Items.SQUID_SPAWN_EGG);
					output.accept(Items.STRAY_SPAWN_EGG);
					output.accept(Items.STRIDER_SPAWN_EGG);
					output.accept(Items.TADPOLE_SPAWN_EGG);
					output.accept(Items.TRADER_LLAMA_SPAWN_EGG);
					output.accept(Items.TROPICAL_FISH_SPAWN_EGG);
					output.accept(Items.TURTLE_SPAWN_EGG);
					output.accept(Items.VEX_SPAWN_EGG);
					output.accept(Items.VILLAGER_SPAWN_EGG);
					output.accept(Items.VINDICATOR_SPAWN_EGG);
					output.accept(Items.WANDERING_TRADER_SPAWN_EGG);
					output.accept(Items.WARDEN_SPAWN_EGG);
					output.accept(Items.WITCH_SPAWN_EGG);
					output.accept(Items.WITHER_SKELETON_SPAWN_EGG);
					output.accept(Items.WOLF_SPAWN_EGG);
					output.accept(Items.ZOGLIN_SPAWN_EGG);
					output.accept(Items.ZOMBIE_SPAWN_EGG);
					output.accept(Items.ZOMBIE_HORSE_SPAWN_EGG);
					output.accept(Items.ZOMBIE_VILLAGER_SPAWN_EGG);
					output.accept(Items.ZOMBIFIED_PIGLIN_SPAWN_EGG);
				})
				.build()
		);
		Registry.register(
			registry,
			OP_BLOCKS,
			CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 5)
				.title(Component.translatable("itemGroup.op"))
				.icon(() -> new ItemStack(Items.COMMAND_BLOCK))
				.alignedRight()
				.displayItems(
					(itemDisplayParameters, output) -> {
						if (itemDisplayParameters.hasPermissions()) {
							output.accept(Items.COMMAND_BLOCK);
							output.accept(Items.CHAIN_COMMAND_BLOCK);
							output.accept(Items.REPEATING_COMMAND_BLOCK);
							output.accept(Items.COMMAND_BLOCK_MINECART);
							output.accept(Items.JIGSAW);
							output.accept(Items.STRUCTURE_BLOCK);
							output.accept(Items.STRUCTURE_VOID);
							output.accept(Items.BARRIER);
							output.accept(Items.DEBUG_STICK);

							for (int i = 15; i >= 0; i--) {
								output.accept(LightBlock.setLightOnStack(new ItemStack(Items.LIGHT), i));
							}

							itemDisplayParameters.holders()
								.lookup(Registries.PAINTING_VARIANT)
								.ifPresent(
									registryLookup -> generatePresetPaintings(
											output, registryLookup, holder -> !holder.is(PaintingVariantTags.PLACEABLE), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
										)
								);
						}
					}
				)
				.build()
		);
		return Registry.register(
			registry,
			INVENTORY,
			CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 6)
				.title(Component.translatable("itemGroup.inventory"))
				.icon(() -> new ItemStack(Blocks.CHEST))
				.backgroundSuffix("inventory.png")
				.hideTitle()
				.alignedRight()
				.type(CreativeModeTab.Type.INVENTORY)
				.noScrollBar()
				.build()
		);
	}

	public static void validate() {
		Map<Pair<CreativeModeTab.Row, Integer>, String> map = new HashMap();

		for (ResourceKey<CreativeModeTab> resourceKey : BuiltInRegistries.CREATIVE_MODE_TAB.registryKeySet()) {
			CreativeModeTab creativeModeTab = BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(resourceKey);
			String string = creativeModeTab.getDisplayName().getString();
			String string2 = (String)map.put(Pair.of(creativeModeTab.row(), creativeModeTab.column()), string);
			if (string2 != null) {
				throw new IllegalArgumentException("Duplicate position: " + string + " vs. " + string2);
			}
		}
	}

	public static CreativeModeTab getDefaultTab() {
		return BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(BUILDING_BLOCKS);
	}

	private static void generatePotionEffectTypes(
		CreativeModeTab.Output output, HolderLookup<Potion> holderLookup, Item item, CreativeModeTab.TabVisibility tabVisibility
	) {
		holderLookup.listElements().map(reference -> PotionContents.createItemStack(item, reference)).forEach(itemStack -> output.accept(itemStack, tabVisibility));
	}

	private static void generateEnchantmentBookTypesOnlyMaxLevel(
		CreativeModeTab.Output output, HolderLookup<Enchantment> holderLookup, Set<TagKey<Item>> set, CreativeModeTab.TabVisibility tabVisibility
	) {
		holderLookup.listElements()
			.map(Holder::value)
			.filter(enchantment -> set.contains(enchantment.getMatch()))
			.map(enchantment -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchantment.getMaxLevel())))
			.forEach(itemStack -> output.accept(itemStack, tabVisibility));
	}

	private static void generateEnchantmentBookTypesAllLevels(
		CreativeModeTab.Output output, HolderLookup<Enchantment> holderLookup, Set<TagKey<Item>> set, CreativeModeTab.TabVisibility tabVisibility
	) {
		holderLookup.listElements()
			.map(Holder::value)
			.filter(enchantment -> set.contains(enchantment.getMatch()))
			.flatMap(
				enchantment -> IntStream.rangeClosed(enchantment.getMinLevel(), enchantment.getMaxLevel())
						.mapToObj(i -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, i)))
			)
			.forEach(itemStack -> output.accept(itemStack, tabVisibility));
	}

	private static void generateInstrumentTypes(
		CreativeModeTab.Output output, HolderLookup<Instrument> holderLookup, Item item, TagKey<Instrument> tagKey, CreativeModeTab.TabVisibility tabVisibility
	) {
		holderLookup.get(tagKey)
			.ifPresent(named -> named.stream().map(holder -> InstrumentItem.create(item, holder)).forEach(itemStack -> output.accept(itemStack, tabVisibility)));
	}

	private static void generateSuspiciousStews(CreativeModeTab.Output output, CreativeModeTab.TabVisibility tabVisibility) {
		List<SuspiciousEffectHolder> list = SuspiciousEffectHolder.getAllEffectHolders();
		Set<ItemStack> set = ItemStackLinkedSet.createTypeAndComponentsSet();

		for (SuspiciousEffectHolder suspiciousEffectHolder : list) {
			ItemStack itemStack = new ItemStack(Items.SUSPICIOUS_STEW);
			itemStack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, suspiciousEffectHolder.getSuspiciousEffects());
			set.add(itemStack);
		}

		output.acceptAll(set, tabVisibility);
	}

	private static void generateFireworksAllDurations(CreativeModeTab.Output output, CreativeModeTab.TabVisibility tabVisibility) {
		for (byte b : FireworkRocketItem.CRAFTABLE_DURATIONS) {
			ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
			itemStack.set(DataComponents.FIREWORKS, new Fireworks(b, List.of()));
			output.accept(itemStack, tabVisibility);
		}
	}

	private static void generatePresetPaintings(
		CreativeModeTab.Output output,
		HolderLookup.RegistryLookup<PaintingVariant> registryLookup,
		Predicate<Holder<PaintingVariant>> predicate,
		CreativeModeTab.TabVisibility tabVisibility
	) {
		registryLookup.listElements()
			.filter(predicate)
			.sorted(PAINTING_COMPARATOR)
			.forEach(
				reference -> {
					CustomData customData = Util.getOrThrow(CustomData.EMPTY.update(Painting.VARIANT_MAP_CODEC, reference), IllegalStateException::new)
						.update(compoundTag -> compoundTag.putString("id", "minecraft:painting"));
					ItemStack itemStack = new ItemStack(Items.PAINTING);
					itemStack.set(DataComponents.ENTITY_DATA, customData);
					output.accept(itemStack, tabVisibility);
				}
			);
	}

	public static List<CreativeModeTab> tabs() {
		return streamAllTabs().filter(CreativeModeTab::shouldDisplay).toList();
	}

	public static List<CreativeModeTab> allTabs() {
		return streamAllTabs().toList();
	}

	private static Stream<CreativeModeTab> streamAllTabs() {
		return BuiltInRegistries.CREATIVE_MODE_TAB.stream();
	}

	public static CreativeModeTab searchTab() {
		return BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(SEARCH);
	}

	private static void buildAllTabContents(CreativeModeTab.ItemDisplayParameters itemDisplayParameters) {
		streamAllTabs()
			.filter(creativeModeTab -> creativeModeTab.getType() == CreativeModeTab.Type.CATEGORY)
			.forEach(creativeModeTab -> creativeModeTab.buildContents(itemDisplayParameters));
		streamAllTabs()
			.filter(creativeModeTab -> creativeModeTab.getType() != CreativeModeTab.Type.CATEGORY)
			.forEach(creativeModeTab -> creativeModeTab.buildContents(itemDisplayParameters));
	}

	public static boolean tryRebuildTabContents(FeatureFlagSet featureFlagSet, boolean bl, HolderLookup.Provider provider) {
		if (CACHED_PARAMETERS != null && !CACHED_PARAMETERS.needsUpdate(featureFlagSet, bl, provider)) {
			return false;
		} else {
			CACHED_PARAMETERS = new CreativeModeTab.ItemDisplayParameters(featureFlagSet, bl, provider);
			buildAllTabContents(CACHED_PARAMETERS);
			return true;
		}
	}
}
