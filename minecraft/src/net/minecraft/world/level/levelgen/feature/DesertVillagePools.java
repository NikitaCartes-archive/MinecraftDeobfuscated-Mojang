package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.FeaturePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class DesertVillagePools {
	public static void bootstrap() {
	}

	static {
		ImmutableList<StructureProcessor> immutableList = ImmutableList.of(
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
		);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/desert/town_centers"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/desert/town_centers/desert_meeting_point_1"), 98),
						new Pair<>(new LegacySinglePoolElement("village/desert/town_centers/desert_meeting_point_2"), 98),
						new Pair<>(new LegacySinglePoolElement("village/desert/town_centers/desert_meeting_point_3"), 49),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/town_centers/desert_meeting_point_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/town_centers/desert_meeting_point_2", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/town_centers/desert_meeting_point_3", immutableList), 1)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/desert/streets"),
					new ResourceLocation("village/desert/terminators"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/desert/streets/corner_01"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/streets/corner_02"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/streets/straight_01"), 4),
						new Pair<>(new LegacySinglePoolElement("village/desert/streets/straight_02"), 4),
						new Pair<>(new LegacySinglePoolElement("village/desert/streets/straight_03"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/streets/crossroad_01"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/streets/crossroad_02"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/streets/crossroad_03"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/streets/square_01"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/streets/square_02"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/streets/turn_01"), 3)
					),
					StructureTemplatePool.Projection.TERRAIN_MATCHING
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/desert/zombie/streets"),
					new ResourceLocation("village/desert/zombie/terminators"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/streets/corner_01"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/streets/corner_02"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/streets/straight_01"), 4),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/streets/straight_02"), 4),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/streets/straight_03"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/streets/crossroad_01"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/streets/crossroad_02"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/streets/crossroad_03"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/streets/square_01"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/streets/square_02"), 3),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/streets/turn_01"), 3)
					),
					StructureTemplatePool.Projection.TERRAIN_MATCHING
				)
			);
		ImmutableList<StructureProcessor> immutableList2 = ImmutableList.of(
			new RuleProcessor(
				ImmutableList.of(
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState()),
					new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState())
				)
			)
		);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/desert/houses"),
					new ResourceLocation("village/desert/terminators"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_small_house_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_small_house_2"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_small_house_3"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_small_house_4"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_small_house_5"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_small_house_6"), 1),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_small_house_7"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_small_house_8"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_medium_house_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_medium_house_2"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_butcher_shop_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_tool_smith_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_fletcher_house_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_shepherd_house_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_armorer_1"), 1),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_fisher_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_tannery_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_cartographer_house_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_library_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_mason_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_weaponsmith_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_temple_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_temple_2"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_large_farm_1", immutableList2), 11),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_farm_1", immutableList2), 4),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_farm_2", immutableList2), 4),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_animal_pen_1"), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_animal_pen_2"), 2),
						Pair.of(EmptyPoolElement.INSTANCE, 5)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/desert/zombie/houses"),
					new ResourceLocation("village/desert/zombie/terminators"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/houses/desert_small_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/houses/desert_small_house_2", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/houses/desert_small_house_3", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/houses/desert_small_house_4", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/houses/desert_small_house_5", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/houses/desert_small_house_6", immutableList), 1),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/houses/desert_small_house_7", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/houses/desert_small_house_8", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/houses/desert_medium_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/houses/desert_medium_house_2", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_butcher_shop_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_tool_smith_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_fletcher_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_shepherd_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_armorer_1", immutableList), 1),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_fisher_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_tannery_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_cartographer_house_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_library_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_mason_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_weaponsmith_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_temple_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_temple_2", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_large_farm_1", immutableList), 7),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_farm_1", immutableList), 4),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_farm_2", immutableList), 4),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_animal_pen_1", immutableList), 2),
						new Pair<>(new LegacySinglePoolElement("village/desert/houses/desert_animal_pen_2", immutableList), 2),
						Pair.of(EmptyPoolElement.INSTANCE, 5)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/desert/terminators"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/desert/terminators/terminator_01"), 1),
						new Pair<>(new LegacySinglePoolElement("village/desert/terminators/terminator_02"), 1)
					),
					StructureTemplatePool.Projection.TERRAIN_MATCHING
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/desert/zombie/terminators"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/desert/terminators/terminator_01"), 1),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/terminators/terminator_02"), 1)
					),
					StructureTemplatePool.Projection.TERRAIN_MATCHING
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/desert/decor"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/desert/desert_lamp_1"), 10),
						new Pair<>(new FeaturePoolElement(Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.CACTUS_CONFIG)), 4),
						new Pair<>(new FeaturePoolElement(Feature.BLOCK_PILE.configured(BiomeDefaultFeatures.HAY_PILE_CONFIG)), 4),
						Pair.of(EmptyPoolElement.INSTANCE, 10)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/desert/zombie/decor"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/desert/desert_lamp_1", immutableList), 10),
						new Pair<>(new FeaturePoolElement(Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.CACTUS_CONFIG)), 4),
						new Pair<>(new FeaturePoolElement(Feature.BLOCK_PILE.configured(BiomeDefaultFeatures.HAY_PILE_CONFIG)), 4),
						Pair.of(EmptyPoolElement.INSTANCE, 10)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/desert/villagers"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/desert/villagers/nitwit"), 1),
						new Pair<>(new LegacySinglePoolElement("village/desert/villagers/baby"), 1),
						new Pair<>(new LegacySinglePoolElement("village/desert/villagers/unemployed"), 10)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
		JigsawPlacement.POOLS
			.register(
				new StructureTemplatePool(
					new ResourceLocation("village/desert/zombie/villagers"),
					new ResourceLocation("empty"),
					ImmutableList.of(
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/villagers/nitwit"), 1),
						new Pair<>(new LegacySinglePoolElement("village/desert/zombie/villagers/unemployed"), 10)
					),
					StructureTemplatePool.Projection.RIGID
				)
			);
	}
}
