package net.minecraft.data.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class UpdateOneTwentyOnePools {
	public static final ResourceKey<StructureTemplatePool> EMPTY = createKey("empty");

	public static ResourceKey<StructureTemplatePool> createKey(String string) {
		return ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(string));
	}

	public static void register(BootstapContext<StructureTemplatePool> bootstapContext, String string, StructureTemplatePool structureTemplatePool) {
		Pools.register(bootstapContext, string, structureTemplatePool);
	}

	public static void bootstrap(BootstapContext<StructureTemplatePool> bootstapContext) {
		TrialChambersStructurePools.bootstrap(bootstapContext);
	}
}
