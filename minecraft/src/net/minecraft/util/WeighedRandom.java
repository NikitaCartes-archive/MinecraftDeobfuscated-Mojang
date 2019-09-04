package net.minecraft.util;

import java.util.List;
import java.util.Random;
import net.minecraft.Util;

public class WeighedRandom {
	public static int getTotalWeight(List<? extends WeighedRandom.WeighedRandomItem> list) {
		int i = 0;
		int j = 0;

		for (int k = list.size(); j < k; j++) {
			WeighedRandom.WeighedRandomItem weighedRandomItem = (WeighedRandom.WeighedRandomItem)list.get(j);
			i += weighedRandomItem.weight;
		}

		return i;
	}

	public static <T extends WeighedRandom.WeighedRandomItem> T getRandomItem(Random random, List<T> list, int i) {
		if (i <= 0) {
			throw (IllegalArgumentException)Util.pauseInIde((T)(new IllegalArgumentException()));
		} else {
			int j = random.nextInt(i);
			return getWeightedItem(list, j);
		}
	}

	public static <T extends WeighedRandom.WeighedRandomItem> T getWeightedItem(List<T> list, int i) {
		int j = 0;

		for (int k = list.size(); j < k; j++) {
			T weighedRandomItem = (T)list.get(j);
			i -= weighedRandomItem.weight;
			if (i < 0) {
				return weighedRandomItem;
			}
		}

		return null;
	}

	public static <T extends WeighedRandom.WeighedRandomItem> T getRandomItem(Random random, List<T> list) {
		return getRandomItem(random, list, getTotalWeight(list));
	}

	public static class WeighedRandomItem {
		protected final int weight;

		public WeighedRandomItem(int i) {
			this.weight = i;
		}
	}
}
