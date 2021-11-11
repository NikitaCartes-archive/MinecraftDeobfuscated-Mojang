package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.worldgen.features.PileFeatures;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

public class SavannaVillagePools {
	public static final StructureTemplatePool START = Pools.register(
		new StructureTemplatePool(
			new ResourceLocation("village/savanna/town_centers"),
			new ResourceLocation("empty"),
			ImmutableList.of(
				Pair.of(StructurePoolElement.legacy("village/savanna/town_centers/savanna_meeting_point_1"), 100),
				Pair.of(StructurePoolElement.legacy("village/savanna/town_centers/savanna_meeting_point_2"), 50),
				Pair.of(StructurePoolElement.legacy("village/savanna/town_centers/savanna_meeting_point_3"), 150),
				Pair.of(StructurePoolElement.legacy("village/savanna/town_centers/savanna_meeting_point_4"), 150),
				Pair.of(StructurePoolElement.legacy("village/savanna/zombie/town_centers/savanna_meeting_point_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
				Pair.of(StructurePoolElement.legacy("village/savanna/zombie/town_centers/savanna_meeting_point_2", ProcessorLists.ZOMBIE_SAVANNA), 1),
				Pair.of(StructurePoolElement.legacy("village/savanna/zombie/town_centers/savanna_meeting_point_3", ProcessorLists.ZOMBIE_SAVANNA), 3),
				Pair.of(StructurePoolElement.legacy("village/savanna/zombie/town_centers/savanna_meeting_point_4", ProcessorLists.ZOMBIE_SAVANNA), 3)
			),
			StructureTemplatePool.Projection.RIGID
		)
	);

	public static void bootstrap() {
	}

	static {
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("village/savanna/streets"),
				new ResourceLocation("village/savanna/terminators"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/corner_01", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/corner_03", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/straight_02", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/straight_04", ProcessorLists.STREET_SAVANNA), 7),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/straight_05", ProcessorLists.STREET_SAVANNA), 3),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/straight_06", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/straight_08", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/straight_09", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/straight_10", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/straight_11", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/crossroad_02", ProcessorLists.STREET_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/crossroad_03", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/crossroad_04", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/crossroad_05", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/crossroad_06", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/crossroad_07", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/split_01", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/split_02", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/streets/turn_01", ProcessorLists.STREET_SAVANNA), 3)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("village/savanna/zombie/streets"),
				new ResourceLocation("village/savanna/zombie/terminators"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/corner_01", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/corner_03", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/straight_02", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/straight_04", ProcessorLists.STREET_SAVANNA), 7),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/straight_05", ProcessorLists.STREET_SAVANNA), 3),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/straight_06", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/straight_08", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/straight_09", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/straight_10", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/straight_11", ProcessorLists.STREET_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/crossroad_02", ProcessorLists.STREET_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/crossroad_03", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/crossroad_04", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/crossroad_05", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/crossroad_06", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/crossroad_07", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/split_01", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/split_02", ProcessorLists.STREET_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/streets/turn_01", ProcessorLists.STREET_SAVANNA), 3)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("village/savanna/houses"),
				new ResourceLocation("village/savanna/terminators"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_small_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_small_house_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_small_house_3"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_small_house_4"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_small_house_5"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_small_house_6"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_small_house_7"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_small_house_8"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_medium_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_medium_house_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_butchers_shop_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_butchers_shop_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_tool_smith_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_fletcher_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_shepherd_1"), 7),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_armorer_1"), 1),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_fisher_cottage_1"), 3),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_tannery_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_cartographer_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_library_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_mason_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_weaponsmith_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_weaponsmith_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_temple_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_temple_2"), 3),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_large_farm_1", ProcessorLists.FARM_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_large_farm_2", ProcessorLists.FARM_SAVANNA), 6),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_small_farm", ProcessorLists.FARM_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_animal_pen_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_animal_pen_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_animal_pen_3"), 2),
					Pair.of(StructurePoolElement.empty(), 5)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("village/savanna/zombie/houses"),
				new ResourceLocation("village/savanna/zombie/terminators"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_small_house_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_small_house_2", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_small_house_3", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_small_house_4", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_small_house_5", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_small_house_6", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_small_house_7", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_small_house_8", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_medium_house_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_medium_house_2", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_butchers_shop_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_butchers_shop_2", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_tool_smith_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_fletcher_house_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_shepherd_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_armorer_1", ProcessorLists.ZOMBIE_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_fisher_cottage_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_tannery_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_cartographer_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_library_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_mason_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_weaponsmith_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_weaponsmith_2", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_temple_1", ProcessorLists.ZOMBIE_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_temple_2", ProcessorLists.ZOMBIE_SAVANNA), 3),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_large_farm_1", ProcessorLists.ZOMBIE_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_large_farm_2", ProcessorLists.ZOMBIE_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_small_farm", ProcessorLists.ZOMBIE_SAVANNA), 4),
					Pair.of(StructurePoolElement.legacy("village/savanna/houses/savanna_animal_pen_1", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_animal_pen_2", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/houses/savanna_animal_pen_3", ProcessorLists.ZOMBIE_SAVANNA), 2),
					Pair.of(StructurePoolElement.empty(), 5)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("village/savanna/terminators"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_01", ProcessorLists.STREET_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_02", ProcessorLists.STREET_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_03", ProcessorLists.STREET_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_04", ProcessorLists.STREET_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/savanna/terminators/terminator_05", ProcessorLists.STREET_SAVANNA), 1)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("village/savanna/zombie/terminators"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_01", ProcessorLists.STREET_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_02", ProcessorLists.STREET_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_03", ProcessorLists.STREET_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_04", ProcessorLists.STREET_SAVANNA), 1),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/terminators/terminator_05", ProcessorLists.STREET_SAVANNA), 1)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("village/savanna/trees"),
				new ResourceLocation("empty"),
				ImmutableList.of(Pair.of(StructurePoolElement.feature(TreePlacements.ACACIA_CHECKED), 1)),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("village/savanna/decor"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/savanna/savanna_lamp_post_01"), 4),
					Pair.of(StructurePoolElement.feature(TreePlacements.ACACIA_CHECKED), 4),
					Pair.of(StructurePoolElement.feature(PileFeatures.PILE_HAY.placed()), 4),
					Pair.of(StructurePoolElement.feature(PileFeatures.PILE_MELON.placed()), 1),
					Pair.of(StructurePoolElement.empty(), 4)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("village/savanna/zombie/decor"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/savanna/savanna_lamp_post_01", ProcessorLists.ZOMBIE_SAVANNA), 4),
					Pair.of(StructurePoolElement.feature(TreePlacements.ACACIA_CHECKED), 4),
					Pair.of(StructurePoolElement.feature(PileFeatures.PILE_HAY.placed()), 4),
					Pair.of(StructurePoolElement.feature(PileFeatures.PILE_MELON.placed()), 1),
					Pair.of(StructurePoolElement.empty(), 4)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("village/savanna/villagers"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/savanna/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/savanna/villagers/baby"), 1),
					Pair.of(StructurePoolElement.legacy("village/savanna/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("village/savanna/zombie/villagers"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/savanna/zombie/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
