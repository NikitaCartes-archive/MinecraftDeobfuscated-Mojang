package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.worldgen.placement.AncientCityPlacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

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
					Pair.of(StructurePoolElement.single("ancient_city/structures/small_portal_statue", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/large_ruin_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/medium_ruin_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/medium_ruin_2", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/small_ruin_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/small_ruin_2", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/large_pillar_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/medium_pillar_1", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("ancient_city/sculk"),
				new ResourceLocation("empty"),
				ImmutableList.of(Pair.of(StructurePoolElement.feature(AncientCityPlacements.SCULK_CATALYST_WITH_PATCHES_CITY), 6), Pair.of(StructurePoolElement.empty(), 1)),
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
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_3", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1)
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
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_3", ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			new StructureTemplatePool(
				new ResourceLocation("ancient_city/city_center/walls"),
				new ResourceLocation("empty"),
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_left_corner", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_right_corner", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
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
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/top_piece", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/bottom_piece", ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
