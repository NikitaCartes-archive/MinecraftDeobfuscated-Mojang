package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

record RandomGroup(SimpleWeightedRandomList<List<PoolAliasBinding>> groups) implements PoolAliasBinding {
	static Codec<RandomGroup> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(SimpleWeightedRandomList.wrappedCodec(Codec.list(PoolAliasBinding.CODEC)).fieldOf("groups").forGetter(RandomGroup::groups))
				.apply(instance, RandomGroup::new)
	);

	@Override
	public void forEachResolved(RandomSource randomSource, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> biConsumer) {
		this.groups
			.getRandom(randomSource)
			.ifPresent(wrapper -> ((List)wrapper.getData()).forEach(poolAliasBinding -> poolAliasBinding.forEachResolved(randomSource, biConsumer)));
	}

	@Override
	public Stream<ResourceKey<StructureTemplatePool>> allTargets() {
		return this.groups.unwrap().stream().flatMap(wrapper -> ((List)wrapper.getData()).stream()).flatMap(PoolAliasBinding::allTargets);
	}

	@Override
	public Codec<RandomGroup> codec() {
		return CODEC;
	}
}
