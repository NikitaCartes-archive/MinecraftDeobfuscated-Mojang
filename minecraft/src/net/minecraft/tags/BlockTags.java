package net.minecraft.tags;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public final class BlockTags {
	protected static final StaticTagHelper<Block> HELPER = StaticTags.create(new ResourceLocation("block"), TagContainer::getBlocks);
	public static final Tag.Named<Block> WOOL = bind("wool");
	public static final Tag.Named<Block> PLANKS = bind("planks");
	public static final Tag.Named<Block> STONE_BRICKS = bind("stone_bricks");
	public static final Tag.Named<Block> WOODEN_BUTTONS = bind("wooden_buttons");
	public static final Tag.Named<Block> BUTTONS = bind("buttons");
	public static final Tag.Named<Block> CARPETS = bind("carpets");
	public static final Tag.Named<Block> WOODEN_DOORS = bind("wooden_doors");
	public static final Tag.Named<Block> WOODEN_STAIRS = bind("wooden_stairs");
	public static final Tag.Named<Block> WOODEN_SLABS = bind("wooden_slabs");
	public static final Tag.Named<Block> WOODEN_FENCES = bind("wooden_fences");
	public static final Tag.Named<Block> PRESSURE_PLATES = bind("pressure_plates");
	public static final Tag.Named<Block> WOODEN_PRESSURE_PLATES = bind("wooden_pressure_plates");
	public static final Tag.Named<Block> STONE_PRESSURE_PLATES = bind("stone_pressure_plates");
	public static final Tag.Named<Block> WOODEN_TRAPDOORS = bind("wooden_trapdoors");
	public static final Tag.Named<Block> DOORS = bind("doors");
	public static final Tag.Named<Block> SAPLINGS = bind("saplings");
	public static final Tag.Named<Block> LOGS_THAT_BURN = bind("logs_that_burn");
	public static final Tag.Named<Block> LOGS = bind("logs");
	public static final Tag.Named<Block> DARK_OAK_LOGS = bind("dark_oak_logs");
	public static final Tag.Named<Block> OAK_LOGS = bind("oak_logs");
	public static final Tag.Named<Block> BIRCH_LOGS = bind("birch_logs");
	public static final Tag.Named<Block> ACACIA_LOGS = bind("acacia_logs");
	public static final Tag.Named<Block> JUNGLE_LOGS = bind("jungle_logs");
	public static final Tag.Named<Block> SPRUCE_LOGS = bind("spruce_logs");
	public static final Tag.Named<Block> CRIMSON_STEMS = bind("crimson_stems");
	public static final Tag.Named<Block> WARPED_STEMS = bind("warped_stems");
	public static final Tag.Named<Block> BANNERS = bind("banners");
	public static final Tag.Named<Block> SAND = bind("sand");
	public static final Tag.Named<Block> STAIRS = bind("stairs");
	public static final Tag.Named<Block> SLABS = bind("slabs");
	public static final Tag.Named<Block> WALLS = bind("walls");
	public static final Tag.Named<Block> ANVIL = bind("anvil");
	public static final Tag.Named<Block> RAILS = bind("rails");
	public static final Tag.Named<Block> LEAVES = bind("leaves");
	public static final Tag.Named<Block> TRAPDOORS = bind("trapdoors");
	public static final Tag.Named<Block> SMALL_FLOWERS = bind("small_flowers");
	public static final Tag.Named<Block> BEDS = bind("beds");
	public static final Tag.Named<Block> FENCES = bind("fences");
	public static final Tag.Named<Block> TALL_FLOWERS = bind("tall_flowers");
	public static final Tag.Named<Block> FLOWERS = bind("flowers");
	public static final Tag.Named<Block> PIGLIN_REPELLENTS = bind("piglin_repellents");
	public static final Tag.Named<Block> GOLD_ORES = bind("gold_ores");
	public static final Tag.Named<Block> NON_FLAMMABLE_WOOD = bind("non_flammable_wood");
	public static final Tag.Named<Block> FLOWER_POTS = bind("flower_pots");
	public static final Tag.Named<Block> ENDERMAN_HOLDABLE = bind("enderman_holdable");
	public static final Tag.Named<Block> ICE = bind("ice");
	public static final Tag.Named<Block> VALID_SPAWN = bind("valid_spawn");
	public static final Tag.Named<Block> IMPERMEABLE = bind("impermeable");
	public static final Tag.Named<Block> UNDERWATER_BONEMEALS = bind("underwater_bonemeals");
	public static final Tag.Named<Block> CORAL_BLOCKS = bind("coral_blocks");
	public static final Tag.Named<Block> WALL_CORALS = bind("wall_corals");
	public static final Tag.Named<Block> CORAL_PLANTS = bind("coral_plants");
	public static final Tag.Named<Block> CORALS = bind("corals");
	public static final Tag.Named<Block> BAMBOO_PLANTABLE_ON = bind("bamboo_plantable_on");
	public static final Tag.Named<Block> STANDING_SIGNS = bind("standing_signs");
	public static final Tag.Named<Block> WALL_SIGNS = bind("wall_signs");
	public static final Tag.Named<Block> SIGNS = bind("signs");
	public static final Tag.Named<Block> DRAGON_IMMUNE = bind("dragon_immune");
	public static final Tag.Named<Block> WITHER_IMMUNE = bind("wither_immune");
	public static final Tag.Named<Block> WITHER_SUMMON_BASE_BLOCKS = bind("wither_summon_base_blocks");
	public static final Tag.Named<Block> BEEHIVES = bind("beehives");
	public static final Tag.Named<Block> CROPS = bind("crops");
	public static final Tag.Named<Block> BEE_GROWABLES = bind("bee_growables");
	public static final Tag.Named<Block> PORTALS = bind("portals");
	public static final Tag.Named<Block> FIRE = bind("fire");
	public static final Tag.Named<Block> NYLIUM = bind("nylium");
	public static final Tag.Named<Block> WART_BLOCKS = bind("wart_blocks");
	public static final Tag.Named<Block> BEACON_BASE_BLOCKS = bind("beacon_base_blocks");
	public static final Tag.Named<Block> SOUL_SPEED_BLOCKS = bind("soul_speed_blocks");
	public static final Tag.Named<Block> WALL_POST_OVERRIDE = bind("wall_post_override");
	public static final Tag.Named<Block> CLIMBABLE = bind("climbable");
	public static final Tag.Named<Block> SHULKER_BOXES = bind("shulker_boxes");
	public static final Tag.Named<Block> HOGLIN_REPELLENTS = bind("hoglin_repellents");
	public static final Tag.Named<Block> SOUL_FIRE_BASE_BLOCKS = bind("soul_fire_base_blocks");
	public static final Tag.Named<Block> STRIDER_WARM_BLOCKS = bind("strider_warm_blocks");
	public static final Tag.Named<Block> CAMPFIRES = bind("campfires");
	public static final Tag.Named<Block> GUARDED_BY_PIGLINS = bind("guarded_by_piglins");
	public static final Tag.Named<Block> PREVENT_MOB_SPAWNING_INSIDE = bind("prevent_mob_spawning_inside");
	public static final Tag.Named<Block> FENCE_GATES = bind("fence_gates");
	public static final Tag.Named<Block> UNSTABLE_BOTTOM_CENTER = bind("unstable_bottom_center");
	public static final Tag.Named<Block> MUSHROOM_GROW_BLOCK = bind("mushroom_grow_block");
	public static final Tag.Named<Block> INFINIBURN_OVERWORLD = bind("infiniburn_overworld");
	public static final Tag.Named<Block> INFINIBURN_NETHER = bind("infiniburn_nether");
	public static final Tag.Named<Block> INFINIBURN_END = bind("infiniburn_end");
	public static final Tag.Named<Block> BASE_STONE_OVERWORLD = bind("base_stone_overworld");
	public static final Tag.Named<Block> BASE_STONE_NETHER = bind("base_stone_nether");

	private static Tag.Named<Block> bind(String string) {
		return HELPER.bind(string);
	}

	public static TagCollection<Block> getAllTags() {
		return HELPER.getAllTags();
	}

	public static List<? extends Tag.Named<Block>> getWrappers() {
		return HELPER.getWrappers();
	}
}
