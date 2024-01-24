package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionHoglinStablePools {
	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<StructureProcessorList> holderGetter = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder = holderGetter.getOrThrow(ProcessorLists.STABLE_DEGRADATION);
		Holder<StructureProcessorList> holder2 = holderGetter.getOrThrow(ProcessorLists.SIDE_WALL_DEGRADATION);
		HolderGetter<StructureTemplatePool> holderGetter2 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder3 = holderGetter2.getOrThrow(Pools.EMPTY);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/starting_pieces",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/starting_stairs_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/starting_stairs_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/starting_stairs_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/starting_stairs_3", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/starting_stairs_4", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/mirrored_starting_pieces",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/stairs_0_mirrored", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/stairs_1_mirrored", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/stairs_2_mirrored", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/stairs_3_mirrored", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/starting_pieces/stairs_4_mirrored", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/wall_bases",
			new StructureTemplatePool(
				holder3, ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/hoglin_stable/walls/wall_base", holder), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/walls",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/walls/side_wall_0", holder2), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/walls/side_wall_1", holder2), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/stairs",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_1_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_1_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_1_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_1_3", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_1_4", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_2_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_2_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_2_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_2_3", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_2_4", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_3_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_3_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_3_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_3_3", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/stairs/stairs_3_4", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/small_stables/inner",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/inner_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/inner_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/inner_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/inner_3", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/small_stables/outer",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/outer_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/outer_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/outer_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/small_stables/outer_3", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/large_stables/inner",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/inner_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/inner_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/inner_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/inner_3", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/inner_4", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/large_stables/outer",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/outer_0", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/outer_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/outer_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/outer_3", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/large_stables/outer_4", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/posts",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/posts/stair_post", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/posts/end_post", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/ramparts",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/ramparts/ramparts_1", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/ramparts/ramparts_2", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/ramparts/ramparts_3", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/rampart_plates",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/hoglin_stable/rampart_plates/rampart_plate_1", holder), 1)),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"bastion/hoglin_stable/connectors",
			new StructureTemplatePool(
				holder3,
				ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/hoglin_stable/connectors/end_post_connector", holder), 1)),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
