package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class AncientCityStructurePieces {
	public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("ancient_city/city_center");

	public static void bootstrap(BootstapContext<StructureTemplatePool> bootstapContext) {
		HolderGetter<StructureProcessorList> holderGetter = bootstapContext.lookup(Registry.PROCESSOR_LIST_REGISTRY);
		Holder<StructureProcessorList> holder = holderGetter.getOrThrow(ProcessorLists.ANCIENT_CITY_START_DEGRADATION);
		HolderGetter<StructureTemplatePool> holderGetter2 = bootstapContext.lookup(Registry.TEMPLATE_POOL_REGISTRY);
		Holder<StructureTemplatePool> holder2 = holderGetter2.getOrThrow(Pools.EMPTY);
		bootstapContext.register(
			START,
			new StructureTemplatePool(
				holder2,
				ImmutableList.of(
					Pair.of(StructurePoolElement.single("ancient_city/city_center/city_center_1", holder), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/city_center_2", holder), 1),
					Pair.of(StructurePoolElement.single("ancient_city/city_center/city_center_3", holder), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		AncientCityStructurePools.bootstrap(bootstapContext);
	}
}
