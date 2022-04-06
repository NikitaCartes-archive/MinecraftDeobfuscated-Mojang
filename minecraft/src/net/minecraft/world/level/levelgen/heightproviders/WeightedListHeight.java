package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class WeightedListHeight extends HeightProvider {
	public static final Codec<WeightedListHeight> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					SimpleWeightedRandomList.wrappedCodec(HeightProvider.CODEC).fieldOf("distribution").forGetter(weightedListHeight -> weightedListHeight.distribution)
				)
				.apply(instance, WeightedListHeight::new)
	);
	private final SimpleWeightedRandomList<HeightProvider> distribution;

	public WeightedListHeight(SimpleWeightedRandomList<HeightProvider> simpleWeightedRandomList) {
		this.distribution = simpleWeightedRandomList;
	}

	@Override
	public int sample(RandomSource randomSource, WorldGenerationContext worldGenerationContext) {
		return ((HeightProvider)this.distribution.getRandomValue(randomSource).orElseThrow(IllegalStateException::new)).sample(randomSource, worldGenerationContext);
	}

	@Override
	public HeightProviderType<?> getType() {
		return HeightProviderType.WEIGHTED_LIST;
	}
}
