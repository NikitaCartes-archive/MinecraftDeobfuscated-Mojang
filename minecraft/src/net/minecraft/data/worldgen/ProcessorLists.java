package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.AxisAlignedLinearPosTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.CappedProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.AppendLoot;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class ProcessorLists {
	private static final ResourceKey<StructureProcessorList> EMPTY = createKey("empty");
	public static final ResourceKey<StructureProcessorList> ZOMBIE_PLAINS = createKey("zombie_plains");
	public static final ResourceKey<StructureProcessorList> ZOMBIE_POTATO = createKey("zombie_potato");
	public static final ResourceKey<StructureProcessorList> ZOMBIE_SAVANNA = createKey("zombie_savanna");
	public static final ResourceKey<StructureProcessorList> ZOMBIE_SNOWY = createKey("zombie_snowy");
	public static final ResourceKey<StructureProcessorList> ZOMBIE_TAIGA = createKey("zombie_taiga");
	public static final ResourceKey<StructureProcessorList> ZOMBIE_DESERT = createKey("zombie_desert");
	public static final ResourceKey<StructureProcessorList> MOSSIFY_10_PERCENT = createKey("mossify_10_percent");
	public static final ResourceKey<StructureProcessorList> MOSSIFY_20_PERCENT = createKey("mossify_20_percent");
	public static final ResourceKey<StructureProcessorList> MOSSIFY_70_PERCENT = createKey("mossify_70_percent");
	public static final ResourceKey<StructureProcessorList> STREET_PLAINS = createKey("street_plains");
	public static final ResourceKey<StructureProcessorList> SPOIL_10_PERCENT = createKey("spoil_10_percent");
	public static final ResourceKey<StructureProcessorList> SPOIL_20_PERCENT = createKey("spoil_20_percent");
	public static final ResourceKey<StructureProcessorList> SPOIL_70_PERCENT = createKey("spoil_70_percent");
	public static final ResourceKey<StructureProcessorList> STREET_POTATO = createKey("street_potato");
	public static final ResourceKey<StructureProcessorList> STREET_SAVANNA = createKey("street_savanna");
	public static final ResourceKey<StructureProcessorList> STREET_SNOWY_OR_TAIGA = createKey("street_snowy_or_taiga");
	public static final ResourceKey<StructureProcessorList> FARM_PLAINS = createKey("farm_plains");
	public static final ResourceKey<StructureProcessorList> FARM_POTATO = createKey("farm_potato");
	public static final ResourceKey<StructureProcessorList> FARM_SAVANNA = createKey("farm_savanna");
	public static final ResourceKey<StructureProcessorList> FARM_SNOWY = createKey("farm_snowy");
	public static final ResourceKey<StructureProcessorList> FARM_TAIGA = createKey("farm_taiga");
	public static final ResourceKey<StructureProcessorList> FARM_DESERT = createKey("farm_desert");
	public static final ResourceKey<StructureProcessorList> OUTPOST_ROT = createKey("outpost_rot");
	public static final ResourceKey<StructureProcessorList> BOTTOM_RAMPART = createKey("bottom_rampart");
	public static final ResourceKey<StructureProcessorList> TREASURE_ROOMS = createKey("treasure_rooms");
	public static final ResourceKey<StructureProcessorList> HOUSING = createKey("housing");
	public static final ResourceKey<StructureProcessorList> SIDE_WALL_DEGRADATION = createKey("side_wall_degradation");
	public static final ResourceKey<StructureProcessorList> STABLE_DEGRADATION = createKey("stable_degradation");
	public static final ResourceKey<StructureProcessorList> BASTION_GENERIC_DEGRADATION = createKey("bastion_generic_degradation");
	public static final ResourceKey<StructureProcessorList> RAMPART_DEGRADATION = createKey("rampart_degradation");
	public static final ResourceKey<StructureProcessorList> ENTRANCE_REPLACEMENT = createKey("entrance_replacement");
	public static final ResourceKey<StructureProcessorList> BRIDGE = createKey("bridge");
	public static final ResourceKey<StructureProcessorList> ROOF = createKey("roof");
	public static final ResourceKey<StructureProcessorList> HIGH_WALL = createKey("high_wall");
	public static final ResourceKey<StructureProcessorList> HIGH_RAMPART = createKey("high_rampart");
	public static final ResourceKey<StructureProcessorList> FOSSIL_ROT = createKey("fossil_rot");
	public static final ResourceKey<StructureProcessorList> FOSSIL_COAL = createKey("fossil_coal");
	public static final ResourceKey<StructureProcessorList> COLOSSEUM_VEINS = createKey("colosseum_veins");
	public static final ResourceKey<StructureProcessorList> FOSSIL_DIAMONDS = createKey("fossil_diamonds");
	public static final ResourceKey<StructureProcessorList> ANCIENT_CITY_START_DEGRADATION = createKey("ancient_city_start_degradation");
	public static final ResourceKey<StructureProcessorList> ANCIENT_CITY_GENERIC_DEGRADATION = createKey("ancient_city_generic_degradation");
	public static final ResourceKey<StructureProcessorList> ANCIENT_CITY_WALLS_DEGRADATION = createKey("ancient_city_walls_degradation");
	public static final ResourceKey<StructureProcessorList> TRAIL_RUINS_HOUSES_ARCHAEOLOGY = createKey("trail_ruins_houses_archaeology");
	public static final ResourceKey<StructureProcessorList> TRAIL_RUINS_ROADS_ARCHAEOLOGY = createKey("trail_ruins_roads_archaeology");
	public static final ResourceKey<StructureProcessorList> TRAIL_RUINS_TOWER_TOP_ARCHAEOLOGY = createKey("trail_ruins_tower_top_archaeology");

	private static ResourceKey<StructureProcessorList> createKey(String string) {
		return ResourceKey.create(Registries.PROCESSOR_LIST, new ResourceLocation(string));
	}

	private static void register(
		BootstrapContext<StructureProcessorList> bootstrapContext, ResourceKey<StructureProcessorList> resourceKey, List<StructureProcessor> list
	) {
		bootstrapContext.register(resourceKey, new StructureProcessorList(list));
	}

	public static void bootstrap(BootstrapContext<StructureProcessorList> bootstrapContext) {
		HolderGetter<Block> holderGetter = bootstrapContext.lookup(Registries.BLOCK);
		ProcessorRule processorRule = new ProcessorRule(
			new RandomBlockMatchTest(Blocks.BLACKSTONE, 0.01F), AlwaysTrueTest.INSTANCE, Blocks.GILDED_BLACKSTONE.defaultBlockState()
		);
		ProcessorRule processorRule2 = new ProcessorRule(
			new RandomBlockMatchTest(Blocks.GILDED_BLACKSTONE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.BLACKSTONE.defaultBlockState()
		);
		register(bootstrapContext, EMPTY, ImmutableList.of());
		register(
			bootstrapContext,
			ZOMBIE_PLAINS,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
						new ProcessorRule(new TagMatchTest(BlockTags.DOORS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.WALL_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.07F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.MOSSY_COBBLESTONE, 0.07F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHITE_TERRACOTTA, 0.07F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.OAK_LOG, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.OAK_PLANKS, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.OAK_STAIRS, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.STRIPPED_OAK_LOG, 0.02F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.GLASS_PANE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(
							new BlockStateMatchTest(
								Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
							),
							AlwaysTrueTest.INSTANCE,
							Blocks.BROWN_STAINED_GLASS_PANE
								.defaultBlockState()
								.setValue(IronBarsBlock.NORTH, Boolean.valueOf(true))
								.setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
						),
						new ProcessorRule(
							new BlockStateMatchTest(
								Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)).setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
							),
							AlwaysTrueTest.INSTANCE,
							Blocks.BROWN_STAINED_GLASS_PANE
								.defaultBlockState()
								.setValue(IronBarsBlock.EAST, Boolean.valueOf(true))
								.setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			ZOMBIE_POTATO,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.TATERSTONE, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.EXPIRED_BAKED_POTATO_BRICKS.defaultBlockState()),
						new ProcessorRule(new TagMatchTest(BlockTags.DOORS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.WALL_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.TATERSTONE, 0.07F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.EXPIRED_BAKED_POTATO_BRICKS, 0.07F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHITE_TERRACOTTA, 0.07F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.POTATO_STEM, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.POTATO_PLANKS, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.POTATO_STAIRS, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.STRIPPED_OAK_LOG, 0.02F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.GLASS_PANE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(
							new BlockStateMatchTest(
								Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
							),
							AlwaysTrueTest.INSTANCE,
							Blocks.BROWN_STAINED_GLASS_PANE
								.defaultBlockState()
								.setValue(IronBarsBlock.NORTH, Boolean.valueOf(true))
								.setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
						),
						new ProcessorRule(
							new BlockStateMatchTest(
								Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)).setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
							),
							AlwaysTrueTest.INSTANCE,
							Blocks.BROWN_STAINED_GLASS_PANE
								.defaultBlockState()
								.setValue(IronBarsBlock.EAST, Boolean.valueOf(true))
								.setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			ZOMBIE_SAVANNA,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new TagMatchTest(BlockTags.DOORS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.WALL_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.ACACIA_PLANKS, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.ACACIA_STAIRS, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.ACACIA_LOG, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.ACACIA_WOOD, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.ORANGE_TERRACOTTA, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.YELLOW_TERRACOTTA, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.RED_TERRACOTTA, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.GLASS_PANE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(
							new BlockStateMatchTest(
								Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
							),
							AlwaysTrueTest.INSTANCE,
							Blocks.BROWN_STAINED_GLASS_PANE
								.defaultBlockState()
								.setValue(IronBarsBlock.NORTH, Boolean.valueOf(true))
								.setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
						),
						new ProcessorRule(
							new BlockStateMatchTest(
								Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)).setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
							),
							AlwaysTrueTest.INSTANCE,
							Blocks.BROWN_STAINED_GLASS_PANE
								.defaultBlockState()
								.setValue(IronBarsBlock.EAST, Boolean.valueOf(true))
								.setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			ZOMBIE_SNOWY,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new TagMatchTest(BlockTags.DOORS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.WALL_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.LANTERN), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.SPRUCE_PLANKS, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.SPRUCE_SLAB, 0.4F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.STRIPPED_SPRUCE_LOG, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.STRIPPED_SPRUCE_WOOD, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.GLASS_PANE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(
							new BlockStateMatchTest(
								Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
							),
							AlwaysTrueTest.INSTANCE,
							Blocks.BROWN_STAINED_GLASS_PANE
								.defaultBlockState()
								.setValue(IronBarsBlock.NORTH, Boolean.valueOf(true))
								.setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
						),
						new ProcessorRule(
							new BlockStateMatchTest(
								Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)).setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
							),
							AlwaysTrueTest.INSTANCE,
							Blocks.BROWN_STAINED_GLASS_PANE
								.defaultBlockState()
								.setValue(IronBarsBlock.EAST, Boolean.valueOf(true))
								.setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			ZOMBIE_TAIGA,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
						new ProcessorRule(new TagMatchTest(BlockTags.DOORS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.WALL_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(
							new BlockMatchTest(Blocks.CAMPFIRE), AlwaysTrueTest.INSTANCE, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, Boolean.valueOf(false))
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.SPRUCE_LOG, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.GLASS_PANE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(
							new BlockStateMatchTest(
								Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
							),
							AlwaysTrueTest.INSTANCE,
							Blocks.BROWN_STAINED_GLASS_PANE
								.defaultBlockState()
								.setValue(IronBarsBlock.NORTH, Boolean.valueOf(true))
								.setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
						),
						new ProcessorRule(
							new BlockStateMatchTest(
								Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)).setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
							),
							AlwaysTrueTest.INSTANCE,
							Blocks.BROWN_STAINED_GLASS_PANE
								.defaultBlockState()
								.setValue(IronBarsBlock.EAST, Boolean.valueOf(true))
								.setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.PUMPKIN_STEM.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			ZOMBIE_DESERT,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new TagMatchTest(BlockTags.DOORS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.WALL_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.SMOOTH_SANDSTONE, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.CUT_SANDSTONE, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.TERRACOTTA, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.SMOOTH_SANDSTONE_STAIRS, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.SMOOTH_SANDSTONE_SLAB, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			MOSSIFY_10_PERCENT,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			MOSSIFY_20_PERCENT,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			MOSSIFY_70_PERCENT,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.7F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			STREET_PLAINS,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new BlockMatchTest(Blocks.DIRT_PATH), new BlockMatchTest(Blocks.WATER), Blocks.OAK_PLANKS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.DIRT_PATH, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.PEELGRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.CORRUPTED_PEELGRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.TERREDEPOMME), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			SPOIL_10_PERCENT,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.TATERSTONE, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.EXPIRED_BAKED_POTATO_BRICKS.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			SPOIL_20_PERCENT,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.TATERSTONE, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.EXPIRED_BAKED_POTATO_BRICKS.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			SPOIL_70_PERCENT,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.TATERSTONE, 0.7F), AlwaysTrueTest.INSTANCE, Blocks.EXPIRED_BAKED_POTATO_BRICKS.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			STREET_POTATO,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new BlockMatchTest(Blocks.POISON_PATH), new BlockMatchTest(Blocks.WATER), Blocks.POTATO_PLANKS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.POISON_PATH, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.PEELGRASS_BLOCK.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.PEELGRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.TERREDEPOMME), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.CORRUPTED_PEELGRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			STREET_SAVANNA,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new BlockMatchTest(Blocks.DIRT_PATH), new BlockMatchTest(Blocks.WATER), Blocks.ACACIA_PLANKS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.DIRT_PATH, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.PEELGRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.CORRUPTED_PEELGRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.TERREDEPOMME), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			STREET_SNOWY_OR_TAIGA,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new BlockMatchTest(Blocks.DIRT_PATH), new BlockMatchTest(Blocks.WATER), Blocks.SPRUCE_PLANKS.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.DIRT_PATH), new BlockMatchTest(Blocks.ICE), Blocks.SPRUCE_PLANKS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.DIRT_PATH, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.PEELGRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.CORRUPTED_PEELGRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
						new ProcessorRule(new BlockMatchTest(Blocks.TERREDEPOMME), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			FARM_PLAINS,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			FARM_POTATO,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			FARM_SAVANNA,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState()))
				)
			)
		);
		register(
			bootstrapContext,
			FARM_SNOWY,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			FARM_TAIGA,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.PUMPKIN_STEM.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			FARM_DESERT,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState())
					)
				)
			)
		);
		register(bootstrapContext, OUTPOST_ROT, ImmutableList.of(new BlockRotProcessor(0.05F)));
		register(
			bootstrapContext,
			BOTTOM_RAMPART,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.MAGMA_BLOCK, 0.75F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, 0.15F),
							AlwaysTrueTest.INSTANCE,
							Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						processorRule2,
						processorRule
					)
				)
			)
		);
		register(
			bootstrapContext,
			TREASURE_ROOMS,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.35F),
							AlwaysTrueTest.INSTANCE,
							Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.1F),
							AlwaysTrueTest.INSTANCE,
							Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						processorRule2,
						processorRule
					)
				)
			)
		);
		register(
			bootstrapContext,
			HOUSING,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F),
							AlwaysTrueTest.INSTANCE,
							Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						processorRule2,
						processorRule
					)
				)
			)
		);
		register(
			bootstrapContext,
			SIDE_WALL_DEGRADATION,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						processorRule2,
						processorRule
					)
				)
			)
		);
		register(
			bootstrapContext,
			STABLE_DEGRADATION,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.1F),
							AlwaysTrueTest.INSTANCE,
							Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						processorRule2,
						processorRule
					)
				)
			)
		);
		register(
			bootstrapContext,
			BASTION_GENERIC_DEGRADATION,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F),
							AlwaysTrueTest.INSTANCE,
							Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						processorRule2,
						processorRule
					)
				)
			)
		);
		register(
			bootstrapContext,
			RAMPART_DEGRADATION,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.4F),
							AlwaysTrueTest.INSTANCE,
							Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.BLACKSTONE, 0.01F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						processorRule2,
						processorRule
					)
				)
			)
		);
		register(
			bootstrapContext,
			ENTRANCE_REPLACEMENT,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.6F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						processorRule2,
						processorRule
					)
				)
			)
		);
		register(
			bootstrapContext,
			BRIDGE,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F),
							AlwaysTrueTest.INSTANCE,
							Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			ROOF,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F),
							AlwaysTrueTest.INSTANCE,
							Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.15F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.BLACKSTONE.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			HIGH_WALL,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.01F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.5F),
							AlwaysTrueTest.INSTANCE,
							Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.BLACKSTONE.defaultBlockState()),
						processorRule2
					)
				)
			)
		);
		register(
			bootstrapContext,
			HIGH_RAMPART,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
						),
						new ProcessorRule(
							AlwaysTrueTest.INSTANCE, AlwaysTrueTest.INSTANCE, new AxisAlignedLinearPosTest(0.0F, 0.05F, 0, 100, Direction.Axis.Y), Blocks.AIR.defaultBlockState()
						),
						processorRule2
					)
				)
			)
		);
		register(bootstrapContext, FOSSIL_ROT, ImmutableList.of(new BlockRotProcessor(0.9F), new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)));
		register(bootstrapContext, FOSSIL_COAL, ImmutableList.of(new BlockRotProcessor(0.1F), new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)));
		register(
			bootstrapContext,
			COLOSSEUM_VEINS,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(
							new RandomBlockMatchTest(Blocks.BAKED_POTATO_BRICKS, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.EXPIRED_BAKED_POTATO_BRICKS.defaultBlockState()
						),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.BAKED_POTATO_BRICKS, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
					)
				)
			)
		);
		register(
			bootstrapContext,
			FOSSIL_DIAMONDS,
			ImmutableList.of(
				new BlockRotProcessor(0.1F),
				new RuleProcessor(
					ImmutableList.of(new ProcessorRule(new BlockMatchTest(Blocks.COAL_ORE), AlwaysTrueTest.INSTANCE, Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState()))
				),
				new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
			)
		);
		register(
			bootstrapContext,
			ANCIENT_CITY_START_DEGRADATION,
			ImmutableList.of(
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.DEEPSLATE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.DEEPSLATE_TILES, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
					)
				),
				new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
			)
		);
		register(
			bootstrapContext,
			ANCIENT_CITY_GENERIC_DEGRADATION,
			ImmutableList.of(
				new BlockRotProcessor(holderGetter.getOrThrow(BlockTags.ANCIENT_CITY_REPLACEABLE), 0.95F),
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.DEEPSLATE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.DEEPSLATE_TILES, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
					)
				),
				new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
			)
		);
		register(
			bootstrapContext,
			ANCIENT_CITY_WALLS_DEGRADATION,
			ImmutableList.of(
				new BlockRotProcessor(holderGetter.getOrThrow(BlockTags.ANCIENT_CITY_REPLACEABLE), 0.95F),
				new RuleProcessor(
					ImmutableList.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.DEEPSLATE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.DEEPSLATE_TILES, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.DEEPSLATE_TILE_SLAB, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
					)
				),
				new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
			)
		);
		register(
			bootstrapContext,
			TRAIL_RUINS_HOUSES_ARCHAEOLOGY,
			List.of(
				new RuleProcessor(
					List.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.GRAVEL, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.DIRT.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.GRAVEL, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.COARSE_DIRT.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.MUD_BRICKS, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.PACKED_MUD.defaultBlockState())
					)
				),
				trailsArchyLootProcessor(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON, 6),
				trailsArchyLootProcessor(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE, 3)
			)
		);
		register(
			bootstrapContext,
			TRAIL_RUINS_ROADS_ARCHAEOLOGY,
			List.of(
				new RuleProcessor(
					List.of(
						new ProcessorRule(new RandomBlockMatchTest(Blocks.GRAVEL, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.DIRT.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.GRAVEL, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.COARSE_DIRT.defaultBlockState()),
						new ProcessorRule(new RandomBlockMatchTest(Blocks.MUD_BRICKS, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.PACKED_MUD.defaultBlockState())
					)
				),
				trailsArchyLootProcessor(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON, 2)
			)
		);
		register(bootstrapContext, TRAIL_RUINS_TOWER_TOP_ARCHAEOLOGY, List.of(trailsArchyLootProcessor(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON, 2)));
	}

	private static CappedProcessor trailsArchyLootProcessor(ResourceKey<LootTable> resourceKey, int i) {
		return new CappedProcessor(
			new RuleProcessor(
				List.of(
					new ProcessorRule(
						new TagMatchTest(BlockTags.TRAIL_RUINS_REPLACEABLE),
						AlwaysTrueTest.INSTANCE,
						PosAlwaysTrueTest.INSTANCE,
						Blocks.SUSPICIOUS_GRAVEL.defaultBlockState(),
						new AppendLoot(resourceKey)
					)
				)
			),
			ConstantInt.of(i)
		);
	}
}
