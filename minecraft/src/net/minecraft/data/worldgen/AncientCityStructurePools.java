package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class AncientCityStructurePools {
	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<PlacedFeature> holderGetter = bootstrapContext.lookup(Registries.PLACED_FEATURE);
		Holder<PlacedFeature> holder = holderGetter.getOrThrow(CavePlacements.SCULK_PATCH_ANCIENT_CITY);
		HolderGetter<StructureProcessorList> holderGetter2 = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder2 = holderGetter2.getOrThrow(ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION);
		Holder<StructureProcessorList> holder3 = holderGetter2.getOrThrow(ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION);
		HolderGetter<StructureTemplatePool> holderGetter3 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder4 = holderGetter3.getOrThrow(Pools.EMPTY);
		Pools.register(
			bootstrapContext,
			"ancient_city/structures",
			new StructureTemplatePool(
				holder4,
				ImmutableList.of(
					Pair.of(StructurePoolElement.empty(), 7),
					Pair.of(StructurePoolElement.single("ancient_city/structures/barracks", holder2), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/chamber_1", holder2), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/chamber_2", holder2), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/chamber_3", holder2), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/sauna_1", holder2), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/small_statue", holder2), 4),
					Pair.of(StructurePoolElement.single("ancient_city/structures/large_ruin_1", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_1", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_2", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_3", holder2), 2),
					Pair.of(StructurePoolElement.single("ancient_city/structures/tall_ruin_4", holder2), 2),
					Pair.of(
						StructurePoolElement.list(
							ImmutableList.of(
								StructurePoolElement.single("ancient_city/structures/camp_1", holder2),
								StructurePoolElement.single("ancient_city/structures/camp_2", holder2),
								StructurePoolElement.single("ancient_city/structures/camp_3", holder2)
							)
						),
						1
					),
					Pair.of(StructurePoolElement.single("ancient_city/structures/medium_ruin_1", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/medium_ruin_2", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/small_ruin_1", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/small_ruin_2", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/large_pillar_1", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/structures/medium_pillar_1", holder2), 1),
					Pair.of(StructurePoolElement.list(ImmutableList.of(StructurePoolElement.single("ancient_city/structures/ice_box_1"))), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"ancient_city/sculk",
			new StructureTemplatePool(
				holder4,
				ImmutableList.of(Pair.of(StructurePoolElement.feature(holder), 6), Pair.of(StructurePoolElement.empty(), 1)),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"ancient_city/walls",
			new StructureTemplatePool(
				holder4,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_corner_wall_1", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_intersection_wall_1", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_lshape_wall_1", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_1", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_2", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_1", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_2", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_3", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_4", holder3), 4),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_passage_1", holder3), 3),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_corner_wall_1", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_corner_wall_2", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_1", holder3), 2),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_2", holder3), 2),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_3", holder3), 3),
					Pair.of(StructurePoolElement.single("ancient_city/walls/ruined_horizontal_wall_stairs_4", holder3), 3)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"ancient_city/walls/no_corners",
			new StructureTemplatePool(
				holder4,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_1", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_2", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_1", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_2", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_3", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_4", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_stairs_5", holder3), 1),
					Pair.of(StructurePoolElement.single("ancient_city/walls/intact_horizontal_wall_bridge", holder3), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"ancient_city/city_center/walls",
			new StructureTemplatePool(
				holder4,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_1", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_2", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_left_corner", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_right_corner_1", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/bottom_right_corner_2", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/left", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/right", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/top", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/top_right_corner", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/walls/top_left_corner", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"ancient_city/city/entrance",
			new StructureTemplatePool(
				holder4,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_connector", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_1", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_2", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_3", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_4", holder2), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city/entrance/entrance_path_5", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
