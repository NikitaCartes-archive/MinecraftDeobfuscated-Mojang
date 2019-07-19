package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.FeaturePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class SnowyVillagePools {
	public static void bootstrap() {
	}

	static {
		ImmutableList<StructureProcessor> immutableList = ImmutableList.of(
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
		);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/snowy/town_centers"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new SinglePoolElement("village/snowy/town_centers/snowy_meeting_point_1"), 100),
						new Pair<>(new SinglePoolElement("village/snowy/town_centers/snowy_meeting_point_2"), 50),
						new Pair<>(new SinglePoolElement("village/snowy/town_centers/snowy_meeting_point_3"), 150),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/town_centers/snowy_meeting_point_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/town_centers/snowy_meeting_point_2"), 1),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/town_centers/snowy_meeting_point_3"), 3)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		ImmutableList<StructureProcessor> immutableList2 = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new BlockMatchTest(Blocks.GRASS_PATH), new BlockMatchTest(Blocks.WATER), Blocks.SPRUCE_PLANKS.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.GRASS_PATH, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
					new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
					new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
				)
			)
		);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/snowy/streets"),
					new ResourceLocation("village/snowy/terminators"),
					ImmutableList.of(
						new Pair<>(new SinglePoolElement("village/snowy/streets/corner_01", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/streets/corner_02", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/streets/corner_03", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/streets/square_01", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/streets/straight_01", immutableList2), 4),
						new Pair<>(new SinglePoolElement("village/snowy/streets/straight_02", immutableList2), 4),
						new Pair<>(new SinglePoolElement("village/snowy/streets/straight_03", immutableList2), 4),
						new Pair<>(new SinglePoolElement("village/snowy/streets/straight_04", immutableList2), 7),
						new Pair<>(new SinglePoolElement("village/snowy/streets/straight_06", immutableList2), 4),
						new Pair<>(new SinglePoolElement("village/snowy/streets/straight_08", immutableList2), 4),
						new Pair<>(new SinglePoolElement("village/snowy/streets/crossroad_02", immutableList2), 1),
						new Pair<>(new SinglePoolElement("village/snowy/streets/crossroad_03", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/streets/crossroad_04", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/streets/crossroad_05", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/streets/crossroad_06", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/streets/turn_01", immutableList2), 3)
					),
					StructureTemplatePool.Projection.TERRAIN_MATCHING
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/snowy/zombie/streets"),
					new ResourceLocation("village/snowy/terminators"),
					ImmutableList.of(
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/corner_01", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/corner_02", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/corner_03", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/square_01", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/straight_01", immutableList2), 4),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/straight_02", immutableList2), 4),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/straight_03", immutableList2), 4),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/straight_04", immutableList2), 7),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/straight_06", immutableList2), 4),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/straight_08", immutableList2), 4),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/crossroad_02", immutableList2), 1),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/crossroad_03", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/crossroad_04", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/crossroad_05", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/crossroad_06", immutableList2), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/streets/turn_01", immutableList2), 3)
					),
					StructureTemplatePool.Projection.TERRAIN_MATCHING
				)
			);
		ImmutableList<StructureProcessor> immutableList3 = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
				)
			)
		);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/snowy/houses"),
					new ResourceLocation("village/snowy/terminators"),
					ImmutableList.of(
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_small_house_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_small_house_2"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_small_house_3"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_small_house_4"), 3),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_small_house_5"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_small_house_6"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_small_house_7"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_small_house_8"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_medium_house_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_medium_house_2"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_medium_house_3"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_butchers_shop_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_butchers_shop_2"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_tool_smith_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_fletcher_house_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_shepherds_house_1"), 3),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_armorer_house_1"), 1),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_armorer_house_2"), 1),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_fisher_cottage"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_tannery_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_cartographer_house_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_library_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_masons_house_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_masons_house_2"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_weapon_smith_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_temple_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_farm_1", immutableList3), 3),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_farm_2", immutableList3), 3),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_animal_pen_1"), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_animal_pen_2"), 2),
						Pair.of(EmptyPoolElement.INSTANCE, 6)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/snowy/zombie/houses"),
					new ResourceLocation("village/snowy/terminators"),
					ImmutableList.of(
						new Pair<>(new SinglePoolElement("village/snowy/zombie/houses/snowy_small_house_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/houses/snowy_small_house_2", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/houses/snowy_small_house_3", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/houses/snowy_small_house_4", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/houses/snowy_small_house_5", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/houses/snowy_small_house_6", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/houses/snowy_small_house_7", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/houses/snowy_small_house_8", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/houses/snowy_medium_house_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/houses/snowy_medium_house_2", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/houses/snowy_medium_house_3", immutableList), 1),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_butchers_shop_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_butchers_shop_2", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_tool_smith_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_fletcher_house_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_shepherds_house_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_armorer_house_1", immutableList), 1),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_armorer_house_2", immutableList), 1),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_fisher_cottage", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_tannery_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_cartographer_house_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_library_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_masons_house_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_masons_house_2", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_weapon_smith_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_temple_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_farm_1", immutableList), 3),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_farm_2", immutableList), 3),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_animal_pen_1", immutableList), 2),
						new Pair<>(new SinglePoolElement("village/snowy/houses/snowy_animal_pen_2", immutableList), 2),
						Pair.of(EmptyPoolElement.INSTANCE, 6)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/snowy/terminators"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_01", immutableList2), 1),
						new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_02", immutableList2), 1),
						new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_03", immutableList2), 1),
						new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_04", immutableList2), 1)
					),
					StructureTemplatePool.Projection.TERRAIN_MATCHING
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/snowy/trees"),
					new ResourceLocation("empty"),
					ImmutableList.of(new Pair<>(new FeaturePoolElement(new ConfiguredFeature<>(Feature.SPRUCE_TREE, FeatureConfiguration.NONE)), 1)),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/snowy/decor"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new SinglePoolElement("village/snowy/snowy_lamp_post_01"), 4),
						new Pair<>(new SinglePoolElement("village/snowy/snowy_lamp_post_02"), 4),
						new Pair<>(new SinglePoolElement("village/snowy/snowy_lamp_post_03"), 1),
						new Pair<>(new FeaturePoolElement(new ConfiguredFeature<>(Feature.SPRUCE_TREE, FeatureConfiguration.NONE)), 4),
						new Pair<>(new FeaturePoolElement(new ConfiguredFeature<>(Feature.SNOW_PILE, FeatureConfiguration.NONE)), 4),
						new Pair<>(new FeaturePoolElement(new ConfiguredFeature<>(Feature.ICE_PILE, FeatureConfiguration.NONE)), 1),
						Pair.of(EmptyPoolElement.INSTANCE, 9)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/snowy/zombie/decor"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new SinglePoolElement("village/snowy/snowy_lamp_post_01", immutableList), 1),
						new Pair<>(new SinglePoolElement("village/snowy/snowy_lamp_post_02", immutableList), 1),
						new Pair<>(new SinglePoolElement("village/snowy/snowy_lamp_post_03", immutableList), 1),
						new Pair<>(new FeaturePoolElement(new ConfiguredFeature<>(Feature.SPRUCE_TREE, FeatureConfiguration.NONE)), 4),
						new Pair<>(new FeaturePoolElement(new ConfiguredFeature<>(Feature.SNOW_PILE, FeatureConfiguration.NONE)), 4),
						new Pair<>(new FeaturePoolElement(new ConfiguredFeature<>(Feature.ICE_PILE, FeatureConfiguration.NONE)), 4),
						Pair.of(EmptyPoolElement.INSTANCE, 7)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/snowy/villagers"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new SinglePoolElement("village/snowy/villagers/nitwit"), 1),
						new Pair<>(new SinglePoolElement("village/snowy/villagers/baby"), 1),
						new Pair<>(new SinglePoolElement("village/snowy/villagers/unemployed"), 10)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/snowy/zombie/villagers"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new SinglePoolElement("village/snowy/zombie/villagers/nitwit"), 1),
						new Pair<>(new SinglePoolElement("village/snowy/zombie/villagers/unemployed"), 10)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
	}
}
