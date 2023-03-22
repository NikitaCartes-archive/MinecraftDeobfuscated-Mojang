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

	public static void bootstrap(BootstapContext<StructureTemplatePool> bootstapContext) {
		HolderGetter<StructureTemplatePool> holderGetter = bootstapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder = holderGetter.getOrThrow(Pools.EMPTY);
		HolderGetter<StructureProcessorList> holderGetter2 = bootstapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder2 = holderGetter2.getOrThrow(ProcessorLists.TRAIL_RUINS_SUSPICIOUS_SAND);
		bootstapContext.register(
			START,
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/tower_3", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstapContext,
			"trail_ruins/tower/additions",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/tower/large_hall_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/large_hall_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/platform_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/hall_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/hall_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/stable_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/tower/one_room_1", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstapContext,
			"trail_ruins/roads",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/roads/long_road_end"), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_end_1"), 2),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_section_1"), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_section_2"), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_section_3"), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_section_4"), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/roads/road_spacer_1"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstapContext,
			"trail_ruins/buildings",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_entrance_three_1", holder2), 3),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_entrance_two_1", holder2), 3),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_entrance_two_2", holder2), 3),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/large_room_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/large_room_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/large_room_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/one_room_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/one_room_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/one_room_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/one_room_4", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstapContext,
			"trail_ruins/buildings/grouped",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_one_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_one_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_two_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_two_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_two_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_two_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/buildings/group_room_two_5", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstapContext,
			"trail_ruins/decor",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_1", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_2", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_3", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_4", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_5", holder2), 1),
					Pair.of(StructurePoolElement.single("trail_ruins/decor/decor_6", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
