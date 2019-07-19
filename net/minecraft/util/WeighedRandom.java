/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.List;
import java.util.Random;

public class WeighedRandom {
    public static int getTotalWeight(List<? extends WeighedRandomItem> list) {
        int i = 0;
        int k = list.size();
        for (int j = 0; j < k; ++j) {
            WeighedRandomItem weighedRandomItem = list.get(j);
            i += weighedRandomItem.weight;
        }
        return i;
    }

    public static <T extends WeighedRandomItem> T getRandomItem(Random random, List<T> list, int i) {
        if (i <= 0) {
            throw new IllegalArgumentException();
        }
        int j = random.nextInt(i);
        return WeighedRandom.getWeightedItem(list, j);
    }

    public static <T extends WeighedRandomItem> T getWeightedItem(List<T> list, int i) {
        int k = list.size();
        for (int j = 0; j < k; ++j) {
            WeighedRandomItem weighedRandomItem = (WeighedRandomItem)list.get(j);
            if ((i -= weighedRandomItem.weight) >= 0) continue;
            return (T)weighedRandomItem;
        }
        return null;
    }

    public static <T extends WeighedRandomItem> T getRandomItem(Random random, List<T> list) {
        return WeighedRandom.getRandomItem(random, list, WeighedRandom.getTotalWeight(list));
    }

    public static class WeighedRandomItem {
        protected final int weight;

        public WeighedRandomItem(int i) {
            this.weight = i;
        }
    }
}

