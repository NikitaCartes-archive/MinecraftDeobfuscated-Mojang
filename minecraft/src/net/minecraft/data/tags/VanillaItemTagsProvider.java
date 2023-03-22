package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class VanillaItemTagsProvider extends ItemTagsProvider {
	public VanillaItemTagsProvider(
		PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagsProvider.TagLookup<Block>> completableFuture2
	) {
		super(packOutput, completableFuture, completableFuture2);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.copy(BlockTags.WOOL, ItemTags.WOOL);
		this.copy(BlockTags.PLANKS, ItemTags.PLANKS);
		this.copy(BlockTags.STONE_BRICKS, ItemTags.STONE_BRICKS);
		this.copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
		this.copy(BlockTags.BUTTONS, ItemTags.BUTTONS);
		this.copy(BlockTags.WOOL_CARPETS, ItemTags.WOOL_CARPETS);
		this.copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
		this.copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
		this.copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
		this.copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
		this.copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
		this.copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
		this.copy(BlockTags.DOORS, ItemTags.DOORS);
		this.copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
		this.copy(BlockTags.BAMBOO_BLOCKS, ItemTags.BAMBOO_BLOCKS);
		this.copy(BlockTags.OAK_LOGS, ItemTags.OAK_LOGS);
		this.copy(BlockTags.DARK_OAK_LOGS, ItemTags.DARK_OAK_LOGS);
		this.copy(BlockTags.BIRCH_LOGS, ItemTags.BIRCH_LOGS);
		this.copy(BlockTags.ACACIA_LOGS, ItemTags.ACACIA_LOGS);
		this.copy(BlockTags.SPRUCE_LOGS, ItemTags.SPRUCE_LOGS);
		this.copy(BlockTags.MANGROVE_LOGS, ItemTags.MANGROVE_LOGS);
		this.copy(BlockTags.JUNGLE_LOGS, ItemTags.JUNGLE_LOGS);
		this.copy(BlockTags.CHERRY_LOGS, ItemTags.CHERRY_LOGS);
		this.copy(BlockTags.CRIMSON_STEMS, ItemTags.CRIMSON_STEMS);
		this.copy(BlockTags.WARPED_STEMS, ItemTags.WARPED_STEMS);
		this.copy(BlockTags.WART_BLOCKS, ItemTags.WART_BLOCKS);
		this.copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
		this.copy(BlockTags.LOGS, ItemTags.LOGS);
		this.copy(BlockTags.SAND, ItemTags.SAND);
		this.copy(BlockTags.SMELTS_TO_GLASS, ItemTags.SMELTS_TO_GLASS);
		this.copy(BlockTags.SLABS, ItemTags.SLABS);
		this.copy(BlockTags.WALLS, ItemTags.WALLS);
		this.copy(BlockTags.STAIRS, ItemTags.STAIRS);
		this.copy(BlockTags.ANVIL, ItemTags.ANVIL);
		this.copy(BlockTags.RAILS, ItemTags.RAILS);
		this.copy(BlockTags.LEAVES, ItemTags.LEAVES);
		this.copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
		this.copy(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
		this.copy(BlockTags.SMALL_FLOWERS, ItemTags.SMALL_FLOWERS);
		this.copy(BlockTags.BEDS, ItemTags.BEDS);
		this.copy(BlockTags.FENCES, ItemTags.FENCES);
		this.copy(BlockTags.TALL_FLOWERS, ItemTags.TALL_FLOWERS);
		this.copy(BlockTags.FLOWERS, ItemTags.FLOWERS);
		this.copy(BlockTags.SOUL_FIRE_BASE_BLOCKS, ItemTags.SOUL_FIRE_BASE_BLOCKS);
		this.copy(BlockTags.CANDLES, ItemTags.CANDLES);
		this.copy(BlockTags.DAMPENS_VIBRATIONS, ItemTags.DAMPENS_VIBRATIONS);
		this.copy(BlockTags.GOLD_ORES, ItemTags.GOLD_ORES);
		this.copy(BlockTags.IRON_ORES, ItemTags.IRON_ORES);
		this.copy(BlockTags.DIAMOND_ORES, ItemTags.DIAMOND_ORES);
		this.copy(BlockTags.REDSTONE_ORES, ItemTags.REDSTONE_ORES);
		this.copy(BlockTags.LAPIS_ORES, ItemTags.LAPIS_ORES);
		this.copy(BlockTags.COAL_ORES, ItemTags.COAL_ORES);
		this.copy(BlockTags.EMERALD_ORES, ItemTags.EMERALD_ORES);
		this.copy(BlockTags.COPPER_ORES, ItemTags.COPPER_ORES);
		this.copy(BlockTags.DIRT, ItemTags.DIRT);
		this.copy(BlockTags.TERRACOTTA, ItemTags.TERRACOTTA);
		this.copy(BlockTags.COMPLETES_FIND_TREE_TUTORIAL, ItemTags.COMPLETES_FIND_TREE_TUTORIAL);
		this.tag(ItemTags.BANNERS)
			.add(
				Items.WHITE_BANNER,
				Items.ORANGE_BANNER,
				Items.MAGENTA_BANNER,
				Items.LIGHT_BLUE_BANNER,
				Items.YELLOW_BANNER,
				Items.LIME_BANNER,
				Items.PINK_BANNER,
				Items.GRAY_BANNER,
				Items.LIGHT_GRAY_BANNER,
				Items.CYAN_BANNER,
				Items.PURPLE_BANNER,
				Items.BLUE_BANNER,
				Items.BROWN_BANNER,
				Items.GREEN_BANNER,
				Items.RED_BANNER,
				Items.BLACK_BANNER
			);
		this.tag(ItemTags.BOATS)
			.add(
				Items.OAK_BOAT,
				Items.SPRUCE_BOAT,
				Items.BIRCH_BOAT,
				Items.JUNGLE_BOAT,
				Items.ACACIA_BOAT,
				Items.DARK_OAK_BOAT,
				Items.MANGROVE_BOAT,
				Items.BAMBOO_RAFT,
				Items.CHERRY_BOAT
			)
			.addTag(ItemTags.CHEST_BOATS);
		this.tag(ItemTags.CHEST_BOATS)
			.add(
				Items.OAK_CHEST_BOAT,
				Items.SPRUCE_CHEST_BOAT,
				Items.BIRCH_CHEST_BOAT,
				Items.JUNGLE_CHEST_BOAT,
				Items.ACACIA_CHEST_BOAT,
				Items.DARK_OAK_CHEST_BOAT,
				Items.MANGROVE_CHEST_BOAT,
				Items.BAMBOO_CHEST_RAFT,
				Items.CHERRY_CHEST_BOAT
			);
		this.tag(ItemTags.FISHES).add(Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON, Items.PUFFERFISH, Items.TROPICAL_FISH);
		this.copy(BlockTags.STANDING_SIGNS, ItemTags.SIGNS);
		this.copy(BlockTags.CEILING_HANGING_SIGNS, ItemTags.HANGING_SIGNS);
		this.tag(ItemTags.CREEPER_DROP_MUSIC_DISCS)
			.add(
				Items.MUSIC_DISC_13,
				Items.MUSIC_DISC_CAT,
				Items.MUSIC_DISC_BLOCKS,
				Items.MUSIC_DISC_CHIRP,
				Items.MUSIC_DISC_FAR,
				Items.MUSIC_DISC_MALL,
				Items.MUSIC_DISC_MELLOHI,
				Items.MUSIC_DISC_STAL,
				Items.MUSIC_DISC_STRAD,
				Items.MUSIC_DISC_WARD,
				Items.MUSIC_DISC_11,
				Items.MUSIC_DISC_WAIT
			);
		this.tag(ItemTags.MUSIC_DISCS)
			.addTag(ItemTags.CREEPER_DROP_MUSIC_DISCS)
			.add(Items.MUSIC_DISC_PIGSTEP)
			.add(Items.MUSIC_DISC_OTHERSIDE)
			.add(Items.MUSIC_DISC_5);
		this.tag(ItemTags.COALS).add(Items.COAL, Items.CHARCOAL);
		this.tag(ItemTags.ARROWS).add(Items.ARROW, Items.TIPPED_ARROW, Items.SPECTRAL_ARROW);
		this.tag(ItemTags.LECTERN_BOOKS).add(Items.WRITTEN_BOOK, Items.WRITABLE_BOOK);
		this.tag(ItemTags.BEACON_PAYMENT_ITEMS).add(Items.NETHERITE_INGOT, Items.EMERALD, Items.DIAMOND, Items.GOLD_INGOT, Items.IRON_INGOT);
		this.tag(ItemTags.PIGLIN_REPELLENTS).add(Items.SOUL_TORCH).add(Items.SOUL_LANTERN).add(Items.SOUL_CAMPFIRE);
		this.tag(ItemTags.PIGLIN_LOVED)
			.addTag(ItemTags.GOLD_ORES)
			.add(
				Items.GOLD_BLOCK,
				Items.GILDED_BLACKSTONE,
				Items.LIGHT_WEIGHTED_PRESSURE_PLATE,
				Items.GOLD_INGOT,
				Items.BELL,
				Items.CLOCK,
				Items.GOLDEN_CARROT,
				Items.GLISTERING_MELON_SLICE,
				Items.GOLDEN_APPLE,
				Items.ENCHANTED_GOLDEN_APPLE,
				Items.GOLDEN_HELMET,
				Items.GOLDEN_CHESTPLATE,
				Items.GOLDEN_LEGGINGS,
				Items.GOLDEN_BOOTS,
				Items.GOLDEN_HORSE_ARMOR,
				Items.GOLDEN_SWORD,
				Items.GOLDEN_PICKAXE,
				Items.GOLDEN_SHOVEL,
				Items.GOLDEN_AXE,
				Items.GOLDEN_HOE,
				Items.RAW_GOLD,
				Items.RAW_GOLD_BLOCK
			);
		this.tag(ItemTags.IGNORED_BY_PIGLIN_BABIES).add(Items.LEATHER);
		this.tag(ItemTags.PIGLIN_FOOD).add(Items.PORKCHOP, Items.COOKED_PORKCHOP);
		this.tag(ItemTags.FOX_FOOD).add(Items.SWEET_BERRIES, Items.GLOW_BERRIES);
		this.tag(ItemTags.NON_FLAMMABLE_WOOD)
			.add(
				Items.WARPED_STEM,
				Items.STRIPPED_WARPED_STEM,
				Items.WARPED_HYPHAE,
				Items.STRIPPED_WARPED_HYPHAE,
				Items.CRIMSON_STEM,
				Items.STRIPPED_CRIMSON_STEM,
				Items.CRIMSON_HYPHAE,
				Items.STRIPPED_CRIMSON_HYPHAE,
				Items.CRIMSON_PLANKS,
				Items.WARPED_PLANKS,
				Items.CRIMSON_SLAB,
				Items.WARPED_SLAB,
				Items.CRIMSON_PRESSURE_PLATE,
				Items.WARPED_PRESSURE_PLATE,
				Items.CRIMSON_FENCE,
				Items.WARPED_FENCE,
				Items.CRIMSON_TRAPDOOR,
				Items.WARPED_TRAPDOOR,
				Items.CRIMSON_FENCE_GATE,
				Items.WARPED_FENCE_GATE,
				Items.CRIMSON_STAIRS,
				Items.WARPED_STAIRS,
				Items.CRIMSON_BUTTON,
				Items.WARPED_BUTTON,
				Items.CRIMSON_DOOR,
				Items.WARPED_DOOR,
				Items.CRIMSON_SIGN,
				Items.WARPED_SIGN,
				Items.WARPED_HANGING_SIGN,
				Items.CRIMSON_HANGING_SIGN
			);
		this.tag(ItemTags.STONE_TOOL_MATERIALS).add(Items.COBBLESTONE, Items.BLACKSTONE, Items.COBBLED_DEEPSLATE);
		this.tag(ItemTags.STONE_CRAFTING_MATERIALS).add(Items.COBBLESTONE, Items.BLACKSTONE, Items.COBBLED_DEEPSLATE);
		this.tag(ItemTags.FREEZE_IMMUNE_WEARABLES)
			.add(Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET, Items.LEATHER_HORSE_ARMOR);
		this.tag(ItemTags.AXOLOTL_TEMPT_ITEMS).add(Items.TROPICAL_FISH_BUCKET);
		this.tag(ItemTags.CLUSTER_MAX_HARVESTABLES)
			.add(Items.DIAMOND_PICKAXE, Items.GOLDEN_PICKAXE, Items.IRON_PICKAXE, Items.NETHERITE_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE);
		this.tag(ItemTags.COMPASSES).add(Items.COMPASS).add(Items.RECOVERY_COMPASS);
		this.tag(ItemTags.CREEPER_IGNITERS).add(Items.FLINT_AND_STEEL).add(Items.FIRE_CHARGE);
		this.tag(ItemTags.SWORDS)
			.add(Items.DIAMOND_SWORD)
			.add(Items.STONE_SWORD)
			.add(Items.GOLDEN_SWORD)
			.add(Items.NETHERITE_SWORD)
			.add(Items.WOODEN_SWORD)
			.add(Items.IRON_SWORD);
		this.tag(ItemTags.AXES).add(Items.DIAMOND_AXE).add(Items.STONE_AXE).add(Items.GOLDEN_AXE).add(Items.NETHERITE_AXE).add(Items.WOODEN_AXE).add(Items.IRON_AXE);
		this.tag(ItemTags.PICKAXES)
			.add(Items.DIAMOND_PICKAXE)
			.add(Items.STONE_PICKAXE)
			.add(Items.GOLDEN_PICKAXE)
			.add(Items.NETHERITE_PICKAXE)
			.add(Items.WOODEN_PICKAXE)
			.add(Items.IRON_PICKAXE);
		this.tag(ItemTags.SHOVELS)
			.add(Items.DIAMOND_SHOVEL)
			.add(Items.STONE_SHOVEL)
			.add(Items.GOLDEN_SHOVEL)
			.add(Items.NETHERITE_SHOVEL)
			.add(Items.WOODEN_SHOVEL)
			.add(Items.IRON_SHOVEL);
		this.tag(ItemTags.HOES).add(Items.DIAMOND_HOE).add(Items.STONE_HOE).add(Items.GOLDEN_HOE).add(Items.NETHERITE_HOE).add(Items.WOODEN_HOE).add(Items.IRON_HOE);
		this.tag(ItemTags.TOOLS)
			.addTag(ItemTags.SWORDS)
			.addTag(ItemTags.AXES)
			.addTag(ItemTags.PICKAXES)
			.addTag(ItemTags.SHOVELS)
			.addTag(ItemTags.HOES)
			.add(Items.TRIDENT);
		this.tag(ItemTags.BREAKS_DECORATED_POTS).addTag(ItemTags.TOOLS);
		this.tag(ItemTags.DECORATED_POT_SHARDS)
			.add(
				Items.BRICK,
				Items.ANGLER_POTTERY_SHARD,
				Items.ARCHER_POTTERY_SHARD,
				Items.ARMS_UP_POTTERY_SHARD,
				Items.BLADE_POTTERY_SHARD,
				Items.BREWER_POTTERY_SHARD,
				Items.BURN_POTTERY_SHARD,
				Items.DANGER_POTTERY_SHARD,
				Items.EXPLORER_POTTERY_SHARD,
				Items.FRIEND_POTTERY_SHARD,
				Items.HEART_POTTERY_SHARD,
				Items.HEARTBREAK_POTTERY_SHARD,
				Items.HOWL_POTTERY_SHARD,
				Items.MINER_POTTERY_SHARD,
				Items.MOURNER_POTTERY_SHARD,
				Items.PLENTY_POTTERY_SHARD,
				Items.PRIZE_POTTERY_SHARD,
				Items.SHEAF_POTTERY_SHARD,
				Items.SHELTER_POTTERY_SHARD,
				Items.SKULL_POTTERY_SHARD,
				Items.SNORT_POTTERY_SHARD
			);
		this.tag(ItemTags.TRIMMABLE_ARMOR)
			.add(Items.NETHERITE_HELMET)
			.add(Items.NETHERITE_CHESTPLATE)
			.add(Items.NETHERITE_LEGGINGS)
			.add(Items.NETHERITE_BOOTS)
			.add(Items.DIAMOND_HELMET)
			.add(Items.DIAMOND_CHESTPLATE)
			.add(Items.DIAMOND_LEGGINGS)
			.add(Items.DIAMOND_BOOTS)
			.add(Items.GOLDEN_HELMET)
			.add(Items.GOLDEN_CHESTPLATE)
			.add(Items.GOLDEN_LEGGINGS)
			.add(Items.GOLDEN_BOOTS)
			.add(Items.IRON_HELMET)
			.add(Items.IRON_CHESTPLATE)
			.add(Items.IRON_LEGGINGS)
			.add(Items.IRON_BOOTS)
			.add(Items.CHAINMAIL_HELMET)
			.add(Items.CHAINMAIL_CHESTPLATE)
			.add(Items.CHAINMAIL_LEGGINGS)
			.add(Items.CHAINMAIL_BOOTS)
			.add(Items.LEATHER_HELMET)
			.add(Items.LEATHER_CHESTPLATE)
			.add(Items.LEATHER_LEGGINGS)
			.add(Items.LEATHER_BOOTS)
			.add(Items.TURTLE_HELMET);
		this.tag(ItemTags.TRIM_MATERIALS)
			.add(Items.IRON_INGOT)
			.add(Items.COPPER_INGOT)
			.add(Items.GOLD_INGOT)
			.add(Items.LAPIS_LAZULI)
			.add(Items.EMERALD)
			.add(Items.DIAMOND)
			.add(Items.NETHERITE_INGOT)
			.add(Items.REDSTONE)
			.add(Items.QUARTZ)
			.add(Items.AMETHYST_SHARD);
		this.tag(ItemTags.TRIM_TEMPLATES)
			.add(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE)
			.add(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE);
		this.tag(ItemTags.BOOKSHELF_BOOKS).add(Items.BOOK, Items.WRITTEN_BOOK, Items.ENCHANTED_BOOK, Items.WRITABLE_BOOK);
		this.tag(ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS)
			.add(Items.ZOMBIE_HEAD, Items.SKELETON_SKULL, Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.WITHER_SKELETON_SKULL, Items.PIGLIN_HEAD, Items.PLAYER_HEAD);
		this.tag(ItemTags.SNIFFER_FOOD).add(Items.TORCHFLOWER_SEEDS);
	}
}
