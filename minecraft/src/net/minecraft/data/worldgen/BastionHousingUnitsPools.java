package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionHousingUnitsPools {
	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<StructureProcessorList> holderGetter = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder = holderGetter.getOrThrow(ProcessorLists.HOUSING);
		HolderGetter<StructureTemplatePool> holderGetter2 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder2 = holderGetter2.getOrThrow(Pools.EMPTY);
		Pools.register(
			bootstrapContext,
			"bastion/units/center_pieces",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/units/center_pieces/center_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/center_pieces/center_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/center_pieces/center_2", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/pathways",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/units/pathways/pathway_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/pathways/pathway_wall_0", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/walls/wall_bases",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/units/walls/wall_base", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/walls/connected_wall", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/stages/stage_0",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_3", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/stages/stage_1",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_3", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/stages/rot/stage_1",
			new StructureTemplatePool(
				holder2, ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/stages/rot/stage_1_0", holder), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/stages/stage_2",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_2_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_2_1", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/stages/stage_3",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_3", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/fillers/stage_0",
			new StructureTemplatePool(
				holder2, ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/fillers/stage_0", holder), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/edges",
			new StructureTemplatePool(
				holder2, ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/edges/edge_0", holder), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/wall_units",
			new StructureTemplatePool(
				holder2, ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/wall_units/unit_0", holder), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/edge_wall_units",
			new StructureTemplatePool(
				holder2, ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/wall_units/edge_0_large", holder), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/ramparts",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_2", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/large_ramparts",
			new StructureTemplatePool(
				holder2, ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_0", holder), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/units/rampart_plates",
			new StructureTemplatePool(
				holder2, ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/rampart_plates/plate_0", holder), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
