package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;

public class WeightedListInt extends IntProvider {
	public static final Codec<WeightedListInt> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					SimpleWeightedRandomList.wrappedCodec(IntProvider.CODEC).fieldOf("distribution").forGetter(weightedListInt -> weightedListInt.distribution)
				)
				.apply(instance, WeightedListInt::new)
	);
	private final SimpleWeightedRandomList<IntProvider> distribution;
	private final int minValue;
	private final int maxValue;

	public WeightedListInt(SimpleWeightedRandomList<IntProvider> simpleWeightedRandomList) {
		this.distribution = simpleWeightedRandomList;
		List<WeightedEntry.Wrapper<IntProvider>> list = simpleWeightedRandomList.unwrap();
		int i = Integer.MAX_VALUE;
		int j = Integer.MIN_VALUE;

		for (WeightedEntry.Wrapper<IntProvider> wrapper : list) {
			int k = wrapper.getData().getMinValue();
			int l = wrapper.getData().getMaxValue();
			i = Math.min(i, k);
			j = Math.max(j, l);
		}

		this.minValue = i;
		this.maxValue = j;
	}

	@Override
	public int sample(Random random) {
		return ((IntProvider)this.distribution.getRandomValue(random).orElseThrow(IllegalStateException::new)).sample(random);
	}

	@Override
	public int getMinValue() {
		return this.minValue;
	}

	@Override
	public int getMaxValue() {
		return this.maxValue;
	}

	@Override
	public IntProviderType<?> getType() {
		return IntProviderType.WEIGHTED_LIST;
	}
}
