/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.BitRandomSource;
import net.minecraft.world.level.levelgen.RandomSource;

public class SingleThreadedRandomSource
implements BitRandomSource {
    private static final int MODULUS_BITS = 48;
    private static final long MODULUS_MASK = 0xFFFFFFFFFFFFL;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private long seed;
    private double nextNextGaussian;
    private boolean haveNextNextGaussian;

    public SingleThreadedRandomSource(long l) {
        this.setSeed(l);
    }

    @Override
    public RandomSource fork() {
        return new SingleThreadedRandomSource(this.nextLong());
    }

    @Override
    public void setSeed(long l) {
        this.seed = (l ^ 0x5DEECE66DL) & 0xFFFFFFFFFFFFL;
    }

    @Override
    public int next(int i) {
        long l;
        this.seed = l = this.seed * 25214903917L + 11L & 0xFFFFFFFFFFFFL;
        return (int)(l >> 48 - i);
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

