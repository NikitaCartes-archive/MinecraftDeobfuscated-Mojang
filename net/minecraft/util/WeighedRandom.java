/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WeighedRandom {
    private static final Logger LOGGER = LogManager.getLogger();

    public static int getTotalWeight(List<? extends WeighedRandomItem> list) {
        long l = 0L;
        for (WeighedRandomItem weighedRandomItem : list) {
            l += (long)weighedRandomItem.weight;
        }
        if (l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        }
        return (int)l;
    }

    public static <T extends WeighedRandomItem> Optional<T> getRandomItem(Random random, List<T> list, int i) {
        if (i < 0) {
            throw Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
        }
        if (i == 0) {
            return Optional.empty();
        }
        int j = random.nextInt(i);
        return WeighedRandom.getWeightedItem(list, j);
    }

    public static <T extends WeighedRandomItem> Optional<T> getWeightedItem(List<T> list, int i) {
        for (WeighedRandomItem weighedRandomItem : list) {
            if ((i -= weighedRandomItem.weight) >= 0) continue;
            return Optional.of(weighedRandomItem);
        }
        return Optional.empty();
    }

    public static <T extends WeighedRandomItem> Optional<T> getRandomItem(Random random, List<T> list) {
        return WeighedRandom.getRandomItem(random, list, WeighedRandom.getTotalWeight(list));
    }

    public static class WeighedRandomItem {
        protected final int weight;

        public WeighedRandomItem(int i) {
            if (i < 0) {
                throw Util.pauseInIde(new IllegalArgumentException("Weight should be >= 0"));
            }
            if (i == 0 && SharedConstants.IS_RUNNING_IN_IDE) {
                LOGGER.warn("Found 0 weight, make sure this is intentional!");
            }
            this.weight = i;
        }
    }
}

