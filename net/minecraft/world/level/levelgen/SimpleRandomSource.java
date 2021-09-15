/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Mth;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.world.level.levelgen.BitRandomSource;
import net.minecraft.world.level.levelgen.RandomSource;

public class SimpleRandomSource
implements BitRandomSource {
    private static final int MODULUS_BITS = 48;
    private static final long MODULUS_MASK = 0xFFFFFFFFFFFFL;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private final AtomicLong seed = new AtomicLong();
    private double nextNextGaussian;
    private boolean haveNextNextGaussian;

    public SimpleRandomSource(long l) {
        this.setSeed(l);
    }

    @Override
    public RandomSource fork() {
        return new SimpleRandomSource(this.nextLong());
    }

    @Override
    public void setSeed(long l) {
        if (!this.seed.compareAndSet(this.seed.get(), (l ^ 0x5DEECE66DL) & 0xFFFFFFFFFFFFL)) {
            throw ThreadingDetector.makeThreadingException("SimpleRandomSource", null);
        }
    }

    @Override
    public int next(int i) {
        long m;
        long l = this.seed.get();
        if (!this.seed.compareAndSet(l, m = l * 25214903917L + 11L & 0xFFFFFFFFFFFFL)) {
            throw ThreadingDetector.makeThreadingException("SimpleRandomSource", null);
        }
        return (int)(m >> 48 - i);
    }

    @Override
    public double nextGaussian() {
        double e;
        double d;
        double f;
        if (this.haveNextNextGaussian) {
            this.haveNextNextGaussian = false;
            return this.nextNextGaussian;
        }
        do {
            d = 2.0 * this.nextDouble() - 1.0;
            e = 2.0 * this.nextDouble() - 1.0;
        } while ((f = Mth.square(d) + Mth.square(e)) >= 1.0 || f == 0.0);
        double g = Math.sqrt(-2.0 * Math.log(f) / f);
        this.nextNextGaussian = e * g;
        this.haveNextNextGaussian = true;
        return d * g;
    }
}

