/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;

public class MarsagliaPolarGaussian {
    public final RandomSource randomSource;
    private double nextNextGaussian;
    private boolean haveNextNextGaussian;

    public MarsagliaPolarGaussian(RandomSource randomSource) {
        this.randomSource = randomSource;
    }

    public void reset() {
        this.haveNextNextGaussian = false;
    }

    public double nextGaussian() {
        double e;
        double d;
        double f;
        if (this.haveNextNextGaussian) {
            this.haveNextNextGaussian = false;
            return this.nextNextGaussian;
        }
        do {
            d = 2.0 * this.randomSource.nextDouble() - 1.0;
            e = 2.0 * this.randomSource.nextDouble() - 1.0;
        } while ((f = Mth.square(d) + Mth.square(e)) >= 1.0 || f == 0.0);
        double g = Math.sqrt(-2.0 * Math.log(f) / f);
        this.nextNextGaussian = e * g;
        this.haveNextNextGaussian = true;
        return d * g;
    }
}

