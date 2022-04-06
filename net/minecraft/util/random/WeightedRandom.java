/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.random;

import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;

public class WeightedRandom {
    private WeightedRandom() {
    }

    public static int getTotalWeight(List<? extends WeightedEntry> list) {
        long l = 0L;
        for (WeightedEntry weightedEntry : list) {
            l += (long)weightedEntry.getWeight().asInt();
        }
        if (l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        }
        return (int)l;
    }

    public static <T extends WeightedEntry> Optional<T> getRandomItem(RandomSource randomSource, List<T> list, int i) {
        if (i < 0) {
            throw Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
        }
        if (i == 0) {
            return Optional.empty();
        }
        int j = randomSource.nextInt(i);
        return WeightedRandom.getWeightedItem(list, j);
    }

    public static <T extends WeightedEntry> Optional<T> getWeightedItem(List<T> list, int i) {
        for (WeightedEntry weightedEntry : list) {
            if ((i -= weightedEntry.getWeight().asInt()) >= 0) continue;
            return Optional.of(weightedEntry);
        }
        return Optional.empty();
    }

    public static <T extends WeightedEntry> Optional<T> getRandomItem(RandomSource randomSource, List<T> list) {
        return WeightedRandom.getRandomItem(randomSource, list, WeightedRandom.getTotalWeight(list));
    }
}

