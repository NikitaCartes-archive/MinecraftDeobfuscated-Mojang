package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Pools {
	public static final ResourceKey<StructureTemplatePool> EMPTY = createKey("empty");

	public static ResourceKey<StructureTemplatePool> createKey(ResourceLocation resourceLocation) {
		return ResourceKey.create(Registries.TEMPLATE_POOL, resourceLocation);
	}

	public static ResourceKey<StructureTemplatePool> createKey(String string) {
		return createKey(ResourceLocation.withDefaultNamespace(string));
	}

	public static ResourceKey<StructureTemplatePool> parseKey(String string) {
		return createKey(ResourceLocation.parse(string));
	}

	public static void register(BootstrapContext<StructureTemplatePool> bootstrapContext, String string, StructureTemplatePool structureTemplatePool) {
		bootstrapContext.register(createKey(string), structureTemplatePool);
	}

	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<StructureTemplatePool> holderGetter = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder = holderGetter.getOrThrow(EMPTY);
		bootstrapContext.register(EMPTY, new StructureTemplatePool(holder, ImmutableList.of(), StructureTemplatePool.Projection.RIGID));
		BastionPieces.bootstrap(bootstrapContext);
		PillagerOutpostPools.bootstrap(bootstrapContext);
		VillagePools.bootstrap(bootstrapContext);
		AncientCityStructurePieces.bootstrap(bootstrapContext);
		TrailRuinsStructurePools.bootstrap(bootstrapContext);
		TrialChambersStructurePools.bootstrap(bootstrapContext);
	}
}
