package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.AxisAlignedLinearPosTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class ProcessorLists {
	private static final ProcessorRule GILDED_BLACKSTONE_REPLACEMENT_RULE = new ProcessorRule(
		new RandomBlockMatchTest(Blocks.BLACKSTONE, 0.01F), AlwaysTrueTest.INSTANCE, Blocks.GILDED_BLACKSTONE.defaultBlockState()
	);
	public static final ImmutableList<StructureProcessor> ZOMBIE_PLAINS = register(
		"zombie_plains",
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
	public static final ImmutableList<StructureProcessor> ZOMBIE_SAVANNA = register(
		"zombie_savanna",
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
	public static final ImmutableList<StructureProcessor> ZOMBIE_SNOVY = register(
		"zombie_snovy",
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
	public static final ImmutableList<StructureProcessor> ZOMBIE_TAIGA = register(
		"zombie_taiga",
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
	public static final ImmutableList<StructureProcessor> ZOMBIE_DESERT = register(
		"zombie_desert",
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
	public static final ImmutableList<StructureProcessor> MOSSIFY_10_PERCENT = register(
		"mossify_10_percent",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> MOSSIFY_20_PERCENT = register(
		"mossify_20_percent",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> MOSSIFY_70_PERCENT = register(
		"mossify_70_percent",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.7F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> STREET_PLAINS = register(
		"street_plains",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new BlockMatchTest(Blocks.GRASS_PATH), new BlockMatchTest(Blocks.WATER), Blocks.OAK_PLANKS.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.GRASS_PATH, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
					new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
					new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> STREET_SAVANNA = register(
		"street_savanna",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new BlockMatchTest(Blocks.GRASS_PATH), new BlockMatchTest(Blocks.WATER), Blocks.ACACIA_PLANKS.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.GRASS_PATH, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
					new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
					new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> STREET_SNOVY_OR_TAIGA = register(
		"street_snovy_or_taiga",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new BlockMatchTest(Blocks.GRASS_PATH), new BlockMatchTest(Blocks.WATER), Blocks.SPRUCE_PLANKS.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.GRASS_PATH, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
					new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
					new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> FARM_PLAINS = register(
		"farm_plains",
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
	public static final ImmutableList<StructureProcessor> FARM_SAVANNA = register(
		"farm_savanna",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState()))
			)
		)
	);
	public static final ImmutableList<StructureProcessor> FARM_SNOVY = register(
		"farm_snovy",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> FARM_TAIGA = register(
		"farm_taiga",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.PUMPKIN_STEM.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> FARM_DESERT = register(
		"farm_desert",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> BOTTOM_RAMPART = register(
		"bottom_rampart",
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
					GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> TREASURE_ROOMS = register(
		"treasure_rooms",
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
					GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> HOUSING = register(
		"housing",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> SIDE_WALL_DEGRADATION = register(
		"side_wall_degradation",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> STABLE_DEGRADATION = register(
		"stable_degradation",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> BASTION_GENERIC_DEGRADATION = register(
		"bastion_generic_degradation",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> RAMPART_DEGRADATION = register(
		"rampart_degradation",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.4F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.BLACKSTONE, 0.01F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> ENTRANCE_REPLACEMENT = register(
		"entrance_replacement",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.6F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					GILDED_BLACKSTONE_REPLACEMENT_RULE
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> BRIDGE = register(
		"bridge",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> ROOF = register(
		"roof",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.15F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.BLACKSTONE.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> HIGH_WALL = register(
		"high_wall",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.01F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.BLACKSTONE.defaultBlockState())
				)
			)
		)
	);
	public static final ImmutableList<StructureProcessor> HIGH_RAMPART = register(
		"high_rampart",
		ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(
						new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
					),
					new ProcessorRule(
						AlwaysTrueTest.INSTANCE, AlwaysTrueTest.INSTANCE, new AxisAlignedLinearPosTest(0.0F, 0.05F, 0, 100, Direction.Axis.Y), Blocks.AIR.defaultBlockState()
					)
				)
			)
		)
	);

	private static ImmutableList<StructureProcessor> register(String string, ImmutableList<StructureProcessor> immutableList) {
		return BuiltinRegistries.register(BuiltinRegistries.PROCESSOR_LIST, string, immutableList);
	}
}
