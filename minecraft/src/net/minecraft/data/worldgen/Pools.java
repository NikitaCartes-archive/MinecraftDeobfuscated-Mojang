package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Pools {
	public static final ResourceKey<StructureTemplatePool> EMPTY = createKey("empty");

	public static ResourceKey<StructureTemplatePool> createKey(String string) {
		return ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, new ResourceLocation(string));
	}

	public static void register(BootstapContext<StructureTemplatePool> bootstapContext, String string, StructureTemplatePool structureTemplatePool) {
		bootstapContext.register(createKey(string), structureTemplatePool);
	}

	public static void bootstrap(BootstapContext<StructureTemplatePool> bootstapContext) {
		HolderGetter<StructureTemplatePool> holderGetter = bootstapContext.lookup(Registry.TEMPLATE_POOL_REGISTRY);
		Holder<StructureTemplatePool> holder = holderGetter.getOrThrow(EMPTY);
		bootstapContext.register(EMPTY, new StructureTemplatePool(holder, ImmutableList.of(), StructureTemplatePool.Projection.RIGID));
		BastionPieces.bootstrap(bootstapContext);
		PillagerOutpostPools.bootstrap(bootstapContext);
		VillagePools.bootstrap(bootstapContext);
		AncientCityStructurePieces.bootstrap(bootstapContext);
	}
}
