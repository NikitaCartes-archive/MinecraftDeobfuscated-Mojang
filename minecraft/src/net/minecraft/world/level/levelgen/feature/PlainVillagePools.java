package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.FeaturePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class PlainVillagePools {
	public static void bootstrap() {
	}

	static {
		ImmutableList<StructureProcessor> immutableList = ImmutableList.of(
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
		);
		ImmutableList<StructureProcessor> immutableList2 = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
				)
			)
		);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/plains/town_centers"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(
							new LegacySinglePoolElement(
								"village/plains/town_centers/plains_fountain_01",
								ImmutableList.of(
									new RuleProcessor(
										ImmutableList.of(
											new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
										)
									)
								)
							),
							50
						),
						new Pair<>(
							new LegacySinglePoolElement(
								"village/plains/town_centers/plains_meeting_point_1",
								ImmutableList.of(
									new RuleProcessor(
										ImmutableList.of(
											new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
										)
									)
								)
							),
							50
						),
						new Pair<>(new LegacySinglePoolElement("village/plains/town_centers/plains_meeting_point_2"), 50),
						new Pair<>(
							new LegacySinglePoolElement(
								"village/plains/town_centers/plains_meeting_point_3",
								ImmutableList.of(
									new RuleProcessor(
										ImmutableList.of(
											new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.7F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
										)
									)
								)
							),
							50
						),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/town_centers/plains_fountain_01", immutableList), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/town_centers/plains_meeting_point_1", immutableList), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/town_centers/plains_meeting_point_2", immutableList), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/town_centers/plains_meeting_point_3", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		ImmutableList<StructureProcessor> immutableList3 = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new BlockMatchTest(Blocks.GRASS_PATH), new BlockMatchTest(Blocks.WATER), Blocks.OAK_PLANKS.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.GRASS_PATH, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
					new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
					new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
				)
			)
		);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/plains/streets"),
					new ResourceLocation("village/plains/terminators"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/corner_01", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/corner_02", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/corner_03", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/straight_01", immutableList3), 4),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/straight_02", immutableList3), 4),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/straight_03", immutableList3), 7),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/straight_04", immutableList3), 7),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/straight_05", immutableList3), 3),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/straight_06", immutableList3), 4),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/crossroad_01", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/crossroad_02", immutableList3), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/crossroad_03", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/crossroad_04", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/crossroad_05", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/crossroad_06", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/streets/turn_01", immutableList3), 3)
					),
					StructureTemplatePool.Projection.TERRAIN_MATCHING
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/plains/zombie/streets"),
					new ResourceLocation("village/plains/terminators"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/corner_01", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/corner_02", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/corner_03", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/straight_01", immutableList3), 4),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/straight_02", immutableList3), 4),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/straight_03", immutableList3), 7),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/straight_04", immutableList3), 7),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/straight_05", immutableList3), 3),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/straight_06", immutableList3), 4),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/crossroad_01", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/crossroad_02", immutableList3), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/crossroad_03", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/crossroad_04", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/crossroad_05", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/crossroad_06", immutableList3), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/streets/turn_01", immutableList3), 3)
					),
					StructureTemplatePool.Projection.TERRAIN_MATCHING
				)
			);
		ImmutableList<StructureProcessor> immutableList4 = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState())
				)
			)
		);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/plains/houses"),
					new ResourceLocation("village/plains/terminators"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_small_house_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_small_house_2", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_small_house_3", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_small_house_4", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_small_house_5", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_small_house_6", immutableList2), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_small_house_7", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_small_house_8", immutableList2), 3),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_medium_house_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_medium_house_2", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_big_house_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_butcher_shop_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_butcher_shop_2", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_tool_smith_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_fletcher_house_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_shepherds_house_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_armorer_house_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_fisher_cottage_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_tannery_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_cartographer_1", immutableList2), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_library_1", immutableList2), 5),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_library_2", immutableList2), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_masons_house_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_weaponsmith_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_temple_3", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_temple_4", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_stable_1", immutableList2), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_stable_2"), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_large_farm_1", immutableList4), 4),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_small_farm_1", immutableList4), 4),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_animal_pen_1"), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_animal_pen_2"), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_animal_pen_3"), 5),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_accessory_1"), 1),
						new Pair<>(
							new LegacySinglePoolElement(
								"village/plains/houses/plains_meeting_point_4",
								ImmutableList.of(
									new RuleProcessor(
										ImmutableList.of(
											new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.7F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
										)
									)
								)
							),
							3
						),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_meeting_point_5"), 1),
						Pair.of(EmptyPoolElement.INSTANCE, 10)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/plains/zombie/houses"),
					new ResourceLocation("village/plains/terminators"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_small_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_small_house_2", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_small_house_3", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_small_house_4", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_small_house_5", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_small_house_6", immutableList), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_small_house_7", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_small_house_8", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_medium_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_medium_house_2", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_big_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_butcher_shop_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_butcher_shop_2", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_tool_smith_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_fletcher_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_shepherds_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_armorer_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_fisher_cottage_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_tannery_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_cartographer_1", immutableList), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_library_1", immutableList), 3),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_library_2", immutableList), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_masons_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_weaponsmith_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_temple_3", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_temple_4", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_stable_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_stable_2", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_large_farm_1", immutableList), 4),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_small_farm_1", immutableList), 4),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_animal_pen_1", immutableList), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/houses/plains_animal_pen_2", immutableList), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_animal_pen_3", immutableList), 5),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_meeting_point_4", immutableList), 3),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/houses/plains_meeting_point_5", immutableList), 1),
						Pair.of(EmptyPoolElement.INSTANCE, 10)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/plains/terminators"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/plains/terminators/terminator_01", immutableList3), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/terminators/terminator_02", immutableList3), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/terminators/terminator_03", immutableList3), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/terminators/terminator_04", immutableList3), 1)
					),
					StructureTemplatePool.Projection.TERRAIN_MATCHING
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/plains/trees"),
					new ResourceLocation("empty"),
					ImmutableList.of(new Pair<>(new FeaturePoolElement(Feature.TREE.configured(BiomeDefaultFeatures.NORMAL_TREE_CONFIG)), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/plains/decor"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/plains/plains_lamp_1"), 2),
						new Pair<>(new FeaturePoolElement(Feature.TREE.configured(BiomeDefaultFeatures.NORMAL_TREE_CONFIG)), 1),
						new Pair<>(new FeaturePoolElement(Feature.FLOWER.configured(BiomeDefaultFeatures.PLAIN_FLOWER_CONFIG)), 1),
						new Pair<>(new FeaturePoolElement(Feature.BLOCK_PILE.configured(BiomeDefaultFeatures.HAY_PILE_CONFIG)), 1),
						Pair.of(EmptyPoolElement.INSTANCE, 2)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/plains/zombie/decor"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/plains/plains_lamp_1", immutableList), 1),
						new Pair<>(new FeaturePoolElement(Feature.TREE.configured(BiomeDefaultFeatures.NORMAL_TREE_CONFIG)), 1),
						new Pair<>(new FeaturePoolElement(Feature.FLOWER.configured(BiomeDefaultFeatures.PLAIN_FLOWER_CONFIG)), 1),
						new Pair<>(new FeaturePoolElement(Feature.BLOCK_PILE.configured(BiomeDefaultFeatures.HAY_PILE_CONFIG)), 1),
						Pair.of(EmptyPoolElement.INSTANCE, 2)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/plains/villagers"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/plains/villagers/nitwit"), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/villagers/baby"), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/villagers/unemployed"), 10)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/plains/zombie/villagers"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/villagers/nitwit"), 1),
						new Pair<>(new LegacySinglePoolElement("village/plains/zombie/villagers/unemployed"), 10)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/common/animals"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cows_1"), 7),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/pigs_1"), 7),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/horses_1"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/horses_2"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/horses_3"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/horses_4"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/horses_5"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/sheep_1"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/sheep_2"), 1),
						Pair.of(EmptyPoolElement.INSTANCE, 5)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/common/sheep"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/common/animals/sheep_1"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/sheep_2"), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/common/cats"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cat_black"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cat_british"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cat_calico"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cat_persian"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cat_ragdoll"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cat_red"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cat_siamese"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cat_tabby"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cat_white"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cat_jellie"), 1),
						Pair.of(EmptyPoolElement.INSTANCE, 3)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/common/butcher_animals"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/common/animals/cows_1"), 3),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/pigs_1"), 3),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/sheep_1"), 1),
						new Pair<>(new LegacySinglePoolElement("village/common/animals/sheep_2"), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/common/iron_golem"),
					new ResourceLocation("empty"),
					ImmutableList.of(new Pair<>(new LegacySinglePoolElement("village/common/iron_golem"), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/common/well_bottoms"),
					new ResourceLocation("empty"),
					ImmutableList.of(new Pair<>(new LegacySinglePoolElement("village/common/well_bottom"), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
	}
}
