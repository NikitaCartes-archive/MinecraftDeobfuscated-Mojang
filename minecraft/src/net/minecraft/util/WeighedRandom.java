package net.minecraft.util;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WeighedRandom {
	static final Logger LOGGER = LogManager.getLogger();

	public static int getTotalWeight(List<? extends WeighedRandom.WeighedRandomItem> list) {
		long l = 0L;

		for (WeighedRandom.WeighedRandomItem weighedRandomItem : list) {
			l += (long)weighedRandomItem.weight;
		}

		if (l > 2147483647L) {
			throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
		} else {
			return (int)l;
		}
	}

	public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getRandomItem(Random random, List<T> list, int i) {
		if (i < 0) {
			throw (IllegalArgumentException)Util.pauseInIde((T)(new IllegalArgumentException("Negative total weight in getRandomItem")));
		} else if (i == 0) {
			return Optional.empty();
		} else {
			int j = random.nextInt(i);
			return getWeightedItem(list, j);
		}
	}

	public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getWeightedItem(List<T> list, int i) {
		for (T weighedRandomItem : list) {
			i -= weighedRandomItem.weight;
			if (i < 0) {
				return Optional.of(weighedRandomItem);
			}
		}

		return Optional.empty();
	}

	public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getRandomItem(Random random, List<T> list) {
		return getRandomItem(random, list, getTotalWeight(list));
	}

	public static class WeighedRandomItem {
		protected final int weight;

		public WeighedRandomItem(int i) {
			if (i < 0) {
				throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Weight should be >= 0"));
			} else {
				if (i == 0 && SharedConstants.IS_RUNNING_IN_IDE) {
					WeighedRandom.LOGGER.warn("Found 0 weight, make sure this is intentional!");
				}

				this.weight = i;
			}
		}
	}
}
