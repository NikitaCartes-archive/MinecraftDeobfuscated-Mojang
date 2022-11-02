package net.minecraft.data.worldgen;

import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class VillagePools {
	public static void bootstrap(BootstapContext<StructureTemplatePool> bootstapContext) {
		PlainVillagePools.bootstrap(bootstapContext);
		SnowyVillagePools.bootstrap(bootstapContext);
		SavannaVillagePools.bootstrap(bootstapContext);
		DesertVillagePools.bootstrap(bootstapContext);
		TaigaVillagePools.bootstrap(bootstapContext);
	}
}
