/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import java.util.Random;
import java.util.function.LongFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class WorldgenRandom
extends Random
implements RandomSource {
    private final RandomSource randomSource;
    private int count;

    public WorldgenRandom(RandomSource randomSource) {
        super(0L);
        this.randomSource = randomSource;
    }

    public int getCount() {
        return this.count;
    }

    @Override
    public RandomSource fork() {
        return this.randomSource.fork();
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return this.randomSource.forkPositional();
    }

    @Override
    public int next(int i) {
        ++this.count;
        RandomSource randomSource = this.randomSource;
        if (randomSource instanceof LegacyRandomSource) {
            LegacyRandomSource legacyRandomSource = (LegacyRandomSource)randomSource;
            return legacyRandomSource.next(i);
        }
        return (int)(this.randomSource.nextLong() >>> 64 - i);
    }

    @Override
    public synchronized void setSeed(long l) {
        if (this.randomSource == null) {
            return;
        }
        this.randomSource.setSeed(l);
    }

    public void setBaseChunkSeed(int i, int j) {
        long l = (long)i * 341873128712L + (long)j * 132897987541L;
        this.setSeed(l);
    }

    public long setDecorationSeed(long l, int i, int j) {
        this.setSeed(l);
        long m = this.nextLong() | 1L;
        long n = this.nextLong() | 1L;
        long o = (long)i * m + (long)j * n ^ l;
        this.setSeed(o);
        return o;
    }

    public void setFeatureSeed(long l, int i, int j) {
        long m = l + (long)i + (long)(10000 * j);
        this.setSeed(m);
    }

    public void setLargeFeatureSeed(long l, int i, int j) {
        this.setSeed(l);
        long m = this.nextLong();
        long n = this.nextLong();
        long o = (long)i * m ^ (long)j * n ^ l;
        this.setSeed(o);
    }

    public void setLargeFeatureWithSalt(long l, int i, int j, int k) {
        long m = (long)i * 341873128712L + (long)j * 132897987541L + l + (long)k;
        this.setSeed(m);
    }

    public static Random seedSlimeChunk(int i, int j, long l, long m) {
        return new Random(l + (long)(i * i * 4987142) + (long)(i * 5947611) + (long)(j * j) * 4392871L + (long)(j * 389711) ^ m);
    }

    public static enum Algorithm {
        LEGACY(LegacyRandomSource::new),
        XOROSHIRO(XoroshiroRandomSource::new);

        private final LongFunction<RandomSource> constructor;

        private Algorithm(LongFunction<RandomSource> longFunction) {
            this.constructor = longFunction;
        }

        public RandomSource newInstance(long l) {
            return this.constructor.apply(l);
        }
    }
}

