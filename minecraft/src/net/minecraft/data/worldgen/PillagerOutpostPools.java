package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class PillagerOutpostPools {
	public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("pillager_outpost/base_plates");

	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<StructureProcessorList> holderGetter = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder = holderGetter.getOrThrow(ProcessorLists.OUTPOST_ROT);
		HolderGetter<StructureTemplatePool> holderGetter2 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder2 = holderGetter2.getOrThrow(Pools.EMPTY);
		bootstrapContext.register(
			START,
			new StructureTemplatePool(
				holder2, ImmutableList.of(Pair.of(StructurePoolElement.legacy("pillager_outpost/base_plate"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"pillager_outpost/towers",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(
						StructurePoolElement.list(
							ImmutableList.of(
								StructurePoolElement.legacy("pillager_outpost/watchtower"), StructurePoolElement.legacy("pillager_outpost/watchtower_overgrown", holder)
							)
						),
						1
					)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"pillager_outpost/feature_plates",
			new StructureTemplatePool(
				holder2, ImmutableList.of(Pair.of(StructurePoolElement.legacy("pillager_outpost/feature_plate"), 1)), StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"pillager_outpost/features",
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("pillager_outpost/feature_cage1"), 1),
					Pair.of(StructurePoolElement.legacy("pillager_outpost/feature_cage2"), 1),
					Pair.of(StructurePoolElement.legacy("pillager_outpost/feature_cage_with_allays"), 1),
					Pair.of(StructurePoolElement.legacy("pillager_outpost/feature_logs"), 1),
					Pair.of(StructurePoolElement.legacy("pillager_outpost/feature_tent1"), 1),
					Pair.of(StructurePoolElement.legacy("pillager_outpost/feature_tent2"), 1),
					Pair.of(StructurePoolElement.legacy("pillager_outpost/feature_targets"), 1),
					Pair.of(StructurePoolElement.empty(), 6)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
