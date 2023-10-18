package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public interface PoolAliasBinding {
	Codec<PoolAliasBinding> CODEC = BuiltInRegistries.POOL_ALIAS_BINDING_TYPE.byNameCodec().dispatch(PoolAliasBinding::codec, Function.identity());

	void forEachResolved(RandomSource randomSource, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> biConsumer);

	Stream<ResourceKey<StructureTemplatePool>> allTargets();

	static Direct direct(String string, String string2) {
		return direct(Pools.createKey(string), Pools.createKey(string2));
	}

	static Direct direct(ResourceKey<StructureTemplatePool> resourceKey, ResourceKey<StructureTemplatePool> resourceKey2) {
		return new Direct(resourceKey, resourceKey2);
	}

	static Random random(String string, SimpleWeightedRandomList<String> simpleWeightedRandomList) {
		SimpleWeightedRandomList.Builder<ResourceKey<StructureTemplatePool>> builder = SimpleWeightedRandomList.builder();
		simpleWeightedRandomList.unwrap().forEach(wrapper -> builder.add(Pools.createKey((String)wrapper.getData()), wrapper.getWeight().asInt()));
		return random(Pools.createKey(string), builder.build());
	}

	static Random random(ResourceKey<StructureTemplatePool> resourceKey, SimpleWeightedRandomList<ResourceKey<StructureTemplatePool>> simpleWeightedRandomList) {
		return new Random(resourceKey, simpleWeightedRandomList);
	}

	static RandomGroup randomGroup(SimpleWeightedRandomList<List<PoolAliasBinding>> simpleWeightedRandomList) {
		return new RandomGroup(simpleWeightedRandomList);
	}

	Codec<? extends PoolAliasBinding> codec();
}
