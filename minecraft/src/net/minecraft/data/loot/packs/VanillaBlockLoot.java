package net.minecraft.data.loot.packs;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.PotatoBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class VanillaBlockLoot extends BlockLootSubProvider {
	private static final float[] JUNGLE_LEAVES_SAPLING_CHANGES = new float[]{0.025F, 0.027777778F, 0.03125F, 0.041666668F, 0.1F};
	private static final Set<Item> EXPLOSION_RESISTANT = (Set<Item>)Stream.of(
			Blocks.DRAGON_EGG,
			Blocks.BEACON,
			Blocks.CONDUIT,
			Blocks.SKELETON_SKULL,
			Blocks.WITHER_SKELETON_SKULL,
			Blocks.PLAYER_HEAD,
			Blocks.ZOMBIE_HEAD,
			Blocks.CREEPER_HEAD,
			Blocks.DRAGON_HEAD,
			Blocks.PIGLIN_HEAD,
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
		)
		.map(ItemLike::asItem)
		.collect(Collectors.toSet());

	public VanillaBlockLoot() {
		super(EXPLOSION_RESISTANT, FeatureFlags.REGISTRY.allFlags());
	}

	@Override
	protected void generate() {
		this.dropSelf(Blocks.GRANITE);
		this.dropSelf(Blocks.POLISHED_GRANITE);
		this.dropSelf(Blocks.DIORITE);
		this.dropSelf(Blocks.POLISHED_DIORITE);
		this.dropSelf(Blocks.ANDESITE);
		this.dropSelf(Blocks.POLISHED_ANDESITE);
		this.dropSelf(Blocks.DIRT);
		this.dropSelf(Blocks.COARSE_DIRT);
		this.dropSelf(Blocks.COBBLESTONE);
		this.dropSelf(Blocks.OAK_PLANKS);
		this.dropSelf(Blocks.SPRUCE_PLANKS);
		this.dropSelf(Blocks.BIRCH_PLANKS);
		this.dropSelf(Blocks.JUNGLE_PLANKS);
		this.dropSelf(Blocks.ACACIA_PLANKS);
		this.dropSelf(Blocks.DARK_OAK_PLANKS);
		this.dropSelf(Blocks.MANGROVE_PLANKS);
		this.dropSelf(Blocks.CHERRY_PLANKS);
		this.dropSelf(Blocks.BAMBOO_PLANKS);
		this.dropSelf(Blocks.BAMBOO_MOSAIC);
		this.add(Blocks.DECORATED_POT, this::createDecoratedPotTable);
		this.dropSelf(Blocks.OAK_SAPLING);
		this.dropSelf(Blocks.SPRUCE_SAPLING);
		this.dropSelf(Blocks.BIRCH_SAPLING);
		this.dropSelf(Blocks.JUNGLE_SAPLING);
		this.dropSelf(Blocks.ACACIA_SAPLING);
		this.dropSelf(Blocks.DARK_OAK_SAPLING);
		this.dropSelf(Blocks.CHERRY_SAPLING);
		this.dropSelf(Blocks.SAND);
		this.add(Blocks.SUSPICIOUS_SAND, noDrop());
		this.add(Blocks.SUSPICIOUS_GRAVEL, noDrop());
		this.dropSelf(Blocks.RED_SAND);
		this.dropSelf(Blocks.OAK_LOG);
		this.dropSelf(Blocks.SPRUCE_LOG);
		this.dropSelf(Blocks.BIRCH_LOG);
		this.dropSelf(Blocks.JUNGLE_LOG);
		this.dropSelf(Blocks.ACACIA_LOG);
		this.dropSelf(Blocks.DARK_OAK_LOG);
		this.dropSelf(Blocks.CHERRY_LOG);
		this.dropSelf(Blocks.BAMBOO_BLOCK);
		this.dropSelf(Blocks.STRIPPED_OAK_LOG);
		this.dropSelf(Blocks.STRIPPED_SPRUCE_LOG);
		this.dropSelf(Blocks.STRIPPED_BIRCH_LOG);
		this.dropSelf(Blocks.STRIPPED_JUNGLE_LOG);
		this.dropSelf(Blocks.STRIPPED_ACACIA_LOG);
		this.dropSelf(Blocks.STRIPPED_DARK_OAK_LOG);
		this.dropSelf(Blocks.STRIPPED_MANGROVE_LOG);
		this.dropSelf(Blocks.STRIPPED_CHERRY_LOG);
		this.dropSelf(Blocks.STRIPPED_BAMBOO_BLOCK);
		this.dropSelf(Blocks.STRIPPED_WARPED_STEM);
		this.dropSelf(Blocks.STRIPPED_CRIMSON_STEM);
		this.dropSelf(Blocks.OAK_WOOD);
		this.dropSelf(Blocks.SPRUCE_WOOD);
		this.dropSelf(Blocks.BIRCH_WOOD);
		this.dropSelf(Blocks.JUNGLE_WOOD);
		this.dropSelf(Blocks.ACACIA_WOOD);
		this.dropSelf(Blocks.DARK_OAK_WOOD);
		this.dropSelf(Blocks.MANGROVE_WOOD);
		this.dropSelf(Blocks.CHERRY_WOOD);
		this.dropSelf(Blocks.STRIPPED_OAK_WOOD);
		this.dropSelf(Blocks.STRIPPED_SPRUCE_WOOD);
		this.dropSelf(Blocks.STRIPPED_BIRCH_WOOD);
		this.dropSelf(Blocks.STRIPPED_JUNGLE_WOOD);
		this.dropSelf(Blocks.STRIPPED_ACACIA_WOOD);
		this.dropSelf(Blocks.STRIPPED_DARK_OAK_WOOD);
		this.dropSelf(Blocks.STRIPPED_MANGROVE_WOOD);
		this.dropSelf(Blocks.STRIPPED_CHERRY_WOOD);
		this.dropSelf(Blocks.STRIPPED_CRIMSON_HYPHAE);
		this.dropSelf(Blocks.STRIPPED_WARPED_HYPHAE);
		this.dropSelf(Blocks.SPONGE);
		this.dropSelf(Blocks.WET_SPONGE);
		this.dropSelf(Blocks.LAPIS_BLOCK);
		this.dropSelf(Blocks.SANDSTONE);
		this.dropSelf(Blocks.CHISELED_SANDSTONE);
		this.dropSelf(Blocks.CUT_SANDSTONE);
		this.dropSelf(Blocks.NOTE_BLOCK);
		this.dropSelf(Blocks.POWERED_RAIL);
		this.dropSelf(Blocks.DETECTOR_RAIL);
		this.dropSelf(Blocks.STICKY_PISTON);
		this.dropSelf(Blocks.PISTON);
		this.dropSelf(Blocks.WHITE_WOOL);
		this.dropSelf(Blocks.ORANGE_WOOL);
		this.dropSelf(Blocks.MAGENTA_WOOL);
		this.dropSelf(Blocks.LIGHT_BLUE_WOOL);
		this.dropSelf(Blocks.YELLOW_WOOL);
		this.dropSelf(Blocks.LIME_WOOL);
		this.dropSelf(Blocks.PINK_WOOL);
		this.dropSelf(Blocks.GRAY_WOOL);
		this.dropSelf(Blocks.LIGHT_GRAY_WOOL);
		this.dropSelf(Blocks.CYAN_WOOL);
		this.dropSelf(Blocks.PURPLE_WOOL);
		this.dropSelf(Blocks.BLUE_WOOL);
		this.dropSelf(Blocks.BROWN_WOOL);
		this.dropSelf(Blocks.GREEN_WOOL);
		this.dropSelf(Blocks.RED_WOOL);
		this.dropSelf(Blocks.BLACK_WOOL);
		this.dropSelf(Blocks.DANDELION);
		this.dropSelf(Blocks.POPPY);
		this.dropSelf(Blocks.TORCHFLOWER);
		this.dropSelf(Blocks.BLUE_ORCHID);
		this.dropSelf(Blocks.ALLIUM);
		this.dropSelf(Blocks.AZURE_BLUET);
		this.dropSelf(Blocks.RED_TULIP);
		this.dropSelf(Blocks.ORANGE_TULIP);
		this.dropSelf(Blocks.WHITE_TULIP);
		this.dropSelf(Blocks.PINK_TULIP);
		this.dropSelf(Blocks.OXEYE_DAISY);
		this.dropSelf(Blocks.CORNFLOWER);
		this.dropSelf(Blocks.WITHER_ROSE);
		this.dropSelf(Blocks.LILY_OF_THE_VALLEY);
		this.dropSelf(Blocks.BROWN_MUSHROOM);
		this.dropSelf(Blocks.RED_MUSHROOM);
		this.dropSelf(Blocks.GOLD_BLOCK);
		this.dropSelf(Blocks.IRON_BLOCK);
		this.dropSelf(Blocks.BRICKS);
		this.dropSelf(Blocks.MOSSY_COBBLESTONE);
		this.dropSelf(Blocks.OBSIDIAN);
		this.dropSelf(Blocks.CRYING_OBSIDIAN);
		this.dropSelf(Blocks.TORCH);
		this.dropSelf(Blocks.OAK_STAIRS);
		this.dropSelf(Blocks.MANGROVE_STAIRS);
		this.dropSelf(Blocks.BAMBOO_STAIRS);
		this.dropSelf(Blocks.BAMBOO_MOSAIC_STAIRS);
		this.dropSelf(Blocks.REDSTONE_WIRE);
		this.dropSelf(Blocks.DIAMOND_BLOCK);
		this.dropSelf(Blocks.CRAFTING_TABLE);
		this.dropSelf(Blocks.OAK_SIGN);
		this.dropSelf(Blocks.SPRUCE_SIGN);
		this.dropSelf(Blocks.BIRCH_SIGN);
		this.dropSelf(Blocks.ACACIA_SIGN);
		this.dropSelf(Blocks.JUNGLE_SIGN);
		this.dropSelf(Blocks.DARK_OAK_SIGN);
		this.dropSelf(Blocks.MANGROVE_SIGN);
		this.dropSelf(Blocks.CHERRY_SIGN);
		this.dropSelf(Blocks.BAMBOO_SIGN);
		this.dropSelf(Blocks.OAK_HANGING_SIGN);
		this.dropSelf(Blocks.SPRUCE_HANGING_SIGN);
		this.dropSelf(Blocks.BIRCH_HANGING_SIGN);
		this.dropSelf(Blocks.ACACIA_HANGING_SIGN);
		this.dropSelf(Blocks.CHERRY_HANGING_SIGN);
		this.dropSelf(Blocks.JUNGLE_HANGING_SIGN);
		this.dropSelf(Blocks.DARK_OAK_HANGING_SIGN);
		this.dropSelf(Blocks.MANGROVE_HANGING_SIGN);
		this.dropSelf(Blocks.CRIMSON_HANGING_SIGN);
		this.dropSelf(Blocks.WARPED_HANGING_SIGN);
		this.dropSelf(Blocks.BAMBOO_HANGING_SIGN);
		this.dropSelf(Blocks.LADDER);
		this.dropSelf(Blocks.RAIL);
		this.dropSelf(Blocks.COBBLESTONE_STAIRS);
		this.dropSelf(Blocks.LEVER);
		this.dropSelf(Blocks.STONE_PRESSURE_PLATE);
		this.dropSelf(Blocks.OAK_PRESSURE_PLATE);
		this.dropSelf(Blocks.SPRUCE_PRESSURE_PLATE);
		this.dropSelf(Blocks.BIRCH_PRESSURE_PLATE);
		this.dropSelf(Blocks.JUNGLE_PRESSURE_PLATE);
		this.dropSelf(Blocks.ACACIA_PRESSURE_PLATE);
		this.dropSelf(Blocks.DARK_OAK_PRESSURE_PLATE);
		this.dropSelf(Blocks.MANGROVE_PRESSURE_PLATE);
		this.dropSelf(Blocks.CHERRY_PRESSURE_PLATE);
		this.dropSelf(Blocks.BAMBOO_PRESSURE_PLATE);
		this.dropSelf(Blocks.REDSTONE_TORCH);
		this.dropSelf(Blocks.STONE_BUTTON);
		this.dropSelf(Blocks.CACTUS);
		this.dropSelf(Blocks.SUGAR_CANE);
		this.dropSelf(Blocks.JUKEBOX);
		this.dropSelf(Blocks.OAK_FENCE);
		this.dropSelf(Blocks.MANGROVE_FENCE);
		this.dropSelf(Blocks.BAMBOO_FENCE);
		this.dropSelf(Blocks.PUMPKIN);
		this.dropSelf(Blocks.NETHERRACK);
		this.dropSelf(Blocks.SOUL_SAND);
		this.dropSelf(Blocks.SOUL_SOIL);
		this.dropSelf(Blocks.BASALT);
		this.dropSelf(Blocks.POLISHED_BASALT);
		this.dropSelf(Blocks.SMOOTH_BASALT);
		this.dropSelf(Blocks.SOUL_TORCH);
		this.dropSelf(Blocks.CARVED_PUMPKIN);
		this.dropSelf(Blocks.JACK_O_LANTERN);
		this.dropSelf(Blocks.REPEATER);
		this.dropSelf(Blocks.OAK_TRAPDOOR);
		this.dropSelf(Blocks.SPRUCE_TRAPDOOR);
		this.dropSelf(Blocks.BIRCH_TRAPDOOR);
		this.dropSelf(Blocks.JUNGLE_TRAPDOOR);
		this.dropSelf(Blocks.ACACIA_TRAPDOOR);
		this.dropSelf(Blocks.DARK_OAK_TRAPDOOR);
		this.dropSelf(Blocks.MANGROVE_TRAPDOOR);
		this.dropSelf(Blocks.CHERRY_TRAPDOOR);
		this.dropSelf(Blocks.BAMBOO_TRAPDOOR);
		this.add(Blocks.COPPER_TRAPDOOR, noDrop());
		this.add(Blocks.EXPOSED_COPPER_TRAPDOOR, noDrop());
		this.add(Blocks.WEATHERED_COPPER_TRAPDOOR, noDrop());
		this.add(Blocks.OXIDIZED_COPPER_TRAPDOOR, noDrop());
		this.add(Blocks.WAXED_COPPER_TRAPDOOR, noDrop());
		this.add(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, noDrop());
		this.add(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, noDrop());
		this.add(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR, noDrop());
		this.dropSelf(Blocks.STONE_BRICKS);
		this.dropSelf(Blocks.MOSSY_STONE_BRICKS);
		this.dropSelf(Blocks.CRACKED_STONE_BRICKS);
		this.dropSelf(Blocks.CHISELED_STONE_BRICKS);
		this.dropSelf(Blocks.IRON_BARS);
		this.dropSelf(Blocks.OAK_FENCE_GATE);
		this.dropSelf(Blocks.MANGROVE_FENCE_GATE);
		this.dropSelf(Blocks.BAMBOO_FENCE_GATE);
		this.dropSelf(Blocks.BRICK_STAIRS);
		this.dropSelf(Blocks.STONE_BRICK_STAIRS);
		this.dropSelf(Blocks.LILY_PAD);
		this.dropSelf(Blocks.NETHER_BRICKS);
		this.dropSelf(Blocks.NETHER_BRICK_FENCE);
		this.dropSelf(Blocks.NETHER_BRICK_STAIRS);
		this.dropSelf(Blocks.CAULDRON);
		this.dropSelf(Blocks.END_STONE);
		this.dropSelf(Blocks.REDSTONE_LAMP);
		this.dropSelf(Blocks.SANDSTONE_STAIRS);
		this.dropSelf(Blocks.TRIPWIRE_HOOK);
		this.dropSelf(Blocks.EMERALD_BLOCK);
		this.dropSelf(Blocks.SPRUCE_STAIRS);
		this.dropSelf(Blocks.BIRCH_STAIRS);
		this.dropSelf(Blocks.JUNGLE_STAIRS);
		this.dropSelf(Blocks.COBBLESTONE_WALL);
		this.dropSelf(Blocks.MOSSY_COBBLESTONE_WALL);
		this.dropSelf(Blocks.FLOWER_POT);
		this.dropSelf(Blocks.OAK_BUTTON);
		this.dropSelf(Blocks.SPRUCE_BUTTON);
		this.dropSelf(Blocks.BIRCH_BUTTON);
		this.dropSelf(Blocks.JUNGLE_BUTTON);
		this.dropSelf(Blocks.ACACIA_BUTTON);
		this.dropSelf(Blocks.DARK_OAK_BUTTON);
		this.dropSelf(Blocks.MANGROVE_BUTTON);
		this.dropSelf(Blocks.CHERRY_BUTTON);
		this.dropSelf(Blocks.BAMBOO_BUTTON);
		this.dropSelf(Blocks.SKELETON_SKULL);
		this.dropSelf(Blocks.WITHER_SKELETON_SKULL);
		this.dropSelf(Blocks.ZOMBIE_HEAD);
		this.dropSelf(Blocks.CREEPER_HEAD);
		this.dropSelf(Blocks.DRAGON_HEAD);
		this.dropSelf(Blocks.PIGLIN_HEAD);
		this.dropSelf(Blocks.ANVIL);
		this.dropSelf(Blocks.CHIPPED_ANVIL);
		this.dropSelf(Blocks.DAMAGED_ANVIL);
		this.dropSelf(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
		this.dropSelf(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
		this.dropSelf(Blocks.COMPARATOR);
		this.dropSelf(Blocks.DAYLIGHT_DETECTOR);
		this.dropSelf(Blocks.REDSTONE_BLOCK);
		this.dropSelf(Blocks.QUARTZ_BLOCK);
		this.dropSelf(Blocks.CHISELED_QUARTZ_BLOCK);
		this.dropSelf(Blocks.QUARTZ_PILLAR);
		this.dropSelf(Blocks.QUARTZ_STAIRS);
		this.dropSelf(Blocks.ACTIVATOR_RAIL);
		this.dropSelf(Blocks.WHITE_TERRACOTTA);
		this.dropSelf(Blocks.ORANGE_TERRACOTTA);
		this.dropSelf(Blocks.MAGENTA_TERRACOTTA);
		this.dropSelf(Blocks.LIGHT_BLUE_TERRACOTTA);
		this.dropSelf(Blocks.YELLOW_TERRACOTTA);
		this.dropSelf(Blocks.LIME_TERRACOTTA);
		this.dropSelf(Blocks.PINK_TERRACOTTA);
		this.dropSelf(Blocks.GRAY_TERRACOTTA);
		this.dropSelf(Blocks.LIGHT_GRAY_TERRACOTTA);
		this.dropSelf(Blocks.CYAN_TERRACOTTA);
		this.dropSelf(Blocks.PURPLE_TERRACOTTA);
		this.dropSelf(Blocks.BLUE_TERRACOTTA);
		this.dropSelf(Blocks.BROWN_TERRACOTTA);
		this.dropSelf(Blocks.GREEN_TERRACOTTA);
		this.dropSelf(Blocks.RED_TERRACOTTA);
		this.dropSelf(Blocks.BLACK_TERRACOTTA);
		this.dropSelf(Blocks.ACACIA_STAIRS);
		this.dropSelf(Blocks.DARK_OAK_STAIRS);
		this.dropSelf(Blocks.CHERRY_STAIRS);
		this.dropSelf(Blocks.SLIME_BLOCK);
		this.dropSelf(Blocks.IRON_TRAPDOOR);
		this.dropSelf(Blocks.PRISMARINE);
		this.dropSelf(Blocks.PRISMARINE_BRICKS);
		this.dropSelf(Blocks.DARK_PRISMARINE);
		this.dropSelf(Blocks.PRISMARINE_STAIRS);
		this.dropSelf(Blocks.PRISMARINE_BRICK_STAIRS);
		this.dropSelf(Blocks.DARK_PRISMARINE_STAIRS);
		this.dropSelf(Blocks.HAY_BLOCK);
		this.dropSelf(Blocks.WHITE_CARPET);
		this.dropSelf(Blocks.ORANGE_CARPET);
		this.dropSelf(Blocks.MAGENTA_CARPET);
		this.dropSelf(Blocks.LIGHT_BLUE_CARPET);
		this.dropSelf(Blocks.YELLOW_CARPET);
		this.dropSelf(Blocks.LIME_CARPET);
		this.dropSelf(Blocks.PINK_CARPET);
		this.dropSelf(Blocks.GRAY_CARPET);
		this.dropSelf(Blocks.LIGHT_GRAY_CARPET);
		this.dropSelf(Blocks.CYAN_CARPET);
		this.dropSelf(Blocks.PURPLE_CARPET);
		this.dropSelf(Blocks.BLUE_CARPET);
		this.dropSelf(Blocks.BROWN_CARPET);
		this.dropSelf(Blocks.GREEN_CARPET);
		this.dropSelf(Blocks.RED_CARPET);
		this.dropSelf(Blocks.BLACK_CARPET);
		this.dropSelf(Blocks.TERRACOTTA);
		this.dropSelf(Blocks.COAL_BLOCK);
		this.dropSelf(Blocks.RED_SANDSTONE);
		this.dropSelf(Blocks.CHISELED_RED_SANDSTONE);
		this.dropSelf(Blocks.CUT_RED_SANDSTONE);
		this.dropSelf(Blocks.RED_SANDSTONE_STAIRS);
		this.dropSelf(Blocks.SMOOTH_STONE);
		this.dropSelf(Blocks.SMOOTH_SANDSTONE);
		this.dropSelf(Blocks.SMOOTH_QUARTZ);
		this.dropSelf(Blocks.SMOOTH_RED_SANDSTONE);
		this.dropSelf(Blocks.SPRUCE_FENCE_GATE);
		this.dropSelf(Blocks.BIRCH_FENCE_GATE);
		this.dropSelf(Blocks.JUNGLE_FENCE_GATE);
		this.dropSelf(Blocks.ACACIA_FENCE_GATE);
		this.dropSelf(Blocks.DARK_OAK_FENCE_GATE);
		this.dropSelf(Blocks.CHERRY_FENCE_GATE);
		this.dropSelf(Blocks.SPRUCE_FENCE);
		this.dropSelf(Blocks.BIRCH_FENCE);
		this.dropSelf(Blocks.JUNGLE_FENCE);
		this.dropSelf(Blocks.ACACIA_FENCE);
		this.dropSelf(Blocks.DARK_OAK_FENCE);
		this.dropSelf(Blocks.CHERRY_FENCE);
		this.dropSelf(Blocks.END_ROD);
		this.dropSelf(Blocks.PURPUR_BLOCK);
		this.dropSelf(Blocks.PURPUR_PILLAR);
		this.dropSelf(Blocks.PURPUR_STAIRS);
		this.dropSelf(Blocks.END_STONE_BRICKS);
		this.dropSelf(Blocks.MAGMA_BLOCK);
		this.dropSelf(Blocks.NETHER_WART_BLOCK);
		this.dropSelf(Blocks.RED_NETHER_BRICKS);
		this.dropSelf(Blocks.BONE_BLOCK);
		this.dropSelf(Blocks.OBSERVER);
		this.dropSelf(Blocks.TARGET);
		this.dropSelf(Blocks.WHITE_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.ORANGE_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.MAGENTA_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.YELLOW_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.LIME_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.PINK_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.GRAY_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.CYAN_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.PURPLE_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.BLUE_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.BROWN_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.GREEN_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.RED_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.BLACK_GLAZED_TERRACOTTA);
		this.dropSelf(Blocks.WHITE_CONCRETE);
		this.dropSelf(Blocks.ORANGE_CONCRETE);
		this.dropSelf(Blocks.MAGENTA_CONCRETE);
		this.dropSelf(Blocks.LIGHT_BLUE_CONCRETE);
		this.dropSelf(Blocks.YELLOW_CONCRETE);
		this.dropSelf(Blocks.LIME_CONCRETE);
		this.dropSelf(Blocks.PINK_CONCRETE);
		this.dropSelf(Blocks.GRAY_CONCRETE);
		this.dropSelf(Blocks.LIGHT_GRAY_CONCRETE);
		this.dropSelf(Blocks.CYAN_CONCRETE);
		this.dropSelf(Blocks.PURPLE_CONCRETE);
		this.dropSelf(Blocks.BLUE_CONCRETE);
		this.dropSelf(Blocks.BROWN_CONCRETE);
		this.dropSelf(Blocks.GREEN_CONCRETE);
		this.dropSelf(Blocks.RED_CONCRETE);
		this.dropSelf(Blocks.BLACK_CONCRETE);
		this.dropSelf(Blocks.WHITE_CONCRETE_POWDER);
		this.dropSelf(Blocks.ORANGE_CONCRETE_POWDER);
		this.dropSelf(Blocks.MAGENTA_CONCRETE_POWDER);
		this.dropSelf(Blocks.LIGHT_BLUE_CONCRETE_POWDER);
		this.dropSelf(Blocks.YELLOW_CONCRETE_POWDER);
		this.dropSelf(Blocks.LIME_CONCRETE_POWDER);
		this.dropSelf(Blocks.PINK_CONCRETE_POWDER);
		this.dropSelf(Blocks.GRAY_CONCRETE_POWDER);
		this.dropSelf(Blocks.LIGHT_GRAY_CONCRETE_POWDER);
		this.dropSelf(Blocks.CYAN_CONCRETE_POWDER);
		this.dropSelf(Blocks.PURPLE_CONCRETE_POWDER);
		this.dropSelf(Blocks.BLUE_CONCRETE_POWDER);
		this.dropSelf(Blocks.BROWN_CONCRETE_POWDER);
		this.dropSelf(Blocks.GREEN_CONCRETE_POWDER);
		this.dropSelf(Blocks.RED_CONCRETE_POWDER);
		this.dropSelf(Blocks.BLACK_CONCRETE_POWDER);
		this.dropSelf(Blocks.KELP);
		this.dropSelf(Blocks.DRIED_KELP_BLOCK);
		this.dropSelf(Blocks.DEAD_TUBE_CORAL_BLOCK);
		this.dropSelf(Blocks.DEAD_BRAIN_CORAL_BLOCK);
		this.dropSelf(Blocks.DEAD_BUBBLE_CORAL_BLOCK);
		this.dropSelf(Blocks.DEAD_FIRE_CORAL_BLOCK);
		this.dropSelf(Blocks.DEAD_HORN_CORAL_BLOCK);
		this.dropSelf(Blocks.CONDUIT);
		this.dropSelf(Blocks.DRAGON_EGG);
		this.dropSelf(Blocks.BAMBOO);
		this.dropSelf(Blocks.POLISHED_GRANITE_STAIRS);
		this.dropSelf(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
		this.dropSelf(Blocks.MOSSY_STONE_BRICK_STAIRS);
		this.dropSelf(Blocks.POLISHED_DIORITE_STAIRS);
		this.dropSelf(Blocks.MOSSY_COBBLESTONE_STAIRS);
		this.dropSelf(Blocks.END_STONE_BRICK_STAIRS);
		this.dropSelf(Blocks.STONE_STAIRS);
		this.dropSelf(Blocks.SMOOTH_SANDSTONE_STAIRS);
		this.dropSelf(Blocks.SMOOTH_QUARTZ_STAIRS);
		this.dropSelf(Blocks.GRANITE_STAIRS);
		this.dropSelf(Blocks.ANDESITE_STAIRS);
		this.dropSelf(Blocks.RED_NETHER_BRICK_STAIRS);
		this.dropSelf(Blocks.POLISHED_ANDESITE_STAIRS);
		this.dropSelf(Blocks.DIORITE_STAIRS);
		this.dropSelf(Blocks.BRICK_WALL);
		this.dropSelf(Blocks.PRISMARINE_WALL);
		this.dropSelf(Blocks.RED_SANDSTONE_WALL);
		this.dropSelf(Blocks.MOSSY_STONE_BRICK_WALL);
		this.dropSelf(Blocks.GRANITE_WALL);
		this.dropSelf(Blocks.STONE_BRICK_WALL);
		this.dropSelf(Blocks.NETHER_BRICK_WALL);
		this.dropSelf(Blocks.ANDESITE_WALL);
		this.dropSelf(Blocks.RED_NETHER_BRICK_WALL);
		this.dropSelf(Blocks.SANDSTONE_WALL);
		this.dropSelf(Blocks.END_STONE_BRICK_WALL);
		this.dropSelf(Blocks.DIORITE_WALL);
		this.dropSelf(Blocks.MUD_BRICK_WALL);
		this.dropSelf(Blocks.LOOM);
		this.dropSelf(Blocks.SCAFFOLDING);
		this.dropSelf(Blocks.HONEY_BLOCK);
		this.dropSelf(Blocks.HONEYCOMB_BLOCK);
		this.dropSelf(Blocks.RESPAWN_ANCHOR);
		this.dropSelf(Blocks.LODESTONE);
		this.dropSelf(Blocks.WARPED_STEM);
		this.dropSelf(Blocks.WARPED_HYPHAE);
		this.dropSelf(Blocks.WARPED_FUNGUS);
		this.dropSelf(Blocks.WARPED_WART_BLOCK);
		this.dropSelf(Blocks.CRIMSON_STEM);
		this.dropSelf(Blocks.CRIMSON_HYPHAE);
		this.dropSelf(Blocks.CRIMSON_FUNGUS);
		this.dropSelf(Blocks.SHROOMLIGHT);
		this.dropSelf(Blocks.CRIMSON_PLANKS);
		this.dropSelf(Blocks.WARPED_PLANKS);
		this.dropSelf(Blocks.WARPED_PRESSURE_PLATE);
		this.dropSelf(Blocks.WARPED_FENCE);
		this.dropSelf(Blocks.WARPED_TRAPDOOR);
		this.dropSelf(Blocks.WARPED_FENCE_GATE);
		this.dropSelf(Blocks.WARPED_STAIRS);
		this.dropSelf(Blocks.WARPED_BUTTON);
		this.dropSelf(Blocks.WARPED_SIGN);
		this.dropSelf(Blocks.CRIMSON_PRESSURE_PLATE);
		this.dropSelf(Blocks.CRIMSON_FENCE);
		this.dropSelf(Blocks.CRIMSON_TRAPDOOR);
		this.dropSelf(Blocks.CRIMSON_FENCE_GATE);
		this.dropSelf(Blocks.CRIMSON_STAIRS);
		this.dropSelf(Blocks.CRIMSON_BUTTON);
		this.dropSelf(Blocks.CRIMSON_SIGN);
		this.dropSelf(Blocks.NETHERITE_BLOCK);
		this.dropSelf(Blocks.ANCIENT_DEBRIS);
		this.dropSelf(Blocks.BLACKSTONE);
		this.dropSelf(Blocks.POLISHED_BLACKSTONE_BRICKS);
		this.dropSelf(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
		this.dropSelf(Blocks.BLACKSTONE_STAIRS);
		this.dropSelf(Blocks.BLACKSTONE_WALL);
		this.dropSelf(Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
		this.dropSelf(Blocks.CHISELED_POLISHED_BLACKSTONE);
		this.dropSelf(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
		this.dropSelf(Blocks.POLISHED_BLACKSTONE);
		this.dropSelf(Blocks.POLISHED_BLACKSTONE_STAIRS);
		this.dropSelf(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE);
		this.dropSelf(Blocks.POLISHED_BLACKSTONE_BUTTON);
		this.dropSelf(Blocks.POLISHED_BLACKSTONE_WALL);
		this.dropSelf(Blocks.CHISELED_NETHER_BRICKS);
		this.dropSelf(Blocks.CRACKED_NETHER_BRICKS);
		this.dropSelf(Blocks.QUARTZ_BRICKS);
		this.dropSelf(Blocks.CHAIN);
		this.dropSelf(Blocks.WARPED_ROOTS);
		this.dropSelf(Blocks.CRIMSON_ROOTS);
		this.dropSelf(Blocks.MUD_BRICKS);
		this.dropSelf(Blocks.MUDDY_MANGROVE_ROOTS);
		this.dropSelf(Blocks.MUD_BRICK_STAIRS);
		this.dropSelf(Blocks.AMETHYST_BLOCK);
		this.dropSelf(Blocks.CALCITE);
		this.dropSelf(Blocks.TUFF);
		this.dropSelf(Blocks.TINTED_GLASS);
		this.dropWhenSilkTouch(Blocks.SCULK_SENSOR);
		this.dropWhenSilkTouch(Blocks.CALIBRATED_SCULK_SENSOR);
		this.dropWhenSilkTouch(Blocks.SCULK);
		this.dropWhenSilkTouch(Blocks.SCULK_CATALYST);
		this.add(Blocks.SCULK_VEIN, block -> this.createMultifaceBlockDrops(block, HAS_SILK_TOUCH));
		this.dropWhenSilkTouch(Blocks.SCULK_SHRIEKER);
		this.dropWhenSilkTouch(Blocks.CHISELED_BOOKSHELF);
		this.dropSelf(Blocks.COPPER_BLOCK);
		this.dropSelf(Blocks.EXPOSED_COPPER);
		this.dropSelf(Blocks.WEATHERED_COPPER);
		this.dropSelf(Blocks.OXIDIZED_COPPER);
		this.dropSelf(Blocks.CUT_COPPER);
		this.dropSelf(Blocks.EXPOSED_CUT_COPPER);
		this.dropSelf(Blocks.WEATHERED_CUT_COPPER);
		this.dropSelf(Blocks.OXIDIZED_CUT_COPPER);
		this.dropSelf(Blocks.WAXED_COPPER_BLOCK);
		this.dropSelf(Blocks.WAXED_WEATHERED_COPPER);
		this.dropSelf(Blocks.WAXED_EXPOSED_COPPER);
		this.dropSelf(Blocks.WAXED_OXIDIZED_COPPER);
		this.dropSelf(Blocks.WAXED_CUT_COPPER);
		this.dropSelf(Blocks.WAXED_WEATHERED_CUT_COPPER);
		this.dropSelf(Blocks.WAXED_EXPOSED_CUT_COPPER);
		this.dropSelf(Blocks.WAXED_OXIDIZED_CUT_COPPER);
		this.dropSelf(Blocks.WAXED_CUT_COPPER_STAIRS);
		this.dropSelf(Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS);
		this.dropSelf(Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS);
		this.dropSelf(Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS);
		this.dropSelf(Blocks.CUT_COPPER_STAIRS);
		this.dropSelf(Blocks.EXPOSED_CUT_COPPER_STAIRS);
		this.dropSelf(Blocks.WEATHERED_CUT_COPPER_STAIRS);
		this.dropSelf(Blocks.OXIDIZED_CUT_COPPER_STAIRS);
		this.dropSelf(Blocks.LIGHTNING_ROD);
		this.dropSelf(Blocks.POINTED_DRIPSTONE);
		this.dropSelf(Blocks.DRIPSTONE_BLOCK);
		this.dropSelf(Blocks.SPORE_BLOSSOM);
		this.dropSelf(Blocks.FLOWERING_AZALEA);
		this.dropSelf(Blocks.AZALEA);
		this.dropSelf(Blocks.MOSS_CARPET);
		this.add(Blocks.PINK_PETALS, this.createPetalsDrops(Blocks.PINK_PETALS));
		this.dropSelf(Blocks.BIG_DRIPLEAF);
		this.dropSelf(Blocks.MOSS_BLOCK);
		this.dropSelf(Blocks.ROOTED_DIRT);
		this.dropSelf(Blocks.COBBLED_DEEPSLATE);
		this.dropSelf(Blocks.COBBLED_DEEPSLATE_STAIRS);
		this.dropSelf(Blocks.COBBLED_DEEPSLATE_WALL);
		this.dropSelf(Blocks.POLISHED_DEEPSLATE);
		this.dropSelf(Blocks.POLISHED_DEEPSLATE_STAIRS);
		this.dropSelf(Blocks.POLISHED_DEEPSLATE_WALL);
		this.dropSelf(Blocks.DEEPSLATE_TILES);
		this.dropSelf(Blocks.DEEPSLATE_TILE_STAIRS);
		this.dropSelf(Blocks.DEEPSLATE_TILE_WALL);
		this.dropSelf(Blocks.DEEPSLATE_BRICKS);
		this.dropSelf(Blocks.DEEPSLATE_BRICK_STAIRS);
		this.dropSelf(Blocks.DEEPSLATE_BRICK_WALL);
		this.dropSelf(Blocks.CHISELED_DEEPSLATE);
		this.dropSelf(Blocks.CRACKED_DEEPSLATE_BRICKS);
		this.dropSelf(Blocks.CRACKED_DEEPSLATE_TILES);
		this.dropSelf(Blocks.RAW_IRON_BLOCK);
		this.dropSelf(Blocks.RAW_COPPER_BLOCK);
		this.dropSelf(Blocks.RAW_GOLD_BLOCK);
		this.dropSelf(Blocks.OCHRE_FROGLIGHT);
		this.dropSelf(Blocks.VERDANT_FROGLIGHT);
		this.dropSelf(Blocks.PEARLESCENT_FROGLIGHT);
		this.dropSelf(Blocks.MANGROVE_ROOTS);
		this.dropSelf(Blocks.MANGROVE_LOG);
		this.dropSelf(Blocks.MUD);
		this.dropSelf(Blocks.PACKED_MUD);
		this.add(Blocks.CRAFTER, noDrop());
		this.add(Blocks.CHISELED_TUFF, noDrop());
		this.add(Blocks.TUFF_STAIRS, noDrop());
		this.add(Blocks.TUFF_WALL, noDrop());
		this.add(Blocks.POLISHED_TUFF, noDrop());
		this.add(Blocks.POLISHED_TUFF_STAIRS, noDrop());
		this.add(Blocks.POLISHED_TUFF_WALL, noDrop());
		this.add(Blocks.TUFF_BRICKS, noDrop());
		this.add(Blocks.TUFF_BRICK_STAIRS, noDrop());
		this.add(Blocks.TUFF_BRICK_WALL, noDrop());
		this.add(Blocks.CHISELED_TUFF_BRICKS, noDrop());
		this.add(Blocks.TUFF_SLAB, noDrop());
		this.add(Blocks.TUFF_BRICK_SLAB, noDrop());
		this.add(Blocks.POLISHED_TUFF_SLAB, noDrop());
		this.add(Blocks.CHISELED_COPPER, noDrop());
		this.add(Blocks.EXPOSED_CHISELED_COPPER, noDrop());
		this.add(Blocks.WEATHERED_CHISELED_COPPER, noDrop());
		this.add(Blocks.OXIDIZED_CHISELED_COPPER, noDrop());
		this.add(Blocks.WAXED_CHISELED_COPPER, noDrop());
		this.add(Blocks.WAXED_EXPOSED_CHISELED_COPPER, noDrop());
		this.add(Blocks.WAXED_WEATHERED_CHISELED_COPPER, noDrop());
		this.add(Blocks.WAXED_OXIDIZED_CHISELED_COPPER, noDrop());
		this.add(Blocks.COPPER_GRATE, noDrop());
		this.add(Blocks.EXPOSED_COPPER_GRATE, noDrop());
		this.add(Blocks.WEATHERED_COPPER_GRATE, noDrop());
		this.add(Blocks.OXIDIZED_COPPER_GRATE, noDrop());
		this.add(Blocks.WAXED_COPPER_GRATE, noDrop());
		this.add(Blocks.WAXED_EXPOSED_COPPER_GRATE, noDrop());
		this.add(Blocks.WAXED_WEATHERED_COPPER_GRATE, noDrop());
		this.add(Blocks.WAXED_OXIDIZED_COPPER_GRATE, noDrop());
		this.add(Blocks.COPPER_BULB, noDrop());
		this.add(Blocks.EXPOSED_COPPER_BULB, noDrop());
		this.add(Blocks.WEATHERED_COPPER_BULB, noDrop());
		this.add(Blocks.OXIDIZED_COPPER_BULB, noDrop());
		this.add(Blocks.WAXED_COPPER_BULB, noDrop());
		this.add(Blocks.WAXED_EXPOSED_COPPER_BULB, noDrop());
		this.add(Blocks.WAXED_WEATHERED_COPPER_BULB, noDrop());
		this.add(Blocks.WAXED_OXIDIZED_COPPER_BULB, noDrop());
		this.dropOther(Blocks.FARMLAND, Blocks.DIRT);
		this.dropOther(Blocks.TRIPWIRE, Items.STRING);
		this.dropOther(Blocks.DIRT_PATH, Blocks.DIRT);
		this.dropOther(Blocks.KELP_PLANT, Blocks.KELP);
		this.dropOther(Blocks.BAMBOO_SAPLING, Blocks.BAMBOO);
		this.dropOther(Blocks.WATER_CAULDRON, Blocks.CAULDRON);
		this.dropOther(Blocks.LAVA_CAULDRON, Blocks.CAULDRON);
		this.dropOther(Blocks.POWDER_SNOW_CAULDRON, Blocks.CAULDRON);
		this.dropOther(Blocks.BIG_DRIPLEAF_STEM, Blocks.BIG_DRIPLEAF);
		this.add(Blocks.STONE, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.COBBLESTONE));
		this.add(Blocks.DEEPSLATE, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.COBBLED_DEEPSLATE));
		this.add(Blocks.GRASS_BLOCK, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
		this.add(Blocks.PODZOL, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
		this.add(Blocks.MYCELIUM, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
		this.add(Blocks.TUBE_CORAL_BLOCK, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.DEAD_TUBE_CORAL_BLOCK));
		this.add(Blocks.BRAIN_CORAL_BLOCK, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.DEAD_BRAIN_CORAL_BLOCK));
		this.add(Blocks.BUBBLE_CORAL_BLOCK, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.DEAD_BUBBLE_CORAL_BLOCK));
		this.add(Blocks.FIRE_CORAL_BLOCK, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.DEAD_FIRE_CORAL_BLOCK));
		this.add(Blocks.HORN_CORAL_BLOCK, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.DEAD_HORN_CORAL_BLOCK));
		this.add(Blocks.CRIMSON_NYLIUM, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.NETHERRACK));
		this.add(Blocks.WARPED_NYLIUM, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.NETHERRACK));
		this.add(Blocks.BOOKSHELF, block -> this.createSingleItemTableWithSilkTouch(block, Items.BOOK, ConstantValue.exactly(3.0F)));
		this.add(Blocks.CLAY, block -> this.createSingleItemTableWithSilkTouch(block, Items.CLAY_BALL, ConstantValue.exactly(4.0F)));
		this.add(Blocks.ENDER_CHEST, block -> this.createSingleItemTableWithSilkTouch(block, Blocks.OBSIDIAN, ConstantValue.exactly(8.0F)));
		this.add(Blocks.SNOW_BLOCK, block -> this.createSingleItemTableWithSilkTouch(block, Items.SNOWBALL, ConstantValue.exactly(4.0F)));
		this.add(Blocks.CHORUS_PLANT, this.createSingleItemTable(Items.CHORUS_FRUIT, UniformGenerator.between(0.0F, 1.0F)));
		this.dropPottedContents(Blocks.POTTED_OAK_SAPLING);
		this.dropPottedContents(Blocks.POTTED_SPRUCE_SAPLING);
		this.dropPottedContents(Blocks.POTTED_BIRCH_SAPLING);
		this.dropPottedContents(Blocks.POTTED_JUNGLE_SAPLING);
		this.dropPottedContents(Blocks.POTTED_ACACIA_SAPLING);
		this.dropPottedContents(Blocks.POTTED_DARK_OAK_SAPLING);
		this.dropPottedContents(Blocks.POTTED_MANGROVE_PROPAGULE);
		this.dropPottedContents(Blocks.POTTED_CHERRY_SAPLING);
		this.dropPottedContents(Blocks.POTTED_FERN);
		this.dropPottedContents(Blocks.POTTED_DANDELION);
		this.dropPottedContents(Blocks.POTTED_POPPY);
		this.dropPottedContents(Blocks.POTTED_BLUE_ORCHID);
		this.dropPottedContents(Blocks.POTTED_ALLIUM);
		this.dropPottedContents(Blocks.POTTED_AZURE_BLUET);
		this.dropPottedContents(Blocks.POTTED_RED_TULIP);
		this.dropPottedContents(Blocks.POTTED_ORANGE_TULIP);
		this.dropPottedContents(Blocks.POTTED_WHITE_TULIP);
		this.dropPottedContents(Blocks.POTTED_PINK_TULIP);
		this.dropPottedContents(Blocks.POTTED_OXEYE_DAISY);
		this.dropPottedContents(Blocks.POTTED_CORNFLOWER);
		this.dropPottedContents(Blocks.POTTED_LILY_OF_THE_VALLEY);
		this.dropPottedContents(Blocks.POTTED_WITHER_ROSE);
		this.dropPottedContents(Blocks.POTTED_RED_MUSHROOM);
		this.dropPottedContents(Blocks.POTTED_BROWN_MUSHROOM);
		this.dropPottedContents(Blocks.POTTED_DEAD_BUSH);
		this.dropPottedContents(Blocks.POTTED_CACTUS);
		this.dropPottedContents(Blocks.POTTED_BAMBOO);
		this.dropPottedContents(Blocks.POTTED_CRIMSON_FUNGUS);
		this.dropPottedContents(Blocks.POTTED_WARPED_FUNGUS);
		this.dropPottedContents(Blocks.POTTED_CRIMSON_ROOTS);
		this.dropPottedContents(Blocks.POTTED_WARPED_ROOTS);
		this.dropPottedContents(Blocks.POTTED_AZALEA);
		this.dropPottedContents(Blocks.POTTED_FLOWERING_AZALEA);
		this.dropPottedContents(Blocks.POTTED_TORCHFLOWER);
		this.add(Blocks.OAK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.PETRIFIED_OAK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.SPRUCE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.BIRCH_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.JUNGLE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.ACACIA_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.DARK_OAK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.MANGROVE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.CHERRY_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.BAMBOO_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.BAMBOO_MOSAIC_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.BRICK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.COBBLESTONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.DARK_PRISMARINE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.NETHER_BRICK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.PRISMARINE_BRICK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.PRISMARINE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.PURPUR_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.QUARTZ_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.RED_SANDSTONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.SANDSTONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.CUT_RED_SANDSTONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.CUT_SANDSTONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.STONE_BRICK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.STONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.SMOOTH_STONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.POLISHED_GRANITE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.SMOOTH_RED_SANDSTONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.MOSSY_STONE_BRICK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.POLISHED_DIORITE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.MOSSY_COBBLESTONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.END_STONE_BRICK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.SMOOTH_SANDSTONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.SMOOTH_QUARTZ_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.GRANITE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.ANDESITE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.RED_NETHER_BRICK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.POLISHED_ANDESITE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.DIORITE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.CRIMSON_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.WARPED_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.BLACKSTONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.POLISHED_BLACKSTONE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.OXIDIZED_CUT_COPPER_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.WEATHERED_CUT_COPPER_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.EXPOSED_CUT_COPPER_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.CUT_COPPER_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.WAXED_CUT_COPPER_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.COBBLED_DEEPSLATE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.POLISHED_DEEPSLATE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.DEEPSLATE_TILE_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.DEEPSLATE_BRICK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.MUD_BRICK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.OAK_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.SPRUCE_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.BIRCH_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.JUNGLE_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.ACACIA_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.DARK_OAK_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.MANGROVE_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.CHERRY_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.BAMBOO_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.WARPED_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.CRIMSON_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.IRON_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.COPPER_DOOR, noDrop());
		this.add(Blocks.EXPOSED_COPPER_DOOR, noDrop());
		this.add(Blocks.WEATHERED_COPPER_DOOR, noDrop());
		this.add(Blocks.OXIDIZED_COPPER_DOOR, noDrop());
		this.add(Blocks.WAXED_COPPER_DOOR, noDrop());
		this.add(Blocks.WAXED_EXPOSED_COPPER_DOOR, noDrop());
		this.add(Blocks.WAXED_WEATHERED_COPPER_DOOR, noDrop());
		this.add(Blocks.WAXED_OXIDIZED_COPPER_DOOR, noDrop());
		this.add(Blocks.BLACK_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.BLUE_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.BROWN_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.CYAN_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.GRAY_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.GREEN_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.LIGHT_BLUE_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.LIGHT_GRAY_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.LIME_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.MAGENTA_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.PURPLE_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.ORANGE_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.PINK_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.RED_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.WHITE_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.YELLOW_BED, block -> this.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.LILAC, block -> this.createSinglePropConditionTable(block, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		this.add(Blocks.SUNFLOWER, block -> this.createSinglePropConditionTable(block, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		this.add(Blocks.PEONY, block -> this.createSinglePropConditionTable(block, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		this.add(Blocks.ROSE_BUSH, block -> this.createSinglePropConditionTable(block, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		this.add(
			Blocks.TNT,
			LootTable.lootTable()
				.withPool(
					this.applyExplosionCondition(
						Blocks.TNT,
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(
								LootItem.lootTableItem(Blocks.TNT)
									.when(
										LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.TNT)
											.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TntBlock.UNSTABLE, false))
									)
							)
					)
				)
		);
		this.add(
			Blocks.COCOA,
			block -> LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(
								(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
									block,
									LootItem.lootTableItem(Items.COCOA_BEANS)
										.apply(
											SetItemCountFunction.setCount(ConstantValue.exactly(3.0F))
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CocoaBlock.AGE, 2))
												)
										)
								)
							)
					)
		);
		this.add(
			Blocks.SEA_PICKLE,
			block -> LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(
								(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
									Blocks.SEA_PICKLE,
									LootItem.lootTableItem(block)
										.apply(
											List.of(2, 3, 4),
											integer -> SetItemCountFunction.setCount(ConstantValue.exactly((float)integer.intValue()))
													.when(
														LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
															.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, integer))
													)
										)
								)
							)
					)
		);
		this.add(
			Blocks.COMPOSTER,
			block -> LootTable.lootTable()
					.withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(block, LootItem.lootTableItem(Items.COMPOSTER))))
					.withPool(
						LootPool.lootPool()
							.add(LootItem.lootTableItem(Items.BONE_MEAL))
							.when(
								LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
									.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ComposterBlock.LEVEL, 8))
							)
					)
		);
		this.add(Blocks.CAVE_VINES, block -> BlockLootSubProvider.createCaveVinesDrop(block));
		this.add(Blocks.CAVE_VINES_PLANT, block -> BlockLootSubProvider.createCaveVinesDrop(block));
		this.add(Blocks.CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.WHITE_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.ORANGE_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.MAGENTA_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.LIGHT_BLUE_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.YELLOW_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.LIME_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.PINK_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.GRAY_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.LIGHT_GRAY_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.CYAN_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.PURPLE_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.BLUE_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.BROWN_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.GREEN_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.RED_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.BLACK_CANDLE, block -> this.createCandleDrops(block));
		this.add(Blocks.BEACON, block -> this.createNameableBlockEntityTable(block));
		this.add(Blocks.BREWING_STAND, block -> this.createNameableBlockEntityTable(block));
		this.add(Blocks.CHEST, block -> this.createNameableBlockEntityTable(block));
		this.add(Blocks.DISPENSER, block -> this.createNameableBlockEntityTable(block));
		this.add(Blocks.DROPPER, block -> this.createNameableBlockEntityTable(block));
		this.add(Blocks.ENCHANTING_TABLE, block -> this.createNameableBlockEntityTable(block));
		this.add(Blocks.FURNACE, block -> this.createNameableBlockEntityTable(block));
		this.add(Blocks.HOPPER, block -> this.createNameableBlockEntityTable(block));
		this.add(Blocks.TRAPPED_CHEST, block -> this.createNameableBlockEntityTable(block));
		this.add(Blocks.SMOKER, block -> this.createNameableBlockEntityTable(block));
		this.add(Blocks.BLAST_FURNACE, block -> this.createNameableBlockEntityTable(block));
		this.add(Blocks.BARREL, block -> this.createNameableBlockEntityTable(block));
		this.dropSelf(Blocks.CARTOGRAPHY_TABLE);
		this.dropSelf(Blocks.FLETCHING_TABLE);
		this.dropSelf(Blocks.GRINDSTONE);
		this.dropSelf(Blocks.LECTERN);
		this.dropSelf(Blocks.SMITHING_TABLE);
		this.dropSelf(Blocks.STONECUTTER);
		this.add(Blocks.BELL, this::createSingleItemTable);
		this.add(Blocks.LANTERN, this::createSingleItemTable);
		this.add(Blocks.SOUL_LANTERN, this::createSingleItemTable);
		this.add(Blocks.SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.BLACK_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.BLUE_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.BROWN_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.CYAN_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.GRAY_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.GREEN_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.LIGHT_BLUE_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.LIGHT_GRAY_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.LIME_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.MAGENTA_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.ORANGE_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.PINK_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.PURPLE_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.RED_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.WHITE_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.YELLOW_SHULKER_BOX, block -> this.createShulkerBoxDrop(block));
		this.add(Blocks.BLACK_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.BLUE_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.BROWN_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.CYAN_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.GRAY_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.GREEN_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.LIGHT_BLUE_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.LIGHT_GRAY_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.LIME_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.MAGENTA_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.ORANGE_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.PINK_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.PURPLE_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.RED_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.WHITE_BANNER, block -> this.createBannerDrop(block));
		this.add(Blocks.YELLOW_BANNER, block -> this.createBannerDrop(block));
		this.add(
			Blocks.PLAYER_HEAD,
			block -> LootTable.lootTable()
					.withPool(
						this.applyExplosionCondition(
							block,
							LootPool.lootPool()
								.setRolls(ConstantValue.exactly(1.0F))
								.add(
									LootItem.lootTableItem(block)
										.apply(
											CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
												.copy("SkullOwner", "SkullOwner")
												.copy("note_block_sound", String.format(Locale.ROOT, "%s.%s", "BlockEntityTag", "note_block_sound"))
										)
								)
						)
					)
		);
		this.add(Blocks.BEE_NEST, block -> createBeeNestDrop(block));
		this.add(Blocks.BEEHIVE, block -> createBeeHiveDrop(block));
		this.add(Blocks.OAK_LEAVES, block -> this.createOakLeavesDrops(block, Blocks.OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.SPRUCE_LEAVES, block -> this.createLeavesDrops(block, Blocks.SPRUCE_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.BIRCH_LEAVES, block -> this.createLeavesDrops(block, Blocks.BIRCH_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.JUNGLE_LEAVES, block -> this.createLeavesDrops(block, Blocks.JUNGLE_SAPLING, JUNGLE_LEAVES_SAPLING_CHANGES));
		this.add(Blocks.ACACIA_LEAVES, block -> this.createLeavesDrops(block, Blocks.ACACIA_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.DARK_OAK_LEAVES, block -> this.createOakLeavesDrops(block, Blocks.DARK_OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.CHERRY_LEAVES, block -> this.createLeavesDrops(block, Blocks.CHERRY_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.AZALEA_LEAVES, block -> this.createLeavesDrops(block, Blocks.AZALEA, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.FLOWERING_AZALEA_LEAVES, block -> this.createLeavesDrops(block, Blocks.FLOWERING_AZALEA, NORMAL_LEAVES_SAPLING_CHANCES));
		LootItemCondition.Builder builder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.BEETROOTS)
			.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BeetrootBlock.AGE, 3));
		this.add(Blocks.BEETROOTS, this.createCropDrops(Blocks.BEETROOTS, Items.BEETROOT, Items.BEETROOT_SEEDS, builder));
		LootItemCondition.Builder builder2 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.WHEAT)
			.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7));
		this.add(Blocks.WHEAT, this.createCropDrops(Blocks.WHEAT, Items.WHEAT, Items.WHEAT_SEEDS, builder2));
		LootItemCondition.Builder builder3 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.CARROTS)
			.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CarrotBlock.AGE, 7));
		LootItemCondition.Builder builder4 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.MANGROVE_PROPAGULE)
			.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MangrovePropaguleBlock.AGE, 4));
		this.add(
			Blocks.MANGROVE_PROPAGULE,
			this.applyExplosionDecay(
				Blocks.MANGROVE_PROPAGULE, LootTable.lootTable().withPool(LootPool.lootPool().when(builder4).add(LootItem.lootTableItem(Items.MANGROVE_PROPAGULE)))
			)
		);
		this.add(
			Blocks.TORCHFLOWER_CROP,
			this.applyExplosionDecay(Blocks.TORCHFLOWER_CROP, LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.TORCHFLOWER_SEEDS))))
		);
		this.dropSelf(Blocks.SNIFFER_EGG);
		this.add(Blocks.PITCHER_CROP, block -> this.createPitcherCropLoot());
		this.dropSelf(Blocks.PITCHER_PLANT);
		this.add(
			Blocks.PITCHER_PLANT,
			this.applyExplosionDecay(
				Blocks.PITCHER_PLANT,
				LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.add(
								LootItem.lootTableItem(Items.PITCHER_PLANT)
									.when(
										LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.PITCHER_PLANT)
											.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))
									)
							)
					)
			)
		);
		this.add(
			Blocks.CARROTS,
			this.applyExplosionDecay(
				Blocks.CARROTS,
				LootTable.lootTable()
					.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.CARROT)))
					.withPool(
						LootPool.lootPool()
							.when(builder3)
							.add(LootItem.lootTableItem(Items.CARROT).apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3)))
					)
			)
		);
		LootItemCondition.Builder builder5 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.POTATOES)
			.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PotatoBlock.AGE, 7));
		this.add(
			Blocks.POTATOES,
			this.applyExplosionDecay(
				Blocks.POTATOES,
				LootTable.lootTable()
					.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.POTATO)))
					.withPool(
						LootPool.lootPool()
							.when(builder5)
							.add(LootItem.lootTableItem(Items.POTATO).apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3)))
					)
					.withPool(LootPool.lootPool().when(builder5).add(LootItem.lootTableItem(Items.POISONOUS_POTATO).when(LootItemRandomChanceCondition.randomChance(0.02F))))
			)
		);
		this.add(
			Blocks.SWEET_BERRY_BUSH,
			block -> this.applyExplosionDecay(
					block,
					LootTable.lootTable()
						.withPool(
							LootPool.lootPool()
								.when(
									LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SWEET_BERRY_BUSH)
										.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 3))
								)
								.add(LootItem.lootTableItem(Items.SWEET_BERRIES))
								.apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F)))
								.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
						)
						.withPool(
							LootPool.lootPool()
								.when(
									LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SWEET_BERRY_BUSH)
										.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 2))
								)
								.add(LootItem.lootTableItem(Items.SWEET_BERRIES))
								.apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
								.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
						)
				)
		);
		this.add(Blocks.BROWN_MUSHROOM_BLOCK, block -> this.createMushroomBlockDrop(block, Blocks.BROWN_MUSHROOM));
		this.add(Blocks.RED_MUSHROOM_BLOCK, block -> this.createMushroomBlockDrop(block, Blocks.RED_MUSHROOM));
		this.add(Blocks.COAL_ORE, block -> this.createOreDrop(block, Items.COAL));
		this.add(Blocks.DEEPSLATE_COAL_ORE, block -> this.createOreDrop(block, Items.COAL));
		this.add(Blocks.EMERALD_ORE, block -> this.createOreDrop(block, Items.EMERALD));
		this.add(Blocks.DEEPSLATE_EMERALD_ORE, block -> this.createOreDrop(block, Items.EMERALD));
		this.add(Blocks.NETHER_QUARTZ_ORE, block -> this.createOreDrop(block, Items.QUARTZ));
		this.add(Blocks.DIAMOND_ORE, block -> this.createOreDrop(block, Items.DIAMOND));
		this.add(Blocks.DEEPSLATE_DIAMOND_ORE, block -> this.createOreDrop(block, Items.DIAMOND));
		this.add(Blocks.COPPER_ORE, block -> this.createCopperOreDrops(block));
		this.add(Blocks.DEEPSLATE_COPPER_ORE, block -> this.createCopperOreDrops(block));
		this.add(Blocks.IRON_ORE, block -> this.createOreDrop(block, Items.RAW_IRON));
		this.add(Blocks.DEEPSLATE_IRON_ORE, block -> this.createOreDrop(block, Items.RAW_IRON));
		this.add(Blocks.GOLD_ORE, block -> this.createOreDrop(block, Items.RAW_GOLD));
		this.add(Blocks.DEEPSLATE_GOLD_ORE, block -> this.createOreDrop(block, Items.RAW_GOLD));
		this.add(
			Blocks.NETHER_GOLD_ORE,
			block -> createSilkTouchDispatchTable(
					block,
					(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
						block,
						LootItem.lootTableItem(Items.GOLD_NUGGET)
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 6.0F)))
							.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
					)
				)
		);
		this.add(Blocks.LAPIS_ORE, block -> this.createLapisOreDrops(block));
		this.add(Blocks.DEEPSLATE_LAPIS_ORE, block -> this.createLapisOreDrops(block));
		this.add(
			Blocks.COBWEB,
			block -> createSilkTouchOrShearsDispatchTable(
					block, (LootPoolEntryContainer.Builder<?>)this.applyExplosionCondition(block, LootItem.lootTableItem(Items.STRING))
				)
		);
		this.add(
			Blocks.DEAD_BUSH,
			block -> createShearsDispatchTable(
					block,
					(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
						block, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
					)
				)
		);
		this.add(Blocks.NETHER_SPROUTS, itemLike -> BlockLootSubProvider.createShearsOnlyDrop(itemLike));
		this.add(Blocks.SEAGRASS, itemLike -> BlockLootSubProvider.createShearsOnlyDrop(itemLike));
		this.add(Blocks.VINE, itemLike -> BlockLootSubProvider.createShearsOnlyDrop(itemLike));
		this.add(Blocks.GLOW_LICHEN, block -> this.createMultifaceBlockDrops(block, HAS_SHEARS));
		this.add(Blocks.HANGING_ROOTS, itemLike -> BlockLootSubProvider.createShearsOnlyDrop(itemLike));
		this.add(Blocks.SMALL_DRIPLEAF, itemLike -> BlockLootSubProvider.createShearsOnlyDrop(itemLike));
		this.add(Blocks.MANGROVE_LEAVES, block -> this.createMangroveLeavesDrops(block));
		this.add(Blocks.TALL_SEAGRASS, createDoublePlantShearsDrop(Blocks.SEAGRASS));
		this.add(Blocks.LARGE_FERN, block -> this.createDoublePlantWithSeedDrops(block, Blocks.FERN));
		this.add(Blocks.TALL_GRASS, block -> this.createDoublePlantWithSeedDrops(block, Blocks.GRASS));
		this.add(Blocks.MELON_STEM, block -> this.createStemDrops(block, Items.MELON_SEEDS));
		this.add(Blocks.ATTACHED_MELON_STEM, block -> this.createAttachedStemDrops(block, Items.MELON_SEEDS));
		this.add(Blocks.PUMPKIN_STEM, block -> this.createStemDrops(block, Items.PUMPKIN_SEEDS));
		this.add(Blocks.ATTACHED_PUMPKIN_STEM, block -> this.createAttachedStemDrops(block, Items.PUMPKIN_SEEDS));
		this.add(
			Blocks.CHORUS_FLOWER,
			block -> LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(
								((LootPoolSingletonContainer.Builder)this.applyExplosionCondition(block, LootItem.lootTableItem(block)))
									.when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS))
							)
					)
		);
		this.add(Blocks.FERN, block -> this.createGrassDrops(block));
		this.add(Blocks.GRASS, block -> this.createGrassDrops(block));
		this.add(
			Blocks.GLOWSTONE,
			block -> createSilkTouchDispatchTable(
					block,
					(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
						block,
						LootItem.lootTableItem(Items.GLOWSTONE_DUST)
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
							.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
							.apply(LimitCount.limitCount(IntRange.range(1, 4)))
					)
				)
		);
		this.add(
			Blocks.MELON,
			block -> createSilkTouchDispatchTable(
					block,
					(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
						block,
						LootItem.lootTableItem(Items.MELON_SLICE)
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 7.0F)))
							.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
							.apply(LimitCount.limitCount(IntRange.upperBound(9)))
					)
				)
		);
		this.add(Blocks.REDSTONE_ORE, block -> this.createRedstoneOreDrops(block));
		this.add(Blocks.DEEPSLATE_REDSTONE_ORE, block -> this.createRedstoneOreDrops(block));
		this.add(
			Blocks.SEA_LANTERN,
			block -> createSilkTouchDispatchTable(
					block,
					(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
						block,
						LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS)
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F)))
							.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
							.apply(LimitCount.limitCount(IntRange.range(1, 5)))
					)
				)
		);
		this.add(
			Blocks.NETHER_WART,
			block -> LootTable.lootTable()
					.withPool(
						this.applyExplosionDecay(
							block,
							LootPool.lootPool()
								.setRolls(ConstantValue.exactly(1.0F))
								.add(
									LootItem.lootTableItem(Items.NETHER_WART)
										.apply(
											SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(NetherWartBlock.AGE, 3))
												)
										)
										.apply(
											ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(NetherWartBlock.AGE, 3))
												)
										)
								)
						)
					)
		);
		this.add(
			Blocks.SNOW,
			block -> LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS))
							.add(
								AlternativesEntry.alternatives(
									AlternativesEntry.alternatives(
											SnowLayerBlock.LAYERS.getPossibleValues(),
											integer -> LootItem.lootTableItem(Items.SNOWBALL)
													.when(
														LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
															.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, integer))
													)
													.apply(SetItemCountFunction.setCount(ConstantValue.exactly((float)integer.intValue())))
										)
										.when(HAS_NO_SILK_TOUCH),
									AlternativesEntry.alternatives(
										SnowLayerBlock.LAYERS.getPossibleValues(),
										integer -> integer == 8
												? LootItem.lootTableItem(Blocks.SNOW_BLOCK)
												: LootItem.lootTableItem(Blocks.SNOW)
													.apply(SetItemCountFunction.setCount(ConstantValue.exactly((float)integer.intValue())))
													.when(
														LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
															.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, integer))
													)
									)
								)
							)
					)
		);
		this.add(
			Blocks.GRAVEL,
			block -> createSilkTouchDispatchTable(
					block,
					this.applyExplosionCondition(
						block,
						LootItem.lootTableItem(Items.FLINT)
							.when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.1F, 0.14285715F, 0.25F, 1.0F))
							.otherwise(LootItem.lootTableItem(block))
					)
				)
		);
		this.add(
			Blocks.CAMPFIRE,
			block -> createSilkTouchDispatchTable(
					block,
					(LootPoolEntryContainer.Builder<?>)this.applyExplosionCondition(
						block, LootItem.lootTableItem(Items.CHARCOAL).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))
					)
				)
		);
		this.add(
			Blocks.GILDED_BLACKSTONE,
			block -> createSilkTouchDispatchTable(
					block,
					this.applyExplosionCondition(
						block,
						LootItem.lootTableItem(Items.GOLD_NUGGET)
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
							.when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.1F, 0.14285715F, 0.25F, 1.0F))
							.otherwise(LootItem.lootTableItem(block))
					)
				)
		);
		this.add(
			Blocks.SOUL_CAMPFIRE,
			block -> createSilkTouchDispatchTable(
					block,
					(LootPoolEntryContainer.Builder<?>)this.applyExplosionCondition(
						block, LootItem.lootTableItem(Items.SOUL_SOIL).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))
					)
				)
		);
		this.add(
			Blocks.AMETHYST_CLUSTER,
			block -> createSilkTouchDispatchTable(
					block,
					LootItem.lootTableItem(Items.AMETHYST_SHARD)
						.apply(SetItemCountFunction.setCount(ConstantValue.exactly(4.0F)))
						.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
						.when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)))
						.otherwise(
							(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
								block, LootItem.lootTableItem(Items.AMETHYST_SHARD).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))
							)
						)
				)
		);
		this.dropWhenSilkTouch(Blocks.SMALL_AMETHYST_BUD);
		this.dropWhenSilkTouch(Blocks.MEDIUM_AMETHYST_BUD);
		this.dropWhenSilkTouch(Blocks.LARGE_AMETHYST_BUD);
		this.dropWhenSilkTouch(Blocks.GLASS);
		this.dropWhenSilkTouch(Blocks.WHITE_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.ORANGE_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.MAGENTA_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.LIGHT_BLUE_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.YELLOW_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.LIME_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.PINK_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.GRAY_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.LIGHT_GRAY_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.CYAN_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.PURPLE_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.BLUE_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.BROWN_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.GREEN_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.RED_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.BLACK_STAINED_GLASS);
		this.dropWhenSilkTouch(Blocks.GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.WHITE_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.ORANGE_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.MAGENTA_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.YELLOW_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.LIME_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.PINK_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.GRAY_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.CYAN_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.PURPLE_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.BLUE_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.BROWN_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.GREEN_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.RED_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.BLACK_STAINED_GLASS_PANE);
		this.dropWhenSilkTouch(Blocks.ICE);
		this.dropWhenSilkTouch(Blocks.PACKED_ICE);
		this.dropWhenSilkTouch(Blocks.BLUE_ICE);
		this.dropWhenSilkTouch(Blocks.TURTLE_EGG);
		this.dropWhenSilkTouch(Blocks.MUSHROOM_STEM);
		this.dropWhenSilkTouch(Blocks.DEAD_TUBE_CORAL);
		this.dropWhenSilkTouch(Blocks.DEAD_BRAIN_CORAL);
		this.dropWhenSilkTouch(Blocks.DEAD_BUBBLE_CORAL);
		this.dropWhenSilkTouch(Blocks.DEAD_FIRE_CORAL);
		this.dropWhenSilkTouch(Blocks.DEAD_HORN_CORAL);
		this.dropWhenSilkTouch(Blocks.TUBE_CORAL);
		this.dropWhenSilkTouch(Blocks.BRAIN_CORAL);
		this.dropWhenSilkTouch(Blocks.BUBBLE_CORAL);
		this.dropWhenSilkTouch(Blocks.FIRE_CORAL);
		this.dropWhenSilkTouch(Blocks.HORN_CORAL);
		this.dropWhenSilkTouch(Blocks.DEAD_TUBE_CORAL_FAN);
		this.dropWhenSilkTouch(Blocks.DEAD_BRAIN_CORAL_FAN);
		this.dropWhenSilkTouch(Blocks.DEAD_BUBBLE_CORAL_FAN);
		this.dropWhenSilkTouch(Blocks.DEAD_FIRE_CORAL_FAN);
		this.dropWhenSilkTouch(Blocks.DEAD_HORN_CORAL_FAN);
		this.dropWhenSilkTouch(Blocks.TUBE_CORAL_FAN);
		this.dropWhenSilkTouch(Blocks.BRAIN_CORAL_FAN);
		this.dropWhenSilkTouch(Blocks.BUBBLE_CORAL_FAN);
		this.dropWhenSilkTouch(Blocks.FIRE_CORAL_FAN);
		this.dropWhenSilkTouch(Blocks.HORN_CORAL_FAN);
		this.otherWhenSilkTouch(Blocks.INFESTED_STONE, Blocks.STONE);
		this.otherWhenSilkTouch(Blocks.INFESTED_COBBLESTONE, Blocks.COBBLESTONE);
		this.otherWhenSilkTouch(Blocks.INFESTED_STONE_BRICKS, Blocks.STONE_BRICKS);
		this.otherWhenSilkTouch(Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS);
		this.otherWhenSilkTouch(Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS);
		this.otherWhenSilkTouch(Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS);
		this.otherWhenSilkTouch(Blocks.INFESTED_DEEPSLATE, Blocks.DEEPSLATE);
		this.addNetherVinesDropTable(Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT);
		this.addNetherVinesDropTable(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT);
		this.add(Blocks.CAKE, noDrop());
		this.add(Blocks.CANDLE_CAKE, createCandleCakeDrops(Blocks.CANDLE));
		this.add(Blocks.WHITE_CANDLE_CAKE, createCandleCakeDrops(Blocks.WHITE_CANDLE));
		this.add(Blocks.ORANGE_CANDLE_CAKE, createCandleCakeDrops(Blocks.ORANGE_CANDLE));
		this.add(Blocks.MAGENTA_CANDLE_CAKE, createCandleCakeDrops(Blocks.MAGENTA_CANDLE));
		this.add(Blocks.LIGHT_BLUE_CANDLE_CAKE, createCandleCakeDrops(Blocks.LIGHT_BLUE_CANDLE));
		this.add(Blocks.YELLOW_CANDLE_CAKE, createCandleCakeDrops(Blocks.YELLOW_CANDLE));
		this.add(Blocks.LIME_CANDLE_CAKE, createCandleCakeDrops(Blocks.LIME_CANDLE));
		this.add(Blocks.PINK_CANDLE_CAKE, createCandleCakeDrops(Blocks.PINK_CANDLE));
		this.add(Blocks.GRAY_CANDLE_CAKE, createCandleCakeDrops(Blocks.GRAY_CANDLE));
		this.add(Blocks.LIGHT_GRAY_CANDLE_CAKE, createCandleCakeDrops(Blocks.LIGHT_GRAY_CANDLE));
		this.add(Blocks.CYAN_CANDLE_CAKE, createCandleCakeDrops(Blocks.CYAN_CANDLE));
		this.add(Blocks.PURPLE_CANDLE_CAKE, createCandleCakeDrops(Blocks.PURPLE_CANDLE));
		this.add(Blocks.BLUE_CANDLE_CAKE, createCandleCakeDrops(Blocks.BLUE_CANDLE));
		this.add(Blocks.BROWN_CANDLE_CAKE, createCandleCakeDrops(Blocks.BROWN_CANDLE));
		this.add(Blocks.GREEN_CANDLE_CAKE, createCandleCakeDrops(Blocks.GREEN_CANDLE));
		this.add(Blocks.RED_CANDLE_CAKE, createCandleCakeDrops(Blocks.RED_CANDLE));
		this.add(Blocks.BLACK_CANDLE_CAKE, createCandleCakeDrops(Blocks.BLACK_CANDLE));
		this.add(Blocks.FROSTED_ICE, noDrop());
		this.add(Blocks.SPAWNER, noDrop());
		this.add(Blocks.TRIAL_SPAWNER, noDrop());
		this.add(Blocks.FIRE, noDrop());
		this.add(Blocks.SOUL_FIRE, noDrop());
		this.add(Blocks.NETHER_PORTAL, noDrop());
		this.add(Blocks.BUDDING_AMETHYST, noDrop());
		this.add(Blocks.POWDER_SNOW, noDrop());
		this.add(Blocks.FROGSPAWN, noDrop());
		this.add(Blocks.REINFORCED_DEEPSLATE, noDrop());
		this.add(Blocks.SUSPICIOUS_SAND, noDrop());
		this.add(Blocks.SUSPICIOUS_GRAVEL, noDrop());
	}

	private LootTable.Builder createDecoratedPotTable(Block block) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(
						DynamicLoot.dynamicEntry(DecoratedPotBlock.SHERDS_DYNAMIC_DROP_ID)
							.when(
								LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
									.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DecoratedPotBlock.CRACKED, true))
							)
							.otherwise(LootItem.lootTableItem(block).apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("sherds", "BlockEntityTag.sherds")))
					)
			);
	}

	private LootTable.Builder createPitcherCropLoot() {
		return this.applyExplosionDecay(
			Blocks.PITCHER_CROP,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.add(
							AlternativesEntry.alternatives(
								PitcherCropBlock.AGE.getPossibleValues(),
								integer -> {
									LootItemBlockStatePropertyCondition.Builder builder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.PITCHER_CROP)
										.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
									LootItemBlockStatePropertyCondition.Builder builder2 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.PITCHER_CROP)
										.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PitcherCropBlock.AGE, integer));
									return integer == 4
										? LootItem.lootTableItem(Items.PITCHER_PLANT).when(builder2).when(builder).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))
										: LootItem.lootTableItem(Items.PITCHER_POD).when(builder2).when(builder).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)));
								}
							)
						)
				)
		);
	}
}
