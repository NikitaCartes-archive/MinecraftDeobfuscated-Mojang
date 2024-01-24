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

public class BastionPieces {
	public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("bastion/starts");

	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<StructureProcessorList> holderGetter = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder = holderGetter.getOrThrow(ProcessorLists.BASTION_GENERIC_DEGRADATION);
		HolderGetter<StructureTemplatePool> holderGetter2 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder2 = holderGetter2.getOrThrow(Pools.EMPTY);
		bootstrapContext.register(
			START,
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("bastion/units/air_base", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/hoglin_stable/air_base", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/treasure/big_air_full", holder), 1),
					Pair.of(StructurePoolElement.single("bastion/bridge/starting_pieces/entrance_base", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		BastionHousingUnitsPools.bootstrap(bootstrapContext);
		BastionHoglinStablePools.bootstrap(bootstrapContext);
		BastionTreasureRoomPools.bootstrap(bootstrapContext);
		BastionBridgePools.bootstrap(bootstrapContext);
		BastionSharedPools.bootstrap(bootstrapContext);
	}
}
