package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

@FunctionalInterface
public interface PoolAliasLookup {
	PoolAliasLookup EMPTY = resourceKey -> resourceKey;

	ResourceKey<StructureTemplatePool> lookup(ResourceKey<StructureTemplatePool> resourceKey);

	static PoolAliasLookup create(List<PoolAliasBinding> list, BlockPos blockPos, long l) {
		if (list.isEmpty()) {
			return EMPTY;
		} else {
			RandomSource randomSource = RandomSource.create(l).forkPositional().at(blockPos);
			Builder<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> builder = ImmutableMap.builder();
			list.forEach(poolAliasBinding -> poolAliasBinding.forEachResolved(randomSource, builder::put));
			Map<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> map = builder.build();
			return resourceKey -> (ResourceKey<StructureTemplatePool>)Objects.requireNonNull(
					(ResourceKey)map.getOrDefault(resourceKey, resourceKey), () -> "alias " + resourceKey + " was mapped to null value"
				);
		}
	}
}
