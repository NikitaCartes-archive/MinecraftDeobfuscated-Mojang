/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.ThreadSafeLegacyRandomSource;

public interface RandomSource {
    public static RandomSource create() {
        return RandomSource.create(System.nanoTime());
    }

    @Deprecated
    public static RandomSource createThreadSafe() {
        return new ThreadSafeLegacyRandomSource(System.nanoTime());
    }

    public static RandomSource create(long l) {
        return new LegacyRandomSource(l);
    }

    public static RandomSource createNewThreadLocalInstance() {
        return new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong());
    }

    public RandomSource fork();

    public PositionalRandomFactory forkPositional();

    public void setSeed(long var1);

    public int nextInt();

    public int nextInt(int var1);

    default public int nextIntBetweenInclusive(int i, int j) {
        return this.nextInt(j - i + 1) + i;
    }

    public long nextLong();

    public boolean nextBoolean();

    public float nextFloat();

    public double nextDouble();

    public double nextGaussian();

    default public void consumeCount(int i) {
        for (int j = 0; j < i; ++j) {
            this.nextInt();
        }
    }

    default public int nextInt(int i, int j) {
        if (i >= j) {
            throw new IllegalArgumentException("bound - origin is non positive");
        }
        return i + this.nextInt(j - i);
    }
}

