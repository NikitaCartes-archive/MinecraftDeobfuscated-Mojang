package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;

public class WeightedRandomList<E extends WeightedEntry> {
	private final int totalWeight;
	private final ImmutableList<E> items;

	WeightedRandomList(List<? extends E> list) {
		this.items = ImmutableList.copyOf(list);
		this.totalWeight = WeightedRandom.getTotalWeight(list);
	}

	public static <E extends WeightedEntry> WeightedRandomList<E> create() {
		return new WeightedRandomList<>(ImmutableList.of());
	}

	@SafeVarargs
	public static <E extends WeightedEntry> WeightedRandomList<E> create(E... weightedEntrys) {
		return new WeightedRandomList<>(ImmutableList.copyOf(weightedEntrys));
	}

	public static <E extends WeightedEntry> WeightedRandomList<E> create(List<E> list) {
		return new WeightedRandomList<>(list);
	}

	public boolean isEmpty() {
		return this.items.isEmpty();
	}

	public Optional<E> getRandom(RandomSource randomSource) {
		if (this.totalWeight == 0) {
			return Optional.empty();
		} else {
			int i = randomSource.nextInt(this.totalWeight);
			return WeightedRandom.getWeightedItem(this.items, i);
		}
	}

	public Optional<E> getRandomUnweighted(RandomSource randomSource) {
		return Util.getRandomSafe(this.items, randomSource);
	}

	public List<E> unwrap() {
		return this.items;
	}

	public static <E extends WeightedEntry> Codec<WeightedRandomList<E>> codec(Codec<E> codec) {
		return codec.listOf().xmap(WeightedRandomList::create, WeightedRandomList::unwrap);
	}
}
