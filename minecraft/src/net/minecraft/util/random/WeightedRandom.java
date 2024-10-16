package net.minecraft.util.random;

import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;

public class WeightedRandom {
	private WeightedRandom() {
	}

	public static int getTotalWeight(List<? extends WeightedEntry> list) {
		long l = 0L;

		for (WeightedEntry weightedEntry : list) {
			l += (long)weightedEntry.getWeight().asInt();
		}

		if (l > 2147483647L) {
			throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
		} else {
			return (int)l;
		}
	}

	public static <T extends WeightedEntry> Optional<T> getRandomItem(RandomSource randomSource, List<T> list, int i) {
		if (i < 0) {
			throw (IllegalArgumentException)Util.pauseInIde((T)(new IllegalArgumentException("Negative total weight in getRandomItem")));
		} else if (i == 0) {
			return Optional.empty();
		} else {
			int j = randomSource.nextInt(i);
			return getWeightedItem(list, j);
		}
	}

	public static <T extends WeightedEntry> Optional<T> getWeightedItem(List<T> list, int i) {
		for (T weightedEntry : list) {
			i -= weightedEntry.getWeight().asInt();
			if (i < 0) {
				return Optional.of(weightedEntry);
			}
		}

		return Optional.empty();
	}

	public static <T extends WeightedEntry> Optional<T> getRandomItem(RandomSource randomSource, List<T> list) {
		return getRandomItem(randomSource, list, getTotalWeight(list));
	}
}
