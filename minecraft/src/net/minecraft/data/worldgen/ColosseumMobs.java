package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ColosseumMobs {
	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		Holder<StructureTemplatePool> holder = bootstrapContext.lookup(Registries.TEMPLATE_POOL).getOrThrow(Pools.EMPTY);
		Pools.register(
			bootstrapContext,
			"colosseum/mobs/toxifin",
			new StructureTemplatePool(
				holder, ImmutableList.of(Pair.of(StructurePoolElement.single("colosseum/mobs/toxifin"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/mobs/plaguewhale",
			new StructureTemplatePool(
				holder, ImmutableList.of(Pair.of(StructurePoolElement.single("colosseum/mobs/plaguewhale"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"colosseum/mobs/mega_spud",
			new StructureTemplatePool(
				holder, ImmutableList.of(Pair.of(StructurePoolElement.single("colosseum/mobs/mega_spud"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
