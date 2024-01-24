package net.minecraft.data.worldgen;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class TrailRuinsStructurePools {
	public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("trail_ruins/tower");

	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<StructureTemplatePool> holderGetter = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder = holderGetter.getOrThrow(Pools.EMPTY);
		HolderGetter<StructureProcessorList> holderGetter2 = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder2 = holderGetter2.getOrThrow(ProcessorLists.TRAIL_RUINS_HOUSES_ARCHAEOLOGY);
		Holder<StructureProcessorList> holder3 = holderGetter2.getOrThrow(ProcessorLists.TRAIL_RUINS_ROADS_ARCHAEOLOGY);
		Holder<StructureProcessorList> holder4 = holderGetter2.getOrThrow(ProcessorLists.TRAIL_RUINS_TOWER_TOP_ARCHAEOLOGY);
		bootstrapContext.register(
			START,
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_5", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trail_ruins/tower/tower_top",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_top_1", holder4), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_top_2", holder4), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_top_3", holder4), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_top_4", holder4), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_top_5", holder4), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trail_ruins/tower/additions",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/tower/hall_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/hall_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/hall_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/hall_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/hall_5", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/large_hall_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/large_hall_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/large_hall_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/large_hall_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/large_hall_5", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/one_room_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/one_room_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/one_room_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/one_room_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/one_room_5", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/platform_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/platform_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/platform_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/platform_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/platform_5", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/stable_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/stable_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/stable_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/stable_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/stable_5", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trail_ruins/roads",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/roads/long_road_end", holder3), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_end_1", holder3), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_section_1", holder3), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_section_2", holder3), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_section_3", holder3), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_section_4", holder3), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_spacer_1", holder3), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trail_ruins/buildings",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_hall_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_hall_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_hall_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_hall_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_hall_5", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/large_room_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/large_room_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/large_room_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/large_room_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/large_room_5", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/one_room_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/one_room_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/one_room_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/one_room_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/one_room_5", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trail_ruins/buildings/grouped",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_full_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_full_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_full_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_full_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_full_5", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_lower_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_lower_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_lower_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_lower_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_lower_5", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_upper_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_upper_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_upper_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_upper_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_upper_5", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_5", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trail_ruins/decor",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_5", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_6", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_7", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
