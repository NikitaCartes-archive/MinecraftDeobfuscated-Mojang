package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class ColosseumPools {
	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		Holder<StructureProcessorList> holder = bootstrapContext.lookup(Registries.PROCESSOR_LIST).getOrThrow(ProcessorLists.COLOSSEUM_VEINS);
		Holder<StructureTemplatePool> holder2 = bootstrapContext.lookup(Registries.TEMPLATE_POOL).getOrThrow(Pools.EMPTY);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/bases",
			new StructureTemplatePool(
				holder2, ImmutableList.of(Pair.of(StructurePoolElement.single("colosseum/treasure/bases/lava_basin", holder), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/stairs",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(Pair.of(StructurePoolElement.single("colosseum/treasure/stairs/lower_stairs", holder), 1)),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/bases/centers",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/bases/centers/center_0", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/bases/centers/center_1", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/bases/centers/center_2", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/bases/centers/center_3", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/brains",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(Pair.of(StructurePoolElement.single("colosseum/treasure/brains/center_brain", holder), 1)),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/walls",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/lava_wall", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/entrance_wall", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/walls/outer",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/outer/top_corner", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/outer/mid_corner", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/outer/bottom_corner", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/outer/outer_wall", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/outer/medium_outer_wall", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/outer/tall_outer_wall", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/walls/bottom",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/bottom/wall_0", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/bottom/wall_1", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/bottom/wall_2", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/bottom/wall_3", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/walls/mid",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/mid/wall_0", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/mid/wall_1", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/mid/wall_2", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/walls/top",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/top/main_entrance", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/top/wall_0", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/walls/top/wall_1", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/connectors",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/connectors/center_to_wall_middle", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/connectors/center_to_wall_top", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/connectors/center_to_wall_top_entrance", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/corners/bottom",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/corners/bottom/corner_0", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/corners/bottom/corner_1", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/corners/edges",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/corners/edges/bottom", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/corners/edges/middle", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/corners/edges/top", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/corners/middle",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/corners/middle/corner_0", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/corners/middle/corner_1", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/corners/top",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/corners/top/corner_0", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/corners/top/corner_1", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/extensions/large_pool",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/empty", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/empty", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/fire_room", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/large_bridge_0", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/large_bridge_1", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/large_bridge_2", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/large_bridge_3", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/roofed_bridge", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/empty", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/extensions/small_pool",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/empty", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/fire_room", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/empty", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/small_bridge_0", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/small_bridge_1", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/small_bridge_2", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/small_bridge_3", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/extensions/houses",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/house_0", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/extensions/house_1", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/treasure/roofs",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("colosseum/treasure/roofs/wall_roof", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/roofs/corner_roof", holder), 1),
					Pair.of(StructurePoolElement.single("colosseum/treasure/roofs/center_roof", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
