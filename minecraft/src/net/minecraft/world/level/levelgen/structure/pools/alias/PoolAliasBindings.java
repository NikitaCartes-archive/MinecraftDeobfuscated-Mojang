package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class PoolAliasBindings {
	public static MapCodec<? extends PoolAliasBinding> bootstrap(Registry<MapCodec<? extends PoolAliasBinding>> registry) {
		Registry.register(registry, "random", Random.CODEC);
		Registry.register(registry, "random_group", RandomGroup.CODEC);
		return Registry.register(registry, "direct", Direct.CODEC);
	}

	public static void registerTargetsAsPools(
		BootstrapContext<StructureTemplatePool> bootstrapContext, Holder<StructureTemplatePool> holder, List<PoolAliasBinding> list
	) {
		list.stream()
			.flatMap(PoolAliasBinding::allTargets)
			.map(resourceKey -> resourceKey.location().getPath())
			.forEach(
				string -> Pools.register(
						bootstrapContext,
						string,
						new StructureTemplatePool(holder, List.of(Pair.of(StructurePoolElement.single(string), 1)), StructureTemplatePool.Projection.RIGID)
					)
			);
	}
}
