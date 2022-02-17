package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

public class AncientCityStructurePieces {
	public static final StructureTemplatePool START = Pools.register(
		new StructureTemplatePool(
			new ResourceLocation("ancient_city/city_center"),
			new ResourceLocation("empty"),
			ImmutableList.of(Pair.of(StructurePoolElement.single("ancient_city/city_center/city_center", ProcessorLists.ANCIENT_CITY_START_DEGRADATION), 1)),
			StructureTemplatePool.Projection.RIGID
		)
	);

	public static void bootstrap() {
		AncientCityStructurePools.bootstrap();
	}
}
