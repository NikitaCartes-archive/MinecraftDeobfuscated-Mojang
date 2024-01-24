package net.minecraft.data.worldgen;

import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class VillagePools {
	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		PlainVillagePools.bootstrap(bootstrapContext);
		SnowyVillagePools.bootstrap(bootstrapContext);
		SavannaVillagePools.bootstrap(bootstrapContext);
		DesertVillagePools.bootstrap(bootstrapContext);
		TaigaVillagePools.bootstrap(bootstrapContext);
	}
}
