package net.minecraft.data.tags;

import java.nio.file.Path;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BlockTagsProvider extends TagsProvider<Block> {
	public BlockTagsProvider(DataGenerator dataGenerator) {
		super(dataGenerator, Registry.BLOCK);
	}

	@Override
	protected void addTags() {
		this.tag(BlockTags.WOOL)
			.add(
				Blocks.WHITE_WOOL,
				Blocks.ORANGE_WOOL,
				Blocks.MAGENTA_WOOL,
				Blocks.LIGHT_BLUE_WOOL,
				Blocks.YELLOW_WOOL,
				Blocks.LIME_WOOL,
				Blocks.PINK_WOOL,
				Blocks.GRAY_WOOL,
				Blocks.LIGHT_GRAY_WOOL,
				Blocks.CYAN_WOOL,
				Blocks.PURPLE_WOOL,
				Blocks.BLUE_WOOL,
				Blocks.BROWN_WOOL,
				Blocks.GREEN_WOOL,
				Blocks.RED_WOOL,
				Blocks.BLACK_WOOL
			);
		this.tag(BlockTags.PLANKS)
			.add(
				Blocks.OAK_PLANKS,
				Blocks.SPRUCE_PLANKS,
				Blocks.BIRCH_PLANKS,
				Blocks.JUNGLE_PLANKS,
				Blocks.ACACIA_PLANKS,
				Blocks.DARK_OAK_PLANKS,
				Blocks.CRIMSON_PLANKS,
				Blocks.WARPED_PLANKS
			);
		this.tag(BlockTags.STONE_BRICKS).add(Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS);
		this.tag(BlockTags.WOODEN_BUTTONS)
			.add(
				Blocks.OAK_BUTTON,
				Blocks.SPRUCE_BUTTON,
				Blocks.BIRCH_BUTTON,
				Blocks.JUNGLE_BUTTON,
				Blocks.ACACIA_BUTTON,
				Blocks.DARK_OAK_BUTTON,
				Blocks.CRIMSON_BUTTON,
				Blocks.WARPED_BUTTON
			);
		this.tag(BlockTags.BUTTONS).addTag(BlockTags.WOODEN_BUTTONS).add(Blocks.STONE_BUTTON).add(Blocks.POLISHED_BLACKSTONE_BUTTON);
		this.tag(BlockTags.CARPETS)
			.add(
				Blocks.WHITE_CARPET,
				Blocks.ORANGE_CARPET,
				Blocks.MAGENTA_CARPET,
				Blocks.LIGHT_BLUE_CARPET,
				Blocks.YELLOW_CARPET,
				Blocks.LIME_CARPET,
				Blocks.PINK_CARPET,
				Blocks.GRAY_CARPET,
				Blocks.LIGHT_GRAY_CARPET,
				Blocks.CYAN_CARPET,
				Blocks.PURPLE_CARPET,
				Blocks.BLUE_CARPET,
				Blocks.BROWN_CARPET,
				Blocks.GREEN_CARPET,
				Blocks.RED_CARPET,
				Blocks.BLACK_CARPET
			);
		this.tag(BlockTags.WOODEN_DOORS)
			.add(
				Blocks.OAK_DOOR,
				Blocks.SPRUCE_DOOR,
				Blocks.BIRCH_DOOR,
				Blocks.JUNGLE_DOOR,
				Blocks.ACACIA_DOOR,
				Blocks.DARK_OAK_DOOR,
				Blocks.CRIMSON_DOOR,
				Blocks.WARPED_DOOR
			);
		this.tag(BlockTags.WOODEN_STAIRS)
			.add(
				Blocks.OAK_STAIRS,
				Blocks.SPRUCE_STAIRS,
				Blocks.BIRCH_STAIRS,
				Blocks.JUNGLE_STAIRS,
				Blocks.ACACIA_STAIRS,
				Blocks.DARK_OAK_STAIRS,
				Blocks.CRIMSON_STAIRS,
				Blocks.WARPED_STAIRS
			);
		this.tag(BlockTags.WOODEN_SLABS)
			.add(
				Blocks.OAK_SLAB,
				Blocks.SPRUCE_SLAB,
				Blocks.BIRCH_SLAB,
				Blocks.JUNGLE_SLAB,
				Blocks.ACACIA_SLAB,
				Blocks.DARK_OAK_SLAB,
				Blocks.CRIMSON_SLAB,
				Blocks.WARPED_SLAB
			);
		this.tag(BlockTags.WOODEN_FENCES)
			.add(
				Blocks.OAK_FENCE,
				Blocks.ACACIA_FENCE,
				Blocks.DARK_OAK_FENCE,
				Blocks.SPRUCE_FENCE,
				Blocks.BIRCH_FENCE,
				Blocks.JUNGLE_FENCE,
				Blocks.CRIMSON_FENCE,
				Blocks.WARPED_FENCE
			);
		this.tag(BlockTags.DOORS).addTag(BlockTags.WOODEN_DOORS).add(Blocks.IRON_DOOR);
		this.tag(BlockTags.SAPLINGS)
			.add(Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING);
		this.tag(BlockTags.DARK_OAK_LOGS).add(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_WOOD);
		this.tag(BlockTags.OAK_LOGS).add(Blocks.OAK_LOG, Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_OAK_WOOD);
		this.tag(BlockTags.ACACIA_LOGS).add(Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_ACACIA_WOOD);
		this.tag(BlockTags.BIRCH_LOGS).add(Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_BIRCH_WOOD);
		this.tag(BlockTags.JUNGLE_LOGS).add(Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_WOOD);
		this.tag(BlockTags.SPRUCE_LOGS).add(Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_WOOD);
		this.tag(BlockTags.CRIMSON_STEMS).add(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE);
		this.tag(BlockTags.WARPED_STEMS).add(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE);
		this.tag(BlockTags.LOGS_THAT_BURN)
			.addTag(BlockTags.DARK_OAK_LOGS)
			.addTag(BlockTags.OAK_LOGS)
			.addTag(BlockTags.ACACIA_LOGS)
			.addTag(BlockTags.BIRCH_LOGS)
			.addTag(BlockTags.JUNGLE_LOGS)
			.addTag(BlockTags.SPRUCE_LOGS);
		this.tag(BlockTags.LOGS).addTag(BlockTags.LOGS_THAT_BURN).addTag(BlockTags.CRIMSON_STEMS).addTag(BlockTags.WARPED_STEMS);
		this.tag(BlockTags.ANVIL).add(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL);
		this.tag(BlockTags.SMALL_FLOWERS)
			.add(
				Blocks.DANDELION,
				Blocks.POPPY,
				Blocks.BLUE_ORCHID,
				Blocks.ALLIUM,
				Blocks.AZURE_BLUET,
				Blocks.RED_TULIP,
				Blocks.ORANGE_TULIP,
				Blocks.WHITE_TULIP,
				Blocks.PINK_TULIP,
				Blocks.OXEYE_DAISY,
				Blocks.CORNFLOWER,
				Blocks.LILY_OF_THE_VALLEY,
				Blocks.WITHER_ROSE,
				Blocks.AZALEA_LEAVES_FLOWERS,
				Blocks.FLOWERING_AZALEA
			);
		this.tag(BlockTags.DIRT).add(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.COARSE_DIRT, Blocks.MYCELIUM, Blocks.ROOTED_DIRT, Blocks.MOSS_BLOCK);
		this.tag(BlockTags.ENDERMAN_HOLDABLE)
			.addTag(BlockTags.SMALL_FLOWERS)
			.addTag(BlockTags.DIRT)
			.add(
				Blocks.SAND,
				Blocks.RED_SAND,
				Blocks.GRAVEL,
				Blocks.BROWN_MUSHROOM,
				Blocks.RED_MUSHROOM,
				Blocks.TNT,
				Blocks.CACTUS,
				Blocks.CLAY,
				Blocks.PUMPKIN,
				Blocks.CARVED_PUMPKIN,
				Blocks.MELON,
				Blocks.MYCELIUM,
				Blocks.CRIMSON_FUNGUS,
				Blocks.CRIMSON_NYLIUM,
				Blocks.CRIMSON_ROOTS,
				Blocks.WARPED_FUNGUS,
				Blocks.WARPED_NYLIUM,
				Blocks.WARPED_ROOTS
			);
		this.tag(BlockTags.FLOWER_POTS)
			.add(
				Blocks.FLOWER_POT,
				Blocks.POTTED_POPPY,
				Blocks.POTTED_BLUE_ORCHID,
				Blocks.POTTED_ALLIUM,
				Blocks.POTTED_AZURE_BLUET,
				Blocks.POTTED_RED_TULIP,
				Blocks.POTTED_ORANGE_TULIP,
				Blocks.POTTED_WHITE_TULIP,
				Blocks.POTTED_PINK_TULIP,
				Blocks.POTTED_OXEYE_DAISY,
				Blocks.POTTED_DANDELION,
				Blocks.POTTED_OAK_SAPLING,
				Blocks.POTTED_SPRUCE_SAPLING,
				Blocks.POTTED_BIRCH_SAPLING,
				Blocks.POTTED_JUNGLE_SAPLING,
				Blocks.POTTED_ACACIA_SAPLING,
				Blocks.POTTED_DARK_OAK_SAPLING,
				Blocks.POTTED_RED_MUSHROOM,
				Blocks.POTTED_BROWN_MUSHROOM,
				Blocks.POTTED_DEAD_BUSH,
				Blocks.POTTED_FERN,
				Blocks.POTTED_CACTUS,
				Blocks.POTTED_CORNFLOWER,
				Blocks.POTTED_LILY_OF_THE_VALLEY,
				Blocks.POTTED_WITHER_ROSE,
				Blocks.POTTED_BAMBOO,
				Blocks.POTTED_CRIMSON_FUNGUS,
				Blocks.POTTED_WARPED_FUNGUS,
				Blocks.POTTED_CRIMSON_ROOTS,
				Blocks.POTTED_WARPED_ROOTS
			);
		this.tag(BlockTags.BANNERS)
			.add(
				Blocks.WHITE_BANNER,
				Blocks.ORANGE_BANNER,
				Blocks.MAGENTA_BANNER,
				Blocks.LIGHT_BLUE_BANNER,
				Blocks.YELLOW_BANNER,
				Blocks.LIME_BANNER,
				Blocks.PINK_BANNER,
				Blocks.GRAY_BANNER,
				Blocks.LIGHT_GRAY_BANNER,
				Blocks.CYAN_BANNER,
				Blocks.PURPLE_BANNER,
				Blocks.BLUE_BANNER,
				Blocks.BROWN_BANNER,
				Blocks.GREEN_BANNER,
				Blocks.RED_BANNER,
				Blocks.BLACK_BANNER,
				Blocks.WHITE_WALL_BANNER,
				Blocks.ORANGE_WALL_BANNER,
				Blocks.MAGENTA_WALL_BANNER,
				Blocks.LIGHT_BLUE_WALL_BANNER,
				Blocks.YELLOW_WALL_BANNER,
				Blocks.LIME_WALL_BANNER,
				Blocks.PINK_WALL_BANNER,
				Blocks.GRAY_WALL_BANNER,
				Blocks.LIGHT_GRAY_WALL_BANNER,
				Blocks.CYAN_WALL_BANNER,
				Blocks.PURPLE_WALL_BANNER,
				Blocks.BLUE_WALL_BANNER,
				Blocks.BROWN_WALL_BANNER,
				Blocks.GREEN_WALL_BANNER,
				Blocks.RED_WALL_BANNER,
				Blocks.BLACK_WALL_BANNER
			);
		this.tag(BlockTags.WOODEN_PRESSURE_PLATES)
			.add(
				Blocks.OAK_PRESSURE_PLATE,
				Blocks.SPRUCE_PRESSURE_PLATE,
				Blocks.BIRCH_PRESSURE_PLATE,
				Blocks.JUNGLE_PRESSURE_PLATE,
				Blocks.ACACIA_PRESSURE_PLATE,
				Blocks.DARK_OAK_PRESSURE_PLATE,
				Blocks.CRIMSON_PRESSURE_PLATE,
				Blocks.WARPED_PRESSURE_PLATE
			);
		this.tag(BlockTags.STONE_PRESSURE_PLATES).add(Blocks.STONE_PRESSURE_PLATE, Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE);
		this.tag(BlockTags.PRESSURE_PLATES)
			.add(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE)
			.addTag(BlockTags.WOODEN_PRESSURE_PLATES)
			.addTag(BlockTags.STONE_PRESSURE_PLATES);
		this.tag(BlockTags.STAIRS)
			.addTag(BlockTags.WOODEN_STAIRS)
			.add(
				Blocks.COBBLESTONE_STAIRS,
				Blocks.SANDSTONE_STAIRS,
				Blocks.NETHER_BRICK_STAIRS,
				Blocks.STONE_BRICK_STAIRS,
				Blocks.BRICK_STAIRS,
				Blocks.PURPUR_STAIRS,
				Blocks.QUARTZ_STAIRS,
				Blocks.RED_SANDSTONE_STAIRS,
				Blocks.PRISMARINE_BRICK_STAIRS,
				Blocks.PRISMARINE_STAIRS,
				Blocks.DARK_PRISMARINE_STAIRS,
				Blocks.POLISHED_GRANITE_STAIRS,
				Blocks.SMOOTH_RED_SANDSTONE_STAIRS,
				Blocks.MOSSY_STONE_BRICK_STAIRS,
				Blocks.POLISHED_DIORITE_STAIRS,
				Blocks.MOSSY_COBBLESTONE_STAIRS,
				Blocks.END_STONE_BRICK_STAIRS,
				Blocks.STONE_STAIRS,
				Blocks.SMOOTH_SANDSTONE_STAIRS,
				Blocks.SMOOTH_QUARTZ_STAIRS,
				Blocks.GRANITE_STAIRS,
				Blocks.ANDESITE_STAIRS,
				Blocks.RED_NETHER_BRICK_STAIRS,
				Blocks.POLISHED_ANDESITE_STAIRS,
				Blocks.DIORITE_STAIRS,
				Blocks.BLACKSTONE_STAIRS,
				Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
				Blocks.POLISHED_BLACKSTONE_STAIRS,
				Blocks.COBBLED_DEEPSLATE_STAIRS,
				Blocks.POLISHED_DEEPSLATE_STAIRS,
				Blocks.DEEPSLATE_TILE_STAIRS,
				Blocks.DEEPSLATE_BRICK_STAIRS,
				Blocks.OXIDIZED_CUT_COPPER_STAIRS,
				Blocks.WEATHERED_CUT_COPPER_STAIRS,
				Blocks.EXPOSED_CUT_COPPER_STAIRS,
				Blocks.CUT_COPPER_STAIRS,
				Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS,
				Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS,
				Blocks.WAXED_CUT_COPPER_STAIRS,
				Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS
			);
		this.tag(BlockTags.SLABS)
			.addTag(BlockTags.WOODEN_SLABS)
			.add(
				Blocks.STONE_SLAB,
				Blocks.SMOOTH_STONE_SLAB,
				Blocks.STONE_BRICK_SLAB,
				Blocks.SANDSTONE_SLAB,
				Blocks.PURPUR_SLAB,
				Blocks.QUARTZ_SLAB,
				Blocks.RED_SANDSTONE_SLAB,
				Blocks.BRICK_SLAB,
				Blocks.COBBLESTONE_SLAB,
				Blocks.NETHER_BRICK_SLAB,
				Blocks.PETRIFIED_OAK_SLAB,
				Blocks.PRISMARINE_SLAB,
				Blocks.PRISMARINE_BRICK_SLAB,
				Blocks.DARK_PRISMARINE_SLAB,
				Blocks.POLISHED_GRANITE_SLAB,
				Blocks.SMOOTH_RED_SANDSTONE_SLAB,
				Blocks.MOSSY_STONE_BRICK_SLAB,
				Blocks.POLISHED_DIORITE_SLAB,
				Blocks.MOSSY_COBBLESTONE_SLAB,
				Blocks.END_STONE_BRICK_SLAB,
				Blocks.SMOOTH_SANDSTONE_SLAB,
				Blocks.SMOOTH_QUARTZ_SLAB,
				Blocks.GRANITE_SLAB,
				Blocks.ANDESITE_SLAB,
				Blocks.RED_NETHER_BRICK_SLAB,
				Blocks.POLISHED_ANDESITE_SLAB,
				Blocks.DIORITE_SLAB,
				Blocks.CUT_SANDSTONE_SLAB,
				Blocks.CUT_RED_SANDSTONE_SLAB,
				Blocks.BLACKSTONE_SLAB,
				Blocks.POLISHED_BLACKSTONE_BRICK_SLAB,
				Blocks.POLISHED_BLACKSTONE_SLAB,
				Blocks.COBBLED_DEEPSLATE_SLAB,
				Blocks.POLISHED_DEEPSLATE_SLAB,
				Blocks.DEEPSLATE_TILE_SLAB,
				Blocks.DEEPSLATE_BRICK_SLAB,
				Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB,
				Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,
				Blocks.WAXED_CUT_COPPER_SLAB,
				Blocks.OXIDIZED_CUT_COPPER_SLAB,
				Blocks.WEATHERED_CUT_COPPER_SLAB,
				Blocks.EXPOSED_CUT_COPPER_SLAB,
				Blocks.CUT_COPPER_SLAB,
				Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB
			);
		this.tag(BlockTags.WALLS)
			.add(
				Blocks.COBBLESTONE_WALL,
				Blocks.MOSSY_COBBLESTONE_WALL,
				Blocks.BRICK_WALL,
				Blocks.PRISMARINE_WALL,
				Blocks.RED_SANDSTONE_WALL,
				Blocks.MOSSY_STONE_BRICK_WALL,
				Blocks.GRANITE_WALL,
				Blocks.STONE_BRICK_WALL,
				Blocks.NETHER_BRICK_WALL,
				Blocks.ANDESITE_WALL,
				Blocks.RED_NETHER_BRICK_WALL,
				Blocks.SANDSTONE_WALL,
				Blocks.END_STONE_BRICK_WALL,
				Blocks.DIORITE_WALL,
				Blocks.BLACKSTONE_WALL,
				Blocks.POLISHED_BLACKSTONE_BRICK_WALL,
				Blocks.POLISHED_BLACKSTONE_WALL,
				Blocks.COBBLED_DEEPSLATE_WALL,
				Blocks.POLISHED_DEEPSLATE_WALL,
				Blocks.DEEPSLATE_TILE_WALL,
				Blocks.DEEPSLATE_BRICK_WALL
			);
		this.tag(BlockTags.CORAL_PLANTS).add(Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL);
		this.tag(BlockTags.CORALS)
			.addTag(BlockTags.CORAL_PLANTS)
			.add(Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN);
		this.tag(BlockTags.WALL_CORALS)
			.add(Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN);
		this.tag(BlockTags.SAND).add(Blocks.SAND, Blocks.RED_SAND);
		this.tag(BlockTags.RAILS).add(Blocks.RAIL, Blocks.POWERED_RAIL, Blocks.DETECTOR_RAIL, Blocks.ACTIVATOR_RAIL);
		this.tag(BlockTags.CORAL_BLOCKS)
			.add(Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK);
		this.tag(BlockTags.ICE).add(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.FROSTED_ICE);
		this.tag(BlockTags.VALID_SPAWN).add(Blocks.GRASS_BLOCK, Blocks.PODZOL);
		this.tag(BlockTags.LEAVES)
			.add(
				Blocks.JUNGLE_LEAVES,
				Blocks.OAK_LEAVES,
				Blocks.SPRUCE_LEAVES,
				Blocks.DARK_OAK_LEAVES,
				Blocks.ACACIA_LEAVES,
				Blocks.BIRCH_LEAVES,
				Blocks.AZALEA_LEAVES,
				Blocks.AZALEA_LEAVES_FLOWERS
			);
		this.tag(BlockTags.IMPERMEABLE)
			.add(
				Blocks.GLASS,
				Blocks.WHITE_STAINED_GLASS,
				Blocks.ORANGE_STAINED_GLASS,
				Blocks.MAGENTA_STAINED_GLASS,
				Blocks.LIGHT_BLUE_STAINED_GLASS,
				Blocks.YELLOW_STAINED_GLASS,
				Blocks.LIME_STAINED_GLASS,
				Blocks.PINK_STAINED_GLASS,
				Blocks.GRAY_STAINED_GLASS,
				Blocks.LIGHT_GRAY_STAINED_GLASS,
				Blocks.CYAN_STAINED_GLASS,
				Blocks.PURPLE_STAINED_GLASS,
				Blocks.BLUE_STAINED_GLASS,
				Blocks.BROWN_STAINED_GLASS,
				Blocks.GREEN_STAINED_GLASS,
				Blocks.RED_STAINED_GLASS,
				Blocks.BLACK_STAINED_GLASS,
				Blocks.TINTED_GLASS
			);
		this.tag(BlockTags.WOODEN_TRAPDOORS)
			.add(
				Blocks.ACACIA_TRAPDOOR,
				Blocks.BIRCH_TRAPDOOR,
				Blocks.DARK_OAK_TRAPDOOR,
				Blocks.JUNGLE_TRAPDOOR,
				Blocks.OAK_TRAPDOOR,
				Blocks.SPRUCE_TRAPDOOR,
				Blocks.CRIMSON_TRAPDOOR,
				Blocks.WARPED_TRAPDOOR
			);
		this.tag(BlockTags.TRAPDOORS).addTag(BlockTags.WOODEN_TRAPDOORS).add(Blocks.IRON_TRAPDOOR);
		this.tag(BlockTags.UNDERWATER_BONEMEALS).add(Blocks.SEAGRASS).addTag(BlockTags.CORALS).addTag(BlockTags.WALL_CORALS);
		this.tag(BlockTags.BAMBOO_PLANTABLE_ON).addTag(BlockTags.SAND).addTag(BlockTags.DIRT).add(Blocks.BAMBOO, Blocks.BAMBOO_SAPLING, Blocks.GRAVEL);
		this.tag(BlockTags.STANDING_SIGNS)
			.add(
				Blocks.OAK_SIGN,
				Blocks.SPRUCE_SIGN,
				Blocks.BIRCH_SIGN,
				Blocks.ACACIA_SIGN,
				Blocks.JUNGLE_SIGN,
				Blocks.DARK_OAK_SIGN,
				Blocks.CRIMSON_SIGN,
				Blocks.WARPED_SIGN
			);
		this.tag(BlockTags.WALL_SIGNS)
			.add(
				Blocks.OAK_WALL_SIGN,
				Blocks.SPRUCE_WALL_SIGN,
				Blocks.BIRCH_WALL_SIGN,
				Blocks.ACACIA_WALL_SIGN,
				Blocks.JUNGLE_WALL_SIGN,
				Blocks.DARK_OAK_WALL_SIGN,
				Blocks.CRIMSON_WALL_SIGN,
				Blocks.WARPED_WALL_SIGN
			);
		this.tag(BlockTags.SIGNS).addTag(BlockTags.STANDING_SIGNS).addTag(BlockTags.WALL_SIGNS);
		this.tag(BlockTags.BEDS)
			.add(
				Blocks.RED_BED,
				Blocks.BLACK_BED,
				Blocks.BLUE_BED,
				Blocks.BROWN_BED,
				Blocks.CYAN_BED,
				Blocks.GRAY_BED,
				Blocks.GREEN_BED,
				Blocks.LIGHT_BLUE_BED,
				Blocks.LIGHT_GRAY_BED,
				Blocks.LIME_BED,
				Blocks.MAGENTA_BED,
				Blocks.ORANGE_BED,
				Blocks.PINK_BED,
				Blocks.PURPLE_BED,
				Blocks.WHITE_BED,
				Blocks.YELLOW_BED
			);
		this.tag(BlockTags.FENCES).addTag(BlockTags.WOODEN_FENCES).add(Blocks.NETHER_BRICK_FENCE);
		this.tag(BlockTags.DRAGON_IMMUNE)
			.add(
				Blocks.BARRIER,
				Blocks.BEDROCK,
				Blocks.END_PORTAL,
				Blocks.END_PORTAL_FRAME,
				Blocks.END_GATEWAY,
				Blocks.COMMAND_BLOCK,
				Blocks.REPEATING_COMMAND_BLOCK,
				Blocks.CHAIN_COMMAND_BLOCK,
				Blocks.STRUCTURE_BLOCK,
				Blocks.JIGSAW,
				Blocks.MOVING_PISTON,
				Blocks.OBSIDIAN,
				Blocks.CRYING_OBSIDIAN,
				Blocks.END_STONE,
				Blocks.IRON_BARS,
				Blocks.RESPAWN_ANCHOR
			);
		this.tag(BlockTags.WITHER_IMMUNE)
			.add(
				Blocks.BARRIER,
				Blocks.BEDROCK,
				Blocks.END_PORTAL,
				Blocks.END_PORTAL_FRAME,
				Blocks.END_GATEWAY,
				Blocks.COMMAND_BLOCK,
				Blocks.REPEATING_COMMAND_BLOCK,
				Blocks.CHAIN_COMMAND_BLOCK,
				Blocks.STRUCTURE_BLOCK,
				Blocks.JIGSAW,
				Blocks.MOVING_PISTON
			);
		this.tag(BlockTags.WITHER_SUMMON_BASE_BLOCKS).add(Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
		this.tag(BlockTags.TALL_FLOWERS).add(Blocks.SUNFLOWER, Blocks.LILAC, Blocks.PEONY, Blocks.ROSE_BUSH);
		this.tag(BlockTags.FLOWERS).addTag(BlockTags.SMALL_FLOWERS).addTag(BlockTags.TALL_FLOWERS);
		this.tag(BlockTags.BEEHIVES).add(Blocks.BEE_NEST, Blocks.BEEHIVE);
		this.tag(BlockTags.CROPS).add(Blocks.BEETROOTS, Blocks.CARROTS, Blocks.POTATOES, Blocks.WHEAT, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
		this.tag(BlockTags.BEE_GROWABLES).addTag(BlockTags.CROPS).add(Blocks.SWEET_BERRY_BUSH);
		this.tag(BlockTags.SHULKER_BOXES)
			.add(
				Blocks.SHULKER_BOX,
				Blocks.BLACK_SHULKER_BOX,
				Blocks.BLUE_SHULKER_BOX,
				Blocks.BROWN_SHULKER_BOX,
				Blocks.CYAN_SHULKER_BOX,
				Blocks.GRAY_SHULKER_BOX,
				Blocks.GREEN_SHULKER_BOX,
				Blocks.LIGHT_BLUE_SHULKER_BOX,
				Blocks.LIGHT_GRAY_SHULKER_BOX,
				Blocks.LIME_SHULKER_BOX,
				Blocks.MAGENTA_SHULKER_BOX,
				Blocks.ORANGE_SHULKER_BOX,
				Blocks.PINK_SHULKER_BOX,
				Blocks.PURPLE_SHULKER_BOX,
				Blocks.RED_SHULKER_BOX,
				Blocks.WHITE_SHULKER_BOX,
				Blocks.YELLOW_SHULKER_BOX
			);
		this.tag(BlockTags.PORTALS).add(Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_GATEWAY);
		this.tag(BlockTags.FIRE).add(Blocks.FIRE, Blocks.SOUL_FIRE);
		this.tag(BlockTags.NYLIUM).add(Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM);
		this.tag(BlockTags.WART_BLOCKS).add(Blocks.NETHER_WART_BLOCK, Blocks.WARPED_WART_BLOCK);
		this.tag(BlockTags.BEACON_BASE_BLOCKS).add(Blocks.NETHERITE_BLOCK, Blocks.EMERALD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK);
		this.tag(BlockTags.SOUL_SPEED_BLOCKS).add(Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
		this.tag(BlockTags.WALL_POST_OVERRIDE)
			.add(Blocks.TORCH, Blocks.SOUL_TORCH, Blocks.REDSTONE_TORCH, Blocks.TRIPWIRE)
			.addTag(BlockTags.SIGNS)
			.addTag(BlockTags.BANNERS)
			.addTag(BlockTags.PRESSURE_PLATES);
		this.tag(BlockTags.CLIMBABLE)
			.add(
				Blocks.LADDER,
				Blocks.VINE,
				Blocks.SCAFFOLDING,
				Blocks.WEEPING_VINES,
				Blocks.WEEPING_VINES_PLANT,
				Blocks.TWISTING_VINES,
				Blocks.TWISTING_VINES_PLANT,
				Blocks.CAVE_VINES,
				Blocks.CAVE_VINES_PLANT
			);
		this.tag(BlockTags.PIGLIN_REPELLENTS)
			.add(Blocks.SOUL_FIRE)
			.add(Blocks.SOUL_TORCH)
			.add(Blocks.SOUL_LANTERN)
			.add(Blocks.SOUL_WALL_TORCH)
			.add(Blocks.SOUL_CAMPFIRE);
		this.tag(BlockTags.HOGLIN_REPELLENTS).add(Blocks.WARPED_FUNGUS).add(Blocks.POTTED_WARPED_FUNGUS).add(Blocks.NETHER_PORTAL).add(Blocks.RESPAWN_ANCHOR);
		this.tag(BlockTags.GOLD_ORES).add(Blocks.GOLD_ORE, Blocks.NETHER_GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE);
		this.tag(BlockTags.IRON_ORES).add(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
		this.tag(BlockTags.DIAMOND_ORES).add(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
		this.tag(BlockTags.REDSTONE_ORES).add(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
		this.tag(BlockTags.COAL_ORES).add(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
		this.tag(BlockTags.EMERALD_ORES).add(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
		this.tag(BlockTags.COPPER_ORES).add(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
		this.tag(BlockTags.LAPIS_ORES).add(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
		this.tag(BlockTags.SOUL_FIRE_BASE_BLOCKS).add(Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
		this.tag(BlockTags.NON_FLAMMABLE_WOOD)
			.add(
				Blocks.WARPED_STEM,
				Blocks.STRIPPED_WARPED_STEM,
				Blocks.WARPED_HYPHAE,
				Blocks.STRIPPED_WARPED_HYPHAE,
				Blocks.CRIMSON_STEM,
				Blocks.STRIPPED_CRIMSON_STEM,
				Blocks.CRIMSON_HYPHAE,
				Blocks.STRIPPED_CRIMSON_HYPHAE,
				Blocks.CRIMSON_PLANKS,
				Blocks.WARPED_PLANKS,
				Blocks.CRIMSON_SLAB,
				Blocks.WARPED_SLAB,
				Blocks.CRIMSON_PRESSURE_PLATE,
				Blocks.WARPED_PRESSURE_PLATE,
				Blocks.CRIMSON_FENCE,
				Blocks.WARPED_FENCE,
				Blocks.CRIMSON_TRAPDOOR,
				Blocks.WARPED_TRAPDOOR,
				Blocks.CRIMSON_FENCE_GATE,
				Blocks.WARPED_FENCE_GATE,
				Blocks.CRIMSON_STAIRS,
				Blocks.WARPED_STAIRS,
				Blocks.CRIMSON_BUTTON,
				Blocks.WARPED_BUTTON,
				Blocks.CRIMSON_DOOR,
				Blocks.WARPED_DOOR,
				Blocks.CRIMSON_SIGN,
				Blocks.WARPED_SIGN,
				Blocks.CRIMSON_WALL_SIGN,
				Blocks.WARPED_WALL_SIGN
			);
		this.tag(BlockTags.STRIDER_WARM_BLOCKS).add(Blocks.LAVA);
		this.tag(BlockTags.CAMPFIRES).add(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
		this.tag(BlockTags.GUARDED_BY_PIGLINS)
			.add(Blocks.GOLD_BLOCK, Blocks.BARREL, Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.GILDED_BLACKSTONE, Blocks.TRAPPED_CHEST)
			.addTag(BlockTags.SHULKER_BOXES)
			.addTag(BlockTags.GOLD_ORES);
		this.tag(BlockTags.PREVENT_MOB_SPAWNING_INSIDE).addTag(BlockTags.RAILS);
		this.tag(BlockTags.FENCE_GATES)
			.add(
				Blocks.ACACIA_FENCE_GATE,
				Blocks.BIRCH_FENCE_GATE,
				Blocks.DARK_OAK_FENCE_GATE,
				Blocks.JUNGLE_FENCE_GATE,
				Blocks.OAK_FENCE_GATE,
				Blocks.SPRUCE_FENCE_GATE,
				Blocks.CRIMSON_FENCE_GATE,
				Blocks.WARPED_FENCE_GATE
			);
		this.tag(BlockTags.UNSTABLE_BOTTOM_CENTER).addTag(BlockTags.FENCE_GATES);
		this.tag(BlockTags.MUSHROOM_GROW_BLOCK).add(Blocks.MYCELIUM).add(Blocks.PODZOL).add(Blocks.CRIMSON_NYLIUM).add(Blocks.WARPED_NYLIUM);
		this.tag(BlockTags.INFINIBURN_OVERWORLD).add(Blocks.NETHERRACK, Blocks.MAGMA_BLOCK);
		this.tag(BlockTags.INFINIBURN_NETHER).addTag(BlockTags.INFINIBURN_OVERWORLD);
		this.tag(BlockTags.INFINIBURN_END).addTag(BlockTags.INFINIBURN_OVERWORLD).add(Blocks.BEDROCK);
		this.tag(BlockTags.STONE_ORE_REPLACEABLES).add(Blocks.STONE).add(Blocks.GRANITE).add(Blocks.DIORITE).add(Blocks.ANDESITE);
		this.tag(BlockTags.DEEPSLATE_ORE_REPLACEABLES).add(Blocks.DEEPSLATE).add(Blocks.TUFF);
		this.tag(BlockTags.BASE_STONE_OVERWORLD)
			.add(Blocks.STONE)
			.add(Blocks.GRANITE)
			.add(Blocks.DIORITE)
			.add(Blocks.ANDESITE)
			.add(Blocks.TUFF)
			.add(Blocks.DEEPSLATE);
		this.tag(BlockTags.BASE_STONE_NETHER).add(Blocks.NETHERRACK).add(Blocks.BASALT).add(Blocks.BLACKSTONE);
		this.tag(BlockTags.CANDLES)
			.add(
				Blocks.CANDLE,
				Blocks.WHITE_CANDLE,
				Blocks.ORANGE_CANDLE,
				Blocks.MAGENTA_CANDLE,
				Blocks.LIGHT_BLUE_CANDLE,
				Blocks.YELLOW_CANDLE,
				Blocks.LIME_CANDLE,
				Blocks.PINK_CANDLE,
				Blocks.GRAY_CANDLE,
				Blocks.LIGHT_GRAY_CANDLE,
				Blocks.CYAN_CANDLE,
				Blocks.PURPLE_CANDLE,
				Blocks.BLUE_CANDLE,
				Blocks.BROWN_CANDLE,
				Blocks.GREEN_CANDLE,
				Blocks.RED_CANDLE,
				Blocks.BLACK_CANDLE
			);
		this.tag(BlockTags.CANDLE_CAKES)
			.add(
				Blocks.CANDLE_CAKE,
				Blocks.WHITE_CANDLE_CAKE,
				Blocks.ORANGE_CANDLE_CAKE,
				Blocks.MAGENTA_CANDLE_CAKE,
				Blocks.LIGHT_BLUE_CANDLE_CAKE,
				Blocks.YELLOW_CANDLE_CAKE,
				Blocks.LIME_CANDLE_CAKE,
				Blocks.PINK_CANDLE_CAKE,
				Blocks.GRAY_CANDLE_CAKE,
				Blocks.LIGHT_GRAY_CANDLE_CAKE,
				Blocks.CYAN_CANDLE_CAKE,
				Blocks.PURPLE_CANDLE_CAKE,
				Blocks.BLUE_CANDLE_CAKE,
				Blocks.BROWN_CANDLE_CAKE,
				Blocks.GREEN_CANDLE_CAKE,
				Blocks.RED_CANDLE_CAKE,
				Blocks.BLACK_CANDLE_CAKE
			);
		this.tag(BlockTags.CRYSTAL_SOUND_BLOCKS).add(Blocks.AMETHYST_BLOCK, Blocks.BUDDING_AMETHYST);
		this.tag(BlockTags.CAULDRONS).add(Blocks.CAULDRON, Blocks.WATER_CAULDRON, Blocks.LAVA_CAULDRON, Blocks.POWDER_SNOW_CAULDRON);
		this.tag(BlockTags.INSIDE_STEP_SOUND_BLOCKS).add(Blocks.SNOW, Blocks.POWDER_SNOW);
		this.tag(BlockTags.DRIPSTONE_REPLACEABLE).addTag(BlockTags.BASE_STONE_OVERWORLD).add(Blocks.DIRT);
		this.tag(BlockTags.CAVE_VINES).add(Blocks.CAVE_VINES_PLANT).add(Blocks.CAVE_VINES);
		this.tag(BlockTags.MOSS_REPLACEABLE).addTag(BlockTags.BASE_STONE_OVERWORLD).addTag(BlockTags.CAVE_VINES).addTag(BlockTags.DIRT);
		this.tag(BlockTags.LUSH_GROUND_REPLACEABLE).addTag(BlockTags.MOSS_REPLACEABLE).add(Blocks.CLAY).add(Blocks.GRAVEL).add(Blocks.MOSS_BLOCK).add(Blocks.SAND);
		this.tag(BlockTags.SMALL_DRIPLEAF_PLACEABLE).add(Blocks.CLAY).add(Blocks.MOSS_BLOCK);
		this.tag(BlockTags.OCCLUDES_VIBRATION_SIGNALS).addTag(BlockTags.WOOL);
		this.tag(BlockTags.SNOW).add(Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW);
	}

	@Override
	protected Path getPath(ResourceLocation resourceLocation) {
		return this.generator.getOutputFolder().resolve("data/" + resourceLocation.getNamespace() + "/tags/blocks/" + resourceLocation.getPath() + ".json");
	}

	@Override
	public String getName() {
		return "Block Tags";
	}
}
