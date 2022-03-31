package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class AncientCityStructurePools {
	public static void bootstrap() {
	}

	static {
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("ancient_city/structures"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.empty(), 7),
					Pair.of(StructurePoolElement.single("ancient_city/structures/barracks", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/chamber_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/chamber_2", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/chamber_3", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/sauna_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/small_statue", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/large_ruin_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_2", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_3", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 2),
					Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_4", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 2),
					Pair.of(
						StructurePoolElement.list(
							ImmutableList.of(
								StructurePoolElement.single("ancient_city/structures/camp_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION),
								StructurePoolElement.single("ancient_city/structures/camp_2", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION),
								StructurePoolElement.single("ancient_city/structures/camp_3", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION)
							)
						),
						1
					),
					Pair.of(StructurePoolElement.single("ancient_city/structures/medium_ruin_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/medium_ruin_2", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/small_ruin_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/small_ruin_2", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/large_pillar_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/medium_pillar_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.list(ImmutableList.of(StructurePoolElement.single("ancient_city/structures/ice_box_1"))), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("ancient_city/sculk"),
				new ResourceLocation("empty"),
				ImmutableList.of(Pair.of(StructurePoolElement.feature(CavePlacements.SCULK_PATCH_ANCIENT_CITY), 6), Pair.of(StructurePoolElement.empty(), 1)),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("ancient_city/walls"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_corner_wall_1", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_intersection_wall_1", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_lshape_wall_1", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_1", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_2", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_1", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_2", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_3", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_4", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 4),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_passage_1", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 3),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_corner_wall_1", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_corner_wall_2", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_1", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 2),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_2", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 2),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_3", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 3),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_4", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 3)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("ancient_city/walls/no_corners"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_1", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_2", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_1", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_2", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_3", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_4", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_5", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_bridge", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("ancient_city/city_center/walls"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_2", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_left_corner", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_right_corner_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_right_corner_2", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/left", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/right", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/top", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/top_right_corner", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/top_left_corner", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("ancient_city/city/entrance"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_connector", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_2", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_3", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_4", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_5", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
